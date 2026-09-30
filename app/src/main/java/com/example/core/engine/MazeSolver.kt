package com.example.core.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

/**
 * Hệ thống giải và phân tích mê cung hiệu năng cao (Zero-Allocation A* & Dijkstra PathFinder).
 * Được tối ưu hóa đặc biệt cho bản đồ siêu lớn (lên tới 1000x1000 ô) với Min-Heap dạng mảng nguyên thủy.
 */
object MazePathFinder {

    private const val YIELD_INTERVAL = 10_000

    /**
     * Tìm đường đi ngắn nhất giữa start và end bằng thuật toán A* Search hiệu năng cao.
     * Sử dụng 1D Binary Min-Heap cấp phát sẵn, không tạo Object Node/Cell.
     *
     * @param grid Mảng 1D ByteArray chứa bitmask 4 bức tường.
     * @param width Chiều rộng lưới.
     * @param height Chiều cao lưới.
     * @param startIndex Tọa độ 1D điểm xuất phát.
     * @param endIndex Tọa độ 1D điểm đích.
     * @return Mảng IntArray chứa chuỗi các chỉ số index 1D từ start đến end, hoặc rỗng nếu không có đường.
     */
    suspend fun solveAStar(
        grid: ByteArray,
        width: Int,
        height: Int,
        startIndex: Int,
        endIndex: Int
    ): IntArray = withContext(Dispatchers.Default) {
        val totalCells = width * height
        if (startIndex !in 0 until totalCells || endIndex !in 0 until totalCells) {
            return@withContext IntArray(0)
        }
        if (startIndex == endIndex) {
            return@withContext intArrayOf(startIndex)
        }

        val targetX = endIndex % width
        val targetY = endIndex / width

        // Mảng lưu trữ khoảng cách thực tế (gCost) từ start tới từng ô (khởi tạo MAX_VALUE)
        val gCost = IntArray(totalCells) { Int.MAX_VALUE }
        // Mảng lưu vết ô trước đó để phục hồi đường đi (cameFrom)
        val cameFrom = IntArray(totalCells) { -1 }
        // Mảng đánh dấu ô đã đóng (closed set)
        val closed = BooleanArray(totalCells)

        // Custom Binary Min-Heap quản lý các cặp (fCost, cellIndex)
        // Lưu trữ trong 2 mảng nguyên thủy song song: heapCost và heapIndex
        val maxHeapCapacity = totalCells
        val heapCost = IntArray(maxHeapCapacity)
        val heapIndex = IntArray(maxHeapCapacity)
        var heapSize = 0

        fun pushHeap(cost: Int, index: Int) {
            var i = heapSize++
            heapCost[i] = cost
            heapIndex[i] = index
            // Sift Up
            while (i > 0) {
                val parent = (i - 1) shr 1
                if (heapCost[i] < heapCost[parent]) {
                    // Swap
                    val tc = heapCost[i]; heapCost[i] = heapCost[parent]; heapCost[parent] = tc
                    val ti = heapIndex[i]; heapIndex[i] = heapIndex[parent]; heapIndex[parent] = ti
                    i = parent
                } else break
            }
        }

        fun popHeap(): Int {
            val topIndex = heapIndex[0]
            val last = --heapSize
            heapCost[0] = heapCost[last]
            heapIndex[0] = heapIndex[last]

            // Sift Down
            var i = 0
            while (true) {
                val left = (i shl 1) + 1
                val right = left + 1
                var smallest = i

                if (left < heapSize && heapCost[left] < heapCost[smallest]) {
                    smallest = left
                }
                if (right < heapSize && heapCost[right] < heapCost[smallest]) {
                    smallest = right
                }
                if (smallest != i) {
                    val tc = heapCost[i]; heapCost[i] = heapCost[smallest]; heapCost[smallest] = tc
                    val ti = heapIndex[i]; heapIndex[i] = heapIndex[smallest]; heapIndex[smallest] = ti
                    i = smallest
                } else break
            }
            return topIndex
        }

        // Khởi tạo điểm bắt đầu
        gCost[startIndex] = 0
        val initialH = abs(startIndex % width - targetX) + abs(startIndex / width - targetY)
        pushHeap(initialH, startIndex)

        var loopCounter = 0

        while (heapSize > 0) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            val current = popHeap()
            if (current == endIndex) break
            if (closed[current]) continue
            closed[current] = true

            val cx = current % width
            val cy = current / width
            val currentG = gCost[current]
            val mask = grid[current].toInt() and 0x0F

            // Kiểm tra 4 hướng di chuyển dựa trên Bitmask tường
            // TOP (y - 1)
            if (cy > 0 && (mask and MazeGenerator.WALL_TOP.toInt()) == 0) {
                val neighbor = (cy - 1) * width + cx
                if (!closed[neighbor]) {
                    val tentativeG = currentG + 1
                    if (tentativeG < gCost[neighbor]) {
                        gCost[neighbor] = tentativeG
                        cameFrom[neighbor] = current
                        val h = abs(cx - targetX) + abs((cy - 1) - targetY)
                        pushHeap(tentativeG + h, neighbor)
                    }
                }
            }

            // RIGHT (x + 1)
            if (cx < width - 1 && (mask and MazeGenerator.WALL_RIGHT.toInt()) == 0) {
                val neighbor = cy * width + (cx + 1)
                if (!closed[neighbor]) {
                    val tentativeG = currentG + 1
                    if (tentativeG < gCost[neighbor]) {
                        gCost[neighbor] = tentativeG
                        cameFrom[neighbor] = current
                        val h = abs((cx + 1) - targetX) + abs(cy - targetY)
                        pushHeap(tentativeG + h, neighbor)
                    }
                }
            }

            // BOTTOM (y + 1)
            if (cy < height - 1 && (mask and MazeGenerator.WALL_BOTTOM.toInt()) == 0) {
                val neighbor = (cy + 1) * width + cx
                if (!closed[neighbor]) {
                    val tentativeG = currentG + 1
                    if (tentativeG < gCost[neighbor]) {
                        gCost[neighbor] = tentativeG
                        cameFrom[neighbor] = current
                        val h = abs(cx - targetX) + abs((cy + 1) - targetY)
                        pushHeap(tentativeG + h, neighbor)
                    }
                }
            }

            // LEFT (x - 1)
            if (cx > 0 && (mask and MazeGenerator.WALL_LEFT.toInt()) == 0) {
                val neighbor = cy * width + (cx - 1)
                if (!closed[neighbor]) {
                    val tentativeG = currentG + 1
                    if (tentativeG < gCost[neighbor]) {
                        gCost[neighbor] = tentativeG
                        cameFrom[neighbor] = current
                        val h = abs((cx - 1) - targetX) + abs(cy - targetY)
                        pushHeap(tentativeG + h, neighbor)
                    }
                }
            }
        }

        // Tái tạo đường đi từ cameFrom
        if (cameFrom[endIndex] == -1 && startIndex != endIndex) {
            return@withContext IntArray(0) // Không tìm thấy đường
        }

        // Đếm chiều dài đường đi
        var len = 1
        var curr = endIndex
        while (curr != startIndex && cameFrom[curr] != -1) {
            len++
            curr = cameFrom[curr]
        }

        val path = IntArray(len)
        var pIdx = len - 1
        curr = endIndex
        while (pIdx >= 0) {
            path[pIdx--] = curr
            if (curr == startIndex) break
            curr = cameFrom[curr]
        }

        path
    }

    /**
     * Tính toán trường khoảng cách (Dijkstra Distance Field / Heatmap) từ 1 ô đích tới toàn bộ bản đồ.
     * Dùng để ước lượng độ khó, tạo gợi ý động, và hiển thị bản đồ nhiệt bước đi.
     */
    suspend fun computeDistanceField(
        grid: ByteArray,
        width: Int,
        height: Int,
        targetIndex: Int
    ): IntArray = withContext(Dispatchers.Default) {
        val totalCells = width * height
        val dist = IntArray(totalCells) { -1 }
        val queue = IntArray(totalCells)
        var head = 0
        var tail = 0

        queue[tail++] = targetIndex
        dist[targetIndex] = 0

        var loopCounter = 0

        while (head < tail) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            val curr = queue[head++]
            val d = dist[curr]
            val cx = curr % width
            val cy = curr / width
            val mask = grid[curr].toInt() and 0x0F

            if (cy > 0 && (mask and MazeGenerator.WALL_TOP.toInt()) == 0) {
                val n = (cy - 1) * width + cx
                if (dist[n] == -1) {
                    dist[n] = d + 1
                    queue[tail++] = n
                }
            }
            if (cx < width - 1 && (mask and MazeGenerator.WALL_RIGHT.toInt()) == 0) {
                val n = cy * width + (cx + 1)
                if (dist[n] == -1) {
                    dist[n] = d + 1
                    queue[tail++] = n
                }
            }
            if (cy < height - 1 && (mask and MazeGenerator.WALL_BOTTOM.toInt()) == 0) {
                val n = (cy + 1) * width + cx
                if (dist[n] == -1) {
                    dist[n] = d + 1
                    queue[tail++] = n
                }
            }
            if (cx > 0 && (mask and MazeGenerator.WALL_LEFT.toInt()) == 0) {
                val n = cy * width + (cx - 1)
                if (dist[n] == -1) {
                    dist[n] = d + 1
                    queue[tail++] = n
                }
            }
        }

        dist
    }

    /**
     * Phân tích các chỉ số độ khó của mê cung:
     * - Tỷ lệ ngõ cụt (Dead-end ratio)
     * - Chiều dài đường tối ưu (Optimal path length)
     * - Độ phức tạp phân nhánh (Branching factor)
     */
    fun analyzeDifficulty(
        grid: ByteArray,
        width: Int,
        height: Int,
        optimalLength: Int
    ): MazeMetrics {
        val totalCells = width * height
        var deadEndCount = 0
        var junctionCount = 0
        var totalCorridorLength = 0

        for (i in 0 until totalCells) {
            val mask = grid[i].toInt() and 0x0F
            val wallCount = Integer.bitCount(mask)
            when (wallCount) {
                3 -> deadEndCount++ // Ngõ cụt (chỉ 1 lối mở)
                2 -> totalCorridorLength++ // Hành lang thẳng hoặc góc cua (2 lối mở)
                0, 1 -> junctionCount++ // Ngã ba hoặc ngã tư (3 hoặc 4 lối mở)
            }
        }

        val deadEndRatio = deadEndCount.toFloat() / totalCells
        val branchingRatio = junctionCount.toFloat() / totalCells
        val difficultyScore = (optimalLength * 0.4f + deadEndCount * 1.5f + junctionCount * 0.8f) / totalCells * 100f

        return MazeMetrics(
            totalCells = totalCells,
            deadEndCount = deadEndCount,
            deadEndRatio = deadEndRatio,
            junctionCount = junctionCount,
            branchingRatio = branchingRatio,
            optimalPathLength = optimalLength,
            difficultyScore = difficultyScore.coerceIn(1f, 100f)
        )
    }
}

/**
 * Bộ chỉ số phân tích cấu trúc mê cung.
 */
data class MazeMetrics(
    val totalCells: Int,
    val deadEndCount: Int,
    val deadEndRatio: Float,
    val junctionCount: Int,
    val branchingRatio: Float,
    val optimalPathLength: Int,
    val difficultyScore: Float
)
