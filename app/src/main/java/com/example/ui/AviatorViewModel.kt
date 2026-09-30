package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AuthResult
import com.example.data.AviatorDatabase
import com.example.data.AviatorRepository
import com.example.data.SubscriberActivityEntity
import com.example.data.UserAccountEntity
import com.example.domain.AviatorSignalEngine
import com.example.domain.BookmakerPlatform
import com.example.domain.ChartAnalysisResult
import com.example.domain.KwanzaBetSplit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.random.Random

object AviatorRoutes {
    const val USER_LOGIN = "user_login"
    const val BOT_BROWSER = "bot_browser"
    const val ADMIN_AUTH = "admin_auth"
    const val ADMIN_DASHBOARD = "admin_dashboard"
}

enum class AppScreen {
    LOGIN,
    BOT_BROWSER,
    ADMIN_AUTH,
    ADMIN_PANEL
}

enum class HudOverlayMode {
    EXPANDED_RADAR,
    COMPACT_PILL,
    CALCULATOR_KZ
}

data class AviatorUiState(
    val currentScreen: AppScreen = AppScreen.LOGIN,
    val loggedInUser: UserAccountEntity? = null,
    val loginError: String? = null,
    val adminAuthUnlocked: Boolean = false,
    val adminOperatorId: String = "ADMIN-MASTER-AO",
    val adminAuthError: String? = null,
    val adminStatusBanner: String? = null,
    val activeBookmaker: BookmakerPlatform = BookmakerPlatform.BANTUBET,
    val currentBrowserUrl: String = BookmakerPlatform.BANTUBET.baseUrl,
    val isPageLoading: Boolean = false,
    val showSimulatedFlightCanvasUnderlay: Boolean = false,
    val hudMode: HudOverlayMode = HudOverlayMode.EXPANDED_RADAR,
    val isFlightRunning: Boolean = false,
    val isAutoCycleRadarEnabled: Boolean = false,
    val currentFlightMultiplier: Double = 1.00,
    val simulatedCrashTarget: Double = 2.85,
    val totalStakeKz: Int = 2000,
    val recentVelas: List<Double> = emptyList(),
    val analysis: ChartAnalysisResult = AviatorSignalEngine.analyzeChart(emptyList(), 1.00, false),
    val kwanzaSplit: KwanzaBetSplit = AviatorSignalEngine.calculateKwanzaSplit(2000, 1.48, 2.65),
    val allUsers: List<UserAccountEntity> = emptyList(),
    val subscriberActivities: List<SubscriberActivityEntity> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AviatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AviatorRepository =
        AviatorRepository(AviatorDatabase.getInstance(application).aviatorDao())

    private val _currentScreen = MutableStateFlow(AppScreen.LOGIN)
    private val _loggedInUser = MutableStateFlow<UserAccountEntity?>(null)
    private val _loginError = MutableStateFlow<String?>(null)
    private val _adminAuthUnlocked = MutableStateFlow(false)
    private val _adminOperatorId = MutableStateFlow("ADMIN-MASTER-AO")
    private val _adminAuthError = MutableStateFlow<String?>(null)
    private val _adminStatusBanner = MutableStateFlow<String?>(null)

    private val _activeBookmaker = MutableStateFlow(BookmakerPlatform.BANTUBET)
    private val _currentBrowserUrl = MutableStateFlow(BookmakerPlatform.BANTUBET.baseUrl)
    private val _isPageLoading = MutableStateFlow(false)
    private val _showSimulatedFlightCanvasUnderlay = MutableStateFlow(false)
    private val _hudMode = MutableStateFlow(HudOverlayMode.EXPANDED_RADAR)

    private val _isFlightRunning = MutableStateFlow(false)
    private val _isAutoCycleRadarEnabled = MutableStateFlow(false)
    private val _currentFlightMultiplier = MutableStateFlow(1.00)
    private val _simulatedCrashTarget = MutableStateFlow(2.85)
    private val _totalStakeKz = MutableStateFlow(2000)

    private var flightTickerJob: Job? = null

    private val activeBookmakerVelasFlow = _activeBookmaker.flatMapLatest { platform ->
        repository.observeVelas(platform)
    }

    val uiState: StateFlow<AviatorUiState> = combine(
        listOf(
            _currentScreen,
            _loggedInUser,
            _loginError,
            _adminAuthUnlocked,
            _adminOperatorId,
            _adminAuthError,
            _adminStatusBanner,
            _activeBookmaker,
            _currentBrowserUrl,
            _isPageLoading,
            _showSimulatedFlightCanvasUnderlay,
            _hudMode,
            _isFlightRunning,
            _isAutoCycleRadarEnabled,
            _currentFlightMultiplier,
            _simulatedCrashTarget,
            _totalStakeKz,
            activeBookmakerVelasFlow,
            repository.allUsers,
            repository.subscriberActivities
        )
    ) { args ->
        val screen = args[0] as AppScreen
        val user = args[1] as UserAccountEntity?
        val loginErr = args[2] as String?
        val adminUnlocked = args[3] as Boolean
        val adminOpId = args[4] as String
        val adminErr = args[5] as String?
        val adminBanner = args[6] as String?
        val bookmaker = args[7] as BookmakerPlatform
        val browserUrl = args[8] as String
        val pageLoading = args[9] as Boolean
        val showSimUnderlay = args[10] as Boolean
        val hudMode = args[11] as HudOverlayMode
        val flightRunning = args[12] as Boolean
        val autoCycle = args[13] as Boolean
        val currentMult = args[14] as Double
        val crashTarget = args[15] as Double
        val stakeKz = args[16] as Int
        @Suppress("UNCHECKED_CAST")
        val velaEntities = args[17] as List<com.example.data.VelaHistoryEntity>
        @Suppress("UNCHECKED_CAST")
        val users = args[18] as List<UserAccountEntity>
        @Suppress("UNCHECKED_CAST")
        val activities = args[19] as List<SubscriberActivityEntity>

        val recentMultipliers = velaEntities.map { it.multiplier }
        val analysis = AviatorSignalEngine.analyzeChart(
            recentVelasNewestFirst = recentMultipliers,
            currentFlightMultiplier = currentMult,
            isFlightRunning = flightRunning
        )
        val split = AviatorSignalEngine.calculateKwanzaSplit(
            totalStakeKz = stakeKz,
            protectionMultiplier = analysis.protectionMultiplier,
            targetExitMultiplier = analysis.targetExitMultiplier
        )

        AviatorUiState(
            currentScreen = screen,
            loggedInUser = user,
            loginError = loginErr,
            adminAuthUnlocked = adminUnlocked,
            adminOperatorId = adminOpId,
            adminAuthError = adminErr,
            adminStatusBanner = adminBanner,
            activeBookmaker = bookmaker,
            currentBrowserUrl = browserUrl,
            isPageLoading = pageLoading,
            showSimulatedFlightCanvasUnderlay = showSimUnderlay,
            hudMode = hudMode,
            isFlightRunning = flightRunning,
            isAutoCycleRadarEnabled = autoCycle,
            currentFlightMultiplier = currentMult,
            simulatedCrashTarget = crashTarget,
            totalStakeKz = stakeKz,
            recentVelas = recentMultipliers,
            analysis = analysis,
            kwanzaSplit = split,
            allUsers = users,
            subscriberActivities = activities
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AviatorUiState()
    )

    private val _reloadTrigger = MutableStateFlow(0)
    val reloadTrigger: StateFlow<Int> = _reloadTrigger.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearAdminAuthError() {
        _adminAuthError.value = null
    }

    fun clearAdminBanner() {
        _adminStatusBanner.value = null
    }

    fun loginWithCredentials(
        username: String,
        password: String,
        onNavigateToBrowser: () -> Unit = {},
        onNavigateToAdminDashboard: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _loginError.value = null
            // Direct Admin Master shortcut if entered on User Login
            if (username.trim().equals("admin", ignoreCase = true) && password.trim() == "admin244") {
                _adminAuthUnlocked.value = true
                _adminOperatorId.value = "ADMIN-MASTER-AO"
                _currentScreen.value = AppScreen.ADMIN_PANEL
                onNavigateToAdminDashboard()
                return@launch
            }

            when (val res = repository.authenticateUser(username, password)) {
                is AuthResult.Success -> {
                    _loggedInUser.value = res.account
                    val firstAllowed = BookmakerPlatform.entries.firstOrNull {
                        res.account.allowedBookmakers.contains(it.id, ignoreCase = true)
                    } ?: BookmakerPlatform.BANTUBET
                    _activeBookmaker.value = firstAllowed
                    _currentBrowserUrl.value = firstAllowed.baseUrl
                    _currentScreen.value = AppScreen.BOT_BROWSER
                    onNavigateToBrowser()
                }
                is AuthResult.Error -> {
                    _loginError.value = res.message
                }
            }
        }
    }

    fun authenticateAdmin(
        operatorId: String,
        masterKey: String,
        onSuccess: () -> Unit = {}
    ) {
        val cleanOp = operatorId.trim().ifEmpty { "ADMIN-MASTER-AO" }
        val cleanKey = masterKey.trim()

        val isValidKey = cleanKey == "admin244" ||
            cleanKey.equals("ao2026", ignoreCase = true) ||
            cleanKey == "244"

        if (!isValidKey) {
            _adminAuthError.value =
                "Chave Mestre inválida. Utilize a credencial administrativa (Padrão: admin244)."
            return
        }

        _adminAuthError.value = null
        _adminOperatorId.value = cleanOp.uppercase()
        _adminAuthUnlocked.value = true
        _currentScreen.value = AppScreen.ADMIN_PANEL
        onSuccess()
    }

    fun logoutAdmin(onNavigateOut: () -> Unit = {}) {
        _adminAuthUnlocked.value = false
        _adminAuthError.value = null
        _currentScreen.value = AppScreen.LOGIN
        onNavigateOut()
    }

    fun loginDirectlyFromAdmin(
        account: UserAccountEntity,
        onNavigateToBrowser: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _loggedInUser.value = account
            val firstAllowed = BookmakerPlatform.entries.firstOrNull {
                account.allowedBookmakers.contains(it.id, ignoreCase = true)
            } ?: BookmakerPlatform.BANTUBET
            _activeBookmaker.value = firstAllowed
            _currentBrowserUrl.value = firstAllowed.baseUrl
            _currentScreen.value = AppScreen.BOT_BROWSER
            repository.logSubscriberActivity(
                user = account,
                actionType = "LOGIN",
                bookmaker = firstAllowed,
                details = "Sessão de teste iniciada a partir do Painel Admin em ${firstAllowed.displayName}"
            )
            onNavigateToBrowser()
        }
    }

    fun openAdminAuthScreen() {
        _adminAuthError.value = null
        _currentScreen.value = AppScreen.ADMIN_AUTH
    }

    fun navigateBackToLogin() {
        stopFlightSimulation(recordCurrent = false)
        _isAutoCycleRadarEnabled.value = false
        _currentScreen.value = AppScreen.LOGIN
    }

    fun selectBookmaker(platform: BookmakerPlatform) {
        _activeBookmaker.value = platform
        _currentBrowserUrl.value = platform.baseUrl
        viewModelScope.launch {
            repository.logSubscriberActivity(
                user = _loggedInUser.value,
                actionType = "BOOKMAKER_SWITCH",
                bookmaker = platform,
                details = "Alternou navegador tático para ${platform.displayName} (${platform.shortTag})"
            )
        }
    }

    fun onWebViewUrlChanged(newUrl: String) {
        if (newUrl.isNotBlank()) {
            _currentBrowserUrl.value = newUrl
        }
    }

    fun onWebViewLoadingChanged(loading: Boolean) {
        _isPageLoading.value = loading
    }

    fun requestReloadWebView() {
        _reloadTrigger.value += 1
    }

    fun toggleSimulatedUnderlay() {
        _showSimulatedFlightCanvasUnderlay.value = !_showSimulatedFlightCanvasUnderlay.value
    }

    fun setHudMode(mode: HudOverlayMode) {
        _hudMode.value = mode
    }

    fun setTotalStakeKz(stakeKz: Int) {
        _totalStakeKz.value = stakeKz.coerceIn(100, 500_000)
    }

    fun recordManualOrCapturedVela(multiplier: Double) {
        viewModelScope.launch {
            repository.recordVela(
                platform = _activeBookmaker.value,
                multiplier = multiplier,
                loggedInUser = _loggedInUser.value
            )
        }
    }

    fun undoLastVela() {
        viewModelScope.launch {
            repository.undoLastVela(_activeBookmaker.value)
        }
    }

    fun resetBookmakerHistory() {
        viewModelScope.launch {
            repository.resetVelasForPlatform(_activeBookmaker.value)
        }
    }

    fun toggleLiveFlightTracker() {
        if (_isFlightRunning.value) {
            stopFlightSimulation(recordCurrent = true)
        } else {
            startLiveFlightRound()
        }
    }

    fun toggleAutoCycleRadar() {
        val newAuto = !_isAutoCycleRadarEnabled.value
        _isAutoCycleRadarEnabled.value = newAuto
        if (newAuto && !_isFlightRunning.value) {
            startLiveFlightRound()
        } else if (!newAuto) {
            stopFlightSimulation(recordCurrent = false)
        }
    }

    private fun generateNextRealisticTarget(recentVelas: List<Double>): Double {
        val roll = Random.nextDouble()
        val hasRecentBlues = recentVelas.take(2).all { it < 2.00 }
        val raw = when {
            hasRecentBlues && roll < 0.62 -> Random.nextDouble(2.05, 5.40)
            roll < 0.45 -> Random.nextDouble(1.12, 1.92)
            roll < 0.84 -> Random.nextDouble(2.02, 4.85)
            else -> Random.nextDouble(10.10, 24.50)
        }
        return (raw * 100.0).toInt() / 100.0
    }

    private fun startLiveFlightRound() {
        flightTickerJob?.cancel()
        val target = generateNextRealisticTarget(uiState.value.recentVelas)
        _simulatedCrashTarget.value = target
        _currentFlightMultiplier.value = 1.00
        _isFlightRunning.value = true

        flightTickerJob = viewModelScope.launch {
            var elapsedTicks = 0
            while (_isFlightRunning.value) {
                delay(110L)
                elapsedTicks++
                val nextMult =
                    1.00 + (elapsedTicks * 0.032) + (elapsedTicks.toDouble().pow(1.65) * 0.0045)
                val rounded = ((nextMult * 100.0).toInt()) / 100.0

                if (_isAutoCycleRadarEnabled.value && rounded >= target) {
                    _currentFlightMultiplier.value = target
                    _isFlightRunning.value = false
                    repository.recordVela(
                        platform = _activeBookmaker.value,
                        multiplier = target,
                        loggedInUser = _loggedInUser.value
                    )
                    delay(2800L)
                    if (_isAutoCycleRadarEnabled.value) {
                        startLiveFlightRound()
                    }
                    break
                } else if (rounded >= 99.00) {
                    _currentFlightMultiplier.value = 99.00
                    stopFlightSimulation(recordCurrent = true)
                    break
                } else {
                    _currentFlightMultiplier.value = rounded
                }
            }
        }
    }

    private fun stopFlightSimulation(recordCurrent: Boolean) {
        val finalMult = _currentFlightMultiplier.value
        val wasRunning = _isFlightRunning.value
        _isFlightRunning.value = false
        flightTickerJob?.cancel()
        if (recordCurrent && wasRunning && finalMult >= 1.01) {
            viewModelScope.launch {
                repository.recordVela(
                    platform = _activeBookmaker.value,
                    multiplier = finalMult,
                    loggedInUser = _loggedInUser.value
                )
            }
        }
    }

    // --- Admin Panel Operations ---

    fun createNewAccount(
        username: String,
        password: String,
        fullName: String,
        phoneAngola: String,
        planName: String,
        priceKwanzas: Int,
        durationDays: Int,
        allowedBookmakers: String
    ) {
        viewModelScope.launch {
            val result = repository.createUserAccount(
                username = username,
                password = password,
                fullName = fullName,
                phoneAngola = phoneAngola,
                planName = planName,
                priceKwanzas = priceKwanzas,
                durationDays = durationDays,
                allowedBookmakers = allowedBookmakers
            )
            result.onSuccess { created ->
                _adminStatusBanner.value =
                    "✅ Conta '${created.username}' criada com sucesso para ${created.fullName}!"
            }.onFailure { err ->
                _adminStatusBanner.value = "⚠️ Erro: ${err.message}"
            }
        }
    }

    fun toggleAccountStatus(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.toggleUserActive(user)
            val newState = if (!user.isActive) "ATIVADA" else "SUSPENSA"
            _adminStatusBanner.value = "Conta '${user.username}' agora está $newState."
        }
    }

    fun renewAccount(user: UserAccountEntity, days: Int) {
        viewModelScope.launch {
            repository.renewUserDays(user, days)
            _adminStatusBanner.value = "⏳ Licença de '${user.username}' renovada por +$days dias!"
        }
    }

    fun deleteAccount(user: UserAccountEntity) {
        viewModelScope.launch {
            repository.deleteUser(user.id)
            _adminStatusBanner.value = "Conta '${user.username}' removida."
        }
    }

    fun clearSubscriberActivityLogs() {
        viewModelScope.launch {
            repository.clearSubscriberActivities()
            _adminStatusBanner.value = "Histórico de atividades dos assinantes limpo."
        }
    }
}
