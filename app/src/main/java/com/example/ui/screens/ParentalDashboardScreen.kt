package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.GiftRedemptionRequest
import com.example.data.model.KidProfile
import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.RedemptionStatus
import com.example.data.model.RoutineType
import com.example.data.model.VisualReward
import com.example.ui.components.GiftBoxIllustrationCanvas
import com.example.ui.components.TaskIconBadge
import com.example.ui.theme.QuestMint
import com.example.ui.theme.QuestPurple
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarGoldBright
import com.example.ui.theme.StreakFlameCoral
import com.example.ui.theme.StreakFlameOrange

private enum class RedemptionFilter(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    APPROVED("Approved"),
    REJECTED("Rejected")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParentalDashboardScreen(
    profile: KidProfile,
    redemptions: List<GiftRedemptionRequest>,
    rewards: List<VisualReward>,
    onApproveRedemption: (GiftRedemptionRequest, String) -> Unit,
    onRejectRedemption: (GiftRedemptionRequest, String) -> Unit,
    onOpenNewRewardDialog: () -> Unit,
    onOpenNewQuestDialog: () -> Unit,
    onSimulateNextStreakDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingRequests = remember(redemptions) {
        redemptions.filter { RedemptionStatus.fromKey(it.status) == RedemptionStatus.PENDING }
    }
    val historyRequests = remember(redemptions) {
        redemptions.filter { RedemptionStatus.fromKey(it.status) != RedemptionStatus.PENDING }
    }
    val reservedStars = remember(pendingRequests) {
        pendingRequests.sumOf { it.starCost }
    }
    val approvedCount = remember(redemptions) {
        redemptions.count { RedemptionStatus.fromKey(it.status) == RedemptionStatus.APPROVED }
    }
    val rejectedCount = remember(redemptions) {
        redemptions.count { RedemptionStatus.fromKey(it.status) == RedemptionStatus.REJECTED }
    }

    val noteDrafts = remember { mutableStateMapOf<String, String>() }
    var selectedHistoryFilter by remember { mutableStateOf(RedemptionFilter.ALL) }

    val filteredHistory = remember(redemptions, selectedHistoryFilter) {
        when (selectedHistoryFilter) {
            RedemptionFilter.ALL -> historyRequests
            RedemptionFilter.PENDING -> pendingRequests
            RedemptionFilter.APPROVED -> redemptions.filter {
                RedemptionStatus.fromKey(it.status) == RedemptionStatus.APPROVED
            }
            RedemptionFilter.REJECTED -> redemptions.filter {
                RedemptionStatus.fromKey(it.status) == RedemptionStatus.REJECTED
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("parental_dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Parental Control & Star Balance Banner
        item {
            ParentalOverviewHeroCard(
                profile = profile,
                pendingCount = pendingRequests.size,
                reservedStars = reservedStars,
                approvedCount = approvedCount,
                rejectedCount = rejectedCount
            )
        }

        // 2. Quick Parent Controls Bar
        item {
            ParentQuickActionsRow(
                onOpenNewRewardDialog = onOpenNewRewardDialog,
                onOpenNewQuestDialog = onOpenNewQuestDialog,
                onSimulateNextStreakDay = onSimulateNextStreakDay
            )
        }

        // 3. Pending Approvals Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = null,
                        tint = StreakFlameOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Pending Gift Approvals (${pendingRequests.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Approve to grant the gift or reject to refund stars back to ${profile.kidName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (reservedStars > 0) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StarGold.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, StarGold.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "★ $reservedStars Reserved",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // 4. Pending Request Cards or All-Caught-Up State
        if (pendingRequests.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_pending_redemptions_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = QuestMint.copy(alpha = 0.09f)
                    ),
                    border = BorderStroke(1.dp, QuestMint.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = QuestMint.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = QuestMint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "All Caught Up!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = QuestMint
                            )
                            Text(
                                text = "No gift requests are waiting for approval right now. When ${profile.kidName} redeems stars in the Gift Chest, requests will appear here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            items(pendingRequests, key = { it.id }) { request ->
                val noteText = noteDrafts[request.id] ?: ""
                PendingRedemptionDecisionCard(
                    request = request,
                    noteText = noteText,
                    onNoteChange = { noteDrafts[request.id] = it },
                    onApprove = {
                        val finalNote = noteText.trim().ifBlank { "Approved by Parent! Enjoy your reward!" }
                        onApproveRedemption(request, finalNote)
                        noteDrafts.remove(request.id)
                    },
                    onReject = {
                        val finalNote = noteText.trim().ifBlank { "Stars refunded — let's save for another time!" }
                        onRejectRedemption(request, finalNote)
                        noteDrafts.remove(request.id)
                    }
                )
            }
        }

        // 5. Redemption History & Filter Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = QuestPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Redemption Activity Log (${redemptions.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RedemptionFilter.entries.forEach { filter ->
                        val selected = selectedHistoryFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { selectedHistoryFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QuestPurple.copy(alpha = 0.16f),
                                selectedLabelColor = QuestPurple
                            ),
                            modifier = Modifier.testTag("redemption_filter_${filter.name.lowercase()}")
                        )
                    }
                }
            }
        }

        if (filteredHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Text(
                        text = "No redemption records match this filter yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(filteredHistory, key = { "history_${it.id}" }) { record ->
                RedemptionHistoryRowCard(record = record)
            }
        }

        // 6. Active Gift Catalog Overview for Parents
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CardGiftcard,
                                contentDescription = null,
                                tint = QuestPurple
                            )
                            Column {
                                Text(
                                    text = "Parent Gift Chest Catalog (${rewards.size} Prizes)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Prizes your child can request with earned stars",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onOpenNewRewardDialog,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("parent_catalog_add_gift_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Add Gift",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    rewards.take(6).forEachIndexed { index, reward ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reward.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Approved ${reward.timesClaimed}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "★ ${reward.starCost}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun ParentalOverviewHeroCard(
    profile: KidProfile,
    pendingCount: Int,
    reservedStars: Long,
    approvedCount: Int,
    rejectedCount: Int
) {
    val activePet = remember(profile.petType) { PetCatalog.getById(profile.petType) }
    val exactLevel = remember(profile.totalStars, profile.petLevel) {
        maxOf(
            PetGrowthStage.levelFromStarBalance(profile.totalStars),
            profile.petLevel
        ).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("parental_dashboard_hero_card"),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF1E1B4B),
                            QuestRoyalBlue,
                            QuestPurple
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.16f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AdminPanelSettings,
                                    contentDescription = "Parental Dashboard",
                                    tint = StarGoldBright,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "PARENTAL DASHBOARD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = StarGoldBright
                            )
                            Text(
                                text = "${profile.kidName}'s Star Bank & Gifts",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.16f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = StreakFlameOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${profile.currentStreak}d Streak",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Star Balance & Redemption Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ParentStatTile(
                        label = "Available Stars",
                        value = "★ ${profile.totalStars}",
                        subtext = "Ready to spend",
                        accentColor = StarGoldBright,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("parent_available_stars_tile")
                    )
                    ParentStatTile(
                        label = "Pending Requests",
                        value = "$pendingCount",
                        subtext = "★ $reservedStars held",
                        accentColor = StreakFlameOrange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("parent_pending_count_tile")
                    )
                    ParentStatTile(
                        label = "Approved Gifts",
                        value = "$approvedCount",
                        subtext = "$rejectedCount refunded",
                        accentColor = QuestMint,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("parent_approved_count_tile")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Companion: ${profile.petName.ifBlank { activePet.defaultName }} (${activePet.speciesTitle})",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lv. $exactLevel / ${PetGrowthStage.MAX_PET_LEVEL}",
                            style = MaterialTheme.typography.labelMedium,
                            color = StarGoldBright,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentStatTile(
    label: String,
    value: String,
    subtext: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ParentQuickActionsRow(
    onOpenNewRewardDialog: () -> Unit,
    onOpenNewQuestDialog: () -> Unit,
    onSimulateNextStreakDay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Parent Controls & Quick Actions",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenNewRewardDialog,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("parent_quick_add_gift_button")
                ) {
                    Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Gift", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenNewQuestDialog,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("parent_quick_add_quest_button")
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Quest", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onSimulateNextStreakDay,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("parent_bonus_streak_button")
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+10★ Bonus", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PendingRedemptionDecisionCard(
    request: GiftRedemptionRequest,
    noteText: String,
    onNoteChange: (String) -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val presetNotes = listOf(
        "Awesome job earning this!",
        "Approved for tonight!",
        "Let's save stars for the weekend!"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("pending_redemption_card_${request.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(2.dp, StreakFlameOrange.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(58.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TaskIconBadge(
                        iconName = request.iconName,
                        completed = true,
                        routine = RoutineType.WEEKEND
                    )
                    GiftBoxIllustrationCanvas(
                        accentColor = Color(0xFFF72585),
                        unlocked = true,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = StreakFlameOrange.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "PENDING PARENT APPROVAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = StreakFlameOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = request.rewardTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Requested by ${request.kidName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StarGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, StarGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${request.starCost}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick parent note chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presetNotes.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onNoteChange(preset) }
                    ) {
                        Text(
                            text = preset,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = onNoteChange,
                label = { Text("Optional Parent Note for ${request.kidName}") },
                placeholder = { Text("e.g., Great job finishing your quests!") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("parent_note_input_${request.id}")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, StreakFlameCoral),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = StreakFlameCoral
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("reject_redemption_${request.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = "Reject and Refund Stars",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reject (+${request.starCost}★)",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = onApprove,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QuestMint,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("approve_redemption_${request.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Approve Gift Redemption",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Approve Gift",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun RedemptionHistoryRowCard(record: GiftRedemptionRequest) {
    val statusEnum = RedemptionStatus.fromKey(record.status)
    val (statusLabel, statusColor, statusIcon) = when (statusEnum) {
        RedemptionStatus.APPROVED -> Triple("APPROVED", QuestMint, Icons.Filled.CheckCircle)
        RedemptionStatus.REJECTED -> Triple("REFUNDED (+${record.starCost}★)", StreakFlameCoral, Icons.Filled.Close)
        RedemptionStatus.PENDING -> Triple("PENDING", StreakFlameOrange, Icons.Filled.HourglassTop)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("redemption_history_card_${record.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.14f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = record.rewardTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                if (record.parentNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Note: \"${record.parentNote}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "★ ${record.starCost}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFB45309)
            )
        }
    }
}
