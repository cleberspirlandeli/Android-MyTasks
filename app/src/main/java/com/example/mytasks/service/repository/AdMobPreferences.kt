package com.example.mytasks.service.repository

import android.content.Context
import android.content.SharedPreferences

class AdMobPreferences(context: Context) {

    private val mPreferences: SharedPreferences =
        context.getSharedPreferences("MyTasksShared", Context.MODE_PRIVATE)

    fun putInt(key: String, value: Int) {
        mPreferences.edit().putInt(key, value).apply()
    }

    fun putString(key: String, value: String) {
        mPreferences.edit().putString(key, value).apply()
    }

    fun remove(key: String) {
        mPreferences.edit().remove(key).apply()
    }

    fun getString(key: String): String {
        return mPreferences.getString(key, "") ?: ""
    }

    fun getInt(key: String): Int {
        return mPreferences.getInt(key, 0) ?: 0
    }

}
