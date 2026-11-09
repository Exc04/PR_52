package com.example.pr_52.data

import android.content.Context

/**
 * Небольшая прослойка над DAO, чтобы экраны не работали с базой напрямую.
 */
class RatesRepository(context: Context) {

    private val dao = AppDatabase.get(context).rateDao()

    suspend fun all(): List<RateEntity> = dao.getAll()

    suspend fun count(): Int = dao.count()

    suspend fun add(rate: RateEntity): Long = dao.insert(rate)

    suspend fun update(rate: RateEntity) = dao.update(rate)

    suspend fun delete(rate: RateEntity) = dao.delete(rate)
}
