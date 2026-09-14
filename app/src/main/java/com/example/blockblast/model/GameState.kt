package com.example.blockblast.model

const val GRID_SIZE = 8

data class FloatingAlert(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val bonusPoints: Int
)

data class GameState(
    val grid: List<List<Int>> = List(GRID_SIZE) { List(GRID_SIZE) { 0 } },
    val candidatePieces: List<BlockPiece?> = BlockPiece.generatePieceSet(),
    val score: Int = 0,
    val highScore: Int = 0,
    val comboStreak: Int = 0,
    val isGameOver: Boolean = false,
    val blastingCells: Set<Pair<Int, Int>> = emptySet(),
    val floatingAlert: FloatingAlert? = null,
    val selectedPieceIndex: Int? = null,
    val isExtraordinaryEnabled: Boolean = false,
    val isWhiteBorderEnabled: Boolean = true,
    val isPuzzleBorderEnabled: Boolean = false,
    val isGridBorderEnabled: Boolean = true
)
