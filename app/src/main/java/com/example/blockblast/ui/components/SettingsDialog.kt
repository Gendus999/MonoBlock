package com.example.blockblast.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blockblast.model.AssistantMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    currentAssistantMode: AssistantMode,
    highScoresByMode: Map<AssistantMode, Int>,
    onSelectAssistantMode: (AssistantMode) -> Unit,
    isHapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    isExtraordinaryEnabled: Boolean,
    onToggleExtraordinary: () -> Unit,
    isWhiteBorderEnabled: Boolean,
    onToggleWhiteBorder: () -> Unit,
    isGridBorderEnabled: Boolean,
    onToggleGridBorder: () -> Unit,
    isPuzzleBorderEnabled: Boolean,
    onTogglePuzzleBorder: () -> Unit,
    onRestartGame: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getIntegerInstance(Locale.US)

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.92f),
        exit = fadeOut(tween(150)) + scaleOut(tween(150)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.90f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF07070A))
                    .border(1.dp, Color(0xFF22222E), RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .testTag("settings_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NASTAVENIA",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16161E))
                                .testTag("close_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Zavrieť nastavenia",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 1. REKORDY SKÓRE PODĽA POMOCNÍKA (HIGH SCORES SECTION)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F0F16))
                            .border(1.dp, Color(0xFF232332), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "REKORDY SKÓRE",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // Required order: 1. Lot, 2. Medium, 3. Low, 4. Off
                        val scoreRows = listOf(
                            Triple("1. Lot", AssistantMode.LOT, Color(0xFFFF6E9C)),
                            Triple("2. Medium", AssistantMode.MEDIUM, Color(0xFFFFB300)),
                            Triple("3. Low", AssistantMode.LOW, Color(0xFF4CAF50)),
                            Triple("4. Off", AssistantMode.OFF, Color(0xFF9E9EAA))
                        )

                        scoreRows.forEach { (label, mode, accentColor) ->
                            val score = highScoresByMode[mode] ?: 0
                            val isActive = mode == currentAssistantMode

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) Color(0xFF1B1B26) else Color(0xFF0A0A10))
                                    .border(
                                        width = 1.dp,
                                        color = if (isActive) accentColor.copy(alpha = 0.6f) else Color(0xFF181822),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accentColor)
                                    )
                                    Text(
                                        text = label,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                    if (isActive) {
                                        Text(
                                            text = "(aktívny)",
                                            color = accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    text = formatter.format(score),
                                    color = if (score > 0) Color.White else Color(0xFF6B6B78),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // 2. POMOCNÍK SETTING (OFF / LOW / MEDIUM / LOT)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F0F16))
                            .border(1.dp, Color(0xFF232332), RoundedCornerShape(18.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "POMOCNÍK",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }

                        // 4 Mode selection buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val selectableModes = listOf(
                                AssistantMode.OFF,
                                AssistantMode.LOW,
                                AssistantMode.MEDIUM,
                                AssistantMode.LOT
                            )

                            selectableModes.forEach { mode ->
                                val isSelected = mode == currentAssistantMode
                                val btnColor = when (mode) {
                                    AssistantMode.LOT -> Color(0xFFFF6E9C)
                                    AssistantMode.MEDIUM -> Color(0xFFFFB300)
                                    AssistantMode.LOW -> Color(0xFF4CAF50)
                                    AssistantMode.OFF -> Color(0xFF9E9EAA)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) btnColor else Color(0xFF14141E))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFF20202E),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSelectAssistantMode(mode) }
                                        .padding(vertical = 10.dp)
                                        .testTag("assistant_mode_${mode.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode.title,
                                        color = if (isSelected) Color.Black else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Description of current assistant mode
                        Text(
                            text = currentAssistantMode.description,
                            color = Color(0xFF9696A8),
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }

                    // Section Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ZOBRAZENIE A OVLÁDANIE",
                            color = Color(0xFF7A7A8E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Vibrations Setting Row
                    SettingToggleRow(
                        icon = Icons.Rounded.Vibration,
                        title = "Vibrácie",
                        subtitle = if (isHapticsEnabled) "Zapnuté" else "Vypnuté",
                        isChecked = isHapticsEnabled,
                        onToggle = onToggleHaptics,
                        testTag = "vibration_switch"
                    )

                    // Extraordinary Blocks Setting Row
                    SettingToggleRow(
                        icon = Icons.Rounded.AutoAwesome,
                        title = "Extraordinary Blocks",
                        subtitle = if (isExtraordinaryEnabled) "Špeciálne tvary (C, U, kríž, rám...)" else "Vypnuté (iba klasické tvary)",
                        isChecked = isExtraordinaryEnabled,
                        onToggle = onToggleExtraordinary,
                        testTag = "extraordinary_blocks_switch"
                    )

                    // White Board Border Setting Row
                    SettingToggleRow(
                        icon = Icons.Rounded.CropSquare,
                        title = "Svetlý biely rámček",
                        subtitle = if (isWhiteBorderEnabled) "Zapnutý (zvýraznené plátno)" else "Vypnutý (jemný tmavý okraj)",
                        isChecked = isWhiteBorderEnabled,
                        onToggle = onToggleWhiteBorder,
                        testTag = "white_border_switch"
                    )

                    // Grid Cells White Border Setting Row
                    SettingToggleRow(
                        icon = Icons.Rounded.GridView,
                        title = "Biele políčka plátna",
                        subtitle = if (isGridBorderEnabled) "Zapnuté (biele obrysy 8×8 kociek)" else "Vypnuté (pôvodné tmavé políčka)",
                        isChecked = isGridBorderEnabled,
                        onToggle = onToggleGridBorder,
                        testTag = "grid_border_switch"
                    )

                    // Puzzle White Border Setting Row
                    SettingToggleRow(
                        icon = Icons.Rounded.Extension,
                        title = "Biely rámček Puzzle",
                        subtitle = if (isPuzzleBorderEnabled) "Zapnutý (zvýraznené puzzle dieliky)" else "Vypnutý (jemný tmavý okraj)",
                        isChecked = isPuzzleBorderEnabled,
                        onToggle = onTogglePuzzleBorder,
                        testTag = "puzzle_border_switch"
                    )

                    // Restart Game Row / Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F0F16))
                            .border(1.dp, Color(0xFF232332), RoundedCornerShape(16.dp))
                            .clickable {
                                onRestartGame()
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("restart_game_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Reštartovať hru",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vynulovať aktuálne skóre a dosku",
                                    color = Color(0xFF7A7A88),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                onRestartGame()
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .testTag("restart_game_dialog_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Reštartovať",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isChecked) Color(0xFF13131A) else Color(0xFF0F0F16))
            .border(
                width = 1.dp,
                color = if (isChecked) Color(0xFF282836) else Color(0xFF1E1E2A),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) Color.White else Color(0xFF7A7A88),
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = if (isChecked) Color(0xFFA0A0B0) else Color(0xFF6B6B78),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color.White,
                checkedBorderColor = Color.White,
                uncheckedThumbColor = Color(0xFFA8A8B8),
                uncheckedTrackColor = Color(0xFF262634),
                uncheckedBorderColor = Color(0xFF3E3E50)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
