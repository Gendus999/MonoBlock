package com.example.blockblast.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.example.blockblast.model.BlockPiece

@Composable
fun BlockPieceRenderer(
    piece: BlockPiece,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    glowAlpha: Float = 0.4f,
    blockSizeRatio: Float = 0.82f
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalW = size.width
            val totalH = size.height
            val pieceCols = piece.width
            val pieceRows = piece.height

            if (pieceCols == 0 || pieceRows == 0) return@Canvas

            val spacing = 2.5f
            // Fit within canvas
            val maxCellW = (totalW - spacing * (pieceCols - 1)) / pieceCols
            val maxCellH = (totalH - spacing * (pieceRows - 1)) / pieceRows
            val cellSize = minOf(maxCellW, maxCellH) * blockSizeRatio
            val cornerRadius = cellSize * 0.22f

            val drawnW = pieceCols * cellSize + (pieceCols - 1) * spacing
            val drawnH = pieceRows * cellSize + (pieceRows - 1) * spacing

            val startX = (totalW - drawnW) / 2f
            val startY = (totalH - drawnH) / 2f

            for (r in 0 until pieceRows) {
                for (c in 0 until pieceCols) {
                    if (piece.matrix[r][c]) {
                        val topLeft = Offset(
                            startX + c * (cellSize + spacing),
                            startY + r * (cellSize + spacing)
                        )
                        drawGlowingWhiteBlock(
                            topLeft = topLeft,
                            size = Size(cellSize, cellSize),
                            cornerRadiusPx = cornerRadius,
                            glowAlpha = glowAlpha,
                            scale = 1f,
                            isBlasting = false,
                            alpha = alpha
                        )
                    }
                }
            }
        }
    }
}
