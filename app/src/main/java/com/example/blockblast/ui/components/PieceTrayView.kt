package com.example.blockblast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.blockblast.logic.DragState
import com.example.blockblast.model.BlockPiece
import com.example.blockblast.model.GameState

@Composable
fun PieceTrayView(
    gameState: GameState,
    dragState: DragState,
    canPieceFit: (BlockPiece) -> Boolean,
    onStartDrag: (Int, Offset) -> Unit,
    onUpdateDrag: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onSelectPiece: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (index in 0 until 3) {
            val piece = gameState.candidatePieces.getOrNull(index)
            val isBeingDragged = dragState.pieceIndex == index
            val isSelected = gameState.selectedPieceIndex == index
            val canFit = piece != null && canPieceFit(piece)

            PieceTraySlot(
                index = index,
                piece = piece,
                isBeingDragged = isBeingDragged,
                isSelected = isSelected,
                canFit = canFit,
                isPuzzleBorderEnabled = gameState.isPuzzleBorderEnabled,
                onStartDrag = onStartDrag,
                onUpdateDrag = onUpdateDrag,
                onEndDrag = onEndDrag,
                onSelectPiece = onSelectPiece,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun PieceTraySlot(
    index: Int,
    piece: BlockPiece?,
    isBeingDragged: Boolean,
    isSelected: Boolean,
    canFit: Boolean,
    isPuzzleBorderEnabled: Boolean,
    onStartDrag: (Int, Offset) -> Unit,
    onUpdateDrag: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onSelectPiece: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var slotBoundsInRoot by remember { mutableStateOf(Rect.Zero) }

    val currentPiece by androidx.compose.runtime.rememberUpdatedState(piece)
    val latestOnStartDrag by androidx.compose.runtime.rememberUpdatedState(onStartDrag)
    val latestOnUpdateDrag by androidx.compose.runtime.rememberUpdatedState(onUpdateDrag)
    val latestOnEndDrag by androidx.compose.runtime.rememberUpdatedState(onEndDrag)
    val latestOnSelectPiece by androidx.compose.runtime.rememberUpdatedState(onSelectPiece)

    val slotBorderWidth = when {
        isSelected -> 2.2.dp
        isPuzzleBorderEnabled -> 1.8.dp
        else -> 1.dp
    }
    val slotBorderColor = when {
        isSelected -> Color.White
        isPuzzleBorderEnabled -> Color.White
        else -> Color(0xFF181822)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF08080B))
            .border(
                width = slotBorderWidth,
                color = slotBorderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .onGloballyPositioned { coordinates ->
                slotBoundsInRoot = coordinates.boundsInRoot()
            }
            .pointerInput(index) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val p = currentPiece ?: return@awaitEachGesture

                    var currentTouch = slotBoundsInRoot.topLeft + down.position
                    var totalDistanceMoved = 0f

                    // Immediately start dragging and send first coordinate
                    latestOnStartDrag(index, currentTouch)
                    latestOnUpdateDrag(currentTouch)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.pressed) {
                            val changeOffset = change.positionChange()
                            if (changeOffset != Offset.Zero) {
                                totalDistanceMoved += changeOffset.getDistance()
                                currentTouch += changeOffset
                                latestOnUpdateDrag(currentTouch)
                                change.consume()
                            }
                        } else {
                            change.consume()
                            if (totalDistanceMoved >= 12f) {
                                // It was a drag / swipe gesture -> drop the piece
                                latestOnEndDrag()
                            } else {
                                // Tap in place without movement
                                latestOnEndDrag()
                                latestOnSelectPiece(index)
                            }
                            break
                        }
                    }
                }
            }
            .testTag("piece_slot_$index"),
        contentAlignment = Alignment.Center
    ) {
        // Completely hide the piece from the tray while it is being dragged so it is never shown twice!
        if (piece != null && !isBeingDragged) {
            BlockPieceRenderer(
                piece = piece,
                alpha = if (!canFit) 0.35f else 1f,
                glowAlpha = if (isSelected) 0.55f else 0.3f,
                blockSizeRatio = 0.72f,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            )
        }
    }
}

