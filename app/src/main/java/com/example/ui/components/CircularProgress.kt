package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CircularProgress(
    size: Dp,
    progress: Float,
    strokeWidth: Dp = 10.dp,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val clampedProgress = progress.coerceIn(0.0f, 1.0f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (this.size.minDimension - strokeWidthPx) / 2f
            
            // Track Circle
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )
            // Progress Arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = clampedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        content()
    }
}
