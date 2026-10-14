package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PizzaDarkColorScheme = darkColorScheme(
    primary = PizzaOrange,
    onPrimary = Color.White,
    primaryContainer = PizzaOrangeDark,
    onPrimaryContainer = Color.White,
    secondary = PizzaOrangeLight,
    onSecondary = Color.Black,
    tertiary = PizzaGreen,
    onTertiary = Color.White,
    background = PizzaDarkBg,
    onBackground = TextPrimary,
    surface = PizzaCardBg,
    onSurface = TextPrimary,
    surfaceVariant = PizzaPanelBg,
    onSurfaceVariant = TextSecondary,
    outline = PizzaBorder,
    outlineVariant = PizzaBorderLight,
    error = PizzaRed,
    onError = Color.White
)

@Composable
fun PizzaPosTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = PizzaDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PizzaDarkBg.toArgb()
                window.navigationBarColor = PizzaDarkBg.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
