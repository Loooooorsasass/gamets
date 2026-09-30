package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeCyan
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeSurface3
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import kotlin.math.roundToInt

/**
 * Thẻ điều chỉnh độ nhạy di chuyển hiển thị trên màn hình Cài Đặt / Trang Chủ.
 */
@Composable
fun MoveSensitivityCard(
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    lang: AppLanguage,
    modifier: Modifier = Modifier
) {
    val percent = (sensitivity * 100).roundToInt()
    val tierLabel = when {
        percent <= 65 -> Strings.sensitivityPresetSlow(lang)
        percent in 66..115 -> Strings.sensitivityPresetNormal(lang)
        percent in 116..165 -> Strings.sensitivityPresetFast(lang)
        else -> Strings.sensitivityPresetHyper(lang)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MazeSurface1),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(16.dp))
            .testTag("move_sensitivity_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header Row: Title, Speed Icon & Value Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MazeCyan.copy(alpha = 0.15f))
                            .border(1.dp, MazeCyan.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = Strings.sensitivityLabel(lang),
                            tint = MazeCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = Strings.sensitivityLabel(lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MazeTextH1
                        )
                        Text(
                            text = Strings.sensitivityDesc(lang),
                            fontSize = 10.5.sp,
                            color = MazeTextMuted
                        )
                    }
                }

                // Value Pill
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MazeSurface3,
                    border = BorderStroke(1.dp, MazeCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$percent%",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = MazeCyan
                        )
                        Text(
                            text = "($tierLabel)",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = MazeTextBody
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Slider Component
            Slider(
                value = sensitivity.coerceIn(0.5f, 2.5f),
                onValueChange = { onSensitivityChange(it) },
                valueRange = 0.5f..2.5f,
                steps = 19, // bước nhảy 0.1 (10%)
                colors = SliderDefaults.colors(
                    thumbColor = MazeCyan,
                    activeTrackColor = MazeCyan,
                    inactiveTrackColor = MazeSurface3,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("move_sensitivity_slider")
            )

            // Preset Chips Row (50%, 100%, 150%, 200%, 250%)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val presets = listOf(
                    0.5f to if (lang == AppLanguage.VI) "50% Chậm" else "50% Slow",
                    1.0f to if (lang == AppLanguage.VI) "100% Chuẩn" else "100% Default",
                    1.5f to if (lang == AppLanguage.VI) "150% Nhanh" else "150% Fast",
                    2.0f to if (lang == AppLanguage.VI) "200% Siêu Tốc" else "200% Hyper"
                )

                presets.forEach { (presetVal, label) ->
                    val isSelected = kotlin.math.abs(sensitivity - presetVal) < 0.05f
                    Surface(
                        onClick = { onSensitivityChange(presetVal) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MazeCyan.copy(alpha = 0.22f) else MazeSurface2,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MazeCyan else MazeEdgeHighlight
                        ),
                        modifier = Modifier.testTag("sensitivity_preset_${(presetVal * 100).toInt()}")
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) MazeCyan else MazeTextBody,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hộp thoại tùy chỉnh độ nhạy di chuyển trực tiếp trong màn chơi (In-Game Sensitivity Dialog).
 */
@Composable
fun MoveSensitivityDialog(
    sensitivity: Float,
    onSensitivityChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    lang: AppLanguage
) {
    val percent = (sensitivity * 100).roundToInt()
    val tierLabel = when {
        percent <= 65 -> Strings.sensitivityPresetSlow(lang)
        percent in 66..115 -> Strings.sensitivityPresetNormal(lang)
        percent in 116..165 -> Strings.sensitivityPresetFast(lang)
        else -> Strings.sensitivityPresetHyper(lang)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, MazeCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .testTag("in_game_sensitivity_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MazeCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = Strings.sensitivityDialogTitle(lang),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MazeTextH1
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MazeTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Big Percentage Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MazeSurface2,
                    border = BorderStroke(1.dp, MazeCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$percent%",
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp,
                            color = MazeCyan
                        )
                        Text(
                            text = tierLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MazeTextBody
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Slider
                Slider(
                    value = sensitivity.coerceIn(0.5f, 2.5f),
                    onValueChange = { onSensitivityChange(it) },
                    valueRange = 0.5f..2.5f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = MazeCyan,
                        activeTrackColor = MazeCyan,
                        inactiveTrackColor = MazeSurface3,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preset Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val presets = listOf(
                        0.5f to "50%",
                        1.0f to "100%",
                        1.5f to "150%",
                        2.0f to "200%"
                    )

                    presets.forEach { (presetVal, label) ->
                        val isSelected = kotlin.math.abs(sensitivity - presetVal) < 0.05f
                        Surface(
                            onClick = { onSensitivityChange(presetVal) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MazeCyan.copy(alpha = 0.25f) else MazeSurface2,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MazeCyan else MazeEdgeHighlight
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MazeCyan else MazeTextBody,
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Explanation note
                Text(
                    text = Strings.sensitivityDesc(lang),
                    fontSize = 11.sp,
                    color = MazeTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Close / Apply Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MazeCyan,
                        contentColor = Color(0xFF080D1A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("close_sensitivity_dialog_btn")
                ) {
                    Text(
                        text = if (lang == AppLanguage.VI) "Xong" else "Done",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
