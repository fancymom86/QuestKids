package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [36])
class QuestRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createTask_validPayload_createsDocumentAndReturnsId() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = QuestRepository(firestore)

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask("Brush Teeth", "MORNING", "toothbrush", 5L)
        }
        assertTrue(createResult.isSuccess)
        val taskId = createResult.getOrThrow()
        assertTrue(taskId.isNotEmpty())

        val tasksResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserTasks(uid)
        }
        assertTrue(tasksResult.isSuccess)
        assertTrue(tasksResult.getOrThrow().any { it.id == taskId })
    }

    @Test
    fun observeTasks_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = QuestRepository(firestore)
        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask("Make My Bed", "MORNING", "bed", 5L).getOrThrow()
        }

        val emittedTasks = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTasks(uid).first { list -> list.any { it.id == taskId } }
        }
        assertTrue(emittedTasks.any { it.id == taskId })
    }

    @Test
    fun getTaskById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = QuestRepository(firestore)
        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createTask("Alice Secret Task", "EVENING", "star", 10L).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = QuestRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getTaskById(taskId, targetUserId = aliceUid)
        }
        assertTrue(result.isFailure)
        val firestoreEx = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeTasks_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = QuestRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTasks(aliceUid).first()
            }
            fail("Expected exception when observing tasks unauthenticated")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(
                FirebaseFirestoreException.Code.PERMISSION_DENIED,
                firestoreException.code
            )
        }
    }

    @Test
    fun markTaskComplete_updatesTotalStarBalanceInFirestore() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = QuestRepository(firestore)
        val today = "2026-11-04"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureStarterData(uid, "Leo", today).getOrThrow()
        }

        val initialProfile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()
        }
        assertNotNull(initialProfile)
        val startingStars = initialProfile!!.totalStars

        val taskId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createTask("Clean Up Lego Bricks", "AFTERNOON", "toys", 15L).getOrThrow()
        }
        val tasks = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserTasks(uid).getOrThrow()
        }
        val createdTask = tasks.first { it.id == taskId }

        val newBalanceResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.markTaskCompleteAndAddStars(
                task = createdTask,
                allTasks = tasks,
                currentProfile = initialProfile,
                todayDate = today
            )
        }
        assertTrue(newBalanceResult.isSuccess)
        assertEquals(startingStars + 15L, newBalanceResult.getOrThrow())

        val updatedProfile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()
        }
        assertEquals(startingStars + 15L, updatedProfile?.totalStars)
    }

    @Test
    fun petGrowthAndMultiRealmSelection_persistsInFirestore() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = QuestRepository(firestore)
        val today = "2026-11-04"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureStarterData(uid, "Maya", today).getOrThrow()
        }

        // Switch pet to Space Realm Nebula Astro-Kitty
        val switchRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateSelectedPet("space_kitty", "Cosmo")
        }
        assertTrue(switchRes.isSuccess)

        val profileAfterSwitch = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()
        }
        assertEquals("space_kitty", profileAfterSwitch?.petType)
        assertEquals("Cosmo", profileAfterSwitch?.petName)

        // Sync pet growth milestone to Stage 2 (Sprout)
        val milestoneRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncPetGrowthMilestoneInFirestore(
                currentProfile = profileAfterSwitch!!.copy(totalStars = 20L),
                newPetLevel = 2L,
                milestoneBonusStars = 5L
            )
        }
        assertTrue(milestoneRes.isSuccess)
        assertEquals(25L, milestoneRes.getOrThrow())

        val evolvedProfile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()
        }
        assertEquals(2L, evolvedProfile?.petLevel)
        assertEquals(25L, evolvedProfile?.totalStars)
    }

    @Test
    fun parentalDashboard_approveAndRejectRedemptions_updatesStarsAndStatus() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = QuestRepository(firestore)
        val today = "2026-11-04"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureStarterData(uid, "Leo", today).getOrThrow()
        }

        // Give Leo 40 stars
        val baseProfile = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()!!
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncPetGrowthMilestoneInFirestore(
                currentProfile = baseProfile.copy(totalStars = 35L),
                newPetLevel = 5L,
                milestoneBonusStars = 5L
            ).getOrThrow()
        }

        val profileWith40Stars = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()!!
        }
        assertEquals(40L, profileWith40Stars.totalStars)

        // Create a 15-star gift and claim it -> creates a PENDING redemption request and deducts 15 stars
        val rewardId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createReward("Lego Spaceship Pack", "toys", 15L).getOrThrow()
        }
        val rewards = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeRewards(uid).first { list -> list.any { it.id == rewardId } }
        }
        val targetReward = rewards.first { it.id == rewardId }

        val claimRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.claimReward(targetReward, profileWith40Stars)
        }
        assertTrue(claimRes.isSuccess)

        val profileAfterClaim = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()!!
        }
        assertEquals(25L, profileAfterClaim.totalStars)

        val redemptionsAfterClaim = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getRedemptions(uid).getOrThrow()
        }
        val pendingRequest = redemptionsAfterClaim.first { it.rewardId == rewardId && it.status == "PENDING" }

        // Parent rejects the request -> status becomes REJECTED and 15 stars are refunded back to 40
        val rejectRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.rejectRedemptionRequest(pendingRequest, profileAfterClaim, "Save stars for Saturday!")
        }
        assertTrue(rejectRes.isSuccess)
        assertEquals(40L, rejectRes.getOrThrow())

        // Claim again and Parent approves -> status becomes APPROVED and reward_redeemer badge unlocks
        val profileAfterRefund = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()!!
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.claimReward(targetReward, profileAfterRefund).getOrThrow()
        }
        val profileAfterSecondClaim = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProfile(uid).getOrThrow()!!
        }
        assertEquals(25L, profileAfterSecondClaim.totalStars)

        val secondPending = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getRedemptions(uid).getOrThrow()
        }.first { it.rewardId == rewardId && it.status == "PENDING" }

        val approveRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.approveRedemptionRequest(secondPending, profileAfterSecondClaim, "Approved by Mom!")
        }
        assertTrue(approveRes.isSuccess)

        val finalRedemptions = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getRedemptions(uid).getOrThrow()
        }
        assertTrue(finalRedemptions.any { it.id == secondPending.id && it.status == "APPROVED" })
    }

    private companion object {
        const val ALICE_EMAIL = "alice@questkids.test"
        const val BOB_EMAIL = "bob@questkids.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
