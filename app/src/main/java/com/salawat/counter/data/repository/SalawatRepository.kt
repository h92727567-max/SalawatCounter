package com.salawat.counter.data.repository

import com.salawat.counter.data.dao.SalawatDao
import com.salawat.counter.data.entity.DailyRecord
import com.salawat.counter.data.entity.SessionRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SalawatRepository(private val dao: SalawatDao) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): String = LocalDate.now().format(dateFormatter)

    fun getTodayRecord(): Flow<DailyRecord> {
        val today = today()
        return dao.getDailyRecord(today).map { it ?: DailyRecord(date = today) }
    }

    fun getAllRecords(): Flow<List<DailyRecord>> = dao.getAllDailyRecords()

    fun getTotalCount(): Flow<Int> = dao.getTotalCount().map { it ?: 0 }

    fun getRecentSessions(): Flow<List<SessionRecord>> = dao.getRecentSessions()

    suspend fun ensureTodayRecord() {
        val today = today()
        dao.insertDailyRecord(DailyRecord(date = today))
    }

    suspend fun incrementToday() {
        ensureTodayRecord()
        dao.incrementCount(today(), System.currentTimeMillis())
    }

    suspend fun resetToday() {
        ensureTodayRecord()
        dao.resetCount(today(), System.currentTimeMillis())
    }

    suspend fun updateTarget(target: Int) {
        ensureTodayRecord()
        dao.updateTarget(today(), target)
    }

    suspend fun saveSession(count: Int, method: String, startTime: Long, endTime: Long) {
        dao.insertSession(
            SessionRecord(
                date = today(),
                startTime = startTime,
                endTime = endTime,
                count = count,
                method = method
            )
        )
    }
}
