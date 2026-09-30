package com.example.core.engine

import com.example.data.local.MazeLevelEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.random.Random


/**
 * Kết quả sinh mê cung trả về cho UI Layer.
 *
 * @param grid Mảng 1D ByteArray chứa bitmask 4 bức tường của từng ô (kích thước width * height).
 * @param startIndex Vị trí xuất phát (1D index = y * width + x).
 * @param endIndex Vị trí đích đến xa nhất (1D index = y * width + x).
 */
data class MazeResult(
    val grid: ByteArray,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Hệ thống sinh mê cung 2D hiệu năng cao ứng dụng Thuật toán Prim Cải tiến (Modified Randomized Prim's Algorithm).
 *
 * Tính năng nổi bật:
 * 1. Thuật toán Prim với Quán tính Hành lang (Corridor Momentum):
 *    Khắc phục triệt để nhược điểm của Prim truyền thống (thường tạo ra quá nhiều ngõ cụt 1 ô ngắn ngủn),
 *    tạo ra các hành lang quanh co, đan xen sâu rộng, độ phân nhánh phong phú và đầy tính đánh lừa.
 * 2. Triệt tiêu Ngõ Cụt Nông (Obvious Dead-End Elimination):
 *    Tự động quét và đục thông các ngõ cụt ngắn, biến chúng thành các tuyến đường vòng hoặc nhánh rẽ sâu.
 * 3. Thuật toán Braid Loops:
 *    Tạo các vòng lặp đánh lừa thị giác, khiến người chơi không thể nhìn lướt để nhận biết đường đi.
 * 4. Double BFS (Graph Diameter):
 *    Tìm 2 điểm xa nhất trong mê cung làm Start và Goal (cờ đỏ).
 */
object MazeGenerator {

    // =========================================================================
    // BITMASK CONSTANTS (Lưu trữ 4 tường + Trạng thái đã duyệt trong 1 byte)
    // =========================================================================
    const val WALL_TOP: Byte = 1        // Bit 0 (0000 0001) - Tường phía trên (y + 1)
    const val WALL_RIGHT: Byte = 2      // Bit 1 (0000 0010) - Tường bên phải (x + 1)
    const val WALL_BOTTOM: Byte = 4     // Bit 2 (0000 0100) - Tường phía dưới (y - 1)
    const val WALL_LEFT: Byte = 8       // Bit 3 (0000 1000) - Tường bên trái (x - 1)
    const val FLAG_VISITED: Byte = 16   // Bit 4 (0001 0000) - Cờ ô đã thuộc cây mê cung (IN_MAZE)
    const val WALLS_ALL: Byte = 15      // 1 | 2 | 4 | 8 (0000 1111) - Khởi tạo cả 4 bức tường đều đóng

    // Chu kỳ ngắt nhường CPU để chống ANR khi sinh bản đồ lớn
    private const val YIELD_INTERVAL = 10_000

    /**
     * Điểm vào chính sinh mê cung bất đồng bộ bằng Thuật toán Prim Cải tiến trên Worker Thread.
     */
    suspend fun generate(config: MazeConfig): MazeResult = withContext(Dispatchers.Default) {
        val width = config.width
        val height = config.height
        val totalCells = width * height
        val random = Random(config.seed)

        // 1. Cấp phát mảng 1D ByteArray duy nhất đại diện cho lưới (Zero-Allocation)
        val grid = ByteArray(totalCells) { WALLS_ALL }

        // 2. Thuật toán sinh cốt lõi: Modified Randomized Prim's với Quán tính hành lang
        generatePrimMaze(grid, width, height, config.corridorMomentum, random)

        // 3. Triệt tiêu các ngõ cụt ngắn rõ ràng & đục đường vòng (Braiding)
        eliminateObviousDeadEndsAndBraid(grid, width, height, config.braidPercentage, random)

        // 4. Tìm cặp điểm bắt đầu (Start) và kết thúc (End) có lộ trình dài nhất bằng Double BFS
        val (startIndex, endIndex) = findLongestPath(grid, width, height)

        // 5. Làm sạch cờ FLAG_VISITED để dữ liệu grid chỉ chứa 4 bit tường (0..15)
        for (i in 0 until totalCells) {
            grid[i] = (grid[i].toInt() and 0x0F).toByte()
            if (i % YIELD_INTERVAL == 0) {
                coroutineContext.ensureActive()
            }
        }

        MazeResult(
            grid = grid,
            startIndex = startIndex,
            endIndex = endIndex
        )
    }

    /**
     * Thuật toán Prim Cải tiến (Modified Randomized Prim's Algorithm).
     *
     * Mã hóa Frontier Edge trong 1 số Int:
     * - (fromIndex shl 2) or direction (0: TOP, 1: RIGHT, 2: BOTTOM, 3: LEFT)
     */
    private suspend fun generatePrimMaze(
        grid: ByteArray,
        width: Int,
        height: Int,
        corridorMomentum: Float,
        random: Random
    ) {
        val totalCells = width * height

        // Mảng động quản lý danh sách Frontier dạng IntArray (Zero Object Allocation)
        val maxFrontierSize = totalCells * 4
        val frontier = IntArray(maxFrontierSize)
        var frontierSize = 0

        fun addFrontierWalls(cellIndex: Int) {
            val cx = cellIndex % width
            val cy = cellIndex / width

            // TOP (y + 1)
            if (cy < height - 1) {
                val nIndex = (cy + 1) * width + cx
                if ((grid[nIndex].toInt() and FLAG_VISITED.toInt()) == 0) {
                    frontier[frontierSize++] = (cellIndex shl 2) or 0
                }
            }
            // RIGHT (x + 1)
            if (cx < width - 1) {
                val nIndex = cy * width + (cx + 1)
                if ((grid[nIndex].toInt() and FLAG_VISITED.toInt()) == 0) {
                    frontier[frontierSize++] = (cellIndex shl 2) or 1
                }
            }
            // BOTTOM (y - 1)
            if (cy > 0) {
                val nIndex = (cy - 1) * width + cx
                if ((grid[nIndex].toInt() and FLAG_VISITED.toInt()) == 0) {
                    frontier[frontierSize++] = (cellIndex shl 2) or 2
                }
            }
            // LEFT (x - 1)
            if (cx > 0) {
                val nIndex = cy * width + (cx - 1)
                if ((grid[nIndex].toInt() and FLAG_VISITED.toInt()) == 0) {
                    frontier[frontierSize++] = (cellIndex shl 2) or 3
                }
            }
        }

        // Bắt đầu từ 1 ô ngẫu nhiên
        val startCell = random.nextInt(totalCells)
        grid[startCell] = (grid[startCell].toInt() or FLAG_VISITED.toInt()).toByte()
        addFrontierWalls(startCell)

        var lastCarvedCell = startCell
        var lastDirection = -1
        var loopCounter = 0

        while (frontierSize > 0) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            // Quán tính hành lang (Corridor Momentum):
            // Kiểm tra xem từ ô vừa đục có thể tiếp tục đục theo cùng hướng hay không
            var chosenEdge = -1
            var chosenFrontierIndex = -1

            if (lastDirection != -1 && random.nextFloat() < corridorMomentum) {
                val lcx = lastCarvedCell % width
                val lcy = lastCarvedCell / width
                var nextTargetIndex = -1
                when (lastDirection) {
                    0 -> if (lcy < height - 1) nextTargetIndex = (lcy + 1) * width + lcx
                    1 -> if (lcx < width - 1) nextTargetIndex = lcy * width + (lcx + 1)
                    2 -> if (lcy > 0) nextTargetIndex = (lcy - 1) * width + lcx
                    3 -> if (lcx > 0) nextTargetIndex = lcy * width + (lcx - 1)
                }

                if (nextTargetIndex != -1 && (grid[nextTargetIndex].toInt() and FLAG_VISITED.toInt()) == 0) {
                    chosenEdge = (lastCarvedCell shl 2) or lastDirection
                }
            }

            // Nếu không có quán tính hoặc hướng cũ bị chặn, chọn ngẫu nhiên 1 cạnh từ Frontier
            if (chosenEdge == -1) {
                chosenFrontierIndex = random.nextInt(frontierSize)
                chosenEdge = frontier[chosenFrontierIndex]

                // Xóa phần tử đã chọn bằng cách hoán đổi với phần tử cuối (O(1))
                frontier[chosenFrontierIndex] = frontier[--frontierSize]
            }

            val fromIndex = chosenEdge shr 2
            val dir = chosenEdge and 3

            val fcx = fromIndex % width
            val fcy = fromIndex / width

            var toIndex = -1
            var wallToCarve: Byte = 0
            var oppWallToCarve: Byte = 0

            when (dir) {
                0 -> { // TOP
                    toIndex = (fcy + 1) * width + fcx
                    wallToCarve = WALL_TOP
                    oppWallToCarve = WALL_BOTTOM
                }
                1 -> { // RIGHT
                    toIndex = fcy * width + (fcx + 1)
                    wallToCarve = WALL_RIGHT
                    oppWallToCarve = WALL_LEFT
                }
                2 -> { // BOTTOM
                    toIndex = (fcy - 1) * width + fcx
                    wallToCarve = WALL_BOTTOM
                    oppWallToCarve = WALL_TOP
                }
                3 -> { // LEFT
                    toIndex = fcy * width + (fcx - 1)
                    wallToCarve = WALL_LEFT
                    oppWallToCarve = WALL_RIGHT
                }
            }

            // Chỉ đục nếu ô đích chưa nằm trong cây mê cung
            if (toIndex != -1 && (grid[toIndex].toInt() and FLAG_VISITED.toInt()) == 0) {
                // Phá tường giữa fromIndex và toIndex
                grid[fromIndex] = (grid[fromIndex].toInt() and wallToCarve.toInt().inv()).toByte()
                grid[toIndex] = (grid[toIndex].toInt() and oppWallToCarve.toInt().inv()).toByte()

                // Đánh dấu ô toIndex đã thuộc mê cung
                grid[toIndex] = (grid[toIndex].toInt() or FLAG_VISITED.toInt()).toByte()

                // Cập nhật trạng thái quán tính
                lastCarvedCell = toIndex
                lastDirection = dir

                // Bổ sung các bức tường xung quanh ô mới vào Frontier
                addFrontierWalls(toIndex)
            } else {
                lastDirection = -1
            }
        }
    }

    /**
     * Triệt tiêu các ngõ cụt ngắn nông hiển nhiên và áp dụng Braiding có kiểm soát.
     * Đảm bảo mọi nhánh rẽ trong mê cung đều sâu và thách thức, không thể nhìn ra lối cụt dễ dàng.
     */
    private suspend fun eliminateObviousDeadEndsAndBraid(
        grid: ByteArray,
        width: Int,
        height: Int,
        braidProbability: Float,
        random: Random
    ) {
        val totalCells = width * height
        var loopCounter = 0

        val candidateWalls = ByteArray(4)
        val candidateOpposites = ByteArray(4)
        val candidateNeighbors = IntArray(4)

        for (index in 0 until totalCells) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            val wallMask = grid[index].toInt() and 0x0F

            // Kiểm tra ngõ cụt (có đúng 3 bức tường đóng)
            if (Integer.bitCount(wallMask) == 3) {
                if (random.nextFloat() < braidProbability) {
                    val cx = index % width
                    val cy = index / width
                    var candidateCount = 0

                    // TOP Wall (y + 1)
                    if (cy < height - 1 && (wallMask and WALL_TOP.toInt()) != 0) {
                        candidateWalls[candidateCount] = WALL_TOP
                        candidateOpposites[candidateCount] = WALL_BOTTOM
                        candidateNeighbors[candidateCount] = (cy + 1) * width + cx
                        candidateCount++
                    }

                    // RIGHT Wall (x + 1)
                    if (cx < width - 1 && (wallMask and WALL_RIGHT.toInt()) != 0) {
                        candidateWalls[candidateCount] = WALL_RIGHT
                        candidateOpposites[candidateCount] = WALL_LEFT
                        candidateNeighbors[candidateCount] = cy * width + (cx + 1)
                        candidateCount++
                    }

                    // BOTTOM Wall (y - 1)
                    if (cy > 0 && (wallMask and WALL_BOTTOM.toInt()) != 0) {
                        candidateWalls[candidateCount] = WALL_BOTTOM
                        candidateOpposites[candidateCount] = WALL_TOP
                        candidateNeighbors[candidateCount] = (cy - 1) * width + cx
                        candidateCount++
                    }

                    // LEFT Wall (x - 1)
                    if (cx > 0 && (wallMask and WALL_LEFT.toInt()) != 0) {
                        candidateWalls[candidateCount] = WALL_LEFT
                        candidateOpposites[candidateCount] = WALL_RIGHT
                        candidateNeighbors[candidateCount] = cy * width + (cx - 1)
                        candidateCount++
                    }

                    if (candidateCount > 0) {
                        val pick = if (candidateCount == 1) 0 else random.nextInt(candidateCount)
                        val wall = candidateWalls[pick].toInt()
                        val opp = candidateOpposites[pick].toInt()
                        val neighbor = candidateNeighbors[pick]

                        // Đục tường để tạo vòng lặp, xóa bỏ ngõ cụt nông
                        grid[index] = (grid[index].toInt() and wall.inv()).toByte()
                        grid[neighbor] = (grid[neighbor].toInt() and opp.inv()).toByte()
                    }
                }
            }
        }
    }

    /**
     * Tìm lộ trình dài nhất trên đồ thị mê cung (Đường kính đồ thị) bằng thuật toán Double BFS.
     */
    private suspend fun findLongestPath(
        grid: ByteArray,
        width: Int,
        height: Int
    ): Pair<Int, Int> {
        val totalCells = width * height
        val queue = IntArray(totalCells)
        val dist = IntArray(totalCells)

        // BFS 1: Từ index 0 tìm Node A xa nhất
        dist.fill(-1)
        var head = 0
        var tail = 0

        queue[tail++] = 0
        dist[0] = 0

        var nodeA = 0
        var maxDistA = 0
        var loopCounter = 0

        while (head < tail) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            val curr = queue[head++]
            val d = dist[curr]
            if (d > maxDistA) {
                maxDistA = d
                nodeA = curr
            }

            val cx = curr % width
            val cy = curr / width
            val mask = grid[curr].toInt() and 0x0F

            if (cy < height - 1 && (mask and WALL_TOP.toInt()) == 0) {
                val nIndex = (cy + 1) * width + cx
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
            if (cx < width - 1 && (mask and WALL_RIGHT.toInt()) == 0) {
                val nIndex = cy * width + (cx + 1)
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
            if (cy > 0 && (mask and WALL_BOTTOM.toInt()) == 0) {
                val nIndex = (cy - 1) * width + cx
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
            if (cx > 0 && (mask and WALL_LEFT.toInt()) == 0) {
                val nIndex = cy * width + (cx - 1)
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
        }

        // BFS 2: Từ Node A tìm Node B xa nhất
        dist.fill(-1)
        head = 0
        tail = 0

        queue[tail++] = nodeA
        dist[nodeA] = 0

        var nodeB = nodeA
        var maxDistB = 0
        loopCounter = 0

        while (head < tail) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            val curr = queue[head++]
            val d = dist[curr]
            if (d > maxDistB) {
                maxDistB = d
                nodeB = curr
            }

            val cx = curr % width
            val cy = curr / width
            val mask = grid[curr].toInt() and 0x0F

            if (cy < height - 1 && (mask and WALL_TOP.toInt()) == 0) {
                val nIndex = (cy + 1) * width + cx
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
            if (cx < width - 1 && (mask and WALL_RIGHT.toInt()) == 0) {
                val nIndex = cy * width + (cx + 1)
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
            if (cy > 0 && (mask and WALL_BOTTOM.toInt()) == 0) {
                val nIndex = (cy - 1) * width + cx
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
            if (cx > 0 && (mask and WALL_LEFT.toInt()) == 0) {
                val nIndex = cy * width + (cx - 1)
                if (dist[nIndex] == -1) { dist[nIndex] = d + 1; queue[tail++] = nIndex }
            }
        }

        return Pair(nodeA, nodeB)
    }

    // =========================================================================
    // CÁC HÀM TIỆN ÍCH TƯƠNG THÍCH VÀ CƠ SỞ DỮ LIỆU ROOM
    // =========================================================================

    /**
     * Sinh cấu trúc màn chơi dựa trên LevelDef (đồng bộ với hệ thống cấp độ game & 3 thuật toán xoay vòng).
     */
    fun generateFromDef(def: LevelDef, seed: Long): MazeLevelData {
        val maze = MazeBuilder.buildMaze(
            w = def.w,
            h = def.h,
            target = def.target,
            seedBig = seed.toULong(),
            algorithmIndex = def.algorithmIndex
        )

        return MazeLevelData(
            levelId = def.id,
            width = def.w,
            height = def.h,
            targetSteps = def.target,
            start = maze.start,
            end = maze.goal,
            walls = maze.masks,
            optimalPath = maze.spine,
            seed = seed
        )
    }

    /**
     * Chuyển đổi đối tượng MazeLevelData thành Room Entity để lưu xuống SQLite.
     */
    fun toEntity(data: MazeLevelData): MazeLevelEntity {
        val wallsCsv = data.walls.joinToString(separator = ",")
        val pathCsv = data.optimalPath.joinToString(separator = ";") { "${it.x},${it.y}" }

        return MazeLevelEntity(
            levelId = data.levelId,
            width = data.width,
            height = data.height,
            targetSteps = data.targetSteps,
            startX = data.start.x,
            startY = data.start.y,
            endX = data.end.x,
            endY = data.end.y,
            wallsData = wallsCsv,
            optimalPath = pathCsv,
            seed = data.seed,
            createdAt = System.currentTimeMillis()
        )
    }

    /**
     * Phục hồi cấu trúc màn chơi từ Room Entity.
     */
    fun fromEntity(entity: MazeLevelEntity): MazeLevelData {
        val wallsArray = if (entity.wallsData.isNotEmpty()) {
            entity.wallsData.split(",").map { it.trim().toIntOrNull() ?: 0 }.toIntArray()
        } else {
            IntArray(entity.width * entity.height) { 0 }
        }

        val optimalList = if (entity.optimalPath.isNotEmpty()) {
            entity.optimalPath.split(";").mapNotNull { token ->
                val parts = token.split(",")
                if (parts.size == 2) {
                    val x = parts[0].trim().toIntOrNull()
                    val y = parts[1].trim().toIntOrNull()
                    if (x != null && y != null) Point(x, y) else null
                } else null
            }
        } else {
            emptyList()
        }

        return MazeLevelData(
            levelId = entity.levelId,
            width = entity.width,
            height = entity.height,
            targetSteps = entity.targetSteps,
            start = Point(entity.startX, entity.startY),
            end = Point(entity.endX, entity.endY),
            walls = wallsArray,
            optimalPath = optimalList,
            seed = entity.seed
        )
    }
}

/**
 * Dữ liệu cấu trúc logic của một màn chơi mê cung.
 */
data class MazeLevelData(
    val levelId: String,
    val width: Int,
    val height: Int,
    val targetSteps: Int,
    val start: Point,
    val end: Point,
    val walls: IntArray,
    val optimalPath: List<Point>,
    val seed: Long
) {
    fun cellAt(x: Int, y: Int): Int {
        if (x !in 0 until width || y !in 0 until height) return 0
        return walls[y * width + x]
    }

    fun isTileWalkable(x: Int, y: Int, dx: Int, dy: Int): Boolean {
        if (x !in 0 until width || y !in 0 until height) return false
        val nx = x + dx
        val ny = y + dy
        if (nx !in 0 until width || ny !in 0 until height) return false

        val dir = when {
            dx == 1 && dy == 0 -> EAST
            dx == -1 && dy == 0 -> WEST
            dx == 0 && dy == 1 -> NORTH
            dx == 0 && dy == -1 -> SOUTH
            else -> return false
        }

        val mask = cellAt(x, y)
        return (mask and dir) != 0
    }

    fun toMaze(): Maze {
        return Maze(
            w = width,
            h = height,
            masks = walls,
            start = start,
            goal = end,
            spine = optimalPath,
            target = targetSteps,
            seedStr = seed.toString()
        )
    }
}
