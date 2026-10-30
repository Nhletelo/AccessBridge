package com.example.accessbridge.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.accessbridge.data.local.dao.ResourceDao
import com.example.accessbridge.data.local.entity.ResourceEntity

@Database(
    entities = [ResourceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AccessBridgeDatabase : RoomDatabase() {

    abstract fun resourceDao(): ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: AccessBridgeDatabase? = null

        fun getDatabase(context: Context): AccessBridgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AccessBridgeDatabase::class.java,
                    "access_bridge_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
