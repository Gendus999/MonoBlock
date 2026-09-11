package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme =
  darkColorScheme(
    primary = WhitePrimary,
    onPrimary = PureBlack,
    secondary = WhiteSecondary,
    onSecondary = PureBlack,
    tertiary = SilverMuted,
    background = PureBlack,
    onBackground = WhitePrimary,
    surface = DarkSurface,
    onSurface = WhitePrimary,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = SilverMuted,
    outline = DarkCellBorder,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}

