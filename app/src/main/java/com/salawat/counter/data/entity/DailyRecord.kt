package com.salawat.counter.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey val date: String,
    val count: Int = 0,
    val target: Int = 100,
    val lastUpdated: Long = System.currentTimeMillis()
)
