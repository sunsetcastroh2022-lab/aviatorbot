package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

@Composable
fun AdminAuthScreen(
    activeUsersCount: Int,
    totalActivitiesCount: Int,
    authError: String?,
    onClearError: () -> Unit,
    onAuthenticateAdmin: (operatorId: String, masterKey: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }

    var operatorId by rememberSaveable { mutableStateOf("ADMIN-MASTER-AO") }
    var masterKey by rememberSaveable { mutableStateOf("admin244") }
    var showKey by rememberSaveable { mutableStateOf(false) }

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
                modifier = Modifier.widthIn(max = 540.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Bar with Back Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = CockpitCard,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .border(1.dp, CockpitBorder, RoundedCornerShape(12.dp))
                            .clickable { onNavigateBack() }
                            .testTag("admin_auth_back_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar ao Login do Jogador",
                                tint = TelemetryWhite,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Voltar ao Login",
                                style = MaterialTheme.typography.labelLarge,
                                color = TelemetryWhite
                            )
                        }
                    }

                    Surface(
                        color = KwanzaGold.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.border(1.dp, KwanzaGold, RoundedCornerShape(50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(KwanzaGold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PORTAL ADMIN ANGOLA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = KwanzaGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Admin Emblem Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, KwanzaGold.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CockpitCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = KwanzaGold.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(68.dp)
                                .border(1.5.dp, KwanzaGold, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = "Autenticação Administrativa",
                                    tint = KwanzaGold,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "AUTENTICAÇÃO DO ADMINISTRADOR",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = KwanzaGold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Acesso restrito ao Painel Criador de Contas, Gestão de Assinantes e Telemetria de Atividade em Tempo Real.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TelemetrySilver,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live System Telemetry Mini Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = CockpitObsidian,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, CockpitBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "ASSINANTES ATIVOS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TelemetrySilver
                                    )
                                    Text(
                                        text = "$activeUsersCount Contas",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalEmerald
                                    )
                                }
                            }

                            Surface(
                                color = CockpitObsidian,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, CockpitBorder, RoundedCornerShape(12.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "EVENTOS REGISTADOS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TelemetrySilver
                                    )
                                    Text(
                                        text = "$totalActivitiesCount Logs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = VelaBlue
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Credentials Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CockpitBorder, RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CockpitCardElevated)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = KwanzaGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CREDENCIAIS DO OPERADOR MESTRE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TelemetryWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = operatorId,
                            onValueChange = {
                                operatorId = it
                                onClearError()
                            },
                            label = { Text("ID do Operador / Administrador") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Badge,
                                    contentDescription = "Operador",
                                    tint = KwanzaGold
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_auth_operator_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KwanzaGold,
                                unfocusedBorderColor = CockpitBorder,
                                focusedLabelColor = KwanzaGold,
                                unfocusedLabelColor = TelemetrySilver,
                                focusedTextColor = TelemetryWhite,
                                unfocusedTextColor = TelemetryWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = masterKey,
                            onValueChange = {
                                masterKey = it
                                onClearError()
                            },
                            label = { Text("Chave Mestre Admin (Padrão: admin244)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = "Chave Mestre",
                                    tint = KwanzaGold
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showKey = !showKey },
                                    modifier = Modifier.testTag("admin_auth_toggle_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (showKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Mostrar ou ocultar chave",
                                        tint = TelemetrySilver
                                    )
                                }
                            },
                            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_auth_key_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KwanzaGold,
                                unfocusedBorderColor = CockpitBorder,
                                focusedLabelColor = KwanzaGold,
                                unfocusedLabelColor = TelemetrySilver,
                                focusedTextColor = TelemetryWhite,
                                unfocusedTextColor = TelemetryWhite
                            )
                        )

                        AnimatedVisibility(visible = authError != null) {
                            Surface(
                                color = SignalDangerDark,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .border(1.dp, SignalDanger, RoundedCornerShape(12.dp))
                                    .testTag("admin_auth_error_banner")
                            ) {
                                Text(
                                    text = authError.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TelemetryWhite,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onAuthenticateAdmin(operatorId, masterKey) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("admin_auth_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KwanzaGold,
                                contentColor = CockpitObsidian
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DESBLOQUEAR DASHBOARD ADMIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Preset Credentials Chip
                        Surface(
                            color = SignalEmeraldDark.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SignalEmerald.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .clickable {
                                    operatorId = "ADMIN-MASTER-AO"
                                    masterKey = "admin244"
                                    onClearError()
                                }
                                .testTag("admin_auth_quick_fill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = SignalEmerald,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Credencial Padrão Ativa: Operador ADMIN-MASTER-AO | Chave: admin244",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TelemetryWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TelemetrySilver)
                ) {
                    Text("Voltar à Tela de Login de Usuários")
                }
            }
        }
    }
}
