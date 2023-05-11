package com.example.fileexplorer

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object HashUtils {
    private const val BUFFER_SIZE = 32*1024

    fun getHashFromFile(file: File): String {
        val buffer = ByteArray(BUFFER_SIZE)
        val digest = MessageDigest.getInstance("SHA-256")
        val fis = FileInputStream(file)
        var result = fis.read(buffer, 0, BUFFER_SIZE)
        while (result > -1) {
            digest.update(buffer, 0 , BUFFER_SIZE)
            result = fis.read(buffer, 0, BUFFER_SIZE)
        }
        fis.close()
        return digest.digest().toHexString()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}