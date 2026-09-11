package com.example.blockblast.model

import java.util.UUID
import kotlin.random.Random

data class BlockPiece(
    val id: String = UUID.randomUUID().toString(),
    val matrix: List<List<Boolean>>,
    val colorVariant: Int = 0 // for subtle silver/white tint variation
) {
    val height: Int = matrix.size
    val width: Int = if (matrix.isNotEmpty()) matrix[0].size else 0
    val blockCount: Int = matrix.sumOf { row -> row.count { it } }

    companion object {
        // Classic / Basic shapes (always available, includes the classic basic 3x3 solid block)
        val BASIC_SHAPES: List<List<List<Boolean>>> = listOf(
            // 1x1 Dot
            listOf(listOf(true)),

            // 1x2 Horizontal & Vertical
            listOf(listOf(true, true)),
            listOf(listOf(true), listOf(true)),

            // 1x3 Horizontal & Vertical
            listOf(listOf(true, true, true)),
            listOf(listOf(true), listOf(true), listOf(true)),

            // 1x4 Horizontal & Vertical
            listOf(listOf(true, true, true, true)),
            listOf(listOf(true), listOf(true), listOf(true), listOf(true)),

            // 1x5 Horizontal & Vertical
            listOf(listOf(true, true, true, true, true)),
            listOf(listOf(true), listOf(true), listOf(true), listOf(true), listOf(true)),

            // 2x2 Square
            listOf(
                listOf(true, true),
                listOf(true, true)
            ),

            // Basic 3x3 Solid Square (9 blocks - classic)
            listOf(
                listOf(true, true, true),
                listOf(true, true, true),
                listOf(true, true, true)
            ),

            // Small Corners (2x2 - 3 blocks)
            listOf(
                listOf(true, false),
                listOf(true, true)
            ),
            listOf(
                listOf(false, true),
                listOf(true, true)
            ),
            listOf(
                listOf(true, true),
                listOf(true, false)
            ),
            listOf(
                listOf(true, true),
                listOf(false, true)
            ),

            // Big Corners (3x3 - 5 blocks)
            listOf(
                listOf(true, false, false),
                listOf(true, false, false),
                listOf(true, true, true)
            ),
            listOf(
                listOf(false, false, true),
                listOf(false, false, true),
                listOf(true, true, true)
            ),
            listOf(
                listOf(true, true, true),
                listOf(true, false, false),
                listOf(true, false, false)
            ),
            listOf(
                listOf(true, true, true),
                listOf(false, false, true),
                listOf(false, false, true)
            ),

            // Medium L-Shapes (3x2 & 2x3 - 4 blocks)
            listOf(
                listOf(true, false),
                listOf(true, false),
                listOf(true, true)
            ),
            listOf(
                listOf(false, true),
                listOf(false, true),
                listOf(true, true)
            ),
            listOf(
                listOf(true, true, true),
                listOf(true, false, false)
            ),
            listOf(
                listOf(true, true, true),
                listOf(false, false, true)
            ),
            listOf(
                listOf(true, true),
                listOf(true, false),
                listOf(true, false)
            ),
            listOf(
                listOf(true, true),
                listOf(false, true),
                listOf(false, true)
            ),
            listOf(
                listOf(1, 0, 0).map { it == 1 },
                listOf(1, 1, 1).map { it == 1 }
            ),
            listOf(
                listOf(0, 0, 1).map { it == 1 },
                listOf(1, 1, 1).map { it == 1 }
            ),

            // T-Shapes (4 blocks)
            listOf(
                listOf(true, true, true),
                listOf(false, true, false)
            ),
            listOf(
                listOf(false, true, false),
                listOf(true, true, true)
            ),
            listOf(
                listOf(true, false),
                listOf(true, true),
                listOf(true, false)
            ),
            listOf(
                listOf(false, true),
                listOf(true, true),
                listOf(false, true)
            ),

            // Z & S shapes (4 blocks)
            listOf(
                listOf(true, true, false),
                listOf(false, true, true)
            ),
            listOf(
                listOf(false, true, true),
                listOf(true, true, false)
            ),
            listOf(
                listOf(true, false),
                listOf(true, true),
                listOf(false, true)
            ),
            listOf(
                listOf(false, true),
                listOf(true, true),
                listOf(true, false)
            ),

            // 2x3 Rectangles
            listOf(
                listOf(true, true, true),
                listOf(true, true, true)
            ),
            listOf(
                listOf(true, true),
                listOf(true, true),
                listOf(true, true)
            )
        )

        // Special Extraordinary 3x3 shapes (C-shape, U-shape, +, Frame, Diagonals, etc.)
        val EXTRAORDINARY_3X3_SHAPES: List<List<List<Boolean>>> = listOf(
            // C-Shape & Reversed C-Shape
            listOf(
                listOf(true, true, true),
                listOf(true, false, false),
                listOf(true, true, true)
            ),
            listOf(
                listOf(true, true, true),
                listOf(false, false, true),
                listOf(true, true, true)
            ),

            // U-Shape & Inverted U-Shape
            listOf(
                listOf(true, false, true),
                listOf(true, false, true),
                listOf(true, true, true)
            ),
            listOf(
                listOf(true, true, true),
                listOf(true, false, true),
                listOf(true, false, true)
            ),

            // 3x3 Plus / Cross (+)
            listOf(
                listOf(false, true, false),
                listOf(true, true, true),
                listOf(false, true, false)
            ),

            // 3x3 Hollow Frame (8 blocks)
            listOf(
                listOf(true, true, true),
                listOf(true, false, true),
                listOf(true, true, true)
            ),

            // 3x3 Diagonal Lines
            listOf(
                listOf(true, false, false),
                listOf(false, true, false),
                listOf(false, false, true)
            ),
            listOf(
                listOf(false, false, true),
                listOf(false, true, false),
                listOf(true, false, false)
            ),

            // 3x3 Big T / Hammer Shapes
            listOf(
                listOf(true, true, true),
                listOf(false, true, false),
                listOf(false, true, false)
            ),
            listOf(
                listOf(false, true, false),
                listOf(false, true, false),
                listOf(true, true, true)
            )
        )

        val ALL_SHAPES: List<List<List<Boolean>>> = BASIC_SHAPES + EXTRAORDINARY_3X3_SHAPES

        fun randomPiece(allowExtraordinary: Boolean = false): BlockPiece {
            val pool = if (allowExtraordinary) ALL_SHAPES else BASIC_SHAPES
            val shape = pool.random()
            return BlockPiece(
                matrix = shape,
                colorVariant = Random.nextInt(0, 3)
            )
        }

        fun generatePieceSet(allowExtraordinary: Boolean = false): List<BlockPiece> {
            val pool = if (allowExtraordinary) ALL_SHAPES else BASIC_SHAPES
            val smallShapes = pool.filter { shape ->
                shape.sumOf { row -> row.count { it } } <= 4
            }

            // High probability (~45%) of including a special extraordinary 3x3 piece when enabled
            val shape1 = if (allowExtraordinary && Random.nextFloat() < 0.45f) {
                EXTRAORDINARY_3X3_SHAPES.random()
            } else {
                pool.random()
            }
            val shape2 = smallShapes.random() // Guarantees playability
            val shape3 = pool.random()

            val pieces = listOf(shape1, shape2, shape3).shuffled().map { shape ->
                BlockPiece(matrix = shape, colorVariant = Random.nextInt(0, 3))
            }
            return pieces
        }
    }
}
