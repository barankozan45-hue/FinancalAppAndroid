package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
fun main() = application {
    val windowState = rememberWindowState(width = 1350.dp, height = 900.dp)

    // 🛠️ 1. MERKEZİ SERVİSLER
    val dbManager = remember { DatabaseManager() }
    val accountRepo = remember { AccountRepository(dbManager) }
    val goalWriter = remember { Goal_Writing(dbManager) }
    val investmentRepo = remember { InvestmentRepository(dbManager) }
    val recurringRepo = remember { RecurringRepository(dbManager) }

    // 🗃️ 2. MERKEZİ STATE HOLDER
    val appState = remember { AppState(accountRepo, goalWriter) }
    val accounts by appState.accounts.collectAsState()
    val goals by appState.goals.collectAsState()

    // 🧭 3. NAVİGASYON VE UI DURUMLARI
    var currentScreen by remember { mutableStateOf("WELCOME") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var refreshKey by remember { mutableIntStateOf(0) }

    // 🛡️ 4. GLOBAL DİALOG STATE'LERİ
    var showAccountDialog by remember { mutableStateOf(false) }
    var selectedAccountForEdit by remember { mutableStateOf<Account?>(null) }
    var showIncomeDialog by remember { mutableStateOf(false) }
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showInvestmentDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showGoalActionDialog by remember { mutableStateOf(false) }
    var activeGoalForAction by remember { mutableStateOf<FinancialGoal?>(null) }
    var isInvestmentAction by remember { mutableStateOf(true) }

    // 🔄 5. REFRESH — one place, one call
    fun triggerRefresh() {
        refreshKey++
        appState.refresh()
    }

    // 🌱 6. SEED on first launch
    LaunchedEffect(Unit) {
        appState.refresh()
    }

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "💎 Finance Pro v3.2 - Master Financial Suite",
        resizable = true
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = true,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = SurfaceColor
                ) {
                    SideMenu(
                        selectedScreen = currentScreen,
                        onScreenSelected = { destination ->
                            currentScreen = destination
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize().background(AppBackground)) {

                if (currentScreen != "WELCOME") {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (currentScreen == "DASHBOARD") "FINANCE PRO" else currentScreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, "Menü", tint = IceWhite)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = SurfaceColor.copy(alpha = 0.95f)
                        )
                    )
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    key(currentScreen, refreshKey) {
                        when (currentScreen) {

                            // ✅ FIXED: accountRepo removed, accounts + goals passed from AppState
                            "WELCOME" -> AppMain(
                                dbManager = dbManager,
                                accounts = accounts,
                                goals = goals,
                                onEnterApp = { currentScreen = "DASHBOARD" },
                                onAddAccount = {
                                    selectedAccountForEdit = null
                                    showAccountDialog = true
                                },
                                onAddIncome = { showIncomeDialog = true },
                                onAddExpense = { showExpenseDialog = true },
                                onAddInvestment = { showInvestmentDialog = true },
                                onAddGoal = { showGoalDialog = true },
                                onMenuClick = { scope.launch { drawerState.open() } }
                            )

                            // ✅ FIXED: accounts + goals passed from AppState
                            "DASHBOARD" -> App(
                                accounts = accounts,
                                goals = goals,
                                dbManager = dbManager,
                                accountRepo = accountRepo,
                                recurringRepo = RecurringRepository(dbManager),   // already remembered in main
                                investmentRepo = investmentRepo,
                                goalWriting = goalWriter,
                                onNavigate = { currentScreen = it }
                            )

                            "STATS" -> StatisticPage(
                                dbManager = dbManager,
                                onNavigate = { currentScreen = it },
                                refreshTrigger = refreshKey
                            )

                            // ✅ FIXED: accountRepo removed, accounts passed from AppState
                            "ACCOUNTS" -> AccountPage(
                                accounts = accounts,
                                onAccountClick = { account ->
                                    selectedAccountForEdit = account
                                    showAccountDialog = true
                                },
                                onAddAccountClick = {
                                    selectedAccountForEdit = null
                                    showAccountDialog = true
                                },
                                onNavigate = { currentScreen = it }
                            )

                            "FUTURE" -> ForecastPage(
                                goals = goals,
                                accountRepo = accountRepo,
                                projectionResult = MonthNet.calculateMonthEndProjection(
                                    dbManager,
                                    accountRepo
                                ),
                                onOpenSimulation = { currentScreen = "FULL_FORECAST" },
                                onGoalInvestment = { goal ->
                                    activeGoalForAction = goal
                                    isInvestmentAction = true
                                    showGoalActionDialog = true
                                },
                                onGoalIncome = { goal ->
                                    activeGoalForAction = goal
                                    isInvestmentAction = false
                                    showGoalActionDialog = true
                                }
                            )

                            "FULL_FORECAST" -> FullForecastScreen(dbManager, accountRepo)

                            else -> {
                                if (currentScreen.startsWith("FULL_TABLE_")) {
                                    val tableType = currentScreen.removePrefix("FULL_TABLE_")
                                    DetailedFullTablePage(
                                        dbManager = dbManager,
                                        accounts = accounts,                // from AppState
                                        initialTable = tableType,
                                        refreshTrigger = refreshKey,
                                        onNavigate = { currentScreen = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 🛡️ GLOBAL DİALOGLAR ---

        if (showAccountDialog) {
            Account_Buton(
                accountRepo = accountRepo,
                editingAccount = selectedAccountForEdit,
                onAccountAdded = {
                    triggerRefresh()
                    showAccountDialog = false
                },
                onDismiss = {
                    showAccountDialog = false
                    selectedAccountForEdit = null
                }
            )
        }

        if (showGoalActionDialog && activeGoalForAction != null) {
            val linkedAccount = accounts.find {
                it.id == activeGoalForAction!!.linkedAccountId
            }
            QuickActionDialog(
                goal = activeGoalForAction!!,
                linkedAccountName = linkedAccount?.name ?: "Bilinmeyen Hesap",
                isInvestment = isInvestmentAction,
                onConfirm = { amount ->
                    if (amount > 0 && linkedAccount != null) {
                        val incomeRepo = IncomeRepository(dbManager)
                        incomeRepo.addIncome(
                            Income(
                                amount = amount,
                                accountId = linkedAccount.id,
                                category = if (isInvestmentAction) "Yatırım" else "Gelir",
                                date = java.time.LocalDate.now().toString()
                            )
                        )
                    }
                    triggerRefresh()
                    showGoalActionDialog = false
                },
                onDismiss = { showGoalActionDialog = false }
            )
        }


        if (showIncomeDialog) {
            Income_Buton(
                accounts = accounts,
                dbManager = dbManager
            ) { triggerRefresh(); showIncomeDialog = false }
        }

        if (showExpenseDialog) {
            Expense_Buton(
                accounts = accounts,
                dbManager = dbManager
            ) { triggerRefresh(); showExpenseDialog = false }
        }

        if (showInvestmentDialog) {
            Investment_Buton(
                accounts = accounts,
                dbManager = dbManager,
                investmentRepo = investmentRepo
            ) { triggerRefresh(); showInvestmentDialog = false }
        }

        if (showGoalDialog) {
            Goal_Buton(
                accounts = accounts,
                onAddNewAccount = {
                    showGoalDialog = false
                    selectedAccountForEdit = null
                    showAccountDialog = true
                },
                onConfirm = { newGoal ->
                    if (goalWriter.saveNewGoal(newGoal)) triggerRefresh()
                    showGoalDialog = false
                },
                onDismiss = { showGoalDialog = false }
            )
        }
    }
}