package com.aplan.shoealerts.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Blue700,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Blue200,
    secondary = Teal400,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = Teal200,
    tertiary = Orange400,
    background = Surface,
    surface = Surface,
    error = Red400
)

private val DarkColors = darkColorScheme(
    primary = Blue200,
    onPrimary = Blue700,
    primaryContainer = Blue700,
    secondary = Teal200,
    onSecondary = SurfaceDark,
    secondaryContainer = Teal400,
    tertiary = Orange400,
    background = SurfaceDark,
    surface = SurfaceDark,
    error = Red400
)

@Composable
fun ShoeAlertTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
