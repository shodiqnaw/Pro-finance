package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY date DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)
}

@Dao
interface DebtInstallmentDao {
    @Query("SELECT * FROM debt_installments ORDER BY date DESC")
    fun getAllInstallments(): Flow<List<DebtInstallmentEntity>>

    @Query("SELECT * FROM debt_installments WHERE debtId = :debtId ORDER BY date DESC")
    fun getInstallmentsByDebtId(debtId: Int): Flow<List<DebtInstallmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstallment(installment: DebtInstallmentEntity): Long

    @Delete
    suspend fun deleteInstallment(installment: DebtInstallmentEntity)
}

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY name ASC")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity): Long

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)
}

@Dao
interface AssetCategoryDao {
    @Query("SELECT * FROM asset_categories ORDER BY name ASC")
    fun getAllAssetCategories(): Flow<List<AssetCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssetCategory(category: AssetCategoryEntity): Long

    @Update
    suspend fun updateAssetCategory(category: AssetCategoryEntity)

    @Delete
    suspend fun deleteAssetCategory(category: AssetCategoryEntity)
}

@Dao
interface UserSettingDao {
    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    suspend fun getSettingByKey(key: String): UserSettingEntity?

    @Query("SELECT * FROM user_settings WHERE `key` = :key")
    fun getSettingByKeyFlow(key: String): Flow<UserSettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: UserSettingEntity)
}
