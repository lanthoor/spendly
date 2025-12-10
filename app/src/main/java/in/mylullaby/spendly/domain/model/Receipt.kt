package `in`.mylullaby.spendly.domain.model

/**
 * Domain model representing a receipt attachment for an expense.
 * Supports JPG, PNG, WebP, and PDF formats with a max size of 5MB.
 */
data class Receipt(
    val id: Long = 0,
    val expenseId: Long,
    val filePath: String,
    val fileType: String, // JPG, PNG, WebP, PDF
    val fileSizeBytes: Long, // max 5MB
    val compressed: Boolean
) {
    /**
     * Checks if the receipt file is an image format.
     * @return true if fileType is JPG, PNG, or WebP
     */
    fun isImage(): Boolean = fileType in listOf("JPG", "PNG", "WebP")

    /**
     * Checks if the receipt file is a PDF.
     * @return true if fileType is PDF
     */
    fun isPdf(): Boolean = fileType == "PDF"

    /**
     * Converts file size from bytes to megabytes.
     * @return File size in MB (e.g., 2097152 bytes = 2.0 MB)
     */
    fun fileSizeMB(): Double = fileSizeBytes / (1024.0 * 1024.0)

    companion object {
        /**
         * Maximum allowed file size in bytes (5MB).
         */
        const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L
    }
}
