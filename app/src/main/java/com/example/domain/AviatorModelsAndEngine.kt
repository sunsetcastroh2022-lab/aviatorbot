package com.example.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

enum class BookmakerPlatform(
    val id: String,
    val displayName: String,
    val shortTag: String,
    val baseUrl: String,
    val aviatorHint: String,
    val accentHex: Long
) {
    BANTUBET(
        id = "BANTUBET",
        displayName = "BantuBet Angola",
        shortTag = "m.bantubet.co.ao",
        baseUrl = "https://m.bantubet.co.ao",
        aviatorHint = "Menu Casino / Crash -> Aviator Spribe",
        accentHex = 0xFFFFB300
    ),
    ELEPHANTBET(
        id = "ELEPHANTBET",
        displayName = "ElephantBet AO",
        shortTag = "elephantbet.co.ao",
        baseUrl = "https://www.elephantbet.co.ao",
        aviatorHint = "Aba Aviator / Jogos Rápidos",
        accentHex = 0xFF1EA7FD
    ),
    KWANZABET(
        id = "KWANZABET",
        displayName = "KwanzaBet AO",
        shortTag = "m.kwanzabet.ao",
        baseUrl = "https://m.kwanzabet.ao",
        aviatorHint = "Lobby Principal -> Aviator",
        accentHex = 0xFF00E676
    );

    companion object {
        fun fromId(id: String): BookmakerPlatform =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BANTUBET
    }
}

enum class ChartConditionStatus(
    val badgeTitle: String,
    val shortAction: String
) {
    FAVORABLE(
        badgeTitle = "RAZOÁVEL P/ APOSTAR · SINAL VERDE",
        shortAction = "ENTRADA CONFIRMADA"
    ),
    MODERATE(
        badgeTitle = "MODERADO · APOSTAR C/ PROTEÇÃO ALTA",
        shortAction = "CAUTELA / PROTEÇÃO"
    ),
    UNFAVORABLE(
        badgeTitle = "NÃO APOSTAR · GRÁFICO EM RECOLHA",
        shortAction = "AGUARDAR PADRÃO"
    )
}

data class VelaReachBand(
    val id: String,
    val title: String,
    val subtitle: String,
    val minRange: Double,
    val maxRange: Double,
    val probabilityPercent: Int,
    val isReachedInFlight: Boolean = false,
    val isCurrentZoneInFlight: Boolean = false
)

data class KwanzaBetSplit(
    val totalStakeKz: Int,
    val protectionStakeKz: Int,
    val protectionAutoCashout: Double,
    val protectionReturnKz: Int,
    val targetStakeKz: Int,
    val targetAutoCashout: Double,
    val targetReturnKz: Int,
    val totalProfitIfBothHitKz: Int
)

data class ChartAnalysisResult(
    val status: ChartConditionStatus,
    val scorePercent: Int,
    val headline: String,
    val detailedAnalysis: String,
    val protectionMultiplier: Double,
    val targetExitMultiplier: Double,
    val maxCeilingMultiplier: Double,
    val reachBands: List<VelaReachBand>,
    val blueCount: Int,
    val purpleCount: Int,
    val pinkCount: Int,
    val roundsSincePink: Int,
    val movingAverage5: Double,
    val volatilityPercent: Int,
    val liveProgressDiagnosis: String
)

object AviatorSignalEngine {

    /**
     * Analisa as últimas velas (da mais recente no índice 0 até as anteriores)
     * e também leva em conta o multiplicador em tempo real ao decorrer da vela (currentFlightMultiplier).
     */
    fun analyzeChart(
        recentVelasNewestFirst: List<Double>,
        currentFlightMultiplier: Double,
        isFlightRunning: Boolean
    ): ChartAnalysisResult {
        val velas = if (recentVelasNewestFirst.isEmpty()) {
            listOf(1.42, 1.18, 2.35, 1.65, 4.10, 1.22, 1.88, 12.40, 1.15, 2.80)
        } else {
            recentVelasNewestFirst.take(20)
        }

        val last5 = velas.take(5)
        val last10 = velas.take(10)

        val blueCount = velas.count { it < 2.00 }
        val purpleCount = velas.count { it in 2.00..9.99 }
        val pinkCount = velas.count { it >= 10.00 }

        val idxPink = velas.indexOfFirst { it >= 10.00 }
        val roundsSincePink = if (idxPink == -1) velas.size else idxPink

        val avg5 = if (last5.isNotEmpty()) last5.average() else 1.80
        val variance = if (last10.size > 1) {
            val mean = last10.average()
            last10.map { (it - mean) * (it - mean) }.average()
        } else 1.0
        val stdDev = sqrt(variance)
        val volatilityPercent = min(98, max(12, (stdDev * 14.0).roundToInt()))

        // Detect low crash streak (1.00x - 1.15x)
        val instantCrashesInLast4 = velas.take(4).count { it <= 1.15 }
        // Detect consecutive blues (< 2.00x) at the head of the list
        var consecutiveBluesAtHead = 0
        for (v in velas) {
            if (v < 2.00) consecutiveBluesAtHead++ else break
        }
        // Check if a massive pink (>= 15x) just hit in the last 1-2 rounds (often followed by bankroll collection)
        val recentHeavyPink = velas.take(2).any { it >= 14.0 }

        val status: ChartConditionStatus
        val score: Int
        val protectionMult: Double
        val targetExitMult: Double
        val maxCeilingMult: Double
        val headline: String
        val detailed: String

        when {
            instantCrashesInLast4 >= 2 -> {
                status = ChartConditionStatus.UNFAVORABLE
                score = max(18, 34 - instantCrashesInLast4 * 6)
                protectionMult = 1.35
                targetExitMult = 1.85
                maxCeilingMult = 2.60
                headline = "Não Razoável Agora: Sequência de Crash Curto (≤1.15x)"
                detailed = "O gráfico apresenta recolha agressiva nas últimas 4 rodadas. Aguarde pelo menos 1 vela acima de 1.90x antes de entrar."
            }
            recentHeavyPink && consecutiveBluesAtHead <= 1 -> {
                status = ChartConditionStatus.UNFAVORABLE
                score = 32
                protectionMult = 1.40
                targetExitMult = 1.95
                maxCeilingMult = 3.10
                headline = "Cautela Máxima: Pós-Vela Rosa Alta (Ciclo de Recolha)"
                detailed = "Após vela Rosa elevada recente, o algoritmo costuma compensar com 2 a 3 velas azuis curtas. Recomendado aguardar estabilização."
            }
            consecutiveBluesAtHead in 2..4 && instantCrashesInLast4 == 0 -> {
                status = ChartConditionStatus.FAVORABLE
                score = min(94, 78 + consecutiveBluesAtHead * 4)
                protectionMult = 1.48
                targetExitMult = 2.65
                maxCeilingMult = if (roundsSincePink >= 7) 11.50 else 5.80
                headline = "Gráfico Razoável p/ Apostar: Compressão de $consecutiveBluesAtHead Azuis Estáveis"
                detailed = "Padrão de recuperação detectado após $consecutiveBluesAtHead velas azuis sem crash de 1.00x. Alta probabilidade de expansão para zona Roxa (2.20x–3.80x)."
            }
            last5.count { it >= 2.00 } >= 3 && !recentHeavyPink -> {
                status = ChartConditionStatus.FAVORABLE
                score = 84
                protectionMult = 1.52
                targetExitMult = 2.90
                maxCeilingMult = if (roundsSincePink >= 6) 14.20 else 7.40
                headline = "Gráfico Favorável: Corredor de Velas Roxas Ativo"
                detailed = "3 das últimas 5 velas superaram 2.00x com média de ${formatMult(avg5)}. Excelente janela para Saída na Proteção (1.52x) e Alvo em 2.90x."
            }
            consecutiveBluesAtHead >= 5 -> {
                status = ChartConditionStatus.MODERATE
                score = 58
                protectionMult = 1.42
                targetExitMult = 2.15
                maxCeilingMult = 4.50
                headline = "Moderado: Sequência Longa de $consecutiveBluesAtHead Azuis"
                detailed = "Gráfico em acumulação prolongada. Se entrar, priorize Proteção curta em 1.42x com 75% da aposta e Saída conservadora em 2.15x."
            }
            else -> {
                status = ChartConditionStatus.MODERATE
                score = 66
                protectionMult = 1.45
                targetExitMult = 2.35
                maxCeilingMult = if (roundsSincePink >= 5) 9.80 else 5.20
                headline = "Gráfico Estável / Razoável com Proteção em 1.45x"
                detailed = "Alternância equilibrada entre velas azuis e roxas ($roundsSincePink rodadas desde a última Rosa). Use gestão dupla: Proteção 1.45x e Saída 2.35x."
            }
        }

        // Dynamic probabilities adjusted as the current flight multiplier progresses ("ao decorrer da vela")
        val baseProb1 = when (status) {
            ChartConditionStatus.FAVORABLE -> 74
            ChartConditionStatus.MODERATE -> 63
            ChartConditionStatus.UNFAVORABLE -> 44
        }
        val baseProb2 = when (status) {
            ChartConditionStatus.FAVORABLE -> 49
            ChartConditionStatus.MODERATE -> 37
            ChartConditionStatus.UNFAVORABLE -> 21
        }
        val baseProb3 = when (status) {
            ChartConditionStatus.FAVORABLE -> if (roundsSincePink >= 6) 24 else 16
            ChartConditionStatus.MODERATE -> 12
            ChartConditionStatus.UNFAVORABLE -> 6
        }

        // Adjust dynamic probability if the vela is currently flying and already passed certain thresholds
        val dynamicProb1 = if (isFlightRunning && currentFlightMultiplier >= protectionMult) 100
        else if (isFlightRunning && currentFlightMultiplier > 1.15) min(95, baseProb1 + 12)
        else baseProb1

        val dynamicProb2 = if (isFlightRunning && currentFlightMultiplier >= targetExitMult) 100
        else if (isFlightRunning && currentFlightMultiplier >= protectionMult) min(88, baseProb2 + 22)
        else baseProb2

        val dynamicProb3 = if (isFlightRunning && currentFlightMultiplier >= 10.0) 100
        else if (isFlightRunning && currentFlightMultiplier >= targetExitMult) min(68, baseProb3 + 25)
        else baseProb3

        val reachBands = listOf(
            VelaReachBand(
                id = "BAND_PROTECTION",
                title = "1º Alcance · Zona de Proteção",
                subtitle = "Garante cobertura total das 2 apostas",
                minRange = 1.35,
                maxRange = protectionMult + 0.22,
                probabilityPercent = dynamicProb1,
                isReachedInFlight = isFlightRunning && currentFlightMultiplier >= protectionMult,
                isCurrentZoneInFlight = isFlightRunning && currentFlightMultiplier in 1.00..(protectionMult + 0.22)
            ),
            VelaReachBand(
                id = "BAND_PURPLE",
                title = "2º Alcance · Alvo Vela Roxa",
                subtitle = "Zona principal de Saída com lucro",
                minRange = 2.00,
                maxRange = max(3.20, targetExitMult + 0.85),
                probabilityPercent = dynamicProb2,
                isReachedInFlight = isFlightRunning && currentFlightMultiplier >= targetExitMult,
                isCurrentZoneInFlight = isFlightRunning && currentFlightMultiplier in (protectionMult + 0.22)..max(3.20, targetExitMult + 0.85)
            ),
            VelaReachBand(
                id = "BAND_PINK",
                title = "3º Alcance · Expansão Rosa (Pico)",
                subtitle = "Deixar apenas 20% se romper ${formatMult(targetExitMult)}",
                minRange = 5.00,
                maxRange = maxCeilingMult,
                probabilityPercent = dynamicProb3,
                isReachedInFlight = isFlightRunning && currentFlightMultiplier >= maxCeilingMult,
                isCurrentZoneInFlight = isFlightRunning && currentFlightMultiplier > max(3.20, targetExitMult + 0.85)
            )
        )

        val liveProgressDiagnosis = when {
            !isFlightRunning -> "Radar pronto p/ próxima vela · Proteção em ${formatMult(protectionMult)} | Saída em ${formatMult(targetExitMult)}"
            currentFlightMultiplier < 1.25 -> "Subida inicial (${formatMult(currentFlightMultiplier)}) -> Aguardando romper zona crítica 1.25x"
            currentFlightMultiplier < protectionMult -> "Rumo à Proteção (${formatMult(protectionMult)}) -> Prepare Auto-Cashout 1!"
            currentFlightMultiplier < 2.00 -> "✅ PROTEÇÃO BATIDA (${formatMult(protectionMult)})! Aposta 1 paga · Buscando Vela Roxa 2.00x+"
            currentFlightMultiplier < targetExitMult -> "🟣 VELA ROXA ATIVA (${formatMult(currentFlightMultiplier)})! Próximo alcance: Saída Alvo ${formatMult(targetExitMult)}"
            currentFlightMultiplier < 10.00 -> "🎯 SAÍDA ALVO (${formatMult(targetExitMult)}) ATINGIDA! Lucro garantido · Rumo ao teto ${formatMult(maxCeilingMult)}"
            else -> "🌸 VELA ROSA EXPLOSIVA (${formatMult(currentFlightMultiplier)})! Realize o lucro imediatamente!"
        }

        return ChartAnalysisResult(
            status = status,
            scorePercent = score,
            headline = headline,
            detailedAnalysis = detailed,
            protectionMultiplier = protectionMult,
            targetExitMultiplier = targetExitMult,
            maxCeilingMultiplier = maxCeilingMult,
            reachBands = reachBands,
            blueCount = blueCount,
            purpleCount = purpleCount,
            pinkCount = pinkCount,
            roundsSincePink = roundsSincePink,
            movingAverage5 = avg5,
            volatilityPercent = volatilityPercent,
            liveProgressDiagnosis = liveProgressDiagnosis
        )
    }

    /**
     * Calcula a divisão exata em Kwanzas (Kz) para os 2 slots de aposta do Aviator:
     * Slot 1 (Proteção): ~70% da banca da rodada para cobrir 100% do valor total no multiplicador de proteção.
     * Slot 2 (Saída Alvo): ~30% da banca da rodada buscando o multiplicador da Vela Roxa/Rosa.
     */
    fun calculateKwanzaSplit(
        totalStakeKz: Int,
        protectionMultiplier: Double,
        targetExitMultiplier: Double
    ): KwanzaBetSplit {
        val safeTotal = max(100, totalStakeKz)
        // Ideal protection stake so that protectionStake * protectionMultiplier >= totalStakeKz * 1.02
        val rawProtectionRatio = min(0.76, max(0.62, 1.03 / max(1.30, protectionMultiplier)))
        val protectionStake = ((safeTotal * rawProtectionRatio) / 10.0).roundToInt() * 10
        val targetStake = max(10, safeTotal - protectionStake)

        val protectionReturn = (protectionStake * protectionMultiplier).roundToInt()
        val targetReturn = (targetStake * targetExitMultiplier).roundToInt()
        val netProfitBoth = (protectionReturn + targetReturn) - safeTotal

        return KwanzaBetSplit(
            totalStakeKz = safeTotal,
            protectionStakeKz = protectionStake,
            protectionAutoCashout = protectionMultiplier,
            protectionReturnKz = protectionReturn,
            targetStakeKz = targetStake,
            targetAutoCashout = targetExitMultiplier,
            targetReturnKz = targetReturn,
            totalProfitIfBothHitKz = netProfitBoth
        )
    }

    fun formatMult(value: Double): String = String.format(java.util.Locale.US, "%.2fx", value)
}
