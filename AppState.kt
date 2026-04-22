package com.example.sql_arac

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Single source of truth for app-wide data.
 * All screens read from here — no screen calls getAllAccounts() directly.
 */
class AppState(
    private val accountRepo: AccountRepository,
    private val goalWriting: Goal_Writing
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _goals = MutableStateFlow<List<FinancialGoal>>(emptyList())
    val goals: StateFlow<List<FinancialGoal>> = _goals.asStateFlow()

    // Call this once on startup and after every mutation
    fun refresh() {
        scope.launch {
            _accounts.value = accountRepo.getAllAccounts()
            _goals.value = goalWriting.fetchAllActiveGoals()
        }
    }
}