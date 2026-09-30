package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserAccountEntity
import com.example.ui.theme.AviatorCrimson
import com.example.ui.theme.CockpitBorder
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardElevated
import com.example.ui.theme.CockpitNavy
import com.example.ui.theme.CockpitObsidian
import com.example.ui.theme.KwanzaGold
import com.example.ui.theme.SignalDanger
import com.example.ui.theme.SignalDangerDark
import com.example.ui.theme.SignalEmerald
import com.example.ui.theme.SignalEmeraldDark
import com.example.ui.theme.TelemetrySilver
import com.example.ui.theme.TelemetryWhite
import com.example.ui.theme.VelaBlue
import com.example.ui.theme.VelaPink

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    users: List<UserAccountEntity>,
    loginError: String?,
    onClearError: () -> Unit,
    onLoginSubmit: (String, String) -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    var username by rememberSaveable { mutableStateOf("AO-VIP01") }
    var password by rememberSaveable { mutableStateOf("1234") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("admin244") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CockpitObsidian, CockpitNavy, CockpitObsidian)
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 560.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hero Cockpit Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CockpitBorder, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CockpitCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_cockpit_banner),
                            contentDescription = "Radar de Telemetria Aviator AO",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            CockpitObsidian.copy(alpha = 0.25f),
                                            CockpitObsidian.copy(alpha = 0.85f),
                                            CockpitObsidian
                                        )
                                    )
                                )
                        )

                        // Top Angola Badge + Live Radar Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = CockpitObsidian.copy(alpha = 0.82f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(1.dp, AviatorCrimson, RoundedCornerShape(50))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SignalEmerald)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ANGOLA · RADAR V4.2 ATIVO",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = TelemetryWhite
                                    )
                                }
                            }

                            Surface(
                                color = VelaPink.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.border(1.dp, VelaPink, RoundedCornerShape(50))
                            ) {
                                Text(
                                    text = "SAÍDA & PROTEÇÃO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VelaPink,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Bottom Title inside Hero
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.FlightTakeoff,
                                    contentDescription = "Ícone Aviator AO",
                                    tint = AviatorCrimson,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = TelemetryWhite
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = AviatorCrimson,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "BOT PRO",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TelemetryWhite,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.login_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TelemetrySilver
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Supported Angolan Bookmakers Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BookmakerPillBadge(
                        name = "BantuBet",
                        domain = "m.bantubet.co.ao",
                        accent = KwanzaGold,
                        modifier = Modifier.weight(1f)
                    )
                    BookmakerPillBadge(
                        name = "ElephantBet",
                        domain = "elephantbet.co.ao",
                        accent = VelaBlue,
                        modifier = Modifier.weight(1f)
                    )
                    BookmakerPillBadge(
                        name = "KwanzaBet",
                        domain = "m.kwanzabet.ao",
                        accent = SignalEmerald,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Login Card
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
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Radar,
                                    contentDescription = "Acesso ao BOT",
                                    tint = SignalEmerald,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACESSO DO JOGADOR AO BOT",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TelemetryWhite
                                )
                            }
                            Text(
                                text = "Licença Verificada",
                                style = MaterialTheme.typography.labelSmall,
                                color = SignalEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                onClearError()
                            },
                            label = { Text(stringResource(id = R.string.login_username_label)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Usuário",
                                    tint = TelemetrySilver
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AviatorCrimson,
                                unfocusedBorderColor = CockpitBorder,
                                focusedLabelColor = AviatorCrimson,
                                unfocusedLabelColor = TelemetrySilver,
                                focusedTextColor = TelemetryWhite,
                                unfocusedTextColor = TelemetryWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                onClearError()
                            },
                            label = { Text(stringResource(id = R.string.login_password_label)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "Senha",
                                    tint = TelemetrySilver
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("toggle_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Mostrar ou ocultar senha",
                                        tint = TelemetrySilver
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AviatorCrimson,
                                unfocusedBorderColor = CockpitBorder,
                                focusedLabelColor = AviatorCrimson,
                                unfocusedLabelColor = TelemetrySilver,
                                focusedTextColor = TelemetryWhite,
                                unfocusedTextColor = TelemetryWhite
                            )
                        )

                        AnimatedVisibility(visible = loginError != null) {
                            Surface(
                                color = SignalDangerDark,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .border(1.dp, SignalDanger, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = loginError.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TelemetryWhite,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onLoginSubmit(username, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AviatorCrimson,
                                contentColor = TelemetryWhite
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FlightTakeoff,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(id = R.string.login_button_enter),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick-Fill Active Accounts created in Admin Panel
                        if (users.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "CONTAS ATIVAS NO SISTEMA (TOQUE P/ PREENCHER):",
                                style = MaterialTheme.typography.labelSmall,
                                color = TelemetrySilver
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                users.take(5).forEach { acc ->
                                    Surface(
                                        color = if (username == acc.username) SignalEmeraldDark else CockpitCardElevated,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .border(
                                                1.dp,
                                                if (username == acc.username) SignalEmerald else CockpitBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                username = acc.username
                                                password = acc.password
                                                onClearError()
                                            }
                                            .testTag("quick_account_${acc.username}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                tint = if (acc.isActive) SignalEmerald else SignalDanger,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${acc.username} (${acc.planName.substringBefore(" ")})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = TelemetryWhite
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dedicated Admin App / Account Creator Panel Entry Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, KwanzaGold.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CockpitCardElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = KwanzaGold.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = "Painel Admin",
                                        tint = KwanzaGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "APP PAINEL ADMIN · CRIADOR DE CONTAS",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = KwanzaGold
                                )
                                Text(
                                    text = "Crie logins e senhas p/ clientes do BOT Aviator AO, defina validade e envie via WhatsApp.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TelemetrySilver
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { onOpenAdminPanel() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("open_admin_panel_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = KwanzaGold
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.admin_panel_button),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.statistical_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = TelemetrySilver,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }

    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPinDialog = false },
            containerColor = CockpitCard,
            titleContentColor = TelemetryWhite,
            textContentColor = TelemetrySilver,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = KwanzaGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acesso Mestre — Criador de Contas",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Insira a chave mestre do Administrador para abrir o painel gerador de contas de usuários (Padrão: admin244):",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = { adminPinInput = it },
                        label = { Text("Chave Mestre Admin") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KwanzaGold,
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
                        showAdminPinDialog = false
                        onOpenAdminPanel()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KwanzaGold,
                        contentColor = CockpitObsidian
                    ),
                    modifier = Modifier.testTag("confirm_admin_pin_button")
                ) {
                    Text("ABRIR PAINEL ADMIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminPinDialog = false }) {
                    Text("Cancelar", color = TelemetrySilver)
                }
            }
        )
    }
}

@Composable
private fun BookmakerPillBadge(
    name: String,
    domain: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CockpitCard,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    color = TelemetryWhite,
                    fontSize = 12.sp
                )
            }
            Text(
                text = domain,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                fontSize = 9.sp
            )
        }
    }
}
