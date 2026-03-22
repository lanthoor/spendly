package dev.lanthoor.spendly.ui.screens.expenses.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.FolderOpen
import dev.lanthoor.spendly.R

/**
 * Modal bottom sheet for selecting receipt source (file picker or camera).
 *
 * @param onDismiss Callback when sheet is dismissed
 * @param onSelectFromFiles Callback when "Choose from files" is clicked
 * @param onCapturePhoto Callback when "Take photo" is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPickerSheet(
    onDismiss: () -> Unit,
    onSelectFromFiles: () -> Unit,
    onCapturePhoto: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = stringResource(R.string.header_add_receipt),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Choose from files option
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.choose_from_files))
                },
                supportingContent = {
                    Text(stringResource(R.string.select_file_types_desc))
                },
                leadingContent = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onSelectFromFiles()
                    onDismiss()
                }
            )

            // Take photo option
            ListItem(
                headlineContent = {
                    Text(stringResource(R.string.take_photo))
                },
                supportingContent = {
                    Text(stringResource(R.string.capture_using_camera))
                },
                leadingContent = {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Camera,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                modifier = Modifier.clickable {
                    onCapturePhoto()
                    onDismiss()
                }
            )
        }
    }
}
