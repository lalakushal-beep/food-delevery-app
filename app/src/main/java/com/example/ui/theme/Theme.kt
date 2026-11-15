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
    primary = TandoorTerracotta,
    onPrimary = Color.White,
    primaryContainer = TerracottaContainerLight,
    onPrimaryContainer = OnTerracottaContainerLight,
    secondary = SaffronGold,
    onSecondary = Color.White,
    secondaryContainer = SaffronContainerLight,
    onSecondaryContainer = OnSaffronContainerLight,
    tertiary = MintChutneyGreen,
    onTertiary = Color.White,
    tertiaryContainer = MintContainerLight,
    onTertiaryContainer = OnMintContainerLight,
    background = CreamBackground,
    onBackground = EspressoInk,
    surface = CardSurfaceLight,
    onSurface = EspressoInk,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = MutedCocoa,
    surfaceContainerLow = WarmParchment,
    surfaceContainer = WarmParchment,
    outline = WarmOutlineLight,
    outlineVariant = Color(0xFFEDE0D6),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = TandoorTerracottaBright,
    onPrimary = Color(0xFF2D0600),
    primaryContainer = TerracottaContainerDark,
    onPrimaryContainer = OnTerracottaContainerDark,
    secondary = SaffronGoldBright,
    onSecondary = Color(0xFF2B1700),
    secondaryContainer = SaffronContainerDark,
    onSecondaryContainer = OnSaffronContainerDark,
    tertiary = Color(0xFF52D19F),
    onTertiary = Color(0xFF002114),
    tertiaryContainer = MintContainerDark,
    onTertiaryContainer = OnMintContainerDark,
    background = EspressoBackgroundDark,
    onBackground = WarmIvoryText,
    surface = ClaySurfaceDark,
    onSurface = WarmIvoryText,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = MutedSandText,
    surfaceContainerLow = ClaySurfaceDark,
    surfaceContainer = CardSurfaceDark,
    outline = WarmOutlineDark,
    outlineVariant = Color(0xFF3E3028),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MyFoodDeliveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
