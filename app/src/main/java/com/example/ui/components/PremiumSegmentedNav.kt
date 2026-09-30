package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeCyan
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import com.example.ui.viewmodel.AppScreen

data class NavSegmentItem(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun PremiumSegmentedNav(
    selectedScreen: AppScreen,
    language: AppLanguage,
    onSelectScreen: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavSegmentItem(
            screen = AppScreen.DAILY_CHALLENGE,
            label = Strings.tabDaily(language),
            icon = Icons.Default.CalendarToday,
            testTag = "nav_daily"
        ),
        NavSegmentItem(
            screen = AppScreen.LEADERBOARD,
            label = Strings.tabLeaderboard(language),
            icon = Icons.Default.Leaderboard,
            testTag = "nav_leaderboard"
        ),
        NavSegmentItem(
            screen = AppScreen.SHOP,
            label = Strings.tabShop(language),
            icon = Icons.Default.ShoppingBag,
            testTag = "nav_shop"
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MazeSurface1)
            .border(1.dp, MazeEdgeHighlight, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedScreen == item.screen
                val isShopTab = item.screen == AppScreen.SHOP
                val bgColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> Color(0xFF223561)
                        isShopTab -> Color(0xFF2B2011)
                        else -> Color.Transparent
                    },
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "segmentBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = when {
                        isSelected -> MazeTextH1
                        isShopTab -> MazeAmber
                        else -> MazeTextMuted
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "segmentText"
                )
                val iconTint by animateColorAsState(
                    targetValue = when {
                        isSelected -> MazeCyan
                        isShopTab -> MazeAmber
                        else -> MazeTextMuted
                    },
                    animationSpec = tween(durationMillis = 200),
                    label = "segmentIcon"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .let {
                            if (isShopTab && !isSelected) {
                                it.border(1.dp, MazeAmber.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            } else if (isSelected) {
                                it.border(1.dp, MazeCyan.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                            } else it
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelectScreen(item.screen) }
                        )
                        .testTag(item.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.label,
                            color = contentColor,
                            fontSize = 12.5.sp,
                            fontWeight = if (isSelected || isShopTab) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
