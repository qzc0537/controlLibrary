package com.rhizo.common.util

import java.io.File
import java.io.FileInputStream

object FileUtil {

    fun getFileSize(file: File): Int {
        val fis: FileInputStream
        return try {
            fis = FileInputStream(file)
            fis.available()
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}