package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeStar
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import com.example.ui.theme.StudioMapPalette

/**
 * Studio Pro Tactile Level Button:
 * - Tích hợp bảng 10 màu Bản Đồ Studio (StudioMapPalette) đặc sắc với ánh sáng rõ ràng
 * - Hiệu ứng Spotlight phát quang tâm nút + Viền bắt sáng Specular 3D
 * - Icon Vector sắc nét cho trạng thái Tiếp theo, Đang lưu, Ngôi sao và Mở khóa
 */
@Composable
fun TactileLevelButton(
    levelNumber: Int,
    size: Int,
    isUnlocked: Boolean,
    isCleared: Boolean,
    starsEarned: Int,
    reqStars: Int,
    currentStars: Int,
    isSaved: Boolean,
    isCurrentTarget: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorSpec = StudioMapPalette.forLevel(levelNumber)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_target_$levelNumber")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isCurrentTarget) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = if (isCurrentTarget) 1.0f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val targetWidth = if (isCurrentTarget) 104.dp else 94.dp
    val targetHeight = if (isCurrentTarget) 114.dp else 104.dp
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .size(width = targetWidth, height = targetHeight)
            .scale(if (isCurrentTarget) pulseScale else 1f)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = if (isUnlocked) {
                        listOf(
                            colorSpec.surfaceTop,
                            colorSpec.surfaceBottom
                        )
                    } else {
                        listOf(
                            Color(0xFF152036),
                            Color(0xFF0F172A)
                        )
                    }
                )
            )
            .border(
                width = if (isCurrentTarget) 2.5.dp else if (isUnlocked) 1.5.dp else 1.dp,
                brush = Brush.verticalGradient(
                    colors = when {
                        isCurrentTarget -> listOf(colorSpec.brightGlow, MazeAmber)
                        isSaved -> listOf(colorSpec.brightGlow, colorSpec.primary)
                        isCleared -> listOf(colorSpec.brightGlow.copy(alpha = 0.9f), colorSpec.primary.copy(alpha = 0.65f))
                        isUnlocked -> listOf(colorSpec.primary.copy(alpha = 0.8f), colorSpec.primary.copy(alpha = 0.35f))
                        else -> listOf(Color(0xFF334155), Color(0xFF1E293B))
                    }
                ),
                shape = shape
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_btn_$levelNumber")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Hàng trên: Huy hiệu trạng thái hoặc Sao đánh giá (không bao giờ tràn dòng)
            Row(
                modifier = Modifier.height(18.dp),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentTarget && !isCleared) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(MazeAmber, Color(0xFFFDE047))
                                )
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = "TIẾP THEO",
                            fontSize = 7.sp,
                            lineHeight = 9.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    }
                } else if (isUnlocked && isCleared) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= starsEarned) MazeStar else Color(0x33CBD5E1),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                } else if (isSaved) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colorSpec.primary.copy(alpha = 0.16f))
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = colorSpec.primary,
                            modifier = Modifier.size(9.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "ĐANG CHƠI",
                            fontSize = 7.sp,
                            lineHeight = 9.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorSpec.primary
                        )
                    }
                } else {
                    // Color zone indicator dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colorSpec.primary.copy(alpha = 0.7f))
                    )
                }
            }

            // Số Level sắc nét nằm ở giữa, có lineHeight cố định để không đè lên chữ bên dưới
            Text(
                text = "$levelNumber",
                fontSize = if (isCurrentTarget) 23.sp else 20.sp,
                lineHeight = if (isCurrentTarget) 24.sp else 21.sp,
                maxLines = 1,
                softWrap = false,
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    isCurrentTarget -> colorSpec.primary
                    isUnlocked -> MazeTextH1
                    else -> MazeTextMuted
                }
            )

            // Thông số kích thước + Thuật toán (Màn 1-50: xen kẽ 1,2; Màn 51-100: xen kẽ 2,3) trên đúng 1 dòng
            val algIndex = com.example.core.engine.MazeConfig.algorithmIndexForLevel(levelNumber)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colorSpec.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(4.5.dp)
                        .clip(CircleShape)
                        .background(colorSpec.primary)
                )
                Text(
                    text = "${size}×${size}•TT$algIndex",
                    fontSize = 8.5.sp,
                    lineHeight = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) colorSpec.primary else MazeTextMuted
                )
            }
        }

        // Trạng thái Khóa (Locked State tương phản cao trên nền Studio)
        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD90B1120)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0xFF475569), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Đã khóa",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    if (reqStars > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "$currentStars/$reqStars★",
                            color = Color(0xFFFBBF24),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}
