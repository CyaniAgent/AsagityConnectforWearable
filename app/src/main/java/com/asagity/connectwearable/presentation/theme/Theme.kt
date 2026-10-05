package com.asagity.connectwearable.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography

@Composable
fun AsagityConnectForWearableTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        // Follow the device system font (e.g. Samsung One UI Watch custom
        // system fonts) instead of a bundled brand font.
        typography = Typography(defaultFontFamily = FontFamily.Default),
        content = content
    )
}
