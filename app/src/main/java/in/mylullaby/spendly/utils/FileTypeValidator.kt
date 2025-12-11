package `in`.mylullaby.spendly.utils

import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Validates file types using magic number (file header) detection.
 *
 * SECURITY: Prevents malicious files disguised as images.
 * Extension-only validation is unsafe - a file named "malware.apk" can be renamed to "malware.jpg".
 * Magic numbers are the first few bytes of a file that identify its true format.
 *
 * Supported formats:
 * - JPG/JPEG: FF D8 FF
 * - PNG: 89 50 4E 47
 * - WebP: 52 49 46 46 (RIFF)
 * - PDF: 25 50 44 46 (%PDF)
 */
object FileTypeValidator {

    private const val TAG = "FileTypeValidator"

    /**
     * Magic numbers for supported file types.
     * Key: file extension
     * Value: list of possible magic number patterns (some formats have multiple valid headers)
     */
    private val MAGIC_NUMBERS = mapOf(
        "jpg" to listOf(
            byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        ),
        "jpeg" to listOf(
            byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        ),
        "png" to listOf(
            byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        ),
        "webp" to listOf(
            byteArrayOf(0x52, 0x49, 0x46, 0x46) // "RIFF" - need to check "WEBP" at byte 8
        ),
        "pdf" to listOf(
            byteArrayOf(0x25, 0x50, 0x44, 0x46) // "%PDF"
        )
    )

    /**
     * Validates a file's magic number against its expected extension.
     *
     * @param file The file to validate
     * @param expectedExtension The expected file extension (jpg, png, webp, pdf)
     * @return true if magic number matches extension, false otherwise
     */
    fun validateFileType(file: File, expectedExtension: String): Boolean {
        val magicNumbers = MAGIC_NUMBERS[expectedExtension.lowercase()]
        if (magicNumbers == null) {
            Log.w(TAG, "Unknown file extension: $expectedExtension")
            return false
        }

        return try {
            file.inputStream().use { input ->
                // Special case for WebP - need to check both RIFF header and WEBP signature
                if (expectedExtension.lowercase() in setOf("webp")) {
                    return validateWebP(input)
                }

                // For other formats, check if file starts with any valid magic number
                magicNumbers.any { magic ->
                    val header = ByteArray(magic.size)
                    val bytesRead = input.read(header)

                    if (bytesRead < magic.size) {
                        Log.w(TAG, "File too small to contain magic number: ${file.name}")
                        return@any false
                    }

                    val matches = header.contentEquals(magic)
                    if (!matches) {
                        Log.w(TAG, "Magic number mismatch for ${file.name}: expected ${magic.toHexString()}, got ${header.toHexString()}")
                    }
                    matches
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read file magic number: ${file.name}", e)
            false
        }
    }

    /**
     * Validates WebP format specifically.
     * WebP files start with "RIFF" (4 bytes), then 4 bytes of file size, then "WEBP" (4 bytes).
     */
    private fun validateWebP(input: java.io.InputStream): Boolean {
        return try {
            val header = ByteArray(12) // Read first 12 bytes
            val bytesRead = input.read(header)

            if (bytesRead < 12) {
                Log.w(TAG, "File too small for WebP format")
                return false
            }

            // Check RIFF signature (bytes 0-3)
            val riffSignature = byteArrayOf(0x52, 0x49, 0x46, 0x46) // "RIFF"
            val hasRiff = header.sliceArray(0..3).contentEquals(riffSignature)

            // Check WEBP signature (bytes 8-11)
            val webpSignature = byteArrayOf(0x57, 0x45, 0x42, 0x50) // "WEBP"
            val hasWebp = header.sliceArray(8..11).contentEquals(webpSignature)

            val valid = hasRiff && hasWebp
            if (!valid) {
                Log.w(TAG, "WebP validation failed: hasRiff=$hasRiff, hasWebP=$hasWebp")
            }
            valid
        } catch (e: Exception) {
            Log.e(TAG, "WebP validation error", e)
            false
        }
    }

    /**
     * Result of file validation.
     */
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data object InvalidExtension : ValidationResult()
        data object MagicNumberMismatch : ValidationResult()
        data object FileTooLarge : ValidationResult()
        data object FileNotFound : ValidationResult()

        fun isValid() = this is Valid
        fun getErrorMessage(): String = when (this) {
            is Valid -> ""
            is InvalidExtension -> "Unsupported file type. Please use JPG, PNG, WebP, or PDF."
            is MagicNumberMismatch -> "File appears to be corrupted or not a valid image/PDF."
            is FileTooLarge -> "File is too large. Maximum size is 5MB."
            is FileNotFound -> "File not found."
        }
    }

    /**
     * Comprehensive validation of a receipt file.
     * Checks extension, magic number, and file size.
     *
     * @param file The file to validate
     * @param maxSizeBytes Maximum allowed file size (default 5MB)
     * @return ValidationResult indicating success or failure reason
     */
    fun validateReceiptFile(file: File, maxSizeBytes: Long = 5 * 1024 * 1024): ValidationResult {
        // Check file exists
        if (!file.exists()) {
            Log.w(TAG, "File not found: ${file.absolutePath}")
            return ValidationResult.FileNotFound
        }

        // Check extension
        val extension = file.extension.lowercase()
        if (extension !in setOf("jpg", "jpeg", "png", "webp", "pdf")) {
            Log.w(TAG, "Invalid extension: $extension")
            return ValidationResult.InvalidExtension
        }

        // Check magic number
        if (!validateFileType(file, extension)) {
            Log.w(TAG, "Magic number mismatch for: ${file.name}")
            return ValidationResult.MagicNumberMismatch
        }

        // Check file size
        if (file.length() > maxSizeBytes) {
            Log.w(TAG, "File too large: ${file.length()} bytes (max: $maxSizeBytes)")
            return ValidationResult.FileTooLarge
        }

        return ValidationResult.Valid
    }

    /**
     * Helper to convert byte array to hex string for logging.
     */
    private fun ByteArray.toHexString(): String {
        return joinToString(" ") { "%02X".format(it) }
    }
}
