package com.example.pr_52.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Сохранённый курс валюты — строка таблицы базы данных Room.
 */
@Entity(tableName = "saved_rates")
data class RateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pair: String,
    val rate: Double,
    val note: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
