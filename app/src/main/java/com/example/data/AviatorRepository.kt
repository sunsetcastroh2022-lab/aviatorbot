package com.example.data

import com.example.domain.BookmakerPlatform
import kotlinx.coroutines.flow.Flow

class AviatorRepository(private val dao: AviatorDao) {

    val allUsers: Flow<List<UserAccountEntity>> = dao.observeAllUsers()
    val subscriberActivities: Flow<List<SubscriberActivityEntity>> = dao.observeSubscriberActivities()

    fun observeVelas(bookmaker: BookmakerPlatform): Flow<List<VelaHistoryEntity>> {
        return dao.observeVelasForBookmaker(bookmaker.id)
    }

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L
        val hourMs = 60L * 60L * 1000L

        if (dao.getUserCount() == 0) {
            dao.insertUser(
                UserAccountEntity(
                    username = "AO-VIP01",
                    password = "1234",
                    fullName = "Mateus Domingos (Luanda)",
                    phoneAngola = "+244 923 450 112",
                    planName = "VIP Mensal (30 Dias)",
                    priceKwanzas = 15000,
                    expiresAtMillis = now + 30L * dayMs,
                    isActive = true,
                    allowedBookmakers = "BANTUBET,ELEPHANTBET,KWANZABET",
                    createdAtMillis = now - 2L * dayMs,
                    lastLoginAtMillis = now - 18L * 60_000L,
                    loginCount = 14,
                    lastActiveBookmaker = "BANTUBET"
                )
            )
            dao.insertUser(
                UserAccountEntity(
                    username = "bantubet99",
                    password = "2026",
                    fullName = "Paulo Kassoma (Benguela)",
                    phoneAngola = "+244 931 882 045",
                    planName = "Semanal Pro (7 Dias)",
                    priceKwanzas = 5000,
                    expiresAtMillis = now + 7L * dayMs,
                    isActive = true,
                    allowedBookmakers = "BANTUBET,ELEPHANTBET,KWANZABET",
                    createdAtMillis = now - dayMs,
                    lastLoginAtMillis = now - 2L * hourMs,
                    loginCount = 6,
                    lastActiveBookmaker = "ELEPHANTBET"
                )
            )
            dao.insertUser(
                UserAccountEntity(
                    username = "huambo24",
                    password = "7777",
                    fullName = "Teresa Nzinga (Huambo)",
                    phoneAngola = "+244 944 119 300",
                    planName = "Diário (24 Horas)",
                    priceKwanzas = 2000,
                    expiresAtMillis = now + dayMs,
                    isActive = true,
                    allowedBookmakers = "BANTUBET,KWANZABET",
                    createdAtMillis = now - 4L * hourMs,
                    lastLoginAtMillis = now - 45L * 60_000L,
                    loginCount = 3,
                    lastActiveBookmaker = "KWANZABET"
                )
            )
        }

        if (dao.getActivityCount() == 0) {
            val seedActivities = listOf(
                SubscriberActivityEntity(
                    username = "AO-VIP01",
                    fullName = "Mateus Domingos (Luanda)",
                    actionType = "VELA_SIGNAL",
                    bookmakerId = "BANTUBET",
                    details = "Capturou Vela Roxa 4.12x · Proteção 1.48x batida com sucesso",
                    timestamp = now - 12L * 60_000L
                ),
                SubscriberActivityEntity(
                    username = "AO-VIP01",
                    fullName = "Mateus Domingos (Luanda)",
                    actionType = "LOGIN",
                    bookmakerId = "BANTUBET",
                    details = "Sessão autenticada no BOT · Conectado em m.bantubet.co.ao",
                    timestamp = now - 18L * 60_000L
                ),
                SubscriberActivityEntity(
                    username = "huambo24",
                    fullName = "Teresa Nzinga (Huambo)",
                    actionType = "BOOKMAKER_SWITCH",
                    bookmakerId = "KWANZABET",
                    details = "Alternou navegador tático para m.kwanzabet.ao",
                    timestamp = now - 42L * 60_000L
                ),
                SubscriberActivityEntity(
                    username = "huambo24",
                    fullName = "Teresa Nzinga (Huambo)",
                    actionType = "LOGIN",
                    bookmakerId = "KWANZABET",
                    details = "Sessão iniciada · Plano Diário (24 Horas)",
                    timestamp = now - 45L * 60_000L
                ),
                SubscriberActivityEntity(
                    username = "bantubet99",
                    fullName = "Paulo Kassoma (Benguela)",
                    actionType = "VELA_SIGNAL",
                    bookmakerId = "ELEPHANTBET",
                    details = "Sinal Favorável (84%) · Saída Alvo atingida em 3.15x na ElephantBet",
                    timestamp = now - 95L * 60_000L
                ),
                SubscriberActivityEntity(
                    username = "bantubet99",
                    fullName = "Paulo Kassoma (Benguela)",
                    actionType = "LOGIN",
                    bookmakerId = "ELEPHANTBET",
                    details = "Acesso autenticado via dispositivo Android · Benguela",
                    timestamp = now - 120L * 60_000L
                )
            )
            seedActivities.reversed().forEach { dao.insertActivity(it) }
        }

        // Seed realistic initial vela sequences for each Angolan bookmaker if empty
        seedBookmakerVelasIfEmpty(
            BookmakerPlatform.BANTUBET,
            listOf(2.15, 1.18, 14.60, 1.25, 3.40, 1.72, 2.85, 4.12, 1.38, 1.54)
        )
        seedBookmakerVelasIfEmpty(
            BookmakerPlatform.ELEPHANTBET,
            listOf(1.45, 2.60, 1.12, 5.80, 1.92, 11.20, 1.34, 2.48, 3.15, 1.62)
        )
        seedBookmakerVelasIfEmpty(
            BookmakerPlatform.KWANZABET,
            listOf(3.20, 1.55, 2.10, 8.90, 1.28, 2.75, 1.44, 18.50, 1.08, 1.11)
        )
    }

    private suspend fun seedBookmakerVelasIfEmpty(
        platform: BookmakerPlatform,
        multipliersOldestFirst: List<Double>
    ) {
        if (dao.getVelaCountForBookmaker(platform.id) == 0) {
            val baseTime = System.currentTimeMillis() - (multipliersOldestFirst.size * 30_000L)
            multipliersOldestFirst.forEachIndexed { index, mult ->
                dao.insertVela(
                    VelaHistoryEntity(
                        bookmakerId = platform.id,
                        multiplier = mult,
                        timestamp = baseTime + index * 30_000L
                    )
                )
            }
        }
    }

    suspend fun authenticateUser(usernameInput: String, passwordInput: String): AuthResult {
        val cleanUser = usernameInput.trim()
        val cleanPass = passwordInput.trim()

        if (cleanUser.isEmpty() || cleanPass.isEmpty()) {
            return AuthResult.Error("Preencha o usuário e a senha de acesso.")
        }

        val account = dao.findUserByUsername(cleanUser)
            ?: return AuthResult.Error("Conta '$cleanUser' não encontrada. Solicite acesso no Painel Admin.")

        if (account.password != cleanPass) {
            return AuthResult.Error("Senha incorreta para o usuário '${account.username}'.")
        }

        if (!account.isActive) {
            return AuthResult.Error("A conta '${account.username}' está suspensa pelo Administrador.")
        }

        if (System.currentTimeMillis() > account.expiresAtMillis) {
            return AuthResult.Error("Licença expirada para '${account.username}'. Renove no Painel Admin.")
        }

        val now = System.currentTimeMillis()
        val firstAllowed = BookmakerPlatform.entries.firstOrNull {
            account.allowedBookmakers.contains(it.id, ignoreCase = true)
        } ?: BookmakerPlatform.BANTUBET

        val updatedAccount = account.copy(
            lastLoginAtMillis = now,
            loginCount = account.loginCount + 1,
            lastActiveBookmaker = firstAllowed.id
        )
        dao.updateUser(updatedAccount)

        dao.insertActivity(
            SubscriberActivityEntity(
                username = updatedAccount.username,
                fullName = updatedAccount.fullName,
                actionType = "LOGIN",
                bookmakerId = firstAllowed.id,
                details = "Login autenticado (#${updatedAccount.loginCount}) · Entrou em ${firstAllowed.displayName}",
                timestamp = now
            )
        )

        return AuthResult.Success(updatedAccount)
    }

    suspend fun logSubscriberActivity(
        user: UserAccountEntity?,
        actionType: String,
        bookmaker: BookmakerPlatform,
        details: String
    ) {
        val username = user?.username ?: "AO-VIP01"
        val fullName = user?.fullName ?: "Operador BOT Angola"
        if (user != null && user.lastActiveBookmaker != bookmaker.id) {
            dao.updateUser(user.copy(lastActiveBookmaker = bookmaker.id))
        }
        dao.insertActivity(
            SubscriberActivityEntity(
                username = username,
                fullName = fullName,
                actionType = actionType,
                bookmakerId = bookmaker.id,
                details = details,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearSubscriberActivities() {
        dao.clearAllActivities()
    }

    suspend fun createUserAccount(
        username: String,
        password: String,
        fullName: String,
        phoneAngola: String,
        planName: String,
        priceKwanzas: Int,
        durationDays: Int,
        allowedBookmakers: String
    ): Result<UserAccountEntity> {
        val cleanUser = username.trim()
        if (cleanUser.isEmpty() || password.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Informe o código de usuário e a senha."))
        }
        val existing = dao.findUserByUsername(cleanUser)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Já existe uma conta com o usuário '$cleanUser'."))
        }
        val now = System.currentTimeMillis()
        val expiresAt = now + durationDays.toLong() * 24L * 60L * 60L * 1000L
        val firstBookmaker = allowedBookmakers.split(",").firstOrNull()?.trim()?.ifEmpty { "BANTUBET" } ?: "BANTUBET"
        val entity = UserAccountEntity(
            username = cleanUser,
            password = password.trim(),
            fullName = fullName.trim().ifEmpty { "Jogador Angola ($cleanUser)" },
            phoneAngola = phoneAngola.trim().ifEmpty { "+244 923 000 000" },
            planName = planName,
            priceKwanzas = priceKwanzas,
            expiresAtMillis = expiresAt,
            isActive = true,
            allowedBookmakers = allowedBookmakers,
            createdAtMillis = now,
            lastLoginAtMillis = null,
            loginCount = 0,
            lastActiveBookmaker = firstBookmaker
        )
        dao.insertUser(entity)
        dao.insertActivity(
            SubscriberActivityEntity(
                username = entity.username,
                fullName = entity.fullName,
                actionType = "ACCOUNT_CREATED",
                bookmakerId = firstBookmaker,
                details = "Nova licença criada pelo Admin · ${entity.planName} (${entity.priceKwanzas} Kz)",
                timestamp = now
            )
        )
        return Result.success(entity)
    }

    suspend fun toggleUserActive(user: UserAccountEntity) {
        val newActive = !user.isActive
        dao.updateUser(user.copy(isActive = newActive))
        dao.insertActivity(
            SubscriberActivityEntity(
                username = user.username,
                fullName = user.fullName,
                actionType = "STATUS_CHANGED",
                bookmakerId = user.lastActiveBookmaker,
                details = if (newActive) "Conta reativada pelo Administrador" else "Conta suspensa pelo Administrador",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun renewUserDays(user: UserAccountEntity, extraDays: Int) {
        val base = maxOf(System.currentTimeMillis(), user.expiresAtMillis)
        val updatedExpiry = base + extraDays.toLong() * 24L * 60L * 60L * 1000L
        dao.updateUser(user.copy(expiresAtMillis = updatedExpiry, isActive = true))
        dao.insertActivity(
            SubscriberActivityEntity(
                username = user.username,
                fullName = user.fullName,
                actionType = "LICENSE_RENEWED",
                bookmakerId = user.lastActiveBookmaker,
                details = "Licença renovada por +$extraDays dias no Painel Admin",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteUser(userId: Int) {
        dao.deleteUserById(userId)
    }

    suspend fun recordVela(
        platform: BookmakerPlatform,
        multiplier: Double,
        loggedInUser: UserAccountEntity? = null
    ) {
        val clamped = multiplier.coerceIn(1.00, 9999.0)
        val now = System.currentTimeMillis()
        dao.insertVela(
            VelaHistoryEntity(
                bookmakerId = platform.id,
                multiplier = clamped,
                timestamp = now
            )
        )
        val tier = when {
            clamped >= 10.0 -> "Vela Rosa (${String.format(java.util.Locale.US, "%.2fx", clamped)})"
            clamped >= 2.0 -> "Vela Roxa (${String.format(java.util.Locale.US, "%.2fx", clamped)})"
            else -> "Vela Azul (${String.format(java.util.Locale.US, "%.2fx", clamped)})"
        }
        dao.insertActivity(
            SubscriberActivityEntity(
                username = loggedInUser?.username ?: "AO-VIP01",
                fullName = loggedInUser?.fullName ?: "Radar Automático BOT",
                actionType = "VELA_SIGNAL",
                bookmakerId = platform.id,
                details = "Registou $tier em ${platform.displayName}",
                timestamp = now
            )
        )
    }

    suspend fun undoLastVela(platform: BookmakerPlatform) {
        dao.deleteLatestVelaForBookmaker(platform.id)
    }

    suspend fun resetVelasForPlatform(platform: BookmakerPlatform) {
        dao.clearVelasForBookmaker(platform.id)
        when (platform) {
            BookmakerPlatform.BANTUBET -> seedBookmakerVelasIfEmpty(
                BookmakerPlatform.BANTUBET,
                listOf(2.15, 1.18, 14.60, 1.25, 3.40, 1.72, 2.85, 4.12, 1.38, 1.54)
            )
            BookmakerPlatform.ELEPHANTBET -> seedBookmakerVelasIfEmpty(
                BookmakerPlatform.ELEPHANTBET,
                listOf(1.45, 2.60, 1.12, 5.80, 1.92, 11.20, 1.34, 2.48, 3.15, 1.62)
            )
            BookmakerPlatform.KWANZABET -> seedBookmakerVelasIfEmpty(
                BookmakerPlatform.KWANZABET,
                listOf(3.20, 1.55, 2.10, 8.90, 1.28, 2.75, 1.44, 18.50, 1.08, 1.11)
            )
        }
    }
}

sealed class AuthResult {
    data class Success(val account: UserAccountEntity) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
