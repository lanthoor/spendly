package dev.lanthoor.spendly.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Helper object for managing runtime locale changes.
 * Provides methods to update context configuration with selected language.
 */
object LocaleHelper {

    /**
     * Wraps the given context with the specified language locale.
     * This should be called from Activity.attachBaseContext() to apply locale changes.
     *
     * @param context The base context to wrap
     * @param language The language to apply
     * @return Context with updated locale configuration
     */
    fun wrap(context: Context, language: AppLanguage): Context {
        val locale = Locale.forLanguageTag(language.code)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Gets the current system locale language code.
     * Used for initial setup if no preference is stored.
     */
    fun getSystemLanguageCode(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Locale.getDefault().language
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault().language
        }
    }
}
