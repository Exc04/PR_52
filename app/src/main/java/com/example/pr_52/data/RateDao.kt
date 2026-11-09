package com.example.pr_52.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface RateDao {

    @Query("SELECT * FROM saved_rates ORDER BY savedAt DESC")
    suspend fun getAll(): List<RateEntity>

    @Query("SELECT COUNT(*) FROM saved_rates")
    suspend fun count(): Int

    @Insert
    suspend fun insert(rate: RateEntity): Long

    @Update
    suspend fun update(rate: RateEntity)

    @Delete
    suspend fun delete(rate: RateEntity)
}
