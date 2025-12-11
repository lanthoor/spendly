package `in`.mylullaby.spendly.data.local.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import `in`.mylullaby.spendly.data.local.SpendlyDatabase
import `in`.mylullaby.spendly.utils.TestDataBuilders
import `in`.mylullaby.spendly.utils.createTestDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for TagDao.
 *
 * Tests CRUD operations, unique name constraint, alphabetical sorting, and Flow reactivity.
 *
 * Pattern follows CurrencyUtilsTest: methodName_inputCondition_expectedResult
 */
@RunWith(AndroidJUnit4::class)
class TagDaoTest {

    private lateinit var database: SpendlyDatabase
    private lateinit var dao: TagDao

    @Before
    fun setUp() {
        database = createTestDatabase()
        dao = database.tagDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // CRUD Operations Tests

    @Test
    fun insertTag_withValidData_returnsInsertedId() = runTest {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Business")

        // Act
        val id = dao.insert(tag)

        // Assert
        assertTrue("Inserted ID should be positive", id > 0)
        dao.getTagById(id).test {
            val retrieved = awaitItem()
            assertEquals("Business", retrieved?.name)
            assertEquals(tag.color, retrieved?.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun insertTag_withDuplicateName_replaces() = runTest {
        // Arrange
        val tag1 = TestDataBuilders.createTestTagEntity(
            name = "Business",
            color = 0xFFFF0000.toInt()
        )
        dao.insert(tag1)

        // Act - Insert with same name but different color
        val tag2 = TestDataBuilders.createTestTagEntity(
            name = "Business",
            color = 0xFF00FF00.toInt()
        )
        dao.insert(tag2)

        // Assert - Should replace due to OnConflictStrategy.REPLACE
        dao.getAllTags().test {
            val tags = awaitItem()
            val businessTags = tags.filter { it.name == "Business" }
            assertEquals(1, businessTags.size)
            assertEquals(0xFF00FF00.toInt(), businessTags[0].color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateTag_changesData() = runTest {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(
            name = "Original",
            color = 0xFFFF0000.toInt()
        )
        val id = dao.insert(tag)

        // Act
        dao.getTagById(id).test {
            val inserted = awaitItem()!!
            val updated = inserted.copy(
                name = "Updated",
                color = 0xFF00FF00.toInt()
            )
            dao.update(updated)

            // Assert
            val retrieved = awaitItem()
            assertEquals("Updated", retrieved?.name)
            assertEquals(0xFF00FF00.toInt(), retrieved?.color)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteTag_removesFromDatabase() = runTest {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Test")
        val id = dao.insert(tag)

        // Act
        dao.getTagById(id).test {
            val retrieved = awaitItem()!!
            dao.delete(retrieved)

            // Assert
            val afterDelete = awaitItem()
            assertNull("Tag should be null after deletion", afterDelete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Query Tests

    @Test
    fun getAllTags_orderedAlphabetically() = runTest {
        // Arrange
        val tag1 = TestDataBuilders.createTestTagEntity(name = "Zebra")
        val tag2 = TestDataBuilders.createTestTagEntity(name = "Apple")
        val tag3 = TestDataBuilders.createTestTagEntity(name = "Mango")

        dao.insert(tag1)
        dao.insert(tag2)
        dao.insert(tag3)

        // Act & Assert
        dao.getAllTags().test {
            val tags = awaitItem()
            assertEquals(3, tags.size)
            // Should be ordered alphabetically (ASC)
            assertEquals("Apple", tags[0].name)
            assertEquals("Mango", tags[1].name)
            assertEquals("Zebra", tags[2].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTagById_returnsCorrectTag() = runTest {
        // Arrange
        val tag1 = TestDataBuilders.createTestTagEntity(name = "First")
        val tag2 = TestDataBuilders.createTestTagEntity(name = "Second")
        val id1 = dao.insert(tag1)
        val id2 = dao.insert(tag2)

        // Act & Assert
        dao.getTagById(id2).test {
            val retrieved = awaitItem()
            assertEquals("Second", retrieved?.name)
            assertEquals(id2, retrieved?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTagById_withNonexistentId_returnsNull() = runTest {
        // Act & Assert
        dao.getTagById(999L).test {
            val retrieved = awaitItem()
            assertNull("Should return null for non-existent ID", retrieved)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Validation Tests

    @Test
    fun getTagByName_withExistingName_returnsTag() = runBlocking {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        dao.insert(tag)

        // Act
        val retrieved = dao.getTagByName("Business")

        // Assert
        assertNotNull(retrieved)
        assertEquals("Business", retrieved?.name)
    }

    @Test
    fun getTagByName_withNonexistentName_returnsNull() = runBlocking {
        // Act
        val retrieved = dao.getTagByName("Nonexistent")

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun exists_withExistingTag_returnsOne() = runBlocking {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Business")
        val id = dao.insert(tag)

        // Act
        val count = dao.exists(id)

        // Assert
        assertEquals(1, count)
    }

    @Test
    fun exists_withNonexistentTag_returnsZero() = runBlocking {
        // Act
        val count = dao.exists(999L)

        // Assert
        assertEquals(0, count)
    }

    // Flow Reactivity Tests

    @Test
    fun getAllTags_emitsUpdatesOnInsert() = runTest {
        // Act & Assert
        dao.getAllTags().test {
            // Initial emission (empty)
            assertEquals(0, awaitItem().size)

            // Insert tag
            dao.insert(TestDataBuilders.createTestTagEntity(name = "Business"))

            // Verify Flow emits updated list
            assertEquals(1, awaitItem().size)

            // Insert another
            dao.insert(TestDataBuilders.createTestTagEntity(name = "Personal"))
            assertEquals(2, awaitItem().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getTagById_emitsUpdatesOnUpdate() = runTest {
        // Arrange
        val tag = TestDataBuilders.createTestTagEntity(name = "Original")
        val id = dao.insert(tag)

        // Act & Assert
        dao.getTagById(id).test {
            val original = awaitItem()!!
            assertEquals("Original", original.name)

            // Update
            val updated = original.copy(name = "Updated")
            dao.update(updated)

            // Verify Flow emits updated tag
            val afterUpdate = awaitItem()
            assertEquals("Updated", afterUpdate?.name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
