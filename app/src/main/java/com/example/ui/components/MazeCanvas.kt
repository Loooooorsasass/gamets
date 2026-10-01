package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.core.engine.Maze
import com.example.core.engine.MazeCollectible
import com.example.core.engine.Point
import com.example.data.shop.MazeTheme
import com.example.data.shop.PlayerSkin

@Composable
fun MazeCanvas(
    maze: Maze,
    player: Point,
    visitedCells: Set<Int>,
    theme: MazeTheme,
    skin: PlayerSkin,
    vision: Int?,
    hintPath: List<Point>? = null,
    pathHistory: List<Point>? = null,
    replayTrail: List<Point>? = null,
    collectibles: List<MazeCollectible> = emptyList(),
    wolfPos: Point? = null,
    wolfCubPos: Point? = null,
    isWolfFrenzy: Boolean = false,
    isWolfStunned: Boolean = false,
    hasShieldProtection: Boolean = false,
    isPlayerFrozen: Boolean = false,
    isIceBeamActive: Boolean = false,
    controlMode: GridControlMode = GridControlMode.AUTO,
    moveSensitivity: Float = 1.0f,
    onMove: (dx: Int, dy: Int) -> Boolean = { _, _ -> true },
    onUndo: (() -> Unit)? = null,
    onWallHit: (() -> Unit)? = null,
    onWin: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                MazeGameView(ctx).apply {
                    setOnMazeMoveListener(object : MazeGameView.OnMazeMoveListener {
                        override fun onMove(dx: Int, dy: Int, newPlayer: MazeGameView.MazePoint?, totalMoves: Int) {
                            onMove(dx, dy)
                        }

                        override fun onUndo(restoredPlayer: MazeGameView.MazePoint) {
                            onUndo?.invoke()
                        }

                        override fun onWallHit() {
                            onWallHit?.invoke()
                        }

                        override fun onGameWin(totalMoves: Int) {
                            onWin?.invoke()
                        }
                    })

                    setStepByStepMode(controlMode == GridControlMode.STEP_BY_STEP)
                    setMoveSensitivity(moveSensitivity)
                    setPlayerSkinId(skin.id)

                    updateStudioTheme(
                        theme.panelColor.toArgb(),
                        theme.wallColor.toArgb(),
                        theme.pathVisitedColor.toArgb(),
                        theme.accentColor.toArgb()
                    )

                    val hintConverted = hintPath?.map { MazeGameView.MazePoint(it.x, it.y) }
                    val historyConverted = (pathHistory ?: replayTrail)?.map { MazeGameView.MazePoint(it.x, it.y) }
                        ?: listOf(MazeGameView.MazePoint(player.x, player.y))
                    val collectiblesConverted = collectibles.map {
                        MazeGameView.MazeCollectibleItem(it.x, it.y, it.type.name)
                    }
                    val wolfConverted = wolfPos?.let { MazeGameView.MazePoint(it.x, it.y) }
                    val wolfCubConverted = wolfCubPos?.let { MazeGameView.MazePoint(it.x, it.y) }

                    setDynamicEntities(
                        collectiblesConverted,
                        wolfConverted,
                        wolfCubConverted,
                        isWolfFrenzy,
                        isWolfStunned,
                        hasShieldProtection,
                        isPlayerFrozen,
                        isIceBeamActive
                    )

                    setMazeData(
                        maze.w,
                        maze.h,
                        maze.masks,
                        MazeGameView.MazePoint(maze.start.x, maze.start.y),
                        MazeGameView.MazePoint(maze.goal.x, maze.goal.y),
                        MazeGameView.MazePoint(player.x, player.y),
                        visitedCells,
                        historyConverted,
                        hintConverted,
                        vision
                    )
                }
            },
            update = { view ->
                view.setStepByStepMode(controlMode == GridControlMode.STEP_BY_STEP)
                view.setMoveSensitivity(moveSensitivity)
                view.setPlayerSkinId(skin.id)

                view.updateStudioTheme(
                    theme.panelColor.toArgb(),
                    theme.wallColor.toArgb(),
                    theme.pathVisitedColor.toArgb(),
                    theme.accentColor.toArgb()
                )

                val hintConverted = hintPath?.map { MazeGameView.MazePoint(it.x, it.y) }
                val historyConverted = (pathHistory ?: replayTrail)?.map { MazeGameView.MazePoint(it.x, it.y) }
                    ?: listOf(MazeGameView.MazePoint(player.x, player.y))
                val collectiblesConverted = collectibles.map {
                    MazeGameView.MazeCollectibleItem(it.x, it.y, it.type.name)
                }
                val wolfConverted = wolfPos?.let { MazeGameView.MazePoint(it.x, it.y) }
                val wolfCubConverted = wolfCubPos?.let { MazeGameView.MazePoint(it.x, it.y) }

                view.setDynamicEntities(
                    collectiblesConverted,
                    wolfConverted,
                    wolfCubConverted,
                    isWolfFrenzy,
                    isWolfStunned,
                    hasShieldProtection,
                    isPlayerFrozen,
                    isIceBeamActive
                )

                view.setMazeData(
                    maze.w,
                    maze.h,
                    maze.masks,
                    MazeGameView.MazePoint(maze.start.x, maze.start.y),
                    MazeGameView.MazePoint(maze.goal.x, maze.goal.y),
                    MazeGameView.MazePoint(player.x, player.y),
                    visitedCells,
                    historyConverted,
                    hintConverted,
                    vision
                )
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
