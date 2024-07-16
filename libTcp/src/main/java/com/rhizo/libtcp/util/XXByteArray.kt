package com.rhizo.libtcp.util

import org.apache.commons.lang3.StringUtils

/**
 * 字节数组工具类
 */
object XXByteArray {


    /**
     * 使用chunked()函数将字符串按特定长度分割成多个子字符串。
     */
    fun splitStringByLength(input: String, length: Int): List<String> {
        return input.chunked(length)
    }

    /**
     * 16进制字符串 转化为 字节数组
     */
    fun hexToBytes(hex: String): ByteArray {
        return ByteArray(hex.length / 2) { hex.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    /**
     * Int整数 转化为 8位 16进制字符串
     * 例如：1680247411 -> 64268A73
     */
    fun toHexOf4Bytes(intValue: Int): String {
        return String.format("%08X", intValue)
    }

    /**
     * Int整数 转化为 4位 16进制字符串
     * 例如：41 -> 0029
     */
    fun toHexOf2Bytes(intValue: Int): String {
        return String.format("%04X", intValue)
    }

    /**
     * 字节数组转带空格的十六进制字符串，且合并 FE
     * 例如：FF FF 00 01 00 01 00 01 00 01 64 21 26 45 00 1A 72 65 67 69 73 74 72 61 FE01 69 6F 6E 20 FE00 75 63 63 65 73 73 66 75 6C 3A 4F 4B B9 33 FF
     */
    fun bytesToSpaceHex(bytes: ByteArray): String {
        return h264Bytes2Hex(bytes)
    }

    fun h264Bytes2Hex(bytes: ByteArray?): String {
        if (bytes == null) {
            return ""
        }
        val HEXES = "0123456789ABCDEF"
        val hex = java.lang.StringBuilder(2 * bytes.size)
        for (b in bytes) {
            val left = HEXES[b.toInt() and 0xF0 shr 4]
            val right = HEXES[b.toInt() and 0x0F]
            if (left == 'F' && right == 'E') {
                hex.append(left).append(right)
            } else {
                hex.append(left).append(right).append(" ")
            }
        }
        return hex.toString()
    }

    /**
     * 转义 FE00 FE01 -> FE FF，无空格
     * 例如 FFFF0001000100010001642127E7001A7265676973747261FF696F6E20FE75636365737366756C3A4F4B26A0FF
     */
    fun removeSpace(dataHex: String): String {
        return StringUtils.replace(dataHex, " ", "")
    }

    /**
     * 转义 FE00 FE01 -> FE FF，无空格
     * 例如 FFFF0001000100010001642127E7001A7265676973747261FF696F6E20FE75636365737366756C3A4F4B26A0FF
     */
    fun feChangeFromHex(dataHex: String): String {
        val replaceFE01 = StringUtils.replace(dataHex, "E01", "F")
        val result = StringUtils.replace(replaceFE01, "E00", "E")
        return StringUtils.replace(result, " ", "")
    }

    /**
     * 字节数组 转 十六进制大写字符，无空格
     * 例如：FFFF000100010001000164212645001A7265676973747261FE016F6E20EE00636365737366756C3A4F4BB933FF
     */
    fun toHeX(bytes: ByteArray): String {
        return buildString {
            for (bt in bytes) {
                append(String.format("%02X", bt))
            }
        }
    }

    /**
     * 无汉字十六进制字符串 转 字节数组
     */
    fun fromHex(str: String): ByteArray {
        return ByteArray(str.length / 2) { str.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    /**
     * Int型数值 转 4字节数组
     * shl(bits):左移运算符。
     * Kotlin的位运算符只能对Int和Long两种数据类型起作用。
     * 使用&&连接两个表达式时，会从左往右依次判定每个表达式的结果，当遇到某个表达式的结果为false时，则会直接返回整个表达式的结果为false，不会再执行接下来的表达式。
     * 使用and连接两个表达式时，会执行所有的表达式并收集结果，最后把and两边的结果再做逻辑与操作得出最终结果。
     * @param num Int
     * @return ByteArray
     */
    fun fromInt(num: Int): ByteArray {
        val byteArray = ByteArray(4)
        val highH = ((num shr 24) and 0xff).toByte()
        val highL = ((num shr 16) and 0xff).toByte()
        val lowH = ((num shr 8) and 0xff).toByte()
        val lowL = (num and 0xff).toByte()
        byteArray[0] = highH
        byteArray[1] = highL
        byteArray[2] = lowH
        byteArray[3] = lowL
        return byteArray
    }

    /**
     * 判断两个字节数组是否一样
     * kotlin数组提供了一个indices属性，这个属性可返回数组的索引区间
     * @param b1 ByteArray
     * @param b2 ByteArray
     * @return Boolean
     */
    fun equals(b1: ByteArray, b2: ByteArray): Boolean {
        if (b1.size == b2.size) {
            for (i in b1.indices) {
                if (b1[i] != b2[i]) {
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

    /**
     * 合并 两个字节数组
     * @param b1 ByteArray
     * @param b2 ByteArray
     * @return ByteArray
     */
    fun merge(b1: ByteArray?, b2: ByteArray?): ByteArray? {
        if (b1 == null) return b2
        if (b2 == null) return b1
        val b1Size = b1.size
        val b2Size = b2.size
        if (b1Size == 0) return b2
        if (b2Size == 0) return b1
        val result = ByteArray(b1Size + b2Size)
        System.arraycopy(b1, 0, result, 0, b1Size)
        System.arraycopy(b2, 0, result, b1Size, b2Size)
        return result
    }

    /**
     * 截取 字节数组
     * System.arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
     * src 原数组 / srcPos 原数组起始位置（从这个位置开始复制） / dest 目标数组 / destPos 目标数组粘贴的起始位置 / length 复制的个数
     * @param bytes  被截取数组
     * @param start  被截取数组开始截取位置
     * @param length 新数组的长度
     * @return ByteArray
     */
    fun split(bytes: ByteArray, start: Int, length: Int): ByteArray {
        val result = ByteArray(length)
        System.arraycopy(bytes, start, result, 0, length)
        return result
    }

    /**
     * 合并多个字节数组
     * @param first ByteArray
     * @param rest Array<out ByteArray?>
     * @return ByteArray
     */
    fun combineMultiBytes(first: ByteArray, vararg rest: ByteArray): ByteArray {
        var totalLength = first.size
        for (array in rest) {
            totalLength += array.size
        }
        val result = first.copyOf(totalLength)
        var offset = first.size
        for (array in rest) {
            System.arraycopy(array, 0, result, offset, array.size)
            offset += array.size
        }
        return result
    }

    // 高字节在前
    fun convertTwoSignInt(byteArray: ByteArray): Int =
        (byteArray[1].toInt() shl 8) or (byteArray[0].toInt() and 0xFF)

    fun convertTwoUnSignInt(byteArray: ByteArray): Int =
        (byteArray[3].toInt() shl 24) or (byteArray[2].toInt() and 0xFF) or (byteArray[1].toInt() shl 8) or (byteArray[0].toInt() and 0xFF)

    // 无符号
    fun convertFourUnSignInt(byteArray: ByteArray): Int =
        (byteArray[1].toInt() and 0xFF) shl 8 or (byteArray[0].toInt() and 0xFF)

    fun convertFourUnSignLong(byteArray: ByteArray): Long =
        ((byteArray[3].toInt() and 0xFF) shl 24 or (byteArray[2].toInt() and 0xFF) shl 16 or (byteArray[1].toInt() and 0xFF) shl 8 or (byteArray[0].toInt() and 0xFF)).toLong()

    // 16进制转10进制
    fun hexOf2BytesToInt(substring: String): Int {
        return substring.toInt(16)
    }

    // Int 数值转2字节 ByteArray
    fun intToByteArrayOf2Size(value: Int): ByteArray {
        return unsignedShortTo2Byte(value)
    }

    // 数值转 2 字节数组
    fun unsignedShortTo2Byte(s: Int): ByteArray {
        val targets = ByteArray(2)
        targets[0] = (s shr 8 and 0xFF).toByte()
        targets[1] = (s and 0xFF).toByte()
        return targets
    }

    fun hexOf4BytesToInt(hex: String): Int {
        val b = fromHex(hex)
        return b[3].toInt() and 0xFF or (b[2].toInt() and 0xFF shl 8) or (b[1].toInt() and 0xFF shl 16) or (b[0].toInt() and 0xFF shl 24)
    }

    /**
     * 获取 CRC 校验值
     */
    fun crcCheckInt(bytes: ByteArray): Int {
        var crc = 0xA000
        val p = 0xA001
        for (b in bytes) {
            crc = crcResolve(crc, b.toInt(), p)
        }
        return crc.inv()
    }

    /**
     * 校验结果
     */
    private fun crcResolve(crcInt: Int, bInt: Int, p: Int): Int {
        var crc = crcInt
        var b = bInt
        for (i in 0..7) {
            val f = crc and 0x8000 shr 8
            val g = b and 0x80
            if (f xor g != 0) {
                crc = crc shl 1
                crc = crc xor p
            } else {
                crc = crc shl 1
            }
            b = b shl 1
        }
        return crc
    }

    fun getBatteryDate() {
        // 生产日期 2 字节 比如 0x2068,其中日期为最低 5 为：0x2068&0x1f = 8 表示日期;月份 （0x2068>>5）&0x0f= 0x03 表示 3 月;
        // 年份就为 2000+ (0x2068>>9) = 2000 + 0x10 =2016
    }


    fun hexToString(hex: String): String {
        val finalString = StringBuilder()
        val tempString = StringBuilder()
        var i = 0
        while (i < hex.length - 1) {
            val output = hex.substring(i, i + 2)
            val decimal = output.toInt(16)
            finalString.append(decimal.toChar())
            tempString.append(decimal)
            i += 2
        }
        return finalString.toString()
    }

}