package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AchievementBadge
import com.example.data.model.BadgeCatalog
import com.example.data.model.GiftRedemptionRequest
import com.example.data.model.KidProfile
import com.example.data.model.PetCareAction
import com.example.data.model.PetCatalog
import com.example.data.model.PetGrowthStage
import com.example.data.model.PetMiniGameType
import com.example.data.model.PetSpecies
import com.example.data.model.QuestDateUtils
import com.example.data.model.QuestTask
import com.example.data.model.RoutineType
import com.example.data.model.VisualReward
import com.example.data.repository.QuestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CelebrationBanner(
    val title: String,
    val subtitle: String,
    val starsEarned: Long = 0L,
    val newTotalStars: Long = 0L,
    val unlockedBadge: AchievementBadge? = null
)

data class PetEvolutionMilestoneEvent(
    val petSpecies: PetSpecies,
    val petName: String,
    val previousLevel: Long,
    val newLevel: Long,
    val previousStage: PetGrowthStage,
    val newStage: PetGrowthStage,
    val totalStars: Long,
    val bonusStarsAwarded: Long
)

class QuestViewModel(
    private val repository: QuestRepository,
    private val currentUserId: String,
    defaultDisplayName: String
) : ViewModel() {

    val todayDate: String = QuestDateUtils.todayIsoString()

    private val _selectedRoutine = MutableStateFlow<RoutineType?>(null)
    val selectedRoutine: StateFlow<RoutineType?> = _selectedRoutine.asStateFlow()

    private val _celebration = MutableStateFlow<CelebrationBanner?>(null)
    val celebration: StateFlow<CelebrationBanner?> = _celebration.asStateFlow()

    private val _petEvolutionMilestone = MutableStateFlow<PetEvolutionMilestoneEvent?>(null)
    val petEvolutionMilestone: StateFlow<PetEvolutionMilestoneEvent?> = _petEvolutionMilestone.asStateFlow()

    private val _petVisualPulseTick = MutableStateFlow(0)
    val petVisualPulseTick: StateFlow<Int> = _petVisualPulseTick.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    val profileState: StateFlow<UiState<KidProfile>> = repository.observeProfile(currentUserId)
        .map<KidProfile?, UiState<KidProfile>> { profile ->
            UiState.Success(profile ?: KidProfile(userId = currentUserId))
        }
        .catch { e ->
            Log.w(TAG, "Error observing profile", e)
            emit(UiState.Error(e.localizedMessage ?: "Failed to load adventurer profile"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val tasksState: StateFlow<UiState<List<QuestTask>>> = repository.observeTasks(currentUserId)
        .map<List<QuestTask>, UiState<List<QuestTask>>> { list -> UiState.Success(list) }
        .catch { e ->
            Log.w(TAG, "Error observing tasks", e)
            emit(UiState.Error(e.localizedMessage ?: "Failed to load daily quests"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val rewardsState: StateFlow<UiState<List<VisualReward>>> = repository.observeRewards(currentUserId)
        .map<List<VisualReward>, UiState<List<VisualReward>>> { list -> UiState.Success(list) }
        .catch { e ->
            Log.w(TAG, "Error observing rewards", e)
            emit(UiState.Error(e.localizedMessage ?: "Failed to load treasure chest rewards"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val redemptionsState: StateFlow<UiState<List<GiftRedemptionRequest>>> = repository.observeRedemptions(currentUserId)
        .map<List<GiftRedemptionRequest>, UiState<List<GiftRedemptionRequest>>> { list -> UiState.Success(list) }
        .catch { e ->
            Log.w(TAG, "Error observing redemptions", e)
            emit(UiState.Error(e.localizedMessage ?: "Failed to load gift redemption requests"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    init {
        viewModelScope.launch {
            val firstName = defaultDisplayName.substringBefore(" ").ifBlank { "Star Explorer" }
            repository.ensureStarterData(
                userId = currentUserId,
                defaultKidName = firstName,
                todayDate = todayDate
            )
        }
    }

    fun selectRoutineFilter(routine: RoutineType?) {
        _selectedRoutine.value = routine
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    fun dismissPetEvolutionMilestone() {
        _petEvolutionMilestone.value = null
    }

    fun clearError() {
        _actionError.value = null
    }

    /**
     * ViewModel function that handles the pet's growth levels (across all 205 levels & 11 evolution tiers)
     * based on the total star balance, persisting new growth milestones to Firestore and triggering
     * a visual evolution celebration when a star milestone is reached.
     */
    fun handlePetGrowthFromStarBalance(
        previousStars: Long,
        newTotalStars: Long,
        profile: KidProfile,
        guaranteedMinLevel: Long = profile.petLevel
    ) {
        val prevLevel = maxOf(
            PetGrowthStage.levelFromStarBalance(previousStars),
            profile.petLevel
        ).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)

        val targetLevel = maxOf(
            PetGrowthStage.levelFromStarBalance(newTotalStars),
            guaranteedMinLevel
        ).coerceIn(1L, PetGrowthStage.MAX_PET_LEVEL)

        val previousStage = PetGrowthStage.fromLevel(prevLevel)
        val targetStage = PetGrowthStage.fromLevel(targetLevel)

        // Trigger visual animation pulse on the pet
        _petVisualPulseTick.value += 1

        if (targetLevel > prevLevel) {
            val species = PetCatalog.getById(profile.petType)
            val crossedMajorTier = targetStage.tierIndex > previousStage.tierIndex
            val bonusStars = if (crossedMajorTier) targetStage.milestoneBonusStars else 2L
            viewModelScope.launch {
                repository.syncPetGrowthMilestoneInFirestore(
                    currentProfile = profile.copy(totalStars = newTotalStars),
                    newPetLevel = targetLevel,
                    milestoneBonusStars = bonusStars
                )
                if (crossedMajorTier || targetLevel % 5L == 0L || targetLevel == PetGrowthStage.MAX_PET_LEVEL) {
                    _petEvolutionMilestone.value = PetEvolutionMilestoneEvent(
                        petSpecies = species,
                        petName = profile.petName.ifBlank { species.defaultName },
                        previousLevel = prevLevel,
                        newLevel = targetLevel,
                        previousStage = previousStage,
                        newStage = targetStage,
                        totalStars = newTotalStars + bonusStars,
                        bonusStarsAwarded = bonusStars
                    )
                }
            }
        }
    }

    /**
     * ViewModel function that updates the total star balance in Firestore whenever a task
     * is marked as complete, and evaluates pet growth milestones across 205 levels.
     */
    fun onTaskCompletedUpdateStars(task: QuestTask) {
        val currentTasks = (tasksState.value as? UiState.Success)?.data ?: emptyList()
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: KidProfile(userId = currentUserId)
        val wasDone = task.isCompletedOn(todayDate)
        val previousStars = currentProfile.totalStars
        val starDelta = if (!wasDone) task.starReward else -task.starReward
        val updatedTotalStars = (previousStars + starDelta).coerceAtLeast(0L)

        viewModelScope.launch {
            val result = repository.toggleTaskCompletion(
                task = task,
                allTasks = currentTasks,
                currentProfile = currentProfile,
                todayDate = todayDate
            )
            result.onSuccess { newBadgeIds ->
                if (!wasDone) {
                    handlePetGrowthFromStarBalance(
                        previousStars = previousStars,
                        newTotalStars = updatedTotalStars,
                        profile = currentProfile
                    )
                    val unlockedBadge = newBadgeIds.firstNotNullOfOrNull { id ->
                        BadgeCatalog.allBadges.firstOrNull { it.id == id }
                    }
                    _celebration.value = if (unlockedBadge != null) {
                        CelebrationBanner(
                            title = "NEW BADGE UNLOCKED!",
                            subtitle = "${unlockedBadge.title} — ${unlockedBadge.description}",
                            starsEarned = task.starReward,
                            newTotalStars = updatedTotalStars,
                            unlockedBadge = unlockedBadge
                        )
                    } else {
                        CelebrationBanner(
                            title = "Awesome Job, ${currentProfile.kidName}!",
                            subtitle = "Completed \"${task.title}\" & earned +1 Pet Treat!",
                            starsEarned = task.starReward,
                            newTotalStars = updatedTotalStars
                        )
                    }
                } else {
                    _celebration.value = null
                }
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not update quest star balance"
            }
        }
    }

    fun toggleTask(task: QuestTask) {
        onTaskCompletedUpdateStars(task)
    }

    fun careForPet(action: PetCareAction) {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        val previousStars = currentProfile.totalStars
        viewModelScope.launch {
            val result = repository.careForPetInFirestore(currentProfile, action)
            result.onSuccess { newStars ->
                handlePetGrowthFromStarBalance(
                    previousStars = previousStars,
                    newTotalStars = newStars,
                    profile = currentProfile
                )
                _celebration.value = CelebrationBanner(
                    title = "${currentProfile.petName} Loves That! ${action.emoji}",
                    subtitle = "${action.label} gave +${action.bonusStars} Gift Stars & +${action.xpGain} Pet XP!",
                    starsEarned = action.bonusStars,
                    newTotalStars = newStars
                )
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Need more Pet Treats!"
            }
        }
    }

    fun completePetMiniGame(
        miniGame: PetMiniGameType,
        starsEarned: Long,
        xpEarned: Long,
        treatsEarned: Long
    ) {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        val previousStars = currentProfile.totalStars
        val guaranteedNextLevel = (currentProfile.petLevel + 1L).coerceAtMost(PetGrowthStage.MAX_PET_LEVEL)
        viewModelScope.launch {
            val result = repository.completePetMiniGameInFirestore(
                currentProfile = currentProfile,
                starsEarned = starsEarned,
                xpEarned = xpEarned,
                treatsEarned = treatsEarned
            )
            result.onSuccess { newStars ->
                handlePetGrowthFromStarBalance(
                    previousStars = previousStars,
                    newTotalStars = newStars,
                    profile = currentProfile,
                    guaranteedMinLevel = guaranteedNextLevel
                )
                _celebration.value = CelebrationBanner(
                    title = "${miniGame.emoji} ${miniGame.title} Victory!",
                    subtitle = "${currentProfile.petName} leveled up to Lv. $guaranteedNextLevel! (+$xpEarned XP & +$treatsEarned 🍓)",
                    starsEarned = starsEarned,
                    newTotalStars = newStars
                )
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not save mini-game rewards"
            }
        }
    }

    fun selectPetCompanion(petType: String, petName: String) {
        viewModelScope.launch {
            repository.updateSelectedPet(petType, petName).onSuccess {
                _petVisualPulseTick.value += 1
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not switch pet companion"
            }
        }
    }

    fun addQuestTask(title: String, routine: RoutineType, iconName: String, starReward: Long) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val result = repository.createTask(
                title = title,
                routine = routine.key,
                iconName = iconName,
                starReward = starReward
            )
            result.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not add quest"
            }
        }
    }

    fun deleteQuestTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId).onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not remove quest"
            }
        }
    }

    fun addReward(title: String, iconName: String, starCost: Long) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.createReward(title, iconName, starCost).onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not add reward"
            }
        }
    }

    fun claimReward(reward: VisualReward) {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        val remainingStars = (currentProfile.totalStars - reward.starCost).coerceAtLeast(0L)
        viewModelScope.launch {
            val result = repository.claimReward(reward, currentProfile)
            result.onSuccess {
                _celebration.value = CelebrationBanner(
                    title = "GIFT REQUEST SENT TO PARENTS! 🎁",
                    subtitle = "Sent \"${reward.title}\" (${reward.starCost} ★) to the Parental Dashboard for approval!",
                    starsEarned = 0L,
                    newTotalStars = remainingStars
                )
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Not enough stars yet!"
            }
        }
    }

    fun approveRedemption(
        request: GiftRedemptionRequest,
        parentNote: String = "Approved! Great job earning your stars!"
    ) {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val result = repository.approveRedemptionRequest(request, currentProfile, parentNote)
            result.onSuccess { newBadgeIds ->
                val unlockedBadge = newBadgeIds.firstNotNullOfOrNull { id ->
                    BadgeCatalog.allBadges.firstOrNull { it.id == id }
                }
                _celebration.value = CelebrationBanner(
                    title = "PARENT APPROVED GIFT! ✅",
                    subtitle = "Approved \"${request.rewardTitle}\" for ${request.kidName}!",
                    starsEarned = 0L,
                    newTotalStars = currentProfile.totalStars,
                    unlockedBadge = unlockedBadge
                )
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not approve gift redemption"
            }
        }
    }

    fun rejectRedemption(
        request: GiftRedemptionRequest,
        parentNote: String = "Stars refunded to your bank for later!"
    ) {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        val previousStars = currentProfile.totalStars
        viewModelScope.launch {
            val result = repository.rejectRedemptionRequest(request, currentProfile, parentNote)
            result.onSuccess { refundedTotalStars ->
                handlePetGrowthFromStarBalance(
                    previousStars = previousStars,
                    newTotalStars = refundedTotalStars,
                    profile = currentProfile
                )
                _celebration.value = CelebrationBanner(
                    title = "Redemption Rejected & Stars Refunded ↩️",
                    subtitle = "Refunded +${request.starCost} ★ back to ${request.kidName}'s Star Bank!",
                    starsEarned = request.starCost,
                    newTotalStars = refundedTotalStars
                )
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not reject gift redemption"
            }
        }
    }

    fun deleteReward(rewardId: String) {
        viewModelScope.launch {
            repository.deleteReward(rewardId).onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not delete reward"
            }
        }
    }

    fun updateProfile(kidName: String, avatarId: String) {
        viewModelScope.launch {
            repository.updateProfileIdentity(kidName, avatarId).onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not update profile"
            }
        }
    }

    fun awardBonusStreakDay() {
        val currentProfile = (profileState.value as? UiState.Success)?.data ?: return
        val previousStars = currentProfile.totalStars
        val updatedTotalStars = previousStars + 10L
        viewModelScope.launch {
            val result = repository.logBonusStreakDay(currentProfile, todayDate)
            result.onSuccess { newBadgeIds ->
                handlePetGrowthFromStarBalance(
                    previousStars = previousStars,
                    newTotalStars = updatedTotalStars,
                    profile = currentProfile
                )
                val unlockedBadge = newBadgeIds.firstNotNullOfOrNull { id ->
                    BadgeCatalog.allBadges.firstOrNull { it.id == id }
                }
                _celebration.value = if (unlockedBadge != null) {
                    CelebrationBanner(
                        title = "BADGE UNLOCKED!",
                        subtitle = "${unlockedBadge.title} — ${unlockedBadge.description}",
                        starsEarned = 10L,
                        newTotalStars = updatedTotalStars,
                        unlockedBadge = unlockedBadge
                    )
                } else {
                    CelebrationBanner(
                        title = "Streak Boosted +1 Day!",
                        subtitle = "Parent bonus day logged with +10 Stars & +1 Pet Treat!",
                        starsEarned = 10L,
                        newTotalStars = updatedTotalStars
                    )
                }
            }.onFailure { err ->
                _actionError.value = err.localizedMessage ?: "Could not log bonus streak day"
            }
        }
    }

    private companion object {
        const val TAG = "QuestViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
