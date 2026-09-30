package com.example.ui.components

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeAmberGlow
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextMuted
import com.example.ui.theme.StudioMapPalette

/**
 * Nút tầng Impossible & Siêu cấp chuẩn Studio:
 * - Ánh sáng Spotlight tâm nút + Viền Specular bắt sáng
 * - Sắc màu phân tầng theo bảng màu StudioMapPalette
 */
@Composable
fun TactileImpossibleTierButton(
    title: String,
    subtitle: String,
    isUnlocked: Boolean,
    isCleared: Boolean,
    isSaved: Boolean,
    isSuper: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorIndex = if (isSuper) 2 else (title.hashCode().let { if (it < 0) -it else it } % StudioMapPalette.COLORS.size)
    val spec = StudioMapPalette.forIndex(colorIndex)
    val accentColor = if (isSuper) MazeAmberGlow else spec.brightGlow
    val shape = RoundedCornerShape(15.dp)

    Box(
        modifier = modifier
            .size(width = 114.dp, height = 100.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (isSuper) {
                        listOf(Color(0xFF2B1F0D), Color(0xFF1C1917))
                    } else {
                        listOf(spec.surfaceTop, spec.surfaceBottom)
                    }
                )
            )
            .border(
                width = if (isSuper || isSaved) 2.dp else 1.5.dp,
                brush = Brush.verticalGradient(
                    when {
                        isSuper -> listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                        isSaved -> listOf(accentColor, MazeAmber)
                        isCleared -> listOf(accentColor, spec.primary)
                        isUnlocked -> listOf(accentColor.copy(alpha = 0.85f), spec.primary.copy(alpha = 0.55f))
                        else -> listOf(Color(0xFF334155), Color(0xFF1E293B))
                    }
                ),
                shape = shape
            )
            .clickable(enabled = isUnlocked, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = if (isSuper) Icons.Default.WorkspacePremium else Icons.Default.Bolt,
                contentDescription = null,
                tint = if (isUnlocked) spec.primary else MazeTextMuted,
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = title,
                fontSize = if (isSuper) 13.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isUnlocked) Color(0xFFF8FAFC) else MazeTextMuted,
                textAlign = TextAlign.Center
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlocked) spec.brightGlow else MazeTextMuted,
                textAlign = TextAlign.Center
            )

            if (isCleared) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Đã vượt",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "ĐÃ VƯỢT", fontSize = 8.5.sp, color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold)
                }
            } else if (isSaved) {
                Text(text = "ĐANG CHƠI", fontSize = 8.5.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.ExtraBold)
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Trạng thái Khóa (Locked State trên nền Studio)
        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xD90B1120)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF475569), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Đã khóa",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
