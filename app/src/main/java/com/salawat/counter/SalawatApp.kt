package com.salawat.counter

import android.app.Application
import com.salawat.counter.data.database.SalawatDatabase
import com.salawat.counter.data.repository.SalawatRepository

class SalawatApp : Application() {
    val database by lazy { SalawatDatabase.getDatabase(this) }
    val repository by lazy { SalawatRepository(database.salawatDao()) }
}
