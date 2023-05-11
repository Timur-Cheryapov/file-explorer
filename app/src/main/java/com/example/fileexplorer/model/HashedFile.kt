package com.example.fileexplorer.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// File's path, its hash value
@Entity
data class HashedFile(
    @PrimaryKey val path: String,
    val hash: String
)