package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.AchievementBadge
import com.example.data.model.BadgeCatalog
import com.example.data.model.KidProfile
import com.example.data.model.QuestDateUtils
import com.example.data.model.QuestTask
import com.example.ui.components.BadgeMedallionCanvas
import com.example.ui.theme.QuestMint
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarGoldBright
import com.example.ui.theme.StreakFlameCoral
import com.example.ui.theme.StreakFlameOrange

@Composable
fun StreaksAndBadgesTabScreen(
    profile: KidProfile,
    tasks: List<QuestTask>,
    todayDate: String,
    onAwardBonusStreakDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = QuestDateUtils.lastSevenDays(todayDate)
    val completedSet = profile.completedDates.toSet()
    val morningPerfectSet = profile.morningPerfectDates.toSet()
    val unlockedCount = BadgeCatalog.allBadges.count { profile.unlockedBadges.contains(it.id) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("streaks_and_badges_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Streak Flame Card
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
                                    Color(0xFF9A031E),
                                    StreakFlameCoral,
                                    StreakFlameOrange
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak Flame",
                                            tint = StarGoldBright,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "${profile.currentStreak}-Day Streak!",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Best Record: ${profile.longestStreak} Consecutive Days",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 7-Day Visual Streak Calendar Strip
                        Surface(
                            color = Color.Black.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Last 7 Days Check-In Trail",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StarGoldBright
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    weekDays.forEach { (isoDate, dayLabel) ->
                                        val isDone = completedSet.contains(isoDate)
                                        val isMorningPerfect = morningPerfectSet.contains(isoDate)
                                        val isToday = isoDate == todayDate

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = dayLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isToday) StarGoldBright else Color.White.copy(alpha = 0.85f)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        when {
                                                            isDone -> StarGoldBright
                                                            isToday -> Color.White.copy(alpha = 0.25f)
                                                            else -> Color.White.copy(alpha = 0.12f)
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday) 2.dp else 0.dp,
                                                        color = if (isToday) Color.White else Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isDone) {
                                                    Icon(
                                                        imageVector = if (isMorningPerfect) {
                                                            Icons.Default.WbSunny
                                                        } else {
                                                            Icons.Default.Check
                                                        },
                                                        contentDescription = "Completed",
                                                        tint = Color(0xFF182238),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = isoDate.takeLast(2),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Routine Streak Trackers (Morning Routine Master + Weekend Warrior)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "☀️ Morning Master",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = StarGoldBright
                                    )
                                    Text(
                                        text = "${profile.morningPerfectDates.size} / 7 Perfect Mornings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🏆 Weekend Warrior",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = StarGoldBright
                                    )
                                    Text(
                                        text = if (profile.weekendPerfectDates.isNotEmpty()) {
                                            "Unlocked (${profile.weekendPerfectDates.size}x)!"
                                        } else {
                                            "Finish Weekend Quests"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Parent Bonus Streak Day Button (lets parents credit a past day or test 5-Day Streak & Morning Routine Master)
                        Button(
                            onClick = onAwardBonusStreakDay,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF9A031E)
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bonus_streak_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Parent Bonus: Credit +1 Past Streak Day",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // 2. Achievement Badges Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Achievement Badges",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Complete streaks and routines to earn shiny medals!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = QuestRoyalBlue.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = QuestRoyalBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$unlockedCount / ${BadgeCatalog.allBadges.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = QuestRoyalBlue
                        )
                    }
                }
            }
        }

        // 3. Achievement Badge Cards
        items(BadgeCatalog.allBadges, key = { it.id }) { badge ->
            val isUnlocked = profile.unlockedBadges.contains(badge.id)
            val (currentProg, targetProg) = BadgeCatalog.progressForBadge(
                badge = badge,
                profile = profile,
                tasks = tasks,
                todayDate = todayDate
            )
            AchievementBadgeCard(
                badge = badge,
                isUnlocked = isUnlocked,
                currentProgress = currentProg,
                targetProgress = targetProg
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun AchievementBadgeCard(
    badge: AchievementBadge,
    isUnlocked: Boolean,
    currentProgress: Int,
    targetProgress: Int
) {
    val accentColor = Color(badge.accentHex)
    val fraction = if (targetProgress > 0) {
        (currentProgress.toFloat() / targetProgress.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isUnlocked) 2.dp else 1.dp,
                color = if (isUnlocked) StarGold else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("badge_card_${badge.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) {
                accentColor.copy(alpha = 0.10f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeMedallionCanvas(
                badge = badge,
                unlocked = isUnlocked,
                modifier = Modifier.size(68.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isUnlocked) {
                        Surface(
                            color = QuestMint.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "UNLOCKED ★",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF007F5F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked badge",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = if (isUnlocked) QuestMint else accentColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$currentProgress / $targetProgress",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isUnlocked) QuestMint else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}
