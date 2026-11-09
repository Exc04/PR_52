package com.example.pr_52.data

import android.content.Context
import com.google.gson.Gson

/**
 * Информация о пользователе. Именно этот объект сериализуется в JSON
 * и сохраняется в SharedPreferences при авторизации.
 */
data class UserProfile(
    val name: String = "",
    val password: String = "",
    val loginAt: Long = 0L
)

/**
 * Последний успешный ответ API — кэш в SharedPreferences (JSON).
 */
data class LastRateCache(
    val pair: String = "",
    val rate: Double = 0.0,
    val rawJson: String = "",
    val savedAt: Long = 0L,
    val source: String = ""
)

object Prefs {

    private const val FILE = "currency_rates_prefs"
    private const val KEY_USER = "user_json"
    private const val KEY_LAST_RATE = "last_rate_json"

    private val gson = Gson()

    private fun prefs(context: Context) =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun saveUser(context: Context, user: UserProfile) {
        prefs(context).edit()
            .putString(KEY_USER, gson.toJson(user))
            .apply()
    }

    fun loadUser(context: Context): UserProfile? {
        val json = prefs(context).getString(KEY_USER, null) ?: return null
        return runCatching { gson.fromJson(json, UserProfile::class.java) }.getOrNull()
    }

    fun clearUser(context: Context) {
        prefs(context).edit().remove(KEY_USER).apply()
    }

    fun saveLastRate(context: Context, cache: LastRateCache) {
        prefs(context).edit()
            .putString(KEY_LAST_RATE, gson.toJson(cache))
            .apply()
    }

    fun loadLastRate(context: Context): LastRateCache? {
        val json = prefs(context).getString(KEY_LAST_RATE, null) ?: return null
        return runCatching { gson.fromJson(json, LastRateCache::class.java) }.getOrNull()
    }
}
