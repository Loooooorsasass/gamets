package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1

/**
 * Nút chuyển đổi ngôn ngữ độc lập, tách biệt rõ ràng giữa Tiếng Việt (VI) và Tiếng Anh (EN).
 * Thiết kế dạng Segmented Pill sang trọng, hỗ trợ phản hồi trực quan và xúc giác.
 */
@Composable
fun LanguageToggleSwitch(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true
) {
    val shape = RoundedCornerShape(999.dp)

    Box(
        modifier = modifier
            .height(36.dp)
            .clip(shape)
            .background(MazeSurface1)
            .border(1.dp, MazeEdgeHighlight, shape)
            .padding(3.dp)
            .testTag("language_toggle_switch")
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LanguageSegment(
                language = AppLanguage.VI,
                isSelected = currentLanguage == AppLanguage.VI,
                label = if (isCompact) "VI" else "Tiếng Việt",
                flag = "🇻🇳",
                onClick = { onLanguageSelected(AppLanguage.VI) }
            )

            LanguageSegment(
                language = AppLanguage.EN,
                isSelected = currentLanguage == AppLanguage.EN,
                label = if (isCompact) "EN" else "English",
                flag = "🇬🇧",
                onClick = { onLanguageSelected(AppLanguage.EN) }
            )
        }
    }
}

@Composable
private fun LanguageSegment(
    language: AppLanguage,
    isSelected: Boolean,
    label: String,
    flag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MazeAmber.copy(alpha = 0.25f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "lang_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MazeAmber else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "lang_border"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MazeAmber else MazeTextBody,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "lang_text"
    )

    val segmentShape = RoundedCornerShape(999.dp)

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .clip(segmentShape)
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), segmentShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("lang_segment_${language.code}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = flag,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
