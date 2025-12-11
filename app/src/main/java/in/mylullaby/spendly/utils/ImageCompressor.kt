package `in`.mylullaby.spendly.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Utility object for compressing images to meet receipt storage requirements.
 * Compresses images to max 1920px dimension, 85% JPEG quality, and max 5MB size.
 */
object ImageCompressor {

    /**
     * Maximum dimension (width or height) for compressed images
     */
    private const val MAX_DIMENSION = 1920

    /**
     * JPEG compression quality (0-100)
     */
    private const val COMPRESSION_QUALITY = 85

    /**
     * Result of compression operation
     */
    data class CompressionResult(
        val success: Boolean,
        val fileSizeBytes: Long,
        val wasCompressed: Boolean,
        val error: String? = null
    )

    /**
     * Compresses an image file to meet receipt requirements.
     * PDFs are copied without compression.
     *
     * @param context Application context
     * @param sourceUri URI of the source file
     * @param destFile Destination file for compressed image
     * @param fileExtension File extension (jpg, png, webp, pdf)
     * @return CompressionResult with operation status
     */
    suspend fun compressImage(
        context: Context,
        sourceUri: Uri,
        destFile: File,
        fileExtension: String
    ): CompressionResult = withContext(Dispatchers.IO) {
        try {
            // PDFs don't need compression - just copy
            if (fileExtension.lowercase() == "pdf") {
                return@withContext copyFile(context, sourceUri, destFile)
            }

            // Load bitmap with proper orientation
            val bitmap = loadBitmapFromUri(context, sourceUri)
                ?: return@withContext CompressionResult(
                    success = false,
                    fileSizeBytes = 0,
                    wasCompressed = false,
                    error = "Failed to decode image"
                )

            // Check if compression is needed
            val needsCompression = bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION

            // Resize if needed
            val resizedBitmap = if (needsCompression) {
                resizeBitmap(bitmap)
            } else {
                bitmap
            }

            // Compress and save
            var quality = COMPRESSION_QUALITY
            var fileSize: Long

            do {
                FileOutputStream(destFile).use { out ->
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
                }
                fileSize = destFile.length()

                // If still too large, reduce quality
                if (fileSize > FileUtils.MAX_FILE_SIZE_BYTES && quality > 50) {
                    quality -= 10
                } else {
                    break
                }
            } while (fileSize > FileUtils.MAX_FILE_SIZE_BYTES && quality >= 50)

            // Clean up bitmaps
            if (resizedBitmap != bitmap) {
                bitmap.recycle()
            }
            resizedBitmap.recycle()

            // Verify final size
            if (fileSize > FileUtils.MAX_FILE_SIZE_BYTES) {
                destFile.delete()
                return@withContext CompressionResult(
                    success = false,
                    fileSizeBytes = fileSize,
                    wasCompressed = true,
                    error = "File too large after compression (${FileUtils.formatFileSize(fileSize)})"
                )
            }

            CompressionResult(
                success = true,
                fileSizeBytes = fileSize,
                wasCompressed = needsCompression || quality < COMPRESSION_QUALITY,
                error = null
            )
        } catch (e: OutOfMemoryError) {
            destFile.delete()
            CompressionResult(
                success = false,
                fileSizeBytes = 0,
                wasCompressed = false,
                error = "Out of memory - image too large"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            destFile.delete()
            CompressionResult(
                success = false,
                fileSizeBytes = 0,
                wasCompressed = false,
                error = "Compression failed: ${e.message}"
            )
        }
    }

    /**
     * Loads a bitmap from URI with proper orientation handling.
     */
    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            // Calculate sample size
            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false

            // Decode with sample size
            val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            } ?: return null

            // Fix orientation
            fixOrientation(context, uri, bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Fixes bitmap orientation based on EXIF data.
     */
    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )

                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                    else -> bitmap
                }
            } ?: bitmap
        } catch (e: Exception) {
            // If EXIF reading fails, return original bitmap
            bitmap
        }
    }

    /**
     * Rotates a bitmap by the specified angle.
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    /**
     * Calculates sample size for bitmap decoding to reduce memory usage.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Resizes a bitmap to fit within MAX_DIMENSION while maintaining aspect ratio.
     */
    private fun resizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate scale factor
        val scale = min(
            MAX_DIMENSION.toFloat() / width,
            MAX_DIMENSION.toFloat() / height
        )

        // Calculate new dimensions
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        // Create scaled bitmap
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Copies a file (for PDFs) without compression.
     */
    private fun copyFile(context: Context, sourceUri: Uri, destFile: File): CompressionResult {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val fileSize = destFile.length()

            if (fileSize > FileUtils.MAX_FILE_SIZE_BYTES) {
                destFile.delete()
                CompressionResult(
                    success = false,
                    fileSizeBytes = fileSize,
                    wasCompressed = false,
                    error = "File too large (${FileUtils.formatFileSize(fileSize)}). Maximum 5MB allowed."
                )
            } else {
                CompressionResult(
                    success = true,
                    fileSizeBytes = fileSize,
                    wasCompressed = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            destFile.delete()
            CompressionResult(
                success = false,
                fileSizeBytes = 0,
                wasCompressed = false,
                error = "Failed to copy file: ${e.message}"
            )
        }
    }
}
