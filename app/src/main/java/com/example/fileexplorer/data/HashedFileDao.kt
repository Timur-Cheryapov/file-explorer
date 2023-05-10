package com.example.fileexplorer.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.fileexplorer.model.HashedFile

/*
Data Access Object for handling events with Database
 */
@Dao
interface HashedFileDao {

    // Insert or update a file in Database
    @Upsert
    suspend fun upsert(hashedFile: HashedFile)

    // Retrieve a file from Database
    @Query("select * from HashedFile where path = :path")
    suspend fun getHashedFile(path: String): HashedFile
}