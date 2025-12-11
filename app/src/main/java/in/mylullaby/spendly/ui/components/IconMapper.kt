package `in`.mylullaby.spendly.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Airplane
import com.adamglin.phosphoricons.regular.Bank
import com.adamglin.phosphoricons.regular.Briefcase
import com.adamglin.phosphoricons.regular.CurrencyDollar
import com.adamglin.phosphoricons.regular.DotsThree
import com.adamglin.phosphoricons.regular.FilmSlate
import com.adamglin.phosphoricons.regular.FirstAid
import com.adamglin.phosphoricons.regular.ForkKnife
import com.adamglin.phosphoricons.regular.Gift
import com.adamglin.phosphoricons.regular.GraduationCap
import com.adamglin.phosphoricons.regular.House
import com.adamglin.phosphoricons.regular.Laptop
import com.adamglin.phosphoricons.regular.Lightbulb
import com.adamglin.phosphoricons.regular.Question
import com.adamglin.phosphoricons.regular.Receipt
import com.adamglin.phosphoricons.regular.ShoppingBag
import com.adamglin.phosphoricons.regular.ShoppingCart
import com.adamglin.phosphoricons.regular.Storefront
import com.adamglin.phosphoricons.regular.Tag
import com.adamglin.phosphoricons.regular.TrendUp
import com.adamglin.phosphoricons.regular.Wrench

/**
 * Maps icon name strings to Phosphor Icon ImageVectors.
 * Used to resolve category icons stored as string names in the database.
 */
object IconMapper {
    @Composable
    fun getIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            // Food & Dining
            "restaurant" -> PhosphorIcons.Regular.ForkKnife

            // Travel
            "flight" -> PhosphorIcons.Regular.Airplane

            // Home/Rent
            "home" -> PhosphorIcons.Regular.House

            // Utilities
            "lightbulb" -> PhosphorIcons.Regular.Lightbulb

            // Services
            "build" -> PhosphorIcons.Regular.Wrench

            // Shopping
            "shopping_cart" -> PhosphorIcons.Regular.ShoppingCart

            // Entertainment
            "movie" -> PhosphorIcons.Regular.FilmSlate

            // Healthcare
            "local_hospital" -> PhosphorIcons.Regular.FirstAid

            // Gifts
            "card_giftcard" -> PhosphorIcons.Regular.Gift

            // Education
            "school" -> PhosphorIcons.Regular.GraduationCap

            // Investments
            "trending_up" -> PhosphorIcons.Regular.TrendUp

            // Groceries
            "local_grocery_store" -> PhosphorIcons.Regular.ShoppingBag

            // Others
            "more_horiz" -> PhosphorIcons.Regular.DotsThree

            // Uncategorized (default)
            "category" -> PhosphorIcons.Regular.Tag

            // Income/Money icon
            "attach_money" -> PhosphorIcons.Regular.CurrencyDollar

            // Income category icons
            "briefcase" -> PhosphorIcons.Regular.Briefcase
            "laptop" -> PhosphorIcons.Regular.Laptop
            "storefront" -> PhosphorIcons.Regular.Storefront
            "bank" -> PhosphorIcons.Regular.Bank
            "receipt" -> PhosphorIcons.Regular.Receipt
            "gift" -> PhosphorIcons.Regular.Gift

            // Fallback
            else -> PhosphorIcons.Regular.Question
        }
    }
}
