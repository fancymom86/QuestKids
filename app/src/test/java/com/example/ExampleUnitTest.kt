package com.example

import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.PetMiniGameType
import com.example.data.model.PetRealm
import com.example.data.model.QuestDateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun petGrowthStage_supports205LevelsAnd11EvolutionTiers() {
        assertEquals(205L, PetGrowthStage.MAX_PET_LEVEL)
        assertEquals(PetGrowthStage.HATCHLING, PetGrowthStage.fromStarBalance(0L))
        assertEquals(PetGrowthStage.SPROUT, PetGrowthStage.fromStarBalance(20L))
        assertEquals(PetGrowthStage.BUDDY, PetGrowthStage.fromStarBalance(45L))
        assertEquals(PetGrowthStage.CHAMPION, PetGrowthStage.fromStarBalance(80L))
        assertEquals(PetGrowthStage.ROYAL_LEGEND, PetGrowthStage.fromStarBalance(125L))
        assertEquals(PetGrowthStage.CRYSTAL_GUARDIAN, PetGrowthStage.fromLevel(75L))
        assertEquals(PetGrowthStage.SOLAR_KNIGHT, PetGrowthStage.fromLevel(100L))
        assertEquals(PetGrowthStage.GALACTIC_SOVEREIGN, PetGrowthStage.fromLevel(130L))
        assertEquals(PetGrowthStage.ASTRAL_MYTHIC, PetGrowthStage.fromLevel(160L))
        assertEquals(PetGrowthStage.CELESTIAL_ASCENDANT, PetGrowthStage.fromLevel(190L))
        assertEquals(PetGrowthStage.OMEGA_OMNIVERSE, PetGrowthStage.fromLevel(205L))
    }

    @Test
    fun petMiniGames_catalogContainsAllThreePlayableGames() {
        assertEquals(3, PetMiniGameType.entries.size)
        assertTrue(PetMiniGameType.entries.all { it.maxStarReward >= 15L && it.xpReward >= 45L })
    }

    @Test
    fun petCatalog_containsPetsForAllFiveRealms() {
        val realmsInCatalog = PetCatalog.allPets.map { it.realm }.toSet()
        assertTrue(realmsInCatalog.contains(PetRealm.SEA))
        assertTrue(realmsInCatalog.contains(PetRealm.LAND))
        assertTrue(realmsInCatalog.contains(PetRealm.AIR))
        assertTrue(realmsInCatalog.contains(PetRealm.ENCHANTMENT))
        assertTrue(realmsInCatalog.contains(PetRealm.SPACE))
        assertEquals(10, PetCatalog.allPets.size)
    }

    @Test
    fun streakCalculation_countsConsecutiveDays() {
        val dates = listOf("2026-11-01", "2026-11-02", "2026-11-03", "2026-11-04")
        assertEquals(4, QuestDateUtils.calculateConsecutiveStreak(dates, "2026-11-04"))
    }
}
