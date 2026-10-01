package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Premium Dark-First Puzzle Studio Palette
// 1. Background: Quiet Dark Navy/Violet (#090B16)
val MazeBgDark = Color(0xFF090B16)

// 2. Surface 1: Studio Card & Panel Base Surface (#131020)
val MazeSurface1 = Color(0xFF131020)

// 3. Surface 2: Elevated Surface (#1B1630)
val MazeSurface2 = Color(0xFF1B1630)

// Surface 3: High-Emphasis Container Surface (#241E3E)
val MazeSurface3 = Color(0xFF241E3E)

// 4. Primary Accent: Warm Gold (#F4C15D) & Clean Accents
val MazeAmber = Color(0xFFF4C15D)
val MazeAmberGlow = Color(0xFFFFD580)
val MazeAccent = MazeAmber
val MazeAccentMint = Color(0xFF48D597)
val MazeCyan = Color(0xFF38BDF8)
val MazeCyanGlow = Color(0xFF7DD3FC)
val MazeViolet = Color(0xFFA855F7)
val MazePink = Color(0xFFF05AAB)

// 5. Studio Lighting Borders & Specular Edge Highlights (#302945)
val MazeEdgeHighlight = Color(0xFF302945)
val MazeEdgeHighlightBright = Color(0xFF463C65)

// 6. Tactile Button Shadows & Highlights
val MazeTactileTopHighlight = Color(0x22FFFFFF)
val MazeTactileShadow = Color(0x66000000)

// Text Tokens (High-contrast, clean typography)
val MazeTextH1 = Color(0xFFF5F3FA)
val MazeTextBody = Color(0xFFA8A2B8)
val MazeTextMuted = Color(0xFF777185)

// Semantic Tokens
val MazeStar = Color(0xFFF4C15D)
val MazeCoin = Color(0xFFF4C15D)
val MazeDanger = Color(0xFFFF5F67)
val MazeSuccess = Color(0xFF48D597)
val MazePlayer = Color(0xFFF4C15D)
val MazeGoal = Color(0xFFFF5B68)
val MazeWallDark = Color(0xFFF05AAB)
val MazeFloor = Color(0xFF110B1C)

// Consistent Studio Tokens
val MazeBgLight = Color(0xFF090B16)
val MazeSurfaceLight = Color(0xFF131020)
val MazeSurface2Light = Color(0xFF1B1630)
val MazeTextLight = Color(0xFFF5F3FA)

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
