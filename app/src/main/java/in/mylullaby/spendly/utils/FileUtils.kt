package `in`.mylullaby.spendly.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

/**
 * Utility object for file operations related to receipt storage.
 * Handles file paths, validation, and storage management.
 */
object FileUtils {

    /**
     * Maximum file size allowed for receipts (5MB in bytes)
     */
    const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L // 5MB

    /**
     * Returns the directory for storing receipt files in internal storage.
     * Creates the directory if it doesn't exist.
     *
     * @param context Application context
     * @return File object representing the receipts directory
     */
    fun getReceiptsDirectory(context: Context): File {
        val receiptsDir = File(context.filesDir, "receipts")
        if (!receiptsDir.exists()) {
            receiptsDir.mkdirs()
        }
        return receiptsDir
    }

    /**
     * Generates a unique filename for a receipt.
     *
     * @param expenseId The ID of the expense this receipt belongs to
     * @param timestamp Current timestamp for uniqueness
     * @param extension File extension (jpg, png, webp, pdf)
     * @return Generated filename (e.g., "receipt_123_1638123456789.jpg")
     */
    fun generateReceiptFileName(expenseId: Long, timestamp: Long, extension: String): String {
        return "receipt_${expenseId}_${timestamp}.${extension.lowercase()}"
    }

    /**
     * Validates if a file size is within the allowed limit.
     *
     * @param sizeBytes File size in bytes
     * @return true if size is valid (≤ 5MB), false otherwise
     */
    fun validateFileSize(sizeBytes: Long): Boolean {
        return sizeBytes > 0 && sizeBytes <= MAX_FILE_SIZE_BYTES
    }

    /**
     * Gets the file extension from a URI.
     * Attempts to determine extension from MIME type first, then from display name.
     *
     * @param uri The content URI
     * @param context Application context
     * @return File extension (e.g., "jpg", "pdf") or "unknown" if not determinable
     */
    fun getFileExtension(uri: Uri, context: Context): String {
        // For file:// URIs, check the path directly
        if (uri.scheme == "file") {
            uri.path?.let { path ->
                val lastDot = path.lastIndexOf('.')
                if (lastDot > 0) {
                    return path.substring(lastDot + 1).lowercase()
                }
            }
        }

        // First try MIME type
        context.contentResolver.getType(uri)?.let { mimeType ->
            when {
                mimeType.contains("jpeg") -> return "jpg"
                mimeType.contains("jpg") -> return "jpg"
                mimeType.contains("png") -> return "png"
                mimeType.contains("webp") -> return "webp"
                mimeType.contains("pdf") -> return "pdf"
            }
        }

        // Fallback to display name
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                val fileName = cursor.getString(nameIndex)
                val lastDot = fileName.lastIndexOf('.')
                if (lastDot > 0) {
                    return fileName.substring(lastDot + 1).lowercase()
                }
            }
        }

        return "unknown"
    }

    /**
     * Gets the file size from a content URI.
     *
     * @param uri The content URI
     * @param context Application context
     * @return File size in bytes, or -1 if not determinable
     */
    fun getFileSizeFromUri(uri: Uri, context: Context): Long {
        // For file:// URIs, get size directly from file
        if (uri.scheme == "file") {
            uri.path?.let { path ->
                return File(path).length()
            }
        }

        // For content:// URIs, query ContentResolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getLong(sizeIndex)
            }
        }
        return -1L
    }

    /**
     * Deletes a receipt file from internal storage.
     *
     * SECURITY: Validates canonical path to prevent path traversal attacks.
     * Malicious paths like "../../sensitive_file" are rejected.
     *
     * @param context Application context
     * @param filePath Relative path of the file (e.g., "receipts/receipt_123_1638123456789.jpg")
     * @return true if deletion succeeded or file doesn't exist, false on failure or invalid path
     */
    fun deleteReceiptFile(context: Context, filePath: String): Boolean {
        return try {
            // Determine receipts directory
            val receiptsDir = File(context.filesDir, "receipts")
            val file = File(receiptsDir, filePath)

            // CRITICAL SECURITY CHECK: Validate canonical path stays within receipts directory
            // This prevents path traversal attacks (e.g., "../../sensitive_file")
            val receiptsDirCanonical = receiptsDir.canonicalPath
            val fileCanonical = file.canonicalPath

            if (!fileCanonical.startsWith(receiptsDirCanonical)) {
                android.util.Log.e("FileUtils", "Path traversal attempt detected: $filePath")
                return false
            }

            // Safe to delete
            if (file.exists()) {
                file.delete()
            } else {
                true // File doesn't exist, consider it deleted
            }
        } catch (e: Exception) {
            android.util.Log.e("FileUtils", "Failed to delete receipt: $filePath", e)
            false
        }
    }

    /**
     * Checks if there's enough storage space available.
     *
     * @param context Application context
     * @param requiredBytes Required space in bytes
     * @return true if enough space is available
     */
    fun hasEnoughStorage(context: Context, requiredBytes: Long): Boolean {
        val filesDir = context.filesDir
        return filesDir.usableSpace >= requiredBytes
    }

    /**
     * Formats file size for display.
     *
     * @param sizeBytes File size in bytes
     * @return Formatted string (e.g., "1.2 MB", "500 KB")
     */
    fun formatFileSize(sizeBytes: Long): String {
        return when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> String.format("%.1f KB", sizeBytes / 1024.0)
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }
    }

    /**
     * Validates if a file type is supported for receipts.
     * DEPRECATED: Use validateReceiptFile() instead for comprehensive validation.
     *
     * @param extension File extension (jpg, png, webp, pdf)
     * @return true if supported
     */
    @Deprecated("Use validateReceiptFile() for comprehensive validation including magic numbers")
    fun isSupportedFileType(extension: String): Boolean {
        return extension.lowercase() in setOf("jpg", "jpeg", "png", "webp", "pdf")
    }

    /**
     * Comprehensive validation of a receipt file.
     *
     * SECURITY: Validates file extension, magic numbers, and size.
     * Prevents malicious files disguised with wrong extensions.
     *
     * @param file The file to validate
     * @return ValidationResult with detailed failure reason
     */
    fun validateReceiptFile(file: File): FileTypeValidator.ValidationResult {
        return FileTypeValidator.validateReceiptFile(file, MAX_FILE_SIZE_BYTES)
    }
}
