package `in`.mylullaby.spendly.ui.components

import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Consistent top app bar for Spendly screens.
 * Wraps Material 3 TopAppBar with consistent styling.
 *
 * @param title Title text to display
 * @param onNavigationClick Callback for back navigation (null to hide back button)
 * @param modifier Optional modifier
 * @param actions Optional trailing actions (icons, buttons, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendlyTopAppBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretLeft,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(),
        modifier = modifier
    )
}

@Preview
@Composable
private fun SpendlyTopAppBarPreview() {
    SpendlyTopAppBar(
        title = "Add Expense",
        onNavigationClick = {}
    )
}

@Preview
@Composable
private fun SpendlyTopAppBarNoBackButtonPreview() {
    SpendlyTopAppBar(
        title = "Expenses"
    )
}
