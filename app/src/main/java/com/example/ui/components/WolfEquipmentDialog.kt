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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.i18n.AppLanguage
import com.example.data.local.GameProgressEntity
import com.example.data.shop.EquipmentItem
import com.example.data.shop.ShopCatalog
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeEdgeHighlight
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted

/**
 * Giao diện Mở Khóa & Trang Bị Vật Phẩm Hỗ Trợ Màn Sói Đuổi:
 * - Giày Thần Tốc (+15s thời gian)
 * - Khiên Aegis Cổ Đại (+1 Khiên miễn phí mỗi màn Sói Đuổi)
 * - La Bàn Tiên Tri (Bật sẵn Radar hướng đích)
 * Lưu ý: Trang bị chỉ có tác dụng trong màn Sói Đuổi.
 */
@Composable
fun WolfEquipmentDialog(
    progress: GameProgressEntity,
    language: AppLanguage,
    onEquip: (String) -> Unit,
    onUnlock: (String) -> Unit,
    onBuyShield: () -> Unit,
    onDismiss: () -> Unit
) {
    val isVi = language == AppLanguage.VI
    val unlockedSet = remember(progress.unlockedGears) {
        progress.unlockedGears
            .split(",")
            .map { ShopCatalog.normalizeGearId(it) }
            .filter { it.isNotEmpty() }
            .toSet()
    }
    val currentEquippedId = ShopCatalog.normalizeGearId(progress.equippedGearId)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.5.dp, Color(0xFFF97316).copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .testTag("wolf_equipment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF97316).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFF97316), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎒", fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isVi) "TRANG BỊ VẬT PHẨM HỖ TRỢ" else "WOLF CHASE EQUIPMENT",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = MazeTextH1
                            )
                            Text(
                                text = if (isVi) "🐺 Trang bị có độ bền 4–5 ván Sói Đuổi • Đồ 1 lần mất ngay khi dùng" else "🐺 Gear lasts 4–5 Wolf Chase games • Single-use items consumed on use",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFB923C)
                            )
                        }
                    }

                    // Số dư Xu & Khiên
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MazeSurface2,
                        border = BorderStroke(1.dp, MazeAmber.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🪙${progress.coins}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "🛡️${progress.shieldCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Danh sách 3 Trang Bị Hỗ Trợ Màn Sói Đuổi (Có độ bền 4-5 ván)
                ShopCatalog.EQUIPMENTS.forEach { gear: EquipmentItem ->
                    val durability = progress.getGearDurability(gear.id)
                    val isUnlocked = unlockedSet.contains(gear.id) && durability > 0
                    val isEquipped = currentEquippedId == gear.id && isUnlocked

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) Color(0xFF064E3B).copy(alpha = 0.45f) else MazeSurface1
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .border(
                                width = if (isEquipped) 2.dp else 1.dp,
                                color = if (isEquipped) Color(0xFF10B981) else MazeEdgeHighlight,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(gear.accentColor.copy(alpha = 0.2f))
                                        .border(1.dp, gear.accentColor, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = gear.iconEmoji, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = gear.getName(language),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = MazeTextH1
                                        )
                                        if (isEquipped) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = if (isVi) "ĐANG DÙNG" else "EQUIPPED",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF34D399),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = gear.getDesc(language),
                                        fontSize = 11.5.sp,
                                        color = MazeTextBody,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isUnlocked) gear.accentColor.copy(alpha = 0.15f) else Color(0xFF334155).copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = if (isUnlocked) {
                                                if (isVi) "🔧 Độ bền còn lại: $durability/${gear.maxDurability} ván Sói Đuổi"
                                                else "🔧 Durability left: $durability/${gear.maxDurability} Wolf games"
                                            } else {
                                                if (isVi) "🔧 Độ bền tối đa: ${gear.maxDurability} ván Sói Đuổi (Hỏng sau ${gear.maxDurability} ván)"
                                                else "🔧 Max durability: ${gear.maxDurability} Wolf games (Breaks after ${gear.maxDurability} games)"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) gear.accentColor else Color(0xFFFBBF24),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isUnlocked) {
                                Button(
                                    onClick = { onEquip(gear.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isEquipped) Color(0xFF10B981) else gear.accentColor,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("equip_gear_${gear.id}")
                                ) {
                                    Text(
                                        text = if (isEquipped) {
                                            if (isVi) "✓ Đang Trang Bị ($durability/${gear.maxDurability} ván) - Nhấn để tháo" else "✓ Equipped ($durability/${gear.maxDurability}) - Tap to unequip"
                                        } else {
                                            if (isVi) "Trang Bị Cho Màn Sói Đuổi ($durability/${gear.maxDurability} ván)" else "Equip for Wolf Chase ($durability/${gear.maxDurability})"
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                Button(
                                    onClick = { onUnlock(gear.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MazeAmber,
                                        contentColor = Color(0xFF0F172A)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(38.dp)
                                        .testTag("unlock_gear_${gear.id}")
                                ) {
                                    Text(
                                        text = if (isVi) "🔓 Mua Trang Bị (${gear.maxDurability} ván • 🪙 ${gear.priceCoins} Xu)" else "🔓 Buy Gear (${gear.maxDurability} games • 🪙 ${gear.priceCoins} Coins)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mua Khiên Dùng 1 Lần (Mất ngay sau khi kích hoạt)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MazeSurface2,
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isVi) "🛡️ Khiên Dùng 1 Lần: ${progress.shieldCount}" else "🛡️ Single-Use Shields: ${progress.shieldCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = if (isVi) "Đồ dùng 1 lần: Mất ngay sau khi kích hoạt hoặc đỡ 1 đòn Sói" else "Single-use item: Consumed immediately upon activation or blocking Wolf",
                                fontSize = 10.5.sp,
                                color = MazeTextBody
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onBuyShield,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF38BDF8),
                                contentColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(
                                text = if (isVi) "+1 Khiên (🪙250)" else "+1 Shield (🪙250)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (isVi) "Đóng" else "Close",
                        fontWeight = FontWeight.Bold,
                        color = MazeAmber
                    )
                }
            }
        }
    }
}
