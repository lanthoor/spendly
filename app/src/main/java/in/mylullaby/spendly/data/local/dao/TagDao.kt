package `in`.mylullaby.spendly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import `in`.mylullaby.spendly.data.local.entities.TagEntity

/**
 * Data Access Object for Tag operations.
 *
 * Provides methods for managing custom tags for transactions.
 */
@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    /**
     * Get all tags ordered alphabetically.
     */
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    /**
     * Get a tag by ID.
     */
    @Query("SELECT * FROM tags WHERE id = :tagId")
    fun getTagById(tagId: Long): Flow<TagEntity?>

    /**
     * Get a tag by name.
     * Used to check for duplicate names before insert.
     */
    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    /**
     * Check if a tag exists by ID.
     */
    @Query("SELECT COUNT(*) FROM tags WHERE id = :tagId")
    suspend fun exists(tagId: Long): Int
}
