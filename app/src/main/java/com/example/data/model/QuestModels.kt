package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class RoutineType(
    val key: String,
    val label: String,
    val subtitle: String,
    val emoji: String
) {
    MORNING("MORNING", "Morning Sun", "Start the day bright & early!", "☀️"),
    AFTERNOON("AFTERNOON", "Afternoon Power", "After-school quests & play!", "🚀"),
    EVENING("EVENING", "Bedtime Stars", "Wind down & get cozy for sleep!", "🌙"),
    WEEKEND("WEEKEND", "Weekend Warrior", "Special Saturday & Sunday adventures!", "🏆");

    companion object {
        fun fromKey(key: String): RoutineType =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: MORNING
    }
}

enum class PetRealm(
    val key: String,
    val label: String,
    val emoji: String,
    val habitatTitle: String,
    val skyTopHex: Long,
    val skyBottomHex: Long,
    val groundHex: Long
) {
    SEA("SEA", "Sea & Reef", "🌊", "Crystal Lagoon Sanctuary", 0xFF0077B6, 0xFF00B4D8, 0xFF48CAE4),
    LAND("LAND", "Sunny Land", "🌿", "Clover Meadow Sanctuary", 0xFF38B000, 0xFF70E000, 0xFF2D6A4F),
    AIR("AIR", "Cloud Sky", "☁️", "Rainbow Cloud Sanctuary", 0xFF4EA8DE, 0xFF90E0EF, 0xFF5E60CE),
    ENCHANTMENT("ENCHANTMENT", "Enchanted", "✨", "Mystic Crystal Glade", 0xFF5A189A, 0xFF9D4EDD, 0xFF3C096C),
    SPACE("SPACE", "Deep Space", "🪐", "Starlight Nebula Orbit", 0xFF0F172A, 0xFF311B92, 0xFF4361EE)
}

data class PetSpecies(
    val id: String,
    val defaultName: String,
    val speciesTitle: String,
    val realm: PetRealm,
    val description: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val bellyColorHex: Long
)

enum class PetGrowthStage(
    val tierIndex: Int,
    val level: Long,
    val maxTierLevel: Long,
    val title: String,
    val minStars: Long,
    val nextMilestoneStars: Long,
    val scaleFactor: Float,
    val accessoryLabel: String,
    val milestoneBonusStars: Long
) {
    HATCHLING(
        tierIndex = 1,
        level = 1L,
        maxTierLevel = 4L,
        title = "Tier 1: Tiny Hatchling (Lv. 1–4)",
        minStars = 0L,
        nextMilestoneStars = 20L,
        scaleFactor = 0.78f,
        accessoryLabel = "Cozy Eggshell Cradle",
        milestoneBonusStars = 5L
    ),
    SPROUT(
        tierIndex = 2,
        level = 5L,
        maxTierLevel = 14L,
        title = "Tier 2: Curious Sprout (Lv. 5–14)",
        minStars = 20L,
        nextMilestoneStars = 45L,
        scaleFactor = 0.85f,
        accessoryLabel = "Explorer Star Bandana",
        milestoneBonusStars = 10L
    ),
    BUDDY(
        tierIndex = 3,
        level = 15L,
        maxTierLevel = 29L,
        title = "Tier 3: Super Companion (Lv. 15–29)",
        minStars = 45L,
        nextMilestoneStars = 80L,
        scaleFactor = 0.92f,
        accessoryLabel = "Heroic Adventure Cape",
        milestoneBonusStars = 15L
    ),
    CHAMPION(
        tierIndex = 4,
        level = 30L,
        maxTierLevel = 49L,
        title = "Tier 4: Mystic Champion (Lv. 30–49)",
        minStars = 80L,
        nextMilestoneStars = 125L,
        scaleFactor = 0.98f,
        accessoryLabel = "Shimmering Evolution Wings",
        milestoneBonusStars = 20L
    ),
    ROYAL_LEGEND(
        tierIndex = 5,
        level = 50L,
        maxTierLevel = 74L,
        title = "Tier 5: Cosmic Royal Legend (Lv. 50–74)",
        minStars = 125L,
        nextMilestoneStars = 500L,
        scaleFactor = 1.03f,
        accessoryLabel = "Golden Royal Crown & Star Halo",
        milestoneBonusStars = 25L
    ),
    CRYSTAL_GUARDIAN(
        tierIndex = 6,
        level = 75L,
        maxTierLevel = 99L,
        title = "Tier 6: Crystal Gem Guardian (Lv. 75–99)",
        minStars = 500L,
        nextMilestoneStars = 875L,
        scaleFactor = 1.06f,
        accessoryLabel = "Floating Mystic Crystal Shield",
        milestoneBonusStars = 30L
    ),
    SOLAR_KNIGHT(
        tierIndex = 7,
        level = 100L,
        maxTierLevel = 129L,
        title = "Tier 7: Solar Sunbeam Knight (Lv. 100–129)",
        minStars = 875L,
        nextMilestoneStars = 1325L,
        scaleFactor = 1.09f,
        accessoryLabel = "Blazing Solar Halo & Armor Crest",
        milestoneBonusStars = 35L
    ),
    GALACTIC_SOVEREIGN(
        tierIndex = 8,
        level = 130L,
        maxTierLevel = 159L,
        title = "Tier 8: Galactic Nova Sovereign (Lv. 130–159)",
        minStars = 1325L,
        nextMilestoneStars = 1775L,
        scaleFactor = 1.12f,
        accessoryLabel = "Twin Orbiting Moons & Nebula Cape",
        milestoneBonusStars = 40L
    ),
    ASTRAL_MYTHIC(
        tierIndex = 9,
        level = 160L,
        maxTierLevel = 189L,
        title = "Tier 9: Astral Starlight Mythic (Lv. 160–189)",
        minStars = 1775L,
        nextMilestoneStars = 2225L,
        scaleFactor = 1.14f,
        accessoryLabel = "Constellation Wings & Star Scepter",
        milestoneBonusStars = 45L
    ),
    CELESTIAL_ASCENDANT(
        tierIndex = 10,
        level = 190L,
        maxTierLevel = 204L,
        title = "Tier 10: Celestial Ascendant (Lv. 190–204)",
        minStars = 2225L,
        nextMilestoneStars = 2450L,
        scaleFactor = 1.16f,
        accessoryLabel = "Diamond Supernova Crown & Prismatic Aura",
        milestoneBonusStars = 50L
    ),
    OMEGA_OMNIVERSE(
        tierIndex = 11,
        level = 205L,
        maxTierLevel = 205L,
        title = "Tier 11: Omega Lv. 205 Omniverse Legend",
        minStars = 2450L,
        nextMilestoneStars = 2500L,
        scaleFactor = 1.18f,
        accessoryLabel = "Ultimate Omniverse Halo & 8 Orbiting Stars",
        milestoneBonusStars = 100L
    );

    companion object {
        const val MAX_PET_LEVEL = 205L

        /**
         * Maps any level 1..205 to its visual evolution tier.
         */
        fun fromLevel(level: Long): PetGrowthStage {
            val clamped = level.coerceIn(1L, MAX_PET_LEVEL)
            return entries.firstOrNull { clamped in it.level..it.maxTierLevel }
                ?: if (clamped >= MAX_PET_LEVEL) OMEGA_OMNIVERSE else HATCHLING
        }

        /**
         * Calculates the exact Pet Evolution Level (1..205) from a star balance.
         * Every 12 stars advances 1 full pet evolution level all the way up to Level 205!
         */
        fun levelFromStarBalance(totalStars: Long): Long {
            val safeStars = totalStars.coerceAtLeast(0L)
            return when {
                safeStars < 20L -> (1L + safeStars / 5L).coerceIn(1L, 4L)
                safeStars < 45L -> (5L + (safeStars - 20L) / 3L).coerceIn(5L, 14L)
                safeStars < 80L -> (15L + (safeStars - 45L) / 3L).coerceIn(15L, 29L)
                safeStars < 125L -> (30L + (safeStars - 80L) / 3L).coerceIn(30L, 49L)
                else -> (50L + (safeStars - 125L) / 15L).coerceIn(50L, MAX_PET_LEVEL)
            }
        }

        fun starsRequiredForLevel(targetLevel: Long): Long {
            val lvl = targetLevel.coerceIn(1L, MAX_PET_LEVEL)
            return when {
                lvl <= 1L -> 0L
                lvl in 2L..4L -> (lvl - 1L) * 5L
                lvl in 5L..14L -> 20L + (lvl - 5L) * 3L
                lvl in 15L..29L -> 45L + (lvl - 15L) * 3L
                lvl in 30L..49L -> 80L + (lvl - 30L) * 3L
                else -> 125L + (lvl - 50L) * 15L
            }
        }

        fun fromStarBalance(totalStars: Long): PetGrowthStage {
            val computedLevel = levelFromStarBalance(totalStars)
            return fromLevel(computedLevel)
        }
    }
}

enum class PetMiniGameType(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val maxStarReward: Long,
    val xpReward: Long,
    val treatReward: Long,
    val accentHex: Long
) {
    STAR_CATCHER(
        id = "star_catcher",
        title = "Star Catcher Dash",
        subtitle = "Guide your pet across 3 sky lanes to catch falling golden stars!",
        emoji = "🌟",
        maxStarReward = 15L,
        xpReward = 45L,
        treatReward = 2L,
        accentHex = 0xFFFFB703
    ),
    BUBBLE_MEMORY(
        id = "bubble_memory",
        title = "Mystic Treasure Match",
        subtitle = "Team up with your pet to match pairs of hidden realm treasures!",
        emoji = "🫧",
        maxStarReward = 20L,
        xpReward = 60L,
        treatReward = 2L,
        accentHex = 0xFF00B4D8
    ),
    COSMIC_JUMP(
        id = "cosmic_jump",
        title = "Cosmic Trick Jump",
        subtitle = "Tap JUMP when the power ring hits gold for mega evolution XP!",
        emoji = "🚀",
        maxStarReward = 25L,
        xpReward = 80L,
        treatReward = 3L,
        accentHex = 0xFF9B5DE5
    )
}

enum class PetCareAction(
    val id: String,
    val label: String,
    val subtitle: String,
    val emoji: String,
    val treatCost: Long,
    val happinessBoost: Long,
    val xpGain: Long,
    val bonusStars: Long
) {
    FEED("feed", "Feed Star Berry", "+5 ★ Gift Points & +20 Happy", "🍓", 1L, 20L, 25L, 5L),
    PLAY("play", "Toss Cosmic Ball", "+5 ★ Gift Points & +25 Happy", "⚽", 1L, 25L, 30L, 5L),
    CUDDLE("cuddle", "Magical Belly Rub", "Free Cuddle! +2 ★ & +15 Happy", "💖", 0L, 15L, 15L, 2L)
}

object PetCatalog {
    val allPets = listOf(
        // SEA REALM
        PetSpecies(
            id = "sea_dolphin",
            defaultName = "Coral",
            speciesTitle = "Splash Dolphin",
            realm = PetRealm.SEA,
            description = "Leaps over ocean waves and blows rainbow water bubbles!",
            primaryColorHex = 0xFF00B4D8,
            secondaryColorHex = 0xFF0077B6,
            bellyColorHex = 0xFFCAF0F8
        ),
        PetSpecies(
            id = "sea_axolotl",
            defaultName = "Bubbles",
            speciesTitle = "Starry Axolotl",
            realm = PetRealm.SEA,
            description = "A smiling pink reef buddy with glowing coral gills!",
            primaryColorHex = 0xFFFF85A1,
            secondaryColorHex = 0xFFF72585,
            bellyColorHex = 0xFFFFE5EC
        ),
        // LAND REALM
        PetSpecies(
            id = "land_puppy",
            defaultName = "Sunny",
            speciesTitle = "Golden Meadow Pup",
            realm = PetRealm.LAND,
            description = "Loyal, bouncy puppy who loves chasing golden stars!",
            primaryColorHex = 0xFFFFB703,
            secondaryColorHex = 0xFFFB8500,
            bellyColorHex = 0xFFFFF3B0
        ),
        PetSpecies(
            id = "land_panda",
            defaultName = "Bamboo",
            speciesTitle = "Cinnamon Red Panda",
            realm = PetRealm.LAND,
            description = "Fluffy forest explorer with a giant striped tail!",
            primaryColorHex = 0xFFE85D04,
            secondaryColorHex = 0xFF9D0208,
            bellyColorHex = 0xFFFFEDD8
        ),
        // AIR REALM
        PetSpecies(
            id = "air_owl",
            defaultName = "Skyler",
            speciesTitle = "Breeze Cloud Owlet",
            realm = PetRealm.AIR,
            description = "Flutters through rainbow clouds with wise sparkling eyes!",
            primaryColorHex = 0xFF4CC9F0,
            secondaryColorHex = 0xFF4361EE,
            bellyColorHex = 0xFFE0FBFC
        ),
        PetSpecies(
            id = "air_griffin",
            defaultName = "Zephyr",
            speciesTitle = "Sunbeam Sky Griffin",
            realm = PetRealm.AIR,
            description = "Majestic winged lion-eagle cub that guards your streak!",
            primaryColorHex = 0xFFFFCA3A,
            secondaryColorHex = 0xFF8AC926,
            bellyColorHex = 0xFFFFF9DB
        ),
        // ENCHANTMENT REALM
        PetSpecies(
            id = "magic_dragon",
            defaultName = "Sparky",
            speciesTitle = "Crystal Gem Dragon",
            realm = PetRealm.ENCHANTMENT,
            description = "Puffs friendly golden starlight and guards the treasure chest!",
            primaryColorHex = 0xFF00C49A,
            secondaryColorHex = 0xFF7209B7,
            bellyColorHex = 0xFFFFD13B
        ),
        PetSpecies(
            id = "magic_unicorn",
            defaultName = "Stardust",
            speciesTitle = "Rainbow Horn Unicorn",
            realm = PetRealm.ENCHANTMENT,
            description = "Gallops across starlight bridges with a glowing spiral horn!",
            primaryColorHex = 0xFFC77DFF,
            secondaryColorHex = 0xFFFF6B8B,
            bellyColorHex = 0xFFF8EDEB
        ),
        // SPACE REALM
        PetSpecies(
            id = "space_kitty",
            defaultName = "Cosmo",
            speciesTitle = "Nebula Astro-Kitty",
            realm = PetRealm.SPACE,
            description = "Zero-gravity space kitten with a starlight helmet & purr power!",
            primaryColorHex = 0xFF7209B7,
            secondaryColorHex = 0xFF4CC9F0,
            bellyColorHex = 0xFFF3EAFF
        ),
        PetSpecies(
            id = "space_hopper",
            defaultName = "Nova",
            speciesTitle = "Saturn Star Hopper",
            realm = PetRealm.SPACE,
            description = "Bouncy cosmic bunny with twin star antennae and Saturn rings!",
            primaryColorHex = 0xFF3A86FF,
            secondaryColorHex = 0xFFFFBE0B,
            bellyColorHex = 0xFFE2EAFC
        )
    )

    fun getById(id: String): PetSpecies =
        allPets.firstOrNull { it.id == id } ?: allPets.first { it.id == "magic_dragon" }
}

@IgnoreExtraProperties
data class KidProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val kidName: String = "Star Explorer",
    val avatarId: String = "star_captain",
    val totalStars: Long = 0L,
    val level: Long = 1L,
    val currentStreak: Long = 0L,
    val longestStreak: Long = 0L,
    val lastCompletedDate: String = "",
    val completedDates: List<String> = emptyList(),
    val morningPerfectDates: List<String> = emptyList(),
    val weekendPerfectDates: List<String> = emptyList(),
    val unlockedBadges: List<String> = emptyList(),
    val claimedRewards: List<String> = emptyList(),
    val petType: String = "magic_dragon",
    val petName: String = "Sparky",
    val petLevel: Long = 1L,
    val petXp: Long = 0L,
    val petHappiness: Long = 85L,
    val petTreats: Long = 3L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class QuestTask(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val routine: String = RoutineType.MORNING.key,
    val iconName: String = "toothbrush",
    val starReward: Long = 5L,
    val completedToday: Boolean = false,
    val lastCompletedDate: String = "",
    val completionCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun isCompletedOn(dateString: String): Boolean {
        return completedToday && lastCompletedDate == dateString
    }
}

@IgnoreExtraProperties
data class VisualReward(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val iconName: String = "ice_cream",
    val starCost: Long = 20L,
    val timesClaimed: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

enum class RedemptionStatus(val key: String, val label: String) {
    PENDING("PENDING", "Waiting for Parent"),
    APPROVED("APPROVED", "Approved by Parent"),
    REJECTED("REJECTED", "Rejected & Stars Refunded");

    companion object {
        fun fromKey(key: String): RedemptionStatus =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: PENDING
    }
}

@IgnoreExtraProperties
data class GiftRedemptionRequest(
    @DocumentId val id: String = "",
    val userId: String = "",
    val rewardId: String = "",
    val rewardTitle: String = "",
    val iconName: String = "gift",
    val starCost: Long = 20L,
    val kidName: String = "Star Explorer",
    val status: String = RedemptionStatus.PENDING.key,
    val parentNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val requirementHint: String,
    val category: String,
    val accentHex: Long,
    val targetProgress: Int
)

data class AvatarOption(
    val id: String,
    val name: String,
    val title: String,
    val primaryHex: Long,
    val secondaryHex: Long
)

object AvatarCatalog {
    val avatars = listOf(
        AvatarOption("star_captain", "Nova", "Star Captain", 0xFFFFB800, 0xFF2D46B9),
        AvatarOption("dino_ranger", "Rexy", "Dino Ranger", 0xFF00C49A, 0xFF0F5257),
        AvatarOption("space_bunny", "Luna", "Cosmic Bunny", 0xFFFF6B8B, 0xFF4A154B),
        AvatarOption("robo_buddy", "Bolt", "Turbo Bot", 0xFF00B4D8, 0xFF03045E),
        AvatarOption("wizard_cat", "Mochi", "Magic Kitty", 0xFF9B5DE5, 0xFF240046),
        AvatarOption("sun_lion", "Leo", "Safari Hero", 0xFFFF7B00, 0xFF5A1807)
    )

    fun getById(id: String): AvatarOption =
        avatars.firstOrNull { it.id == id } ?: avatars.first()
}

object BadgeCatalog {
    val allBadges = listOf(
        AchievementBadge(
            id = "first_quest",
            title = "First Quest Hero",
            description = "Completed your very first daily quest!",
            requirementHint = "Complete 1 daily task",
            category = "MILESTONE",
            accentHex = 0xFF00C49A,
            targetProgress = 1
        ),
        AchievementBadge(
            id = "streak_3",
            title = "3-Day Spark",
            description = "Completed quests for 3 days in a row!",
            requirementHint = "Reach a 3-day streak",
            category = "STREAK",
            accentHex = 0xFFFF8C42,
            targetProgress = 3
        ),
        AchievementBadge(
            id = "streak_5",
            title = "5-Day Streak",
            description = "Unstoppable! 5 consecutive days of task completion!",
            requirementHint = "Reach a 5-day streak",
            category = "STREAK",
            accentHex = 0xFFFF5964,
            targetProgress = 5
        ),
        AchievementBadge(
            id = "streak_7",
            title = "7-Day Supernova",
            description = "A full week of daily task power!",
            requirementHint = "Reach a 7-day streak",
            category = "STREAK",
            accentHex = 0xFF9B5DE5,
            targetProgress = 7
        ),
        AchievementBadge(
            id = "morning_master",
            title = "Morning Routine Master",
            description = "Completed all morning tasks for a full 7-day week!",
            requirementHint = "Complete all Morning tasks on 7 days",
            category = "ROUTINE",
            accentHex = 0xFFFFB800,
            targetProgress = 7
        ),
        AchievementBadge(
            id = "weekend_warrior",
            title = "Weekend Warrior",
            description = "Completed all weekend quests on a weekend!",
            requirementHint = "Complete all Weekend tasks on Sat/Sun",
            category = "ROUTINE",
            accentHex = 0xFF00B4D8,
            targetProgress = 1
        ),
        AchievementBadge(
            id = "all_daily_hero",
            title = "Daily Grand Champion",
            description = "Checked off every single routine task in one day!",
            requirementHint = "Finish 100% of today's tasks",
            category = "MILESTONE",
            accentHex = 0xFFFF6B8B,
            targetProgress = 1
        ),
        AchievementBadge(
            id = "star_collector_50",
            title = "50-Star Collector",
            description = "Collected 50 golden stars in your treasure bank!",
            requirementHint = "Reach 50 total stars",
            category = "STARS",
            accentHex = 0xFFFFCA3A,
            targetProgress = 50
        ),
        AchievementBadge(
            id = "reward_redeemer",
            title = "Treasure Hunter",
            description = "Unlocked and claimed a special parent gift reward!",
            requirementHint = "Claim 1 gift from the Treasure Chest",
            category = "REWARD",
            accentHex = 0xFF8AC926,
            targetProgress = 1
        )
    )

    fun progressForBadge(
        badge: AchievementBadge,
        profile: KidProfile,
        tasks: List<QuestTask>,
        todayDate: String
    ): Pair<Int, Int> {
        val unlocked = profile.unlockedBadges.contains(badge.id)
        if (unlocked) return badge.targetProgress to badge.targetProgress
        val current = when (badge.id) {
            "first_quest" -> if (profile.completedDates.isNotEmpty()) 1 else 0
            "streak_3" -> profile.longestStreak.toInt().coerceAtMost(3)
            "streak_5" -> profile.longestStreak.toInt().coerceAtMost(5)
            "streak_7" -> profile.longestStreak.toInt().coerceAtMost(7)
            "morning_master" -> profile.morningPerfectDates.size.coerceAtMost(7)
            "weekend_warrior" -> profile.weekendPerfectDates.size.coerceAtMost(1)
            "all_daily_hero" -> {
                val doneCount = tasks.count { it.isCompletedOn(todayDate) }
                if (tasks.isNotEmpty() && doneCount == tasks.size) 1 else 0
            }
            "star_collector_50" -> profile.totalStars.toInt().coerceAtMost(50)
            "reward_redeemer" -> profile.claimedRewards.size.coerceAtMost(1)
            else -> 0
        }
        return current to badge.targetProgress
    }
}

object QuestDateUtils {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun todayIsoString(): String = LocalDate.now().format(formatter)

    fun isWeekend(dateIso: String): Boolean {
        return try {
            val d = LocalDate.parse(dateIso, formatter)
            d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY
        } catch (e: Exception) {
            false
        }
    }

    fun calculateConsecutiveStreak(completedDates: List<String>, referenceDateIso: String): Int {
        if (completedDates.isEmpty()) return 0
        val distinctSorted = completedDates.distinct().sorted()
        val parsedSet = distinctSorted.mapNotNull {
            try {
                LocalDate.parse(it, formatter)
            } catch (e: Exception) {
                null
            }
        }.toSet()
        if (parsedSet.isEmpty()) return 0

        val refDate = try {
            LocalDate.parse(referenceDateIso, formatter)
        } catch (e: Exception) {
            parsedSet.maxOrNull() ?: return 0
        }

        var cursor = when {
            parsedSet.contains(refDate) -> refDate
            parsedSet.contains(refDate.minusDays(1)) -> refDate.minusDays(1)
            else -> return 0
        }

        var streak = 0
        while (parsedSet.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    fun lastSevenDays(referenceDateIso: String): List<Pair<String, String>> {
        val refDate = try {
            LocalDate.parse(referenceDateIso, formatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
        return (6 downTo 0).map { offset ->
            val date = refDate.minusDays(offset.toLong())
            val iso = date.format(formatter)
            val shortDay = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
            iso to shortDay
        }
    }
}
