package dev.lanthoor.spendly.ui.screens.expenses

import android.content.Context
import android.net.Uri
import dev.lanthoor.spendly.domain.model.Receipt
import dev.lanthoor.spendly.domain.repository.ReceiptRepository
import dev.lanthoor.spendly.utils.FileTypeValidator
import dev.lanthoor.spendly.utils.FileUtils
import dev.lanthoor.spendly.utils.ImageCompressor
import java.io.File

class ExpenseReceiptService(
    private val receiptRepository: ReceiptRepository
) {
    suspend fun addReceipt(
        context: Context,
        expenseId: Long,
        sourceUri: Uri
    ): Result<Receipt> {
        var tempFile: File? = null
        try {
            if (expenseId == 0L) {
                return Result.failure(Exception("Please save the expense before adding receipts"))
            }

            val extension = FileUtils.getFileExtension(sourceUri, context)
            tempFile = FileUtils.copyUriToTempFile(context, sourceUri, extension)
                ?: return Result.failure(Exception("Failed to read file"))

            val validationResult = FileUtils.validateReceiptFile(tempFile)
            if (validationResult != FileTypeValidator.ValidationResult.Valid) {
                return Result.failure(Exception(validationResult.getErrorMessage()))
            }

            val fileSize = tempFile.length()
            if (!FileUtils.hasEnoughStorage(context, fileSize + (1024 * 1024))) {
                return Result.failure(Exception("Not enough storage space"))
            }

            val timestamp = System.currentTimeMillis()
            val fileName = FileUtils.generateReceiptFileName(expenseId, timestamp, extension)
            val receiptsDir = FileUtils.getReceiptsDirectory(context)
            val destFile = File(receiptsDir, fileName)

            val compressionResult = ImageCompressor.compressImage(
                context = context,
                sourceUri = sourceUri,
                destFile = destFile,
                fileExtension = extension
            )

            if (!compressionResult.success) {
                return Result.failure(
                    Exception(compressionResult.error ?: "Failed to process file")
                )
            }

            val receipt = Receipt(
                expenseId = expenseId,
                filePath = "receipts/$fileName",
                fileType = extension.uppercase(),
                fileSizeBytes = compressionResult.fileSizeBytes,
                compressed = compressionResult.wasCompressed
            )

            val receiptId = receiptRepository.insertReceipt(receipt)
            return Result.success(receipt.copy(id = receiptId))
        } catch (e: Exception) {
            return Result.failure(e)
        } finally {
            tempFile?.delete()
        }
    }

    suspend fun deleteReceipt(receipt: Receipt): Result<Unit> {
        return try {
            receiptRepository.deleteReceipt(receipt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
