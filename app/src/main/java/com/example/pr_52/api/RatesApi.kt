package com.example.pr_52.api

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonParser

/**
 * Результат запроса: пара, курс, сырой JSON и название источника.
 */
data class RateResult(
    val pair: String,
    val rate: Double,
    val rawJson: String,
    val source: String
)

/**
 * Запрос курса валют.
 *
 * Основной источник — Currate API: https://currate.ru/api/?get=rates&pairs=USDRUB&key=KEY
 * Ключ вписывается в CURRATE_API_KEY ниже. Если ключ пуст или Currate недоступен,
 * используется бесплатный источник без регистрации — https://open.er-api.com/v6/latest/USD
 */
class RatesApi(context: Context) {

    private val queue: RequestQueue = Volley.newRequestQueue(context.applicationContext)
    private val gson = Gson()

    fun fetchRate(
        pair: String,
        onSuccess: (RateResult) -> Unit,
        onError: (message: String) -> Unit
    ) {
        if (CURRATE_API_KEY.isNotBlank()) {
            requestCurrate(pair, onSuccess) { requestFallback(pair, onSuccess, onError) }
        } else {
            requestFallback(pair, onSuccess, onError)
        }
    }

    private fun requestCurrate(
        pair: String,
        onSuccess: (RateResult) -> Unit,
        onFailure: () -> Unit
    ) {
        val url = "https://currate.ru/api/?get=rates&pairs=$pair&key=$CURRATE_API_KEY"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { raw ->
                val root = runCatching { JsonParser.parseString(raw).asJsonObject }.getOrNull()
                val value = root?.get("data")
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject
                    ?.get(pair)
                    ?.takeIf { it.isJsonPrimitive }
                    ?.asString
                val rate = value?.replace(',', '.')?.toDoubleOrNull()

                if (root?.get("status")?.asInt == 200 && rate != null) {
                    onSuccess(RateResult(pair, rate, raw, "Currate API"))
                } else {
                    onFailure()
                }
            },
            { onFailure() }
        )
        request.retryPolicy = DefaultRetryPolicy(12_000, 1, 1f)
        queue.add(request)
    }

    private fun requestFallback(
        pair: String,
        onSuccess: (RateResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val base = pair.take(3)
        val quote = pair.drop(3)
        val url = "https://open.er-api.com/v6/latest/$base"

        val request = StringRequest(
            Request.Method.GET,
            url,
            { raw ->
                val root = runCatching { JsonParser.parseString(raw).asJsonObject }.getOrNull()
                val rate = root?.get("rates")
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject
                    ?.get(quote)
                    ?.takeIf { it.isJsonPrimitive }
                    ?.asDouble

                if (root?.get("result")?.asString == "success" && rate != null) {
                    val displayJson = gson.toJson(
                        linkedMapOf(
                            "result" to "success",
                            "base_code" to base,
                            "pair" to pair,
                            "rate" to rate
                        )
                    )
                    onSuccess(RateResult(pair, rate, displayJson, "open.er-api.com"))
                } else {
                    onError("пара $pair не найдена")
                }
            },
            { error ->
                onError(error.message?.let { "нет соединения ($it)" } ?: "нет соединения")
            }
        )
        request.retryPolicy = DefaultRetryPolicy(15_000, 1, 1f)
        queue.add(request)
    }

    companion object {

        /** Ключ Currate API. Пусто — значит берём бесплатный резервный источник. */
        private const val CURRATE_API_KEY = ""

        /** Популярные пары для выпадающего списка. */
        val POPULAR_PAIRS = listOf(
            "USDRUB", "EURRUB", "USDEUR", "GBPRUB", "CNYRUB",
            "JPYRUB", "TRYRUB", "KZTRUB", "USDUAH", "EURUSD"
        )
    }
}
