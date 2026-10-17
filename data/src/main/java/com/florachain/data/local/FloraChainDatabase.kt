package com.florachain.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.florachain.data.local.dao.ProductDao
import com.florachain.data.local.dao.SyncQueueDao
import com.florachain.data.local.entity.ProductEntity
import com.florachain.data.local.entity.SyncQueueEntity

@Database(
    entities = [ProductEntity::class, SyncQueueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FloraChainDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        private const val DB_NAME = "florachain.db"

        @Volatile
        private var INSTANCE: FloraChainDatabase? = null

        fun getInstance(context: Context): FloraChainDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloraChainDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
