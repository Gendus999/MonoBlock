package com.example.blockblast.logic

import android.app.Application
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blockblast.audio.GameHaptics
import com.example.blockblast.model.AssistantMode
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
        val legacyHigh = prefs.getInt("HIGH_SCORE", 0)
        val scoreLot = prefs.getInt(AssistantMode.LOT.prefKey, 0)
        val scoreMedium = prefs.getInt(AssistantMode.MEDIUM.prefKey, 0)
        val scoreLow = prefs.getInt(AssistantMode.LOW.prefKey, 0)
        val scoreOff = prefs.getInt(AssistantMode.OFF.prefKey, legacyHigh)

        val scoresMap = mapOf(
            AssistantMode.LOT to scoreLot,
            AssistantMode.MEDIUM to scoreMedium,
            AssistantMode.LOW to scoreLow,
            AssistantMode.OFF to scoreOff
        )

        val savedModeStr = prefs.getString("ASSISTANT_MODE", AssistantMode.OFF.name)
        val savedAssistantMode = AssistantMode.fromString(savedModeStr)

        val savedExtraordinary = prefs.getBoolean("EXTRAORDINARY_BLOCKS", false)
        val savedWhiteBorder = prefs.getBoolean("WHITE_BORDER", true)
        val savedPuzzleBorder = prefs.getBoolean("PUZZLE_BORDER", false)
        val savedGridBorder = prefs.getBoolean("GRID_BORDER", true)
        val savedHaptics = prefs.getBoolean("HAPTICS_ENABLED", true)
        haptics.isHapticsEnabled = savedHaptics

        val currentHigh = scoresMap[savedAssistantMode] ?: 0

        _gameState.update {
            val emptyGrid = List(GRID_SIZE) { List(GRID_SIZE) { 0 } }
            it.copy(
                highScore = currentHigh,
                assistantMode = savedAssistantMode,
                highScoresByMode = scoresMap,
                isExtraordinaryEnabled = savedExtraordinary,
                isWhiteBorderEnabled = savedWhiteBorder,
                isPuzzleBorderEnabled = savedPuzzleBorder,
                isGridBorderEnabled = savedGridBorder,
                isHapticsEnabled = savedHaptics,
                candidatePieces = BlockPiece.generateAssistedPieceSet(
                    grid = emptyGrid,
                    assistantMode = savedAssistantMode,
                    allowExtraordinary = savedExtraordinary
                )
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

        val maxCol = (GRID_SIZE - piece.width).coerceAtLeast(0)
        val maxRow = (GRID_SIZE - piece.height).coerceAtLeast(0)

        // Check if piece is reasonably near the board (reach tolerance for edges)
        val isNearBoard = exactCol >= -1.8f && exactCol <= (maxCol + 1.8f) &&
                          exactRow >= -1.8f && exactRow <= (maxRow + 1.8f)

        var resolvedTarget: Pair<Int, Int>? = null

        if (isNearBoard) {
            val grid = _gameState.value.grid

            // 1. Clamped position for edge attraction:
            // When dragged against the bezel/edges, player intent is the edge column/row
            val clampedCol = exactCol.coerceIn(0f, maxCol.toFloat())
            val clampedRow = exactRow.coerceIn(0f, maxRow.toFloat())

            val idealCol = Math.round(clampedCol)
            val idealRow = Math.round(clampedRow)

            // Direct fit check at clamped ideal cell
            if (canPlace(piece, idealRow, idealCol, grid)) {
                val dRow = clampedRow - idealRow
                val dCol = clampedCol - idealCol
                if (dRow * dRow + dCol * dCol <= 0.70f) {
                    resolvedTarget = Pair(idealRow, idealCol)
                }
            }

            // 2. If not directly placed or slightly offset, search nearest valid placement with magnetic snapping
            if (resolvedTarget == null) {
                var minDistanceSq = Float.MAX_VALUE
                val prevTarget = currentDrag.targetCell

                for (dr in -2..2) {
                    for (dc in -2..2) {
                        val r = idealRow + dr
                        val c = idealCol + dc
                        if (r in 0..maxRow && c in 0..maxCol && canPlace(piece, r, c, grid)) {
                            val dRow = clampedRow - r
                            val dCol = clampedCol - c
                            var distSq = dRow * dRow + dCol * dCol

                            // Apply small hysteresis/stickiness to currently locked target to prevent jitter
                            if (prevTarget != null && prevTarget.first == r && prevTarget.second == c) {
                                distSq *= 0.78f
                            }

                            // High precision magnetic snap radius (1.3 cells)
                            if (distSq < minDistanceSq && distSq <= 1.69f) {
                                minDistanceSq = distSq
                                resolvedTarget = Pair(r, c)
                            }
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

        val maxRow = (GRID_SIZE - piece.height).coerceAtLeast(0)
        val maxCol = (GRID_SIZE - piece.width).coerceAtLeast(0)

        val idealRow = (row - piece.height / 2).coerceIn(0, maxRow)
        val idealCol = (col - piece.width / 2).coerceIn(0, maxCol)

        if (canPlace(piece, idealRow, idealCol, state.grid)) {
            placePiece(selectedIdx, idealRow, idealCol)
            _gameState.update { it.copy(selectedPieceIndex = null) }
            return
        }

        // Search nearest valid placement within 2 cells around clicked position
        var bestTarget: Pair<Int, Int>? = null
        var minDistanceSq = Float.MAX_VALUE
        for (dr in -2..2) {
            for (dc in -2..2) {
                val r = idealRow + dr
                val c = idealCol + dc
                if (r in 0..maxRow && c in 0..maxCol && canPlace(piece, r, c, state.grid)) {
                    val distSq = (dr * dr + dc * dc).toFloat()
                    if (distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        bestTarget = Pair(r, c)
                    }
                }
            }
        }

        if (bestTarget != null) {
            placePiece(selectedIdx, bestTarget.first, bestTarget.second)
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

        // Grid that new pieces will be generated against
        val effectiveGridForGeneration = if (totalLines > 0) {
            val cleared = updatedGrid.map { it.toMutableList() }
            for (r in fullRows) {
                for (c in 0 until GRID_SIZE) cleared[r][c] = 0
            }
            for (c in fullCols) {
                for (r in 0 until GRID_SIZE) cleared[r][c] = 0
            }
            cleared.map { it.toList() }
        } else {
            updatedGrid.map { it.toList() }
        }

        // If all 3 pieces are used, generate 3 new ones with assistant helper
        val allUsed = nextCandidates.all { it == null }
        val finalCandidates = if (allUsed) {
            BlockPiece.generateAssistedPieceSet(
                grid = effectiveGridForGeneration,
                assistantMode = state.assistantMode,
                allowExtraordinary = state.isExtraordinaryEnabled
            )
        } else {
            nextCandidates
        }

        val currentMode = state.assistantMode
        val currentModeHigh = state.highScoresByMode[currentMode] ?: 0
        val newHigh = maxOf(newScore, currentModeHigh)
        val updatedHighScores = if (newHigh > currentModeHigh) {
            prefs.edit().putInt(currentMode.prefKey, newHigh).apply()
            state.highScoresByMode + (currentMode to newHigh)
        } else {
            state.highScoresByMode
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

            val postBlastHigh = maxOf(newScore, currentModeHigh)
            val finalHighScores = if (postBlastHigh > currentModeHigh) {
                prefs.edit().putInt(currentMode.prefKey, postBlastHigh).apply()
                updatedHighScores + (currentMode to postBlastHigh)
            } else {
                updatedHighScores
            }

            haptics.blastClear(totalLines)

            _gameState.value = state.copy(
                grid = updatedGrid.map { it.toList() },
                candidatePieces = finalCandidates,
                score = newScore,
                highScore = postBlastHigh,
                highScoresByMode = finalHighScores,
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
            val gameOver = checkGameOver(finalCandidates, updatedGrid.map { it.toList() })
            if (gameOver) {
                haptics.gameOver()
            }

            _gameState.value = state.copy(
                grid = updatedGrid.map { it.toList() },
                candidatePieces = finalCandidates,
                score = newScore,
                highScore = newHigh,
                highScoresByMode = updatedHighScores,
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
        val current = _gameState.value
        val emptyGrid = List(GRID_SIZE) { List(GRID_SIZE) { 0 } }
        val modeHigh = current.highScoresByMode[current.assistantMode] ?: 0
        _gameState.update {
            GameState(
                grid = emptyGrid,
                candidatePieces = BlockPiece.generateAssistedPieceSet(
                    grid = emptyGrid,
                    assistantMode = current.assistantMode,
                    allowExtraordinary = current.isExtraordinaryEnabled
                ),
                score = 0,
                highScore = modeHigh,
                assistantMode = current.assistantMode,
                highScoresByMode = current.highScoresByMode,
                comboStreak = 0,
                isGameOver = false,
                blastingCells = emptySet(),
                floatingAlert = null,
                selectedPieceIndex = null,
                isExtraordinaryEnabled = current.isExtraordinaryEnabled,
                isWhiteBorderEnabled = current.isWhiteBorderEnabled,
                isPuzzleBorderEnabled = current.isPuzzleBorderEnabled,
                isGridBorderEnabled = current.isGridBorderEnabled,
                isHapticsEnabled = current.isHapticsEnabled
            )
        }
    }

    fun setAssistantMode(mode: AssistantMode) {
        prefs.edit().putString("ASSISTANT_MODE", mode.name).apply()
        _gameState.update {
            val emptyGrid = List(GRID_SIZE) { List(GRID_SIZE) { 0 } }
            val modeHigh = it.highScoresByMode[mode] ?: 0
            it.copy(
                assistantMode = mode,
                highScore = modeHigh
            )
        }
    }

    fun toggleExtraordinaryBlocks() {
        val current = _gameState.value.isExtraordinaryEnabled
        val newMode = !current
        prefs.edit().putBoolean("EXTRAORDINARY_BLOCKS", newMode).apply()
        _gameState.update { it.copy(isExtraordinaryEnabled = newMode) }
    }

    fun toggleWhiteBorder() {
        val current = _gameState.value.isWhiteBorderEnabled
        val newMode = !current
        prefs.edit().putBoolean("WHITE_BORDER", newMode).apply()
        _gameState.update { it.copy(isWhiteBorderEnabled = newMode) }
    }

    fun togglePuzzleBorder() {
        val current = _gameState.value.isPuzzleBorderEnabled
        val newMode = !current
        prefs.edit().putBoolean("PUZZLE_BORDER", newMode).apply()
        _gameState.update { it.copy(isPuzzleBorderEnabled = newMode) }
    }

    fun toggleGridBorder() {
        val current = _gameState.value.isGridBorderEnabled
        val newMode = !current
        prefs.edit().putBoolean("GRID_BORDER", newMode).apply()
        _gameState.update { it.copy(isGridBorderEnabled = newMode) }
    }

    fun toggleHaptics() {
        val newMode = !_gameState.value.isHapticsEnabled
        prefs.edit().putBoolean("HAPTICS_ENABLED", newMode).apply()
        haptics.isHapticsEnabled = newMode
        _gameState.update { it.copy(isHapticsEnabled = newMode) }
    }
}
