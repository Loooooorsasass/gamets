package com.example.data.shop

import androidx.compose.ui.graphics.Color
import com.example.core.i18n.AppLanguage

data class PlayerSkin(
    val id: String,
    val name: String,
    val description: String,
    val priceCoins: Int,
    val iconEmoji: String,
    val primaryColor: Color,
    val secondaryColor: Color = primaryColor,
    val glowColor: Color,
    val isVipOnly: Boolean = false,
    val avatarType: PlayerAvatarType = PlayerAvatarType.NEON_SPHERE
) {
    companion object {
        fun fromGameItemSkin(item: GameItemSkin, lang: AppLanguage = AppLanguage.VI): PlayerSkin {
            return PlayerSkin(
                id = item.id,
                name = item.getName(lang),
                description = item.getDescription(lang),
                priceCoins = item.priceCoins,
                iconEmoji = item.iconEmoji,
                primaryColor = item.primaryColor,
                secondaryColor = item.secondaryColor,
                glowColor = item.glowColor,
                isVipOnly = item.isVipOnly,
                avatarType = item.avatarType
            )
        }
    }
}

data class MazeTheme(
    val id: String,
    val name: String,
    val nameEn: String,
    val priceCoins: Int,
    val bgColor: Color,
    val panelColor: Color,
    val wallColor: Color,
    val pathVisitedColor: Color,
    val accentColor: Color,
    val goalColor: Color
) {
    fun getDisplayName(lang: AppLanguage): String {
        return if (lang == AppLanguage.VI) name else nameEn
    }
}

data class EquipmentItem(
    val id: String,
    val nameVi: String,
    val nameEn: String,
    val descVi: String,
    val descEn: String,
    val iconEmoji: String,
    val priceCoins: Int,
    val priceKeys: Int,
    val accentColor: Color,
    val maxDurability: Int = 5
) {
    fun getName(lang: AppLanguage): String = if (lang == AppLanguage.VI) nameVi else nameEn
    fun getDesc(lang: AppLanguage): String = if (lang == AppLanguage.VI) descVi else descEn
}

object ShopCatalog {

    const val SINGLE_USE_SHIELD_PRICE_COINS = 280
    const val SINGLE_USE_SHIELD_PRICE_KEYS = 3
    const val SINGLE_USE_HINT_PRICE_COINS = 220
    const val SINGLE_USE_SKIP_PRICE_COINS = 380

    val EQUIPMENTS = listOf(
        EquipmentItem(
            id = "boots_haste",
            nameVi = "Giày Thần Tốc",
            nameEn = "Haste Boots",
            descVi = "+15s thời gian đếm ngược màn Sói Đuổi (Độ bền: 5 ván Sói Đuổi, hỏng sau 5 ván)",
            descEn = "+15s countdown time limit in Wolf Chase (Durability: 5 Wolf Chase games, breaks after 5 runs)",
            iconEmoji = "🥾",
            priceCoins = 450,
            priceKeys = 4,
            accentColor = Color(0xFF38BDF8),
            maxDurability = 5
        ),
        EquipmentItem(
            id = "shield_aegis",
            nameVi = "Khiên Aegis Cổ Đại",
            nameEn = "Ancient Aegis Shield",
            descVi = "Tự động chặn Sói 1 lần mỗi ván Sói Đuổi (Độ bền: 4 ván Sói Đuổi, hỏng sau 4 ván)",
            descEn = "Auto-blocks Wolf once per Wolf Chase run (Durability: 4 Wolf Chase games, breaks after 4 runs)",
            iconEmoji = "🛡️",
            priceCoins = 650,
            priceKeys = 5,
            accentColor = Color(0xFF60A5FA),
            maxDurability = 4
        ),
        EquipmentItem(
            id = "compass_vision",
            nameVi = "La Bàn Tiên Tri",
            nameEn = "Oracle Compass",
            descVi = "Bật sẵn Radar hướng đích & mở rộng tầm nhìn màn Sói Đuổi (Độ bền: 5 ván Sói Đuổi, hỏng sau 5 ván)",
            descEn = "Auto-enables Goal Radar & expands vision in Wolf Chase (Durability: 5 Wolf Chase games, breaks after 5 runs)",
            iconEmoji = "🧭",
            priceCoins = 550,
            priceKeys = 4,
            accentColor = Color(0xFF34D399),
            maxDurability = 5
        )
    )

    fun normalizeGearId(id: String): String {
        return when (id.trim()) {
            "boots_speed" -> "boots_haste"
            "armor_wolf" -> "shield_aegis"
            else -> id.trim()
        }
    }

    fun getEquipmentById(id: String): EquipmentItem? {
        if (id.isBlank()) return null
        val normalized = normalizeGearId(id)
        return EQUIPMENTS.find { it.id == normalized }
    }

    fun getSkins(lang: AppLanguage = AppLanguage.VI): List<PlayerSkin> {
        return GameItemAssets.ALL_SKINS.map { PlayerSkin.fromGameItemSkin(it, lang) }
    }

    val SKINS: List<PlayerSkin>
        get() = getSkins(AppLanguage.VI)

    val THEMES = listOf(
        MazeTheme(
            id = "classic_black",
            name = "0. Studio Đen Tương Phản (Mặc Định)",
            nameEn = "0. Studio High-Contrast Dark (Default)",
            priceCoins = 0,
            bgColor = Color(0xFF0B1120),
            panelColor = Color(0xFF152238),
            wallColor = Color(0xFF38BDF8),
            pathVisitedColor = Color(0x590284C7),
            accentColor = Color(0xFF38BDF8),
            goalColor = Color(0xFF10B981)
        ),
        MazeTheme(
            id = "midnight_cyber",
            name = "1. Lam Ngọc Cyber (Cyan)",
            nameEn = "1. Cyber Cyan",
            priceCoins = 350,
            bgColor = Color(0xFF091326),
            panelColor = Color(0xFF10213D),
            wallColor = Color(0xFF38BDF8),
            pathVisitedColor = Color(0x4D06B6D4),
            accentColor = Color(0xFF22D3EE),
            goalColor = Color(0xFFEF4444)
        ),
        MazeTheme(
            id = "emerald_matrix",
            name = "2. Ngọc Bích Lục Bảo (Emerald)",
            nameEn = "2. Emerald Matrix",
            priceCoins = 450,
            bgColor = Color(0xFF061B15),
            panelColor = Color(0xFF0C2B22),
            wallColor = Color(0xFF34D399),
            pathVisitedColor = Color(0x4D10B981),
            accentColor = Color(0xFF6EE7B7),
            goalColor = Color(0xFFFBBF24)
        ),
        MazeTheme(
            id = "obsidian_gold",
            name = "3. Hoàng Kim Rực Rỡ (Gold)",
            nameEn = "3. Radiant Gold",
            priceCoins = 550,
            bgColor = Color(0xFF1C1408),
            panelColor = Color(0xFF2B1F0D),
            wallColor = Color(0xFFFBBF24),
            pathVisitedColor = Color(0x4DF59E0B),
            accentColor = Color(0xFFFDE047),
            goalColor = Color(0xFFEF4444)
        ),
        MazeTheme(
            id = "sunset_neon",
            name = "4. Tím Tinh Vân (Amethyst)",
            nameEn = "4. Amethyst Nebula",
            priceCoins = 650,
            bgColor = Color(0xFF160D29),
            panelColor = Color(0xFF231540),
            wallColor = Color(0xFFC084FC),
            pathVisitedColor = Color(0x4DA855F7),
            accentColor = Color(0xFFE879F9),
            goalColor = Color(0xFF38BDF8)
        ),
        MazeTheme(
            id = "crimson_ruby",
            name = "5. Hồng Ngọc Hỏa Diệm (Ruby)",
            nameEn = "5. Crimson Ruby",
            priceCoins = 750,
            bgColor = Color(0xFF210A13),
            panelColor = Color(0xFF33101E),
            wallColor = Color(0xFFFB7185),
            pathVisitedColor = Color(0x4DF43F5E),
            accentColor = Color(0xFFFDA4AF),
            goalColor = Color(0xFFFBBF24)
        ),
        MazeTheme(
            id = "cobalt_sapphire",
            name = "6. Lam Đại Dương (Sapphire)",
            nameEn = "6. Ocean Sapphire",
            priceCoins = 850,
            bgColor = Color(0xFF09152E),
            panelColor = Color(0xFF12234A),
            wallColor = Color(0xFF60A5FA),
            pathVisitedColor = Color(0x4D3B82F6),
            accentColor = Color(0xFF93C5FD),
            goalColor = Color(0xFFF97316)
        ),
        MazeTheme(
            id = "coral_sunset",
            name = "7. Cam Hoàng Hôn (Coral)",
            nameEn = "7. Coral Sunset",
            priceCoins = 950,
            bgColor = Color(0xFF211109),
            panelColor = Color(0xFF331B0F),
            wallColor = Color(0xFFFB923C),
            pathVisitedColor = Color(0x4DF97316),
            accentColor = Color(0xFFFDBA74),
            goalColor = Color(0xFF22D3EE)
        ),
        MazeTheme(
            id = "arctic_mint",
            name = "8. Băng Tuyết Bạc Hà (Mint)",
            nameEn = "8. Arctic Mint",
            priceCoins = 1100,
            bgColor = Color(0xFF071D21),
            panelColor = Color(0xFF0E2E33),
            wallColor = Color(0xFF2DD4BF),
            pathVisitedColor = Color(0x4D14B8A6),
            accentColor = Color(0xFF5EEAD4),
            goalColor = Color(0xFFF43F5E)
        ),
        MazeTheme(
            id = "neon_magenta",
            name = "9. Hồng Neon Synth (Magenta)",
            nameEn = "9. Synth Magenta",
            priceCoins = 1300,
            bgColor = Color(0xFF200A21),
            panelColor = Color(0xFF321133),
            wallColor = Color(0xFFF472B6),
            pathVisitedColor = Color(0x4DEC4899),
            accentColor = Color(0xFFF9A8D4),
            goalColor = Color(0xFF34D399)
        ),
        MazeTheme(
            id = "solar_lime",
            name = "10. Lục Quang Năng Lượng (Lime)",
            nameEn = "10. Solar Lime",
            priceCoins = 1600,
            bgColor = Color(0xFF121C08),
            panelColor = Color(0xFF1D2C0D),
            wallColor = Color(0xFFA3E635),
            pathVisitedColor = Color(0x4D84CC16),
            accentColor = Color(0xFFBEF264),
            goalColor = Color(0xFFEF4444)
        )
    )

    fun getSkinById(id: String, lang: AppLanguage = AppLanguage.VI): PlayerSkin {
        val item = GameItemAssets.findSkin(id)
        return PlayerSkin.fromGameItemSkin(item, lang)
    }

    fun getThemeById(id: String): MazeTheme {
        return THEMES.find { it.id == id } ?: THEMES.first()
    }

    fun getThemeByLevel(levelNum: Int): MazeTheme {
        val idx = ((levelNum - 1).coerceAtLeast(0)) % THEMES.size
        return THEMES[idx]
    }
}
