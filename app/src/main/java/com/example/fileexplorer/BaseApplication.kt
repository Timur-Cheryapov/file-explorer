package com.example.fileexplorer

import android.app.Application
import com.example.fileexplorer.data.HashedFileDatabase

// Base Application
class BaseApplication : Application() {

    val database: HashedFileDatabase by lazy { HashedFileDatabase.getDatabase(this) }
}