package com.example.blockblast.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Draws a minimalist glowing white block with depth, soft outer glow halo,
 * inner specular highlight, and bottom-right shadow.
 */
fun DrawScope.drawGlowingWhiteBlock(
    topLeft: Offset,
    size: Size,
    cornerRadiusPx: Float = 14f,
    glowAlpha: Float = 0.35f,
    scale: Float = 1f,
    isBlasting: Boolean = false,
    alpha: Float = 1f
) {
    val scaledSize = Size(size.width * scale, size.height * scale)
    val center = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f)
    val actualTopLeft = Offset(center.x - scaledSize.width / 2f, center.y - scaledSize.height / 2f)

    if (isBlasting) {
        // Intense blast glow explosion
        val blastGlowRadius = scaledSize.width * 0.4f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f * alpha),
            topLeft = Offset(actualTopLeft.x - blastGlowRadius, actualTopLeft.y - blastGlowRadius),
            size = Size(scaledSize.width + blastGlowRadius * 2, scaledSize.height + blastGlowRadius * 2),
            cornerRadius = CornerRadius(cornerRadiusPx * 2, cornerRadiusPx * 2)
        )
        drawRoundRect(
            color = Color.White.copy(alpha = alpha),
            topLeft = actualTopLeft,
            size = scaledSize,
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )
        return
    }

    // 1. Outer Glow Aura (soft layered halos)
    val glowOffset1 = 4f
    drawRoundRect(
        color = Color.White.copy(alpha = (glowAlpha * 0.25f) * alpha),
        topLeft = Offset(actualTopLeft.x - glowOffset1, actualTopLeft.y - glowOffset1),
        size = Size(scaledSize.width + glowOffset1 * 2, scaledSize.height + glowOffset1 * 2),
        cornerRadius = CornerRadius(cornerRadiusPx + glowOffset1, cornerRadiusPx + glowOffset1)
    )

    val glowOffset2 = 2f
    drawRoundRect(
        color = Color.White.copy(alpha = (glowAlpha * 0.6f) * alpha),
        topLeft = Offset(actualTopLeft.x - glowOffset2, actualTopLeft.y - glowOffset2),
        size = Size(scaledSize.width + glowOffset2 * 2, scaledSize.height + glowOffset2 * 2),
        cornerRadius = CornerRadius(cornerRadiusPx + glowOffset2, cornerRadiusPx + glowOffset2)
    )

    // 2. Subtle drop shadow beneath the block
    drawRoundRect(
        color = Color(0x77000000),
        topLeft = Offset(actualTopLeft.x + 1.5f, actualTopLeft.y + 3f),
        size = scaledSize,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )

    // 3. Block Core Body - Pure white with subtle top-to-bottom shading
    val coreBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFEDEDF2)
        ),
        startY = actualTopLeft.y,
        endY = actualTopLeft.y + scaledSize.height
    )
    drawRoundRect(
        brush = coreBrush,
        topLeft = actualTopLeft,
        size = scaledSize,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )

    // 4. Inner Specular Highlight (top-left edge glint)
    val highlightStroke = 1.8f
    val highlightBrush = Brush.linearGradient(
        colors = listOf(
            Color.White,
            Color(0x88FFFFFF),
            Color(0x00FFFFFF)
        ),
        start = actualTopLeft,
        end = Offset(actualTopLeft.x + scaledSize.width * 0.7f, actualTopLeft.y + scaledSize.height * 0.7f)
    )
    drawRoundRect(
        brush = highlightBrush,
        topLeft = Offset(actualTopLeft.x + highlightStroke / 2, actualTopLeft.y + highlightStroke / 2),
        size = Size(scaledSize.width - highlightStroke, scaledSize.height - highlightStroke),
        cornerRadius = CornerRadius(cornerRadiusPx - highlightStroke / 2, cornerRadiusPx - highlightStroke / 2),
        style = Stroke(width = highlightStroke)
    )
}

/**
 * Draws an empty grid slot with a sleek subtle tint and refined border
 */
fun DrawScope.drawEmptySlot(
    topLeft: Offset,
    size: Size,
    cornerRadiusPx: Float = 12f
) {
    // Cell background - subtly, gently lighter so the 8x8 grid cells are clearly visible
    drawRoundRect(
        color = Color(0xFF14141E),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )

    // Subtle dark border for clean definition
    drawRoundRect(
        color = Color(0xFF252535),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        style = Stroke(width = 1.2f)
    )

    // Subtle inner top shadow
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(topLeft.x + 1f, topLeft.y + 1f),
        size = Size(size.width - 2f, 3f),
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )
}

/**
 * Draws a glowing ghost preview cell for dragging feedback
 */
fun DrawScope.drawGhostBlock(
    topLeft: Offset,
    size: Size,
    cornerRadiusPx: Float = 14f,
    pulseAlpha: Float = 0.5f
) {
    // Translucent white fill
    drawRoundRect(
        color = Color.White.copy(alpha = 0.22f * pulseAlpha),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    )

    // Glowing border
    drawRoundRect(
        color = Color.White.copy(alpha = 0.75f * pulseAlpha),
        topLeft = topLeft,
        size = size,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
        style = Stroke(width = 2.2f)
    )
}
