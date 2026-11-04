package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = QuestRoyalBlue,
    onPrimary = Color.White,
    primaryContainer = QuestSkyLight,
    onPrimaryContainer = QuestRoyalBlueDark,
    secondary = StarGold,
    onSecondary = InkNavyText,
    secondaryContainer = StarGoldSoft,
    onSecondaryContainer = Color(0xFF5C4000),
    tertiary = QuestMint,
    onTertiary = Color.White,
    tertiaryContainer = QuestMintSoft,
    onTertiaryContainer = Color(0xFF004D3B),
    error = StreakFlameCoral,
    onError = Color.White,
    errorContainer = StreakFlameSoft,
    onErrorContainer = Color(0xFF7A0B16),
    background = WarmCreamBg,
    onBackground = InkNavyText,
    surface = WarmSurfaceCard,
    onSurface = InkNavyText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = SlateMutedText
)

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimaryBlue,
    onPrimary = NightSkyBg,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = StarGoldBright,
    onSecondary = NightSkyBg,
    secondaryContainer = Color(0xFF422D00),
    onSecondaryContainer = StarGoldSoft,
    tertiary = QuestMint,
    onTertiary = NightSkyBg,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = QuestMintSoft,
    error = StreakFlameCoral,
    onError = Color.White,
    background = NightSkyBg,
    onBackground = NightTextPrimary,
    surface = NightCardSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightTextSecondary
)

val QuestShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = QuestShapes,
        content = content
    )
}
