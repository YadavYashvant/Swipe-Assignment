package com.yashvant.swipe_assignment.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yashvant.swipe_assignment.data.model.Product

@Database(
    entities = [Product::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao

    companion object {
        const val DATABASE_NAME = "swipe_products_db"
    }
}

