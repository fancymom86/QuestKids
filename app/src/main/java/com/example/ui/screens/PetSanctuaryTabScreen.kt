package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.KidProfile
import com.example.data.model.PetCareAction
import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.PetMiniGameType
import com.example.data.model.PetRealm
import com.example.data.model.PetSpecies
import com.example.ui.components.PetCompanionArtworkCanvas
import com.example.ui.components.PetMiniGameModal
import com.example.ui.components.RealmHabitatSceneCanvas
import com.example.ui.theme.QuestMint
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGoldBright

@Composable
fun PetSanctuaryTabScreen(
    profile: KidProfile,
    petPulseTick: Int,
    onCareForPet: (PetCareAction) -> Unit,
    onCompleteMiniGame: (PetMiniGameType, Long, Long, Long) -> Unit,
    onSelectPet: (petType: String, petName: String) -> Unit,
    onOpenRewardsChest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeSpecies = PetCatalog.getById(profile.petType)
    val effectivePetLevel = maxOf(
        profile.petLevel,
        PetGrowthStage.levelFromStarBalance(profile.totalStars)
    ).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)

    val currentStage = PetGrowthStage.fromLevel(effectivePetLevel)
    val petDisplayName = profile.petName.ifBlank { activeSpecies.defaultName }

    var previewStageOverride by rememberSaveable { mutableStateOf<PetGrowthStage?>(null) }
    val displayedStage = previewStageOverride ?: currentStage

    var selectedRealmFilter by rememberSaveable { mutableStateOf<PetRealm?>(null) }
    var showRenamePetDialog by rememberSaveable { mutableStateOf(false) }
    var activeMiniGame by rememberSaveable { mutableStateOf<PetMiniGameType?>(null) }

    val filteredPets = if (selectedRealmFilter == null) {
        PetCatalog.allPets
    } else {
        PetCatalog.allPets.filter { it.realm == selectedRealmFilter }
    }

    val nextLevelTarget = (effectivePetLevel + 1L).coerceAtMost(PetGrowthStage.MAX_PET_LEVEL)
    val currentLevelStars = PetGrowthStage.starsRequiredForLevel(effectivePetLevel)
    val nextLevelStars = PetGrowthStage.starsRequiredForLevel(nextLevelTarget)

    val levelProgress = if (effectivePetLevel >= PetGrowthStage.MAX_PET_LEVEL) {
        1f
    } else {
        val span = (nextLevelStars - currentLevelStars).coerceAtLeast(1L)
        val earned = (profile.totalStars - currentLevelStars).coerceAtLeast(0L)
        (earned.toFloat() / span.toFloat()).coerceIn(0.15f, 1f)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("pet_sanctuary_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Interactive Realm Habitat & 205-Level Evolution Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_habitat_card"),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        RealmHabitatSceneCanvas(
                            species = activeSpecies,
                            growthStage = displayedStage,
                            pulseTick = petPulseTick,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top-left Realm Badge
                        Surface(
                            color = Color.Black.copy(alpha = 0.42f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "${activeSpecies.realm.emoji} ${activeSpecies.realm.habitatTitle}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Top-right Treats & Stars Counter
                        Surface(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🍓 ${profile.petTreats} Treats",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "★ ${profile.totalStars}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StarGoldBright
                                )
                            }
                        }

                        // Bottom Stage Pill Overlay
                        Surface(
                            color = Color(0xFF182238).copy(alpha = 0.86f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = if (previewStageOverride != null) {
                                    "👀 Previewing ${displayedStage.title} • Tap to reset"
                                } else {
                                    "✨ Lv. $effectivePetLevel / 205 • ${displayedStage.accessoryLabel}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = StarGoldBright,
                                modifier = Modifier
                                    .clickable { previewStageOverride = null }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Pet Info, 205-Level Evolution Bar & Roadmap Previewer
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = petDisplayName,
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .clickable { showRenamePetDialog = true }
                                            .testTag("rename_pet_button")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Rename pet",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${activeSpecies.speciesTitle} • ${currentStage.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Happiness Pill
                            Surface(
                                color = Color(0xFFFFE5EC),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Pet Happiness",
                                        tint = Color(0xFFF72585),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${profile.petHappiness}% Happy",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color(0xFF9D0208)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 205-Level Evolution Progress Box
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Evolution Level: Lv. $effectivePetLevel / 205",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Pet XP: ${profile.petXp} ⚡",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = QuestRoyalBlue
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = { levelProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(50)),
                                    color = QuestMint,
                                    trackColor = Color.White
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (effectivePetLevel >= PetGrowthStage.MAX_PET_LEVEL) {
                                        "👑 OMEGA LEVEL 205 OMNIVERSE LEGEND REACHED!"
                                    } else {
                                        "Play Pet Mini-Games, care for $petDisplayName, or complete quests to evolve through all 205 levels!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "205-Level Evolution Tiers (Tap to Preview Look):",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(PetGrowthStage.entries) { stage ->
                                val isUnlocked = effectivePetLevel >= stage.level
                                val isPreviewed = displayedStage == stage
                                FilterChip(
                                    selected = isPreviewed,
                                    onClick = {
                                        previewStageOverride = if (previewStageOverride == stage) null else stage
                                    },
                                    label = {
                                        Text(
                                            text = if (isUnlocked) "✓ Lv.${stage.level}" else "🔒 Lv.${stage.level}"
                                        )
                                    },
                                    shape = RoundedCornerShape(50),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = QuestRoyalBlue,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("evolution_tier_chip_${stage.tierIndex}")
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. PET MINI-GAMES ARCADE (Play with your pet to evolve & earn Gift Stars!)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pet_minigames_arcade_card"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = QuestRoyalBlue.copy(alpha = 0.14f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SportsEsports,
                                        contentDescription = "Pet Mini-Games",
                                        tint = QuestRoyalBlue,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Pet Mini-Game Arcade",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "Play with $petDisplayName to level up (+1 Lv per win) & earn Gift Stars!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PetMiniGameType.entries.forEach { game ->
                            val accent = Color(game.accentHex)
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = accent.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { activeMiniGame = game }
                                    .testTag("minigame_card_${game.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = game.emoji,
                                                    style = MaterialTheme.typography.headlineMedium
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = game.title,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = game.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "🏆 Win: +${game.maxStarReward} ★ • +${game.xpReward} XP • +1 Pet Level",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = QuestRoyalBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = { activeMiniGame = game },
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = QuestRoyalBlue,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("play_minigame_${game.id}")
                                    ) {
                                        Text("Play!")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Care & Play Actions (Earn Bonus Stars for Parent Gifts!)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Feed & Care for $petDisplayName",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Spend Pet Treats from quests & mini-games for extra Gift Stars!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PetCareAction.entries.forEach { action ->
                            val canPerform = profile.petTreats >= action.treatCost
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (canPerform) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.5.dp,
                                        color = if (canPerform) QuestRoyalBlue.copy(alpha = 0.35f) else Color.Transparent,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = action.emoji,
                                                    style = MaterialTheme.typography.headlineSmall
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = action.label,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = action.subtitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Button(
                                        onClick = { onCareForPet(action) },
                                        enabled = canPerform,
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (action.treatCost == 0L) QuestMint else QuestRoyalBlue
                                        ),
                                        modifier = Modifier.testTag("pet_care_${action.id}")
                                    ) {
                                        Text(
                                            text = if (action.treatCost == 0L) {
                                                "Free (+${action.bonusStars}★)"
                                            } else {
                                                "${action.treatCost} 🍓 (+${action.bonusStars}★)"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Redeem Parent Gifts Call-To-Action Banner
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF4A154B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable(onClick = onOpenRewardsChest)
                            .testTag("pet_to_rewards_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Redeem,
                                    contentDescription = null,
                                    tint = StarGoldBright,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Redeem Stars for Parent Gifts!",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "You have ${profile.totalStars} ★ ready to spend in the Parent Gift Chest",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.88f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StarGoldBright
                            )
                        }
                    }
                }
            }
        }

        // 4. Multi-Realm Pet Sanctuary Selector (Sea, Land, Air, Enchantment, Space)
        item {
            Column {
                Text(
                    text = "Explore Pet Realms",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Choose a companion from Sea, Land, Air, Enchantment, or Deep Space!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedRealmFilter == null,
                            onClick = { selectedRealmFilter = null },
                            label = { Text("🌍 All Realms (${PetCatalog.allPets.size})") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QuestRoyalBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("realm_filter_all")
                        )
                    }
                    items(PetRealm.entries) { realm ->
                        FilterChip(
                            selected = selectedRealmFilter == realm,
                            onClick = {
                                selectedRealmFilter = if (selectedRealmFilter == realm) null else realm
                            },
                            label = { Text("${realm.emoji} ${realm.label}") },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(realm.skyTopHex),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("realm_filter_${realm.key}")
                        )
                    }
                }
            }
        }

        // 5. Pet Species Cards
        items(filteredPets, key = { it.id }) { species ->
            val isSelected = species.id == activeSpecies.id
            PetSpeciesSelectorCard(
                species = species,
                growthStage = displayedStage,
                isSelected = isSelected,
                onSelect = {
                    onSelectPet(species.id, species.defaultName)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(76.dp))
        }
    }

    if (showRenamePetDialog) {
        RenamePetDialog(
            currentName = petDisplayName,
            species = activeSpecies,
            growthStage = displayedStage,
            onDismiss = { showRenamePetDialog = false },
            onConfirm = { newName ->
                onSelectPet(activeSpecies.id, newName)
            }
        )
    }

    activeMiniGame?.let { game ->
        PetMiniGameModal(
            miniGame = game,
            petSpecies = activeSpecies,
            petName = petDisplayName,
            growthStage = displayedStage,
            onDismiss = { activeMiniGame = null },
            onVictory = { stars, xp, treats ->
                onCompleteMiniGame(game, stars, xp, treats)
            }
        )
    }
}

@Composable
private fun PetSpeciesSelectorCard(
    species: PetSpecies,
    growthStage: PetGrowthStage,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) QuestMint else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onSelect)
            .testTag("pet_species_card_${species.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 5.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(species.realm.skyTopHex),
                                Color(species.realm.skyBottomHex)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                PetCompanionArtworkCanvas(
                    species = species,
                    growthStage = growthStage,
                    animate = isSelected,
                    modifier = Modifier.size(66.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(species.realm.skyTopHex).copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${species.realm.emoji} ${species.realm.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(species.realm.skyTopHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    if (isSelected) {
                        Surface(
                            color = QuestMint.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Active Buddy",
                                style = MaterialTheme.typography.labelSmall,
                                color = QuestMint,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${species.defaultName} • ${species.speciesTitle}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = species.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected Pet",
                    tint = QuestMint,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                OutlinedButton(
                    onClick = onSelect,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("adopt_pet_${species.id}")
                ) {
                    Text("Choose")
                }
            }
        }
    }
}

@Composable
private fun RenamePetDialog(
    currentName: String,
    species: PetSpecies,
    growthStage: PetGrowthStage,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var petName by remember { mutableStateOf(currentName) }

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
                    text = "Name Your Pet Companion",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(12.dp))

                PetCompanionArtworkCanvas(
                    species = species,
                    growthStage = growthStage,
                    modifier = Modifier.size(92.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = petName,
                    onValueChange = { petName = it },
                    label = { Text("Pet Nickname") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pet_name_input")
                )

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
                            onConfirm(petName.ifBlank { species.defaultName })
                            onDismiss()
                        },
                        modifier = Modifier.testTag("save_pet_name_button")
                    ) {
                        Text("Save Name")
                    }
                }
            }
        }
    }
}
