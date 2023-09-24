package com.example.ud1000

import android.content.Context
import android.content.SharedPreferences

fun readBoolean(key: String, defaultValue: Boolean): Boolean {
    return SharedPreferencesUtil.read(key, defaultValue) as Boolean
}

fun readFloat(key: String, defaultValue: Float): Float {
    return SharedPreferencesUtil.read(key, defaultValue) as Float
}

fun readInt(key: String, defaultValue: Int): Int {
    return SharedPreferencesUtil.read(key, defaultValue) as Int
}
object SharedPreferencesUtil {
    lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("ud1000Prefs", Context.MODE_PRIVATE)
    }

     inline fun <reified T> read(key: String, defaultValue: T): T {
        return when (T::class) {
            String::class -> sharedPreferences.getString(key, defaultValue as String) as T
            Boolean::class -> sharedPreferences.getBoolean(key, defaultValue as Boolean) as T
            Float::class -> sharedPreferences.getFloat(key, defaultValue as Float) as T
            Int::class -> sharedPreferences.getInt(key, defaultValue as Int) as T
            Long::class -> sharedPreferences.getLong(key, defaultValue as Long) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    fun write(key: String, value: Any?) {
        with(sharedPreferences.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }
}