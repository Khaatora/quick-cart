package com.senseicoder.quickcart

import android.content.SharedPreferences

private const val CURRENCY_CODE = "CURRENCY_CODE"
private const val CURRENCY_RATE = "CURRENCY_RATE"

class CurrencyManager(private val currencySharedPreferences: SharedPreferences = CurrencySharedPref.sharedPreferences) {
    private val editor = currencySharedPreferences.edit()

    fun setCurrency(currencyPair: Pair<String, Double>) {
        editor.putString(CURRENCY_CODE, currencyPair.first)
        editor.putFloat(CURRENCY_RATE, currencyPair.second.toFloat())
        editor.apply()
    }

    fun getCurrencyPair(): Pair<String, Double> {
        val code = currencySharedPreferences.getString(CURRENCY_CODE,"EGP") ?: "EGP"
        val rate = currencySharedPreferences.getFloat(CURRENCY_RATE,1.0F)
        return Pair(code,rate.toDouble())
    }


}
