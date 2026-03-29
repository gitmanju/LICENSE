package com.dheemai.treecounter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GreenColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA5D6A7),
    secondary = Color(0xFF558B2F),
    tertiary = Color(0xFF795548),
    background = Color(0xFFF1F8E9),
    surface = Color.White,
)

@Composable
fun TreeCounterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GreenColorScheme,
        content = content
    )
}
