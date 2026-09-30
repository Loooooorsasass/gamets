package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Art Director Design Tokens ("Pro Studio Cyber-Arcade High-Contrast System")
// 1. Background: Deep Rich Midnight Navy (#080D1A)
val MazeBgDark = Color(0xFF080D1A)

// 2. Surface 1: Studio Card & Panel Base Surface (#111C35)
val MazeSurface1 = Color(0xFF111C35)

// 3. Surface 2: Elevated Card & Interactive Surface (#182849)
val MazeSurface2 = Color(0xFF182849)

// Surface 3: High-Emphasis Container Surface (#223561)
val MazeSurface3 = Color(0xFF223561)

// 4. Primary Accent: Radiant Warm Amber Gold & Cyber Cyan
val MazeAmber = Color(0xFFFFB020)
val MazeAmberGlow = Color(0xFFFFC53D)
val MazeAccent = MazeAmber
val MazeAccentMint = Color(0xFF10B981)
val MazeCyan = Color(0xFF00E5FF)
val MazeCyanGlow = Color(0xFF38BDF8)
val MazeViolet = Color(0xFF8B5CF6)
val MazePink = Color(0xFFEC4899)

// 5. Studio Lighting Borders & Specular Edge Highlights
val MazeEdgeHighlight = Color(0xFF293B66) // Crisp modern studio edge
val MazeEdgeHighlightBright = Color(0xFF435C9A) // Strong specular rim highlight

// 6. Tactile Button Shadows & Highlights
val MazeTactileTopHighlight = Color(0x33FFFFFF) // Crisp top inner bevel light
val MazeTactileShadow = Color(0x80020617)

// Text Tokens (High-contrast studio legibility for pro players)
val MazeTextH1 = Color(0xFFF8FAFC)
val MazeTextBody = Color(0xFFCBD5E1)
val MazeTextMuted = Color(0xFF8A9BB8)

// Semantic Tokens
val MazeStar = Color(0xFFFFB800)
val MazeCoin = Color(0xFFFFC000)
val MazeDanger = Color(0xFFFF4D4D)
val MazeSuccess = Color(0xFF10B981)
val MazePlayer = Color(0xFF00E5FF)
val MazeWallDark = Color(0xFF38BDF8)

// Consistent Studio Tokens
val MazeBgLight = Color(0xFF080D1A)
val MazeSurfaceLight = Color(0xFF111C35)
val MazeSurface2Light = Color(0xFF182849)
val MazeTextLight = Color(0xFFF8FAFC)

/**
 * Bảng 10 màu Bản Đồ Studio chuyên nghiệp (10-Color Studio Map Palette)
 * Độ tương phản cao, rõ ràng từng ô cho người chơi chuyên nghiệp.
 */
data class StudioMapColorSpec(
    val index: Int,
    val nameVi: String,
    val nameEn: String,
    val primary: Color,
    val brightGlow: Color,
    val surfaceTop: Color,
    val surfaceBottom: Color
)

object StudioMapPalette {
    val COLORS: List<StudioMapColorSpec> = listOf(
        StudioMapColorSpec(
            index = 0,
            nameVi = "Lam Ngọc Cyber",
            nameEn = "Cyber Cyan",
            primary = Color(0xFF00E5FF),
            brightGlow = Color(0xFF38BDF8),
            surfaceTop = Color(0xFF102B4C),
            surfaceBottom = Color(0xFF08162A)
        ),
        StudioMapColorSpec(
            index = 1,
            nameVi = "Ngọc Bích Lục Bảo",
            nameEn = "Emerald Jade",
            primary = Color(0xFF10B981),
            brightGlow = Color(0xFF34D399),
            surfaceTop = Color(0xFF0C3A2C),
            surfaceBottom = Color(0xFF061F17)
        ),
        StudioMapColorSpec(
            index = 2,
            nameVi = "Hoàng Kim Rực Rỡ",
            nameEn = "Solar Gold",
            primary = Color(0xFFFFB020),
            brightGlow = Color(0xFFFDE047),
            surfaceTop = Color(0xFF3A2B0E),
            surfaceBottom = Color(0xFF201807)
        ),
        StudioMapColorSpec(
            index = 3,
            nameVi = "Tím Tinh Vân",
            nameEn = "Amethyst Nebula",
            primary = Color(0xFFA855F7),
            brightGlow = Color(0xFFC084FC),
            surfaceTop = Color(0xFF2E164E),
            surfaceBottom = Color(0xFF190C2C)
        ),
        StudioMapColorSpec(
            index = 4,
            nameVi = "Hồng Ngọc Hỏa Diệm",
            nameEn = "Crimson Ruby",
            primary = Color(0xFFF43F5E),
            brightGlow = Color(0xFFFB7185),
            surfaceTop = Color(0xFF401323),
            surfaceBottom = Color(0xFF220912)
        ),
        StudioMapColorSpec(
            index = 5,
            nameVi = "Lam Đại Dương",
            nameEn = "Cobalt Sapphire",
            primary = Color(0xFF3B82F6),
            brightGlow = Color(0xFF60A5FA),
            surfaceTop = Color(0xFF142954),
            surfaceBottom = Color(0xFF0A152E)
        ),
        StudioMapColorSpec(
            index = 6,
            nameVi = "Cam Hoàng Hôn",
            nameEn = "Coral Sunset",
            primary = Color(0xFFF97316),
            brightGlow = Color(0xFFFB923C),
            surfaceTop = Color(0xFF3D2010),
            surfaceBottom = Color(0xFF211008)
        ),
        StudioMapColorSpec(
            index = 7,
            nameVi = "Băng Tuyết Bạc Hà",
            nameEn = "Arctic Mint",
            primary = Color(0xFF14B8A6),
            brightGlow = Color(0xFF2DD4BF),
            surfaceTop = Color(0xFF0E3634),
            surfaceBottom = Color(0xFF071E1D)
        ),
        StudioMapColorSpec(
            index = 8,
            nameVi = "Hồng Neon Synth",
            nameEn = "Neon Magenta",
            primary = Color(0xFFEC4899),
            brightGlow = Color(0xFFF472B6),
            surfaceTop = Color(0xFF3C1433),
            surfaceBottom = Color(0xFF210A1C)
        ),
        StudioMapColorSpec(
            index = 9,
            nameVi = "Lục Quang Năng Lượng",
            nameEn = "Solar Lime",
            primary = Color(0xFF84CC16),
            brightGlow = Color(0xFFA3E635),
            surfaceTop = Color(0xFF25380F),
            surfaceBottom = Color(0xFF131F07)
        )
    )

    fun forLevel(levelNumber: Int): StudioMapColorSpec {
        val idx = ((levelNumber - 1).coerceAtLeast(0)) % COLORS.size
        return COLORS[idx]
    }

    fun forIndex(index: Int): StudioMapColorSpec {
        val idx = ((index % COLORS.size) + COLORS.size) % COLORS.size
        return COLORS[idx]
    }
}
