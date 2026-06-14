package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bankName: String,
    val accountNumber: String,
    val initialBalance: Double,
    val currentBalance: Double
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String // "INCOME" or "EXPENSE"
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val date: Long,
    val accountId: Int,
    val categoryName: String,
    val type: String, // "INCOME", "EXPENSE", "TRANSFER_OUT", "TRANSFER_IN"
    val note: String,
    val targetAccountId: Int? = null // for transfer
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double, // Remaining unpaid debt
    val originalAmount: Double,
    val date: Long,
    val note: String,
    val type: String, // "DEBT" (Hutang), "RECEIVABLE" (Piutang)
    val status: String, // "BELUM_LUNAS", "LUNAS"
    val accountId: Int
)

@Entity(tableName = "debt_installments")
data class DebtInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val debtId: Int,
    val amount: Double,
    val date: Long,
    val note: String,
    val accountId: Int
)

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val categoryName: String,
    val quantity: Double,
    val pricePerUnit: Double,
    val totalValue: Double,
    val unit: String
)

@Entity(tableName = "asset_categories")
data class AssetCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "user_settings")
data class UserSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
