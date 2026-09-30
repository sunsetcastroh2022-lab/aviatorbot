package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AviatorCockpitColorScheme = darkColorScheme(
    primary = AviatorCrimson,
    onPrimary = TelemetryWhite,
    primaryContainer = AviatorCrimsonDark,
    onPrimaryContainer = TelemetryWhite,
    secondary = SignalEmerald,
    onSecondary = CockpitObsidian,
    secondaryContainer = SignalEmeraldDark,
    onSecondaryContainer = SignalEmerald,
    tertiary = VelaPink,
    onTertiary = TelemetryWhite,
    tertiaryContainer = VelaPinkSurface,
    onTertiaryContainer = VelaPink,
    background = CockpitObsidian,
    onBackground = TelemetryWhite,
    surface = CockpitNavy,
    onSurface = TelemetryWhite,
    surfaceVariant = CockpitCard,
    onSurfaceVariant = TelemetrySilver,
    outline = CockpitBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AviatorCockpitColorScheme,
        typography = Typography,
        content = content
    )
}
