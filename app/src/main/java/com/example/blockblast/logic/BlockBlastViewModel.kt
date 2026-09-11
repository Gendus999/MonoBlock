package com.example.blockblast.logic

import android.app.Application
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blockblast.audio.GameHaptics
import com.example.blockblast.model.BlockPiece
import com.example.blockblast.model.FloatingAlert
import com.example.blockblast.model.GRID_SIZE
import com.example.blockblast.model.GameState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DragState(
    val pieceIndex: Int? = null,
    val piece: BlockPiece? = null,
    val dragOffset: Offset = Offset.Zero,
    val targetCell: Pair<Int, Int>? = null,
    val isValidPlacement: Boolean = false
)

class BlockBlastViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("block_blast_prefs", Context.MODE_PRIVATE)
    val haptics = GameHaptics(application)

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _dragState = MutableStateFlow(DragState())
    val dragState: StateFlow<DragState> = _dragState.asStateFlow()

    init {
        val savedHigh = prefs.getInt("HIGH_SCORE", 0)
        val savedExtraordinary = prefs.getBoolean("EXTRAORDINARY_BLOCKS", false)
        _gameState.update {
            it.copy(
                highScore = savedHigh,
                isExtraordinaryEnabled = savedExtraordinary,
                candidatePieces = BlockPiece.generatePieceSet(allowExtraordinary = savedExtraordinary)
            )
        }
    }

    fun canPlace(piece: BlockPiece, atRow: Int, atCol: Int, grid: List<List<Int>>): Boolean {
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.matrix[r][c]) {
                    val targetR = atRow + r
                    val targetC = atCol + c
                    if (targetR !in 0 until GRID_SIZE || targetC !in 0 until GRID_SIZE) {
                        return false
                    }
                    if (grid[targetR][targetC] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }

    fun canPieceFitAnywhere(piece: BlockPiece, grid: List<List<Int>>): Boolean {
        for (r in 0..(GRID_SIZE - piece.height)) {
            for (c in 0..(GRID_SIZE - piece.width)) {
                if (canPlace(piece, r, c, grid)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkGameOver(pieces: List<BlockPiece?>, grid: List<List<Int>>): Boolean {
        val activePieces = pieces.filterNotNull()
        if (activePieces.isEmpty()) return false
        return activePieces.none { piece -> canPieceFitAnywhere(piece, grid) }
    }

    fun startDrag(index: Int, touchPos: Offset) {
        val state = _gameState.value
        if (state.isGameOver || state.blastingCells.isNotEmpty()) return
        val piece = state.candidatePieces.getOrNull(index) ?: return

        haptics.piecePickup()
        _dragState.value = DragState(
            pieceIndex = index,
            piece = piece,
            dragOffset = touchPos,
            targetCell = null,
            isValidPlacement = false
        )
        // clear selected piece if tap mode was active
        _gameState.update { it.copy(selectedPieceIndex = null) }
    }

    fun updateDrag(touchPos: Offset, boardBounds: Rect, cellSizePx: Float, liftOffsetPx: Float) {
        val currentDrag = _dragState.value
        val piece = currentDrag.piece ?: return
        val pieceIndex = currentDrag.pieceIndex ?: return

        val spacing = 3.5f
        val step = cellSizePx + spacing

        val pieceWidthPx = piece.width * cellSizePx + (piece.width - 1) * spacing
        val pieceHeightPx = piece.height * cellSizePx + (piece.height - 1) * spacing

        // Exact anchor matching DraggingPieceOverlay
        val pieceTopLeftX = touchPos.x - pieceWidthPx / 2f
        val pieceTopLeftY = touchPos.y - liftOffsetPx - pieceHeightPx / 2f

        val relX = pieceTopLeftX - boardBounds.left
        val relY = pieceTopLeftY - boardBounds.top

        val exactCol = if (step > 0) relX / step else 0f
        val exactRow = if (step > 0) relY / step else 0f

        val idealCol = Math.round(exactCol)
        val idealRow = Math.round(exactRow)

        val grid = _gameState.value.grid

        // Accurate placement resolution:
        // 1. Check direct rounded position
        var resolvedTarget: Pair<Int, Int>? = null
        if (canPlace(piece, idealRow, idealCol, grid)) {
            resolvedTarget = Pair(idealRow, idealCol)
        } else {
            // 2. Magnetic snapping to nearest valid candidate within reach
            var minDistanceSq = Float.MAX_VALUE
            for (dr in -1..1) {
                for (dc in -1..1) {
                    val r = idealRow + dr
                    val c = idealCol + dc
                    if (canPlace(piece, r, c, grid)) {
                        val dRow = exactRow - r
                        val dCol = exactCol - c
                        val distSq = dRow * dRow + dCol * dCol
                        // Generous magnetic threshold (~0.77 cell radius)
                        if (distSq < minDistanceSq && distSq <= 0.60f) {
                            minDistanceSq = distSq
                            resolvedTarget = Pair(r, c)
                        }
                    }
                }
            }
        }

        _dragState.value = currentDrag.copy(
            dragOffset = touchPos,
            targetCell = resolvedTarget,
            isValidPlacement = resolvedTarget != null
        )
    }

    fun endDrag() {
        val currentDrag = _dragState.value
        val index = currentDrag.pieceIndex
        val target = currentDrag.targetCell

        if (index != null && target != null && currentDrag.isValidPlacement) {
            placePiece(index, target.first, target.second)
        }

        _dragState.value = DragState()
    }

    fun selectPieceForTap(index: Int) {
        val state = _gameState.value
        if (state.isGameOver || state.blastingCells.isNotEmpty()) return
        if (state.candidatePieces.getOrNull(index) == null) return

        haptics.piecePickup()
        _gameState.update {
            if (it.selectedPieceIndex == index) {
                it.copy(selectedPieceIndex = null)
            } else {
                it.copy(selectedPieceIndex = index)
            }
        }
    }

    fun onBoardCellClicked(row: Int, col: Int) {
        val state = _gameState.value
        val selectedIdx = state.selectedPieceIndex ?: return
        val piece = state.candidatePieces.getOrNull(selectedIdx) ?: return

        // For tap-to-place: anchor at clicked cell (top-left or centered)
        val targetRow = (row - piece.height / 2).coerceIn(0, GRID_SIZE - piece.height)
        val targetCol = (col - piece.width / 2).coerceIn(0, GRID_SIZE - piece.width)

        if (canPlace(piece, targetRow, targetCol, state.grid)) {
            placePiece(selectedIdx, targetRow, targetCol)
            _gameState.update { it.copy(selectedPieceIndex = null) }
        }
    }

    fun placePiece(pieceIndex: Int, atRow: Int, atCol: Int) {
        val state = _gameState.value
        val piece = state.candidatePieces.getOrNull(pieceIndex) ?: return
        if (!canPlace(piece, atRow, atCol, state.grid)) return

        haptics.piecePlaced()

        // 1. Stamp piece onto the grid
        val updatedGrid = state.grid.map { it.toMutableList() }
        for (r in 0 until piece.height) {
            for (c in 0 until piece.width) {
                if (piece.matrix[r][c]) {
                    updatedGrid[atRow + r][atCol + c] = 1
                }
            }
        }

        val basePoints = piece.blockCount * 10
        var newScore = state.score + basePoints

        // 2. Identify full rows and columns
        val fullRows = mutableListOf<Int>()
        for (r in 0 until GRID_SIZE) {
            if (updatedGrid[r].all { it > 0 }) {
                fullRows.add(r)
            }
        }

        val fullCols = mutableListOf<Int>()
        for (c in 0 until GRID_SIZE) {
            if ((0 until GRID_SIZE).all { r -> updatedGrid[r][c] > 0 }) {
                fullCols.add(c)
            }
        }

        val totalLines = fullRows.size + fullCols.size

        // 3. Update candidate pieces (remove placed piece)
        val nextCandidates = state.candidatePieces.toMutableList()
        nextCandidates[pieceIndex] = null

        // If all 3 pieces are used, generate 3 new ones
        val allUsed = nextCandidates.all { it == null }
        val finalCandidates = if (allUsed) {
            BlockPiece.generatePieceSet(allowExtraordinary = state.isExtraordinaryEnabled)
        } else {
            nextCandidates
        }

        if (totalLines > 0) {
            // Lines completed!
            val newCombo = state.comboStreak + 1
            val linePoints = when (totalLines) {
                1 -> 100
                2 -> 300
                3 -> 600
                4 -> 1000
                else -> totalLines * 250
            }
            val comboBonus = if (newCombo > 1) (newCombo * 120) else 0
            val addedScore = linePoints + comboBonus
            newScore += addedScore

            val blastCoords = mutableSetOf<Pair<Int, Int>>()
            for (r in fullRows) {
                for (c in 0 until GRID_SIZE) {
                    blastCoords.add(Pair(r, c))
                }
            }
            for (c in fullCols) {
                for (r in 0 until GRID_SIZE) {
                    blastCoords.add(Pair(r, c))
                }
            }

            val alertText = when {
                totalLines >= 4 -> "SUPER BLAST!"
                totalLines >= 3 -> "TRIPLE BLAST!"
                totalLines == 2 -> "DOUBLE BLAST!"
                newCombo > 1 -> "COMBO x$newCombo!"
                else -> "BLAST!"
            }

            val newHigh = maxOf(newScore, state.highScore)
            if (newHigh > state.highScore) {
                prefs.edit().putInt("HIGH_SCORE", newHigh).apply()
            }

            haptics.blastClear(totalLines)

            _gameState.value = state.copy(
                grid = updatedGrid.map { it.toList() },
                candidatePieces = finalCandidates,
                score = newScore,
                highScore = newHigh,
                comboStreak = newCombo,
                blastingCells = blastCoords,
                floatingAlert = null,
                selectedPieceIndex = null
            )

            // Snappy cell clearing for blast vanish animation
            viewModelScope.launch {
                delay(220)
                val clearedGrid = _gameState.value.grid.map { it.toMutableList() }
                for ((r, c) in blastCoords) {
                    clearedGrid[r][c] = 0
                }
                val finalGrid = clearedGrid.map { it.toList() }
                val gameOver = checkGameOver(finalCandidates, finalGrid)

                if (gameOver) {
                    haptics.gameOver()
                }

                _gameState.update {
                    it.copy(
                        grid = finalGrid,
                        blastingCells = emptySet(),
                        isGameOver = gameOver
                    )
                }

                // Clear floating alert after 1.2s
                delay(900)
                _gameState.update { it.copy(floatingAlert = null) }
            }
        } else {
            // No lines cleared
            val newHigh = maxOf(newScore, state.highScore)
            if (newHigh > state.highScore) {
                prefs.edit().putInt("HIGH_SCORE", newHigh).apply()
            }

            val gameOver = checkGameOver(finalCandidates, updatedGrid.map { it.toList() })
            if (gameOver) {
                haptics.gameOver()
            }

            _gameState.value = state.copy(
                grid = updatedGrid.map { it.toList() },
                candidatePieces = finalCandidates,
                score = newScore,
                highScore = newHigh,
                comboStreak = 0,
                blastingCells = emptySet(),
                isGameOver = gameOver,
                selectedPieceIndex = null
            )
        }
    }

    fun restartGame() {
        haptics.piecePickup()
        _dragState.value = DragState()
        val isExtra = _gameState.value.isExtraordinaryEnabled
        _gameState.update {
            GameState(
                grid = List(GRID_SIZE) { List(GRID_SIZE) { 0 } },
                candidatePieces = BlockPiece.generatePieceSet(allowExtraordinary = isExtra),
                score = 0,
                highScore = it.highScore,
                comboStreak = 0,
                isGameOver = false,
                blastingCells = emptySet(),
                floatingAlert = null,
                selectedPieceIndex = null,
                isExtraordinaryEnabled = isExtra
            )
        }
    }

    fun toggleExtraordinaryBlocks() {
        val current = _gameState.value.isExtraordinaryEnabled
        val newMode = !current
        prefs.edit().putBoolean("EXTRAORDINARY_BLOCKS", newMode).apply()
        _gameState.update { it.copy(isExtraordinaryEnabled = newMode) }
    }

    fun toggleHaptics() {
        haptics.isHapticsEnabled = !haptics.isHapticsEnabled
    }
}
