package com.salawat.counter.data.dao

import androidx.room.*
import com.salawat.counter.data.entity.DailyRecord
import com.salawat.counter.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SalawatDao {
    @Query("SELECT * FROM daily_records WHERE date = :date")
    fun getDailyRecord(date: String): Flow<DailyRecord?>

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllDailyRecords(): Flow<List<DailyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyRecord(record: DailyRecord)

    @Query("UPDATE daily_records SET count = count + 1, lastUpdated = :timestamp WHERE date = :date")
    suspend fun incrementCount(date: String, timestamp: Long)

    @Query("UPDATE daily_records SET count = 0, lastUpdated = :timestamp WHERE date = :date")
    suspend fun resetCount(date: String, timestamp: Long)

    @Query("UPDATE daily_records SET target = :target WHERE date = :date")
    suspend fun updateTarget(date: String, target: Int)

    @Insert
    suspend fun insertSession(session: SessionRecord)

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT 50")
    fun getRecentSessions(): Flow<List<SessionRecord>>

    @Query("SELECT SUM(count) FROM daily_records")
    fun getTotalCount(): Flow<Int?>
}
