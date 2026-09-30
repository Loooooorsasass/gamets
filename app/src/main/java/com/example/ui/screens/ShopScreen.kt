package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.time.NetworkTimeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ads.AdConstants
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.Strings
import com.example.core.i18n.VietnameseTexts
import com.example.data.shop.EquipmentItem
import com.example.data.shop.GameItemAssets
import com.example.data.shop.GameItemSkin
import com.example.data.shop.MazeTheme
import com.example.data.shop.PlayerSkin
import com.example.data.shop.ShopCatalog
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.theme.MazeAccent
import com.example.ui.theme.MazeAmber
import com.example.ui.theme.MazeSurface1
import com.example.ui.theme.MazeSurface2
import com.example.ui.theme.MazeTextBody
import com.example.ui.theme.MazeTextH1
import com.example.ui.theme.MazeTextMuted
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ShopScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = uiState.language
    val progress = uiState.progress
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Skins/Nhân Vật, 1: Chủ Đề Mê Cung, 2: VIP & Xu

    val unlockedSkins = remember(progress.unlockedSkins) {
        progress.unlockedSkins.split(",").map { it.trim() }.toSet()
    }
    val unlockedThemes = remember(progress.unlockedThemes) {
        progress.unlockedThemes.split(",").map { it.trim() }.toSet()
    }
    val unlockedGears = remember(progress.unlockedGears) {
        progress.unlockedGears.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    var previewSkinId by remember(progress.currentSkinId) { mutableStateOf(progress.currentSkinId) }
    val previewSkin = remember(previewSkinId, lang) {
        ShopCatalog.getSkinById(previewSkinId, lang)
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.HOME)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier.testTag("shop_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = Strings.back(lang),
                        tint = MazeTextH1
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = Strings.shopTitle(lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MazeTextH1
                    )
                    Text(
                        text = if (lang == AppLanguage.VI) VietnameseTexts.SHOP_PREVIEW_SUB else "Equipped characters replace player in games",
                        fontSize = 11.sp,
                        color = MazeTextMuted
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Switcher
                LanguageToggleSwitch(
                    currentLanguage = lang,
                    onLanguageSelected = { viewModel.setLanguage(it) },
                    isCompact = true
                )

                // Coin & Shield balance badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(MazeSurface2)
                        .border(1.dp, MazeAmber.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🪙${progress.coins}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = MazeAmber
                    )
                    Text(
                        text = "🛡️${progress.shieldCount}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFF38BDF8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TAB ROW
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MazeSurface2,
            contentColor = MazeAmber,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(Strings.tabSkins(lang), fontWeight = FontWeight.Bold, fontSize = 11.5.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(Strings.tabThemes(lang), fontWeight = FontWeight.Bold, fontSize = 11.5.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(Strings.tabVip(lang), fontWeight = FontWeight.Bold, fontSize = 11.5.sp) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text(if (lang == AppLanguage.VI) "🛡️ Trang Bị" else "🛡️ Gear", fontWeight = FontWeight.Bold, fontSize = 11.5.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // =========================================================================
                // TAB 0: SKINS & NHÂN VẬT GAME (Interactive Hero Preview Stage + Grid)
                // =========================================================================
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // SÂN KHẤU XEM TRƯỚC NHÂN VẬT (HERO PREVIEW STAGE)
                        val isEquipped = progress.currentSkinId == previewSkin.id
                        val isUnlocked = unlockedSkins.contains(previewSkin.id) || (previewSkin.isVipOnly && progress.isVipNoAds)

                        HeroCharacterPreviewCard(
                            skin = previewSkin,
                            lang = lang,
                            isEquipped = isEquipped,
                            isUnlocked = isUnlocked,
                            onEquip = { viewModel.equipSkin(previewSkin.id) },
                            onBuy = { viewModel.buySkin(previewSkin.id) },
                            onBuyVip = { viewModel.buyVipPass() }
                        )
                    }

                    item {
                        Text(
                            text = if (lang == AppLanguage.VI) "DANH SÁCH VẬT PHẨM NHÂN VẬT" else "ALL CHARACTER SKINS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MazeTextH1,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    // Danh sách toàn bộ Skins
                    val skins = ShopCatalog.getSkins(lang)
                    items(skins) { skin ->
                        val isUnlocked = unlockedSkins.contains(skin.id) || (skin.isVipOnly && progress.isVipNoAds)
                        val isEquipped = progress.currentSkinId == skin.id
                        val isSelectedForPreview = previewSkinId == skin.id

                        SkinItemCard(
                            skin = skin,
                            language = lang,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            isSelectedForPreview = isSelectedForPreview,
                            onSelect = { previewSkinId = skin.id },
                            onEquip = {
                                previewSkinId = skin.id
                                viewModel.equipSkin(skin.id)
                            },
                            onBuy = {
                                previewSkinId = skin.id
                                viewModel.buySkin(skin.id)
                            }
                        )
                    }
                }
            }

            1 -> {
                // =========================================================================
                // TAB 1: CHỦ ĐỀ MÊ CUNG (THEMES - 10 Bản Đồ Màu Tương Phản Cao)
                // =========================================================================
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MazeSurface1,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.7f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (lang == AppLanguage.VI) "🎨 BỘ SƯU TẬP 10 BẢN ĐỒ MÀU STUDIO" else "🎨 10 STUDIO COLOR MAPS COLLECTION",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFBBF24)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (lang == AppLanguage.VI) "Tương phản cao chuẩn Esports • Dùng Gold (Xu) để mở khóa 10 bản đồ màu sắc rực rỡ!" else "High-contrast Esports palette • Spend Gold (Coins) to unlock 10 vibrant color maps!",
                                        fontSize = 11.sp,
                                        color = MazeTextBody
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    onClick = { selectedTab = 2 },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF59E0B)
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.VI) "👑 NẠP GOLD / VIP" else "👑 VIP / GOLD",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(ShopCatalog.THEMES) { theme ->
                        val isUnlocked = unlockedThemes.contains(theme.id) || theme.priceCoins == 0
                        val isEquipped = progress.currentMazeThemeId == theme.id

                        ThemeItemCard(
                            theme = theme,
                            language = lang,
                            isUnlocked = isUnlocked,
                            isEquipped = isEquipped,
                            onEquip = { viewModel.equipTheme(theme.id) },
                            onBuy = { viewModel.buyTheme(theme.id) }
                        )
                    }
                }
            }

            3 -> {
                // =========================================================================
                // TAB 3: ĐỒ DÙNG 1 LẦN & TRANG BỊ SÓI ĐUỔI (HỎNG SAU 4-5 VÁN)
                // =========================================================================
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "⚡", fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (lang == AppLanguage.VI) "VẬT PHẨM DÙNG 1 LẦN (MẤT NGAY KHI KÍCH HOẠT)" else "SINGLE-USE ITEMS (CONSUMED ON ACTIVATION)",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Text(
                                                text = if (lang == AppLanguage.VI) {
                                                    "🛡️ Khiên: ${progress.shieldCount} • 💡 Gợi ý: ${progress.hintCount} • ⏭️ Bỏ qua: ${progress.skipTokens}"
                                                } else {
                                                    "🛡️ Shields: ${progress.shieldCount} • 💡 Hints: ${progress.hintCount} • ⏭️ Skips: ${progress.skipTokens}"
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MazeTextH1
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (lang == AppLanguage.VI) {
                                        "• Đồ dùng 1 lần sẽ mất ngay lập tức sau khi kích hoạt sử dụng trong ván chơi.\n" +
                                        "• Khiên Hộ Mệnh bảo vệ bạn 1 lần khi chạm Sói và làm choáng Sói 2.2 giây!"
                                    } else {
                                        "• Single-use items are consumed immediately upon activation in a match.\n" +
                                        "• Wolf Shield protects you once when touching the Wolf and stuns it for 2.2s!"
                                    },
                                    fontSize = 11.5.sp,
                                    color = MazeTextBody,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.buyShieldPack(useKeys = false) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8), contentColor = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (lang == AppLanguage.VI) "+1 Khiên (🪙250)" else "+1 Shield (🪙250)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.buySingleUseHint() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (lang == AppLanguage.VI) "+1 Gợi Ý (🪙150)" else "+1 Hint (🪙150)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { viewModel.buySingleUseSkip() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (lang == AppLanguage.VI) "+1 Vé Qua Màn Dùng 1 Lần (🪙300 Xu)" else "+1 Single-Use Skip Token (🪙300 Coins)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (lang == AppLanguage.VI) "🐺 TRANG BỊ SÓI ĐUỔI (HỎNG SAU 4–5 VÁN SÓI ĐUỔI)" else "🐺 WOLF CHASE EQUIPMENT (BREAKS AFTER 4–5 WOLF GAMES)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MazeAmber,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                        )
                    }

                    items(ShopCatalog.EQUIPMENTS) { gear: EquipmentItem ->
                        val normalizedEquipped = ShopCatalog.normalizeGearId(progress.equippedGearId)
                        val durability = progress.getGearDurability(gear.id)
                        val isUnlocked = unlockedGears.map { ShopCatalog.normalizeGearId(it) }.contains(gear.id) && durability > 0
                        val isEquipped = normalizedEquipped == gear.id && isUnlocked

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isEquipped) 2.dp else 1.dp,
                                    color = if (isEquipped) gear.accentColor else MazeSurface2,
                                    shape = RoundedCornerShape(14.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
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
                                        Column {
                                            Text(
                                                text = gear.getName(lang),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = MazeTextH1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = gear.getDesc(lang),
                                                fontSize = 11.5.sp,
                                                color = MazeTextBody
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (isUnlocked) {
                                                    if (lang == AppLanguage.VI) "🔧 Độ bền còn lại: $durability/${gear.maxDurability} ván Sói Đuổi"
                                                    else "🔧 Durability left: $durability/${gear.maxDurability} Wolf games"
                                                } else {
                                                    if (lang == AppLanguage.VI) "🔧 Độ bền: ${gear.maxDurability} ván Sói Đuổi"
                                                    else "🔧 Durability: ${gear.maxDurability} Wolf games"
                                                },
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUnlocked) gear.accentColor else Color(0xFFFBBF24)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (isUnlocked) {
                                    Button(
                                        onClick = { viewModel.equipEquipment(gear.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isEquipped) Color(0xFF10B981) else gear.accentColor,
                                            contentColor = Color(0xFF0F172A)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (isEquipped) {
                                                if (lang == AppLanguage.VI) "✓ Đang Trang Bị ($durability/${gear.maxDurability} ván) - Nhấn để tháo" else "✓ Equipped ($durability/${gear.maxDurability}) - Tap to unequip"
                                            } else {
                                                if (lang == AppLanguage.VI) "Trang Bị Cho Màn Sói Đuổi ($durability/${gear.maxDurability} ván)" else "Equip for Wolf Chase ($durability/${gear.maxDurability})"
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.buyEquipment(gear.id, useKeys = false) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MazeAmber,
                                            contentColor = Color(0xFF0F172A)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = if (lang == AppLanguage.VI) "🔓 Mua Trang Bị (${gear.maxDurability} ván • 🪙 ${gear.priceCoins} Xu)" else "🔓 Buy Gear (${gear.maxDurability} games • 🪙 ${gear.priceCoins} Coins)",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // =========================================================================
                // TAB 2: HỆ THỐNG GÓI VIP & QUẢNG CÁO TÍCH LŨY
                // =========================================================================
                val context = LocalContext.current
                val activity = context as? Activity
                val networkTimeManager = remember { NetworkTimeManager.getInstance(context) }
                val isInternetSynced by networkTimeManager.isInternetSyncedFlow.collectAsStateWithLifecycle()

                var currentClockTime by remember { mutableLongStateOf(networkTimeManager.getCurrentInternetTimeMillis()) }

                // Bộ đếm ngược thời gian thực cập nhật mỗi giây (1000ms)
                LaunchedEffect(Unit) {
                    while (isActive) {
                        currentClockTime = networkTimeManager.getCurrentInternetTimeMillis()
                        viewModel.checkAndExpireVipIfNeeded()
                        delay(1000L)
                    }
                }

                val isFlashSale = networkTimeManager.isFlashSaleActive()
                val flashSaleRemainingMillis = networkTimeManager.getFlashSaleRemainingMillis()
                val flashSaleCountdownStr = remember(currentClockTime) {
                    networkTimeManager.formatRemainingDuration(flashSaleRemainingMillis)
                }
                val timeUntilNextFlashSale = remember(currentClockTime) {
                    networkTimeManager.formatRemainingDuration(networkTimeManager.getMillisUntilNextFlashSale())
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. THẺ TRẠNG THÁI VIP HIỆN TẠI & TÙY CHỌN (Luôn lấy cấp độ VIP cao nhất người chơi sở hữu)
                    item {
                        val highestVipTier = progress.highestOwnedVipTier(currentClockTime)
                        val isVipActive = highestVipTier > 0

                        val remainingVipMillis = if (highestVipTier == 3) Long.MAX_VALUE
                            else if (isVipActive) maxOf(0L, progress.vipExpiresAtMillis - currentClockTime)
                            else 0L

                        val vipTitle = when (highestVipTier) {
                            3 -> if (lang == AppLanguage.VI) "VIP 3 - TRỌN ĐỜI" else "VIP 3 - LIFETIME"
                            2 -> if (lang == AppLanguage.VI) "VIP 2 (15 NGÀY)" else "VIP 2 (15 DAYS)"
                            1 -> if (lang == AppLanguage.VI) "VIP 1 (3 NGÀY)" else "VIP 1 (3 DAYS)"
                            else -> if (lang == AppLanguage.VI) "CHƯA KÍCH HOẠT VIP" else "NO VIP ACTIVE"
                        }

                        val remainingDurationStr = remember(currentClockTime, progress.vipExpiresAtMillis, highestVipTier, lang) {
                            if (highestVipTier == 3) {
                                if (lang == AppLanguage.VI) "Vĩnh viễn (Trọn đời)" else "Permanent Lifetime"
                            } else if (isVipActive) {
                                Strings.vipRemainingPrefix(lang) + networkTimeManager.formatRemainingDuration(remainingVipMillis)
                            } else {
                                if (lang == AppLanguage.VI) "Hết hạn hoặc chưa kích hoạt" else "Inactive / Expired"
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    2.dp,
                                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFF59E0B))),
                                    RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = if (isVipActive) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = vipTitle,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 17.sp,
                                                color = if (isVipActive) Color(0xFFFFD700) else MazeTextH1
                                            )
                                            Text(
                                                text = remainingDurationStr,
                                                fontSize = 12.sp,
                                                fontWeight = if (isVipActive) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isVipActive) Color(0xFF34D399) else MazeTextBody
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isVipActive) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF334155),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isVipActive) Color(0xFF10B981) else Color(0xFF475569)
                                        )
                                    ) {
                                        Text(
                                            text = if (isVipActive) Strings.vipStatusActive(lang) else Strings.vipStatusInactive(lang),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isVipActive) Color(0xFF34D399) else Color(0xFF94A3B8),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isInternetSynced) Strings.clockSynced(lang) else Strings.clockDevice(lang),
                                        fontSize = 10.sp,
                                        color = if (isInternetSynced) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                    )
                                }

                                // TÙY CHỌN DÀNH CHO VIP 1: Tắt Banner hoặc Quảng cáo sau game
                                if (progress.vipTier == 1 && isVipActive) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MazeSurface2,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = Strings.vip1OptionsTitle(lang),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFD700)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = Strings.vip1DisableBanner(lang),
                                                    fontSize = 12.sp,
                                                    color = MazeTextH1
                                                )
                                                Switch(
                                                    checked = progress.vip1DisableBanner,
                                                    onCheckedChange = { viewModel.toggleVip1Banner(it) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color(0xFFFFD700),
                                                        checkedTrackColor = Color(0xFF78350F)
                                                    )
                                                )
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = Strings.vip1DisableInter(lang),
                                                    fontSize = 12.sp,
                                                    color = MazeTextH1
                                                )
                                                Switch(
                                                    checked = progress.vip1DisableInterstitial,
                                                    onCheckedChange = { viewModel.toggleVip1Interstitial(it) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color(0xFFFFD700),
                                                        checkedTrackColor = Color(0xFF78350F)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. THẺ TÍCH LŨY QUẢNG CÁO NHẬN THƯỞNG (30 LƯỢT = KÍCH HOẠT VIP 1)
                    item {
                        val adViews = progress.accumulatedRewardedAdViews
                        val targetViews = AdConstants.VIP1_REQUIRED_AD_VIEWS
                        val progressFraction = minOf(1f, adViews.toFloat() / targetViews.toFloat())

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircleFilled,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Strings.adViewsCount(lang, adViews, targetViews),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MazeTextH1
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Strings.adViewsDesc(lang),
                                    fontSize = 11.sp,
                                    color = MazeTextBody,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(999.dp)),
                                    color = Color(0xFF38BDF8),
                                    trackColor = Color(0xFF1E293B)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Nút xem video nhận +50 xu và +1 tích lũy
                                    Button(
                                        onClick = { viewModel.triggerRewardedAd(activity, "COINS") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Text("+50 🪙 (+1)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    // Nút nhận thưởng VIP 1 khi đủ 30 lượt
                                    if (adViews >= targetViews) {
                                        Button(
                                            onClick = { viewModel.activateVip1WithAdViews() },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.weight(1f).height(44.dp)
                                        ) {
                                            Text(Strings.claimVip1Btn(lang), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. BANNER FLASH SALE GIỜ VÀNG
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFlashSale) Color(0xFF451A03) else Color(0xFF1E1E2E)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isFlashSale) 2.dp else 1.dp,
                                    brush = Brush.horizontalGradient(
                                        if (isFlashSale)
                                            listOf(Color(0xFFEF4444), Color(0xFFF59E0B))
                                        else
                                            listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = if (isFlashSale) Color(0xFFF59E0B) else Color(0xFFA5B4FC),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = Strings.flashSaleTitle(lang, isFlashSale),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = if (isFlashSale) Color(0xFFFCD34D) else Color(0xFFC7D2FE)
                                        )
                                        if (isFlashSale) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFDC2626)
                                            ) {
                                                Text(
                                                    text = Strings.flashSaleRemainingPrefix(lang) + flashSaleCountdownStr,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isFlashSale)
                                            Strings.flashSaleDescActive(lang)
                                        else
                                            Strings.flashSaleDescUpcoming(lang, timeUntilNextFlashSale),
                                        fontSize = 11.sp,
                                        color = MazeTextBody
                                    )
                                }
                            }
                        }
                    }

                    // 4. GÓI VIP 1 (3 NGÀY)
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = Strings.vip1PkgTitle(lang),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFFFD700)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = Strings.vip1PkgBadge(lang),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF59E0B),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Strings.vip1PkgDesc(lang),
                                    fontSize = 11.sp,
                                    color = MazeTextBody,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.activateVip1WithCoins() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Text(Strings.vip1CoinsBtn(lang), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.activateVip1WithAdViews() },
                                        shape = RoundedCornerShape(12.dp),
                                        enabled = progress.accumulatedRewardedAdViews >= AdConstants.VIP1_REQUIRED_AD_VIEWS,
                                        modifier = Modifier.weight(1f).height(44.dp)
                                    ) {
                                        Text(
                                            Strings.vip1AdExchangeBtn(lang, progress.accumulatedRewardedAdViews),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. GÓI VIP 2 (15 NGÀY)
                    item {
                        val vip2Price = viewModel.billingManager.getFormattedPrice(2)
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isFlashSale) 2.dp else 1.dp,
                                    color = if (isFlashSale) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = Strings.vip2PkgTitle(lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                        if (isFlashSale) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFEF4444)
                                            ) {
                                                Text(
                                                    text = Strings.flashSaleOff(lang),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (isFlashSale) {
                                            Text(
                                                text = "$1.25",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                )
                                            )
                                        }
                                        Text(
                                            text = vip2Price,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isFlashSale) Color(0xFFFBBF24) else Color(0xFF38BDF8)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Strings.vip2PkgDesc(lang),
                                    fontSize = 11.sp,
                                    color = MazeTextBody,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.purchaseVipRealMoney(activity, 2) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFlashSale) Color(0xFFDC2626) else Color(0xFF0284C7)
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("buy_vip_2_btn")
                                ) {
                                    Text(Strings.vip2BuyBtn(lang, vip2Price), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // 6. GÓI VIP 3 (TRỌN ĐỜI - VĨNH VIỄN)
                    item {
                        val vip3Price = viewModel.billingManager.getFormattedPrice(3)
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MazeSurface1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    2.dp,
                                    Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFA855F7), Color(0xFFEC4899))),
                                    RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = Strings.vip3PkgTitle(lang),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFFFD700)
                                        )
                                        if (isFlashSale) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFEF4444)
                                            ) {
                                                Text(
                                                    text = Strings.flashSaleTag(lang),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        if (isFlashSale) {
                                            Text(
                                                text = "$4.00",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                style = androidx.compose.ui.text.TextStyle(
                                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                                )
                                            )
                                        }
                                        Text(
                                            text = vip3Price,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFFFD700)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = Strings.vip3PkgDesc(lang),
                                    fontSize = 11.sp,
                                    color = MazeTextBody,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { viewModel.purchaseVipRealMoney(activity, 3) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("buy_vip_3_btn")
                                ) {
                                    Text(Strings.vip3BuyBtn(lang, vip3Price), fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * HERO CHARACTER PREVIEW CARD (Phòng Thử Nhân Vật Sắc Nét)
 */
@Composable
private fun HeroCharacterPreviewCard(
    skin: PlayerSkin,
    lang: AppLanguage,
    isEquipped: Boolean,
    isUnlocked: Boolean,
    onEquip: () -> Unit,
    onBuy: () -> Unit,
    onBuyVip: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim_${skin.id}")
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_aura"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MazeSurface1),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        skin.glowColor.copy(alpha = 0.8f),
                        Color(0xFF334155)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sân khấu nhân vật gọn gàng (không vẽ hào quang xung quanh)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MazeSurface2),
                contentAlignment = Alignment.Center
            ) {
                // Vẽ nhân vật trực tiếp trên Canvas với GameItemAssets
                Canvas(modifier = Modifier.size(72.dp)) {
                    val canvasNative = drawContext.canvas.nativeCanvas
                    GameItemAssets.drawPlayerOnCanvas(
                        canvas = canvasNative,
                        cx = size.width / 2f,
                        cy = size.height / 2f,
                        cellSize = size.width,
                        skinId = skin.id
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tên và huy hiệu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = skin.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MazeTextH1
                )
                if (skin.isVipOnly) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFD700),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "👑 VIP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = skin.description,
                fontSize = 12.sp,
                color = MazeTextBody,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Nút bấm hành động cho nhân vật đang xem
            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.equippedBadge(lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            } else if (isUnlocked) {
                Button(
                    onClick = onEquip,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("equip_hero_btn")
                ) {
                    Text(
                        text = Strings.equipItem(lang),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (skin.isVipOnly) {
                Button(
                    onClick = onBuyVip,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(
                        text = if (lang == AppLanguage.VI) VietnameseTexts.SHOP_VIP_UNLOCK_BTN else "Unlock VIP Pass",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onBuy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("buy_hero_btn")
                ) {
                    Text(
                        text = Strings.buyItem(lang, skin.priceCoins),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * THẺ VẬT PHẨM NHÂN VẬT (Skin Item Card)
 */
@Composable
private fun SkinItemCard(
    skin: PlayerSkin,
    language: AppLanguage,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    isSelectedForPreview: Boolean,
    onSelect: () -> Unit,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelectedForPreview) MazeSurface2 else MazeSurface1),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isEquipped) 2.dp else if (isSelectedForPreview) 1.5.dp else 1.dp,
                color = if (isEquipped) Color(0xFF10B981) else if (isSelectedForPreview) MazeAmber else Color(0xFF334155),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onSelect)
            .testTag("skin_item_${skin.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Mini preview avatar
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(skin.glowColor.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val canvasNative = drawContext.canvas.nativeCanvas
                        GameItemAssets.drawPlayerOnCanvas(
                            canvas = canvasNative,
                            cx = size.width / 2f,
                            cy = size.height / 2f,
                            cellSize = size.width,
                            skinId = skin.id
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = skin.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MazeTextH1
                        )
                        if (skin.isVipOnly) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "👑 VIP", fontSize = 10.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = skin.description,
                        fontSize = 11.sp,
                        color = MazeTextBody,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(Strings.equippedBadge(language), fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isUnlocked) {
                OutlinedButton(
                    onClick = onEquip,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(Strings.equipItem(language), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else if (skin.isVipOnly) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
                ) {
                    Text("VIP 👑", fontSize = 11.sp, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp))
                }
            } else {
                Button(
                    onClick = onBuy,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A))
                ) {
                    Text("${skin.priceCoins} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * THẺ CHỦ ĐỀ MÊ CUNG (Theme Item Card)
 */
@Composable
private fun ThemeItemCard(
    theme: MazeTheme,
    language: AppLanguage,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        theme.bgColor,
                        theme.panelColor,
                        MazeSurface1
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .border(
                width = if (isEquipped) 2.dp else 1.2.dp,
                brush = Brush.linearGradient(
                    colors = if (isEquipped) {
                        listOf(Color.White, theme.accentColor, theme.wallColor)
                    } else {
                        listOf(theme.wallColor.copy(alpha = 0.65f), theme.accentColor.copy(alpha = 0.25f))
                    }
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable {
                if (isUnlocked) onEquip() else onBuy()
            }
            .testTag("theme_item_${theme.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Crisp Maze Swatch Preview (No side glow)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.bgColor)
                        .border(2.dp, theme.wallColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(32.dp)) {
                        val stroke = 3.dp.toPx()
                        // Mini maze wall lines
                        drawLine(
                            color = theme.wallColor,
                            start = Offset(4f, 6f),
                            end = Offset(size.width - 4f, 6f),
                            strokeWidth = stroke
                        )
                        drawLine(
                            color = theme.wallColor,
                            start = Offset(6f, 6f),
                            end = Offset(6f, size.height - 6f),
                            strokeWidth = stroke
                        )
                        drawLine(
                            color = theme.wallColor,
                            start = Offset(6f, size.height - 6f),
                            end = Offset(size.width - 10f, size.height - 6f),
                            strokeWidth = stroke
                        )
                        // Crisp path dot
                        drawCircle(
                            color = theme.accentColor,
                            radius = 5.dp.toPx(),
                            center = Offset(size.width * 0.62f, size.height * 0.52f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = theme.getDisplayName(language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.5.sp,
                        color = if (theme.id == "classic_black") MazeTextH1 else Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(theme.wallColor)
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(theme.accentColor)
                        )
                        Text(
                            text = if (theme.priceCoins == 0) {
                                if (language == AppLanguage.VI) "MẶC ĐỊNH • MIỄN PHÍ" else "DEFAULT • FREE"
                            } else {
                                if (language == AppLanguage.VI) "Giá: ${theme.priceCoins} Gold 🪙" else "Price: ${theme.priceCoins} Gold 🪙"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (theme.priceCoins == 0) Color(0xFF94A3B8) else MazeAmber
                        )
                    }
                }
            }

            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.22f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.equippedBadge(language), fontSize = 11.sp, color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold)
                    }
                }
            } else if (isUnlocked) {
                Button(
                    onClick = onEquip,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.wallColor,
                        contentColor = Color(0xFF0A0F1D)
                    )
                ) {
                    Text(Strings.equipItem(language), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            } else {
                Button(
                    onClick = onBuy,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MazeAmber, contentColor = Color(0xFF0F172A))
                ) {
                    Text("${theme.priceCoins} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
