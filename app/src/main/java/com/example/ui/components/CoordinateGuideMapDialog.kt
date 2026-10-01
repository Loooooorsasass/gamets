package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.GoalDirectionHint
import com.example.core.engine.MazeConfig
import com.example.core.engine.Point
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeCyan
import com.example.ui.theme.MazeDanger
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted

/**
 * Coordinate & Direction Guide Map Modal (AA00 / Streamer / Impossible HUD support)
 */
@Composable
fun CoordinateGuideMapDialog(
    player: Point,
    goal: Point,
    wolf: Point?,
    goalHint: GoalDirectionHint?,
    isImpossible: Boolean,
    mazeW: Int,
    mazeH: Int,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val isVi = language == AppLanguage.VI
    val playerCoord = MazeConfig.encodeCoord(player)
    val goalCoord = MazeConfig.encodeCoord(goal)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MazeSurface1,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isVi) "🗺️ Bản Đồ & Tọa Độ (AA00)" else "🗺️ Coordinate Map Guide",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MazeTextH1
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isVi)
                        "Hệ tọa độ chữ cái (AA00, AB01...) giúp định hướng chính xác trong các mê cung lớn và chế độ Khổng Lồ."
                    else
                        "The alphanumeric coordinate system helps pro players navigate large and impossible mazes.",
                    fontSize = 12.5.sp,
                    color = MazeTextBody
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MazeSurface2,
                    border = BorderStroke(1.dp, MazeEdgeHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = MazeAmber, modifier = Modifier.size(16.dp))
                                Text(if (isVi) "Vị trí của bạn" else "Player Position", fontSize = 12.sp, color = MazeTextBody)
                            }
                            Text(playerCoord, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MazeAmber)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = MazeDanger, modifier = Modifier.size(16.dp))
                                Text(if (isVi) "Vị trí Đích" else "Goal Position", fontSize = 12.sp, color = MazeTextBody)
                            }
                            Text(goalCoord, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MazeDanger)
                        }

                        if (wolf != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isVi) "🐺 Sói Truy Đuổi" else "🐺 Wolf Position", fontSize = 12.sp, color = MazeTextBody)
                                Text(MazeConfig.encodeCoord(wolf), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF97316))
                            }
                        }
                    }
                }

                Text(
                    text = "${if (isVi) "Kích thước mê cung" else "Maze dimensions"}: $mazeW × $mazeH",
                    fontSize = 11.sp,
                    color = MazeTextMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF090B16)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(Strings.understood(language), fontWeight = FontWeight.Bold)
            }
        }
    )
}
