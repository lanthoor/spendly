package `in`.mylullaby.spendly.ui.screens.expenses.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.FilePdf
import com.adamglin.phosphoricons.regular.Trash
import `in`.mylullaby.spendly.domain.model.Receipt
import `in`.mylullaby.spendly.utils.FileUtils
import java.io.File

/**
 * Composable that displays a receipt thumbnail with file info and delete action.
 *
 * @param receipt The receipt to display
 * @param onDelete Callback when delete button is clicked
 * @param onClick Callback when thumbnail is clicked (for full-screen view)
 * @param modifier Optional modifier
 */
@Composable
fun ReceiptThumbnail(
    receipt: Receipt,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .size(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Display image or PDF icon
            if (receipt.isImage()) {
                // Load image using Coil with optimizations
                val imageFile = File(context.filesDir, receipt.filePath)

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageFile)
                        .size(240) // 120dp * 2 for high density displays
                        .crossfade(false) // Disable animation for better performance
                        .memoryCacheKey(receipt.filePath)
                        .diskCacheKey(receipt.filePath)
                        .build(),
                    contentDescription = "Receipt image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (receipt.isPdf()) {
                // Show PDF icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.FilePdf,
                            contentDescription = "PDF file",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // File size badge (bottom-left)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = FileUtils.formatFileSize(receipt.fileSizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Delete button (top-right)
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .padding(2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.Trash,
                            contentDescription = "Delete receipt",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Compressed badge (top-left) - optional
            if (receipt.compressed) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Compressed",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}
