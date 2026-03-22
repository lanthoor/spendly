package `in`.co.spendly.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.CaretLeft
import `in`.co.spendly.R

/**
 * Consistent top app bar for Spendly screens.
 * Wraps Material 3 TopAppBar with consistent styling.
 *
 * @param title Title text to display (null to hide title)
 * @param onNavigationClick Callback for back navigation (null to hide back button)
 * @param navigationIcon Custom navigation icon composable (overrides onNavigationClick)
 * @param modifier Optional modifier
 * @param actions Optional trailing actions (icons, buttons, etc.)
 * @param centerContent Optional content to display in the center (replaces title when provided)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendlyTopAppBar(
    title: String? = null,
    modifier: Modifier = Modifier,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    centerContent: @Composable (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            if (centerContent != null) {
                centerContent()
            } else if (title != null) {
                Text(title)
            }
        },
        navigationIcon = {
            if (navigationIcon != null) {
                navigationIcon()
            } else if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretLeft,
                        contentDescription = stringResource(R.string.cd_navigate_back)
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
        title = stringResource(R.string.screen_add_expense_title),
        onNavigationClick = {}
    )
}

@Preview
@Composable
private fun SpendlyTopAppBarNoBackButtonPreview() {
    SpendlyTopAppBar(
        title = stringResource(R.string.screen_expenses_title)
    )
}
