package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CleanHands
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AchievementBadge
import com.example.data.model.AvatarCatalog
import com.example.data.model.KidProfile
import com.example.data.model.RoutineType
import com.example.ui.theme.QuestMint
import com.example.ui.theme.StarGold
import com.example.ui.theme.StarGoldBright
import com.example.ui.theme.StreakFlameCoral
import com.example.ui.theme.StreakFlameOrange
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StarBuddyMascotCanvas(
    avatarId: String,
    modifier: Modifier = Modifier.size(64.dp),
    animate: Boolean = true
) {
    val avatar = AvatarCatalog.getById(avatarId)
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_bob")
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob_y"
    )
    val yShift = if (animate) bobOffset else 0f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f + yShift
        val radius = minOf(w, h) * 0.42f

        val primaryColor = Color(avatar.primaryHex)
        val secondaryColor = Color(avatar.secondaryHex)

        // Outer halo glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(cx, cy),
                radius = radius * 1.25f
            ),
            radius = radius * 1.25f,
            center = Offset(cx, cy)
        )

        // Backdrop coin
        drawCircle(
            color = secondaryColor,
            radius = radius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = primaryColor,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = radius * 0.1f)
        )

        when (avatar.id) {
            "dino_ranger" -> {
                // Cute Dino spikes on top
                for (i in -1..1) {
                    val spikePath = Path().apply {
                        val sx = cx + i * radius * 0.3f
                        val sy = cy - radius * 0.65f
                        moveTo(sx - radius * 0.12f, sy + radius * 0.18f)
                        lineTo(sx, sy - radius * 0.12f)
                        lineTo(sx + radius * 0.12f, sy + radius * 0.18f)
                        close()
                    }
                    drawPath(spikePath, color = Color(0xFFFFD13B))
                }
                drawCircle(color = primaryColor, radius = radius * 0.62f, center = Offset(cx, cy + radius * 0.05f))
            }
            "space_bunny" -> {
                // Bunny ears
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(cx - radius * 0.42f, cy - radius * 0.85f),
                    size = Size(radius * 0.26f, radius * 0.65f),
                    cornerRadius = CornerRadius(radius * 0.13f, radius * 0.13f)
                )
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(cx + radius * 0.16f, cy - radius * 0.85f),
                    size = Size(radius * 0.26f, radius * 0.65f),
                    cornerRadius = CornerRadius(radius * 0.13f, radius * 0.13f)
                )
                drawCircle(color = primaryColor, radius = radius * 0.58f, center = Offset(cx, cy + radius * 0.08f))
            }
            "robo_buddy" -> {
                // Antenna
                drawLine(
                    color = Color(0xFFFFD13B),
                    start = Offset(cx, cy - radius * 0.55f),
                    end = Offset(cx, cy - radius * 0.82f),
                    strokeWidth = radius * 0.08f,
                    cap = StrokeCap.Round
                )
                drawCircle(color = Color(0xFFFF5964), radius = radius * 0.1f, center = Offset(cx, cy - radius * 0.84f))
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(cx - radius * 0.55f, cy - radius * 0.5f),
                    size = Size(radius * 1.1f, radius * 1.05f),
                    cornerRadius = CornerRadius(radius * 0.24f, radius * 0.24f)
                )
            }
            else -> {
                // Five-pointed Star Buddy body
                drawStarShape(
                    center = Offset(cx, cy),
                    outerRadius = radius * 0.78f,
                    innerRadius = radius * 0.42f,
                    color = primaryColor
                )
            }
        }

        // Cute smiling eyes & rosy cheeks
        val eyeOffsetX = radius * 0.22f
        val eyeY = cy - radius * 0.04f
        drawCircle(color = Color(0xFF182238), radius = radius * 0.09f, center = Offset(cx - eyeOffsetX, eyeY))
        drawCircle(color = Color(0xFF182238), radius = radius * 0.09f, center = Offset(cx + eyeOffsetX, eyeY))
        drawCircle(color = Color.White, radius = radius * 0.035f, center = Offset(cx - eyeOffsetX - 1.5f, eyeY - 1.5f))
        drawCircle(color = Color.White, radius = radius * 0.035f, center = Offset(cx + eyeOffsetX - 1.5f, eyeY - 1.5f))

        // Rosy cheeks
        drawCircle(
            color = Color(0xFFFF6B8B).copy(alpha = 0.65f),
            radius = radius * 0.09f,
            center = Offset(cx - radius * 0.34f, eyeY + radius * 0.14f)
        )
        drawCircle(
            color = Color(0xFFFF6B8B).copy(alpha = 0.65f),
            radius = radius * 0.09f,
            center = Offset(cx + radius * 0.34f, eyeY + radius * 0.14f)
        )

        // Happy smile arc
        drawArc(
            color = Color(0xFF182238),
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(cx - radius * 0.18f, eyeY + radius * 0.02f),
            size = Size(radius * 0.36f, radius * 0.24f),
            style = Stroke(width = radius * 0.07f, cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawStarShape(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    val numPoints = 5
    val angleStep = PI / numPoints
    val startAngle = -PI / 2.0

    for (i in 0 until numPoints * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = startAngle + i * angleStep
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}

@Composable
fun HeroQuestIslandBanner(
    profile: KidProfile,
    completedTodayCount: Int,
    totalTasksCount: Int,
    selectedRoutine: RoutineType?,
    onEditProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalTasksCount > 0) {
        (completedTodayCount.toFloat() / totalTasksCount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val gradientColors = when (selectedRoutine) {
        RoutineType.MORNING -> listOf(Color(0xFF2D46B9), Color(0xFF4EA8DE), Color(0xFFFFB703))
        RoutineType.AFTERNOON -> listOf(Color(0xFF1D3557), Color(0xFF2EC4B6), Color(0xFF80ED99))
        RoutineType.EVENING -> listOf(Color(0xFF0F172A), Color(0xFF311B92), Color(0xFF7209B7))
        RoutineType.WEEKEND -> listOf(Color(0xFF3A0CA3), Color(0xFFF72585), Color(0xFFFFBE0B))
        null -> listOf(Color(0xFF1B2A4A), Color(0xFF2D46B9), Color(0xFF4895EF))
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(18.dp)
        ) {
            // Decorative background stars & stepping stone trail
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                // Twinkling background stars
                val starOffsets = listOf(
                    Offset(w * 0.15f, h * 0.18f),
                    Offset(w * 0.45f, h * 0.12f),
                    Offset(w * 0.78f, h * 0.22f),
                    Offset(w * 0.88f, h * 0.55f),
                    Offset(w * 0.28f, h * 0.65f)
                )
                starOffsets.forEachIndexed { idx, pos ->
                    drawCircle(
                        color = Color.White.copy(alpha = if (idx % 2 == 0) 0.35f else 0.2f),
                        radius = if (idx % 2 == 0) 5f else 3.5f,
                        center = pos
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Mascot Avatar + Kid Name + Level + Streak & Star Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onEditProfileClick)
                            .padding(end = 8.dp)
                            .testTag("edit_profile_button")
                    ) {
                        StarBuddyMascotCanvas(
                            avatarId = profile.avatarId,
                            modifier = Modifier.size(58.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = profile.kidName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                            val avatarInfo = AvatarCatalog.getById(profile.avatarId)
                            Text(
                                text = "Lvl ${profile.level} • ${avatarInfo.title}",
                                style = MaterialTheme.typography.labelMedium,
                                color = StarGoldBright
                            )
                        }
                    }

                    // Star Bank & Streak Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Streak Flame Pill
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(
                                1.dp,
                                StreakFlameOrange.copy(alpha = 0.7f),
                                RoundedCornerShape(50)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Current streak days",
                                    tint = StreakFlameOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.currentStreak}d",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }

                        // Star Balance Pill
                        Surface(
                            color = StarGold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.border(
                                1.5.dp,
                                StarGoldBright,
                                RoundedCornerShape(50)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Total stars",
                                    tint = StarGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.totalStars}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Adventure Island Progress Trail
                Surface(
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (completedTodayCount == totalTasksCount && totalTasksCount > 0) {
                                    "🎉 All Daily Quests Complete!"
                                } else {
                                    "Today's Adventure Trail"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = "$completedTodayCount / $totalTasksCount Quests",
                                style = MaterialTheme.typography.labelLarge,
                                color = StarGoldBright
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stepping stone progress bar with glowing nodes
                        SteppingStoneProgressBar(
                            progress = progress,
                            steps = totalTasksCount.coerceIn(3, 8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SteppingStoneProgressBar(
    progress: Float,
    steps: Int,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
    ) {
        val w = size.width
        val h = size.height
        val cy = h / 2f
        val startX = 14.dp.toPx()
        val endX = w - 14.dp.toPx()
        val trackWidth = (endX - startX).coerceAtLeast(1f)

        // Track background
        drawLine(
            color = Color.White.copy(alpha = 0.24f),
            start = Offset(startX, cy),
            end = Offset(endX, cy),
            strokeWidth = 8.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Active golden trail
        if (progress > 0f) {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(QuestMint, StarGoldBright)
                ),
                start = Offset(startX, cy),
                end = Offset(startX + trackWidth * progress, cy),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Stepping stones
        for (i in 0 until steps) {
            val fraction = if (steps > 1) i.toFloat() / (steps - 1) else 1f
            val sx = startX + trackWidth * fraction
            val reached = progress >= (fraction - 0.01f)
            drawCircle(
                color = if (reached) StarGoldBright else Color.White.copy(alpha = 0.45f),
                radius = if (i == steps - 1) 9.dp.toPx() else 6.dp.toPx(),
                center = Offset(sx, cy)
            )
            if (reached) {
                drawCircle(
                    color = Color(0xFF182238),
                    radius = 2.5.dp.toPx(),
                    center = Offset(sx, cy)
                )
            }
        }
    }
}

@Composable
fun BadgeMedallionCanvas(
    badge: AchievementBadge,
    unlocked: Boolean,
    modifier: Modifier = Modifier.size(68.dp)
) {
    val accent = Color(badge.accentHex)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = minOf(w, h) * 0.44f

            if (unlocked) {
                // Bottom award ribbons
                val ribbonPathLeft = Path().apply {
                    moveTo(cx - r * 0.45f, cy + r * 0.55f)
                    lineTo(cx - r * 0.65f, h * 0.96f)
                    lineTo(cx - r * 0.35f, h * 0.88f)
                    lineTo(cx - r * 0.15f, h * 0.98f)
                    lineTo(cx - r * 0.15f, cy + r * 0.6f)
                    close()
                }
                val ribbonPathRight = Path().apply {
                    moveTo(cx + r * 0.45f, cy + r * 0.55f)
                    lineTo(cx + r * 0.65f, h * 0.96f)
                    lineTo(cx + r * 0.35f, h * 0.88f)
                    lineTo(cx + r * 0.15f, h * 0.98f)
                    lineTo(cx + r * 0.15f, cy + r * 0.6f)
                    close()
                }
                drawPath(ribbonPathLeft, color = StreakFlameCoral)
                drawPath(ribbonPathRight, color = StreakFlameCoral)

                // Outer golden burst glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(cx, cy - 2f),
                        radius = r * 1.2f
                    ),
                    radius = r * 1.2f,
                    center = Offset(cx, cy - 2f)
                )
            }

            // Medallion outer rim
            drawCircle(
                color = if (unlocked) StarGoldBright else Color(0xFF94A3B8).copy(alpha = 0.35f),
                radius = r,
                center = Offset(cx, cy - 2f)
            )
            // Inner enamel face
            drawCircle(
                color = if (unlocked) accent else Color(0xFF64748B).copy(alpha = 0.25f),
                radius = r * 0.82f,
                center = Offset(cx, cy - 2f)
            )
            // Center star highlight
            drawStarShape(
                center = Offset(cx, cy - 2f),
                outerRadius = r * 0.48f,
                innerRadius = r * 0.24f,
                color = if (unlocked) Color.White.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.35f)
            )
        }
    }
}

@Composable
fun TaskIconBadge(
    iconName: String,
    completed: Boolean,
    routine: RoutineType,
    modifier: Modifier = Modifier
) {
    val bgTint = when {
        completed -> QuestMint.copy(alpha = 0.18f)
        routine == RoutineType.MORNING -> Color(0xFFFFB800).copy(alpha = 0.18f)
        routine == RoutineType.AFTERNOON -> Color(0xFF00B4D8).copy(alpha = 0.18f)
        routine == RoutineType.EVENING -> Color(0xFF845EC2).copy(alpha = 0.18f)
        else -> Color(0xFFFF5964).copy(alpha = 0.18f)
    }
    val iconTint = when {
        completed -> QuestMint
        routine == RoutineType.MORNING -> Color(0xFFD97706)
        routine == RoutineType.AFTERNOON -> Color(0xFF0284C7)
        routine == RoutineType.EVENING -> Color(0xFF7C3AED)
        else -> StreakFlameCoral
    }

    val vector = when (iconName.lowercase()) {
        "toothbrush", "clean" -> Icons.Default.CleanHands
        "bed", "sleep" -> Icons.Default.Bed
        "apple", "food", "vegetables" -> Icons.Default.LocalDining
        "backpack", "school" -> Icons.Default.Backpack
        "book", "read" -> Icons.Default.AutoStories
        "toys", "play" -> Icons.Default.SmartToy
        "pajamas", "clothes" -> Icons.Default.Checkroom
        "trophy", "sport" -> Icons.Default.EmojiEvents
        "pet", "dog" -> Icons.Default.Pets
        "movie" -> Icons.Default.Movie
        "ice_cream" -> Icons.Default.Icecream
        "gift" -> Icons.Default.Redeem
        else -> Icons.Default.Star
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(bgTint),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = vector,
            contentDescription = iconName,
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}
