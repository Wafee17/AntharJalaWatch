package com.example.anthar_jalawatch.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VisualDepthIndicator(
    depthPercentage: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = depthPercentage.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "DepthAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            val width = size.width
            val height = size.height

            // Draw Ground Line
            drawLine(
                color = Color(0xFF8D6E63), // Brownish color
                start = Offset(0f, 2.dp.toPx()),
                end = Offset(width, 2.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )

            // Draw Water Level (animated height from bottom)
            val waterHeight = height * animatedPercentage
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF38BDF8),
                        Color(0xFF0284C7)
                    )
                ),
                topLeft = Offset(0f, height - waterHeight),
                size = Size(width, waterHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }
    }
}
