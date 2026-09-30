package com.example.core.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

/**
 * Các họ thuật toán sinh mê cung thủ tục (Procedural Content Generation Algorithms).
 */
enum class MazeAlgorithmType(val displayName: String, val description: String) {
    RECURSIVE_BACKTRACKER("DFS Backtracker", "Đường đi uốn lượn sâu, ngõ cụt dài, độ khó cao nhất"),
    WILSONS("Wilson's Algorithm", "Cây bao trùm đồng nhất hoàn hảo (Uniform Spanning Tree), phân bổ hoàn toàn không thiên vị"),
    KRUSKAL("Kruskal's MST", "Nhiều ngõ rẽ ngắn, cấu trúc cành cây phân nhánh rậm rạp"),
    ELLERS("Eller's Streaming", "Sinh mê cung từng hàng vô hạn với bộ nhớ O(Width), phù hợp bản đồ siêu dài"),
    PRIM("Prim's Algorithm", "Cấu trúc tỏa tia từ tâm, lối đi chính tỏa đều các hướng")
}

/**
 * Bộ thư viện thuật toán sinh mê cung đa hình thái (PCG Algorithm Suite).
 * Triển khai đầy đủ các thuật toán kinh điển thế giới với tiêu chuẩn Zero-Allocation trên Android.
 */
object MazeAlgorithms {

    private const val YIELD_INTERVAL = 10_000

    /**
     * Sinh mê cung bằng thuật toán Kruskal ngẫu nhiên kết hợp Disjoint-Set Union (DSU) 1D.
     */
    suspend fun generateKruskal(
        width: Int,
        height: Int,
        seed: Long
    ): ByteArray = withContext(Dispatchers.Default) {
        val totalCells = width * height
        val random = Random(seed)
        val grid = ByteArray(totalCells) { MazeGenerator.WALLS_ALL }

        // DSU Parent Array: Mỗi ô ban đầu là tập hợp riêng của chính nó
        val parent = IntArray(totalCells) { it }

        fun find(i: Int): Int {
            var root = i
            while (root != parent[root]) {
                root = parent[root]
            }
            // Path compression
            var curr = i
            while (curr != root) {
                val next = parent[curr]
                parent[curr] = root
                curr = next
            }
            return root
        }

        fun union(i: Int, j: Int): Boolean {
            val rootI = find(i)
            val rootJ = find(j)
            if (rootI != rootJ) {
                parent[rootI] = rootJ
                return true
            }
            return false
        }

        // Danh sách tất cả các bức tường nội bộ: Encode thành 1 số Int (cellIndex shl 2 or direction)
        // Số cạnh tối đa ~ 2 * width * height
        val totalWalls = (width - 1) * height + width * (height - 1)
        val edges = IntArray(totalWalls)
        var edgeCount = 0

        // Thu thập các cạnh ngang (RIGHT) và dọc (BOTTOM)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                if (x < width - 1) {
                    edges[edgeCount++] = (idx shl 1) or 0 // 0 = RIGHT
                }
                if (y < height - 1) {
                    edges[edgeCount++] = (idx shl 1) or 1 // 1 = BOTTOM
                }
            }
        }

        // Trộn ngẫu nhiên danh sách cạnh (Fisher-Yates Shuffle)
        for (i in edgeCount - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = edges[i]
            edges[i] = edges[j]
            edges[j] = temp
        }

        var carvedEdges = 0
        val targetEdges = totalCells - 1
        var loopCounter = 0

        for (e in 0 until edgeCount) {
            if (++loopCounter >= YIELD_INTERVAL) {
                loopCounter = 0
                coroutineContext.ensureActive()
                yield()
            }

            if (carvedEdges >= targetEdges) break

            val edgeVal = edges[e]
            val cell = edgeVal ushr 1
            val isBottom = (edgeVal and 1) == 1

            val cx = cell % width
            val cy = cell / width

            val neighbor = if (isBottom) (cy + 1) * width + cx else cy * width + (cx + 1)
            val wall = if (isBottom) MazeGenerator.WALL_BOTTOM else MazeGenerator.WALL_RIGHT
            val opp = if (isBottom) MazeGenerator.WALL_TOP else MazeGenerator.WALL_LEFT

            if (union(cell, neighbor)) {
                grid[cell] = (grid[cell].toInt() and wall.toInt().inv()).toByte()
                grid[neighbor] = (grid[neighbor].toInt() and opp.toInt().inv()).toByte()
                carvedEdges++
            }
        }

        grid
    }

    /**
     * Sinh mê cung bằng thuật toán Eller (Streaming Row-by-Row).
     * Bộ nhớ chỉ tốn O(Width), cực kỳ tối ưu cho các màn hình vô tận.
     */
    suspend fun generateEllers(
        width: Int,
        height: Int,
        seed: Long
    ): ByteArray = withContext(Dispatchers.Default) {
        val totalCells = width * height
        val random = Random(seed)
        val grid = ByteArray(totalCells) { MazeGenerator.WALLS_ALL }

        val setRow = IntArray(width) { 0 }
        var nextSetId = 1

        for (y in 0 until height) {
            coroutineContext.ensureActive()

            // 1. Gán ID tập hợp mới cho các ô chưa có nhóm
            for (x in 0 until width) {
                if (setRow[x] == 0) {
                    setRow[x] = nextSetId++
                }
            }

            // 2. Nối ngẫu nhiên các ô liền kề cùng hàng (Right connections)
            val isLastRow = (y == height - 1)
            for (x in 0 until width - 1) {
                val shouldJoin = if (isLastRow) {
                    setRow[x] != setRow[x + 1]
                } else {
                    setRow[x] != setRow[x + 1] && random.nextBoolean()
                }

                if (shouldJoin) {
                    val oldSet = setRow[x + 1]
                    val newSet = setRow[x]
                    // Hợp nhất tập hợp trên hàng hiện tại
                    for (k in 0 until width) {
                        if (setRow[k] == oldSet) {
                            setRow[k] = newSet
                        }
                    }

                    // Phá tường RIGHT giữa ô x và x + 1
                    val currIdx = y * width + x
                    val rightIdx = y * width + (x + 1)
                    grid[currIdx] = (grid[currIdx].toInt() and MazeGenerator.WALL_RIGHT.toInt().inv()).toByte()
                    grid[rightIdx] = (grid[rightIdx].toInt() and MazeGenerator.WALL_LEFT.toInt().inv()).toByte()
                }
            }

            // 3. Tạo đường dọc xuống hàng dưới (Bottom connections)
            if (!isLastRow) {
                val nextSetRow = IntArray(width) { 0 }
                // Đếm số ô của từng tập hợp trên hàng
                var startX = 0
                while (startX < width) {
                    val currentSet = setRow[startX]
                    // Tìm tất cả các ô thuộc cùng tập hợp currentSet
                    var sameSetCount = 0
                    val sameSetIndices = IntArray(width)
                    for (x in 0 until width) {
                        if (setRow[x] == currentSet) {
                            sameSetIndices[sameSetCount++] = x
                        }
                    }

                    // Đảm bảo ít nhất 1 ô trong tập hợp có đường nối xuống dưới
                    val atLeastOne = random.nextInt(sameSetCount)
                    for (i in 0 until sameSetCount) {
                        val col = sameSetIndices[i]
                        if (i == atLeastOne || random.nextBoolean()) {
                            val currIdx = y * width + col
                            val bottomIdx = (y + 1) * width + col
                            grid[currIdx] = (grid[currIdx].toInt() and MazeGenerator.WALL_BOTTOM.toInt().inv()).toByte()
                            grid[bottomIdx] = (grid[bottomIdx].toInt() and MazeGenerator.WALL_TOP.toInt().inv()).toByte()
                            nextSetRow[col] = currentSet
                        }
                    }

                    startX++
                }

                for (x in 0 until width) {
                    setRow[x] = nextSetRow[x]
                }
            }
        }

        grid
    }
}
