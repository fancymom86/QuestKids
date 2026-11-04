package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import com.example.data.model.PetGrowthStage
import com.example.data.model.PetRealm
import com.example.data.model.PetSpecies
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rich, layered 3D-style Canvas illustration for all 10 Pet Species across the 5 Realms
 * (Sea, Land, Air, Enchantment, Space) with visual evolution features for Stages 1..5.
 */
@Composable
fun PetCompanionArtworkCanvas(
    species: PetSpecies,
    growthStage: PetGrowthStage,
    modifier: Modifier = Modifier,
    pulseTick: Int = 0,
    animate: Boolean = true
) {
    val infinite = rememberInfiniteTransition(label = "pet_companion_anim")
    val bouncePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bounce_phase"
    )
    val wingFlap by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wing_flap"
    )
    val stageScale by animateFloatAsState(
        targetValue = growthStage.scaleFactor + (if (pulseTick % 2 == 1) 0.04f else 0f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "stage_scale"
    )

    val primary = Color(species.primaryColorHex)
    val secondary = Color(species.secondaryColorHex)
    val belly = Color(species.bellyColorHex)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)
        val bobY = if (animate) sin(bouncePhase) * minDim * 0.03f else 0f
        val center = Offset(w * 0.5f, h * 0.54f + bobY)
        val tier = growthStage.tierIndex

        // Tier 4+ Evolution Aura Halo (Upgrades at Tier 7 Solar & Tier 10/11 Omniverse)
        if (tier >= 4) {
            val auraColors = when {
                tier >= 10 -> listOf(
                    Color(0xFF00F5D4).copy(alpha = 0.48f),
                    Color(0xFFFFD13B).copy(alpha = 0.34f),
                    Color(0xFFF72585).copy(alpha = 0.22f),
                    Color.Transparent
                )
                tier >= 7 -> listOf(
                    Color(0xFFFF9E00).copy(alpha = 0.48f),
                    Color(0xFFFFEA79).copy(alpha = 0.28f),
                    Color.Transparent
                )
                else -> listOf(
                    Color(0xFFFFD13B).copy(alpha = 0.42f),
                    primary.copy(alpha = 0.18f),
                    Color.Transparent
                )
            }
            drawCircle(
                brush = Brush.radialGradient(
                    colors = auraColors,
                    center = center,
                    radius = minDim * 0.49f
                ),
                radius = minDim * 0.49f,
                center = center
            )
        }

        // Tier 7+ (Lv. 100+) Blazing Solar / Omniverse Halo Ring behind pet
        if (tier >= 7) {
            drawCircle(
                color = if (tier >= 10) Color(0xFF00F5D4) else Color(0xFFFFEA79),
                radius = minDim * 0.39f,
                center = center,
                style = Stroke(width = minDim * 0.022f)
            )
        }

        // Ground shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.18f),
            topLeft = Offset(w * 0.24f, h * 0.84f),
            size = Size(w * 0.52f, h * 0.09f)
        )

        scale(scale = stageScale, pivot = center) {
            // Tier 3+ (Lv. 15+) Heroic Adventure Cape / Tier 8+ Rainbow Nebula Cape
            if (tier >= 3) {
                val capePath = Path().apply {
                    moveTo(center.x - minDim * 0.16f, center.y - minDim * 0.06f)
                    lineTo(center.x - minDim * 0.35f, center.y + minDim * 0.27f)
                    lineTo(center.x + minDim * 0.35f, center.y + minDim * 0.27f)
                    lineTo(center.x + minDim * 0.16f, center.y - minDim * 0.06f)
                    close()
                }
                val capeColors = if (tier >= 8) {
                    listOf(Color(0xFF7209B7), Color(0xFFF72585), Color(0xFF4CC9F0))
                } else {
                    listOf(Color(0xFFFF4D6D), Color(0xFFC9184A))
                }
                drawPath(
                    path = capePath,
                    brush = Brush.verticalGradient(colors = capeColors)
                )
            }

            // Tier 4+ (Lv. 30+) Shimmering Evolution Wings / Tier 9+ Constellation Wings
            if (tier >= 4 || species.realm == PetRealm.AIR || species.id == "magic_dragon") {
                val wingLift = if (animate) wingFlap else 0f
                val leftWing = Path().apply {
                    moveTo(center.x - minDim * 0.16f, center.y - minDim * 0.02f)
                    cubicTo(
                        center.x - minDim * 0.47f, center.y - minDim * 0.28f - wingLift,
                        center.x - minDim * 0.49f, center.y + minDim * 0.12f,
                        center.x - minDim * 0.18f, center.y + minDim * 0.12f
                    )
                    close()
                }
                val rightWing = Path().apply {
                    moveTo(center.x + minDim * 0.16f, center.y - minDim * 0.02f)
                    cubicTo(
                        center.x + minDim * 0.47f, center.y - minDim * 0.28f - wingLift,
                        center.x + minDim * 0.49f, center.y + minDim * 0.12f,
                        center.x + minDim * 0.18f, center.y + minDim * 0.12f
                    )
                    close()
                }
                val wingBrush = when {
                    tier >= 9 -> Brush.linearGradient(
                        listOf(Color(0xFF00F5D4), Color(0xFFFFEA79), Color(0xFFF72585))
                    )
                    tier >= 4 -> Brush.linearGradient(
                        listOf(Color(0xFFFFEA79), secondary, primary)
                    )
                    else -> Brush.linearGradient(listOf(secondary, primary))
                }
                drawPath(leftWing, brush = wingBrush)
                drawPath(rightWing, brush = wingBrush)
            }

            // Species-specific ears / fins / tails / horns behind or attached to body
            drawSpeciesFeaturesBehind(
                species = species,
                center = center,
                minDim = minDim,
                primary = primary,
                secondary = secondary,
                belly = belly,
                bouncePhase = bouncePhase
            )

            // Main Chubby 3D Toy Body
            val bodyWidth = minDim * 0.50f
            val bodyHeight = minDim * 0.48f
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primary.copy(
                            red = (primary.red + 0.14f).coerceAtMost(1f),
                            green = (primary.green + 0.14f).coerceAtMost(1f),
                            blue = (primary.blue + 0.14f).coerceAtMost(1f)
                        ),
                        primary,
                        secondary
                    ),
                    center = Offset(center.x - bodyWidth * 0.15f, center.y - bodyHeight * 0.2f),
                    radius = bodyWidth * 0.85f
                ),
                topLeft = Offset(center.x - bodyWidth / 2f, center.y - bodyHeight / 2f),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(bodyWidth * 0.46f, bodyWidth * 0.46f)
            )

            // Soft glowing belly patch
            val bellyW = bodyWidth * 0.62f
            val bellyH = bodyHeight * 0.55f
            drawOval(
                color = belly,
                topLeft = Offset(center.x - bellyW / 2f, center.y - bellyH * 0.15f),
                size = Size(bellyW, bellyH)
            )

            // Cute Paws / Flippers
            val pawRadius = minDim * 0.068f
            drawCircle(
                color = secondary,
                radius = pawRadius,
                center = Offset(center.x - bodyWidth * 0.30f, center.y + bodyHeight * 0.42f)
            )
            drawCircle(
                color = secondary,
                radius = pawRadius,
                center = Offset(center.x + bodyWidth * 0.30f, center.y + bodyHeight * 0.42f)
            )

            // Expressive Big Sparkling Kid-Friendly Eyes
            val eyeOffsetX = bodyWidth * 0.19f
            val eyeY = center.y - bodyHeight * 0.10f
            val eyeRadius = minDim * 0.062f
            listOf(-1f, 1f).forEach { dir ->
                val ex = center.x + dir * eyeOffsetX
                drawCircle(
                    color = Color(0xFF182238),
                    radius = eyeRadius,
                    center = Offset(ex, eyeY)
                )
                // Big anime/Pixar catchlight
                drawCircle(
                    color = Color.White,
                    radius = eyeRadius * 0.42f,
                    center = Offset(ex - eyeRadius * 0.25f, eyeY - eyeRadius * 0.28f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = eyeRadius * 0.18f,
                    center = Offset(ex + eyeRadius * 0.26f, eyeY + eyeRadius * 0.24f)
                )
            }

            // Rosy Cheeks
            drawOval(
                color = Color(0xFFFF6B8B).copy(alpha = 0.55f),
                topLeft = Offset(center.x - bodyWidth * 0.36f, eyeY + eyeRadius * 0.7f),
                size = Size(minDim * 0.09f, minDim * 0.055f)
            )
            drawOval(
                color = Color(0xFFFF6B8B).copy(alpha = 0.55f),
                topLeft = Offset(center.x + bodyWidth * 0.20f, eyeY + eyeRadius * 0.7f),
                size = Size(minDim * 0.09f, minDim * 0.055f)
            )

            // Species Foreground Details (Beak, Snout, Space Helmet, Unicorn Horn, etc.)
            drawSpeciesFeaturesFront(
                species = species,
                center = center,
                minDim = minDim,
                eyeY = eyeY,
                primary = primary,
                secondary = secondary
            )

            // Tier 2+ (Lv. 5+) Explorer Star Bandana / Tier 7+ Solar Armor Crest
            if (tier >= 2) {
                val bandanaPath = Path().apply {
                    moveTo(center.x - bodyWidth * 0.32f, center.y + bodyHeight * 0.06f)
                    lineTo(center.x + bodyWidth * 0.32f, center.y + bodyHeight * 0.06f)
                    lineTo(center.x, center.y + bodyHeight * 0.25f)
                    close()
                }
                drawPath(
                    path = bandanaPath,
                    color = if (tier >= 7) Color(0xFF4361EE) else Color(0xFFFF5964)
                )
                drawCircle(
                    color = Color(0xFFFFD13B),
                    radius = minDim * 0.035f,
                    center = Offset(center.x, center.y + bodyHeight * 0.13f)
                )
            }

            // Tier 6+ (Lv. 75+) Floating Mystic Crystal Shield Gems
            if (tier >= 6) {
                listOf(-1f, 1f).forEach { side ->
                    val gx = center.x + side * minDim * 0.33f
                    val gy = center.y + minDim * 0.06f
                    val gemPath = Path().apply {
                        moveTo(gx, gy - minDim * 0.065f)
                        lineTo(gx + minDim * 0.045f, gy)
                        lineTo(gx, gy + minDim * 0.065f)
                        lineTo(gx - minDim * 0.045f, gy)
                        close()
                    }
                    drawPath(
                        path = gemPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF00F5D4), Color(0xFF4361EE))
                        )
                    )
                }
            }

            // Tier 9+ (Lv. 160+) Astral Star Scepter
            if (tier >= 9) {
                val sx = center.x + bodyWidth * 0.44f
                val syTop = center.y - bodyHeight * 0.16f
                val syBot = center.y + bodyHeight * 0.38f
                drawLine(
                    color = Color(0xFFFFD13B),
                    start = Offset(sx, syTop),
                    end = Offset(sx, syBot),
                    strokeWidth = minDim * 0.028f,
                    cap = StrokeCap.Round
                )
                drawMiniFourPointStar(
                    center = Offset(sx, syTop),
                    radius = minDim * 0.065f,
                    color = Color(0xFF00F5D4)
                )
            }

            // Tier 1 (Lv. 1–4) Cozy Cracked Eggshell Cradle at bottom
            if (tier == 1) {
                val shellPath = Path().apply {
                    moveTo(center.x - bodyWidth * 0.52f, center.y + bodyHeight * 0.12f)
                    lineTo(center.x - bodyWidth * 0.34f, center.y + bodyHeight * 0.26f)
                    lineTo(center.x - bodyWidth * 0.16f, center.y + bodyHeight * 0.10f)
                    lineTo(center.x, center.y + bodyHeight * 0.26f)
                    lineTo(center.x + bodyWidth * 0.18f, center.y + bodyHeight * 0.10f)
                    lineTo(center.x + bodyWidth * 0.35f, center.y + bodyHeight * 0.25f)
                    lineTo(center.x + bodyWidth * 0.52f, center.y + bodyHeight * 0.12f)
                    cubicTo(
                        center.x + bodyWidth * 0.48f, center.y + bodyHeight * 0.56f,
                        center.x - bodyWidth * 0.48f, center.y + bodyHeight * 0.56f,
                        center.x - bodyWidth * 0.52f, center.y + bodyHeight * 0.12f
                    )
                    close()
                }
                drawPath(
                    path = shellPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE68A))
                    )
                )
            }

            // Tier 5+ (Lv. 50+) Royal Golden Crown / Tier 10+ Diamond Supernova Crown
            if (tier >= 5) {
                val crownTopY = center.y - bodyHeight * 0.54f
                val crownW = minDim * 0.29f
                val crownH = minDim * 0.16f
                val crownPath = Path().apply {
                    moveTo(center.x - crownW / 2f, crownTopY + crownH)
                    lineTo(center.x - crownW * 0.42f, crownTopY)
                    lineTo(center.x - crownW * 0.15f, crownTopY + crownH * 0.52f)
                    lineTo(center.x, crownTopY - crownH * 0.22f)
                    lineTo(center.x + crownW * 0.15f, crownTopY + crownH * 0.52f)
                    lineTo(center.x + crownW * 0.42f, crownTopY)
                    lineTo(center.x + crownW / 2f, crownTopY + crownH)
                    close()
                }
                drawPath(
                    path = crownPath,
                    brush = Brush.verticalGradient(
                        colors = if (tier >= 10) {
                            listOf(Color(0xFF00F5D4), Color(0xFFFFEA79), Color(0xFFFFB703))
                        } else {
                            listOf(Color(0xFFFFEA79), Color(0xFFFFB703))
                        }
                    )
                )
                drawCircle(
                    color = if (tier >= 10) Color(0xFF00F5D4) else Color(0xFFF72585),
                    radius = minDim * 0.026f,
                    center = Offset(center.x, crownTopY + crownH * 0.6f)
                )
            }
        }

        // Tier 8+ (Lv. 130+) Twin Orbiting Moons
        if (tier >= 8) {
            listOf(0f, PI.toFloat()).forEach { offsetAngle ->
                val moonAngle = bouncePhase * 1.4f + offsetAngle
                val mx = center.x + cos(moonAngle) * minDim * 0.44f
                val my = center.y - minDim * 0.18f + sin(moonAngle) * minDim * 0.12f
                drawCircle(
                    color = Color(0xFF90E0EF),
                    radius = minDim * 0.042f,
                    center = Offset(mx, my)
                )
            }
        }

        // Orbiting celebration sparkles (up to 8 stars at Omega Lv. 205!)
        val sparkleCount = tier.coerceIn(1, 8)
        for (i in 0 until sparkleCount) {
            val angle = bouncePhase + (i * (2f * PI / sparkleCount)).toFloat()
            val rx = minDim * 0.41f
            val ry = minDim * 0.34f
            val sx = center.x + cos(angle) * rx
            val sy = center.y + sin(angle) * ry * 0.7f
            drawMiniFourPointStar(
                center = Offset(sx, sy),
                radius = minDim * 0.038f,
                color = if (i % 2 == 0) Color(0xFFFFEA79) else Color(0xFF00F5D4)
            )
        }
    }
}

private fun DrawScope.drawSpeciesFeaturesBehind(
    species: PetSpecies,
    center: Offset,
    minDim: Float,
    primary: Color,
    secondary: Color,
    belly: Color,
    bouncePhase: Float
) {
    when (species.id) {
        "sea_dolphin" -> {
            // Dorsal fin on top & curved tail fluke
            val finPath = Path().apply {
                moveTo(center.x - minDim * 0.05f, center.y - minDim * 0.22f)
                quadraticTo(
                    center.x + minDim * 0.04f, center.y - minDim * 0.38f,
                    center.x + minDim * 0.10f, center.y - minDim * 0.20f
                )
                close()
            }
            drawPath(finPath, color = secondary)
            // Tail fluke on right
            val tailPath = Path().apply {
                moveTo(center.x + minDim * 0.20f, center.y + minDim * 0.10f)
                lineTo(center.x + minDim * 0.40f, center.y + minDim * 0.02f)
                lineTo(center.x + minDim * 0.34f, center.y + minDim * 0.16f)
                lineTo(center.x + minDim * 0.42f, center.y + minDim * 0.26f)
                close()
            }
            drawPath(tailPath, color = secondary)
        }

        "sea_axolotl" -> {
            // 3 cute external coral gill stalks on each side
            listOf(-1f, 1f).forEach { side ->
                listOf(-0.14f, -0.04f, 0.06f).forEach { yOff ->
                    drawLine(
                        color = secondary,
                        start = Offset(center.x + side * minDim * 0.20f, center.y + minDim * yOff),
                        end = Offset(center.x + side * minDim * 0.36f, center.y + minDim * (yOff - 0.06f)),
                        strokeWidth = minDim * 0.045f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        "land_puppy" -> {
            // Floppy golden ears & wagging tail
            val wag = sin(bouncePhase * 2f) * minDim * 0.04f
            drawLine(
                color = secondary,
                start = Offset(center.x + minDim * 0.20f, center.y + minDim * 0.12f),
                end = Offset(center.x + minDim * 0.36f, center.y - minDim * 0.02f + wag),
                strokeWidth = minDim * 0.055f,
                cap = StrokeCap.Round
            )
            listOf(-1f, 1f).forEach { side ->
                drawOval(
                    color = secondary,
                    topLeft = Offset(
                        center.x + side * minDim * 0.25f - minDim * 0.07f,
                        center.y - minDim * 0.22f
                    ),
                    size = Size(minDim * 0.14f, minDim * 0.26f)
                )
            }
        }

        "land_panda" -> {
            // Fluffy striped Red Panda tail + round ears with cream rims
            drawOval(
                color = primary,
                topLeft = Offset(center.x + minDim * 0.16f, center.y + minDim * 0.02f),
                size = Size(minDim * 0.26f, minDim * 0.16f)
            )
            drawCircle(
                color = secondary,
                radius = minDim * 0.09f,
                center = Offset(center.x - minDim * 0.20f, center.y - minDim * 0.22f)
            )
            drawCircle(
                color = secondary,
                radius = minDim * 0.09f,
                center = Offset(center.x + minDim * 0.20f, center.y - minDim * 0.22f)
            )
            drawCircle(
                color = belly,
                radius = minDim * 0.05f,
                center = Offset(center.x - minDim * 0.20f, center.y - minDim * 0.22f)
            )
            drawCircle(
                color = belly,
                radius = minDim * 0.05f,
                center = Offset(center.x + minDim * 0.20f, center.y - minDim * 0.22f)
            )
        }

        "air_owl", "air_griffin" -> {
            // Feathered ear tufts
            listOf(-1f, 1f).forEach { side ->
                val tuft = Path().apply {
                    moveTo(center.x + side * minDim * 0.12f, center.y - minDim * 0.20f)
                    lineTo(center.x + side * minDim * 0.26f, center.y - minDim * 0.34f)
                    lineTo(center.x + side * minDim * 0.22f, center.y - minDim * 0.14f)
                    close()
                }
                drawPath(tuft, color = secondary)
            }
        }

        "magic_dragon" -> {
            // Crystal horns on head
            listOf(-1f, 1f).forEach { side ->
                val horn = Path().apply {
                    moveTo(center.x + side * minDim * 0.10f, center.y - minDim * 0.20f)
                    lineTo(center.x + side * minDim * 0.22f, center.y - minDim * 0.38f)
                    lineTo(center.x + side * minDim * 0.18f, center.y - minDim * 0.18f)
                    close()
                }
                drawPath(horn, color = Color(0xFFFFD13B))
            }
        }

        "magic_unicorn" -> {
            // Pastel ears & rainbow mane puffs
            drawCircle(
                color = Color(0xFFFF6B8B),
                radius = minDim * 0.07f,
                center = Offset(center.x - minDim * 0.22f, center.y - minDim * 0.18f)
            )
            drawCircle(
                color = Color(0xFF4CC9F0),
                radius = minDim * 0.065f,
                center = Offset(center.x - minDim * 0.26f, center.y - minDim * 0.08f)
            )
        }

        "space_kitty" -> {
            // Pointed cosmic cat ears
            listOf(-1f, 1f).forEach { side ->
                val ear = Path().apply {
                    moveTo(center.x + side * minDim * 0.08f, center.y - minDim * 0.20f)
                    lineTo(center.x + side * minDim * 0.24f, center.y - minDim * 0.36f)
                    lineTo(center.x + side * minDim * 0.24f, center.y - minDim * 0.14f)
                    close()
                }
                drawPath(ear, color = primary)
            }
        }

        "space_hopper" -> {
            // Long cosmic bunny ears + glowing star antennae
            listOf(-1f, 1f).forEach { side ->
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(
                        center.x + side * minDim * 0.13f - minDim * 0.045f,
                        center.y - minDim * 0.44f
                    ),
                    size = Size(minDim * 0.09f, minDim * 0.26f),
                    cornerRadius = CornerRadius(minDim * 0.05f, minDim * 0.05f)
                )
                drawCircle(
                    color = Color(0xFFFFBE0B),
                    radius = minDim * 0.036f,
                    center = Offset(center.x + side * minDim * 0.13f, center.y - minDim * 0.46f)
                )
            }
        }
    }
}

private fun DrawScope.drawSpeciesFeaturesFront(
    species: PetSpecies,
    center: Offset,
    minDim: Float,
    eyeY: Float,
    primary: Color,
    secondary: Color
) {
    when (species.id) {
        "air_owl", "air_griffin" -> {
            // Golden diamond beak
            val beak = Path().apply {
                moveTo(center.x, eyeY + minDim * 0.01f)
                lineTo(center.x - minDim * 0.045f, eyeY + minDim * 0.06f)
                lineTo(center.x, eyeY + minDim * 0.11f)
                lineTo(center.x + minDim * 0.045f, eyeY + minDim * 0.06f)
                close()
            }
            drawPath(beak, color = Color(0xFFFFB703))
        }

        "magic_unicorn" -> {
            // Glowing spiral unicorn horn on forehead
            val horn = Path().apply {
                moveTo(center.x - minDim * 0.045f, center.y - minDim * 0.20f)
                lineTo(center.x, center.y - minDim * 0.44f)
                lineTo(center.x + minDim * 0.045f, center.y - minDim * 0.20f)
                close()
            }
            drawPath(
                path = horn,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFEA79), Color(0xFFFF6B8B), Color(0xFF4CC9F0))
                )
            )
            // Happy smile
            drawSmileArc(center, eyeY, minDim)
        }

        "space_kitty", "space_hopper" -> {
            // Translucent Space Glass Visor Ring + Saturn ring
            drawCircle(
                color = Color(0xFF90E0EF).copy(alpha = 0.22f),
                radius = minDim * 0.30f,
                center = Offset(center.x, center.y - minDim * 0.04f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.65f),
                radius = minDim * 0.30f,
                center = Offset(center.x, center.y - minDim * 0.04f),
                style = Stroke(width = minDim * 0.018f)
            )
            drawSmileArc(center, eyeY, minDim)
        }

        "land_puppy", "land_panda" -> {
            // Cute button nose + happy tongue smile
            drawOval(
                color = Color(0xFF182238),
                topLeft = Offset(center.x - minDim * 0.035f, eyeY + minDim * 0.02f),
                size = Size(minDim * 0.07f, minDim * 0.048f)
            )
            drawSmileArc(center, eyeY + minDim * 0.03f, minDim)
        }

        else -> {
            drawSmileArc(center, eyeY, minDim)
        }
    }
}

private fun DrawScope.drawSmileArc(center: Offset, eyeY: Float, minDim: Float) {
    drawArc(
        color = Color(0xFF182238),
        startAngle = 20f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(center.x - minDim * 0.065f, eyeY + minDim * 0.02f),
        size = Size(minDim * 0.13f, minDim * 0.09f),
        style = Stroke(width = minDim * 0.022f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawMiniFourPointStar(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius)
        quadraticTo(center.x, center.y, center.x + radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + radius)
        quadraticTo(center.x, center.y, center.x - radius, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - radius)
        close()
    }
    drawPath(path, color = color)
}

/**
 * Full Realm Habitat Scene with animated environment graphics (Sea reef bubbles,
 * Land sunny hills, Air rainbow clouds, Enchanted crystals, Space nebula & Saturn planet)
 * and the child's animated Pet Companion centered inside.
 */
@Composable
fun RealmHabitatSceneCanvas(
    species: PetSpecies,
    growthStage: PetGrowthStage,
    pulseTick: Int,
    modifier: Modifier = Modifier
) {
    val realm = species.realm
    val infinite = rememberInfiniteTransition(label = "habitat_scene")
    val ambientFloat by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambient_float"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Sky / Realm Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(realm.skyTopHex),
                        Color(realm.skyBottomHex)
                    )
                )
            )

            when (realm) {
                PetRealm.SEA -> {
                    // Underwater sunrays & rising bubbles & coral reef
                    for (i in 0..5) {
                        val bx = w * (0.12f + i * 0.16f)
                        val by = h * (0.75f - ((ambientFloat + i * 0.17f) % 1f) * 0.6f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.28f),
                            radius = 8f + (i % 3) * 5f,
                            center = Offset(bx, by)
                        )
                    }
                    // Sandy reef floor
                    drawOval(
                        color = Color(0xFFFFD166),
                        topLeft = Offset(-w * 0.1f, h * 0.72f),
                        size = Size(w * 1.2f, h * 0.45f)
                    )
                }

                PetRealm.LAND -> {
                    // Warm sun + rolling green meadow hills
                    drawCircle(
                        color = Color(0xFFFFEA79).copy(alpha = 0.85f),
                        radius = minOf(w, h) * 0.14f,
                        center = Offset(w * 0.84f, h * 0.22f)
                    )
                    drawOval(
                        color = Color(0xFF52B788),
                        topLeft = Offset(-w * 0.15f, h * 0.65f),
                        size = Size(w * 0.85f, h * 0.5f)
                    )
                    drawOval(
                        color = Color(realm.groundHex),
                        topLeft = Offset(w * 0.2f, h * 0.68f),
                        size = Size(w * 0.95f, h * 0.5f)
                    )
                }

                PetRealm.AIR -> {
                    // Rainbow arc + fluffy sky clouds
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFFFF5964), Color(0xFFFFCA3A), Color(0xFF8AC926), Color(0xFF1982C4))
                        ),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.12f, h * 0.16f),
                        size = Size(w * 0.76f, h * 0.85f),
                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                    )
                    drawOval(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(w * 0.08f, h * 0.72f),
                        size = Size(w * 0.84f, h * 0.34f)
                    )
                }

                PetRealm.ENCHANTMENT -> {
                    // Glowing crystal spires & magical fireflies
                    for (i in 0..6) {
                        val fx = w * (0.1f + i * 0.13f)
                        val fy = h * (0.2f + sin(ambientFloat * PI.toFloat() + i) * 0.15f)
                        drawCircle(
                            color = Color(0xFFFFEA79).copy(alpha = 0.75f),
                            radius = 6f,
                            center = Offset(fx, fy)
                        )
                    }
                    drawOval(
                        color = Color(realm.groundHex),
                        topLeft = Offset(-w * 0.1f, h * 0.70f),
                        size = Size(w * 1.2f, h * 0.45f)
                    )
                }

                PetRealm.SPACE -> {
                    // Twinkling starfield + ringed planet + cosmic moon surface
                    for (i in 0..9) {
                        val sx = w * ((i * 0.11f + 0.05f) % 0.95f)
                        val sy = h * ((i * 0.17f + 0.08f) % 0.58f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = if (i % 2 == 0) 4f else 2.5f,
                            center = Offset(sx, sy)
                        )
                    }
                    // Ringed planet in top corner
                    val px = w * 0.82f
                    val py = h * 0.24f
                    drawCircle(
                        color = Color(0xFFFF9E00),
                        radius = minOf(w, h) * 0.09f,
                        center = Offset(px, py)
                    )
                    drawOval(
                        color = Color(0xFFFFEA79).copy(alpha = 0.7f),
                        topLeft = Offset(px - minOf(w, h) * 0.16f, py - minOf(w, h) * 0.03f),
                        size = Size(minOf(w, h) * 0.32f, minOf(w, h) * 0.06f),
                        style = Stroke(width = 4f)
                    )
                    drawOval(
                        color = Color(realm.groundHex),
                        topLeft = Offset(-w * 0.1f, h * 0.71f),
                        size = Size(w * 1.2f, h * 0.45f)
                    )
                }
            }
        }

        PetCompanionArtworkCanvas(
            species = species,
            growthStage = growthStage,
            pulseTick = pulseTick,
            modifier = Modifier.fillMaxSize(0.78f)
        )
    }
}

/**
 * Colorful 3D-style Parent Gift Box illustration for the Rewards Chest tab.
 */
@Composable
fun GiftBoxIllustrationCanvas(
    accentColor: Color,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val boxColor = if (unlocked) accentColor else Color(0xFF94A3B8)
        val ribbonColor = if (unlocked) Color(0xFFFFD13B) else Color(0xFFCBD5E1)

        // Gift box base
        drawRoundRect(
            color = boxColor,
            topLeft = Offset(w * 0.16f, h * 0.36f),
            size = Size(w * 0.68f, h * 0.52f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
        )
        // Gift box lid
        drawRoundRect(
            color = boxColor.copy(
                red = (boxColor.red + 0.1f).coerceAtMost(1f),
                green = (boxColor.green + 0.1f).coerceAtMost(1f),
                blue = (boxColor.blue + 0.1f).coerceAtMost(1f)
            ),
            topLeft = Offset(w * 0.11f, h * 0.24f),
            size = Size(w * 0.78f, h * 0.16f),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )
        // Vertical & horizontal gold ribbon
        drawRect(
            color = ribbonColor,
            topLeft = Offset(w * 0.43f, h * 0.24f),
            size = Size(w * 0.14f, h * 0.64f)
        )
        drawRect(
            color = ribbonColor,
            topLeft = Offset(w * 0.16f, h * 0.56f),
            size = Size(w * 0.68f, h * 0.12f)
        )
        // Bow loops on top
        drawOval(
            color = ribbonColor,
            topLeft = Offset(w * 0.26f, h * 0.10f),
            size = Size(w * 0.24f, h * 0.16f),
            style = Stroke(width = w * 0.06f)
        )
        drawOval(
            color = ribbonColor,
            topLeft = Offset(w * 0.50f, h * 0.10f),
            size = Size(w * 0.24f, h * 0.16f),
            style = Stroke(width = w * 0.06f)
        )
    }
}
