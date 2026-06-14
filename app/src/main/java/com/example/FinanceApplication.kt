package com.example

import android.app.Application
import com.example.data.database.FinanceDatabase
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FinanceApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { FinanceDatabase.getDatabase(this, applicationScope) }
    
    val repository by lazy {
        FinanceRepository(
            db = database,
            accountDao = database.accountDao(),
            categoryDao = database.categoryDao(),
            transactionDao = database.transactionDao(),
            debtDao = database.debtDao(),
            debtInstallmentDao = database.debtInstallmentDao(),
            assetDao = database.assetDao(),
            assetCategoryDao = database.assetCategoryDao(),
            userSettingDao = database.userSettingDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
    }
}
