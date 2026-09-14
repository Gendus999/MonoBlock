package com.example.blockblast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.blockblast.logic.BlockBlastViewModel
import com.example.blockblast.ui.components.BlockBlastBoard
import com.example.blockblast.ui.components.DraggingPieceOverlay
import com.example.blockblast.ui.components.GameOverOverlay
import com.example.blockblast.ui.components.PieceTrayView
import com.example.blockblast.ui.components.ScoreHeader
import com.example.blockblast.ui.components.SettingsDialog

@Composable
fun BlockBlastScreen(
    viewModel: BlockBlastViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val dragState by viewModel.dragState.collectAsStateWithLifecycle()

    val density = LocalDensity.current
    val liftOffsetPx = with(density) { 88.dp.toPx() }

    var boardBounds by remember { mutableStateOf(Rect.Zero) }
    var boardCellSizePx by remember { mutableFloatStateOf(0f) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 480.dp)
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header with Settings Gear and Centered Score
            ScoreHeader(
                score = gameState.score,
                highScore = gameState.highScore,
                assistantMode = gameState.assistantMode,
                onOpenSettings = { isSettingsOpen = true }
            )

            // 2. Play Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                BlockBlastBoard(
                    gameState = gameState,
                    dragState = dragState,
                    onBoardPositioned = { bounds, cellSize ->
                        boardBounds = bounds
                        boardCellSizePx = cellSize
                    },
                    onCellClicked = { r, c ->
                        viewModel.onBoardCellClicked(r, c)
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Candidate Pieces Tray
            PieceTrayView(
                gameState = gameState,
                dragState = dragState,
                canPieceFit = { piece ->
                    viewModel.canPieceFitAnywhere(piece, gameState.grid)
                },
                onStartDrag = { index, touchPos ->
                    viewModel.startDrag(index, touchPos)
                },
                onUpdateDrag = { touchPos ->
                    viewModel.updateDrag(touchPos, boardBounds, boardCellSizePx, liftOffsetPx)
                },
                onEndDrag = {
                    viewModel.endDrag()
                },
                onSelectPiece = { index ->
                    viewModel.selectPieceForTap(index)
                }
            )
        }

        // 4. Dragging Piece Overlay
        if (dragState.piece != null && dragState.pieceIndex != null) {
            DraggingPieceOverlay(
                dragState = dragState,
                boardCellSizePx = boardCellSizePx,
                liftOffsetPx = liftOffsetPx
            )
        }

        // 5. Settings Modal
        SettingsDialog(
            isOpen = isSettingsOpen,
            currentAssistantMode = gameState.assistantMode,
            highScoresByMode = gameState.highScoresByMode,
            onSelectAssistantMode = { viewModel.setAssistantMode(it) },
            isHapticsEnabled = gameState.isHapticsEnabled,
            onToggleHaptics = { viewModel.toggleHaptics() },
            isExtraordinaryEnabled = gameState.isExtraordinaryEnabled,
            onToggleExtraordinary = { viewModel.toggleExtraordinaryBlocks() },
            isWhiteBorderEnabled = gameState.isWhiteBorderEnabled,
            onToggleWhiteBorder = { viewModel.toggleWhiteBorder() },
            isGridBorderEnabled = gameState.isGridBorderEnabled,
            onToggleGridBorder = { viewModel.toggleGridBorder() },
            isPuzzleBorderEnabled = gameState.isPuzzleBorderEnabled,
            onTogglePuzzleBorder = { viewModel.togglePuzzleBorder() },
            onRestartGame = { viewModel.restartGame() },
            onDismiss = { isSettingsOpen = false }
        )

        // 6. Game Over Modal
        GameOverOverlay(
            isVisible = gameState.isGameOver,
            score = gameState.score,
            highScore = gameState.highScore,
            onRestartClicked = { viewModel.restartGame() }
        )
    }
}

