package com.example.fileexplorer.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fileexplorer.HashUtils
import com.example.fileexplorer.data.HashedFileDao
import com.example.fileexplorer.model.ASCEND_ORDER
import com.example.fileexplorer.model.ApiStatus
import com.example.fileexplorer.model.DEFAULT_SORT
import com.example.fileexplorer.model.DESCEND_ORDER
import com.example.fileexplorer.model.DataTypes
import com.example.fileexplorer.model.FOLDER_TYPE
import com.example.fileexplorer.model.HashedFile
import com.example.fileexplorer.model.LONG_STRING
import com.example.fileexplorer.model.LightFile
import com.example.fileexplorer.model.SHORT_STRING
import com.example.fileexplorer.model.START_DIRECTORY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.ArrayDeque
import kotlin.math.pow

// The main file for containing all the information across the app
class FileViewModel(
    private val hashedFileDao: HashedFileDao
) : ViewModel() {

    // Current directory which should be displayed in the app
    private var _directory = START_DIRECTORY
    val directory get() = _directory // Turned out to be not helpful, but can be used in the future

    // Live Data of all files to be displayed
    private var _allLightFiles = MutableLiveData<List<LightFile>>()
    val allLightFiles: LiveData<List<LightFile>> = _allLightFiles

    /*
    Stack for handling onCallbacks from Fragment.
    Could have used FragmentManager, but haven't mastered it yet.
     */
    var pathStack = ArrayDeque<String>(listOf())

    // Values for determining the order to be shown
    var sortBy = DEFAULT_SORT
    var order = ASCEND_ORDER

    // Status of the Api (Isn't noticeable in the app)
    private var _status = MutableLiveData<ApiStatus>()
    val status: LiveData<ApiStatus> = _status

    private val MAX_FILE_SIZE = 2.0.pow(30).toLong()

    // Initialise all data from the start
    init {
        initialise(shouldUpdateDatabase = true)
    }

    // Public method for updating the data
    fun updateDatabase(
        // New path to be shown
        path: String = directory,
        // Was it just sorting or going to the new directory?
        // Could be determined without this value just from directory and given path.
        shouldUpdateDatabase: Boolean = false
    ) {
        Log.d("Files", "Initialising in $path")
        // If we've returned back, should pop the last element
        if (path.length < directory.length) pathStack.removeLast()
        // Change directory to new one
        _directory = path
        // Initialise (update) database
        initialise(shouldUpdateDatabase)
    }

    // Method for updating the database
    private fun initialise(
        // If only order was changed or the whole directory
        shouldUpdateDatabase: Boolean = false,
    ) {
        _status.value = ApiStatus.LOADING
        // Files fetched from the directory
        var files: MutableList<File> = mutableListOf()
        // Future light files to be displayed
        var lightFiles: MutableList<LightFile> = mutableListOf()
        // Temporary light file
        var lightFile: LightFile
        // Hash of file
        var hash = ""
        // Get files from directory
        try {
            files =
                File(directory).listFiles()?.toMutableList() ?: mutableListOf()
        } catch (e: Exception) {
            e.message?.let { Log.d("Files", it) }
        }

        if (shouldUpdateDatabase) {
            // Folders are always first.
            // Order files based on chosen sortBy and order
            if (order == DESCEND_ORDER) {
                when (sortBy) {
                    DataTypes.SIZE -> files.sortByDescending {
                        if (it.isDirectory) Long.MAX_VALUE
                        else it.length()
                    }

                    DataTypes.DATE -> files.sortByDescending {
                        if (it.isDirectory) Long.MAX_VALUE
                        else it.lastModified()
                    }

                    DataTypes.EXTENSION -> files.sortByDescending {
                        if (it.isDirectory) LONG_STRING + it.name.lowercase()
                        else it.extension.lowercase() + it.name.lowercase()
                    }

                    else -> files.sortByDescending {
                        if (it.isDirectory) LONG_STRING + it.name.lowercase()
                        else it.name.lowercase()
                    }
                }
            } else {
                when (sortBy) {
                    DataTypes.SIZE -> files.sortBy {
                        if (it.isDirectory) Long.MIN_VALUE
                        else it.length()
                    }

                    DataTypes.DATE -> files.sortBy {
                        if (it.isDirectory) Long.MIN_VALUE
                        else it.lastModified()
                    }

                    DataTypes.EXTENSION -> files.sortBy {
                        if (it.isDirectory) SHORT_STRING + it.name.lowercase()
                        else it.extension.lowercase() + it.name.lowercase()
                    }

                    else -> files.sortBy {
                        if (it.isDirectory) SHORT_STRING + it.name.lowercase()
                        else it.name.lowercase()
                    }
                }
            }
            // Iteration through each fetched file
            files.forEach {
                lightFile = makeLightFile(it)
                Log.d("Files", "FileName: ${lightFile.name}")

                // Handle the case if it is the folder
                if (lightFile.extension != FOLDER_TYPE) {
                    // If it is very big (>1GB), don't count the hash
                    if (lightFile.size < MAX_FILE_SIZE) {
                        hash = HashUtils.getHashFromFile(it)
                        Log.d("Files", "HashCode: $hash")

                        // Check hash from database
                        if (isNewHash(lightFile.path, hash)) {
                            lightFile.wasEdited = true
                            Log.d("Files", "${lightFile.name} hash was edited")
                        }
                    }
                    // Upsert hashed file in database from this directory
                    upsert(makeHashedFile(lightFile.path, hash))
                }
                // Add light file to the future Live Data of all light files
                lightFiles.add(lightFile)
            }
        } else {
            // Else just iterate through each file in all light files with new order
            lightFiles = allLightFiles.value?.toMutableList() ?: mutableListOf()
            if (order == DESCEND_ORDER) {
                when (sortBy) {
                    DataTypes.SIZE -> lightFiles.sortByDescending {
                        if (it.extension == FOLDER_TYPE) Long.MAX_VALUE
                        else it.size
                    }

                    DataTypes.DATE -> lightFiles.sortByDescending {
                        if (it.extension == FOLDER_TYPE) Long.MAX_VALUE
                        else it.date
                    }

                    DataTypes.EXTENSION -> lightFiles.sortByDescending {
                        if (it.extension == FOLDER_TYPE) LONG_STRING + it.name.lowercase()
                        else it.extension.lowercase() + it.name.lowercase()
                    }

                    else -> lightFiles.sortByDescending {
                        if (it.extension == FOLDER_TYPE) LONG_STRING + it.name.lowercase()
                        else it.name.lowercase()
                    }
                }
            } else {
                when (sortBy) {
                    DataTypes.SIZE -> lightFiles.sortBy {
                        if (it.extension == FOLDER_TYPE) Long.MIN_VALUE
                        else it.size
                    }

                    DataTypes.DATE -> lightFiles.sortBy {
                        if (it.extension == FOLDER_TYPE) Long.MIN_VALUE
                        else it.date
                    }

                    DataTypes.EXTENSION -> lightFiles.sortBy {
                        if (it.extension == FOLDER_TYPE) SHORT_STRING + it.name.lowercase()
                        else it.extension.lowercase() + it.name.lowercase()
                    }

                    else -> lightFiles.sortBy {
                        if (it.extension == FOLDER_TYPE) SHORT_STRING + it.name.lowercase()
                        else it.name.lowercase()
                    }
                }
            }
        }
        _status.value = ApiStatus.DONE
        _allLightFiles.value = lightFiles
    }

    // Turn java file to light file
    private fun makeLightFile(file: File, wasEdited: Boolean = false): LightFile {
        val size: Long
        var extension = ""
        if (file.isDirectory) {
            size = file.listFiles()?.size?.toLong() ?: 0
            extension = FOLDER_TYPE
        } else {
            size = file.length()
            extension = file.extension.lowercase()
        }
        return LightFile(
            directory = directory,
            path = file.path,
            name = file.name,
            date = file.lastModified(),
            extension = extension,
            size = size,
            wasEdited = wasEdited
        )
    }

    // Make file with hash
    private fun makeHashedFile(path: String, hash: String): HashedFile {
        return HashedFile(
            path = path,
            hash = hash
        )
    }

    // Dao method upsert
    private fun upsert(hashedFile: HashedFile) {
        viewModelScope.launch(Dispatchers.IO) { hashedFileDao.upsert(hashedFile) }
    }

    // Check if hash was changed
    private fun isNewHash(path: String, newHash: String): Boolean {
        val hashedFile: HashedFile? = runBlocking { hashedFileDao.getHashedFile(path) }
        if (hashedFile != null) return hashedFile.hash != newHash
        else return false
    }
}

class FileViewModelFactory(private val hashedFileDao: HashedFileDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FileViewModel(hashedFileDao) as T
        }
        throw java.lang.IllegalArgumentException("Unknown ViewModel class")
    }
}