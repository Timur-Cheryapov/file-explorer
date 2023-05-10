package com.example.fileexplorer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fileexplorer.model.HashedFile


// Database for files with hash value
@Database(entities = [HashedFile::class], version = 1, exportSchema = false)
abstract class HashedFileDatabase : RoomDatabase() {

    abstract fun hashedFileDao(): HashedFileDao

    companion object {
        @Volatile
        private var INSTANCE: HashedFileDatabase? = null

        fun getDatabase(context: Context): HashedFileDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HashedFileDatabase::class.java,
                    "hashed_file_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}