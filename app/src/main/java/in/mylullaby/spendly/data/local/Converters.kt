package `in`.mylullaby.spendly.data.local

import androidx.room.TypeConverter

/**
 * Room TypeConverters for handling complex data types.
 *
 * Currently empty as all enums are stored as Strings and
 * lists are handled through junction tables.
 *
 * Future converters may be added here for:
 * - Date conversions
 * - Complex object serialization
 * - Collection types
 */
class Converters {
    // No converters needed yet
    // All enums are stored as String
    // Many-to-many relationships use junction tables
}
