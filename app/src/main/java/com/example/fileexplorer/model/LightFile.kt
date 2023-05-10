package com.example.fileexplorer.model

import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round

/*
Data class which represents an ordinary Java file,
but only its important parameters.
Size represents the size of a file or
if it is a folder, represents the number of files in the folder.
 */
data class LightFile(
    val directory: String,
    val path: String,
    val name: String,
    val date: Long,
    val extension: String,
    var size: Long,
    var wasEdited: Boolean = false
) {
    // Special function for formatting last modified date
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    fun getFormattedDate(): String {
        return dateFormat.format(date)
    }

    // Special function for formatting size value for easy-reading
    fun getFormattedSize(): String {
        if (extension != FOLDER_TYPE) {
            var sizef: Double = size * 1.0
            if (sizef > 2.0.pow(30)) {
                sizef = round((sizef / (2.0.pow(30))) * 10.0) / 10.0
                return "$sizef GB"
            } else if (sizef > 2.0.pow(20)) {
                sizef = round((sizef / (2.0.pow(20))) * 10.0) / 10.0
                return "$sizef MB"
            } else if (sizef > 2.0.pow(10)) {
                sizef = round((sizef / (2.0.pow(10))) * 10.0) / 10.0
                return "$sizef KB"
            } else {
                return "$sizef Bytes"
            }
        } else {
            return "$size files"
        }
    }
}