package com.example.ui.components

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.example.core.engine.Point
import kotlinx.coroutines.CoroutineScope
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Hằng số cấu hình điều khiển di chuyển trên lưới ô (đã giảm 25-30% độ trễ để di chuyển nhanh và mượt mà hơn):
 */
const val HOLD_THRESHOLD_MS = 270L
const val MOVE_SPEED_MS = 220L

/**
 * Chế độ điều khiển di chuyển
 */
enum class GridControlMode(val displayName: String, val description: String) {
    AUTO("⚡ Kéo Tự Do", "Kéo ngón tay đến ô nào, nhân vật bám theo đến đó"),
    STEP_BY_STEP("👣 Từng Bước", "Chỉ đi 1 ô mỗi lần chạm/vuốt")
}

/**
 * Bộ xử lý cử chỉ kéo trực tiếp trên lưới (Direct Cell Drag-Follow Handler).
 * Khi người chơi kéo ngón tay trên màn hình đến vị trí ô nào, hệ thống tính toán tọa độ ô (cellX, cellY)
 * ngay dưới ngón tay và lập tức điều khiển nhân vật di chuyển chính xác đến ô đó thay vì chỉ đoán hướng vuốt.
 */
class GridGestureInputHandler(
    private val coroutineScope: CoroutineScope,
    private val onValidateAndMove: (dx: Int, dy: Int) -> Boolean,
    initialControlMode: GridControlMode = GridControlMode.AUTO,
    var targetView: View? = null,
    var onWallHitHaptic: (() -> Unit)? = null,
    var onMoveHaptic: (() -> Unit)? = null
) {
    var controlMode: GridControlMode = initialControlMode
        private set

    private var isTouching = false
    private var lastTargetCell: Point? = null

    fun setControlMode(mode: GridControlMode) {
        controlMode = mode
    }

    fun toggleControlMode(): GridControlMode {
        val newMode = if (controlMode == GridControlMode.AUTO) {
            GridControlMode.STEP_BY_STEP
        } else {
            GridControlMode.AUTO
        }
        setControlMode(newMode)
        return newMode
    }

    fun handleTouchStart(cell: Point, player: Point) {
        isTouching = true
        lastTargetCell = cell
        if (cell != player) {
            moveToCell(cell.x, cell.y, player.x, player.y)
        }
    }

    fun handleTouchMove(cell: Point, player: Point) {
        if (!isTouching) return
        if (cell == lastTargetCell && cell == player) return

        lastTargetCell = cell
        if (cell != player) {
            moveToCell(cell.x, cell.y, player.x, player.y)
        }
    }

    fun handleTouchEnd() {
        isTouching = false
        lastTargetCell = null
    }

    /**
     * Di chuyển nhân vật bám sát theo vị trí ô ngón tay đang trỏ tới (targetX, targetY).
     * Kiểm tra từng bước đi hợp lệ (không va tường) từ vị trí hiện tại đến ô mục tiêu.
     */
    fun moveToCell(targetX: Int, targetY: Int, currentX: Int, currentY: Int): Boolean {
        var curX = currentX
        var curY = currentY
        var movedAny = false
        var steps = 0
        val maxSteps = 20

        while ((curX != targetX || curY != targetY) && steps < maxSteps) {
            steps++
            val dx = (targetX - curX).coerceIn(-1, 1)
            val dy = (targetY - curY).coerceIn(-1, 1)

            var stepSuccess = false

            // Ưu tiên trục có khoảng cách lệch lớn hơn hoặc thử các hướng mở
            if (abs(targetX - curX) >= abs(targetY - curY)) {
                if (dx != 0 && onValidateAndMove(dx, 0)) {
                    curX += dx
                    stepSuccess = true
                } else if (dy != 0 && onValidateAndMove(0, dy)) {
                    curY += dy
                    stepSuccess = true
                }
            } else {
                if (dy != 0 && onValidateAndMove(0, dy)) {
                    curY += dy
                    stepSuccess = true
                } else if (dx != 0 && onValidateAndMove(dx, 0)) {
                    curX += dx
                    stepSuccess = true
                }
            }

            if (stepSuccess) {
                movedAny = true
                triggerMoveHaptic()
            } else {
                // Đụng tường cản, kích hoạt phản hồi xúc giác
                triggerWallHitHaptic()
                break
            }

            // Nếu ở chế độ từng bước, chỉ đi 1 ô
            if (controlMode == GridControlMode.STEP_BY_STEP) {
                break
            }
        }
        return movedAny
    }

    fun triggerMoveHaptic() {
        targetView?.let { view ->
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CLOCK_TICK
                else -> HapticFeedbackConstants.KEYBOARD_TAP
            }
            view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        }
        onMoveHaptic?.invoke()
    }

    fun triggerWallHitHaptic() {
        targetView?.let { view ->
            val constant = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.REJECT
                else -> HapticFeedbackConstants.LONG_PRESS
            }
            view.performHapticFeedback(constant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
        }
        onWallHitHaptic?.invoke()
    }
}

/**
 * Modifier Compose: Lắng nghe chạm và kéo trực tiếp theo ô trên Canvas mê cung.
 */
fun Modifier.directCellDragInput(
    handler: GridGestureInputHandler,
    mazeWidth: Int,
    mazeHeight: Int,
    vision: Int?,
    playerProvider: () -> Point
): Modifier = this.pointerInput(mazeWidth, mazeHeight, vision) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

        val firstCell = calculateCellFromOffset(
            offset = down.position,
            canvasSize = canvasSize,
            mazeWidth = mazeWidth,
            mazeHeight = mazeHeight,
            vision = vision,
            player = playerProvider()
        )

        handler.handleTouchStart(firstCell, playerProvider())
        val pointerId = down.id

        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == pointerId } ?: event.changes.firstOrNull()

            if (change == null || !change.pressed) {
                handler.handleTouchEnd()
                break
            }

            val curPos = change.position
            val targetCell = calculateCellFromOffset(
                offset = curPos,
                canvasSize = canvasSize,
                mazeWidth = mazeWidth,
                mazeHeight = mazeHeight,
                vision = vision,
                player = playerProvider()
            )

            change.consume()
            handler.handleTouchMove(targetCell, playerProvider())
        }
    }
}

/**
 * Tính toán tọa độ ô mê cung (x, y) từ vị trí chạm (Pixel Offset) trên Canvas.
 */
fun calculateCellFromOffset(
    offset: Offset,
    canvasSize: Size,
    mazeWidth: Int,
    mazeHeight: Int,
    vision: Int?,
    player: Point
): Point {
    val canvasW = canvasSize.width
    val canvasH = canvasSize.height

    val viewW = if (vision != null) min(vision, mazeWidth) else mazeWidth
    val viewH = if (vision != null) min(vision, mazeHeight) else mazeHeight

    val originX = if (vision != null) {
        min(max(0, player.x - viewW / 2), mazeWidth - viewW)
    } else 0

    val originY = if (vision != null) {
        min(max(0, player.y - viewH / 2), mazeHeight - viewH)
    } else 0

    val cellSize = min(canvasW / viewW, canvasH / viewH)
    val offX = (canvasW - viewW * cellSize) / 2f
    val offY = (canvasH - viewH * cellSize) / 2f

    val lx = ((offset.x - offX) / cellSize).toInt().coerceIn(0, viewW - 1)
    val ly = (viewH - 1 - ((offset.y - offY) / cellSize).toInt()).coerceIn(0, viewH - 1)

    val gx = (originX + lx).coerceIn(0, mazeWidth - 1)
    val gy = (originY + ly).coerceIn(0, mazeHeight - 1)

    return Point(gx, gy)
}

/**
 * Modifier phím điều hướng bàn phím máy tính
 */
fun Modifier.gridKeyboardInput(
    onMove: (dx: Int, dy: Int) -> Boolean
): Modifier = this.onKeyEvent { keyEvent: KeyEvent ->
    if (keyEvent.type == KeyEventType.KeyDown) {
        when (keyEvent.key) {
            Key.DirectionUp -> onMove(0, 1)
            Key.DirectionDown -> onMove(0, -1)
            Key.DirectionLeft -> onMove(-1, 0)
            Key.DirectionRight -> onMove(1, 0)
            else -> false
        }
    } else {
        false
    }
}
