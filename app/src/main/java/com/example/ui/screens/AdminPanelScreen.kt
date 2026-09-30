package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SubscriberActivityEntity
import com.example.data.UserAccountEntity
import com.example.ui.theme.AviatorCrimson
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitNavy
import com.example.ui.theme.CockpitObsidian
import com.example.ui.theme.KwanzaGold
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalDanger
import com.example.ui.theme.SignalDangerDark
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.SignalEmeraldDark
import com.example.ui.theme.TelemetrySilver
import com.example.ui.theme.TelemetryWhite
import com.example.ui.theme.VelaBlue
import com.example.ui.theme.VelaPink
import com.example.ui.theme.VelaPurple
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class AdminDashboardTab(val title: String) {
    SUBSCRIBERS("Assinantes"),
    ACTIVITY_FEED("Atividade"),
    CREATE_USER("Criar Conta")
}

private data class LicensePlanOption(
    val title: String,
    val days: Int,
    val priceKz: Int
)

private val PLAN_OPTIONS = listOf(
    LicensePlanOption("Diário (24 Horas)", 1, 2000),
    LicensePlanOption("Semanal Pro (7 Dias)", 7, 5000),
    LicensePlanOption("VIP Mensal (30 Dias)", 30, 15000),
    LicensePlanOption("Vitalício (365 Dias)", 365, 35000)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminPanelScreen(
    operatorId: String = "ADMIN-MASTER-AO",
    users: List<UserAccountEntity>,
    activities: List<SubscriberActivityEntity>,
    statusBanner: String?,
    onDismissBanner: () -> Unit,
    onCreateAccount: (
        username: String,
        password: String,
        fullName: String,
        phoneAngola: String,
        planName: String,
        priceKwanzas: Int,
        durationDays: Int,
        allowedBookmakers: String
    ) -> Unit,
    onToggleAccountStatus: (UserAccountEntity) -> Unit,
    onRenewAccount: (UserAccountEntity, Int) -> Unit,
    onDeleteAccount: (UserAccountEntity) -> Unit,
    onClearActivities: () -> Unit,
    onTestLoginAsUser: (UserAccountEntity) -> Unit,
    onLogoutAdmin: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    BackHandler {
        onNavigateBackToLogin()
    }

    val clipboardManager = LocalClipboardManager.current
    var copiedFeedback by remember { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableStateOf(AdminDashboardTab.SUBSCRIBERS) }

    // Search & filter state for Subscribers tab
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var subscriberFilter by rememberSaveable { mutableStateOf("ALL") } // ALL, ACTIVE, SUSPENDED

    // Filter state for Activity tab
    var activityFilter by rememberSaveable { mutableStateOf("ALL") } // ALL, LOGIN, VELA_SIGNAL, ADMIN

    // Create User Form state
    var fullName by rememberSaveable { mutableStateOf("") }
    var phoneAngola by rememberSaveable { mutableStateOf("+244 9") }
    var newUsername by rememberSaveable { mutableStateOf("AO-${Random.nextInt(1000, 9999)}") }
    var newPassword by rememberSaveable { mutableStateOf("${Random.nextInt(1000, 9999)}") }
    var selectedPlanIndex by rememberSaveable { mutableIntStateOf(2) } // VIP Mensal default

    var allowBantuBet by rememberSaveable { mutableStateOf(true) }
    var allowElephantBet by rememberSaveable { mutableStateOf(true) }
    var allowKwanzaBet by rememberSaveable { mutableStateOf(true) }

    val now = System.currentTimeMillis()
    val activeCount = users.count { it.isActive && it.expiresAtMillis > now }
    val totalRevenueKz = users.sumOf { it.priceKwanzas }
    val totalSessionsCount = users.sumOf { it.loginCount }

    val filteredUsers = remember(users, searchQuery, subscriberFilter, now) {
        users.filter { acc ->
            val matchesQuery = searchQuery.isBlank() ||
                acc.username.contains(searchQuery, ignoreCase = true) ||
                acc.fullName.contains(searchQuery, ignoreCase = true) ||
                acc.phoneAngola.contains(searchQuery, ignoreCase = true)

            val isAccActive = acc.isActive && acc.expiresAtMillis > now
            val matchesFilter = when (subscriberFilter) {
                "ACTIVE" -> isAccActive
                "SUSPENDED" -> !isAccActive
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    val filteredActivities = remember(activities, activityFilter) {
        activities.filter { act ->
            when (activityFilter) {
                "LOGIN" -> act.actionType == "LOGIN" || act.actionType == "BOOKMAKER_SWITCH"
                "VELA_SIGNAL" -> act.actionType == "VELA_SIGNAL"
                "ADMIN" -> act.actionType == "ACCOUNT_CREATED" ||
                    act.actionType == "LICENSE_RENEWED" ||
                    act.actionType == "STATUS_CHANGED"
                else -> true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Admin Dashboard Header Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, KwanzaGold.copy(alpha = 0.65f), RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CockpitNavy)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBackToLogin,
                            modifier = Modifier.testTag("admin_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar para Login",
                                tint = TelemetryWhite
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = KwanzaGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(id = R.string.admin_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KwanzaGold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "Operador: $operatorId · Gestão de Contas e Atividade",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TelemetrySilver,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = onLogoutAdmin,
                            modifier = Modifier
                                .size(40.dp)
                                .background(CockpitCardElevated, RoundedCornerShape(10.dp))
                                .testTag("admin_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Bloquear e Sair do Painel Admin",
                                tint = SignalDanger,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Executive KPI Telemetry Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminKpiCard(
                        label = "ASSINANTES ATIVOS",
                        value = "$activeCount/${users.size}",
                        accent = SignalEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        label = "SESSÕES / LOGINS",
                        value = "$totalSessionsCount",
                        accent = VelaBlue,
                        modifier = Modifier.weight(1f)
                    )
                    AdminKpiCard(
                        label = "RECEITA (AOA)",
                        value = "${formatKz(totalRevenueKz)} Kz",
                        accent = KwanzaGold,
                        modifier = Modifier.weight(1.25f)
                    )
                }
            }

            // 3. Status / Clipboard Notification Banner
            item {
                val activeMessage = copiedFeedback ?: statusBanner
                AnimatedVisibility(visible = activeMessage != null) {
                    Surface(
                        color = SignalEmeraldDark,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SignalEmerald, RoundedCornerShape(14.dp))
                            .clickable {
                                copiedFeedback = null
                                onDismissBanner()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = SignalEmerald
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = activeMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TelemetryWhite,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Dashboard Navigation Tabs (Assinantes | Atividade | Criar Conta)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminTabButton(
                        title = "Assinantes (${users.size})",
                        icon = Icons.Filled.Group,
                        selected = selectedTab == AdminDashboardTab.SUBSCRIBERS,
                        tag = "admin_tab_subscribers",
                        onClick = { selectedTab = AdminDashboardTab.SUBSCRIBERS },
                        modifier = Modifier.weight(1f)
                    )
                    AdminTabButton(
                        title = "Atividade (${activities.size})",
                        icon = Icons.Filled.History,
                        selected = selectedTab == AdminDashboardTab.ACTIVITY_FEED,
                        tag = "admin_tab_activity",
                        onClick = { selectedTab = AdminDashboardTab.ACTIVITY_FEED },
                        modifier = Modifier.weight(1f)
                    )
                    AdminTabButton(
                        title = "+ Nova Conta",
                        icon = Icons.Filled.PersonAdd,
                        selected = selectedTab == AdminDashboardTab.CREATE_USER,
                        tag = "admin_tab_create_user",
                        onClick = { selectedTab = AdminDashboardTab.CREATE_USER },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- TAB 1: SUBSCRIBER ACCOUNTS MANAGEMENT ---
            if (selectedTab == AdminDashboardTab.SUBSCRIBERS) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CockpitBorder, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Pesquisar assinante, código AO ou telefone") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Search,
                                        contentDescription = "Pesquisar",
                                        tint = TelemetrySilver
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_search_subscribers_input"),
                                colors = adminFieldColors()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(
                                    "ALL" to "Todos (${users.size})",
                                    "ACTIVE" to "Ativos ($activeCount)",
                                    "SUSPENDED" to "Suspensos (${users.size - activeCount})"
                                ).forEach { (key, label) ->
                                    FilterChip(
                                        selected = subscriberFilter == key,
                                        onClick = { subscriberFilter = key },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = KwanzaGold.copy(alpha = 0.22f),
                                            selectedLabelColor = KwanzaGold,
                                            containerColor = CockpitCardElevated,
                                            labelColor = TelemetrySilver
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredUsers, key = { it.id }) { account ->
                    SubscriberAccountCard(
                        account = account,
                        now = now,
                        onTestLoginAsUser = onTestLoginAsUser,
                        onCopyVoucher = { acc ->
                            val voucher = buildString {
                                appendLine("✈️ *AVIATOR AO BOT — ACESSO LIBERADO*")
                                appendLine("👤 Cliente: ${acc.fullName}")
                                appendLine("🔑 Usuário: ${acc.username}")
                                appendLine("🔒 Senha: ${acc.password}")
                                appendLine("⏳ Plano: ${acc.planName}")
                                appendLine("🎰 Casas: ${acc.allowedBookmakers}")
                            }
                            clipboardManager.setText(AnnotatedString(voucher))
                            copiedFeedback =
                                "📋 Comprovativo de '${acc.username}' copiado p/ enviar no WhatsApp!"
                        },
                        onRenewAccount = { onRenewAccount(account, 7) },
                        onToggleAccountStatus = { onToggleAccountStatus(account) },
                        onDeleteAccount = { onDeleteAccount(account) }
                    )
                }
            }

            // --- TAB 2: SUBSCRIBER ACTIVITY TELEMETRY & LIVE LOGS ---
            if (selectedTab == AdminDashboardTab.ACTIVITY_FEED) {
                item {
                    SubscriberActivityAnalyticsHeader(
                        activities = activities,
                        activeFilter = activityFilter,
                        onSelectFilter = { activityFilter = it },
                        onClearActivities = onClearActivities
                    )
                }

                if (filteredActivities.isEmpty()) {
                    item {
                        Surface(
                            color = CockpitCard,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CockpitBorder, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.History,
                                    contentDescription = null,
                                    tint = TelemetrySilver,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Nenhuma atividade registada para este filtro.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TelemetrySilver
                                )
                            }
                        }
                    }
                } else {
                    items(filteredActivities, key = { it.id }) { activity ->
                        SubscriberActivityRowCard(activity = activity)
                    }
                }
            }

            // --- TAB 3: CREATE NEW SUBSCRIBER ACCOUNT ---
            if (selectedTab == AdminDashboardTab.CREATE_USER) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CockpitBorder, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CockpitCard)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.PersonAdd,
                                        contentDescription = null,
                                        tint = SignalEmerald
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(id = R.string.admin_create_user),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TelemetryWhite
                                    )
                                }

                                TextButtonOrChip(
                                    text = "Gerar Código AO",
                                    onClick = {
                                        newUsername = "AO-${Random.nextInt(1000, 9999)}"
                                        newPassword = "${Random.nextInt(1000, 9999)}"
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = fullName,
                                    onValueChange = { fullName = it },
                                    label = { Text("Nome do Cliente") },
                                    placeholder = { Text("Ex: João - Luanda") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_fullname_input"),
                                    colors = adminFieldColors()
                                )
                                OutlinedTextField(
                                    value = phoneAngola,
                                    onValueChange = { phoneAngola = it },
                                    label = { Text("WhatsApp (+244)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_phone_input"),
                                    colors = adminFieldColors()
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = newUsername,
                                    onValueChange = { newUsername = it },
                                    label = { Text("Usuário / Login BOT") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_username_input"),
                                    colors = adminFieldColors()
                                )
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Senha do Usuário") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_password_input"),
                                    colors = adminFieldColors()
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "PLANO DE VALIDADE & VALOR (KWANZAS):",
                                style = MaterialTheme.typography.labelSmall,
                                color = TelemetrySilver
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PLAN_OPTIONS.forEachIndexed { idx, option ->
                                    FilterChip(
                                        selected = selectedPlanIndex == idx,
                                        onClick = { selectedPlanIndex = idx },
                                        label = {
                                            Text(
                                                text = "${option.title} · ${formatKz(option.priceKz)} Kz",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = KwanzaGold.copy(alpha = 0.22f),
                                            selectedLabelColor = KwanzaGold,
                                            containerColor = CockpitCardElevated,
                                            labelColor = TelemetrySilver
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "CASAS DE APOSTAS LIBERADAS NESTA CONTA:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TelemetrySilver
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = allowBantuBet,
                                    onClick = { allowBantuBet = !allowBantuBet },
                                    label = { Text("BantuBet AO") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalEmeraldDark,
                                        selectedLabelColor = SignalEmerald
                                    )
                                )
                                FilterChip(
                                    selected = allowElephantBet,
                                    onClick = { allowElephantBet = !allowElephantBet },
                                    label = { Text("ElephantBet") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalEmeraldDark,
                                        selectedLabelColor = SignalEmerald
                                    )
                                )
                                FilterChip(
                                    selected = allowKwanzaBet,
                                    onClick = { allowKwanzaBet = !allowKwanzaBet },
                                    label = { Text("KwanzaBet") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SignalEmeraldDark,
                                        selectedLabelColor = SignalEmerald
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val plan = PLAN_OPTIONS[selectedPlanIndex]
                                    val houses = buildList {
                                        if (allowBantuBet) add("BANTUBET")
                                        if (allowElephantBet) add("ELEPHANTBET")
                                        if (allowKwanzaBet) add("KWANZABET")
                                        if (isEmpty()) add("BANTUBET")
                                    }.joinToString(",")

                                    onCreateAccount(
                                        newUsername,
                                        newPassword,
                                        fullName,
                                        phoneAngola,
                                        plan.title,
                                        plan.priceKz,
                                        plan.days,
                                        houses
                                    )
                                    fullName = ""
                                    newUsername = "AO-${Random.nextInt(1000, 9999)}"
                                    newPassword = "${Random.nextInt(1000, 9999)}"
                                    selectedTab = AdminDashboardTab.SUBSCRIBERS
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("admin_create_account_submit"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SignalEmerald,
                                    contentColor = CockpitObsidian
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "GERAR & ATIVAR CONTA DE USUÁRIO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) KwanzaGold.copy(alpha = 0.20f) else CockpitCard,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(44.dp)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) KwanzaGold else CockpitBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) KwanzaGold else TelemetrySilver,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) KwanzaGold else TelemetrySilver,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SubscriberAccountCard(
    account: UserAccountEntity,
    now: Long,
    onTestLoginAsUser: (UserAccountEntity) -> Unit,
    onCopyVoucher: (UserAccountEntity) -> Unit,
    onRenewAccount: () -> Unit,
    onToggleAccountStatus: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val remainingDays =
        ((account.expiresAtMillis - now) / (1000L * 60L * 60L * 24L)).coerceAtLeast(0L)
    val isExpired = account.expiresAtMillis <= now
    val statusColor = when {
        !account.isActive -> SignalDanger
        isExpired -> SignalDanger
        else -> SignalEmerald
    }
    val statusLabel = when {
        !account.isActive -> "SUSPENSA"
        isExpired -> "EXPIRADA"
        else -> "ATIVA · ${remainingDays}d restantes"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TelemetryWhite
                    )
                    Text(
                        text = "${account.phoneAngola} · ${account.planName} (${formatKz(account.priceKwanzas)} Kz)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TelemetrySilver
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.border(1.dp, statusColor, RoundedCornerShape(50))
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subscriber Telemetry Bar (Last Login, Total Logins, Active Bookmaker)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CockpitCard,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Login,
                            contentDescription = null,
                            tint = VelaBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Acessos: ${account.loginCount}x · ${formatRelativeTime(account.lastLoginAtMillis)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetrySilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = CockpitCard,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = KwanzaGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = account.lastActiveBookmaker,
                            style = MaterialTheme.typography.labelSmall,
                            color = KwanzaGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Credentials Box
            Surface(
                color = CockpitObsidian,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CockpitBorder, RoundedCornerShape(10.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "USUÁRIO: ${account.username}",
                            style = MaterialTheme.typography.labelLarge,
                            color = KwanzaGold
                        )
                        Text(
                            text = "SENHA: ${account.password}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TelemetryWhite
                        )
                    }
                    Text(
                        text = account.allowedBookmakers.replace(",", " · "),
                        style = MaterialTheme.typography.labelSmall,
                        color = VelaBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onTestLoginAsUser(account) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AviatorCrimson,
                        contentColor = TelemetryWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("admin_login_as_${account.username}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Entrar c/ Conta",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = { onCopyVoucher(account) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("admin_copy_voucher_${account.username}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = null,
                        tint = KwanzaGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copiar Acesso",
                        style = MaterialTheme.typography.labelMedium,
                        color = KwanzaGold
                    )
                }

                IconButton(
                    onClick = onRenewAccount,
                    modifier = Modifier
                        .size(48.dp)
                        .background(CockpitCard, RoundedCornerShape(10.dp))
                        .testTag("admin_renew_${account.username}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Update,
                        contentDescription = "Renovar +7 dias",
                        tint = SignalEmerald
                    )
                }

                IconButton(
                    onClick = onToggleAccountStatus,
                    modifier = Modifier
                        .size(48.dp)
                        .background(CockpitCard, RoundedCornerShape(10.dp))
                        .testTag("admin_toggle_${account.username}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Block,
                        contentDescription = "Suspender ou Ativar",
                        tint = if (account.isActive) SignalDanger else SignalEmerald
                    )
                }

                IconButton(
                    onClick = onDeleteAccount,
                    modifier = Modifier
                        .size(48.dp)
                        .background(SignalDangerDark, RoundedCornerShape(10.dp))
                        .testTag("admin_delete_${account.username}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Excluir conta",
                        tint = SignalDanger
                    )
                }
            }
        }
    }
}

@Composable
private fun SubscriberActivityAnalyticsHeader(
    activities: List<SubscriberActivityEntity>,
    activeFilter: String,
    onSelectFilter: (String) -> Unit,
    onClearActivities: () -> Unit
) {
    val total = activities.size.coerceAtLeast(1)
    val bantuCount = activities.count { it.bookmakerId.equals("BANTUBET", ignoreCase = true) }
    val elephantCount = activities.count { it.bookmakerId.equals("ELEPHANTBET", ignoreCase = true) }
    val kwanzaCount = activities.count { it.bookmakerId.equals("KWANZABET", ignoreCase = true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CockpitBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Radar,
                        contentDescription = null,
                        tint = SignalEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TELEMETRIA DE USO POR CASA DE APOSTA",
                        style = MaterialTheme.typography.labelLarge,
                        color = TelemetryWhite
                    )
                }
                IconButton(
                    onClick = onClearActivities,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("admin_clear_activities_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = "Limpar registo de atividades",
                        tint = TelemetrySilver,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bookmaker Distribution Bars
            BookmakerUsageRow(
                name = "BantuBet AO (m.bantubet.co.ao)",
                count = bantuCount,
                fraction = bantuCount.toFloat() / total,
                color = KwanzaGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            BookmakerUsageRow(
                name = "ElephantBet AO (elephantbet.co.ao)",
                count = elephantCount,
                fraction = elephantCount.toFloat() / total,
                color = VelaBlue
            )
            Spacer(modifier = Modifier.height(6.dp))
            BookmakerUsageRow(
                name = "KwanzaBet AO (m.kwanzabet.ao)",
                count = kwanzaCount,
                fraction = kwanzaCount.toFloat() / total,
                color = SignalEmerald
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips for Activity Stream
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to "Todos (${activities.size})",
                    "LOGIN" to "Acessos & Navegação",
                    "VELA_SIGNAL" to "Velas & Sinais BOT",
                    "ADMIN" to "Gestão de Licenças"
                ).forEach { (key, label) ->
                    FilterChip(
                        selected = activeFilter == key,
                        onClick = { onSelectFilter(key) },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VelaBlue.copy(alpha = 0.22f),
                            selectedLabelColor = VelaBlue,
                            containerColor = CockpitCardElevated,
                            labelColor = TelemetrySilver
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun BookmakerUsageRow(
    name: String,
    count: Int,
    fraction: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = TelemetrySilver
            )
            Text(
                text = "$count eventos (${(fraction * 100).toInt()}%)",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0.04f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = CockpitCardElevated
        )
    }
}

@Composable
private fun SubscriberActivityRowCard(activity: SubscriberActivityEntity) {
    val accentColor = when (activity.actionType) {
        "LOGIN" -> SignalEmerald
        "VELA_SIGNAL" -> VelaPurple
        "BOOKMAKER_SWITCH" -> VelaBlue
        "ACCOUNT_CREATED" -> KwanzaGold
        "LICENSE_RENEWED" -> SignalEmerald
        else -> SignalAmber
    }
    val badgeText = when (activity.actionType) {
        "LOGIN" -> "LOGIN BOT"
        "VELA_SIGNAL" -> "SINAL VELA"
        "BOOKMAKER_SWITCH" -> "NAVEGADOR"
        "ACCOUNT_CREATED" -> "NOVA CONTA"
        "LICENSE_RENEWED" -> "RENOVAÇÃO"
        else -> "STATUS"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CockpitCardElevated)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${activity.username} · ${activity.fullName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = TelemetryWhite
                    )
                }

                Surface(
                    color = accentColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = activity.details,
                style = MaterialTheme.typography.bodyMedium,
                color = TelemetrySilver
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Casa: ${activity.bookmakerId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = KwanzaGold
                )
                Text(
                    text = formatTimestampPtAo(activity.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TelemetrySilver
                )
            }
        }
    }
}

@Composable
private fun AdminKpiCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CockpitCard,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TelemetrySilver,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TextButtonOrChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = CockpitCardElevated,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .border(1.dp, KwanzaGold, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("admin_generate_random_code")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.AutoFixHigh,
                contentDescription = null,
                tint = KwanzaGold,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = KwanzaGold
            )
        }
    }
}

@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = KwanzaGold,
    unfocusedBorderColor = CockpitBorder,
    focusedLabelColor = KwanzaGold,
    unfocusedLabelColor = TelemetrySilver,
    focusedTextColor = TelemetryWhite,
    unfocusedTextColor = TelemetryWhite
)

fun formatKz(amount: Int): String {
    return NumberFormat.getIntegerInstance(Locale("pt", "AO")).format(amount)
}

private fun formatRelativeTime(timestampMillis: Long?): String {
    if (timestampMillis == null || timestampMillis <= 0L) return "Sem login recente"
    val diffMinutes = ((System.currentTimeMillis() - timestampMillis) / 60_000L).coerceAtLeast(0L)
    return when {
        diffMinutes < 1L -> "Agora mesmo"
        diffMinutes < 60L -> "Há ${diffMinutes}m"
        diffMinutes < 1440L -> "Há ${diffMinutes / 60L}h"
        else -> "Há ${diffMinutes / 1440L}d"
    }
}

private fun formatTimestampPtAo(timestampMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale("pt", "AO"))
    return sdf.format(Date(timestampMillis))
}
