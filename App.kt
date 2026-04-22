package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

/**
 * 📊 DASHBOARD ANA EKRANI
 * accounts ve goals artık AppState'den parametre olarak geliyor.
 * Kendi içinde getAllAccounts() veya fetchAllActiveGoals() çağırmaz.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    accounts: List<Account>,
    goals: List<FinancialGoal>,
    dbManager: DatabaseManager,             // ✅ passed from main.kt
    accountRepo: AccountRepository,         // ✅
    recurringRepo: RecurringRepository,     // ✅
    investmentRepo: InvestmentRepository,   // ✅
    goalWriting: Goal_Writing,              // ✅
    onNavigate: (String) -> Unit
) {

    // --- 🧭 2. STATE YÖNETİMİ ---
    var internalScreen by remember { mutableStateOf("Main") }
    var selectedTableForDetail by remember { mutableStateOf("Expenses") }
    var isRecurringCheckedInSession by remember { mutableStateOf(false) }

    // Diyalog Şalterleri
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddInvestmentDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedAccountForEdit by remember { mutableStateOf<Account?>(null) }

    // Tekrarlı İşlem Onayı
    var pendingRecurringList by remember { mutableStateOf<List<Pair<RecurringTransaction, LocalDate>>>(emptyList()) }
    var showRecurringApproval by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableStateOf(1) }

    // Merkezi Veri Tazeleme
    // ✅ accounts ve goals artık parametre olduğu için burada güncellenmez
    // refreshKey sadece UI'ı yeniden tetiklemek için kullanılır
    fun refreshData() {
        refreshKey++
    }

    // ✅ FIXED LaunchedEffect — only checks recurring, no longer loads accounts/goals
    LaunchedEffect(internalScreen, refreshKey) {
        if (internalScreen == "Main" && !isRecurringCheckedInSession) {
            if (refreshKey == 1) delay(150.milliseconds)
            val pending = recurringRepo.getPendingTransactions()
            if (pending.isNotEmpty()) {
                pendingRecurringList = pending
                showRecurringApproval = true
            }
            isRecurringCheckedInSession = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground,
        floatingActionButton = {
            if (internalScreen == "Main") {
                MultiActionButton(
                    onAddAccount = { showAddAccountDialog = true },
                    onAddIncome = { showAddIncomeDialog = true },
                    onAddExpense = { showAddExpenseDialog = true },
                    onAddInvestment = { showAddInvestmentDialog = true },
                    onAddGoal = { showAddGoalDialog = true }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            when (internalScreen) {
                "Main" -> {
                    val projectionResult = remember(refreshKey) {
                        MonthNet.calculateMonthEndProjection(dbManager, accountRepo)
                    }

                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                        // --- 1. VARLIKLAR ---
                        SectionHeader("VARLIKLAR")

                        if (accounts.isEmpty()) {
                            LoadingPlaceholder()
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(accounts) { account ->
                                    Box(modifier = Modifier.width(280.dp)) {
                                        AccountItem(
                                            account = account,
                                            onClick = { selectedAccountForEdit = account }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- 2. HEDEFLER ---
                        SectionHeader("HEDEFLER")
                        if (goals.isEmpty()) {
                            Text(
                                "Aktif hedef bulunmuyor.",
                                Modifier.padding(16.dp),
                                color = TextSecondary
                            )
                        } else {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp)
                            ) {
                                items(goals) { goal ->
                                    // ✅ balance derived from already-loaded accounts list
                                    val linkedAccount = accounts.find { it.id == goal.linkedAccountId }
                                    FinancialGoalCard(
                                        goal = goal,
                                        currentBalance = linkedAccount?.balance ?: 0.0,
                                        onActionInvestment = { showAddInvestmentDialog = true },
                                        onActionIncome = { showAddIncomeDialog = true }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- 3. ANALİZ BLOĞU ---
                        key(refreshKey) {
                            CenterBlockUI(
                                dbManager = dbManager,
                                accountRepo = accountRepo,
                                refreshTrigger = refreshKey,
                                onNavigateToStats = { onNavigate("STATS") },
                                onNavigateToAllTransactions = { tableName ->
                                    selectedTableForDetail = tableName
                                    internalScreen = "FullTable"
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- 4. PROJEKSİYON BLOĞU ---
                        DownBlockUI(
                            projectionResult = projectionResult,
                            onOpenForecast = { onNavigate("FUTURE") }
                        )
                    }
                }

                "FullTable" -> DetailedFullTablePage(
                    dbManager = dbManager,
                    accounts = accounts,                // pass from App's parameter
                    initialTable = selectedTableForDetail,
                    refreshTrigger = refreshKey,
                    onNavigate = { destination ->
                        if (destination == "DASHBOARD") internalScreen = "Main"
                        else onNavigate(destination) }
                )
            }

            // --- 🛡️ DİALOGLAR ---

            // A. HESAP EKLEME / DÜZENLEME
            if (showAddAccountDialog || selectedAccountForEdit != null) {
                Account_Buton(
                    accountRepo = accountRepo,
                    onAccountAdded = {
                        refreshData()
                        showAddAccountDialog = false
                        selectedAccountForEdit = null
                    },
                    editingAccount = selectedAccountForEdit,
                    onDismiss = {
                        showAddAccountDialog = false
                        selectedAccountForEdit = null
                    }
                )
            }

            // B. GELİR GİRİŞİ
            // ✅ uses accounts param — no getAllAccounts() call
            if (showAddIncomeDialog) {
                Income_Buton(accounts, dbManager) {
                    refreshData()
                    showAddIncomeDialog = false
                }
            }

            // C. HARCAMA GİRİŞİ
            // ✅ uses accounts param — no getAllAccounts() call
            if (showAddExpenseDialog) {
                Expense_Buton(accounts, dbManager) {
                    refreshData()
                    showAddExpenseDialog = false
                }
            }

            // D. YATIRIM GİRİŞİ
            // ✅ uses accounts param — no getAllAccounts() call
            if (showAddInvestmentDialog) {
                Investment_Buton(accounts, dbManager, investmentRepo) {
                    refreshData()
                    showAddInvestmentDialog = false
                }
            }

            // E. HEDEF OLUŞTURMA
            // ✅ uses accounts param — no getAllAccounts() call
            if (showAddGoalDialog) {
                Goal_Buton(
                    accounts = accounts,
                    onAddNewAccount = {
                        showAddGoalDialog = false
                        showAddAccountDialog = true
                    },
                    onConfirm = { goal ->
                        if (goalWriting.saveNewGoal(goal)) refreshData()
                        showAddGoalDialog = false
                    },
                    onDismiss = { showAddGoalDialog = false }
                )
            }

            // F. TEKRARLI İŞLEM ONAYI
            if (showRecurringApproval && pendingRecurringList.isNotEmpty()) {
                RecurringApprovalDialog(
                    pendingTransactions = pendingRecurringList,
                    onConfirm = { approvedList ->
                        recurringRepo.processApprovedTransactions(approvedList)
                        pendingRecurringList = emptyList()
                        showRecurringApproval = false
                        refreshData()
                    },
                    onDismiss = {
                        recurringRepo.updateLastProcessDateToToday()
                        pendingRecurringList = emptyList()
                        showRecurringApproval = false
                    }
                )
            }
        }
    }
}