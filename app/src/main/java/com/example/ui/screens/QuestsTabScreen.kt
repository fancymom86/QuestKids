package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.Brush
import com.example.data.model.KidProfile
import com.example.data.model.PetCareAction
import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.QuestTask
import com.example.data.model.RoutineType
import com.example.ui.components.HeroQuestIslandBanner
import com.example.ui.components.PetCompanionArtworkCanvas
import com.example.ui.components.StarBuddyMascotCanvas
import com.example.ui.components.TaskIconBadge
import com.example.ui.theme.QuestMint
import com.example.ui.theme.QuestMintSoft
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarGoldBright

@Composable
fun QuestsTabScreen(
    profile: KidProfile,
    tasks: List<QuestTask>,
    todayDate: String,
    selectedRoutine: RoutineType?,
    petPulseTick: Int = 0,
    onSelectRoutine: (RoutineType?) -> Unit,
    onToggleTask: (QuestTask) -> Unit,
    onDeleteTask: (String) -> Unit,
    onOpenAddTask: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenPetSanctuary: () -> Unit = {},
    onQuickCarePet: (PetCareAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val completedCount = tasks.count { it.isCompletedOn(todayDate) }
    val filteredTasks = if (selectedRoutine == null) {
        tasks
    } else {
        tasks.filter { it.routine == selectedRoutine.key }
    }

    val activePet = PetCatalog.getById(profile.petType)
    val effectivePetLevel = maxOf(
        profile.petLevel,
        PetGrowthStage.levelFromStarBalance(profile.totalStars)
    ).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)
    val currentStage = PetGrowthStage.fromLevel(effectivePetLevel)
    val petName = profile.petName.ifBlank { activePet.defaultName }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("quests_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            HeroQuestIslandBanner(
                profile = profile,
                completedTodayCount = completedCount,
                totalTasksCount = tasks.size,
                selectedRoutine = selectedRoutine,
                onEditProfileClick = onEditProfile
            )
        }

        // Interactive Pet Companion Growth Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(onClick = onOpenPetSanctuary)
                    .testTag("quests_pet_companion_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(activePet.realm.skyTopHex),
                                    Color(activePet.realm.skyBottomHex)
                                )
                            )
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, StarGoldBright, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        PetCompanionArtworkCanvas(
                            species = activePet,
                            growthStage = currentStage,
                            pulseTick = petPulseTick,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$petName (Lv. $effectivePetLevel / 205)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "${currentStage.title} • 🍓 ${profile.petTreats} Treats",
                            style = MaterialTheme.typography.labelMedium,
                            color = StarGoldBright
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val progress = (effectivePetLevel.toFloat() / PetGrowthStage.MAX_PET_LEVEL.toFloat())
                            .coerceIn(0.08f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(7.dp)
                                .clip(RoundedCornerShape(50)),
                            color = StarGoldBright,
                            trackColor = Color.White.copy(alpha = 0.28f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎮 Tap to play Pet Mini-Games & evolve through 205 levels!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = StarGoldBright,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val action = if (profile.petTreats >= 1L) {
                                    PetCareAction.FEED
                                } else {
                                    PetCareAction.CUDDLE
                                }
                                onQuickCarePet(action)
                            }
                            .testTag("quick_feed_pet_button")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (profile.petTreats >= 1L) "🍓 Feed" else "💖 Pet",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color(0xFF182238)
                            )
                            Text(
                                text = if (profile.petTreats >= 1L) "+5 ★" else "+2 ★",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF182238)
                            )
                        }
                    }
                }
            }
        }

        // Routine Filter Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Routines",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    TextButton(
                        onClick = onOpenAddTask,
                        modifier = Modifier.testTag("header_add_task_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new quest",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Quest")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedRoutine == null,
                            onClick = { onSelectRoutine(null) },
                            label = {
                                Text(
                                    text = "✨ All (${tasks.size})",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QuestRoyalBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_routine_all")
                        )
                    }
                    items(RoutineType.entries) { routine ->
                        val countForRoutine = tasks.count { it.routine == routine.key }
                        val doneForRoutine = tasks.count {
                            it.routine == routine.key && it.isCompletedOn(todayDate)
                        }
                        FilterChip(
                            selected = selectedRoutine == routine,
                            onClick = {
                                onSelectRoutine(if (selectedRoutine == routine) null else routine)
                            },
                            label = {
                                Text(
                                    text = "${routine.emoji} ${routine.label} ($doneForRoutine/$countForRoutine)",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QuestRoyalBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_routine_${routine.key}")
                        )
                    }
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        StarBuddyMascotCanvas(
                            avatarId = profile.avatarId,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Quests in This Routine Yet!",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to add a fun daily task to complete for stars!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val isCompleted = task.isCompletedOn(todayDate)
                QuestTaskCard(
                    task = task,
                    isCompleted = isCompleted,
                    onToggle = { onToggleTask(task) },
                    onDelete = { onDeleteTask(task.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(76.dp))
        }
    }
}

@Composable
private fun QuestTaskCard(
    task: QuestTask,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val routine = RoutineType.fromKey(task.routine)
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 0.99f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isCompleted) {
            QuestMintSoft.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "card_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isCompleted) {
            QuestMint
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        label = "card_border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onToggle)
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 1.dp else 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskIconBadge(
                iconName = task.iconName,
                completed = isCompleted,
                routine = routine
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Routine pill
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${routine.emoji} ${routine.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Star reward pill
                    Surface(
                        color = StarGold.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+${task.starReward} Stars",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete quest",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Big tactile check-off circle button (48dp+ touch target)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) QuestMint else StarGoldBright.copy(alpha = 0.28f)
                    )
                    .border(
                        width = 2.5.dp,
                        color = if (isCompleted) QuestMint else StarGold,
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggle)
                    .testTag("toggle_task_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Tap to complete",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
