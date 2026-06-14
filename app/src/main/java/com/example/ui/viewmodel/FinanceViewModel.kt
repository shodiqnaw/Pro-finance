package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.entity.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // --- BASE FLOWS ---
    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val installments: StateFlow<List<DebtInstallmentEntity>> = repository.allInstallments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assets: StateFlow<List<AssetEntity>> = repository.allAssets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assetCategories: StateFlow<List<AssetCategoryEntity>> = repository.allAssetCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SETTINGS (Theme & Currency backed by DB / Datastore) ---
    private val _theme = MutableStateFlow("System")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _currency = MutableStateFlow("IDR")
    val currency: StateFlow<String> = _currency.asStateFlow()

    init {
        // Load initial settings
        viewModelScope.launch {
            repository.getSettingValue("app_theme")?.let { _theme.value = it }
            repository.getSettingValue("app_currency")?.let { _currency.value = it }
        }
    }

    fun updateTheme(newTheme: String) {
        _theme.value = newTheme
        viewModelScope.launch {
            repository.insertSetting("app_theme", newTheme)
        }
    }

    fun updateCurrency(newCurrency: String) {
        _currency.value = newCurrency
        viewModelScope.launch {
            repository.insertSetting("app_currency", newCurrency)
        }
    }

    // --- DERIVED METRICS (Computed Realtime from Flows) ---
    val totalBalance: Flow<Double> = accounts.map { list ->
        list.sumOf { it.currentBalance }
    }

    val totalIncome: Flow<Double> = transactions.map { list ->
        list.filter { it.type == "INCOME" }.sumOf { it.amount }
    }

    val totalExpense: Flow<Double> = transactions.map { list ->
        list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }

    val totalValueDebt: Flow<Double> = debts.map { list ->
        list.filter { it.type == "DEBT" && it.status == "BELUM_LUNAS" }.sumOf { it.amount }
    }

    val totalValueReceivable: Flow<Double> = debts.map { list ->
        list.filter { it.type == "RECEIVABLE" && it.status == "BELUM_LUNAS" }.sumOf { it.amount }
    }

    val totalValueAssets: Flow<Double> = assets.map { list ->
        list.sumOf { it.totalValue }
    }

    // --- ACTIONS ---
    fun addAccount(name: String, bankName: String, accountNumber: String, initialBalance: Double) {
        viewModelScope.launch {
            val acc = AccountEntity(
                name = name,
                bankName = bankName,
                accountNumber = accountNumber,
                initialBalance = initialBalance,
                currentBalance = initialBalance
            )
            repository.insertAccount(acc)
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch { repository.updateAccount(account) }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch { repository.deleteAccount(account) }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name, type = type))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    fun addTransaction(title: String, amount: Double, date: Long, accountId: Int, categoryName: String, type: String, note: String, targetAccountId: Int? = null) {
        viewModelScope.launch {
            val tx = TransactionEntity(
                title = title,
                amount = amount,
                date = date,
                accountId = accountId,
                categoryName = categoryName,
                type = type,
                note = note,
                targetAccountId = targetAccountId
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addDebt(name: String, amount: Double, date: Long, note: String, type: String, accountId: Int) {
        viewModelScope.launch {
            val d = DebtEntity(
                name = name,
                amount = amount,
                originalAmount = amount,
                date = date,
                note = note,
                type = type,
                status = "BELUM_LUNAS",
                accountId = accountId
            )
            repository.insertDebt(d)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    fun addInstallment(debtId: Int, amount: Double, date: Long, note: String, accountId: Int) {
        viewModelScope.launch {
            // Find debt
            val debt = debts.value.find { it.id == debtId }
            if (debt != null) {
                val inst = DebtInstallmentEntity(
                    debtId = debtId,
                    amount = amount,
                    date = date,
                    note = note,
                    accountId = accountId
                )
                repository.insertInstallment(inst, debt)
            }
        }
    }

    fun addAsset(name: String, categoryName: String, quantity: Double, pricePerUnit: Double, unit: String) {
        viewModelScope.launch {
            val asset = AssetEntity(
                name = name,
                categoryName = categoryName,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                totalValue = quantity * pricePerUnit,
                unit = unit
            )
            repository.insertAsset(asset)
        }
    }

    fun updateAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.updateAsset(asset.copy(totalValue = asset.quantity * asset.pricePerUnit))
        }
    }

    fun deleteAsset(asset: AssetEntity) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun addAssetCategory(name: String) {
        viewModelScope.launch {
            repository.insertAssetCategory(AssetCategoryEntity(name = name))
        }
    }

    fun deleteAssetCategory(category: AssetCategoryEntity) {
        viewModelScope.launch {
            repository.deleteAssetCategory(category)
        }
    }

    // --- JSON BACKUP & RESTORE ENGINES (Offline First Sync Mock) ---
    fun exportBackupJson(): String {
        return try {
            val root = JSONObject()

            val accountsArray = JSONArray()
            accounts.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("bankName", it.bankName)
                obj.put("accountNumber", it.accountNumber)
                obj.put("initialBalance", it.initialBalance)
                obj.put("currentBalance", it.currentBalance)
                accountsArray.put(obj)
            }
            root.put("accounts", accountsArray)

            val categoriesArray = JSONArray()
            categories.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("type", it.type)
                categoriesArray.put(obj)
            }
            root.put("categories", categoriesArray)

            val txArray = JSONArray()
            transactions.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("title", it.title)
                obj.put("amount", it.amount)
                obj.put("date", it.date)
                obj.put("accountId", it.accountId)
                obj.put("categoryName", it.categoryName)
                obj.put("type", it.type)
                obj.put("note", it.note)
                obj.put("targetAccountId", it.targetAccountId ?: -1)
                txArray.put(obj)
            }
            root.put("transactions", txArray)

            val debtArray = JSONArray()
            debts.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("amount", it.amount)
                obj.put("originalAmount", it.originalAmount)
                obj.put("date", it.date)
                obj.put("note", it.note)
                obj.put("type", it.type)
                obj.put("status", it.status)
                obj.put("accountId", it.accountId)
                debtArray.put(obj)
            }
            root.put("debts", debtArray)

            val instArray = JSONArray()
            installments.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("debtId", it.debtId)
                obj.put("amount", it.amount)
                obj.put("date", it.date)
                obj.put("note", it.note)
                obj.put("accountId", it.accountId)
                instArray.put(obj)
            }
            root.put("installments", instArray)

            val assetArray = JSONArray()
            assets.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                obj.put("categoryName", it.categoryName)
                obj.put("quantity", it.quantity)
                obj.put("pricePerUnit", it.pricePerUnit)
                obj.put("totalValue", it.totalValue)
                obj.put("unit", it.unit)
                assetArray.put(obj)
            }
            root.put("assets", assetArray)

            val assetCatArray = JSONArray()
            assetCategories.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("name", it.name)
                assetCatArray.put(obj)
            }
            root.put("assetCategories", assetCatArray)

            root.toString(4)
        } catch (e: Exception) {
            ""
        }
    }

    fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            viewModelScope.launch {
                // Restore accounts
                if (root.has("accounts")) {
                    val arr = root.getJSONArray("accounts")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertAccount(
                            AccountEntity(
                                name = obj.getString("name"),
                                bankName = obj.getString("bankName"),
                                accountNumber = obj.getString("accountNumber"),
                                initialBalance = obj.getDouble("initialBalance"),
                                currentBalance = obj.getDouble("currentBalance")
                            )
                        )
                    }
                }

                // Restore categories
                if (root.has("categories")) {
                    val arr = root.getJSONArray("categories")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertCategory(
                            CategoryEntity(
                                name = obj.getString("name"),
                                type = obj.getString("type")
                            )
                        )
                    }
                }

                // Restore assets
                if (root.has("assets")) {
                    val arr = root.getJSONArray("assets")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertAsset(
                            AssetEntity(
                                name = obj.getString("name"),
                                categoryName = obj.getString("categoryName"),
                                quantity = obj.getDouble("quantity"),
                                pricePerUnit = obj.getDouble("pricePerUnit"),
                                totalValue = obj.getDouble("totalValue"),
                                unit = obj.getString("unit")
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- EXPORT PDF GENERATOR (Native, Simple and Reliable) ---
    fun generatePdfReport(context: Context, period: String, filterTransactions: List<TransactionEntity>) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size Portrait
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // Draw Header Title Box
            paint.color = Color.parseColor("#1B5E20") // Dark fintech green
            canvas.drawRect(0f, 0f, 595f, 100f, paint)

            paint.color = Color.WHITE
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("FINANCEKU PRO REPORT", 30f, 45f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Periode: $period  |  Dibuat: " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()), 30f, 75f, paint)

            // Calculate Metrics
            val totalInc = filterTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExp = filterTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val netFlow = totalInc - totalExp

            // Draw Summary Cards
            paint.color = Color.parseColor("#E8F5E9") // Soft Green card
            canvas.drawRect(30f, 130f, 270f, 220f, paint)
            paint.color = Color.parseColor("#FFEBEE") // Soft Red card
            canvas.drawRect(290f, 130f, 565f, 220f, paint)

            paint.color = Color.BLACK
            paint.textSize = 12f
            canvas.drawText("TOTAL PEMASUKAN", 45f, 160f, paint)
            canvas.drawText("TOTAL PENGELUARAN", 305f, 160f, paint)

            paint.textSize = 18f
            paint.isFakeBoldText = true
            paint.color = Color.parseColor("#2E7D32")
            canvas.drawText(formatCurrency(totalInc), 45f, 195f, paint)

            paint.color = Color.parseColor("#C62828")
            canvas.drawText(formatCurrency(totalExp), 305f, 195f, paint)

            // Net Flow Text
            paint.color = Color.BLACK
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("SELISIH (NET FLOW): " + formatCurrency(netFlow), 30f, 255f, paint)

            // Divider Draw
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            paint.color = Color.LTGRAY
            canvas.drawLine(30f, 270f, 565f, 270f, paint)

            // Draw Transaction Table listings
            paint.style = Paint.Style.FILL
            paint.color = Color.DKGRAY
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText("TANGGAL", 30f, 290f, paint)
            canvas.drawText("KATEGORI / REKENING", 120f, 290f, paint)
            canvas.drawText("CATATAN / JUDUL", 280f, 290f, paint)
            canvas.drawText("NOMINAL", 480f, 290f, paint)

            canvas.drawLine(30f, 300f, 565f, 300f, paint)

            paint.isFakeBoldText = false
            paint.color = Color.BLACK
            var yPos = 320f
            val limit = 15 // List maximum 15 on first page
            filterTransactions.take(limit).forEach { tx ->
                val dateStr = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(tx.date))
                canvas.drawText(dateStr, 30f, yPos, paint)
                
                val accName = accounts.value.find { it.id == tx.accountId }?.name ?: "-"
                canvas.drawText("${tx.categoryName} ($accName)", 120f, yPos, paint)
                canvas.drawText(tx.title, 280f, yPos, paint)

                paint.color = if (tx.type == "INCOME") Color.parseColor("#2E7D32") else Color.parseColor("#C62828")
                val sign = if (tx.type == "INCOME") "+" else "-"
                canvas.drawText("$sign${formatCurrency(tx.amount)}", 480f, yPos, paint)

                paint.color = Color.BLACK
                yPos += 22f
            }

            // Draw total item footer indicator
            if (filterTransactions.size > limit) {
                paint.color = Color.GRAY
                paint.textSize = 9f
                canvas.drawText("... dan ${filterTransactions.size - limit} transaksi lainnya", 30f, yPos, paint)
            }

            // Page end
            pdfDocument.finishPage(page)

            // Write File physically
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            val file = File(directory, "Laporan_Keuangan_Financeku_${period.replace(" ", "_")}.pdf")
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(context, "PDF Berhasil di-export ke: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal export PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatCurrency(amount: Double): String {
        return try {
            val format = java.text.NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            format.format(amount).replace("Rp", "Rp ").replace(",00", "")
        } catch (e: Exception) {
            "Rp ${amount.toLong()}"
        }
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
