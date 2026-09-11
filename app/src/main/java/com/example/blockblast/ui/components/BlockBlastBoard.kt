package com.example.blockblast.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.blockblast.logic.DragState
import com.example.blockblast.model.GRID_SIZE
import com.example.blockblast.model.GameState

@Composable
fun BlockBlastBoard(
    gameState: GameState,
    dragState: DragState,
    onBoardPositioned: (Rect, Float) -> Unit,
    onCellClicked: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Breathing glow animation for glowing blocks
    val infiniteTransition = rememberInfiniteTransition(label = "glow_breath")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val ghostPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghost_alpha"
    )

    var cellSizePx by remember { mutableFloatStateOf(0f) }

    val blastAnim = remember { androidx.compose.animation.core.Animatable(1f) }
    androidx.compose.runtime.LaunchedEffect(gameState.blastingCells) {
        if (gameState.blastingCells.isNotEmpty()) {
            blastAnim.snapTo(1f)
            blastAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
            )
        } else {
            blastAnim.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0C0C10))
            .border(1.2.dp, Color(0xFF1E1E28), RoundedCornerShape(20.dp))
            .padding(8.dp)
            .testTag("block_blast_board")
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInRoot()
                val cellSpacing = 3.5f
                val cellSize = (bounds.width - (cellSpacing * (GRID_SIZE - 1))) / GRID_SIZE
                cellSizePx = cellSize
                onBoardPositioned(bounds, cellSize)
            }
            .pointerInput(gameState.selectedPieceIndex) {
                detectTapGestures { tapOffset ->
                    val step = cellSizePx + 3.5f
                    if (step > 0f) {
                        val col = (tapOffset.x / step).toInt().coerceIn(0, GRID_SIZE - 1)
                        val row = (tapOffset.y / step).toInt().coerceIn(0, GRID_SIZE - 1)
                        onCellClicked(row, col)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cellSpacing = 3.5f
            val totalWidth = size.width
            val cellSize = (totalWidth - (cellSpacing * (GRID_SIZE - 1))) / GRID_SIZE
            val cornerRadius = cellSize * 0.22f

            // Compute ghost cells if dragging and over valid target
            val ghostCells = mutableSetOf<Pair<Int, Int>>()
            if (dragState.isValidPlacement && dragState.piece != null && dragState.targetCell != null) {
                val p = dragState.piece
                val (tRow, tCol) = dragState.targetCell
                for (r in 0 until p.height) {
                    for (c in 0 until p.width) {
                        if (p.matrix[r][c]) {
                            ghostCells.add(Pair(tRow + r, tCol + c))
                        }
                    }
                }
            }

            // Draw 8x8 Grid
            for (row in 0 until GRID_SIZE) {
                for (col in 0 until GRID_SIZE) {
                    val topLeft = Offset(
                        col * (cellSize + cellSpacing),
                        row * (cellSize + cellSpacing)
                    )
                    val cellSizeObject = Size(cellSize, cellSize)
                    val isFilled = gameState.grid[row][col] > 0
                    val isBlasting = gameState.blastingCells.contains(Pair(row, col))
                    val isGhost = ghostCells.contains(Pair(row, col))

                    // Always draw base empty slot
                    drawEmptySlot(
                        topLeft = topLeft,
                        size = cellSizeObject,
                        cornerRadiusPx = cornerRadius
                    )

                    if (isBlasting) {
                        // Smooth dissolve & gentle vanish animation for cleared line
                        val vanishAlpha = blastAnim.value
                        val vanishScale = 0.88f + 0.22f * blastAnim.value
                        drawGlowingWhiteBlock(
                            topLeft = topLeft,
                            size = cellSizeObject,
                            cornerRadiusPx = cornerRadius,
                            glowAlpha = vanishAlpha * 0.7f,
                            scale = vanishScale,
                            isBlasting = true,
                            alpha = vanishAlpha
                        )
                    } else if (isFilled) {
                        // Draw standard glowing white block
                        drawGlowingWhiteBlock(
                            topLeft = topLeft,
                            size = cellSizeObject,
                            cornerRadiusPx = cornerRadius,
                            glowAlpha = glowPulse,
                            scale = 1f,
                            isBlasting = false,
                            alpha = 1f
                        )
                    } else if (isGhost) {
                        // Draw ghost placement preview
                        drawGhostBlock(
                            topLeft = topLeft,
                            size = cellSizeObject,
                            cornerRadiusPx = cornerRadius,
                            pulseAlpha = ghostPulse
                        )
                    }
                }
            }
        }
    }
}
