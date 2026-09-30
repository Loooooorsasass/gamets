package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2

/**
 * Studio Pro Tactile D-Pad Controller:
 * - Thiết kế nút bấm nổi 3D với viền bắt sáng Specular Top Rim
 * - Hào quang Radial Spotlight đồng bộ với bảng 10 màu Bản Đồ Studio
 * - Tâm điều khiển (Studio Core Hub) ở chính giữa giúp định hướng trực quan
 */
@Composable
fun DPad(
    onMove: (dx: Int, dy: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sensitivity: Float = 1.0f,
    accentColor: Color = MazeAmber,
    wallColor: Color = Color(0xFF38BDF8)
) {
    val buttonSize = 36.dp
    val lastTapTimeRef = remember { java.util.concurrent.atomic.AtomicLong(0L) }
    val throttledMove: (Int, Int) -> Unit = { dx, dy ->
        val now = System.currentTimeMillis()
        val delayMs = (85L / sensitivity.coerceIn(0.5f, 2.5f)).toLong()
        if (now - lastTapTimeRef.get() >= delayMs) {
            lastTapTimeRef.set(now)
            onMove(dx, dy)
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // UP
        DPadButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Move Up",
            onClick = { throttledMove(0, 1) },
            enabled = enabled,
            accentColor = accentColor,
            wallColor = wallColor,
            modifier = Modifier.size(buttonSize).testTag("dpad_up")
        )

        // LEFT, STUDIO CORE HUB, RIGHT
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DPadButton(
                icon = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Move Left",
                onClick = { throttledMove(-1, 0) },
                enabled = enabled,
                accentColor = accentColor,
                wallColor = wallColor,
                modifier = Modifier.size(buttonSize).testTag("dpad_left")
            )

            // Central Studio Core Hub
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.28f),
                                MazeSurface1.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(accentColor.copy(alpha = 0.65f), wallColor.copy(alpha = 0.25f))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(11.dp)
                )
            }

            DPadButton(
                icon = Icons.Default.KeyboardArrowRight,
                contentDescription = "Move Right",
                onClick = { throttledMove(1, 0) },
                enabled = enabled,
                accentColor = accentColor,
                wallColor = wallColor,
                modifier = Modifier.size(buttonSize).testTag("dpad_right")
            )
        }

        // DOWN
        DPadButton(
            icon = Icons.Default.KeyboardArrowDown,
            contentDescription = "Move Down",
            onClick = { throttledMove(0, -1) },
            enabled = enabled,
            accentColor = accentColor,
            wallColor = wallColor,
            modifier = Modifier.size(buttonSize).testTag("dpad_down")
        )
    }
}

@Composable
private fun DPadButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    accentColor: Color,
    wallColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MazeSurface2,
                        MazeSurface1
                    )
                )
            )
            .border(
                width = 1.5.dp,
                color = if (enabled) accentColor.copy(alpha = 0.8f) else Color(0xFF334155),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = accentColor),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) Color(0xFFF8FAFC) else Color(0xFF64748B),
            modifier = Modifier.size(24.dp)
        )
    }
}
