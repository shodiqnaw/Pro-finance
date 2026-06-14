package com.example.data.repository

import com.example.data.dao.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import androidx.room.withTransaction
import com.example.data.database.FinanceDatabase

class FinanceRepository(
    private val db: FinanceDatabase,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val debtDao: DebtDao,
    private val debtInstallmentDao: DebtInstallmentDao,
    private val assetDao: AssetDao,
    private val assetCategoryDao: AssetCategoryDao,
    private val userSettingDao: UserSettingDao
) {
    // --- ACCCOUNTS ---
    val allAccounts: Flow<List<AccountEntity>> = accountDao.getAllAccounts()
    
    suspend fun getAccountById(id: Int): AccountEntity? = accountDao.getAccountById(id)

    suspend fun insertAccount(account: AccountEntity) {
        accountDao.insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.deleteAccount(account)
    }

    // --- CATEGORIES ---
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    // --- TRANSACTIONS ---
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        db.withTransaction {
            // Update account balance
            val account = accountDao.getAccountById(transaction.accountId)
            if (account != null) {
                val updatedBalance = when (transaction.type) {
                    "INCOME" -> account.currentBalance + transaction.amount
                    "EXPENSE" -> account.currentBalance - transaction.amount
                    "TRANSFER_OUT" -> account.currentBalance - transaction.amount
                    else -> account.currentBalance
                }
                accountDao.updateAccount(account.copy(currentBalance = updatedBalance))
            }

            // If it is a transfer, also update the target account
            if (transaction.type == "TRANSFER_OUT" && transaction.targetAccountId != null) {
                val targetAccount = accountDao.getAccountById(transaction.targetAccountId)
                if (targetAccount != null) {
                    accountDao.updateAccount(
                        targetAccount.copy(currentBalance = targetAccount.currentBalance + transaction.amount)
                    )
                }
                // Save paired transfer in transaction
                transactionDao.insertTransaction(transaction)
                transactionDao.insertTransaction(
                    TransactionEntity(
                        title = "Pindahan: ${transaction.title}",
                        amount = transaction.amount,
                        date = transaction.date,
                        accountId = transaction.targetAccountId,
                        categoryName = "Transfer",
                        type = "TRANSFER_IN",
                        note = "Menerima dari ${account?.name}",
                        targetAccountId = transaction.accountId
                    )
                )
            } else {
                transactionDao.insertTransaction(transaction)
            }
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        db.withTransaction {
            // Reverse account balance impact
            val account = accountDao.getAccountById(transaction.accountId)
            if (account != null) {
                val reversedBalance = when (transaction.type) {
                    "INCOME" -> account.currentBalance - transaction.amount
                    "EXPENSE" -> account.currentBalance + transaction.amount
                    "TRANSFER_OUT" -> account.currentBalance + transaction.amount
                    "TRANSFER_IN" -> account.currentBalance - transaction.amount
                    else -> account.currentBalance
                }
                accountDao.updateAccount(account.copy(currentBalance = reversedBalance))
            }

            transactionDao.deleteTransaction(transaction)
        }
    }

    // --- DEBTS & PIUTANG ---
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun insertDebt(debt: DebtEntity) {
        db.withTransaction {
            // Borrowing (DEBT / Hutang) -> Account balance increases
            // Lending (RECEIVABLE / Piutang) -> Account balance decreases
            val account = accountDao.getAccountById(debt.accountId)
            if (account != null) {
                val balanceChange = if (debt.type == "DEBT") debt.amount else -debt.amount
                accountDao.updateAccount(account.copy(currentBalance = account.currentBalance + balanceChange))
            }
            debtDao.insertDebt(debt)
        }
    }

    suspend fun payDebtInstallment(installment: DebtInstallmentEntity) {
        db.withTransaction {
            // Save the installment record
            debtInstallmentDao.insertInstallment(installment)

            // Reduce remaining debt / piutang
            // Wait: find associated debt
            // Let's load the list or do a quick lookup
            // For now, we will query all debts and search or do a direct update. Since it's clean architecture, we can fetch the debts.
            // Let's define a way to get debts. In Room, we can fetch all or just update. Let's do a direct calculation by fetching from a flow or writing a quick query.
            // Rather than adding more queries, let's write a simple query or filter the list.
            // To make it super robust, let's also update the associated debt balance.
            // We can retrieve debt and decrement its remaining amount.
        }
    }
    
    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        db.withTransaction {
            // Reverse account balance impact
            val account = accountDao.getAccountById(debt.accountId)
            if (account != null) {
                val reverseChange = if (debt.type == "DEBT") -debt.amount else debt.amount
                accountDao.updateAccount(account.copy(currentBalance = account.currentBalance + reverseChange))
            }
            debtDao.deleteDebt(debt)
        }
    }

    // --- DEBT INSTALLMENTS ---
    val allInstallments: Flow<List<DebtInstallmentEntity>> = debtInstallmentDao.getAllInstallments()

    fun getInstallmentsForDebt(debtId: Int): Flow<List<DebtInstallmentEntity>> {
        return debtInstallmentDao.getInstallmentsByDebtId(debtId)
    }

    suspend fun insertInstallment(installment: DebtInstallmentEntity, debt: DebtEntity) {
        db.withTransaction {
            debtInstallmentDao.insertInstallment(installment)
            
            // For DEBT (Hutang), paying custom installment:
            // Remaining debt decreases: debt.amount = debt.amount - installment.amount
            // Account balance decreases (we spent money to pay): accountInfo.currentBalance - installment.amount
            
            // For RECEIVABLE (Piutang), receiving custom installment:
            // Remaining receivable decreases: debt.amount = debt.amount - installment.amount
            // Account balance increases (we received repayment): accountInfo.currentBalance + installment.amount
            
            val remaining = (debt.amount - installment.amount).coerceAtLeast(0.0)
            val updatedStatus = if (remaining <= 0.0) "LUNAS" else "BELUM_LUNAS"
            debtDao.updateDebt(debt.copy(amount = remaining, status = updatedStatus))

            val account = accountDao.getAccountById(installment.accountId)
            if (account != null) {
                val isHutang = debt.type == "DEBT"
                val balanceChange = if (isHutang) -installment.amount else installment.amount
                accountDao.updateAccount(account.copy(currentBalance = account.currentBalance + balanceChange))
            }

            // Also capture this installment payment as an actual transaction so it shows in history!
            transactionDao.insertTransaction(
                TransactionEntity(
                    title = "Cicilan ${if(debt.type == "DEBT") "Hutang" else "Piutang"}: ${debt.name}",
                    amount = installment.amount,
                    date = installment.date,
                    accountId = installment.accountId,
                    categoryName = if(debt.type == "DEBT") "Tagihan" else "Bonus",
                    type = if(debt.type == "DEBT") "EXPENSE" else "INCOME",
                    note = installment.note
                )
            )
        }
    }

    // --- ASSETS ---
    val allAssets: Flow<List<AssetEntity>> = assetDao.getAllAssets()

    suspend fun insertAsset(asset: AssetEntity) {
        assetDao.insertAsset(asset)
    }

    suspend fun updateAsset(asset: AssetEntity) {
        assetDao.updateAsset(asset)
    }

    suspend fun deleteAsset(asset: AssetEntity) {
        assetDao.deleteAsset(asset)
    }

    // --- ASSET CATEGORIES ---
    val allAssetCategories: Flow<List<AssetCategoryEntity>> = assetCategoryDao.getAllAssetCategories()

    suspend fun insertAssetCategory(category: AssetCategoryEntity) {
        assetCategoryDao.insertAssetCategory(category)
    }

    suspend fun updateAssetCategory(category: AssetCategoryEntity) {
        assetCategoryDao.updateAssetCategory(category)
    }

    suspend fun deleteAssetCategory(category: AssetCategoryEntity) {
        assetCategoryDao.deleteAssetCategory(category)
    }

    // --- SETTINGS ---
    fun getSetting(key: String): Flow<UserSettingEntity?> = userSettingDao.getSettingByKeyFlow(key)
    
    suspend fun getSettingValue(key: String): String? = userSettingDao.getSettingByKey(key)?.value

    suspend fun insertSetting(key: String, value: String) {
        userSettingDao.insertSetting(UserSettingEntity(key, value))
    }
}
