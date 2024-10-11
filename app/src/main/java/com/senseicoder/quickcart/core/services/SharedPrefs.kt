package com.senseicoder.quickcart.core.services

interface SharedPrefs {
    fun setSharedPrefString(key: String, value: String)

    fun setSharedPrefFloat(key: String, value: Float)

    fun getSharedPrefString(key: String, defaultValue : String) : String

    fun setSharedPrefBoolean(key: String, value: Boolean)

    fun getSharedPrefBoolean(key: String, defaultValue : Boolean) : Boolean

    fun logAllSharedPref(tag:String ,nameOfFunction:String)
}