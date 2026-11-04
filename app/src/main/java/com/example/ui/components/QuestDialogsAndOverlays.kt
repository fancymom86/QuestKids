package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AvatarCatalog
import com.example.data.model.KidProfile
import com.example.data.model.RoutineType
import com.example.ui.theme.QuestMint
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarGoldBright
import com.example.ui.viewmodel.CelebrationBanner
import com.example.ui.viewmodel.PetEvolutionMilestoneEvent

@Composable
fun CelebrationFanfareCard(
    celebration: CelebrationBanner?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = celebration != null,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        celebration?.let { banner ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("celebration_banner"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (banner.unlockedBadge != null) {
                                    listOf(Color(0xFF4A154B), Color(0xFF9B5DE5), Color(0xFFFFB800))
                                } else {
                                    listOf(Color(0xFF007F5F), QuestMint, Color(0xFFFFB800))
                                }
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (banner.unlockedBadge != null) {
                            BadgeMedallionCanvas(
                                badge = banner.unlockedBadge,
                                unlocked = true,
                                modifier = Modifier.size(56.dp)
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.24f),
                                modifier = Modifier.size(50.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Stars Earned",
                                        tint = StarGoldBright,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = banner.title,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = banner.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                            if (banner.starsEarned > 0L) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "+${banner.starsEarned} Golden Stars Added! (Balance: ${banner.newTotalStars} ★)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StarGoldBright
                                )
                            } else if (banner.newTotalStars > 0L) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Remaining Star Balance: ${banner.newTotalStars} ★",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StarGoldBright
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("dismiss_celebration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss celebration",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, routine: RoutineType, iconName: String, starReward: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedRoutine by remember { mutableStateOf(RoutineType.MORNING) }
    var selectedIcon by remember { mutableStateOf("toothbrush") }
    var selectedStars by remember { mutableLongStateOf(5L) }

    val iconOptions = listOf(
        "toothbrush" to "Clean",
        "bed" to "Bed",
        "apple" to "Meal",
        "backpack" to "School",
        "book" to "Read",
        "toys" to "Tidy",
        "pajamas" to "PJs",
        "pet" to "Pets",
        "trophy" to "Sport"
    )
    val starOptions = listOf(5L, 10L, 15L, 20L, 25L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "New Daily Quest",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What's the task?") },
                    placeholder = { Text("e.g., Practice Piano for 10 mins") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Routine Time",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RoutineType.entries.forEach { routine ->
                        FilterChip(
                            selected = selectedRoutine == routine,
                            onClick = { selectedRoutine = routine },
                            label = { Text("${routine.emoji} ${routine.label}") },
                            modifier = Modifier.testTag("routine_option_${routine.key}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pick an Icon",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { (iconKey, label) ->
                        FilterChip(
                            selected = selectedIcon == iconKey,
                            onClick = { selectedIcon = iconKey },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Star Reward Value",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    starOptions.forEach { stars ->
                        FilterChip(
                            selected = selectedStars == stars,
                            onClick = { selectedStars = stars },
                            label = { Text("+$stars ★") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddTask(title, selectedRoutine, selectedIcon, selectedStars)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text("Create Quest")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRewardDialog(
    onDismiss: () -> Unit,
    onAddReward: (title: String, iconName: String, starCost: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("ice_cream") }
    var selectedCost by remember { mutableLongStateOf(25L) }

    val iconOptions = listOf(
        "ice_cream" to "Treat",
        "movie" to "Movie",
        "toys" to "Playtime",
        "book" to "Story",
        "trophy" to "Outing",
        "gift" to "Surprise"
    )
    val costOptions = listOf(15L, 25L, 40L, 60L, 100L)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Text(
                    text = "New Visual Reward Prize",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reward Prize Title") },
                    placeholder = { Text("e.g., Trip to the Science Museum") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_reward_title_input")
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Prize Category",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    iconOptions.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedIcon == key,
                            onClick = { selectedIcon = key },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Star Cost",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    costOptions.forEach { cost ->
                        FilterChip(
                            selected = selectedCost == cost,
                            onClick = { selectedCost = cost },
                            label = { Text("$cost ★") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAddReward(title, selectedIcon, selectedCost)
                                onDismiss()
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.testTag("save_reward_button")
                    ) {
                        Text("Add to Chest")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvatarCustomizerDialog(
    profile: KidProfile,
    onDismiss: () -> Unit,
    onSave: (kidName: String, avatarId: String) -> Unit
) {
    var kidName by remember { mutableStateOf(profile.kidName) }
    var selectedAvatarId by remember { mutableStateOf(profile.avatarId) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Customize Your Explorer",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(12.dp))

                StarBuddyMascotCanvas(
                    avatarId = selectedAvatarId,
                    modifier = Modifier.size(84.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = kidName,
                    onValueChange = { kidName = it },
                    label = { Text("Adventurer Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("kid_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Choose Your Quest Buddy",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(10.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AvatarCatalog.avatars.forEach { option ->
                        val isSelected = selectedAvatarId == option.id
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) QuestRoyalBlue else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedAvatarId = option.id }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("avatar_option_${option.id}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StarBuddyMascotCanvas(
                                    avatarId = option.id,
                                    modifier = Modifier.size(34.dp),
                                    animate = false
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = option.name,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = option.title,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            onSave(kidName, selectedAvatarId)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = QuestRoyalBlue),
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text("Save Explorer")
                    }
                }
            }
        }
    }
}

@Composable
fun PetEvolutionCelebrationDialog(
    event: PetEvolutionMilestoneEvent?,
    onDismiss: () -> Unit
) {
    if (event == null) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 12.dp,
            modifier = Modifier.testTag("pet_evolution_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(event.petSpecies.realm.skyTopHex),
                                Color(0xFF182238)
                            )
                        )
                    )
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = StarGoldBright.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "✨ PET EVOLUTION MILESTONE! ✨",
                        style = MaterialTheme.typography.labelLarge,
                        color = StarGoldBright,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(3.dp, StarGoldBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    PetCompanionArtworkCanvas(
                        species = event.petSpecies,
                        growthStage = event.newStage,
                        pulseTick = 1,
                        modifier = Modifier.size(126.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "${event.petName} Reached Lv. ${event.newLevel}!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Lv. ${event.previousLevel} ➔ Lv. ${event.newLevel} / 205 • ${event.newStage.title}",
                    style = MaterialTheme.typography.titleSmall,
                    color = StarGoldBright
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unlocked Visual Upgrade: ${event.newStage.accessoryLabel}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        if (event.bonusStarsAwarded > 0L) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "+${event.bonusStarsAwarded} Bonus Gift Stars Added! (Total: ${event.totalStars} ★)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StarGoldBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StarGoldBright,
                        contentColor = Color(0xFF182238)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_pet_evolution_button")
                ) {
                    Text(
                        text = "Yay! Keep Growing!",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

