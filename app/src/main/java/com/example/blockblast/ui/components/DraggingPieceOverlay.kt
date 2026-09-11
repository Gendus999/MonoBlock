package com.example.blockblast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.blockblast.logic.DragState

@Composable
fun DraggingPieceOverlay(
    dragState: DragState,
    boardCellSizePx: Float,
    liftOffsetPx: Float,
    modifier: Modifier = Modifier
) {
    val piece = dragState.piece ?: return
    if (dragState.pieceIndex == null) return

    val density = LocalDensity.current
    val effectiveCellSize = if (boardCellSizePx > 0) boardCellSizePx else with(density) { 38.dp.toPx() }
    val spacing = 3.5f

    val pieceCols = piece.width
    val pieceRows = piece.height

    val pieceWidthPx = pieceCols * effectiveCellSize + (pieceCols - 1) * spacing
    val pieceHeightPx = pieceRows * effectiveCellSize + (pieceRows - 1) * spacing

    // Position piece centered horizontally on finger, and lifted above finger
    val anchorX = dragState.dragOffset.x - pieceWidthPx / 2f
    val anchorY = dragState.dragOffset.y - liftOffsetPx - pieceHeightPx / 2f

    val cornerRadius = effectiveCellSize * 0.22f

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .offset { IntOffset(anchorX.toInt(), anchorY.toInt()) }
                .size(
                    width = with(density) { pieceWidthPx.toDp() },
                    height = with(density) { pieceHeightPx.toDp() }
                )
        ) {
            // Draw elevated drop shadow
            for (r in 0 until pieceRows) {
                for (c in 0 until pieceCols) {
                    if (piece.matrix[r][c]) {
                        val cellTopLeft = Offset(
                            c * (effectiveCellSize + spacing),
                            r * (effectiveCellSize + spacing)
                        )
                        // Extra elevated shadow for drag feedback
                        drawGlowingWhiteBlock(
                            topLeft = cellTopLeft,
                            size = Size(effectiveCellSize, effectiveCellSize),
                            cornerRadiusPx = cornerRadius,
                            glowAlpha = 0.55f,
                            scale = 1.04f,
                            isBlasting = false,
                            alpha = 0.95f
                        )
                    }
                }
            }
        }
    }
}
