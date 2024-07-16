package com.rhizo.common.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

object GsonUtil {
    val mGson by lazy { Gson() }

    inline fun <reified T> fromJson(json: String): T? {
        return try {
            mGson.fromJson(json, object : TypeToken<T>() {}.type) as T
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T> fromJsonNoException(json: String): T? {

         return mGson.fromJson(json, object : TypeToken<T>() {}.type) as T

    }

    fun toJson(data: Any?): String {
        if (data == null) return ""
        return mGson.toJson(data)
    }

    fun <T> jsonToList(json: String, clazz: Class<T>): ArrayList<T> {
        val list = ArrayList<T>()
        try {
            val array = JsonParser().parse(json).asJsonArray
            for (elem in array) {
                list.add(mGson.fromJson(elem, clazz))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

}