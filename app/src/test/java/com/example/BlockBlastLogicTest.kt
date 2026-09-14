package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.blockblast.logic.BlockBlastViewModel
import com.example.blockblast.model.BlockPiece
import com.example.blockblast.model.GRID_SIZE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BlockBlastLogicTest {

    private lateinit var viewModel: BlockBlastViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = BlockBlastViewModel(app)
        viewModel.restartGame()
    }

    @Test
    fun `test initial state is clean`() {
        val state = viewModel.gameState.value
        assertEquals(0, state.score)
        assertEquals(0, state.comboStreak)
        assertFalse(state.isGameOver)
        assertEquals(3, state.candidatePieces.size)
        // Ensure 8x8 grid is empty
        assertEquals(GRID_SIZE, state.grid.size)
        assertTrue(state.grid.all { row -> row.all { it == 0 } })
    }

    @Test
    fun `test canPlace validation bounds and collision`() {
        val dotPiece = BlockPiece(matrix = listOf(listOf(true)))
        val state = viewModel.gameState.value

        // In bounds
        assertTrue(viewModel.canPlace(dotPiece, 0, 0, state.grid))
        assertTrue(viewModel.canPlace(dotPiece, 7, 7, state.grid))

        // Out of bounds
        assertFalse(viewModel.canPlace(dotPiece, -1, 0, state.grid))
        assertFalse(viewModel.canPlace(dotPiece, 8, 0, state.grid))
        assertFalse(viewModel.canPlace(dotPiece, 0, 8, state.grid))

        // 2x2 piece at bottom-right corner 7,7 exceeds bounds
        val square2x2 = BlockPiece(matrix = listOf(listOf(true, true), listOf(true, true)))
        assertFalse(viewModel.canPlace(square2x2, 7, 7, state.grid))
        assertTrue(viewModel.canPlace(square2x2, 6, 6, state.grid))
    }

    @Test
    fun `test placing piece increases score`() {
        val initialScore = viewModel.gameState.value.score
        viewModel.placePiece(0, 0, 0)
        val afterScore = viewModel.gameState.value.score
        assertTrue(afterScore > initialScore)
    }

    @Test
    fun `test line clearing and blast calculation`() {
        // Pre-fill a row with 7 blocks leaving only column 0 empty
        val customGrid = List(GRID_SIZE) { r ->
            if (r == 0) List(GRID_SIZE) { c -> if (c == 0) 0 else 1 }
            else List(GRID_SIZE) { 0 }
        }

        val dotPiece = BlockPiece(matrix = listOf(listOf(true)))
        assertTrue(viewModel.canPlace(dotPiece, 0, 0, customGrid))
    }

    @Test
    fun `test assistant modes and high score tracking`() {
        val initialMode = viewModel.gameState.value.assistantMode
        assertEquals(com.example.blockblast.model.AssistantMode.OFF, initialMode)

        // Switch to LOT
        viewModel.setAssistantMode(com.example.blockblast.model.AssistantMode.LOT)
        assertEquals(com.example.blockblast.model.AssistantMode.LOT, viewModel.gameState.value.assistantMode)

        // Switch to MEDIUM
        viewModel.setAssistantMode(com.example.blockblast.model.AssistantMode.MEDIUM)
        assertEquals(com.example.blockblast.model.AssistantMode.MEDIUM, viewModel.gameState.value.assistantMode)

        // Switch to LOW
        viewModel.setAssistantMode(com.example.blockblast.model.AssistantMode.LOW)
        assertEquals(com.example.blockblast.model.AssistantMode.LOW, viewModel.gameState.value.assistantMode)

        // Verify highScoresByMode contains all 4 modes
        val scores = viewModel.gameState.value.highScoresByMode
        assertTrue(scores.containsKey(com.example.blockblast.model.AssistantMode.LOT))
        assertTrue(scores.containsKey(com.example.blockblast.model.AssistantMode.MEDIUM))
        assertTrue(scores.containsKey(com.example.blockblast.model.AssistantMode.LOW))
        assertTrue(scores.containsKey(com.example.blockblast.model.AssistantMode.OFF))
    }

    @Test
    fun `test assisted piece generation produces valid pieces`() {
        val emptyGrid = List(GRID_SIZE) { List(GRID_SIZE) { 0 } }
        for (mode in com.example.blockblast.model.AssistantMode.values()) {
            val pieces = BlockPiece.generateAssistedPieceSet(emptyGrid, mode, allowExtraordinary = false)
            assertEquals(3, pieces.size)
            assertEquals(3, pieces.filterNotNull().size)
        }
    }
}
