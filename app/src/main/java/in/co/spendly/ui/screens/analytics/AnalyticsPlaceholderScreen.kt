package `in`.co.spendly.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.ChartLine
import com.adamglin.phosphoricons.regular.ChartPieSlice
import com.adamglin.phosphoricons.regular.TrendUp
import `in`.co.spendly.R

/**
 * Placeholder screen for Analytics feature.
 * Displays a 2x2 grid of chart icons with motivational messaging about upcoming insights.
 *
 * This is a temporary placeholder for Phase 11 - Analytics & Insights implementation.
 */
@Composable
fun AnalyticsPlaceholderScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 2x2 Grid of chart icons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.ChartPieSlice,
                    contentDescription = stringResource(R.string.cd_pie_chart),
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.ChartBar,
                    contentDescription = stringResource(R.string.cd_bar_chart),
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            // Second row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Regular.ChartLine,
                    contentDescription = stringResource(R.string.cd_line_chart),
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = PhosphorIcons.Regular.TrendUp,
                    contentDescription = stringResource(R.string.cd_trend_chart),
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.analytics_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.analytics_desc),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AnalyticsPlaceholderScreenPreview() {
    AnalyticsPlaceholderScreen()
}
