package com.example.blockblast.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blockblast.model.AssistantMode
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ScoreHeader(
    score: Int,
    highScore: Int,
    onOpenSettings: () -> Unit,
    assistantMode: AssistantMode = AssistantMode.OFF,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getIntegerInstance(Locale.US)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Simple Settings Gear Icon Button
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFF0C0C10))
                .border(1.dp, Color(0xFF1E1E26), CircleShape)
                .testTag("settings_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "Nastavenia",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Center: Best Score on top, Score rn below
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = if (assistantMode != AssistantMode.OFF) Color(0xFFFFD54F) else Color(0xFF8E8E9B),
                    modifier = Modifier.size(12.dp)
                )
                val bestPrefix = if (assistantMode != AssistantMode.OFF) "BEST (${assistantMode.title.uppercase()})" else "BEST"
                Text(
                    text = "$bestPrefix  ${formatter.format(highScore)}",
                    color = Color(0xFF8E8E9B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            AnimatedContent(
                targetState = score,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> height / 2 } + fadeIn() togetherWith
                                slideOutVertically { height -> -height / 2 } + fadeOut()
                    } else {
                        fadeIn() togetherWith fadeOut()
                    }
                },
                label = "score_display_anim"
            ) { animatedScore ->
                Text(
                    text = formatter.format(animatedScore),
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // Right: Invisible spacer of matching size to keep the center strictly centered
        Box(modifier = Modifier.size(42.dp))
    }
}

