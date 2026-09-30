package com.example.core.engine

const val NORTH = 1
const val EAST = 2
const val SOUTH = 4
const val WEST = 8

data class Maze(
    val w: Int,
    val h: Int,
    val masks: IntArray,
    val start: Point,
    val goal: Point,
    val spine: List<Point>,
    val target: Int,
    val seedStr: String
) {
    fun cellAt(x: Int, y: Int): Int {
        if (x !in 0 until w || y !in 0 until h) return 0
        return masks[y * w + x]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Maze
        return w == other.w && h == other.h && seedStr == other.seedStr
    }

    override fun hashCode(): Int {
        var result = w
        result = 31 * result + h
        result = 31 * result + seedStr.hashCode()
        return result
    }
}

enum class CollectibleType {
    COIN,
    KEY,
    SHIELD,
    EQUIPMENT_CHEST
}

data class MazeCollectible(
    val x: Int,
    val y: Int,
    val type: CollectibleType,
    val amount: Int = 1
) {
    val cellIndex: Int
        get() = y * 10000 + x
}

data class GoalDirectionHint(
    val nextStepDir: Int, // NORTH, EAST, SOUTH, WEST, or 0 if at goal
    val remainingPathSteps: Int,
    val directDx: Int,
    val directDy: Int,
    val sampledRouteToGoal: List<Point>
) {
    val remainingStepsToGoal: Int
        get() = remainingPathSteps
}

data class LevelDef(
    val id: String,
    val numericLevel: Int?,
    val tier: String? = null,
    val w: Int,
    val h: Int,
    val target: Int,
    val moveCap: Int? = null,
    val vision: Int? = null,
    val isFinal: Boolean = false,
    val noTimer: Boolean = false,
    val isWolfChase: Boolean = false,
    val timeLimitSec: Int = 0,
    val algorithmIndex: Int = 1 // 1: Quay lui (Backtracking), 2: Kruskal (như màn 40), 3: Wilson (như màn 60x60)
) {
    val isReplaySupported: Boolean
        get() = w <= 60 && h <= 60
}

data class MazeConfig(
    val width: Int = 0,
    val height: Int = 0,
    val seed: Long = 0L,
    val braidPercentage: Float = 0.18f,
    val corridorMomentum: Float = 0.70f
) {
    companion object {
        // Hệ thống 100 màn chơi chính:
        // - Màn 1 đến 50: Xen kẽ 2 thuật toán 1 và 2 (2 màn liên tiếp có cùng 1 cỡ bản đồ: 5x5 -> 29x29)
        // - Màn 51 đến 100: Xen kẽ 2 thuật toán 2 và 3 (2 màn liên tiếp có cùng 1 cỡ bản đồ: 30x30 -> 54x54)
        const val MAX_LEVEL = 100
        const val PASSES_TO_UNLOCK_IMPOSSIBLE = 50
        const val FULL_VISION_MAX_SIZE = 10
        const val VISION_WINDOW = 10
        const val NO_STAR_GATE_UNTIL = 7

        // Chế độ Sói Truy Đuổi (Wolf Chase Mode):
        // Mở từ màn số 10 trở đi, các màn sói đuổi từ 5x5 đến 30x30 (26 màn)
        const val WOLF_UNLOCK_LEVEL = 10
        const val PASSES_TO_UNLOCK_WOLF_MODE = WOLF_UNLOCK_LEVEL
        const val MAX_WOLF_LEVEL = 26 // 4 + 1 = 5x5 -> 4 + 26 = 30x30
        const val WOLF_MAX_LEVEL = MAX_WOLF_LEVEL
        const val WOLF_ACTIVATE_DISTANCE_CELLS = 5 // Đếm ngược 5 giây từ lúc bắt đầu trò chơi thì Sói bắt đầu đuổi
        const val WOLF_SPAWN_DELAY_SEC = 5 // Sói đuổi sau 5 giây bắt đầu trò chơi
        const val WOLF_HEAD_START_SECONDS = WOLF_SPAWN_DELAY_SEC
        const val WOLF_NORMAL_STEP_MS = 400L // Bình thường: 0.4 giây / 1 ô
        const val WOLF_HALF_TIME_STEP_MS = 350L // Khi thời gian còn dưới 1 nửa (<50%): 0.35 giây / 1 ô
        const val WOLF_FRENZY_STEP_MS = 300L // Khi thời gian còn dưới 30%: 0.3 giây / 1 ô
        const val WOLF_ICE_MIN_DISTANCE_CELLS = 25 // Ném băng chỉ có tác dụng khi khoảng cách giữa người chơi và sói trên 25 ô
        const val WOLF_ICE_FREEZE_DURATION_MS = 2000L // Người chơi dính băng xuyên tường bị đóng băng 2 giây
        const val WOLF_ICE_COOLDOWN_MS = 15_000L // Thời gian hồi chiêu ném băng của sói là 15 giây

        fun wolfChaseDef(wolfStage: Int): LevelDef = generateWolfLevelDef(wolfStage)

        fun difficultyRatio(i: Int): Double = (0.72 + i * 0.0006).coerceAtMost(0.92)

        /**
         * Trả về chỉ số thuật toán cho một level:
         * - Màn 1..50: Xen kẽ 2 thuật toán 1, 2 (Màn lẻ -> 1, Màn chẵn -> 2)
         * - Màn 51..100: Xen kẽ 2 thuật toán 2, 3 (Màn lẻ -> 2, Màn chẵn -> 3)
         */
        fun algorithmIndexForLevel(levelNumber: Int): Int {
            val clamped = levelNumber.coerceIn(1, MAX_LEVEL)
            return if (clamped <= 50) {
                if (clamped % 2 == 1) 1 else 2
            } else {
                if (clamped % 2 == 1) 2 else 3
            }
        }

        /**
         * Trả về kích thước bản đồ (size x size) cho một level:
         * 2 level liên tiếp có cùng 1 cỡ bản đồ (VD: Lv 1,2 = 5x5; Lv 3,4 = 6x6; ... Lv 49,50 = 29x29; Lv 51,52 = 30x30; ... Lv 99,100 = 54x54)
         */
        fun mapSizeForLevel(levelNumber: Int): Int {
            val clamped = levelNumber.coerceIn(1, MAX_LEVEL)
            return 5 + (clamped - 1) / 2
        }

        fun generateLevelDef(levelNumber: Int, enableWolfChase: Boolean = false): LevelDef {
            val clampedLevel = levelNumber.coerceIn(1, MAX_LEVEL)
            val size = mapSizeForLevel(clampedLevel)
            val algIndex = algorithmIndexForLevel(clampedLevel)
            val n = size * size
            val ratio = difficultyRatio(clampedLevel)
            val baseTarget = ((n - 1) * ratio).toInt().coerceIn(1, n - 1)

            // Giảm thời gian ván game / tính sao theo yêu cầu độ khó:
            // - Dưới 40: giảm 30%
            // - Từ 40 trở đi: giảm 25%
            val reductionPercent = when {
                clampedLevel < 40 -> 0.30
                else -> 0.25
            }
            val target = (baseTarget * (1.0 - reductionPercent)).toInt().coerceIn(1, n - 1)

            val vision = if (size > 50) 10 else if (size > FULL_VISION_MAX_SIZE) VISION_WINDOW else null
            val wolfActive = enableWolfChase && clampedLevel >= WOLF_UNLOCK_LEVEL && size in 5..30
            val timeLimit = if (clampedLevel == 1) 0 else maxOf(24, target * 2)
            return LevelDef(
                id = if (wolfActive) "WOLF_REGULAR_$clampedLevel" else clampedLevel.toString(),
                numericLevel = clampedLevel,
                tier = if (wolfActive) "WOLF" else null,
                w = size,
                h = size,
                target = target,
                moveCap = null,
                vision = vision,
                isFinal = false,
                noTimer = (clampedLevel == 1),
                isWolfChase = wolfActive,
                timeLimitSec = timeLimit,
                algorithmIndex = algIndex
            )
        }

        /**
         * Sinh cấu hình màn chơi Chế Độ Sói Truy Đuổi (từ 5x5 đến 30x30, tương ứng cấp 1..26)
         */
        fun generateWolfLevelDef(wolfStage: Int): LevelDef {
            val clampedStage = wolfStage.coerceIn(1, MAX_WOLF_LEVEL)
            val size = 4 + clampedStage // 5x5 đến 30x30
            val n = size * size
            val ratio = difficultyRatio(clampedStage)
            val baseTarget = ((n - 1) * ratio).toInt().coerceIn(1, n - 1)
            val target = (baseTarget * 0.75).toInt().coerceIn(minOf(12, n - 1), n - 1)
            // Thời gian của game Sói Đuổi giảm đi 30% để tăng độ kịch tính
            val baseWolfTimeSec = maxOf(26, (target * 1.8).toInt() + 10)
            val timeLimitSec = maxOf(18, (baseWolfTimeSec * 0.70).toInt())
            val vision = if (size > 15) VISION_WINDOW else null

            return LevelDef(
                id = "WOLF_$clampedStage",
                numericLevel = clampedStage,
                tier = "WOLF",
                w = size,
                h = size,
                target = target,
                moveCap = null,
                vision = vision,
                isFinal = false,
                noTimer = false,
                isWolfChase = true,
                timeLimitSec = timeLimitSec
            )
        }

        val IMPOSSIBLE_TIER_SIZES = listOf(300, 400, 500)
        const val SUPER_SIZE = 1000

        fun impossibleDefForTier(tier: Int): LevelDef {
            val size = IMPOSSIBLE_TIER_SIZES[(tier - 1).coerceIn(0, IMPOSSIBLE_TIER_SIZES.size - 1)]
            val target = (size * size) / 100
            return LevelDef(
                id = "IMPOSSIBLE_$tier",
                numericLevel = null,
                tier = tier.toString(),
                w = size,
                h = size,
                target = target,
                moveCap = target,
                vision = VISION_WINDOW,
                isFinal = true,
                noTimer = true
            )
        }

        fun superDef(): LevelDef {
            val target = (SUPER_SIZE * SUPER_SIZE) / 100
            return LevelDef(
                id = "IMPOSSIBLE_SUPER",
                numericLevel = null,
                tier = "SUPER",
                w = SUPER_SIZE,
                h = SUPER_SIZE,
                target = target,
                moveCap = target,
                vision = VISION_WINDOW,
                isFinal = true,
                noTimer = true
            )
        }

        fun starReqFor(levelNumber: Int): Int {
            if (levelNumber <= NO_STAR_GATE_UNTIL) return 0
            if (levelNumber % 5 == 0) {
                return 3 * levelNumber - 5
            }
            return 0
        }

        fun starsFromPace(secPerCell: Double): Int = when {
            secPerCell <= 0.7 -> 3
            secPerCell <= 1.0 -> 2
            secPerCell <= 2.0 -> 1
            else -> 0
        }

        fun encodeCoord(p: Point): String {
            var n = p.x.coerceAtLeast(0)
            val out = StringBuilder()
            do {
                out.append(('A'.code + (n % 26)).toChar())
                n /= 26
            } while (n > 0)
            while (out.length < 2) out.append('A')
            val xStr = out.reverse().toString()
            val yStr = if (p.y >= 100) p.y.toString().padStart(3, '0') else p.y.toString().padStart(2, '0')
            return "$xStr$yStr"
        }
    }
}
