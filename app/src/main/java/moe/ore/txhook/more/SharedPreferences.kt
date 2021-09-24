package moe.ore.txhook.more

import android.content.SharedPreferences
import androidx.core.content.edit

fun SharedPreferences.getBooleanOrElse(key: String, block: () -> Boolean) = getBoolean(key, block())

fun SharedPreferences.getIntOrElse(key: String, block: () -> Int) = getInt(key, block())

fun SharedPreferences.getFloatOrElse(key: String, block: () -> Float) = getFloat(key, block())

fun SharedPreferences.getLongOrElse(key: String, block: () -> Long) = getLong(key, block())

fun SharedPreferences.getStringOrElse(key: String, block: () -> String) = getString(key, block())

fun SharedPreferences.put(key: String, string: String) {
    edit(true) {
        putString(key, string)
    }
}

fun SharedPreferences.put(key: String, v: Boolean) {
    edit(true) {
        putBoolean(key, v)
    }
}

fun SharedPreferences.put(key: String, v: Int) {
    edit(true) {
        putInt(key, v)
    }
}

fun SharedPreferences.put(key: String, v: Long) {
    edit(true) {
        putLong(key, v)
    }
}

fun SharedPreferences.put(key: String, v: Float) {
    edit(true) {
        putFloat(key, v)
    }
}