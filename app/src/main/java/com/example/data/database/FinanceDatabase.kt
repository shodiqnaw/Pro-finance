package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        DebtEntity::class,
        DebtInstallmentEntity::class,
        AssetEntity::class,
        AssetCategoryEntity::class,
        UserSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun debtInstallmentDao(): DebtInstallmentDao
    abstract fun assetDao(): AssetDao
    abstract fun assetCategoryDao(): AssetCategoryDao
    abstract fun userSettingDao(): UserSettingDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "financeku_pro_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDefaultCategories(database.categoryDao())
                    populateDefaultAssetCategories(database.assetCategoryDao())
                    populateDefaultAccounts(database.accountDao())
                }
            }
        }

        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaults = listOf(
                CategoryEntity(name = "Gaji", type = "INCOME"),
                CategoryEntity(name = "Bonus", type = "INCOME"),
                CategoryEntity(name = "Investasi", type = "INCOME"),
                CategoryEntity(name = "Bisnis", type = "INCOME"),
                
                CategoryEntity(name = "Makanan", type = "EXPENSE"),
                CategoryEntity(name = "Transportasi", type = "EXPENSE"),
                CategoryEntity(name = "Belanja", type = "EXPENSE"),
                CategoryEntity(name = "Tagihan", type = "EXPENSE"),
                CategoryEntity(name = "Kesehatan", type = "EXPENSE"),
                CategoryEntity(name = "Hiburan", type = "EXPENSE")
            )
            for (cat in defaults) {
                categoryDao.insertCategory(cat)
            }
        }

        private suspend fun populateDefaultAssetCategories(assetCategoryDao: AssetCategoryDao) {
            val defaults = listOf(
                AssetCategoryEntity(name = "Emas"),
                AssetCategoryEntity(name = "Saham"),
                AssetCategoryEntity(name = "Reksa Dana"),
                AssetCategoryEntity(name = "Properti"),
                AssetCategoryEntity(name = "Kendaraan"),
                AssetCategoryEntity(name = "Crypto"),
                AssetCategoryEntity(name = "Koleksi")
            )
            for (acat in defaults) {
                assetCategoryDao.insertAssetCategory(acat)
            }
        }

        private suspend fun populateDefaultAccounts(accountDao: AccountDao) {
            val defaults = listOf(
                AccountEntity(name = "Cash", bankName = "Dompet", accountNumber = "-", initialBalance = 0.0, currentBalance = 0.0),
                AccountEntity(name = "BCA", bankName = "Bank Central Asia", accountNumber = "-", initialBalance = 0.0, currentBalance = 0.0),
                AccountEntity(name = "GoPay", bankName = "Gojek", accountNumber = "-", initialBalance = 0.0, currentBalance = 0.0)
            )
            for (acc in defaults) {
                accountDao.insertAccount(acc)
            }
        }
    }
}
