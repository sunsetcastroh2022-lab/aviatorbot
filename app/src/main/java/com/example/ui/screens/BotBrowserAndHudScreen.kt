package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.domain.AviatorSignalEngine
import com.example.domain.BookmakerPlatform
import com.example.domain.ChartAnalysisResult
import com.example.domain.ChartConditionStatus
import com.example.domain.KwanzaBetSplit
import com.example.domain.VelaReachBand
import com.example.ui.AviatorUiState
import com.example.ui.HudOverlayMode
import com.example.ui.theme.AviatorCrimson
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitNavy
import com.example.ui.theme.CockpitObsidian
import com.example.ui.theme.KwanzaGold
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.SignalAmberDark
import com.example.ui.theme.SignalDanger
import com.example.ui.theme.SignalDangerDark
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.SignalEmeraldDark
import com.example.ui.theme.TelemetrySilver
import com.example.ui.theme.TelemetryWhite
import com.example.ui.theme.VelaBlue
import com.example.ui.theme.VelaBlueSurface
import com.example.ui.theme.VelaPink
import com.example.ui.theme.VelaPinkSurface
import com.example.ui.theme.VelaPurple
import com.example.ui.theme.VelaPurpleSurface
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun BotBrowserAndHudScreen(
    uiState: AviatorUiState,
    reloadTrigger: Int,
    onSelectBookmaker: (BookmakerPlatform) -> Unit,
    onUrlChanged: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onReloadClicked: () -> Unit,
    onToggleSimulatedUnderlay: () -> Unit,
    onSetHudMode: (HudOverlayMode) -> Unit,
    onToggleLiveFlight: () -> Unit,
    onToggleAutoCycle: () -> Unit,
    onRecordVela: (Double) -> Unit,
    onUndoLastVela: () -> Unit,
    onResetVelas: () -> Unit,
    onSetTotalStakeKz: (Int) -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hudOffsetY by remember { mutableFloatStateOf(0f) }
    var showCustomVelaDialog by remember { mutableStateOf(false) }
    var customVelaText by remember { mutableStateOf("2.45") }

    BackHandler {
        val wv = webViewRef
        if (wv != null && wv.canGoBack() && !uiState.showSimulatedFlightCanvasUnderlay) {
            wv.goBack()
        } else {
            onLogout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitObsidian)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Tactical Bookmaker & Browser Bar
            TopBookmakerBrowserBar(
                uiState = uiState,
                onSelectBookmaker = { platform ->
                    onSelectBookmaker(platform)
                    webViewRef?.loadUrl(platform.baseUrl)
                },
                onReloadClicked = {
                    onReloadClicked()
                    webViewRef?.reload()
                },
                onToggleSimulatedUnderlay = onToggleSimulatedUnderlay,
                onOpenAdminPanel = onOpenAdminPanel,
                onLogout = onLogout
            )

            // 2. Main Viewport (Bookmaker WebView OR Live Aviator Radar Underlay + Floating HUD Overlay "Por Cima")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.showSimulatedFlightCanvasUnderlay) {
                    FullScreenAviatorRadarUnderlay(
                        uiState = uiState,
                        onToggleLiveFlight = onToggleLiveFlight,
                        onToggleAutoCycle = onToggleAutoCycle,
                        onSwitchBackToBrowser = onToggleSimulatedUnderlay
                    )
                } else {
                    BookmakerWebViewContainer(
                        targetUrl = uiState.activeBookmaker.baseUrl,
                        reloadTrigger = reloadTrigger,
                        onWebViewCreated = { webViewRef = it },
                        onUrlChanged = onUrlChanged,
                        onLoadingChanged = onLoadingChanged
                    )
                }

                // 3. FLOATING BOT ANALYZER HUD OVERLAY ("POR CIMA DO GRÁFICO")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(0, hudOffsetY.roundToInt()) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                hudOffsetY = (hudOffsetY + dragAmount).coerceIn(0f, 680f)
                            }
                        }
                ) {
                    FloatingAviatorBotHud(
                        uiState = uiState,
                        onSetHudMode = onSetHudMode,
                        onToggleLiveFlight = onToggleLiveFlight,
                        onToggleAutoCycle = onToggleAutoCycle,
                        onRecordVela = onRecordVela,
                        onOpenCustomVelaDialog = { showCustomVelaDialog = true },
                        onUndoLastVela = onUndoLastVela,
                        onResetVelas = onResetVelas,
                        onSetTotalStakeKz = onSetTotalStakeKz
                    )
                }
            }
        }
    }

    if (showCustomVelaDialog) {
        AlertDialog(
            onDismissRequest = { showCustomVelaDialog = false },
            containerColor = CockpitCard,
            titleContentColor = TelemetryWhite,
            textContentColor = TelemetrySilver,
            title = {
                Text(
                    text = "Inserir Vela Manual (${uiState.activeBookmaker.displayName})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Digite o multiplicador em que o avião voou na rodada atual (ex: 1.35, 2.80, 15.40):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customVelaText,
                        onValueChange = { customVelaText = it },
                        label = { Text("Multiplicador da Vela (x)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_vela_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AviatorCrimson,
                            unfocusedBorderColor = CockpitBorder,
                            focusedTextColor = TelemetryWhite,
                            unfocusedTextColor = TelemetryWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = customVelaText.replace(",", ".").replace("x", "").trim().toDoubleOrNull()
                        if (parsed != null && parsed >= 1.00) {
                            onRecordVela(parsed)
                        }
                        showCustomVelaDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalEmerald, contentColor = CockpitObsidian),
                    modifier = Modifier.testTag("confirm_custom_vela_button")
                ) {
                    Text("Registar & Recalcular", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomVelaDialog = false }) {
                    Text("Cancelar", color = TelemetrySilver)
                }
            }
        )
    }
}

@Composable
private fun TopBookmakerBrowserBar(
    uiState: AviatorUiState,
    onSelectBookmaker: (BookmakerPlatform) -> Unit,
    onReloadClicked: () -> Unit,
    onToggleSimulatedUnderlay: () -> Unit,
    onOpenAdminPanel: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        color = CockpitNavy,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = CockpitBorder)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Bookmaker Selector Tabs + User/Admin Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BookmakerPlatform.entries.forEach { platform ->
                    val isSelected = uiState.activeBookmaker == platform
                    val accent = Color(platform.accentHex)
                    Surface(
                        color = if (isSelected) accent.copy(alpha = 0.22f) else CockpitCard,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) accent else CockpitBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectBookmaker(platform) }
                            .testTag("tab_bookmaker_${platform.id}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = platform.displayName.substringBefore(" "),
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) accent else TelemetrySilver,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onOpenAdminPanel,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CockpitCardElevated, RoundedCornerShape(10.dp))
                        .testTag("browser_open_admin_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = "Painel Admin Criador de Contas",
                        tint = KwanzaGold,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CockpitCardElevated, RoundedCornerShape(10.dp))
                        .testTag("browser_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sair da Conta",
                        tint = SignalDanger,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Row 2: Active URL bar + Reload + Toggle Live Flight Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = CockpitObsidian,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, CockpitBorder, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Public,
                            contentDescription = null,
                            tint = SignalEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.currentBrowserUrl,
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetryWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.loggedInUser != null) {
                            Text(
                                text = "👤 ${uiState.loggedInUser.username}",
                                style = MaterialTheme.typography.labelSmall,
                                color = KwanzaGold
                            )
                        }
                    }
                }

                Surface(
                    color = if (uiState.showSimulatedFlightCanvasUnderlay) AviatorCrimson else CockpitCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, AviatorCrimson, RoundedCornerShape(8.dp))
                        .clickable { onToggleSimulatedUnderlay() }
                        .testTag("toggle_radar_underlay_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Radar,
                            contentDescription = null,
                            tint = TelemetryWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.showSimulatedFlightCanvasUnderlay) "Ver Site Web" else "Tela Radar",
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetryWhite
                        )
                    }
                }

                Surface(
                    color = CockpitCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .border(1.dp, CockpitBorder, RoundedCornerShape(8.dp))
                        .clickable { onReloadClicked() }
                        .testTag("reload_webview_button")
                ) {
                    Box(
                        modifier = Modifier.padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Recarregar site da casa de apostas",
                            tint = TelemetryWhite,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (uiState.isPageLoading && !uiState.showSimulatedFlightCanvasUnderlay) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp),
                    color = AviatorCrimson,
                    trackColor = CockpitObsidian
                )
            }
        }
    }
}

@Composable
private fun FloatingAviatorBotHud(
    uiState: AviatorUiState,
    onSetHudMode: (HudOverlayMode) -> Unit,
    onToggleLiveFlight: () -> Unit,
    onToggleAutoCycle: () -> Unit,
    onRecordVela: (Double) -> Unit,
    onOpenCustomVelaDialog: () -> Unit,
    onUndoLastVela: () -> Unit,
    onResetVelas: () -> Unit,
    onSetTotalStakeKz: (Int) -> Unit
) {
    var hudAlpha by remember { mutableFloatStateOf(0.78f) }
    val analysis = uiState.analysis
    val statusColor = when (analysis.status) {
        ChartConditionStatus.FAVORABLE -> SignalEmerald
        ChartConditionStatus.MODERATE -> SignalAmber
        ChartConditionStatus.UNFAVORABLE -> SignalDanger
    }
    val statusBgColor = when (analysis.status) {
        ChartConditionStatus.FAVORABLE -> SignalEmeraldDark.copy(alpha = 0.72f)
        ChartConditionStatus.MODERATE -> SignalAmberDark.copy(alpha = 0.72f)
        ChartConditionStatus.UNFAVORABLE -> SignalDangerDark.copy(alpha = 0.78f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, statusColor.copy(alpha = 0.85f), RoundedCornerShape(18.dp))
            .testTag("floating_bot_hud_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = CockpitObsidian.copy(alpha = hudAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // HUD Top Control Bar: Status Signal + Transparency Toggle + Mode Switcher + Collapse/Expand
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = "Arrastar HUD por cima do jogo",
                        tint = TelemetrySilver,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = statusBgColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.border(1.dp, statusColor, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${analysis.status.badgeTitle} (${analysis.scorePercent}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Mode Switcher Buttons (Transparência / Radar / Kwanzas / Compacto)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudModeIconButton(
                        active = hudAlpha < 0.70f,
                        icon = Icons.Filled.Opacity,
                        contentDescription = "Alternar Transparência do HUD sobre o Gráfico",
                        tag = "hud_opacity_toggle",
                        onClick = {
                            hudAlpha = when {
                                hudAlpha > 0.85f -> 0.76f
                                hudAlpha > 0.65f -> 0.52f
                                else -> 0.90f
                            }
                        }
                    )
                    HudModeIconButton(
                        active = uiState.hudMode == HudOverlayMode.EXPANDED_RADAR,
                        icon = Icons.Filled.Radar,
                        contentDescription = "Modo Radar e Alcances",
                        tag = "hud_mode_radar",
                        onClick = { onSetHudMode(HudOverlayMode.EXPANDED_RADAR) }
                    )
                    HudModeIconButton(
                        active = uiState.hudMode == HudOverlayMode.CALCULATOR_KZ,
                        icon = Icons.Filled.Calculate,
                        contentDescription = "Calculadora de Proteção em Kwanzas",
                        tag = "hud_mode_calculator",
                        onClick = { onSetHudMode(HudOverlayMode.CALCULATOR_KZ) }
                    )
                    HudModeIconButton(
                        active = uiState.hudMode == HudOverlayMode.COMPACT_PILL,
                        icon = if (uiState.hudMode == HudOverlayMode.COMPACT_PILL) Icons.Filled.OpenInFull else Icons.Filled.CloseFullscreen,
                        contentDescription = "Minimizar ou Expandir HUD",
                        tag = "hud_mode_compact",
                        onClick = {
                            if (uiState.hudMode == HudOverlayMode.COMPACT_PILL) {
                                onSetHudMode(HudOverlayMode.EXPANDED_RADAR)
                            } else {
                                onSetHudMode(HudOverlayMode.COMPACT_PILL)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Safety Warning & Risk Advisory Banner over Active WebView
            SafetyWarningHudBanner(
                analysis = analysis,
                isFlightRunning = uiState.isFlightRunning,
                currentFlightMultiplier = uiState.currentFlightMultiplier
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ALWAYS VISIBLE: Predicted Exit Points & Live Flight Strip (PROTEÇÃO | SAÍDA ALVO | VELA EM VOO)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Box 1: PROTEÇÃO (Slot 1)
                TelemetryMetricBox(
                    label = "PROTEÇÃO (AUTO 1)",
                    value = AviatorSignalEngine.formatMult(analysis.protectionMultiplier),
                    subtext = "${formatKz(uiState.kwanzaSplit.protectionStakeKz)} Kz (Cobre 100%)",
                    accentColor = SignalEmerald,
                    surfaceColor = SignalEmeraldDark.copy(alpha = 0.48f),
                    modifier = Modifier.weight(1f)
                )

                // Box 2: SAÍDA ALVO (Slot 2)
                TelemetryMetricBox(
                    label = "SAÍDA ALVO (AUTO 2)",
                    value = AviatorSignalEngine.formatMult(analysis.targetExitMultiplier),
                    subtext = "${formatKz(uiState.kwanzaSplit.targetStakeKz)} Kz (Alvo Lucro)",
                    accentColor = VelaPurple,
                    surfaceColor = VelaPurpleSurface.copy(alpha = 0.55f),
                    modifier = Modifier.weight(1f)
                )

                // Box 3: ALCANCE MÁXIMO / VELA EM VOO AO DECORRER DA VELA
                val isFlying = uiState.isFlightRunning
                val flightColor = when {
                    !isFlying -> VelaPink
                    uiState.currentFlightMultiplier >= 10.0 -> VelaPink
                    uiState.currentFlightMultiplier >= 2.0 -> VelaPurple
                    else -> VelaBlue
                }
                TelemetryMetricBox(
                    label = if (isFlying) "VELA EM VOO ✈️" else "TETO ESTIMADO",
                    value = if (isFlying) {
                        AviatorSignalEngine.formatMult(uiState.currentFlightMultiplier)
                    } else {
                        "Até ${AviatorSignalEngine.formatMult(analysis.maxCeilingMultiplier)}"
                    },
                    subtext = if (isFlying) "Ao decorrer da vela" else "Rosa há ${analysis.roundsSincePink} velas",
                    accentColor = flightColor,
                    surfaceColor = VelaPinkSurface.copy(alpha = 0.48f),
                    modifier = Modifier.weight(1f)
                )
            }

            // EXPANDED MODE 1: Full Chart Analysis + In-Flight Reach Bands + Recent Velas Strip
            AnimatedVisibility(visible = uiState.hudMode == HudOverlayMode.EXPANDED_RADAR) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Diagnosis & Live Curve Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini Live Flight & Trend Sparkline Canvas
                        Box(
                            modifier = Modifier
                                .weight(0.95f)
                                .height(86.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitNavy)
                                .border(1.dp, CockpitBorder, RoundedCornerShape(12.dp))
                        ) {
                            MiniAviatorTrajectoryCanvas(
                                recentVelas = uiState.recentVelas,
                                currentFlightMult = uiState.currentFlightMultiplier,
                                isFlightRunning = uiState.isFlightRunning,
                                protectionMult = analysis.protectionMultiplier,
                                targetExitMult = analysis.targetExitMultiplier
                            )
                            Text(
                                text = if (uiState.isFlightRunning) {
                                    "EM VOO: ${AviatorSignalEngine.formatMult(uiState.currentFlightMultiplier)}"
                                } else {
                                    "MÉDIA 5V: ${AviatorSignalEngine.formatMult(analysis.movingAverage5)}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = TelemetryWhite,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                            )
                        }

                        // Detailed Explanation + In-Flight Trigger Controls
                        Column(
                            modifier = Modifier.weight(1.25f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = analysis.headline,
                                style = MaterialTheme.typography.labelLarge,
                                color = TelemetryWhite,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = analysis.liveProgressDiagnosis,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.isFlightRunning) SignalEmerald else TelemetrySilver,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = onToggleLiveFlight,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.isFlightRunning) SignalDanger else AviatorCrimson,
                                        contentColor = TelemetryWhite
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .testTag("hud_toggle_live_flight_button")
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isFlightRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.isFlightRunning) "Parar Vela" else "Simular Voo",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Surface(
                                    color = if (uiState.isAutoCycleRadarEnabled) SignalEmeraldDark else CockpitCardElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .border(
                                            1.dp,
                                            if (uiState.isAutoCycleRadarEnabled) SignalEmerald else CockpitBorder,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onToggleAutoCycle() }
                                        .testTag("hud_auto_radar_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.AutoMode,
                                            contentDescription = null,
                                            tint = if (uiState.isAutoCycleRadarEnabled) SignalEmerald else TelemetrySilver,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (uiState.isAutoCycleRadarEnabled) "Auto ON" else "Auto",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (uiState.isAutoCycleRadarEnabled) SignalEmerald else TelemetrySilver
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // POSSÍVEIS ALCANCES DAS VELAS AO DECORRER DA VELA
                    Text(
                        text = "POSSÍVEIS ALCANCES AO DECORRER DA VELA:",
                        style = MaterialTheme.typography.labelSmall,
                        color = KwanzaGold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        analysis.reachBands.forEach { band ->
                            ReachBandCompactCard(
                                band = band,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Recent Velas History Strip + 1-Tap Quick Record Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ÚLTIMAS VELAS (${uiState.activeBookmaker.displayName.substringBefore(" ")}):",
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetrySilver
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Azul: ${analysis.blueCount} · Roxa: ${analysis.purpleCount} · Rosa: ${analysis.pinkCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TelemetrySilver
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        itemsIndexed(uiState.recentVelas.take(12)) { idx, mult ->
                            VelaHistoryBadge(multiplier = mult, isLatest = idx == 0)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Vela Input Bar ("+ Registar Vela que acabou de sair no gráfico")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+Vela:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TelemetrySilver
                        )
                        listOf(1.08, 1.35, 1.65, 2.15, 3.40, 6.50, 14.00).forEach { quickMult ->
                            QuickVelaTapChip(
                                multiplier = quickMult,
                                onClick = { onRecordVela(quickMult) }
                            )
                        }
                        Surface(
                            color = CockpitCardElevated,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .border(1.dp, KwanzaGold, RoundedCornerShape(6.dp))
                                .clickable { onOpenCustomVelaDialog() }
                                .testTag("hud_custom_vela_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = KwanzaGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Manual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KwanzaGold
                                )
                            }
                        }
                        IconButton(
                            onClick = onUndoLastVela,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("hud_undo_vela_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Undo,
                                contentDescription = "Desfazer última vela",
                                tint = TelemetrySilver,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        IconButton(
                            onClick = onResetVelas,
                            modifier = Modifier
                                .size(26.dp)
                                .testTag("hud_reset_velas_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Reiniciar padrão de velas",
                                tint = TelemetrySilver,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // EXPANDED MODE 2: Kwanza (AOA) Dual-Slot Bet Split Calculator (Saída & Proteção)
            AnimatedVisibility(visible = uiState.hudMode == HudOverlayMode.CALCULATOR_KZ) {
                KwanzaSplitCalculatorPanel(
                    split = uiState.kwanzaSplit,
                    onSelectStakeKz = onSetTotalStakeKz
                )
            }
        }
    }
}

@Composable
private fun SafetyWarningHudBanner(
    analysis: ChartAnalysisResult,
    isFlightRunning: Boolean,
    currentFlightMultiplier: Double
) {
    val isUnfavorable = analysis.status == ChartConditionStatus.UNFAVORABLE
    val isHighVolatility = analysis.volatilityPercent >= 65
    val bannerColor = when {
        isUnfavorable -> SignalDanger
        isHighVolatility -> SignalAmber
        else -> SignalEmerald
    }
    val bannerBg = when {
        isUnfavorable -> SignalDangerDark.copy(alpha = 0.68f)
        isHighVolatility -> SignalAmberDark.copy(alpha = 0.62f)
        else -> CockpitNavy.copy(alpha = 0.68f)
    }
    val safetyMessage = when {
        isUnfavorable ->
            "⚠️ ALERTA DE RECOLHA: Risco elevado de crash curto (≤1.15x). Não entre agora; aguarde vela de confirmação."
        isFlightRunning && currentFlightMultiplier >= analysis.protectionMultiplier ->
            "🛡️ PROTEÇÃO ATINGIDA (${AviatorSignalEngine.formatMult(analysis.protectionMultiplier)}): Aposta 1 garantida! Buscando Saída 2 em ${AviatorSignalEngine.formatMult(analysis.targetExitMultiplier)}."
        isHighVolatility ->
            "⚠️ VOLATILIDADE ALTA (${analysis.volatilityPercent}%): Trave Auto-Cashout 1 obrigatoriamente em ${AviatorSignalEngine.formatMult(analysis.protectionMultiplier)}."
        else ->
            "🛡️ SEGURANÇA ATIVA (Vol. ${analysis.volatilityPercent}%): Use 70% na Proteção (${AviatorSignalEngine.formatMult(analysis.protectionMultiplier)}) e 30% na Saída (${AviatorSignalEngine.formatMult(analysis.targetExitMultiplier)})."
    }

    Surface(
        color = bannerBg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, bannerColor.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
            .testTag("hud_safety_warning_banner")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isUnfavorable || isHighVolatility) Icons.Filled.WarningAmber else Icons.Filled.Shield,
                contentDescription = "Aviso de Segurança e Proteção",
                tint = bannerColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = safetyMessage,
                style = MaterialTheme.typography.labelSmall,
                color = TelemetryWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ReachBandCompactCard(
    band: VelaReachBand,
    modifier: Modifier = Modifier
) {
    val accent = when (band.id) {
        "BAND_PROTECTION" -> SignalEmerald
        "BAND_PURPLE" -> VelaPurple
        else -> VelaPink
    }
    val borderColor = if (band.isReachedInFlight) SignalEmerald
    else if (band.isCurrentZoneInFlight) KwanzaGold
    else accent.copy(alpha = 0.5f)

    Surface(
        color = if (band.isReachedInFlight) SignalEmeraldDark.copy(alpha = 0.6f) else CockpitNavy,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.border(1.dp, borderColor, RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = band.title.substringAfter("· ").trim(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TelemetryWhite,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (band.isReachedInFlight) "✅" else "${band.probabilityPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (band.isReachedInFlight) SignalEmerald else accent,
                    fontSize = 9.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${AviatorSignalEngine.formatMult(band.minRange)}–${AviatorSignalEngine.formatMult(band.maxRange)}",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = { (band.probabilityPercent / 100f).coerceIn(0.05f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (band.isReachedInFlight) SignalEmerald else accent,
                trackColor = CockpitCardElevated
            )
        }
    }
}

@Composable
private fun KwanzaSplitCalculatorPanel(
    split: KwanzaBetSplit,
    onSelectStakeKz: (Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(split.totalStakeKz) }
    val presetStakes = listOf(500, 1000, 2000, 5000, 10000, 25000)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "DIVISÃO AUTOMÁTICA DE BANCA EM KWANZAS (SAÍDA E PROTEÇÃO):",
            style = MaterialTheme.typography.labelSmall,
            color = KwanzaGold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetStakes.forEach { stake ->
                val isSelected = split.totalStakeKz == stake
                Surface(
                    color = if (isSelected) KwanzaGold else CockpitCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable {
                            selectedTab = stake
                            onSelectStakeKz(stake)
                        }
                        .testTag("stake_preset_$stake")
                ) {
                    Text(
                        text = "${formatKz(stake)} Kz",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CockpitObsidian else TelemetryWhite,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Slot 1 Card: Proteção
            Surface(
                color = SignalEmeraldDark.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SignalEmerald, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = null,
                            tint = SignalEmerald,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "APOSTA 1 · PROTEÇÃO",
                            style = MaterialTheme.typography.labelSmall,
                            color = SignalEmerald
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatKz(split.protectionStakeKz)} Kz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TelemetryWhite
                    )
                    Text(
                        text = "Auto Cashout: ${AviatorSignalEngine.formatMult(split.protectionAutoCashout)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SignalEmerald
                    )
                    Text(
                        text = "Retorno: ${formatKz(split.protectionReturnKz)} Kz (Cobre Total)",
                        style = MaterialTheme.typography.labelSmall,
                        color = TelemetrySilver
                    )
                }
            }

            // Slot 2 Card: Saída Alvo
            Surface(
                color = VelaPurpleSurface.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, VelaPurple, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = VelaPurple,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "APOSTA 2 · SAÍDA ALVO",
                            style = MaterialTheme.typography.labelSmall,
                            color = VelaPurple
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatKz(split.targetStakeKz)} Kz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TelemetryWhite
                    )
                    Text(
                        text = "Auto Cashout: ${AviatorSignalEngine.formatMult(split.targetAutoCashout)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = VelaPurple
                    )
                    Text(
                        text = "Lucro Líquido Total: +${formatKz(split.totalProfitIfBothHitKz)} Kz",
                        style = MaterialTheme.typography.labelSmall,
                        color = KwanzaGold
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryMetricBox(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, accentColor.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
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
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontSize = 17.sp,
                maxLines = 1
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = TelemetryWhite,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HudModeIconButton(
    active: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (active) AviatorCrimson else CockpitCardElevated,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .size(32.dp)
            .border(
                1.dp,
                if (active) AviatorCrimson else CockpitBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TelemetryWhite,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun VelaHistoryBadge(
    multiplier: Double,
    isLatest: Boolean
) {
    val bgColor = when {
        multiplier >= 10.0 -> VelaPinkSurface
        multiplier >= 2.0 -> VelaPurpleSurface
        else -> VelaBlueSurface
    }
    val textColor = when {
        multiplier >= 10.0 -> VelaPink
        multiplier >= 2.0 -> VelaPurple
        else -> VelaBlue
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = Modifier.border(
            width = if (isLatest) 1.5.dp else 1.dp,
            color = if (isLatest) TelemetryWhite else textColor.copy(alpha = 0.7f),
            shape = RoundedCornerShape(50)
        )
    ) {
        Text(
            text = AviatorSignalEngine.formatMult(multiplier),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun QuickVelaTapChip(
    multiplier: Double,
    onClick: () -> Unit
) {
    val accent = when {
        multiplier >= 10.0 -> VelaPink
        multiplier >= 2.0 -> VelaPurple
        else -> VelaBlue
    }
    Surface(
        color = CockpitCard,
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag("quick_vela_${multiplier}")
    ) {
        Text(
            text = "+${AviatorSignalEngine.formatMult(multiplier)}",
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun MiniAviatorTrajectoryCanvas(
    recentVelas: List<Double>,
    currentFlightMult: Double,
    isFlightRunning: Boolean,
    protectionMult: Double,
    targetExitMult: Double
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // Subtle radar grid lines
        val gridDash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        val protY = (h * 0.68f).coerceIn(10f, h - 10f)
        val targetY = (h * 0.36f).coerceIn(10f, h - 10f)

        // Protection horizontal line (Green)
        drawLine(
            color = SignalEmerald.copy(alpha = 0.55f),
            start = Offset(0f, protY),
            end = Offset(w, protY),
            strokeWidth = 1.5f,
            pathEffect = gridDash
        )

        // Target Exit horizontal line (Purple)
        drawLine(
            color = VelaPurple.copy(alpha = 0.65f),
            start = Offset(0f, targetY),
            end = Offset(w, targetY),
            strokeWidth = 1.5f,
            pathEffect = gridDash
        )

        if (isFlightRunning) {
            // Draw live exponential Aviator climb curve
            val progress = (min(12.0, currentFlightMult - 1.0) / 5.0).toFloat().coerceIn(0.08f, 1.0f)
            val endX = w * (0.22f + 0.72f * progress)
            val endY = (h - 10f - (h - 20f) * progress).coerceIn(10f, h - 10f)

            val curvePath = Path().apply {
                moveTo(8f, h - 8f)
                quadraticTo(
                    endX * 0.62f,
                    h - 10f,
                    endX,
                    endY
                )
            }
            val fillPath = Path().apply {
                addPath(curvePath)
                lineTo(endX, h - 6f)
                lineTo(8f, h - 6f)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AviatorCrimson.copy(alpha = 0.48f),
                        AviatorCrimson.copy(alpha = 0.04f)
                    )
                )
            )
            drawPath(
                path = curvePath,
                color = AviatorCrimson,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
            drawCircle(
                color = TelemetryWhite,
                radius = 5f,
                center = Offset(endX, endY)
            )
        } else {
            // Draw recent vela dispersion sparkline (chronological left-to-right)
            val chronological = recentVelas.take(8).reversed()
            if (chronological.size >= 2) {
                val sparkPath = Path()
                val stepX = (w - 20f) / (chronological.size - 1).coerceAtLeast(1)
                chronological.forEachIndexed { i, mult ->
                    val norm = (min(8.0, mult) / 8.0).toFloat()
                    val x = 10f + i * stepX
                    val y = (h - 12f) - norm * (h - 26f)
                    if (i == 0) sparkPath.moveTo(x, y) else sparkPath.lineTo(x, y)

                    val dotColor = when {
                        mult >= 10.0 -> VelaPink
                        mult >= 2.0 -> VelaPurple
                        else -> VelaBlue
                    }
                    drawCircle(color = dotColor, radius = 4f, center = Offset(x, y))
                }
                drawPath(
                    path = sparkPath,
                    color = VelaBlue.copy(alpha = 0.7f),
                    style = Stroke(width = 2.5f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
private fun FullScreenAviatorRadarUnderlay(
    uiState: AviatorUiState,
    onToggleLiveFlight: () -> Unit,
    onToggleAutoCycle: () -> Unit,
    onSwitchBackToBrowser: () -> Unit
) {
    val analysis = uiState.analysis
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CockpitObsidian, CockpitNavy, CockpitObsidian)
                )
            )
    ) {
        // Large interactive flight radar canvas at the bottom half so it sits cleanly under the top HUD
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .border(1.dp, AviatorCrimson.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CockpitCard)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MiniAviatorTrajectoryCanvas(
                        recentVelas = uiState.recentVelas,
                        currentFlightMult = uiState.currentFlightMultiplier,
                        isFlightRunning = uiState.isFlightRunning,
                        protectionMult = analysis.protectionMultiplier,
                        targetExitMult = analysis.targetExitMultiplier
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RADAR DE VOO AO VIVO · ${uiState.activeBookmaker.displayName.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TelemetrySilver
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.isFlightRunning) {
                                AviatorSignalEngine.formatMult(uiState.currentFlightMultiplier)
                            } else {
                                "PRONTO P/ ENTRADA (${AviatorSignalEngine.formatMult(analysis.targetExitMultiplier)})"
                            },
                            style = MaterialTheme.typography.displayLarge,
                            color = if (uiState.isFlightRunning) AviatorCrimson else SignalEmerald
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Proteção Recomendada: ${AviatorSignalEngine.formatMult(analysis.protectionMultiplier)} | Saída Alvo: ${AviatorSignalEngine.formatMult(analysis.targetExitMultiplier)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = TelemetryWhite
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = onToggleLiveFlight,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AviatorCrimson,
                                    contentColor = TelemetryWhite
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FlightTakeoff,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (uiState.isFlightRunning) "PARAR VOO AGORA" else "DISPARAR VELA DE TESTE")
                            }
                            Button(
                                onClick = onSwitchBackToBrowser,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CockpitCardElevated,
                                    contentColor = KwanzaGold
                                )
                            ) {
                                Text("Voltar ao Navegador")
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun BookmakerWebViewContainer(
    targetUrl: String,
    reloadTrigger: Int,
    onWebViewCreated: (WebView) -> Unit,
    onUrlChanged: (String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit
) {
    var lastLoadedBaseUrl by remember { mutableStateOf("") }
    var lastReloadCount by remember { mutableIntStateOf(0) }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookmaker_webview"),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadsImagesAutomatically = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    mediaPlaybackRequiresUserGesture = false
                    userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36"
                }

                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingChanged(true)
                        if (!url.isNullOrBlank()) {
                            onUrlChanged(url)
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                        if (!url.isNullOrBlank()) {
                            onUrlChanged(url)
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val reqUrl = request?.url?.toString() ?: return false
                        if (reqUrl.startsWith("http://") || reqUrl.startsWith("https://")) {
                            return false
                        }
                        return true
                    }
                }

                onWebViewCreated(this)
                lastLoadedBaseUrl = targetUrl
                loadUrl(targetUrl)
            }
        },
        update = { webView ->
            if (lastLoadedBaseUrl != targetUrl) {
                lastLoadedBaseUrl = targetUrl
                webView.loadUrl(targetUrl)
            }
            if (lastReloadCount != reloadTrigger) {
                lastReloadCount = reloadTrigger
                webView.reload()
            }
        }
    )
}
