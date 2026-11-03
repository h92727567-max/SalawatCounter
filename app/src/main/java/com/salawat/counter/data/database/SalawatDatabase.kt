package com.salawat.counter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.salawat.counter.data.dao.SalawatDao
import com.salawat.counter.data.entity.DailyRecord
import com.salawat.counter.data.entity.SessionRecord

@Database(
    entities = [DailyRecord::class, SessionRecord::class],
    version = 1,
    exportSchema = false
)
abstract class SalawatDatabase : RoomDatabase() {
    abstract fun salawatDao(): SalawatDao

    companion object {
        @Volatile
        private var INSTANCE: SalawatDatabase? = null

        fun getDatabase(context: Context): SalawatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalawatDatabase::class.java,
                    "salawat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
