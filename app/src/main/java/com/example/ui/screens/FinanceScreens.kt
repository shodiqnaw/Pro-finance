package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.*
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf("Dashboard") }

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val tabs = listOf(
                    Triple("Dashboard", Icons.Default.Dashboard, "Dashboard"),
                    Triple("Histori", Icons.Default.History, "Histori"),
                    Triple("Transfer", Icons.Default.SwapHoriz, "Transfer"),
                    Triple("Rekening", Icons.Default.AccountBalanceWallet, "Rekening"),
                    Triple("Hutang", Icons.Default.TrendingDown, "Hutang"),
                    Triple("Rekap", Icons.Default.BarChart, "Rekap"),
                    Triple("Aset", Icons.Default.Category, "Aset"),
                    Triple("Pengaturan", Icons.Default.Settings, "Pengaturan")
                )
                tabs.forEach { (tabName, icon, label) ->
                    NavigationBarItem(
                        selected = currentTab == tabName,
                        onClick = { currentTab = tabName },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                "Dashboard" -> DashboardScreen(viewModel)
                "Histori" -> TransactionHistoriScreen(viewModel)
                "Transfer" -> TransferSaldoScreen(viewModel)
                "Rekening" -> AccountsScreen(viewModel)
                "Hutang" -> DebtsScreen(viewModel)
                "Rekap" -> RekapScreen(viewModel)
                "Aset" -> AssetsScreen(viewModel)
                "Pengaturan" -> SettingsScreen(viewModel)
            }
        }
    }
}

// --- HELPER METRIC CARDS ---
@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title.uppercase(),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// --- CURRENCY FORMATTER ---
fun formatIDR(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
        format.format(amount).replace("Rp", "Rp ").replace(",00", "")
    } catch (e: Exception) {
        "Rp ${amount.toLong()}"
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: FinanceViewModel) {
    val totalBal by viewModel.totalBalance.collectAsState(0.0)
    val totalInc by viewModel.totalIncome.collectAsState(0.0)
    val totalExp by viewModel.totalExpense.collectAsState(0.0)
    val totalDbt by viewModel.totalValueDebt.collectAsState(0.0)
    val totalRec by viewModel.totalValueReceivable.collectAsState(0.0)
    val totalAst by viewModel.totalValueAssets.collectAsState(0.0)
    val recentTxs by viewModel.transactions.collectAsState(emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Main App Header with Salutation and profile avatar to match HTML structure
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column {
                    Text(
                        text = "GOOD MORNING,",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "FinanceKu Pro",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Net Balance Card Banner - Colored in Beautiful Brand Blue (Rounded 28dp)
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Saldo Keseluruhan",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatIDR(totalBal),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Soft translucent pill indicators for the clean minimal UI
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Color.Green,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Aktif",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Bulan ini",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Mini Grid for Assets & Debt & Receivable
        item {
            Column {
                Text("REKAP AKSES CEPAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Sisa Hutang", formatIDR(totalDbt), Color(0xFFC62828), Icons.Default.ArrowDownward, Modifier.weight(1f))
                    MetricCard("Sisa Piutang", formatIDR(totalRec), Color(0xFF2E7D32), Icons.Default.ArrowUpward, Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCard("Total Aset", formatIDR(totalAst), Color(0xFF00A86B), Icons.Default.Category, Modifier.weight(1f))
                    // Let's calculate Net Wealth
                    val netWealth = totalBal + totalAst + totalRec - totalDbt
                    MetricCard("Kekayaan Bersih", formatIDR(netWealth), Color(0xFF1976D2), Icons.Default.TrendingFlat, Modifier.weight(1f))
                }
            }
        }

        // Mini Mini Chart
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TREN TRANSAKSI (PEMASUKAN vs PENGELUARAN)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val maxVal = maxOf(totalInc, totalExp, 1.0).toFloat()
                        // Draw Bar Chart Comparison
                        val heightInc = (totalInc.toFloat() / maxVal) * 80.dp.toPx()
                        val heightExp = (totalExp.toFloat() / maxVal) * 80.dp.toPx()

                        // Ink text labels/headers
                        drawRect(
                            color = Color(0xFF2E7D32),
                            topLeft = Offset(size.width * 0.25f - 40f, size.height - heightInc),
                            size = Size(80f, heightInc)
                        )
                        drawRect(
                            color = Color(0xFFC62828),
                            topLeft = Offset(size.width * 0.75f - 40f, size.height - heightExp),
                            size = Size(80f, heightExp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("Masuk", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Text("Keluar", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("TRANSAKSI TERBARU", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }
        }

        if (recentTxs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada transaksi saat ini", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            items(recentTxs.take(5)) { tx ->
                TransactionRowItem(tx, viewModel, onEdit = {}, onDelete = { viewModel.deleteTransaction(tx) })
            }
        }
    }
}

// ==========================================
// 2. TRANSACTION LIST & CRUD SCREEN
// ==========================================
@Composable
fun TransactionHistoriScreen(viewModel: FinanceViewModel) {
    val txs by viewModel.transactions.collectAsState(emptyList())
    val acs by viewModel.accounts.collectAsState(emptyList())
    val cats by viewModel.categories.collectAsState(emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var selectedAccount by remember { mutableStateOf("Semua") }
    var selectedType by remember { mutableStateOf("Semua") } // "Semua", "INCOME", "EXPENSE"

    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = txs.filter { tx ->
        val matchesQuery = tx.title.contains(searchQuery, ignoreCase = true) || tx.note.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "Semua" || tx.categoryName == selectedCategory
        val matchesType = selectedType == "Semua" || tx.type == selectedType
        val accountName = acs.find { it.id == tx.accountId }?.name ?: ""
        val matchesAccount = selectedAccount == "Semua" || accountName == selectedAccount
        matchesQuery && matchesCategory && matchesType && matchesAccount
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("HISTORI ELEGAN TRANSAKSI", fontSize = 18.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(12.dp))

            // Search text box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari rincian transaksi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_transaction_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic filter panel selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Type filter buttons
                Button(
                    onClick = { selectedType = if (selectedType == "Semua") "INCOME" else if (selectedType == "INCOME") "EXPENSE" else "Semua" },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tipe: ${if (selectedType == "Semua") "Semua" else if (selectedType == "INCOME") "Pemasukan" else "Pengeluaran"}", fontSize = 10.sp, maxLines = 1)
                }

                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("btn_add_tx_dialog"),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                    Text(" Tambah", fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = "Empty", tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tidak ada transaksi ditemukan", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList) { tx ->
                        TransactionRowItem(tx, viewModel, onEdit = {}, onDelete = { viewModel.deleteTransaction(tx) })
                    }
                }
            }
        }

        // Pop up dialog for direct additions
        if (showAddDialog) {
            AddTransactionDialog(
                accounts = acs,
                categories = cats,
                onDismiss = { showAddDialog = false },
                onSave = { title, amount, accountId, categoryName, type, note ->
                    viewModel.addTransaction(
                        title = title,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        accountId = accountId,
                        categoryName = categoryName,
                        type = type,
                        note = note
                    )
                    showAddDialog = false
                }
            )
        }
    }
}

// ==========================================
// 3. TRANSACTION ROW ITEM COMPOSABLE
// ==========================================
@Composable
fun TransactionRowItem(
    tx: TransactionEntity,
    viewModel: FinanceViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val acs by viewModel.accounts.collectAsState(emptyList())
    val accountName = acs.find { it.id == tx.accountId }?.name ?: "-"

    Card(
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circle type icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            if (tx.type == "INCOME" || tx.type == "TRANSFER_IN") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tx.type == "INCOME" || tx.type == "TRANSFER_IN") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = tx.type,
                        tint = if (tx.type == "INCOME" || tx.type == "TRANSFER_IN") Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.size(16.dp)
                    )
                }
 
                // Details Text
                Column {
                    Text(tx.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                    Text("${tx.categoryName} • $accountName", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    if (tx.note.isNotBlank()) {
                        Text(tx.note, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f), maxLines = 1)
                    }
                }
            }
 
            // Value + Actions Menu
            Column(horizontalAlignment = Alignment.End) {
                val prefix = if (tx.type == "INCOME" || tx.type == "TRANSFER_IN") "+" else "-"
                val textColor = if (tx.type == "INCOME" || tx.type == "TRANSFER_IN") Color(0xFF2E7D32) else Color(0xFFC62828)
                
                Text(
                    text = "$prefix${formatIDR(tx.amount)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// Dialog for adding transactions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("EXPENSE") } // "INCOME" or "EXPENSE"
    var selectedAccountIndex by remember { mutableStateOf(0) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }

    val filteredCats = categories.filter { it.type == selectedType }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tambah Transaksi Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // Toggle Pemasukan / Pengeluaran
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { selectedType = "INCOME" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "INCOME") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pemasukan")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { selectedType = "EXPENSE" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "EXPENSE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selectedType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pengeluaran")
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul/Rincian") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tx_title_field")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_tx_amount_field")
                )

                // Select Account Dropdown simulated simply
                if (accounts.isNotEmpty()) {
                    Text("Pilih Rekening:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        accounts.forEachIndexed { idx, acc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedAccountIndex == idx) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedAccountIndex = idx }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(acc.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Select Category Dropdown simulated
                if (filteredCats.isNotEmpty()) {
                    Text("Kategori:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        filteredCats.take(4).forEachIndexed { idx, cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selectedCategoryIndex == idx) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { selectedCategoryIndex = idx }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Opsional") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("btn_save_tx"),
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val accId = accounts.getOrNull(selectedAccountIndex)?.id ?: 0
                            val catName = filteredCats.getOrNull(selectedCategoryIndex)?.name ?: "Umum"
                            onSave(title, amt, accId, catName, selectedType, note)
                        }
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. TRANSFER SALDO SCREEN
// ==========================================
@Composable
fun TransferSaldoScreen(viewModel: FinanceViewModel) {
    val acs by viewModel.accounts.collectAsState(emptyList())

    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var sourceIdx by remember { mutableStateOf(0) }
    var targetIdx by remember { mutableStateOf(1) }
    var note by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("PINDAH SALDO / TRANSFER REKENING", fontSize = 18.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Keterangan Transfer") },
                    modifier = Modifier.fillMaxWidth().testTag("transfer_desc_field")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Jumlah Uang (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("transfer_amount_field")
                )

                // Select Source
                Text("REKENING ASAL (PENGIRIM):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    acs.forEachIndexed { index, acc ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (sourceIdx == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { sourceIdx = index }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(acc.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Select Target
                Text("REKENING TUJUAN (PENERIMA):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    acs.forEachIndexed { index, acc ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (targetIdx == index) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { targetIdx = index }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(acc.name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan Internal") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_do_transfer"),
                    onClick = {
                        val src = acs.getOrNull(sourceIdx)
                        val trg = acs.getOrNull(targetIdx)
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (src != null && trg != null && src.id != trg.id && amt > 0.0) {
                            viewModel.addTransaction(
                                title = title.ifEmpty { "Transfer antar Dompet" },
                                amount = amt,
                                date = System.currentTimeMillis(),
                                accountId = src.id,
                                categoryName = "Transfer",
                                type = "TRANSFER_OUT",
                                note = note,
                                targetAccountId = trg.id
                            )
                            title = ""
                            amountStr = ""
                            note = ""
                        }
                    }
                ) {
                    Text("PROSES TRANSFER REKENING")
                }
            }
        }
    }
}

// ==========================================
// 5. ACCOUNTS MANAGEMENT SCREEN
// ==========================================
@Composable
fun AccountsScreen(viewModel: FinanceViewModel) {
    val acs by viewModel.accounts.collectAsState(emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("REKENING & DOMPET SAYA", fontSize = 18.sp, fontWeight = FontWeight.Black)
                Button(onClick = { showDialog = true }, modifier = Modifier.testTag("btn_add_account")) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text(" Tambah")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(acs) { acc ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(acc.name, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                Text("${acc.bankName} • No: ${acc.accountNumber}", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Saldo Awal: ${formatIDR(acc.initialBalance)}", fontSize = 10.sp, color = Color.LightGray)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatIDR(acc.currentBalance), fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                IconButton(onClick = { viewModel.deleteAccount(acc) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddAccountDialog(
                onDismiss = { showDialog = false },
                onSave = { name, bank, number, initial ->
                    viewModel.addAccount(name, bank, number, initial)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddAccountDialog(onDismiss: () -> Unit, onSave: (String, String, String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var initialStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tambah Rekening Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Rekening") }, modifier = Modifier.fillMaxWidth().testTag("acc_name_field"))
                OutlinedTextField(value = bank, onValueChange = { bank = it }, label = { Text("Nama Bank / Penyedia") }, modifier = Modifier.fillMaxWidth().testTag("acc_bank_field"))
                OutlinedTextField(value = number, onValueChange = { number = it }, label = { Text("Nomor Rekening") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = initialStr, onValueChange = { initialStr = it }, label = { Text("Saldo Awal (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().testTag("acc_initial_field"))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("btn_save_account"),
                        onClick = {
                            val initial = initialStr.toDoubleOrNull() ?: 0.0
                            onSave(name, bank, number, initial)
                        }
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. DEBTS & RECEIVABLES (HUTANG PIUTANG) SCREEN
// ==========================================
@Composable
fun DebtsScreen(viewModel: FinanceViewModel) {
    val debtsList by viewModel.debts.collectAsState(emptyList())
    val acs by viewModel.accounts.collectAsState(emptyList())

    var showAddDialog by remember { mutableStateOf(false) }
    var showPayDialog by remember { mutableStateOf(false) }
    var targetDebt by remember { mutableStateOf<DebtEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HUTANG & PIUTANG SAYA", fontSize = 18.sp, fontWeight = FontWeight.Black)
                Button(onClick = { showAddDialog = true }, modifier = Modifier.testTag("btn_add_debt")) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text(" Tambah")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (debtsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada hutang piutang tercatat", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(debtsList) { debt ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        val typeLabel = if (debt.type == "DEBT") "HUTANG KELUAR" else "PIUTANG MASUK"
                                        val typeColor = if (debt.type == "DEBT") Color(0xFFC62828) else Color(0xFF2E7D32)
                                        Text(typeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = typeColor)
                                        Text(debt.name, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                        Text(debt.note, fontSize = 12.sp, color = Color.Gray)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatIDR(debt.amount), fontSize = 16.sp, fontWeight = FontWeight.Black)
                                        Text("Status: ${debt.status}", fontSize = 10.sp, color = if(debt.status == "LUNAS") Color(0xFF2E7D32) else Color(0xFFC62828))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = {
                                            targetDebt = debt
                                            showPayDialog = true
                                        },
                                        enabled = debt.status != "LUNAS",
                                        modifier = Modifier.testTag("btn_pay_installment")
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = "Pay")
                                        Text(" Bayar Cicilan")
                                    }
                                    IconButton(onClick = { viewModel.deleteDebt(debt) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddDebtDialog(
                accounts = acs,
                onDismiss = { showAddDialog = false },
                onSave = { name, isDebt, amount, note, accId ->
                    viewModel.addDebt(
                        name = name,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        note = note,
                        type = if (isDebt) "DEBT" else "RECEIVABLE",
                        accountId = accId
                    )
                    showAddDialog = false
                }
            )
        }

        if (showPayDialog && targetDebt != null) {
            PayInstallmentDialog(
                accounts = acs,
                debt = targetDebt!!,
                onDismiss = { showPayDialog = false },
                onSave = { amount, note, accId ->
                    viewModel.addInstallment(
                        debtId = targetDebt!!.id,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        note = "Cicilan: $note",
                        accountId = accId
                    )
                    showPayDialog = false
                }
            )
        }
    }
}

@Composable
fun AddDebtDialog(accounts: List<AccountEntity>, onDismiss: () -> Unit, onSave: (String, Boolean, Double, String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var isDebt by remember { mutableStateOf(true) } // true: Hutang (DEBT), false: Piutang (RECEIVABLE)
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedAccIdx by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tambah Hutang & Piutang", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { isDebt = true },
                        colors = ButtonDefaults.buttonColors(containerColor = if(isDebt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("Hutang (DEBT)") }
                    Spacer(modifier = Modifier.width(4.dp))
                    Button(
                        onClick = { isDebt = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if(!isDebt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) { Text("Piutang (REC)") }
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Kontak") }, modifier = Modifier.fillMaxWidth().testTag("debt_contact_field"))
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Nominal (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().testTag("debt_amount_field"))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Keterangan Opsional") }, modifier = Modifier.fillMaxWidth())

                if (accounts.isNotEmpty()) {
                    Text("Hubungkan Rekening Aliran Saldo:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row {
                        accounts.forEachIndexed { i, acc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedAccIdx == i) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedAccIdx = i }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(acc.name, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("btn_save_debt"),
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val accId = accounts.getOrNull(selectedAccIdx)?.id ?: 0
                            onSave(name, isDebt, amt, note, accId)
                        }
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

@Composable
fun PayInstallmentDialog(accounts: List<AccountEntity>, debt: DebtEntity, onDismiss: () -> Unit, onSave: (Double, String, Int) -> Unit) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedAccIdx by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Bayar Cicilan Kontak: ${debt.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Nominal Cicilan (Rp)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().testTag("installment_amount_field"))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan Cicilan") }, modifier = Modifier.fillMaxWidth())

                if (accounts.isNotEmpty()) {
                    Text("Pilih Rekening Transaksi:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row {
                        accounts.forEachIndexed { i, acc ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedAccIdx == i) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedAccIdx = i }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) { Text(acc.name, fontSize = 11.sp) }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("btn_save_installment_repay"),
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val accId = accounts.getOrNull(selectedAccIdx)?.id ?: 0
                            onSave(amt, note, accId)
                        }
                    ) { Text("Bayar") }
                }
            }
        }
    }
}

// ==========================================
// 7. STATISTICS & REKAP SCREEN WITH PDF ENGINE
// ==========================================
@Composable
fun RekapScreen(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val txs by viewModel.transactions.collectAsState(emptyList())

    var rekapMode by remember { mutableStateOf("Tahunan") } // "Harian", "Mingguan", "Bulanan", "Tahunan"

    val calendar = Calendar.getInstance()
    val filteredList = txs.filter { tx ->
        val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
        when (rekapMode) {
            "Harian" -> {
                txCal.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            }
            "Mingguan" -> {
                txCal.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR) &&
                txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            }
            "Bulanan" -> {
                txCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            }
            else -> { // Tahunan
                txCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
            }
        }
    }

    val totalInc = filteredList.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExp = filteredList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netFlow = totalInc - totalExp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("REKAP & STATISTIK KATEGORI", fontSize = 18.sp, fontWeight = FontWeight.Black)

        // Mode Toggles
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Harian", "Mingguan", "Bulanan", "Tahunan").forEach { mode ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (rekapMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { rekapMode = mode }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mode, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("RINGKASAN PERIODE $rekapMode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pemasukan", fontSize = 11.sp, color = Color.Gray)
                        Text(formatIDR(totalInc), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Column {
                        Text("Pengeluaran", fontSize = 11.sp, color = Color.Gray)
                        Text(formatIDR(totalExp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                    Column {
                        Text("Selisih", fontSize = 11.sp, color = Color.Gray)
                        Text(formatIDR(netFlow), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Native PDF Button Trigger
        Button(
            modifier = Modifier.fillMaxWidth().testTag("btn_pdf_export"),
            onClick = {
                viewModel.generatePdfReport(context, rekapMode, filteredList)
            }
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
            Text(" EXPORT LAPORAN PDF")
        }

        // Mini list of transaction items
        Text("DAFTAR TRANSAKSI (${filteredList.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(2.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(filteredList) { tx ->
                TransactionRowItem(tx = tx, viewModel = viewModel, onEdit = {}, onDelete = { viewModel.deleteTransaction(tx) })
            }
        }
    }
}

// ==========================================
// 8. ASSETS VALUATION SCREEN
// ==========================================
@Composable
fun AssetsScreen(viewModel: FinanceViewModel) {
    val assetsList by viewModel.assets.collectAsState(emptyList())
    val cats by viewModel.assetCategories.collectAsState(emptyList())

    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DAFTAR VALUASI ASET SAYA", fontSize = 18.sp, fontWeight = FontWeight.Black)
                Button(onClick = { showDialog = true }, modifier = Modifier.testTag("btn_add_asset")) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Text(" Tambah")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (assetsList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada aset terdaftar", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(assetsList) { asset ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(asset.name, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                    Text("Kategori: ${asset.categoryName} • Unit: ${asset.quantity} ${asset.unit}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Harga/Unit: ${formatIDR(asset.pricePerUnit)}", fontSize = 11.sp, color = Color.LightGray)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(formatIDR(asset.totalValue), fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                    IconButton(onClick = { viewModel.deleteAsset(asset) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddAssetDialog(
                categories = cats,
                onDismiss = { showDialog = false },
                onSave = { name, category, quantity, price, unit ->
                    viewModel.addAsset(name, category, quantity, price, unit)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddAssetDialog(categories: List<AssetCategoryEntity>, onDismiss: () -> Unit, onSave: (String, String, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedCatIdx by remember { mutableStateOf(0) }
    var quantityStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var unitInput by remember { mutableStateOf("") }

    val activeCategory = categories.getOrNull(selectedCatIdx)?.name ?: "Gram"
    // Auto Unit matching formula
    val matchedUnit = when (activeCategory) {
        "Emas" -> "Gram"
        "Saham" -> "Lembar"
        "Crypto" -> "Coin"
        "Properti" -> "Unit"
        "Kendaraan" -> "Unit"
        else -> "Unit"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.padding(12.dp)) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tambah Aset Baru", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Aset") }, modifier = Modifier.fillMaxWidth().testTag("asset_name_field"))

                if (categories.isNotEmpty()) {
                    Text("Pilih Kategori Aset:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        categories.take(5).forEachIndexed { index, cat ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedCatIdx == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCatIdx = index }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(cat.name, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Jumlah Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().testTag("asset_qty_field"))
                OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Harga Beli Per Unit") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().testTag("asset_price_field"))
                OutlinedTextField(value = unitInput.ifEmpty { matchedUnit }, onValueChange = { unitInput = it }, label = { Text("Nama Satuan/Unit") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        modifier = Modifier.testTag("btn_save_asset"),
                        onClick = {
                            val qty = quantityStr.toDoubleOrNull() ?: 0.0
                            val prc = priceStr.toDoubleOrNull() ?: 0.0
                            onSave(name, activeCategory, qty, prc, unitInput.ifEmpty { matchedUnit })
                        }
                    ) { Text("Simpan") }
                }
            }
        }
    }
}

// ==========================================
// 8. SETTINGS & JSON DATA BACKUP MANAGER
// ==========================================
@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val currentTheme by viewModel.theme.collectAsState()
    val currencyVal by viewModel.currency.collectAsState()
    val context = LocalContext.current

    var showBackupString by remember { mutableStateOf("") }
    var inputString by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("PENGATURAN TEKNIS & CADANGAN", fontSize = 18.sp, fontWeight = FontWeight.Black)

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Preferensi Aplikasi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Mode Tema Visual", fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("System", "Light", "Dark").forEach { t ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentTheme == t) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { viewModel.updateTheme(t) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }

        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Sistem Sinkronisasi JSON (Offline Backup)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                Button(
                    modifier = Modifier.fillMaxWidth().testTag("btn_export_json"),
                    onClick = {
                        showBackupString = viewModel.exportBackupJson()
                    }
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = "Backup")
                    Text(" EXPORT BACKUP KE CADANGAN")
                }

                if (showBackupString.isNotBlank()) {
                    OutlinedTextField(
                        value = showBackupString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Salin Kode Cadangan Ini") },
                        modifier = Modifier.fillMaxWidth().height(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = inputString,
                    onValueChange = { inputString = it },
                    label = { Text("Tempel Kode Cadangan Untuk Restore") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("restore_json_input_field")
                )

                Button(
                    modifier = Modifier.fillMaxWidth().testTag("btn_import_json"),
                    onClick = {
                        if (inputString.isNotBlank()) {
                            val success = viewModel.restoreBackupJson(inputString)
                            if (success) {
                                inputString = ""
                                Toast.makeText(context, "Database Berhasil Dimuat dari JSON!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Gagal Membaca JSON!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Restore")
                    Text(" RESTORE DATABASE SECARA OFFLINE")
                }
            }
        }
    }
}
