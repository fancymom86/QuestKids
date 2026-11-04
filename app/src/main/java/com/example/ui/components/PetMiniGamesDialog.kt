package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PetGrowthStage
import com.example.data.model.PetMiniGameType
import com.example.data.model.PetSpecies
import com.example.ui.theme.QuestMint
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGoldBright
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun PetMiniGameModal(
    miniGame: PetMiniGameType,
    petSpecies: PetSpecies,
    petName: String,
    growthStage: PetGrowthStage,
    onDismiss: () -> Unit,
    onVictory: (starsEarned: Long, xpEarned: Long, treatsEarned: Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pet_minigame_modal_${miniGame.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${miniGame.emoji} ${miniGame.title}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_minigame_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close mini-game"
                        )
                    }
                }

                Text(
                    text = "Playing with $petName • Win to advance +1 Evolution Level!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (miniGame) {
                    PetMiniGameType.STAR_CATCHER -> {
                        StarCatcherDashGame(
                            petSpecies = petSpecies,
                            petName = petName,
                            growthStage = growthStage,
                            onComplete = { stars ->
                                onVictory(stars, miniGame.xpReward, miniGame.treatReward)
                                onDismiss()
                            }
                        )
                    }

                    PetMiniGameType.BUBBLE_MEMORY -> {
                        BubbleMemoryMatchGame(
                            petSpecies = petSpecies,
                            petName = petName,
                            growthStage = growthStage,
                            onComplete = { stars ->
                                onVictory(stars, miniGame.xpReward, miniGame.treatReward)
                                onDismiss()
                            }
                        )
                    }

                    PetMiniGameType.COSMIC_JUMP -> {
                        CosmicTrickJumpGame(
                            petSpecies = petSpecies,
                            petName = petName,
                            growthStage = growthStage,
                            onComplete = { stars ->
                                onVictory(stars, miniGame.xpReward, miniGame.treatReward)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StarCatcherDashGame(
    petSpecies: PetSpecies,
    petName: String,
    growthStage: PetGrowthStage,
    onComplete: (Long) -> Unit
) {
    val targetCatches = 6
    var caughtCount by remember { mutableIntStateOf(0) }
    var petLane by remember { mutableIntStateOf(1) } // 0 = Left, 1 = Center, 2 = Right
    var starLane by remember { mutableIntStateOf(0) }
    var starEmoji by remember { mutableStateOf("⭐") }
    var pulseTick by remember { mutableIntStateOf(0) }

    val infinite = rememberInfiniteTransition(label = "falling_star")
    val starFallProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "star_fall"
    )

    val isWon = caughtCount >= targetCatches

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Stars Caught: $caughtCount / $targetCatches",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Reward: +15 ★ & +45 XP",
                style = MaterialTheme.typography.labelLarge,
                color = QuestRoyalBlue
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { (caughtCount.toFloat() / targetCatches.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = QuestMint
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3-Lane Sky Arena
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(petSpecies.realm.skyTopHex),
                            Color(petSpecies.realm.skyBottomHex)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (lane in 0..2) {
                    val hasStar = starLane == lane && !isWon
                    val hasPet = petLane == lane
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(186.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (hasPet) Color.White.copy(alpha = 0.16f)
                                else Color.Black.copy(alpha = 0.08f)
                            )
                            .clickable {
                                petLane = lane
                                if (starLane == lane && !isWon) {
                                    caughtCount += 1
                                    pulseTick += 1
                                    starLane = (starLane + Random.nextInt(1, 3)) % 3
                                    starEmoji = listOf("⭐", "🌟", "🍓", "💎").random()
                                }
                            }
                            .testTag("star_lane_$lane"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Falling star in this lane
                        Box(
                            modifier = Modifier
                                .height(86.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (hasStar) {
                                Text(
                                    text = starEmoji,
                                    style = MaterialTheme.typography.displaySmall,
                                    modifier = Modifier.offset(y = (starFallProgress * 46f).dp)
                                )
                            }
                        }

                        // Pet in this lane
                        Box(
                            modifier = Modifier.size(78.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasPet) {
                                PetCompanionArtworkCanvas(
                                    species = petSpecies,
                                    growthStage = growthStage,
                                    pulseTick = pulseTick,
                                    modifier = Modifier.size(72.dp)
                                )
                            } else {
                                Text(
                                    text = "Tap Here",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    if (lane < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isWon) {
            Button(
                onClick = { onComplete(15L) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestMint,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("claim_minigame_reward_button")
            ) {
                Text(
                    text = "🎉 Collect +15 ★ & Level Up $petName!",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = {
                    petLane = starLane
                    caughtCount += 1
                    pulseTick += 1
                    starLane = (starLane + 1) % 3
                    starEmoji = listOf("⭐", "🌟", "🍓", "💎").random()
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StarGoldBright,
                    contentColor = Color(0xFF182238)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("catch_star_action_button")
            ) {
                Text(
                    text = "⭐ Leap & Catch Star with $petName! ($caughtCount/$targetCatches)",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun BubbleMemoryMatchGame(
    petSpecies: PetSpecies,
    petName: String,
    growthStage: PetGrowthStage,
    onComplete: (Long) -> Unit
) {
    val symbols = remember {
        listOf("⭐", "⭐", "🍓", "🍓", "👑", "👑", "🌈", "🌈")
    }
    val matchedIndices = remember { mutableStateListOf<Int>() }
    var firstSelected by remember { mutableIntStateOf(-1) }
    var secondSelected by remember { mutableIntStateOf(-1) }
    var pulseTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(firstSelected, secondSelected) {
        if (firstSelected != -1 && secondSelected != -1) {
            if (symbols[firstSelected] == symbols[secondSelected]) {
                matchedIndices.add(firstSelected)
                matchedIndices.add(secondSelected)
                pulseTick += 1
            } else {
                delay(450)
            }
            firstSelected = -1
            secondSelected = -1
        }
    }

    val pairsFound = matchedIndices.size / 2
    val totalPairs = symbols.size / 2
    val isWon = pairsFound >= totalPairs

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pet cheering header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(petSpecies.realm.skyTopHex).copy(alpha = 0.14f))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetCompanionArtworkCanvas(
                species = petSpecies,
                growthStage = growthStage,
                pulseTick = pulseTick,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isWon) "$petName: \"You matched them all!\""
                    else "$petName: \"Find the matching treasure bubbles!\"",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Pairs Matched: $pairsFound / $totalPairs • Reward: +20 ★ & +60 XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2 rows of 4 bubbles
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0..3) {
                    val idx = row * 4 + col
                    val isMatched = matchedIndices.contains(idx)
                    val isRevealed = isMatched || idx == firstSelected || idx == secondSelected

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = when {
                            isMatched -> QuestMint.copy(alpha = 0.25f)
                            isRevealed -> StarGoldBright.copy(alpha = 0.35f)
                            else -> Color(petSpecies.realm.skyTopHex)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .border(
                                width = 2.dp,
                                color = if (isMatched) QuestMint else StarGoldBright,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(enabled = !isMatched && secondSelected == -1) {
                                if (firstSelected == -1) {
                                    firstSelected = idx
                                } else if (firstSelected != idx) {
                                    secondSelected = idx
                                }
                            }
                            .testTag("memory_bubble_$idx")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isRevealed) symbols[idx] else "🫧",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isWon) {
            Button(
                onClick = { onComplete(20L) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestMint,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("claim_minigame_reward_button")
            ) {
                Text(
                    text = "🎉 Collect +20 ★ & Level Up $petName!",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = {
                    // Reveal next unmatched pair with pet helper!
                    val unmatched = (symbols.indices).filterNot { matchedIndices.contains(it) }
                    if (unmatched.isNotEmpty()) {
                        val targetSym = symbols[unmatched.first()]
                        val pair = symbols.indices.filter { symbols[it] == targetSym }
                        matchedIndices.addAll(pair)
                        pulseTick += 1
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestRoyalBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_helper_match_button")
            ) {
                Text(
                    text = "✨ Ask $petName to Sniff Out a Match! ($pairsFound/$totalPairs)",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable
private fun CosmicTrickJumpGame(
    petSpecies: PetSpecies,
    petName: String,
    growthStage: PetGrowthStage,
    onComplete: (Long) -> Unit
) {
    val targetJumps = 5
    var jumpCombo by remember { mutableIntStateOf(0) }
    var trickLabel by remember { mutableStateOf("Tap SUPER JUMP to perform tricks!") }

    val jumpScale by animateFloatAsState(
        targetValue = if (jumpCombo % 2 == 1) 1.16f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "jump_scale"
    )

    val isWon = jumpCombo >= targetJumps

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = trickLabel,
            style = MaterialTheme.typography.titleSmall,
            color = QuestRoyalBlue,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(petSpecies.realm.skyBottomHex),
                            Color(petSpecies.realm.skyTopHex)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Glowing Trick Target Ring
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .scale(jumpScale)
                    .border(4.dp, StarGoldBright, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                PetCompanionArtworkCanvas(
                    species = petSpecies,
                    growthStage = growthStage,
                    pulseTick = jumpCombo,
                    modifier = Modifier.size(118.dp)
                )
            }

            Surface(
                color = Color.Black.copy(alpha = 0.45f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            ) {
                Text(
                    text = "Trick Combo: $jumpCombo / $targetJumps • Reward: +25 ★ & +80 XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = StarGoldBright,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isWon) {
            Button(
                onClick = { onComplete(25L) },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QuestMint,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("claim_minigame_reward_button")
            ) {
                Text(
                    text = "🎉 Collect +25 ★ & Evolve $petName!",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
            Button(
                onClick = {
                    jumpCombo += 1
                    trickLabel = listOf(
                        "🌟 Starlight Backflip! Combo x$jumpCombo!",
                        "🚀 Supersonic Sky Twirl! Combo x$jumpCombo!",
                        "🌈 Rainbow Loop-de-Loop! Combo x$jumpCombo!",
                        "⚡ Cosmic Super Sommersault! Combo x$jumpCombo!",
                        "👑 Royal Champion Finale! Combo x$jumpCombo!"
                    )[(jumpCombo - 1).coerceIn(0, 4)]
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9B5DE5),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cosmic_jump_action_button")
            ) {
                Text(
                    text = "🚀 SUPER TRICK JUMP! ($jumpCombo/$targetJumps)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
