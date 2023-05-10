package com.example.fileexplorer.model

/*
Constant values for different purposes.
Folders are always first.
 */

const val FOLDER_TYPE = "folder"
val DEFAULT_SORT = DataTypes.NAME
const val START_DIRECTORY = "/storage/emulated/0"
const val ASCEND_ORDER = 1
const val DESCEND_ORDER = 0
const val LONG_STRING = 65535.toChar().toString()
const val SHORT_STRING = "."

// To determine which sort order should be used
enum class DataTypes { NAME, SIZE, DATE, EXTENSION }

// Represents the current status of the Api
enum class ApiStatus { LOADING, DONE }

