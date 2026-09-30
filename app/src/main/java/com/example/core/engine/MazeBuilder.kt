package com.example.core.engine

import java.util.ArrayDeque
import kotlin.random.Random

/**
 * MazeBuilder - Hệ thống sinh mê cung thế hệ mới chia làm 3 cấp độ thuật toán chuyên biệt:
 *
 * 1. Cấp độ nhỏ (5x5 đến 20x20): Thuật toán Quay lui (Recursive Backtracking with Stack)
 *    - Tạo ra những "dòng sông" dài, hẹp, uốn lượn zíc-zắc sâu vào tận cùng trước khi chạm ngõ cụt.
 *    - Lời giải chiếm phần lớn diện tích mê cung, ít ngã rẽ ngắn, thách thức kiên nhẫn và định hướng.
 *
 * 2. Cấp độ trung bình (21x21 đến 50x50): Thuật toán Kruskal ngẫu nhiên (Randomized Kruskal with Disjoint Set)
 *    - Mê cung có tính phân dạng (fractal) với vô số ngõ cụt ngắn và trung bình đan xen chằng chịt.
 *    - Hệ số phân nhánh cực cao, không có đường chính nổi bật, mắt người rất khó tìm ra lối đi bằng cách nhìn lướt.
 *
 * 3. Cấp độ lớn (Trên 50x50): Thuật toán Wilson (Uniform Spanning Tree) & Thuật toán Eller (Row-by-Row O(N))
 *    - Wilson (51x51 đến 80x80): Cây bao trùm đồng nhất qua bước đi ngẫu nhiên xóa vòng lặp (Loop-Erased Random Walk),
 *      hoàn toàn không có thiên vị thị giác, hỗn loạn hoàn hảo.
 *    - Eller (>80x80): Sinh mê cung từng hàng một với bộ nhớ O(N), tối ưu tuyệt đối hiệu suất, không bao giờ tràn RAM.
 */
object MazeBuilder {

    const val WALL_TOP: Byte = 1        // Bit 0 (0000 0001) - Tường trên (y + 1)
    const val WALL_RIGHT: Byte = 2      // Bit 1 (0000 0010) - Tường phải (x + 1)
    const val WALL_BOTTOM: Byte = 4     // Bit 2 (0000 0100) - Tường dưới (y - 1)
    const val WALL_LEFT: Byte = 8       // Bit 3 (0000 1000) - Tường trái (x - 1)
    const val WALLS_ALL: Byte = 15      // 1 | 2 | 4 | 8 (0000 1111) - Khởi tạo cả 4 bức tường đều đóng

    fun randomSeed(): ULong {
        return Random.nextLong().toULong()
    }

    /**
     * Điều hướng và áp dụng thuật toán tạo mê cung theo `algorithmIndex` (1, 2, 3) cho mọi kích cỡ bản đồ:
     * - Thuật toán 1 (`algorithmIndex == 1`): Quay lui (Recursive Backtracking with Stack) - Hành lang dài uốn lượn, Start ở Tâm -> Đích ở Góc xa nhất.
     * - Thuật toán 2 (`algorithmIndex == 2`): Kruskal ngẫu nhiên (Randomized Kruskal - giống màn 40) - Phân nhánh chằng chịt fractal, Start cạnh Trái -> Đích cạnh Phải.
     * - Thuật toán 3 (`algorithmIndex == 3`): Wilson Loop-Erased Random Walk (giống màn 60x60, hoặc Eller nếu >80x80) - Cây bao trùm đồng nhất + cặp điểm sát vách ngăn có đường vòng dài nhất.
     */
    fun buildMaze(
        w: Int,
        h: Int,
        target: Int,
        seedBig: ULong,
        algorithmIndex: Int = 0
    ): Maze {
        val totalCells = w * h
        val maxDim = maxOf(w, h)
        val resolvedAlg = when (algorithmIndex) {
            1, 2, 3 -> algorithmIndex
            else -> when {
                maxDim <= 20 -> 1
                maxDim <= 50 -> 2
                else -> 3
            }
        }
        // Kết hợp algorithmIndex vào seed để 3 màn cùng kích thước (VD: 5x5 ở Level 1, 2, 3) luôn sinh cấu trúc hoàn toàn khác biệt
        val effectiveSeed = seedBig.toLong() xor (resolvedAlg.toLong() * 0x9E3779B97F4A7C15UL.toLong())
        val random = Random(effectiveSeed)

        val grid: ByteArray = when (resolvedAlg) {
            1 -> generateBacktracking(w, h, random)
            2 -> generateKruskal(w, h, random)
            else -> if (maxDim <= 80) generateWilson(w, h, random) else generateEller(w, h, random)
        }

        // Chuyển đổi bitmask sang chuẩn Game Mask (Bit = 1 nghĩa là hướng đi mở thông suốt)
        val masks = IntArray(totalCells)
        for (i in 0 until totalCells) {
            val cellWall = grid[i].toInt() and 0x0F
            var openMask = 0
            if ((cellWall and WALL_TOP.toInt()) == 0) openMask = openMask or NORTH
            if ((cellWall and WALL_RIGHT.toInt()) == 0) openMask = openMask or EAST
            if ((cellWall and WALL_BOTTOM.toInt()) == 0) openMask = openMask or SOUTH
            if ((cellWall and WALL_LEFT.toInt()) == 0) openMask = openMask or WEST
            masks[i] = openMask
        }

        // Xác định vị trí Start và Goal khó nhất theo từng thuật toán (1, 2, 3) & Double BFS
        val (startIdx, goalIdx) = determineOptimalEndpoints(masks, w, h, resolvedAlg, random)
        val start = Point(startIdx % w, startIdx / w)
        val goal = Point(goalIdx % w, goalIdx / w)

        // Tìm đường đi tối ưu đến Lá Cờ Đỏ
        val spine = findShortestPath(masks, w, h, startIdx, goalIdx)

        return Maze(
            w = w,
            h = h,
            masks = masks,
            start = start,
            goal = goal,
            spine = spine,
            target = spine.size - 1,
            seedStr = seedBig.toString()
        )
    }

    // =========================================================================
    // 1. CẤP ĐỘ NHỎ (5x5 ĐẾN 20x20): THUẬT TOÁN QUAY LUI (BACKTRACKING WITH STACK)
    // =========================================================================
    fun generateBacktracking(w: Int, h: Int, random: Random): ByteArray {
        val totalCells = w * h
        val grid = ByteArray(totalCells) { WALLS_ALL }
        val visited = BooleanArray(totalCells)
        val stack = IntArray(totalCells)
        var stackSize = 0

        val startX = random.nextInt(w)
        val startY = random.nextInt(h)
        val startIdx = startY * w + startX

        visited[startIdx] = true
        stack[stackSize++] = startIdx

        val neighbors = IntArray(4)
        val dirs = IntArray(4)

        while (stackSize > 0) {
            val curr = stack[stackSize - 1]
            val cx = curr % w
            val cy = curr / w

            var count = 0
            // TOP (y + 1)
            if (cy < h - 1 && !visited[(cy + 1) * w + cx]) {
                neighbors[count] = (cy + 1) * w + cx
                dirs[count] = 0
                count++
            }
            // RIGHT (x + 1)
            if (cx < w - 1 && !visited[cy * w + (cx + 1)]) {
                neighbors[count] = cy * w + (cx + 1)
                dirs[count] = 1
                count++
            }
            // BOTTOM (y - 1)
            if (cy > 0 && !visited[(cy - 1) * w + cx]) {
                neighbors[count] = (cy - 1) * w + cx
                dirs[count] = 2
                count++
            }
            // LEFT (x - 1)
            if (cx > 0 && !visited[cy * w + (cx - 1)]) {
                neighbors[count] = cy * w + (cx - 1)
                dirs[count] = 3
                count++
            }

            if (count > 0) {
                val pick = random.nextInt(count)
                val nextIdx = neighbors[pick]
                val dir = dirs[pick]

                when (dir) {
                    0 -> { // TOP
                        grid[curr] = (grid[curr].toInt() and WALL_TOP.toInt().inv()).toByte()
                        grid[nextIdx] = (grid[nextIdx].toInt() and WALL_BOTTOM.toInt().inv()).toByte()
                    }
                    1 -> { // RIGHT
                        grid[curr] = (grid[curr].toInt() and WALL_RIGHT.toInt().inv()).toByte()
                        grid[nextIdx] = (grid[nextIdx].toInt() and WALL_LEFT.toInt().inv()).toByte()
                    }
                    2 -> { // BOTTOM
                        grid[curr] = (grid[curr].toInt() and WALL_BOTTOM.toInt().inv()).toByte()
                        grid[nextIdx] = (grid[nextIdx].toInt() and WALL_TOP.toInt().inv()).toByte()
                    }
                    3 -> { // LEFT
                        grid[curr] = (grid[curr].toInt() and WALL_LEFT.toInt().inv()).toByte()
                        grid[nextIdx] = (grid[nextIdx].toInt() and WALL_RIGHT.toInt().inv()).toByte()
                    }
                }

                visited[nextIdx] = true
                stack[stackSize++] = nextIdx
            } else {
                stackSize--
            }
        }
        return grid
    }

    // =========================================================================
    // 2. CẤP ĐỘ TRUNG BÌNH (21x21 ĐẾN 50x50): KRUSKAL NGẪU NHIÊN (DISJOINT SET)
    // =========================================================================
    fun generateKruskal(w: Int, h: Int, random: Random): ByteArray {
        val totalCells = w * h
        val grid = ByteArray(totalCells) { WALLS_ALL }
        val parent = IntArray(totalCells) { it }

        fun find(i: Int): Int {
            var root = i
            while (root != parent[root]) {
                root = parent[root]
            }
            var curr = i
            while (curr != root) {
                val nxt = parent[curr]
                parent[curr] = root
                curr = nxt
            }
            return root
        }

        // Thu thập tất cả bức tường bên trong lưới
        val totalEdges = (w - 1) * h + w * (h - 1)
        val edgeFrom = IntArray(totalEdges)
        val edgeTo = IntArray(totalEdges)
        val edgeDir = IntArray(totalEdges) // 0: TOP (y -> y+1), 1: RIGHT (x -> x+1)
        var edgeCount = 0

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                if (x < w - 1) {
                    edgeFrom[edgeCount] = idx
                    edgeTo[edgeCount] = y * w + (x + 1)
                    edgeDir[edgeCount] = 1
                    edgeCount++
                }
                if (y < h - 1) {
                    edgeFrom[edgeCount] = idx
                    edgeTo[edgeCount] = (y + 1) * w + x
                    edgeDir[edgeCount] = 0
                    edgeCount++
                }
            }
        }

        // Xáo trộn ngẫu nhiên danh sách cạnh (Fisher-Yates Shuffle)
        for (i in edgeCount - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tf = edgeFrom[i]; edgeFrom[i] = edgeFrom[j]; edgeFrom[j] = tf
            val tt = edgeTo[i]; edgeTo[i] = edgeTo[j]; edgeTo[j] = tt
            val td = edgeDir[i]; edgeDir[i] = edgeDir[j]; edgeDir[j] = td
        }

        for (i in 0 until edgeCount) {
            val id1 = edgeFrom[i]
            val id2 = edgeTo[i]
            val root1 = find(id1)
            val root2 = find(id2)

            if (root1 != root2) {
                val dir = edgeDir[i]
                if (dir == 1) { // RIGHT
                    grid[id1] = (grid[id1].toInt() and WALL_RIGHT.toInt().inv()).toByte()
                    grid[id2] = (grid[id2].toInt() and WALL_LEFT.toInt().inv()).toByte()
                } else { // TOP
                    grid[id1] = (grid[id1].toInt() and WALL_TOP.toInt().inv()).toByte()
                    grid[id2] = (grid[id2].toInt() and WALL_BOTTOM.toInt().inv()).toByte()
                }
                parent[root1] = root2
            }
        }
        return grid
    }

    // =========================================================================
    // 3. CẤP ĐỘ LỚN (51x51 ĐẾN 80x80): THUẬT TOÁN WILSON (LOOP-ERASED RANDOM WALK)
    // =========================================================================
    fun generateWilson(w: Int, h: Int, random: Random): ByteArray {
        val totalCells = w * h
        val grid = ByteArray(totalCells) { WALLS_ALL }
        val inMaze = BooleanArray(totalCells)
        val walkDir = IntArray(totalCells) { -1 }

        // Chọn 1 ô ngẫu nhiên đầu tiên để đưa vào mê cung
        val startCell = random.nextInt(totalCells)
        inMaze[startCell] = true

        val unvisitedIndices = IntArray(totalCells) { it }
        var unvisitedCount = totalCells
        // Đưa startCell về cuối và giảm kích thước
        unvisitedIndices[startCell] = unvisitedIndices[totalCells - 1]
        unvisitedCount--

        val candidateDirs = IntArray(4)
        val candidateNeighbors = IntArray(4)

        while (unvisitedCount > 0) {
            // Lấy ngẫu nhiên 1 ô chưa thuộc mê cung
            var pick = random.nextInt(unvisitedCount)
            var startWalk = unvisitedIndices[pick]
            while (inMaze[startWalk]) {
                unvisitedIndices[pick] = unvisitedIndices[unvisitedCount - 1]
                unvisitedCount--
                if (unvisitedCount == 0) break
                pick = random.nextInt(unvisitedCount)
                startWalk = unvisitedIndices[pick]
            }
            if (unvisitedCount == 0) break

            var curr = startWalk
            // Bước đi ngẫu nhiên xóa vòng lặp cho đến khi chạm vào cây mê cung đã có
            while (!inMaze[curr]) {
                val cx = curr % w
                val cy = curr / w

                var validDirCount = 0
                if (cy < h - 1) {
                    candidateDirs[validDirCount] = 0 // TOP
                    candidateNeighbors[validDirCount] = (cy + 1) * w + cx
                    validDirCount++
                }
                if (cx < w - 1) {
                    candidateDirs[validDirCount] = 1 // RIGHT
                    candidateNeighbors[validDirCount] = cy * w + (cx + 1)
                    validDirCount++
                }
                if (cy > 0) {
                    candidateDirs[validDirCount] = 2 // BOTTOM
                    candidateNeighbors[validDirCount] = (cy - 1) * w + cx
                    validDirCount++
                }
                if (cx > 0) {
                    candidateDirs[validDirCount] = 3 // LEFT
                    candidateNeighbors[validDirCount] = cy * w + (cx - 1)
                    validDirCount++
                }

                val dIndex = random.nextInt(validDirCount)
                walkDir[curr] = candidateDirs[dIndex]
                curr = candidateNeighbors[dIndex]
            }

            // Retrace và phá vỡ tường để ghép nhánh mới vào mê cung
            var trace = startWalk
            while (!inMaze[trace]) {
                inMaze[trace] = true
                val dir = walkDir[trace]
                val tcx = trace % w
                val tcy = trace / w
                var nextCell = -1

                when (dir) {
                    0 -> {
                        nextCell = (tcy + 1) * w + tcx
                        grid[trace] = (grid[trace].toInt() and WALL_TOP.toInt().inv()).toByte()
                        grid[nextCell] = (grid[nextCell].toInt() and WALL_BOTTOM.toInt().inv()).toByte()
                    }
                    1 -> {
                        nextCell = tcy * w + (tcx + 1)
                        grid[trace] = (grid[trace].toInt() and WALL_RIGHT.toInt().inv()).toByte()
                        grid[nextCell] = (grid[nextCell].toInt() and WALL_LEFT.toInt().inv()).toByte()
                    }
                    2 -> {
                        nextCell = (tcy - 1) * w + tcx
                        grid[trace] = (grid[trace].toInt() and WALL_BOTTOM.toInt().inv()).toByte()
                        grid[nextCell] = (grid[nextCell].toInt() and WALL_TOP.toInt().inv()).toByte()
                    }
                    3 -> {
                        nextCell = tcy * w + (tcx - 1)
                        grid[trace] = (grid[trace].toInt() and WALL_LEFT.toInt().inv()).toByte()
                        grid[nextCell] = (grid[nextCell].toInt() and WALL_RIGHT.toInt().inv()).toByte()
                    }
                }
                trace = nextCell
            }
        }
        return grid
    }

    // =========================================================================
    // 4. CẤP ĐỘ SIÊU KHỔNG LỒ (>80x80): THUẬT TOÁN ELLER (ROW-BY-ROW O(N))
    // =========================================================================
    fun generateEller(w: Int, h: Int, random: Random): ByteArray {
        val totalCells = w * h
        val grid = ByteArray(totalCells) { WALLS_ALL }
        val rowSets = IntArray(w)
        var nextSetId = 1

        for (y in 0 until h) {
            // 1. Gán set ID cho các ô chưa có set
            for (x in 0 until w) {
                if (rowSets[x] == 0) {
                    rowSets[x] = nextSetId++
                }
            }

            val isLastRow = (y == h - 1)

            // 2. Nối ngang các ô liền kề nếu khác set
            for (x in 0 until w - 1) {
                val currentSet = rowSets[x]
                val nextSet = rowSets[x + 1]

                if (currentSet != nextSet) {
                    val shouldMerge = isLastRow || random.nextBoolean()
                    if (shouldMerge) {
                        val currIdx = y * w + x
                        val nextIdx = y * w + (x + 1)
                        grid[currIdx] = (grid[currIdx].toInt() and WALL_RIGHT.toInt().inv()).toByte()
                        grid[nextIdx] = (grid[nextIdx].toInt() and WALL_LEFT.toInt().inv()).toByte()

                        for (k in 0 until w) {
                            if (rowSets[k] == nextSet) {
                                rowSets[k] = currentSet
                            }
                        }
                    }
                }
            }

            if (!isLastRow) {
                // 3. Nối dọc xuống hàng tiếp theo (Đảm bảo mỗi set có ít nhất 1 kết nối dọc)
                val setElements = mutableMapOf<Int, MutableList<Int>>()
                for (x in 0 until w) {
                    setElements.getOrPut(rowSets[x]) { mutableListOf() }.add(x)
                }

                val nextRowSets = IntArray(w)
                for ((_, indices) in setElements) {
                    var carvedCount = 0
                    for (x in indices) {
                        if (random.nextBoolean() || carvedCount == 0) {
                            val currIdx = y * w + x
                            val nextIdx = (y + 1) * w + x
                            grid[currIdx] = (grid[currIdx].toInt() and WALL_TOP.toInt().inv()).toByte()
                            grid[nextIdx] = (grid[nextIdx].toInt() and WALL_BOTTOM.toInt().inv()).toByte()
                            nextRowSets[x] = rowSets[x]
                            carvedCount++
                        }
                    }
                }

                for (x in 0 until w) {
                    rowSets[x] = nextRowSets[x]
                }
            }
        }
        return grid
    }

    // =========================================================================
    // GRAPH ENDPOINTS DETERMINATION (HARDEST START & END POSITIONS)
    // =========================================================================

    /**
     * Xác định vị trí Start và End khó nhất dựa trên đặc thù hình học của từng thuật toán (1, 2, 3):
     * 1. Thuật toán 1 (Backtracking): Start ở Trung tâm (Center), End ở một góc xa nhất (Corner).
     * 2. Thuật toán 2 (Kruskal - giống màn 40): Start ở giữa cạnh Trái, End ở cạnh Phải có đường đi xa nhất (cắt ngang thớ fractal).
     * 3. Thuật toán 3 (Wilson - giống màn 60x60): Tìm 2 ô sát cạnh nhau (cách 1 vách ngăn) có khoảng cách đường đi trong mê cung dài nhất,
     *    hoặc Double BFS Diameter để tìm 2 điểm xa nhất tuyệt đối.
     */
    private fun determineOptimalEndpoints(masks: IntArray, w: Int, h: Int, algorithmIndex: Int, random: Random): Pair<Int, Int> {
        val totalCells = w * h

        return when (algorithmIndex) {
            // 1. Thuật toán 1 (Quay lui): Start ở Trung tâm, End ở Góc xa nhất
            1 -> {
                val startIdx = (h / 2) * w + (w / 2)
                val dist = bfsDistances(masks, w, h, startIdx)

                // 4 góc của mê cung
                val corners = intArrayOf(
                    0,                      // Top-Left (y=0, x=0)
                    w - 1,                  // Top-Right (y=0, x=w-1)
                    (h - 1) * w,            // Bottom-Left (y=h-1, x=0)
                    (h - 1) * w + (w - 1)   // Bottom-Right (y=h-1, x=w-1)
                )

                // Tìm góc có khoảng cách đường đi từ Trung tâm lớn nhất
                var bestGoal = corners[0]
                var maxCornerDist = dist[corners[0]]
                for (c in corners) {
                    if (dist[c] > maxCornerDist) {
                        maxCornerDist = dist[c]
                        bestGoal = c
                    }
                }

                if (maxCornerDist <= 0) {
                    var maxD = -1
                    var maxIdx = 0
                    for (i in 0 until totalCells) {
                        if (dist[i] > maxD) {
                            maxD = dist[i]
                            maxIdx = i
                        }
                    }
                    bestGoal = maxIdx
                }
                Pair(startIdx, bestGoal)
            }

            // 2. Thuật toán 2 (Kruskal - giống màn 40): Giữa cạnh Trái -> Cạnh Phải đối diện xa nhất
            2 -> {
                val startIdx = (h / 2) * w + 0 // Giữa cạnh Trái (x=0, y=h/2)
                val dist = bfsDistances(masks, w, h, startIdx)

                // Dò các ô nằm trên cạnh Phải (x = w - 1) tìm ô có đường đi xa nhất
                var bestGoal = (h / 2) * w + (w - 1)
                var maxRightDist = dist[bestGoal]
                for (y in 0 until h) {
                    val rightCell = y * w + (w - 1)
                    if (dist[rightCell] > maxRightDist) {
                        maxRightDist = dist[rightCell]
                        bestGoal = rightCell
                    }
                }
                Pair(startIdx, bestGoal)
            }

            // 3. Thuật toán 3 (Wilson - giống các màn 60x60): Hai ô sát cạnh nhau (cách 1 vách ngăn) có path length cực lớn hoặc Double BFS
            else -> {
                val (diaStart, diaGoal) = findGraphDiameter(masks, w, h)
                val spine = findShortestPath(masks, w, h, diaStart, diaGoal)
                val diameterDist = spine.size - 1

                val startDist = bfsDistances(masks, w, h, diaStart)

                var bestAdjacentPair: Pair<Int, Int>? = null
                var maxAdjPathDist = 0
                val minRequiredPath = maxOf(5, (diameterDist * 0.45).toInt())

                // Quét qua các cạnh có tường ngăn kiên cố
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val curr = y * w + x
                        val m = masks[curr]

                        // Kiểm tra tường Phải (EAST)
                        if (x < w - 1 && (m and EAST) == 0) {
                            val right = y * w + (x + 1)
                            val pathD = kotlin.math.abs(startDist[curr] - startDist[right])
                            if (pathD > maxAdjPathDist && pathD >= minRequiredPath) {
                                maxAdjPathDist = pathD
                                bestAdjacentPair = Pair(curr, right)
                            }
                        }

                        // Kiểm tra tường Trên (NORTH)
                        if (y < h - 1 && (m and NORTH) == 0) {
                            val top = (y + 1) * w + x
                            val pathD = kotlin.math.abs(startDist[curr] - startDist[top])
                            if (pathD > maxAdjPathDist && pathD >= minRequiredPath) {
                                maxAdjPathDist = pathD
                                bestAdjacentPair = Pair(curr, top)
                            }
                        }
                    }
                }

                if (bestAdjacentPair != null && maxAdjPathDist >= minRequiredPath) {
                    bestAdjacentPair
                } else {
                    // Fallback to Double BFS Diameter
                    Pair(diaStart, diaGoal)
                }
            }
        }
    }

    private fun bfsDistances(masks: IntArray, w: Int, h: Int, startIdx: Int): IntArray {
        val totalCells = w * h
        val dist = IntArray(totalCells) { -1 }
        val queue = IntArray(totalCells)
        var head = 0
        var tail = 0

        dist[startIdx] = 0
        queue[tail++] = startIdx

        while (head < tail) {
            val curr = queue[head++]
            val d = dist[curr]
            val cx = curr % w
            val cy = curr / w
            val m = masks[curr]

            if (cy < h - 1 && (m and NORTH) != 0) {
                val nIdx = (cy + 1) * w + cx
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cx < w - 1 && (m and EAST) != 0) {
                val nIdx = cy * w + (cx + 1)
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val nIdx = (cy - 1) * w + cx
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val nIdx = cy * w + (cx - 1)
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
        }
        return dist
    }

    // =========================================================================
    // GRAPH DIAMETER & SHORTEST PATH SOLVER
    // =========================================================================
    private fun findGraphDiameter(masks: IntArray, w: Int, h: Int): Pair<Int, Int> {
        val totalCells = w * h
        val queue = IntArray(totalCells)
        val dist = IntArray(totalCells)

        dist.fill(-1)
        var head = 0
        var tail = 0
        queue[tail++] = 0
        dist[0] = 0

        var nodeA = 0
        var maxDistA = 0

        while (head < tail) {
            val curr = queue[head++]
            val d = dist[curr]
            if (d > maxDistA) {
                maxDistA = d
                nodeA = curr
            }
            val cx = curr % w
            val cy = curr / w
            val m = masks[curr]

            if (cy < h - 1 && (m and NORTH) != 0) {
                val nIdx = (cy + 1) * w + cx
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cx < w - 1 && (m and EAST) != 0) {
                val nIdx = cy * w + (cx + 1)
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val nIdx = (cy - 1) * w + cx
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val nIdx = cy * w + (cx - 1)
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
        }

        dist.fill(-1)
        head = 0
        tail = 0
        queue[tail++] = nodeA
        dist[nodeA] = 0

        var nodeB = nodeA
        var maxDistB = 0

        while (head < tail) {
            val curr = queue[head++]
            val d = dist[curr]
            if (d > maxDistB) {
                maxDistB = d
                nodeB = curr
            }
            val cx = curr % w
            val cy = curr / w
            val m = masks[curr]

            if (cy < h - 1 && (m and NORTH) != 0) {
                val nIdx = (cy + 1) * w + cx
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cx < w - 1 && (m and EAST) != 0) {
                val nIdx = cy * w + (cx + 1)
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val nIdx = (cy - 1) * w + cx
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val nIdx = cy * w + (cx - 1)
                if (dist[nIdx] == -1) { dist[nIdx] = d + 1; queue[tail++] = nIdx }
            }
        }

        return Pair(nodeA, nodeB)
    }

    private fun findShortestPath(masks: IntArray, w: Int, h: Int, startIdx: Int, goalIdx: Int): List<Point> {
        val totalCells = w * h
        val prev = IntArray(totalCells) { -1 }
        val visited = BooleanArray(totalCells)
        val queue = ArrayDeque<Int>()

        queue.add(startIdx)
        visited[startIdx] = true

        while (queue.isNotEmpty()) {
            val curr = queue.poll() ?: break
            if (curr == goalIdx) break

            val cx = curr % w
            val cy = curr / w
            val m = masks[curr]

            if (cy < h - 1 && (m and NORTH) != 0) {
                val nIdx = (cy + 1) * w + cx
                if (!visited[nIdx]) { visited[nIdx] = true; prev[nIdx] = curr; queue.add(nIdx) }
            }
            if (cx < w - 1 && (m and EAST) != 0) {
                val nIdx = cy * w + (cx + 1)
                if (!visited[nIdx]) { visited[nIdx] = true; prev[nIdx] = curr; queue.add(nIdx) }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val nIdx = (cy - 1) * w + cx
                if (!visited[nIdx]) { visited[nIdx] = true; prev[nIdx] = curr; queue.add(nIdx) }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val nIdx = cy * w + (cx - 1)
                if (!visited[nIdx]) { visited[nIdx] = true; prev[nIdx] = curr; queue.add(nIdx) }
            }
        }

        val path = mutableListOf<Point>()
        var curr = goalIdx
        while (curr != -1) {
            path.add(Point(curr % w, curr / w))
            if (curr == startIdx) break
            curr = prev[curr]
        }
        return path.reversed()
    }
}

object MazeSolver {
    fun computeDistances(maze: Maze): IntArray {
        val n = maze.w * maze.h
        val dist = IntArray(n) { -1 }
        val startIdx = maze.start.y * maze.w + maze.start.x
        if (startIdx !in 0 until n) return dist
        dist[startIdx] = 0

        val queue = IntArray(n)
        var head = 0
        var tail = 0
        queue[tail++] = startIdx

        while (head < tail) {
            val curr = queue[head++]
            val cx = curr % maze.w
            val cy = curr / maze.w
            val m = maze.masks[curr]
            val nextD = dist[curr] + 1

            if (cy < maze.h - 1 && (m and NORTH) != 0) {
                val ni = (cy + 1) * maze.w + cx
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cx < maze.w - 1 && (m and EAST) != 0) {
                val ni = cy * maze.w + (cx + 1)
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val ni = (cy - 1) * maze.w + cx
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val ni = cy * maze.w + (cx - 1)
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
        }
        return dist
    }

    /**
     * Tính khoảng cách bước đi từ MỌI Ô trong mê cung về ĐÍCH (Goal) bằng mảng nguyên thủy O(W*H).
     * Phục vụ Bản Đồ Gợi Ý Hướng Đích cho cả chế độ Thường, Sói Đuổi và IMPOSSIBLE (300x300 -> 1000x1000).
     */
    fun computeDistancesToGoal(maze: Maze): IntArray {
        val n = maze.w * maze.h
        val dist = IntArray(n) { -1 }
        val goalIdx = maze.goal.y * maze.w + maze.goal.x
        if (goalIdx !in 0 until n) return dist
        dist[goalIdx] = 0

        val queue = IntArray(n)
        var head = 0
        var tail = 0
        queue[tail++] = goalIdx

        while (head < tail) {
            val curr = queue[head++]
            val cx = curr % maze.w
            val cy = curr / maze.w
            val m = maze.masks[curr]
            val nextD = dist[curr] + 1

            if (cy < maze.h - 1 && (m and NORTH) != 0) {
                val ni = (cy + 1) * maze.w + cx
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cx < maze.w - 1 && (m and EAST) != 0) {
                val ni = cy * maze.w + (cx + 1)
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val ni = (cy - 1) * maze.w + cx
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val ni = cy * maze.w + (cx - 1)
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
        }
        return dist
    }

    /**
     * Tạo dữ liệu Bản Đồ Gợi Ý Hướng Đích từ vị trí hiện tại của người chơi tới Đích (hỗ trợ cả Impossible Mode).
     */
    fun computeGoalDirectionHint(
        maze: Maze,
        player: Point,
        distToGoal: IntArray?
    ): GoalDirectionHint {
        val w = maze.w
        val h = maze.h
        val total = w * h
        val pIdx = player.y * w + player.x
        val directDx = maze.goal.x - player.x
        val directDy = maze.goal.y - player.y

        if (distToGoal == null || pIdx !in 0 until total || distToGoal[pIdx] <= 0) {
            return GoalDirectionHint(
                nextStepDir = 0,
                remainingPathSteps = kotlin.math.abs(directDx) + kotlin.math.abs(directDy),
                directDx = directDx,
                directDy = directDy,
                sampledRouteToGoal = listOf(player, maze.goal)
            )
        }

        val totalSteps = distToGoal[pIdx]
        var nextDir = 0
        val route = mutableListOf<Point>()
        route.add(player)

        var currIdx = pIdx
        var currDist = totalSteps
        val sampleStride = maxOf(1, totalSteps / 80) // Giữ tối đa ~80 điểm vẽ mượt mà ngay cả trên bản đồ 1000x1000
        var stepCounter = 0

        while (currDist > 0 && stepCounter < total) {
            val cx = currIdx % w
            val cy = currIdx / w
            val m = maze.masks[currIdx]
            var moved = false

            // Tìm ô lân cận có khoảng cách tới Goal giảm đi 1
            if (cy < h - 1 && (m and NORTH) != 0) {
                val ni = (cy + 1) * w + cx
                if (distToGoal[ni] == currDist - 1) {
                    if (stepCounter == 0) nextDir = NORTH
                    currIdx = ni
                    currDist--
                    moved = true
                }
            }
            if (!moved && cx < w - 1 && (m and EAST) != 0) {
                val ni = cy * w + (cx + 1)
                if (distToGoal[ni] == currDist - 1) {
                    if (stepCounter == 0) nextDir = EAST
                    currIdx = ni
                    currDist--
                    moved = true
                }
            }
            if (!moved && cy > 0 && (m and SOUTH) != 0) {
                val ni = (cy - 1) * w + cx
                if (distToGoal[ni] == currDist - 1) {
                    if (stepCounter == 0) nextDir = SOUTH
                    currIdx = ni
                    currDist--
                    moved = true
                }
            }
            if (!moved && cx > 0 && (m and WEST) != 0) {
                val ni = cy * w + (cx - 1)
                if (distToGoal[ni] == currDist - 1) {
                    if (stepCounter == 0) nextDir = WEST
                    currIdx = ni
                    currDist--
                    moved = true
                }
            }

            if (!moved) break
            stepCounter++
            if (stepCounter % sampleStride == 0 || currDist == 0) {
                route.add(Point(currIdx % w, currIdx / w))
            }
        }

        return GoalDirectionHint(
            nextStepDir = nextDir,
            remainingPathSteps = totalSteps,
            directDx = directDx,
            directDy = directDy,
            sampledRouteToGoal = route
        )
    }

    /**
     * Tính bước đi tiếp theo của Con Sói truy đuổi về phía vị trí hiện tại của người chơi.
     * Sử dụng BFS ngược từ vị trí người chơi tới Sói trên lưới mê cung (siêu nhẹ cho 5x5 -> 30x30).
     */
    fun computeNextWolfStep(maze: Maze, wolf: Point, player: Point): Point {
        if (wolf.x == player.x && wolf.y == player.y) return wolf
        val w = maze.w
        val h = maze.h
        val total = w * h
        val wolfIdx = wolf.y * w + wolf.x
        val playerIdx = player.y * w + player.x
        if (wolfIdx !in 0 until total || playerIdx !in 0 until total) return wolf

        val dist = IntArray(total) { -1 }
        val queue = IntArray(total)
        var head = 0
        var tail = 0

        dist[playerIdx] = 0
        queue[tail++] = playerIdx

        while (head < tail) {
            val curr = queue[head++]
            if (curr == wolfIdx) break
            val cx = curr % w
            val cy = curr / w
            val m = maze.masks[curr]
            val nextD = dist[curr] + 1

            if (cy < h - 1 && (m and NORTH) != 0) {
                val ni = (cy + 1) * w + cx
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cx < w - 1 && (m and EAST) != 0) {
                val ni = cy * w + (cx + 1)
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val ni = (cy - 1) * w + cx
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val ni = cy * w + (cx - 1)
                if (dist[ni] == -1) { dist[ni] = nextD; queue[tail++] = ni }
            }
        }

        val wolfDist = dist[wolfIdx]
        if (wolfDist <= 0) return wolf

        val wx = wolf.x
        val wy = wolf.y
        val wm = maze.masks[wolfIdx]

        if (wy < h - 1 && (wm and NORTH) != 0) {
            val ni = (wy + 1) * w + wx
            if (dist[ni] == wolfDist - 1) return Point(wx, wy + 1)
        }
        if (wx < w - 1 && (wm and EAST) != 0) {
            val ni = wy * w + (wx + 1)
            if (dist[ni] == wolfDist - 1) return Point(wx + 1, wy)
        }
        if (wy > 0 && (wm and SOUTH) != 0) {
            val ni = (wy - 1) * w + wx
            if (dist[ni] == wolfDist - 1) return Point(wx, wy - 1)
        }
        if (wx > 0 && (wm and WEST) != 0) {
            val ni = wy * w + (wx - 1)
            if (dist[ni] == wolfDist - 1) return Point(wx - 1, wy)
        }
        return wolf
    }

    /**
     * Tính khoảng cách số ô đường đi ngắn nhất trong mê cung giữa 2 điểm (ví dụ giữa Sói và Người chơi).
     */
    fun computePathDistance(maze: Maze, from: Point, to: Point): Int {
        if (from.x == to.x && from.y == to.y) return 0
        val w = maze.w
        val h = maze.h
        val n = w * h
        val startIdx = from.y * w + from.x
        val targetIdx = to.y * w + to.x
        if (startIdx !in 0 until n || targetIdx !in 0 until n) {
            return kotlin.math.abs(from.x - to.x) + kotlin.math.abs(from.y - to.y)
        }

        val dist = IntArray(n) { -1 }
        val queue = IntArray(n)
        var head = 0
        var tail = 0

        dist[startIdx] = 0
        queue[tail++] = startIdx

        while (head < tail) {
            val cur = queue[head++]
            val curD = dist[cur]
            if (cur == targetIdx) return curD

            val cx = cur % w
            val cy = cur / w
            val m = maze.masks[cur]

            if (cy < h - 1 && (m and NORTH) != 0) {
                val ni = (cy + 1) * w + cx
                if (dist[ni] == -1) {
                    dist[ni] = curD + 1
                    queue[tail++] = ni
                }
            }
            if (cx < w - 1 && (m and EAST) != 0) {
                val ni = cy * w + (cx + 1)
                if (dist[ni] == -1) {
                    dist[ni] = curD + 1
                    queue[tail++] = ni
                }
            }
            if (cy > 0 && (m and SOUTH) != 0) {
                val ni = (cy - 1) * w + cx
                if (dist[ni] == -1) {
                    dist[ni] = curD + 1
                    queue[tail++] = ni
                }
            }
            if (cx > 0 && (m and WEST) != 0) {
                val ni = cy * w + (cx - 1)
                if (dist[ni] == -1) {
                    dist[ni] = curD + 1
                    queue[tail++] = ni
                }
            }
        }
        return kotlin.math.abs(from.x - to.x) + kotlin.math.abs(from.y - to.y)
    }

    /**
     * Đã xóa các vật phẩm như khóa kho báu, đồng xu, rương trang bị trong game theo yêu cầu.
     */
    fun generateCollectibles(maze: Maze, def: LevelDef): List<MazeCollectible> {
        return emptyList()
    }

    fun findPathToGoal(maze: Maze): List<Point> {
        return maze.spine
    }
}
