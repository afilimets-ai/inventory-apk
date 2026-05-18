package com.inventory.data.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inventory.data.db.InventoryDatabase
import com.inventory.data.entity.Location
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationDaoLookupTest {
    private lateinit var database: InventoryDatabase
    private lateinit var locationDao: LocationDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            InventoryDatabase::class.java
        ).allowMainThreadQueries().build()
        locationDao = database.locationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getByName_returnsInsertedLocation() = runBlocking {
        val id = locationDao.insert(Location(name = "Склад A"))

        val found = locationDao.getByName("Склад A")

        assertNotNull(found)
        assertEquals(id, found?.id)
        assertEquals("Склад A", found?.name)
    }

    @Test
    fun getByName_returnsNull_whenMissing() = runBlocking {
        val found = locationDao.getByName("Відсутня")

        assertNull(found)
    }
}
