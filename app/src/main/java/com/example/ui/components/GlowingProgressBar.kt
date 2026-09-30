package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeAmberGlow
import com.example.ui.theme.MazeSurface2

/**
 * Thanh tiến trình mở khóa cao cấp (height: 8dp, bo tròn hoàn toàn).
 * Thay thế thanh tải file màu xanh lục cũ bằng hiệu ứng Amber/Vàng hổ phách với Glow phát sáng ở đầu thanh chạy.
 */
@Composable
fun GlowingProgressBar(
    progress: Float, // 0.0f .. 1.0f
    modifier: Modifier = Modifier,
    activeColor: Color = MazeAmber,
    glowColor: Color = MazeAmberGlow,
    trackColor: Color = MazeSurface2
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    // Hiệu ứng nhịp đập phát sáng nhẹ ở đầu thanh chạy
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val activeWidth = width * clampedProgress

            if (activeWidth > 0f) {
                // Vẽ thanh chạy chính gradient màu hổ phách
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            activeColor.copy(alpha = 0.85f),
                            activeColor,
                            glowColor
                        ),
                        startX = 0f,
                        endX = activeWidth
                    ),
                    topLeft = Offset.Zero,
                    size = Size(activeWidth, height),
                    cornerRadius = CornerRadius(height / 2, height / 2)
                )

                // Vẽ vầng sáng (Glow) tản mát ở đầu thanh tiến trình
                val glowRadius = height * 1.5f
                val headCenter = Offset(activeWidth, height / 2)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.9f * pulseAlpha),
                            glowColor.copy(alpha = 0.35f * pulseAlpha),
                            Color.Transparent
                        ),
                        center = headCenter,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = headCenter
                )
            }
        }
    }
}
