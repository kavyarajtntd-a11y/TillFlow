package com.kavyaraj.tillflow.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Espresso = Color(0xFF4E342E)
private val Crema = Color(0xFFD7B899)
private val Mint = Color(0xFF2E7D6B)
private val Amber = Color(0xFF8A5300)       // "low stock" on light backgrounds
private val AmberLight = Color(0xFFFFB95C)  // "low stock" on dark backgrounds

private val LightColors = lightColorScheme(
    primary = Espresso,
    secondary = Mint,
    tertiary = Amber,
)

private val DarkColors = darkColorScheme(
    primary = Crema,
    secondary = Color(0xFF7FCDB8),
    tertiary = AmberLight,
)

/** Uses the device's wallpaper colours on Android 12+, and the café palette elsewhere. */
@Composable
fun TillFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
