package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GiftRedemptionRequest
import com.example.data.model.KidProfile
import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.RedemptionStatus
import com.example.data.model.RoutineType
import com.example.data.model.VisualReward
import com.example.ui.components.GiftBoxIllustrationCanvas
import com.example.ui.components.PetCompanionArtworkCanvas
import com.example.ui.components.TaskIconBadge
import com.example.ui.theme.QuestMint
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarGoldBright
import com.example.ui.theme.StreakFlameOrange

@Composable
fun RewardsChestTabScreen(
    profile: KidProfile,
    rewards: List<VisualReward>,
    redemptions: List<GiftRedemptionRequest> = emptyList(),
    onClaimReward: (VisualReward) -> Unit,
    onDeleteReward: (String) -> Unit,
    onOpenAddReward: () -> Unit,
    onOpenParentalDashboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val activePet = PetCatalog.getById(profile.petType)
    val currentStage = maxOf(
        PetGrowthStage.fromStarBalance(profile.totalStars),
        PetGrowthStage.fromLevel(profile.petLevel),
        compareBy { it.level }
    )
    val petName = profile.petName.ifBlank { activePet.defaultName }
    val pendingRedemptions = remember(redemptions) {
        redemptions.filter { RedemptionStatus.fromKey(it.status) == RedemptionStatus.PENDING }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rewards_chest_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Treasure Chest & Parent Gift Shop Header with Pet Companion
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF4A154B),
                                    Color(0xFF7209B7),
                                    Color(0xFFF72585)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        PetCompanionArtworkCanvas(
                                            species = activePet,
                                            growthStage = currentStage,
                                            modifier = Modifier.size(56.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Parent Gift Chest",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Raise $petName & finish quests to request real gifts from your parents!",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.92f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = Color.Black.copy(alpha = 0.24f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Redeemable Gift Star Points",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = StarGoldBright,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${profile.totalStars} Stars",
                                            style = MaterialTheme.typography.displaySmall,
                                            color = StarGoldBright
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        text = "🎁 ${profile.claimedRewards.size} Approved",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1b. Pending Parental Approval Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_parental_dashboard_banner"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (pendingRedemptions.isNotEmpty()) {
                        StreakFlameOrange.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (pendingRedemptions.isNotEmpty()) {
                        StreakFlameOrange.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = if (pendingRedemptions.isNotEmpty()) {
                                StreakFlameOrange.copy(alpha = 0.2f)
                            } else {
                                QuestMint.copy(alpha = 0.16f)
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (pendingRedemptions.isNotEmpty()) {
                                        Icons.Filled.HourglassTop
                                    } else {
                                        Icons.Filled.AdminPanelSettings
                                    },
                                    contentDescription = null,
                                    tint = if (pendingRedemptions.isNotEmpty()) StreakFlameOrange else QuestMint
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (pendingRedemptions.isNotEmpty()) {
                                    "${pendingRedemptions.size} Gift Request(s) Waiting for Parent"
                                } else {
                                    "Parental Approval Dashboard"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (pendingRedemptions.isNotEmpty()) {
                                    "Parents can approve your gift or refund your stars!"
                                } else {
                                    "Parents can review, approve, or reject redeemed gifts."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onOpenParentalDashboard,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("rewards_open_parent_dashboard_button")
                    ) {
                        Text(
                            text = "Parent Hub",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 2. Section header with "+ New Gift" button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Parent Gift Coupons",
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(
                    onClick = onOpenAddReward,
                    modifier = Modifier.testTag("header_add_reward_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new reward",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Gift")
                }
            }
        }

        // 3. Visual Reward Cards
        items(rewards, key = { it.id }) { reward ->
            val canAfford = profile.totalStars >= reward.starCost
            val pendingForThisReward = pendingRedemptions.count { it.rewardId == reward.id }
            val progress = if (reward.starCost > 0) {
                (profile.totalStars.toFloat() / reward.starCost.toFloat()).coerceIn(0f, 1f)
            } else 1f

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (canAfford) 2.dp else 1.dp,
                        color = if (canAfford) StarGold else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("reward_card_${reward.id}"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (canAfford) 4.dp else 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(58.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            TaskIconBadge(
                                iconName = reward.iconName,
                                completed = canAfford,
                                routine = RoutineType.WEEKEND
                            )
                            GiftBoxIllustrationCanvas(
                                accentColor = Color(0xFFF72585),
                                unlocked = canAfford,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reward.title,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = StarGold.copy(alpha = 0.22f),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "★ ${reward.starCost} Stars",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                if (pendingForThisReward > 0) {
                                    Surface(
                                        color = StreakFlameOrange.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = "⏳ $pendingForThisReward Pending",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = StreakFlameOrange,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else if (reward.timesClaimed > 0) {
                                    Text(
                                        text = "Approved ${reward.timesClaimed}x",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = QuestMint
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { onDeleteReward(reward.id) },
                            modifier = Modifier.testTag("delete_reward_${reward.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete reward",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = if (canAfford) QuestMint else StarGold,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (canAfford) {
                                    "Ready to request from parents!"
                                } else {
                                    "${reward.starCost - profile.totalStars} more stars needed"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (canAfford) QuestMint else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Button(
                            onClick = { onClaimReward(reward) },
                            enabled = canAfford,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canAfford) QuestMint else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("claim_reward_${reward.id}")
                        ) {
                            Text(
                                text = if (canAfford) "Request Gift!" else "Locked",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
