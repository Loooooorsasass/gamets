package com.example.data.shop

import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import com.example.core.i18n.AppLanguage
import com.example.core.i18n.VietnameseTexts

/**
 * =========================================================================================
 * 🎨 FILE QUẢN LÝ ICON, HÌNH ẢNH & DANH SÁCH VẬT PHẨM NHÂN VẬT TRONG GAME
 * =========================================================================================
 * 
 * BẠN CÓ THỂ TÙY Ý THAY ĐỔI ICON, HÌNH DÁNG, MÀU SẮC, GIÁ TIỀN CỦA VẬT PHẨM TẠI ĐÂY!
 * Mọi vật phẩm khi được trang bị sẽ THAY THẾ TRỰC TIẾP CHO NHÂN VẬT trong trò chơi.
 * =========================================================================================
 */

enum class PlayerAvatarType {
    NEON_SPHERE,   // Quả cầu Neon phát quang
    CYBER_BOT,     // Robot công nghệ khối lập phương
    GOLDEN_STAR,   // Ngôi sao vàng hoàng kim
    LUCKY_CAT,     // Mèo thần tài dễ thương
    FIRE_DRAGON,   // Ngọn lửa rồng bùng cháy
    ROYAL_CROWN,   // Vương miện hoàng gia quý tộc
    SPACE_ROCKET,  // Phi thuyền không gian
    NINJA_SHADOW,  // Ninja bóng đêm
    DIAMOND_GEM,   // Kim cương tối thượng đa giác
    MAGIC_GHOST,   // Bóng ma tinh nghịch
    CUSTOM_EMOJI   // Tùy biến bằng Emoji bất kỳ
}

data class GameItemSkin(
    val id: String,
    val defaultNameVi: String,
    val defaultNameEn: String,
    val defaultDescVi: String,
    val defaultDescEn: String,
    val priceCoins: Int,
    val iconEmoji: String,
    val avatarType: PlayerAvatarType,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val isVipOnly: Boolean = false,
    val customDrawableResName: String? = null // Tên file ảnh drawable nếu muốn dùng ảnh .png/.xml
) {
    fun getName(lang: AppLanguage): String {
        val vi = when (id) {
            "classic_blue" -> VietnameseTexts.SKIN_CLASSIC_BLUE_NAME
            "cyber_bot" -> VietnameseTexts.SKIN_CYBER_BOT_NAME
            "golden_star" -> VietnameseTexts.SKIN_GOLDEN_STAR_NAME
            "lucky_cat" -> VietnameseTexts.SKIN_LUCKY_CAT_NAME
            "fire_dragon" -> VietnameseTexts.SKIN_FIRE_DRAGON_NAME
            "royal_crown" -> VietnameseTexts.SKIN_ROYAL_CROWN_NAME
            "space_rocket" -> VietnameseTexts.SKIN_SPACE_ROCKET_NAME
            "ninja_shadow" -> VietnameseTexts.SKIN_NINJA_SHADOW_NAME
            "diamond_gem" -> VietnameseTexts.SKIN_DIAMOND_GEM_NAME
            "magic_ghost" -> VietnameseTexts.SKIN_MAGIC_GHOST_NAME
            else -> defaultNameVi
        }
        val en = defaultNameEn
        return when (lang) {
            AppLanguage.VI -> vi
            AppLanguage.EN -> en
        }
    }

    fun getDescription(lang: AppLanguage): String {
        val vi = when (id) {
            "classic_blue" -> VietnameseTexts.SKIN_CLASSIC_BLUE_DESC
            "cyber_bot" -> VietnameseTexts.SKIN_CYBER_BOT_DESC
            "golden_star" -> VietnameseTexts.SKIN_GOLDEN_STAR_DESC
            "lucky_cat" -> VietnameseTexts.SKIN_LUCKY_CAT_DESC
            "fire_dragon" -> VietnameseTexts.SKIN_FIRE_DRAGON_DESC
            "royal_crown" -> VietnameseTexts.SKIN_ROYAL_CROWN_DESC
            "space_rocket" -> VietnameseTexts.SKIN_SPACE_ROCKET_DESC
            "ninja_shadow" -> VietnameseTexts.SKIN_NINJA_SHADOW_DESC
            "diamond_gem" -> VietnameseTexts.SKIN_DIAMOND_GEM_DESC
            "magic_ghost" -> VietnameseTexts.SKIN_MAGIC_GHOST_DESC
            else -> defaultDescVi
        }
        val en = defaultDescEn
        return when (lang) {
            AppLanguage.VI -> vi
            AppLanguage.EN -> en
        }
    }
}

object GameItemAssets {

    /**
     * DANH SÁCH TOÀN BỘ VẬT PHẨM NHÂN VẬT CÓ THỂ THAY ĐỔI
     */
    val ALL_SKINS: List<GameItemSkin> = listOf(
        GameItemSkin(
            id = "classic_blue",
            defaultNameVi = "Neon Sphere",
            defaultNameEn = "Neon Sphere",
            defaultDescVi = "Quả cầu năng lượng phát quang Neon xanh dương thuần khiết",
            defaultDescEn = "Pure glowing blue neon energy sphere",
            priceCoins = 0,
            iconEmoji = "🔵",
            avatarType = PlayerAvatarType.NEON_SPHERE,
            primaryColor = Color(0xFF38BDF8),
            secondaryColor = Color(0xFF0284C7),
            glowColor = Color(0x6638BDF8)
        ),
        GameItemSkin(
            id = "cyber_bot",
            defaultNameVi = "Cyber Bot",
            defaultNameEn = "Cyber Bot",
            defaultDescVi = "Robot chiến binh công nghệ số tương lai với ánh mắt Cyan sắc sảo",
            defaultDescEn = "Futuristic cyber warrior bot with glowing cyan visor",
            priceCoins = 500,
            iconEmoji = "🤖",
            avatarType = PlayerAvatarType.CYBER_BOT,
            primaryColor = Color(0xFF06B6D4),
            secondaryColor = Color(0xFF0891B2),
            glowColor = Color(0x6622D3EE)
        ),
        GameItemSkin(
            id = "golden_star",
            defaultNameVi = "Sao Hoàng Kim",
            defaultNameEn = "Golden Star",
            defaultDescVi = "Ngôi sao vàng rực rỡ lấp lánh biểu tượng của tốc độ và danh vọng",
            defaultDescEn = "Radiant golden star symbolizing unmatched speed",
            priceCoins = 750,
            iconEmoji = "⭐",
            avatarType = PlayerAvatarType.GOLDEN_STAR,
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFD97706),
            glowColor = Color(0x77FDE68A)
        ),
        GameItemSkin(
            id = "lucky_cat",
            defaultNameVi = "Mèo May Mắn",
            defaultNameEn = "Lucky Cat",
            defaultDescVi = "Mèo Maneki Neko linh hoạt, mang lại tài lộc và sự nhanh nhẹn",
            defaultDescEn = "Adorable lucky cat bringing fortune and nimbleness",
            priceCoins = 950,
            iconEmoji = "🐱",
            avatarType = PlayerAvatarType.LUCKY_CAT,
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFFDB2777),
            glowColor = Color(0x66F472B6)
        ),
        GameItemSkin(
            id = "fire_dragon",
            defaultNameVi = "Rồng Lửa Bất Diệt",
            defaultNameEn = "Blazing Dragon",
            defaultDescVi = "Ngọn lửa bùng cháy thiêu rụi mọi chướng ngại vật trong mê cung",
            defaultDescEn = "Eternal mythical dragon flames conquering any maze",
            priceCoins = 1300,
            iconEmoji = "🔥",
            avatarType = PlayerAvatarType.FIRE_DRAGON,
            primaryColor = Color(0xFFEF4444),
            secondaryColor = Color(0xFFDC2626),
            glowColor = Color(0x77F87171)
        ),
        GameItemSkin(
            id = "space_rocket",
            defaultNameVi = "Phi Thuyền Không Gian",
            defaultNameEn = "Cosmic Rocket",
            defaultDescVi = "Tên lửa du hành siêu thanh xuyên qua không gian ma trận",
            defaultDescEn = "Supersonic space explorer rocket navigating cosmic mazes",
            priceCoins = 1500,
            iconEmoji = "🚀",
            avatarType = PlayerAvatarType.SPACE_ROCKET,
            primaryColor = Color(0xFF8B5CF6),
            secondaryColor = Color(0xFF7C3AED),
            glowColor = Color(0x66A78BFA)
        ),
        GameItemSkin(
            id = "ninja_shadow",
            defaultNameVi = "Ninja Bóng Đêm",
            defaultNameEn = "Shadow Ninja",
            defaultDescVi = "Chiến binh nhẫn giả thoắt ẩn thoắt hiện trong bóng tối mê cung",
            defaultDescEn = "Stealthy ninja master gliding through the shadows",
            priceCoins = 1750,
            iconEmoji = "🥷",
            avatarType = PlayerAvatarType.NINJA_SHADOW,
            primaryColor = Color(0xFF475569),
            secondaryColor = Color(0xFF334155),
            glowColor = Color(0x6694A3B8)
        ),
        GameItemSkin(
            id = "diamond_gem",
            defaultNameVi = "Kim Cương Tối Thượng",
            defaultNameEn = "Supreme Diamond",
            defaultDescVi = "Viên kim cương ngũ sắc vĩnh cửu tỏa ánh hào quang rực rỡ",
            defaultDescEn = "Flawless prismatic diamond gem radiating cosmic light",
            priceCoins = 2200,
            iconEmoji = "💎",
            avatarType = PlayerAvatarType.DIAMOND_GEM,
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF059669),
            glowColor = Color(0x6634D399)
        ),
        GameItemSkin(
            id = "magic_ghost",
            defaultNameVi = "Bóng Ma Tinh Nghịch",
            defaultNameEn = "Playful Ghost",
            defaultDescVi = "Hồn ma Neon dễ thương lướt nhẹ nhàng xuyên qua các góc cua",
            defaultDescEn = "Friendly neon specter effortlessly gliding through corners",
            priceCoins = 1100,
            iconEmoji = "👻",
            avatarType = PlayerAvatarType.MAGIC_GHOST,
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFF9333EA),
            glowColor = Color(0x66C084FC)
        ),
        GameItemSkin(
            id = "royal_crown",
            defaultNameVi = "Vương Miện Hoàng Gia",
            defaultNameEn = "Royal Crown",
            defaultDescVi = "Vương miện nạm ngọc quyền quý dành riêng cho thành viên VIP danh dự",
            defaultDescEn = "Opulent bejeweled gold crown reserved exclusively for VIP members",
            priceCoins = 0,
            iconEmoji = "👑",
            avatarType = PlayerAvatarType.ROYAL_CROWN,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFFD97706),
            glowColor = Color(0x88FEF08A),
            isVipOnly = true
        )
    )

    fun findSkin(id: String?): GameItemSkin {
        if (id == null) return ALL_SKINS.first()
        return ALL_SKINS.find { it.id == id } ?: ALL_SKINS.first()
    }

    fun getSkinPrimaryColorArgb(id: String?): Int {
        val skin = findSkin(id)
        return AndroidColor.argb(
            255,
            (skin.primaryColor.red * 255).toInt(),
            (skin.primaryColor.green * 255).toInt(),
            (skin.primaryColor.blue * 255).toInt()
        )
    }

    // =====================================================================================
    // CANVAS RENDERER: VẼ NHÂN VẬT ĐÃ TRANG BỊ TRỰC TIẾP LÊN MÀN HÌNH CHƠI GAME
    // =====================================================================================

    private val playerGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val playerBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val playerSecondaryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val playerDetailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val playerStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val emojiTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val pathHelper = Path()
    private val rectHelper = RectF()

    private val playerStandingLightFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = AndroidColor.argb(68, 255, 255, 255) // Ánh sáng trắng dịu phủ 1 ô vuông
    }
    private val playerStandingLightInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = AndroidColor.argb(48, 255, 255, 255) // Tâm sáng trắng bên trong ô vuông
    }
    private val playerStandingLightBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = AndroidColor.argb(155, 255, 255, 255) // Viền sáng trắng rõ nét phạm vi 1 ô vuông
    }

    /**
     * Vẽ hiệu ứng ánh sáng màu trắng ở đúng block (1 ô vuông) mà nhân vật đang đứng,
     * phạm vi gói gọn trong 1 ô vuông, tách biệt hoàn toàn với màu của nhân vật.
     */
    fun drawPlayerStandingBlockLight(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        cellSize: Float
    ) {
        val half = cellSize * 0.5f
        val outerInset = Math.max(1.0f, cellSize * 0.04f)
        val cornerRadius = Math.max(2.0f, cellSize * 0.12f)

        // Lớp ánh sáng nền trắng phạm vi 1 ô vuông đang đứng
        rectHelper.set(
            cx - half + outerInset,
            cy - half + outerInset,
            cx + half - outerInset,
            cy + half - outerInset
        )
        canvas.drawRoundRect(rectHelper, cornerRadius, cornerRadius, playerStandingLightFillPaint)

        // Lớp ánh sáng trắng dịu ở trung tâm ô vuông
        val innerInset = cellSize * 0.15f
        val innerCorner = Math.max(1.5f, cellSize * 0.09f)
        rectHelper.set(
            cx - half + innerInset,
            cy - half + innerInset,
            cx + half - innerInset,
            cy + half - innerInset
        )
        canvas.drawRoundRect(rectHelper, innerCorner, innerCorner, playerStandingLightInnerPaint)

        // Viền sáng trắng mỏng ôm sát phạm vi 1 ô vuông
        playerStandingLightBorderPaint.strokeWidth = Math.max(1.2f, cellSize * 0.045f)
        rectHelper.set(
            cx - half + outerInset,
            cy - half + outerInset,
            cx + half - outerInset,
            cy + half - outerInset
        )
        canvas.drawRoundRect(rectHelper, cornerRadius, cornerRadius, playerStandingLightBorderPaint)
    }

    /**
     * Hàm vẽ nhân vật đa dạng mẫu mã và icon lên Canvas thuần
     */
    fun drawPlayerOnCanvas(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        cellSize: Float,
        skinId: String
    ) {
        val skin = findSkin(skinId)
        val radius = cellSize * 0.36f
        val glowRadius = cellSize * 0.48f

        // Màu sắc từ skin
        val primary = AndroidColor.argb(
            (skin.primaryColor.alpha * 255).toInt(),
            (skin.primaryColor.red * 255).toInt(),
            (skin.primaryColor.green * 255).toInt(),
            (skin.primaryColor.blue * 255).toInt()
        )
        val secondary = AndroidColor.argb(
            (skin.secondaryColor.alpha * 255).toInt(),
            (skin.secondaryColor.red * 255).toInt(),
            (skin.secondaryColor.green * 255).toInt(),
            (skin.secondaryColor.blue * 255).toInt()
        )
        val glow = AndroidColor.argb(
            (skin.glowColor.alpha * 255).toInt(),
            (skin.glowColor.red * 255).toInt(),
            (skin.glowColor.green * 255).toInt(),
            (skin.glowColor.blue * 255).toInt()
        )

        playerGlowPaint.color = glow
        playerBodyPaint.color = primary
        playerSecondaryPaint.color = secondary
        playerDetailPaint.color = AndroidColor.WHITE
        playerStrokePaint.color = AndroidColor.WHITE
        playerStrokePaint.strokeWidth = Math.max(1.5f, cellSize * 0.04f)

        when (skin.avatarType) {
            PlayerAvatarType.NEON_SPHERE -> {
                // Quả cầu Neon 3D
                canvas.drawCircle(cx, cy, radius, playerBodyPaint)
                canvas.drawCircle(cx - radius * 0.3f, cy - radius * 0.3f, radius * 0.32f, playerDetailPaint)
                canvas.drawCircle(cx, cy, radius * 0.65f, playerSecondaryPaint)
                canvas.drawCircle(cx - radius * 0.15f, cy - radius * 0.15f, radius * 0.2f, playerDetailPaint)
            }

            PlayerAvatarType.CYBER_BOT -> {
                // Robot hình khối công nghệ tương lai gọn gàng
                rectHelper.set(cx - radius * 0.82f, cy - radius * 0.82f, cx + radius * 0.82f, cy + radius * 0.82f)
                canvas.drawRoundRect(rectHelper, radius * 0.25f, radius * 0.25f, playerBodyPaint)
                // Kính mắt Cyberspace
                rectHelper.set(cx - radius * 0.56f, cy - radius * 0.26f, cx + radius * 0.56f, cy + radius * 0.12f)
                playerDetailPaint.color = AndroidColor.parseColor("#E0F2FE")
                canvas.drawRoundRect(rectHelper, radius * 0.1f, radius * 0.1f, playerDetailPaint)
            }

            PlayerAvatarType.GOLDEN_STAR -> {
                // Ngôi sao 5 cánh vàng óng ánh
                drawStar(canvas, cx, cy, radius * 1.15f, radius * 0.52f, playerBodyPaint)
                // Lõi sáng kim cương
                playerDetailPaint.color = AndroidColor.parseColor("#FFFBEB")
                canvas.drawCircle(cx, cy, radius * 0.35f, playerDetailPaint)
                canvas.drawCircle(cx - radius * 0.1f, cy - radius * 0.1f, radius * 0.15f, playerDetailPaint)
            }

            PlayerAvatarType.LUCKY_CAT -> {
                // Đầu mèo
                canvas.drawCircle(cx, cy + radius * 0.1f, radius * 0.85f, playerBodyPaint)
                // Tai trái & tai phải
                pathHelper.reset()
                pathHelper.moveTo(cx - radius * 0.8f, cy - radius * 0.1f)
                pathHelper.lineTo(cx - radius * 0.65f, cy - radius * 0.95f)
                pathHelper.lineTo(cx - radius * 0.15f, cy - radius * 0.45f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerSecondaryPaint)

                pathHelper.reset()
                pathHelper.moveTo(cx + radius * 0.8f, cy - radius * 0.1f)
                pathHelper.lineTo(cx + radius * 0.65f, cy - radius * 0.95f)
                pathHelper.lineTo(cx + radius * 0.15f, cy - radius * 0.45f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerSecondaryPaint)

                // Mắt mèo long lanh
                playerDetailPaint.color = AndroidColor.WHITE
                canvas.drawCircle(cx - radius * 0.3f, cy, radius * 0.18f, playerDetailPaint)
                canvas.drawCircle(cx + radius * 0.3f, cy, radius * 0.18f, playerDetailPaint)
                playerDetailPaint.color = AndroidColor.parseColor("#1E293B")
                canvas.drawCircle(cx - radius * 0.28f, cy, radius * 0.1f, playerDetailPaint)
                canvas.drawCircle(cx + radius * 0.32f, cy, radius * 0.1f, playerDetailPaint)

                // Mũi & Miệng
                playerDetailPaint.color = AndroidColor.parseColor("#F43F5E")
                canvas.drawCircle(cx, cy + radius * 0.22f, radius * 0.08f, playerDetailPaint)
            }

            PlayerAvatarType.FIRE_DRAGON -> {
                // Ngọn lửa rồng bùng cháy 3 tầng
                pathHelper.reset()
                pathHelper.moveTo(cx, cy - radius * 1.2f)
                pathHelper.cubicTo(cx + radius * 1.1f, cy - radius * 0.2f, cx + radius * 0.9f, cy + radius * 0.9f, cx, cy + radius * 1.0f)
                pathHelper.cubicTo(cx - radius * 0.9f, cy + radius * 0.9f, cx - radius * 1.1f, cy - radius * 0.2f, cx, cy - radius * 1.2f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerBodyPaint)

                // Lõi lửa vàng bên trong
                pathHelper.reset()
                pathHelper.moveTo(cx, cy - radius * 0.7f)
                pathHelper.cubicTo(cx + radius * 0.6f, cy, cx + radius * 0.5f, cy + radius * 0.65f, cx, cy + radius * 0.7f)
                pathHelper.cubicTo(cx - radius * 0.5f, cy + radius * 0.65f, cx - radius * 0.6f, cy, cx, cy - radius * 0.7f)
                pathHelper.close()
                playerDetailPaint.color = AndroidColor.parseColor("#FDE047")
                canvas.drawPath(pathHelper, playerDetailPaint)
            }

            PlayerAvatarType.ROYAL_CROWN -> {
                // Vương miện hoàng gia 3 chóp
                pathHelper.reset()
                pathHelper.moveTo(cx - radius * 0.85f, cy + radius * 0.6f)
                pathHelper.lineTo(cx - radius * 0.9f, cy - radius * 0.35f)
                pathHelper.lineTo(cx - radius * 0.35f, cy + radius * 0.1f)
                pathHelper.lineTo(cx, cy - radius * 0.75f)
                pathHelper.lineTo(cx + radius * 0.35f, cy + radius * 0.1f)
                pathHelper.lineTo(cx + radius * 0.9f, cy - radius * 0.35f)
                pathHelper.lineTo(cx + radius * 0.85f, cy + radius * 0.6f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerBodyPaint)

                // Ngọc quý gắn trên đỉnh
                playerDetailPaint.color = AndroidColor.parseColor("#EF4444")
                canvas.drawCircle(cx, cy - radius * 0.75f, radius * 0.16f, playerDetailPaint)
                canvas.drawCircle(cx - radius * 0.9f, cy - radius * 0.35f, radius * 0.14f, playerDetailPaint)
                canvas.drawCircle(cx + radius * 0.9f, cy - radius * 0.35f, radius * 0.14f, playerDetailPaint)

                // Dải đế vương miện
                rectHelper.set(cx - radius * 0.85f, cy + radius * 0.45f, cx + radius * 0.85f, cy + radius * 0.7f)
                canvas.drawRoundRect(rectHelper, radius * 0.1f, radius * 0.1f, playerSecondaryPaint)
            }

            PlayerAvatarType.SPACE_ROCKET -> {
                // Tên lửa hình nón khí động học
                pathHelper.reset()
                pathHelper.moveTo(cx, cy - radius * 1.15f)
                pathHelper.cubicTo(cx + radius * 0.7f, cy - radius * 0.2f, cx + radius * 0.7f, cy + radius * 0.65f, cx + radius * 0.5f, cy + radius * 0.8f)
                pathHelper.lineTo(cx - radius * 0.5f, cy + radius * 0.8f)
                pathHelper.cubicTo(cx - radius * 0.7f, cy + radius * 0.65f, cx - radius * 0.7f, cy - radius * 0.2f, cx, cy - radius * 1.15f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerBodyPaint)

                // Cánh tên lửa 2 bên
                pathHelper.reset()
                pathHelper.moveTo(cx - radius * 0.5f, cy + radius * 0.4f)
                pathHelper.lineTo(cx - radius * 0.95f, cy + radius * 0.85f)
                pathHelper.lineTo(cx - radius * 0.5f, cy + radius * 0.8f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerSecondaryPaint)

                pathHelper.reset()
                pathHelper.moveTo(cx + radius * 0.5f, cy + radius * 0.4f)
                pathHelper.lineTo(cx + radius * 0.95f, cy + radius * 0.85f)
                pathHelper.lineTo(cx + radius * 0.5f, cy + radius * 0.8f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerSecondaryPaint)

                // Cửa sổ phi hành gia
                playerDetailPaint.color = AndroidColor.parseColor("#38BDF8")
                canvas.drawCircle(cx, cy - radius * 0.05f, radius * 0.32f, playerDetailPaint)
                playerDetailPaint.color = AndroidColor.WHITE
                canvas.drawCircle(cx - radius * 0.08f, cy - radius * 0.13f, radius * 0.1f, playerDetailPaint)
            }

            PlayerAvatarType.NINJA_SHADOW -> {
                // Khuôn mặt ninja bóng đêm
                canvas.drawCircle(cx, cy, radius * 0.9f, playerBodyPaint)
                // Băng đeo trán kim loại
                rectHelper.set(cx - radius * 0.75f, cy - radius * 0.55f, cx + radius * 0.75f, cy - radius * 0.15f)
                playerDetailPaint.color = AndroidColor.parseColor("#CBD5E1")
                canvas.drawRoundRect(rectHelper, radius * 0.1f, radius * 0.1f, playerDetailPaint)
                // Mắt nhẫn giả sắc lạnh
                playerDetailPaint.color = AndroidColor.parseColor("#F59E0B")
                canvas.drawCircle(cx - radius * 0.3f, cy + radius * 0.1f, radius * 0.12f, playerDetailPaint)
                canvas.drawCircle(cx + radius * 0.3f, cy + radius * 0.1f, radius * 0.12f, playerDetailPaint)
            }

            PlayerAvatarType.DIAMOND_GEM -> {
                // Viên kim cương đa giác
                pathHelper.reset()
                pathHelper.moveTo(cx, cy - radius * 1.0f)
                pathHelper.lineTo(cx + radius * 0.9f, cy - radius * 0.25f)
                pathHelper.lineTo(cx, cy + radius * 1.0f)
                pathHelper.lineTo(cx - radius * 0.9f, cy - radius * 0.25f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerBodyPaint)

                // Các mặt giác cắt lấp lánh
                pathHelper.reset()
                pathHelper.moveTo(cx, cy - radius * 1.0f)
                pathHelper.lineTo(cx, cy + radius * 1.0f)
                canvas.drawPath(pathHelper, playerStrokePaint)

                playerDetailPaint.color = AndroidColor.WHITE
                canvas.drawCircle(cx - radius * 0.25f, cy - radius * 0.25f, radius * 0.15f, playerDetailPaint)
            }

            PlayerAvatarType.MAGIC_GHOST -> {
                // Bóng ma neon tinh nghịch
                pathHelper.reset()
                pathHelper.moveTo(cx - radius * 0.75f, cy + radius * 0.85f)
                pathHelper.cubicTo(cx - radius * 0.95f, cy - radius * 0.5f, cx + radius * 0.95f, cy - radius * 0.5f, cx + radius * 0.75f, cy + radius * 0.85f)
                pathHelper.lineTo(cx + radius * 0.35f, cy + radius * 0.6f)
                pathHelper.lineTo(cx, cy + radius * 0.85f)
                pathHelper.lineTo(cx - radius * 0.35f, cy + radius * 0.6f)
                pathHelper.close()
                canvas.drawPath(pathHelper, playerBodyPaint)

                // Mắt to tròn tinh nghịch
                playerDetailPaint.color = AndroidColor.WHITE
                canvas.drawCircle(cx - radius * 0.25f, cy - radius * 0.1f, radius * 0.18f, playerDetailPaint)
                canvas.drawCircle(cx + radius * 0.25f, cy - radius * 0.1f, radius * 0.18f, playerDetailPaint)
                playerDetailPaint.color = AndroidColor.parseColor("#1E1B4B")
                canvas.drawCircle(cx - radius * 0.22f, cy - radius * 0.08f, radius * 0.1f, playerDetailPaint)
                canvas.drawCircle(cx + radius * 0.28f, cy - radius * 0.08f, radius * 0.1f, playerDetailPaint)
            }

            PlayerAvatarType.CUSTOM_EMOJI -> {
                // Vẽ Emoji đại diện làm nhân vật
                emojiTextPaint.textSize = cellSize * 0.65f
                val yOffset = (emojiTextPaint.descent() + emojiTextPaint.ascent()) / 2f
                canvas.drawText(skin.iconEmoji, cx, cy - yOffset, emojiTextPaint)
            }
        }
    }

    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, outerR: Float, innerR: Float, paint: Paint) {
        val path = Path()
        val numPoints = 5
        val angle = Math.PI / numPoints
        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val currAngle = i * angle - Math.PI / 2.0
            val x = (cx + r * Math.cos(currAngle)).toFloat()
            val y = (cy + r * Math.sin(currAngle)).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    /**
     * Vẽ vòng Khiên Bảo Vệ (Shield Aura) xung quanh người chơi khi có Khiên trong chế độ Sói Đuổi
     */
    fun drawShieldAuraOnPlayer(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        cellSize: Float,
        shieldCount: Int,
        isShieldTriggered: Boolean
    ) {
        if (!isShieldTriggered) return
        val auraRadius = cellSize * 0.40f
        playerStrokePaint.style = Paint.Style.STROKE
        playerStrokePaint.strokeWidth = Math.max(2.0f, cellSize * 0.06f)
        playerStrokePaint.color = AndroidColor.parseColor("#FFFDE047")
        canvas.drawCircle(cx, cy, auraRadius, playerStrokePaint)
    }

    /**
     * Vẽ Con Sói Truy Đuổi (Wolf Hunter) trực tiếp lên Canvas
     * @param isFrenzy true khi thời gian còn dưới 30% (Sói tăng tốc 0.3s/ô, rực lửa đỏ cam)
     * @param isStunned true khi Sói vừa chạm vào Khiên Bảo Vệ của người chơi
     */
    fun drawWolfOnCanvas(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        cellSize: Float,
        isFrenzy: Boolean,
        isStunned: Boolean
    ) {
        val radius = cellSize * 0.38f

        // Hào quang cảnh báo dưới chân Sói
        playerGlowPaint.color = when {
            isStunned -> AndroidColor.parseColor("#5538BDF8")
            isFrenzy -> AndroidColor.parseColor("#77EF4444")
            else -> AndroidColor.parseColor("#55F97316")
        }
        canvas.drawCircle(cx, cy, cellSize * 0.46f, playerGlowPaint)

        // Đầu Sói (Màu xám đen hung dữ hoặc Đỏ sẫm khi Frenzy <30% thời gian)
        playerBodyPaint.color = when {
            isStunned -> AndroidColor.parseColor("#475569")
            isFrenzy -> AndroidColor.parseColor("#991B1B")
            else -> AndroidColor.parseColor("#334155")
        }
        canvas.drawCircle(cx, cy + radius * 0.08f, radius * 0.86f, playerBodyPaint)

        // 2 Tai Sói nhọn vểnh cao
        playerSecondaryPaint.color = if (isFrenzy) AndroidColor.parseColor("#DC2626") else AndroidColor.parseColor("#1E293B")
        pathHelper.reset()
        pathHelper.moveTo(cx - radius * 0.82f, cy - radius * 0.05f)
        pathHelper.lineTo(cx - radius * 0.72f, cy - radius * 1.05f)
        pathHelper.lineTo(cx - radius * 0.18f, cy - radius * 0.52f)
        pathHelper.close()
        canvas.drawPath(pathHelper, playerSecondaryPaint)

        pathHelper.reset()
        pathHelper.moveTo(cx + radius * 0.82f, cy - radius * 0.05f)
        pathHelper.lineTo(cx + radius * 0.72f, cy - radius * 1.05f)
        pathHelper.lineTo(cx + radius * 0.18f, cy - radius * 0.52f)
        pathHelper.close()
        canvas.drawPath(pathHelper, playerSecondaryPaint)

        // Mõm Sói nhọn phía dưới
        playerDetailPaint.color = AndroidColor.parseColor("#CBD5E1")
        pathHelper.reset()
        pathHelper.moveTo(cx - radius * 0.48f, cy + radius * 0.18f)
        pathHelper.lineTo(cx + radius * 0.48f, cy + radius * 0.18f)
        pathHelper.lineTo(cx, cy + radius * 0.88f)
        pathHelper.close()
        canvas.drawPath(pathHelper, playerDetailPaint)

        // Chóp mũi đen
        playerDetailPaint.color = AndroidColor.parseColor("#0F172A")
        canvas.drawCircle(cx, cy + radius * 0.72f, radius * 0.14f, playerDetailPaint)

        // Mắt Sói phát sáng (Đỏ rực khi săn mồi, Vàng rực khi Frenzy <30%)
        playerDetailPaint.color = when {
            isStunned -> AndroidColor.parseColor("#38BDF8")
            isFrenzy -> AndroidColor.parseColor("#FDE047")
            else -> AndroidColor.parseColor("#EF4444")
        }
        canvas.drawCircle(cx - radius * 0.32f, cy - radius * 0.08f, radius * 0.17f, playerDetailPaint)
        canvas.drawCircle(cx + radius * 0.32f, cy - radius * 0.08f, radius * 0.17f, playerDetailPaint)
    }

    /**
     * Vẽ hiệu ứng Đóng Băng 2 Giây trên người chơi khi trúng Băng Xuyên Tường của Sói
     */
    fun drawFrozenEffectOnPlayer(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        cellSize: Float
    ) {
        val r = cellSize * 0.45f
        // Khối băng lục giác trong suốt bao bọc nhân vật
        pathHelper.reset()
        for (i in 0 until 6) {
            val angle = Math.toRadians((60 * i - 30).toDouble())
            val px = (cx + r * Math.cos(angle)).toFloat()
            val py = (cy + r * Math.sin(angle)).toFloat()
            if (i == 0) pathHelper.moveTo(px, py) else pathHelper.lineTo(px, py)
        }
        pathHelper.close()

        playerGlowPaint.color = AndroidColor.parseColor("#7738BDF8")
        canvas.drawPath(pathHelper, playerGlowPaint)

        playerStrokePaint.style = Paint.Style.STROKE
        playerStrokePaint.strokeWidth = Math.max(2.4f, cellSize * 0.07f)
        playerStrokePaint.color = AndroidColor.parseColor("#E0F2FE")
        canvas.drawPath(pathHelper, playerStrokePaint)

        // Đường tinh thể băng chéo
        playerStrokePaint.strokeWidth = Math.max(1.4f, cellSize * 0.04f)
        playerStrokePaint.color = AndroidColor.parseColor("#7DD3FC")
        canvas.drawLine(cx - r * 0.55f, cy - r * 0.55f, cx + r * 0.55f, cy + r * 0.55f, playerStrokePaint)
        canvas.drawLine(cx + r * 0.55f, cy - r * 0.55f, cx - r * 0.55f, cy + r * 0.55f, playerStrokePaint)
    }

    /**
     * Vẽ tia Băng Xuyên Tường (Wall-Piercing Ice Throw) từ Sói bay thẳng tới Người Chơi
     */
    fun drawIceBeamOnCanvas(
        canvas: Canvas,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        cellSize: Float
    ) {
        playerStrokePaint.style = Paint.Style.STROKE
        playerStrokePaint.strokeCap = Paint.Cap.ROUND

        // Hào quang băng xuyên tường ngoài
        playerStrokePaint.strokeWidth = Math.max(5f, cellSize * 0.22f)
        playerStrokePaint.color = AndroidColor.parseColor("#6638BDF8")
        canvas.drawLine(fromX, fromY, toX, toY, playerStrokePaint)

        // Lõi tia băng sắc lẹm xuyên qua tường
        playerStrokePaint.strokeWidth = Math.max(2.5f, cellSize * 0.09f)
        playerStrokePaint.color = AndroidColor.parseColor("#E0F2FE")
        canvas.drawLine(fromX, fromY, toX, toY, playerStrokePaint)

        // Mảnh băng tại điểm va chạm
        playerGlowPaint.color = AndroidColor.parseColor("#AA7DD3FC")
        canvas.drawCircle(toX, toY, cellSize * 0.34f, playerGlowPaint)
    }

    /**
     * Vẽ Vật Phẩm Thu Thập trên ô mê cung:
     * typeOrdinal: 0 = COIN (Xu 🪙), 1 = KEY (Chìa khóa 🗝️), 2 = SHIELD (Khiên 🛡️), 3 = EQUIPMENT_CHEST (Rương 🎁)
     */
    fun drawCollectibleOnCanvas(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        cellSize: Float,
        typeOrdinal: Int
    ) {
        val r = cellSize * 0.28f
        when (typeOrdinal) {
            0 -> { // COIN (Đồng Xu Vàng 🪙)
                playerGlowPaint.color = AndroidColor.parseColor("#44FBBF24")
                canvas.drawCircle(cx, cy, r * 1.25f, playerGlowPaint)

                playerBodyPaint.color = AndroidColor.parseColor("#F59E0B")
                canvas.drawCircle(cx, cy, r, playerBodyPaint)

                playerSecondaryPaint.color = AndroidColor.parseColor("#FDE047")
                canvas.drawCircle(cx, cy, r * 0.74f, playerSecondaryPaint)

                playerDetailPaint.color = AndroidColor.parseColor("#B45309")
                rectHelper.set(cx - r * 0.14f, cy - r * 0.38f, cx + r * 0.14f, cy + r * 0.38f)
                canvas.drawRoundRect(rectHelper, r * 0.06f, r * 0.06f, playerDetailPaint)
            }
            1 -> { // KEY (Chìa Khóa Kho Báu 🗝️)
                playerGlowPaint.color = AndroidColor.parseColor("#44FDE047")
                canvas.drawCircle(cx, cy, r * 1.3f, playerGlowPaint)

                playerStrokePaint.style = Paint.Style.STROKE
                playerStrokePaint.strokeWidth = Math.max(2f, cellSize * 0.06f)
                playerStrokePaint.color = AndroidColor.parseColor("#FBBF24")

                // Đầu chìa khóa tròn
                canvas.drawCircle(cx - r * 0.35f, cy, r * 0.45f, playerStrokePaint)
                // Thân chìa khóa & răng chìa
                canvas.drawLine(cx + r * 0.1f, cy, cx + r * 0.9f, cy, playerStrokePaint)
                canvas.drawLine(cx + r * 0.55f, cy, cx + r * 0.55f, cy + r * 0.4f, playerStrokePaint)
                canvas.drawLine(cx + r * 0.85f, cy, cx + r * 0.85f, cy + r * 0.45f, playerStrokePaint)
            }
            2 -> { // SHIELD (Khiên Bảo Vệ Chống Sói 🛡️)
                playerGlowPaint.color = AndroidColor.parseColor("#5538BDF8")
                canvas.drawCircle(cx, cy, r * 1.35f, playerGlowPaint)

                pathHelper.reset()
                pathHelper.moveTo(cx, cy - r * 1.05f)
                pathHelper.lineTo(cx + r * 0.9f, cy - r * 0.65f)
                pathHelper.lineTo(cx + r * 0.75f, cy + r * 0.35f)
                pathHelper.lineTo(cx, cy + r * 1.1f)
                pathHelper.lineTo(cx - r * 0.75f, cy + r * 0.35f)
                pathHelper.lineTo(cx - r * 0.9f, cy - r * 0.65f)
                pathHelper.close()

                playerBodyPaint.color = AndroidColor.parseColor("#0284C7")
                canvas.drawPath(pathHelper, playerBodyPaint)

                playerStrokePaint.style = Paint.Style.STROKE
                playerStrokePaint.strokeWidth = Math.max(1.8f, cellSize * 0.05f)
                playerStrokePaint.color = AndroidColor.parseColor("#FDE047")
                canvas.drawPath(pathHelper, playerStrokePaint)

                // Chữ thập sáng giữa khiên
                playerStrokePaint.color = AndroidColor.parseColor("#E0F2FE")
                canvas.drawLine(cx, cy - r * 0.55f, cx, cy + r * 0.55f, playerStrokePaint)
                canvas.drawLine(cx - r * 0.45f, cy - r * 0.1f, cx + r * 0.45f, cy - r * 0.1f, playerStrokePaint)
            }
            else -> { // EQUIPMENT_CHEST (Rương Trang Bị 🎁)
                playerGlowPaint.color = AndroidColor.parseColor("#55EC4899")
                canvas.drawCircle(cx, cy, r * 1.35f, playerGlowPaint)

                rectHelper.set(cx - r * 0.9f, cy - r * 0.65f, cx + r * 0.9f, cy + r * 0.75f)
                playerBodyPaint.color = AndroidColor.parseColor("#9333EA")
                canvas.drawRoundRect(rectHelper, r * 0.2f, r * 0.2f, playerBodyPaint)

                playerStrokePaint.style = Paint.Style.STROKE
                playerStrokePaint.strokeWidth = Math.max(1.8f, cellSize * 0.05f)
                playerStrokePaint.color = AndroidColor.parseColor("#FBBF24")
                canvas.drawRoundRect(rectHelper, r * 0.2f, r * 0.2f, playerStrokePaint)
                canvas.drawLine(cx - r * 0.9f, cy, cx + r * 0.9f, cy, playerStrokePaint)
                canvas.drawCircle(cx, cy, r * 0.22f, playerSecondaryPaint.apply { color = AndroidColor.parseColor("#FDE047") })
            }
        }
    }
}
