package com.rhizo.common.util

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest


object MD5Util {
    // 全局数组
    private val strDigits = arrayOf(
        "0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"
    )
    private val MD5 by lazy { MessageDigest.getInstance("MD5") }

    /**
     * 对一个文件获取md5值
     *
     * @return md5串
     */
    fun getFileMd5(file: File): String {
        var fileInputStream: FileInputStream? = null
        return try {
            fileInputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var length: Int
            while (fileInputStream.read(buffer).also { length = it } != -1) {
                MD5.update(buffer, 0, length)
            }
            byteToHex(MD5.digest())
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        } finally {
            try {
                fileInputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun md5(str: String): String {
        var resultString: String? = null
        try {
            // md.digest() 该函数返回值为存放哈希值结果的byte数组
            resultString = byteTo16String(MD5.digest(str.toByteArray()))
        } catch (ex: java.security.NoSuchAlgorithmException) {
            ex.printStackTrace()
        }
        return resultString ?: ""
    }

    /**
     * 字节数组转16进制字符串
     */
    private fun byteToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    /**
     * 转换字节数组为16进制字串
     */
    private fun byteTo16String(bByte: ByteArray): String {
        val sBuffer = StringBuffer()
        for (i in bByte.indices) {
            sBuffer.append(byteToArrayString(bByte[i]))
        }
        return sBuffer.toString()
    }

    /**
     * 返回形式为数字跟字符串
     */
    private fun byteToArrayString(bByte: Byte): String {
        var iRet = bByte.toInt()
        // System.out.println("iRet="+iRet);
        if (iRet < 0) {
            iRet += 256
        }
        val iD1 = iRet / 16
        val iD2 = iRet % 16
        return strDigits[iD1] + strDigits[iD2]
    }

}