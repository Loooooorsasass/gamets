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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2

/**
 * Minimalist Studio Puzzle Controller:
 * - 48x48dp interactive hitboxes conforming to Android standards
 * - Dark muted background with clear white/accent arrows
 * - Low visual noise so player's eyes stay on the maze
 */
@Composable
fun DPad(
    onMove: (dx: Int, dy: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    sensitivity: Float = 1.0f,
    accentColor: Color = MazeAmber,
    wallColor: Color = Color(0xFFF05AAB)
) {
    val buttonSize = 48.dp
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
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // UP
        DPadButton(
            icon = Icons.Default.KeyboardArrowUp,
            contentDescription = "Move Up",
            onClick = { throttledMove(0, 1) },
            enabled = enabled,
            accentColor = accentColor,
            modifier = Modifier.size(buttonSize).testTag("dpad_up")
        )

        // LEFT, CENTER DOT, RIGHT
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DPadButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Move Left",
                onClick = { throttledMove(-1, 0) },
                enabled = enabled,
                accentColor = accentColor,
                modifier = Modifier.size(buttonSize).testTag("dpad_left")
            )

            // Minimalist Center Dot
            Box(
                modifier = Modifier
                    .size(buttonSize)
                    .clip(CircleShape)
                    .background(MazeSurface1)
                    .border(1.dp, MazeEdgeHighlight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.6f))
                )
            }

            DPadButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Move Right",
                onClick = { throttledMove(1, 0) },
                enabled = enabled,
                accentColor = accentColor,
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
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(MazeSurface2)
            .border(
                width = 1.dp,
                color = if (enabled) MazeEdgeHighlight else MazeSurface1,
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
            tint = if (enabled) Color(0xFFF5F3FA) else Color(0xFF777185),
            modifier = Modifier.size(24.dp)
        )
    }
}
