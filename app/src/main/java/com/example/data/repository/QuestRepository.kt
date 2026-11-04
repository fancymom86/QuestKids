package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.data.model.GiftRedemptionRequest
import com.example.data.model.KidProfile
import com.example.data.model.PetCareAction
import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.QuestDateUtils
import com.example.data.model.QuestTask
import com.example.data.model.RedemptionStatus
import com.example.data.model.RoutineType
import com.example.data.model.VisualReward
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class QuestRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeProfile(userId: String = requireUserId()): Flow<KidProfile?> = flow {
        val path = "users/$userId"
        emitAll(
            db.collection("users").document(userId)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            KidProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    fun observeTasks(userId: String = requireUserId()): Flow<List<QuestTask>> = flow {
        val path = "users/$userId/tasks"
        emitAll(
            db.collection("users").document(userId).collection("tasks")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        QuestTask::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeRewards(userId: String = requireUserId()): Flow<List<VisualReward>> = flow {
        val path = "users/$userId/rewards"
        emitAll(
            db.collection("users").document(userId).collection("rewards")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        VisualReward::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.starCost }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeRedemptions(userId: String = requireUserId()): Flow<List<GiftRedemptionRequest>> = flow {
        val path = "users/$userId/redemptions"
        emitAll(
            db.collection("users").document(userId).collection("redemptions")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        GiftRedemptionRequest::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.createdAt?.seconds ?: 0L }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun getRedemptions(userId: String = requireUserId()): Result<List<GiftRedemptionRequest>> {
        val path = "users/$userId/redemptions"
        return try {
            val snapshot = db.collection("users").document(userId).collection("redemptions").get().await()
            val list = snapshot.toObjects(
                GiftRedemptionRequest::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun createTask(
        title: String,
        routine: String = RoutineType.MORNING.key,
        iconName: String = "toothbrush",
        starReward: Long = 5L
    ): Result<String> {
        val uid = requireUserId()
        val tasksRef = db.collection("users").document(uid).collection("tasks")
        val docRef = tasksRef.document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(80),
            "routine" to routine,
            "iconName" to iconName,
            "starReward" to starReward.coerceIn(1L, 50L),
            "completedToday" to false,
            "lastCompletedDate" to "",
            "completionCount" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserTasks(userId: String = requireUserId()): Result<List<QuestTask>> {
        val path = "users/$userId/tasks"
        return try {
            val snapshot = db.collection("users").document(userId).collection("tasks").get().await()
            val list = snapshot.toObjects(
                QuestTask::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun getProfile(userId: String = requireUserId()): Result<KidProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val profile = snapshot.toObject(
                KidProfile::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markTaskCompleteAndAddStars(
        task: QuestTask,
        allTasks: List<QuestTask>,
        currentProfile: KidProfile,
        todayDate: String
    ): Result<Long> {
        val toggleRes = toggleTaskCompletion(task, allTasks, currentProfile, todayDate)
        return toggleRes.map {
            val wasDone = task.isCompletedOn(todayDate)
            val delta = if (!wasDone) task.starReward else -task.starReward
            (currentProfile.totalStars + delta).coerceAtLeast(0L)
        }
    }

    suspend fun getTaskById(taskId: String, targetUserId: String = requireUserId()): Result<QuestTask?> {
        val docRef = db.collection("users").document(targetUserId).collection("tasks").document(taskId)
        return try {
            val snapshot = docRef.get().await()
            val task = snapshot.toObject(
                QuestTask::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            )
            Result.success(task)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createReward(
        title: String,
        iconName: String = "gift",
        starCost: Long = 20L
    ): Result<String> {
        val uid = requireUserId()
        val rewardsRef = db.collection("users").document(uid).collection("rewards")
        val docRef = rewardsRef.document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title.trim().take(80),
            "iconName" to iconName,
            "starCost" to starCost.coerceIn(5L, 500L),
            "timesClaimed" to 0L,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteTask(taskId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tasks").document(taskId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteReward(rewardId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("rewards").document(rewardId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun ensureStarterData(
        userId: String,
        defaultKidName: String,
        todayDate: String
    ): Result<Unit> {
        val profileRef = db.collection("users").document(userId)
        return try {
            val existingSnap = profileRef.get().await()
            if (!existingSnap.exists()) {
                val safeName = defaultKidName.trim().ifBlank { "Star Explorer" }.take(50)
                val initialProfile = mapOf(
                    "userId" to userId,
                    "kidName" to safeName,
                    "avatarId" to "star_captain",
                    "totalStars" to 15L,
                    "level" to 1L,
                    "currentStreak" to 0L,
                    "longestStreak" to 0L,
                    "lastCompletedDate" to "",
                    "completedDates" to emptyList<String>(),
                    "morningPerfectDates" to emptyList<String>(),
                    "weekendPerfectDates" to emptyList<String>(),
                    "unlockedBadges" to emptyList<String>(),
                    "claimedRewards" to emptyList<String>(),
                    "petType" to "magic_dragon",
                    "petName" to "Sparky",
                    "petLevel" to 1L,
                    "petXp" to 15L,
                    "petHappiness" to 90L,
                    "petTreats" to 3L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                profileRef.set(initialProfile).await()

                val starterTasks = listOf(
                    Triple("Brush Teeth & Sparkle Smile", RoutineType.MORNING.key, "toothbrush" to 5L),
                    Triple("Make My Cozy Bed", RoutineType.MORNING.key, "bed" to 5L),
                    Triple("Eat a Healthy Breakfast", RoutineType.MORNING.key, "apple" to 5L),
                    Triple("Pack Backpack & Water Bottle", RoutineType.AFTERNOON.key, "backpack" to 10L),
                    Triple("Read 15 Minutes of a Fun Book", RoutineType.AFTERNOON.key, "book" to 10L),
                    Triple("Put Toys Back in Treasure Bin", RoutineType.EVENING.key, "toys" to 10L),
                    Triple("Put on Cozy Pajamas", RoutineType.EVENING.key, "pajamas" to 5L),
                    Triple("Family Board Game or Park Adventure", RoutineType.WEEKEND.key, "trophy" to 15L),
                    Triple("Help Water the Plants or Feed Pet", RoutineType.WEEKEND.key, "pet" to 15L)
                )

                val tasksCollection = profileRef.collection("tasks")
                for ((title, routine, iconAndStars) in starterTasks) {
                    val doc = tasksCollection.document()
                    doc.set(
                        mapOf(
                            "userId" to userId,
                            "title" to title,
                            "routine" to routine,
                            "iconName" to iconAndStars.first,
                            "starReward" to iconAndStars.second,
                            "completedToday" to false,
                            "lastCompletedDate" to "",
                            "completionCount" to 0L,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }

                val starterRewards = listOf(
                    Triple("Pick Tonight's Bedtime Story", "book", 15L),
                    Triple("30 Mins Extra Lego or Craft Time", "toys", 25L),
                    Triple("Choose Family Movie Night Pick", "movie", 40L),
                    Triple("Special Ice Cream Sundae Trip", "ice_cream", 60L),
                    Triple("Stay Up 20 Minutes Past Bedtime", "star", 75L)
                )
                val rewardsCollection = profileRef.collection("rewards")
                var firstRewardId = ""
                for ((index, triple) in starterRewards.withIndex()) {
                    val (title, icon, cost) = triple
                    val doc = rewardsCollection.document()
                    if (index == 0) firstRewardId = doc.id
                    doc.set(
                        mapOf(
                            "userId" to userId,
                            "title" to title,
                            "iconName" to icon,
                            "starCost" to cost,
                            "timesClaimed" to 0L,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }

                // Seed 1 starter PENDING gift redemption request for the Parental Dashboard
                if (firstRewardId.isNotBlank()) {
                    val redemptionDoc = profileRef.collection("redemptions").document()
                    redemptionDoc.set(
                        mapOf(
                            "userId" to userId,
                            "rewardId" to firstRewardId,
                            "rewardTitle" to "Pick Tonight's Bedtime Story",
                            "iconName" to "book",
                            "starCost" to 15L,
                            "kidName" to safeName,
                            "status" to RedemptionStatus.PENDING.key,
                            "parentNote" to "",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleTaskCompletion(
        task: QuestTask,
        allTasks: List<QuestTask>,
        currentProfile: KidProfile,
        todayDate: String
    ): Result<List<String>> {
        val uid = requireUserId()
        require(task.id.isNotBlank()) { "Task ID must not be blank" }
        val taskRef = db.collection("users").document(uid).collection("tasks").document(task.id)
        val profileRef = db.collection("users").document(uid)

        val wasCompletedToday = task.isCompletedOn(todayDate)
        val nowCompleted = !wasCompletedToday
        val newLastCompletedDate = if (nowCompleted) todayDate else ""
        val newCompletionCount = if (nowCompleted) {
            task.completionCount + 1L
        } else {
            (task.completionCount - 1L).coerceAtLeast(0L)
        }

        val updatedTasks = allTasks.map {
            if (it.id == task.id) {
                it.copy(
                    completedToday = nowCompleted,
                    lastCompletedDate = newLastCompletedDate,
                    completionCount = newCompletionCount
                )
            } else {
                it
            }
        }

        val anyCompletedToday = updatedTasks.any { it.isCompletedOn(todayDate) }
        val updatedCompletedDates = if (anyCompletedToday) {
            (currentProfile.completedDates + todayDate).distinct().takeLast(90)
        } else {
            currentProfile.completedDates.filterNot { it == todayDate }
        }

        val morningTasks = updatedTasks.filter { it.routine == RoutineType.MORNING.key }
        val allMorningDoneToday = morningTasks.isNotEmpty() && morningTasks.all { it.isCompletedOn(todayDate) }
        val updatedMorningPerfectDates = if (allMorningDoneToday) {
            (currentProfile.morningPerfectDates + todayDate).distinct().takeLast(90)
        } else {
            currentProfile.morningPerfectDates.filterNot { it == todayDate }
        }

        val weekendTasks = updatedTasks.filter { it.routine == RoutineType.WEEKEND.key }
        val allWeekendDoneToday = weekendTasks.isNotEmpty() &&
            weekendTasks.all { it.isCompletedOn(todayDate) } &&
            (QuestDateUtils.isWeekend(todayDate) || weekendTasks.all { it.isCompletedOn(todayDate) })
        val updatedWeekendPerfectDates = if (allWeekendDoneToday) {
            (currentProfile.weekendPerfectDates + todayDate).distinct().takeLast(90)
        } else {
            currentProfile.weekendPerfectDates.filterNot { it == todayDate }
        }

        val newStreak = QuestDateUtils.calculateConsecutiveStreak(updatedCompletedDates, todayDate).toLong()
        val newLongestStreak = maxOf(currentProfile.longestStreak, newStreak)

        val starDelta = if (nowCompleted) task.starReward else -task.starReward
        val newTotalStars = (currentProfile.totalStars + starDelta).coerceAtLeast(0L)
        val newLevel = (1L + (newTotalStars / 25L)).coerceIn(1L, 1000L)

        // Update pet stats based on task completion & new totalStars balance (up to Level 205!)
        val calculatedPetLevel = PetGrowthStage.levelFromStarBalance(newTotalStars)
        val newPetLevel = maxOf(currentProfile.petLevel, calculatedPetLevel).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)
        val newPetXp = if (nowCompleted) {
            (currentProfile.petXp + task.starReward * 3L).coerceIn(0L, 1000000L)
        } else {
            (currentProfile.petXp - task.starReward * 3L).coerceIn(0L, 1000000L)
        }
        val newPetTreats = if (nowCompleted) {
            (currentProfile.petTreats + 1L).coerceIn(0L, 100000L)
        } else {
            (currentProfile.petTreats - 1L).coerceIn(0L, 100000L)
        }
        val newPetHappiness = if (nowCompleted) {
            (currentProfile.petHappiness + 8L).coerceIn(0L, 100L)
        } else {
            currentProfile.petHappiness.coerceIn(0L, 100L)
        }

        val candidateBadges = currentProfile.unlockedBadges.toMutableSet()
        if (updatedCompletedDates.isNotEmpty()) candidateBadges.add("first_quest")
        if (newLongestStreak >= 3L) candidateBadges.add("streak_3")
        if (newLongestStreak >= 5L) candidateBadges.add("streak_5")
        if (newLongestStreak >= 7L) candidateBadges.add("streak_7")
        if (updatedMorningPerfectDates.size >= 7) candidateBadges.add("morning_master")
        if (updatedWeekendPerfectDates.isNotEmpty()) candidateBadges.add("weekend_warrior")
        if (updatedTasks.isNotEmpty() && updatedTasks.all { it.isCompletedOn(todayDate) }) {
            candidateBadges.add("all_daily_hero")
        }
        if (newTotalStars >= 50L) candidateBadges.add("star_collector_50")

        val newlyUnlocked = candidateBadges.filterNot { currentProfile.unlockedBadges.contains(it) }
        val finalBadges = candidateBadges.toList().take(50)

        return try {
            val batch = db.batch()
            batch.update(
                taskRef,
                mapOf(
                    "completedToday" to nowCompleted,
                    "lastCompletedDate" to newLastCompletedDate,
                    "completionCount" to newCompletionCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                profileRef,
                mapOf(
                    "totalStars" to newTotalStars,
                    "level" to newLevel,
                    "currentStreak" to newStreak,
                    "longestStreak" to newLongestStreak,
                    "lastCompletedDate" to (updatedCompletedDates.lastOrNull() ?: ""),
                    "completedDates" to updatedCompletedDates,
                    "morningPerfectDates" to updatedMorningPerfectDates,
                    "weekendPerfectDates" to updatedWeekendPerfectDates,
                    "unlockedBadges" to finalBadges,
                    "petLevel" to newPetLevel,
                    "petXp" to newPetXp,
                    "petHappiness" to newPetHappiness,
                    "petTreats" to newPetTreats,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(newlyUnlocked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun syncPetGrowthMilestoneInFirestore(
        currentProfile: KidProfile,
        newPetLevel: Long,
        milestoneBonusStars: Long
    ): Result<Long> {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val updatedStars = (currentProfile.totalStars + milestoneBonusStars).coerceIn(0L, 1000000L)
        val updatedLevel = (1L + (updatedStars / 25L)).coerceIn(1L, 1000L)
        return try {
            profileRef.update(
                mapOf(
                    "totalStars" to updatedStars,
                    "level" to updatedLevel,
                    "petLevel" to newPetLevel.coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL),
                    "petHappiness" to 100L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(updatedStars)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun careForPetInFirestore(
        currentProfile: KidProfile,
        action: PetCareAction
    ): Result<Long> {
        val uid = requireUserId()
        if (currentProfile.petTreats < action.treatCost) {
            return Result.failure(IllegalArgumentException("Complete a daily quest or play a mini-game to earn more Pet Treats!"))
        }
        val profileRef = db.collection("users").document(uid)
        val newTreats = (currentProfile.petTreats - action.treatCost).coerceAtLeast(0L)
        val newHappiness = (currentProfile.petHappiness + action.happinessBoost).coerceIn(0L, 100L)
        val newXp = (currentProfile.petXp + action.xpGain).coerceIn(0L, 1000000L)
        val newStars = (currentProfile.totalStars + action.bonusStars).coerceIn(0L, 1000000L)
        val calculatedPetLevel = PetGrowthStage.levelFromStarBalance(newStars)
        val newPetLevel = maxOf(currentProfile.petLevel, calculatedPetLevel).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)
        val newExplorerLevel = (1L + (newStars / 25L)).coerceIn(1L, 1000L)

        return try {
            profileRef.update(
                mapOf(
                    "totalStars" to newStars,
                    "level" to newExplorerLevel,
                    "petTreats" to newTreats,
                    "petHappiness" to newHappiness,
                    "petXp" to newXp,
                    "petLevel" to newPetLevel,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(newStars)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun completePetMiniGameInFirestore(
        currentProfile: KidProfile,
        starsEarned: Long,
        xpEarned: Long,
        treatsEarned: Long
    ): Result<Long> {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val newStars = (currentProfile.totalStars + starsEarned.coerceAtLeast(1L)).coerceIn(0L, 1000000L)
        val newXp = (currentProfile.petXp + xpEarned.coerceAtLeast(5L)).coerceIn(0L, 1000000L)
        val newTreats = (currentProfile.petTreats + treatsEarned.coerceAtLeast(0L)).coerceIn(0L, 100000L)
        val newHappiness = (currentProfile.petHappiness + 15L).coerceIn(0L, 100L)
        // Advance at least +1 pet level on mini-game victory or match star balance level up to 205
        val starBasedLevel = PetGrowthStage.levelFromStarBalance(newStars)
        val newPetLevel = maxOf(currentProfile.petLevel + 1L, starBasedLevel)
            .coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)
        val newExplorerLevel = (1L + (newStars / 25L)).coerceIn(1L, 1000L)

        val candidateBadges = currentProfile.unlockedBadges.toMutableSet()
        if (newStars >= 50L) candidateBadges.add("star_collector_50")

        return try {
            profileRef.update(
                mapOf(
                    "totalStars" to newStars,
                    "level" to newExplorerLevel,
                    "petXp" to newXp,
                    "petTreats" to newTreats,
                    "petHappiness" to newHappiness,
                    "petLevel" to newPetLevel,
                    "unlockedBadges" to candidateBadges.toList().take(50),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(newStars)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateSelectedPet(
        petType: String,
        petName: String
    ): Result<Unit> {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val species = PetCatalog.getById(petType)
        val safeName = petName.trim().ifBlank { species.defaultName }.take(40)
        return try {
            profileRef.update(
                mapOf(
                    "petType" to species.id,
                    "petName" to safeName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }

    /**
     * Submits a child's gift redemption request using their star balance, creating a PENDING
     * GiftRedemptionRequest document in Firestore for parent approval/rejection.
     */
    suspend fun claimReward(
        reward: VisualReward,
        currentProfile: KidProfile
    ): Result<String> {
        val uid = requireUserId()
        require(reward.id.isNotBlank()) { "Reward ID must not be blank" }
        if (currentProfile.totalStars < reward.starCost) {
            return Result.failure(IllegalArgumentException("Need ${reward.starCost - currentProfile.totalStars} more stars!"))
        }

        val profileRef = db.collection("users").document(uid)
        val redemptionRef = profileRef.collection("redemptions").document()
        val newStars = (currentProfile.totalStars - reward.starCost).coerceAtLeast(0L)
        val safeKidName = currentProfile.kidName.trim().ifBlank { "Star Explorer" }.take(50)

        return try {
            val batch = db.batch()
            batch.set(
                redemptionRef,
                mapOf(
                    "userId" to uid,
                    "rewardId" to reward.id,
                    "rewardTitle" to reward.title.trim().take(80),
                    "iconName" to reward.iconName.trim().ifBlank { "gift" }.take(30),
                    "starCost" to reward.starCost.coerceIn(5L, 500L),
                    "kidName" to safeKidName,
                    "status" to RedemptionStatus.PENDING.key,
                    "parentNote" to "",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                profileRef,
                mapOf(
                    "totalStars" to newStars,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(redemptionRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, redemptionRef.path)
            Result.failure(e)
        }
    }

    /**
     * Parental Dashboard action: Approves a child's pending gift redemption request,
     * increments the reward's timesClaimed counter, records the claimed reward in KidProfile,
     * and unlocks the Treasure Hunter badge.
     */
    suspend fun approveRedemptionRequest(
        request: GiftRedemptionRequest,
        currentProfile: KidProfile,
        parentNote: String = "Approved by Parent! Great job!"
    ): Result<List<String>> {
        val uid = requireUserId()
        require(request.id.isNotBlank()) { "Redemption ID must not be blank" }

        val profileRef = db.collection("users").document(uid)
        val redemptionRef = profileRef.collection("redemptions").document(request.id)
        val rewardRef = profileRef.collection("rewards").document(request.rewardId)

        val updatedClaimed = (currentProfile.claimedRewards + request.rewardId).takeLast(50)
        val candidateBadges = currentProfile.unlockedBadges.toMutableSet()
        candidateBadges.add("reward_redeemer")
        val newlyUnlocked = candidateBadges.filterNot { currentProfile.unlockedBadges.contains(it) }
        val safeNote = parentNote.trim().ifBlank { "Approved by Parent!" }.take(160)

        return try {
            val rewardSnap = rewardRef.get().await()
            val batch = db.batch()
            batch.update(
                redemptionRef,
                mapOf(
                    "status" to RedemptionStatus.APPROVED.key,
                    "parentNote" to safeNote,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (rewardSnap.exists()) {
                val currentClaimedCount = rewardSnap.getLong("timesClaimed") ?: 0L
                batch.update(
                    rewardRef,
                    mapOf(
                        "timesClaimed" to (currentClaimedCount + 1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.update(
                profileRef,
                mapOf(
                    "claimedRewards" to updatedClaimed,
                    "unlockedBadges" to candidateBadges.toList().take(50),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(newlyUnlocked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, redemptionRef.path)
            Result.failure(e)
        }
    }

    /**
     * Parental Dashboard action: Rejects a child's pending gift redemption request
     * and refunds the full starCost back to the child's totalStars balance in Firestore.
     */
    suspend fun rejectRedemptionRequest(
        request: GiftRedemptionRequest,
        currentProfile: KidProfile,
        parentNote: String = "Stars refunded to your bank for later!"
    ): Result<Long> {
        val uid = requireUserId()
        require(request.id.isNotBlank()) { "Redemption ID must not be blank" }

        val profileRef = db.collection("users").document(uid)
        val redemptionRef = profileRef.collection("redemptions").document(request.id)
        val refundedStars = (currentProfile.totalStars + request.starCost).coerceIn(0L, 1000000L)
        val safeNote = parentNote.trim().ifBlank { "Stars refunded to your bank!" }.take(160)

        return try {
            val batch = db.batch()
            batch.update(
                redemptionRef,
                mapOf(
                    "status" to RedemptionStatus.REJECTED.key,
                    "parentNote" to safeNote,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                profileRef,
                mapOf(
                    "totalStars" to refundedStars,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(refundedStars)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, redemptionRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileIdentity(
        kidName: String,
        avatarId: String
    ): Result<Unit> {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val safeName = kidName.trim().ifBlank { "Star Explorer" }.take(50)
        return try {
            profileRef.update(
                mapOf(
                    "kidName" to safeName,
                    "avatarId" to avatarId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }

    suspend fun logBonusStreakDay(
        currentProfile: KidProfile,
        todayDate: String,
        includeMorningMasterDay: Boolean = true
    ): Result<List<String>> {
        val uid = requireUserId()
        val profileRef = db.collection("users").document(uid)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = try {
            LocalDate.parse(todayDate, formatter)
        } catch (e: Exception) {
            LocalDate.now()
        }

        val existingSet = currentProfile.completedDates.toSet()
        var cursor = today
        while (existingSet.contains(cursor.format(formatter))) {
            cursor = cursor.minusDays(1)
        }
        val newDateStr = cursor.format(formatter)
        val updatedCompletedDates = (currentProfile.completedDates + newDateStr).distinct().sorted().takeLast(90)

        val existingMorningSet = currentProfile.morningPerfectDates.toSet()
        var morningCursor = today
        while (existingMorningSet.contains(morningCursor.format(formatter))) {
            morningCursor = morningCursor.minusDays(1)
        }
        val updatedMorningDates = if (includeMorningMasterDay) {
            (currentProfile.morningPerfectDates + morningCursor.format(formatter)).distinct().sorted().takeLast(90)
        } else {
            currentProfile.morningPerfectDates
        }

        val newStreak = QuestDateUtils.calculateConsecutiveStreak(updatedCompletedDates, todayDate).toLong()
        val newLongest = maxOf(currentProfile.longestStreak, newStreak)
        val newStars = currentProfile.totalStars + 10L
        val newLevel = (1L + (newStars / 25L)).coerceIn(1L, 1000L)
        val newPetStage = PetGrowthStage.fromStarBalance(newStars)
        val newPetLevel = maxOf(currentProfile.petLevel, newPetStage.level)

        val candidateBadges = currentProfile.unlockedBadges.toMutableSet()
        if (updatedCompletedDates.isNotEmpty()) candidateBadges.add("first_quest")
        if (newLongest >= 3L) candidateBadges.add("streak_3")
        if (newLongest >= 5L) candidateBadges.add("streak_5")
        if (newLongest >= 7L) candidateBadges.add("streak_7")
        if (updatedMorningDates.size >= 7) candidateBadges.add("morning_master")
        if (newStars >= 50L) candidateBadges.add("star_collector_50")

        val newlyUnlocked = candidateBadges.filterNot { currentProfile.unlockedBadges.contains(it) }

        return try {
            profileRef.update(
                mapOf(
                    "totalStars" to newStars,
                    "level" to newLevel,
                    "currentStreak" to newStreak,
                    "longestStreak" to newLongest,
                    "lastCompletedDate" to todayDate,
                    "completedDates" to updatedCompletedDates,
                    "morningPerfectDates" to updatedMorningDates,
                    "unlockedBadges" to candidateBadges.toList().take(50),
                    "petLevel" to newPetLevel,
                    "petTreats" to (currentProfile.petTreats + 1L).coerceIn(0L, 100000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(newlyUnlocked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, profileRef.path)
            Result.failure(e)
        }
    }
}
