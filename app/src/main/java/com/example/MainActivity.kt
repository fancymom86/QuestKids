package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.model.KidProfile
import com.example.data.model.RedemptionStatus
import com.example.data.repository.QuestRepository
import com.example.ui.components.AddRewardDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AvatarCustomizerDialog
import com.example.ui.components.CelebrationFanfareCard
import com.example.ui.components.PetEvolutionCelebrationDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ParentalDashboardScreen
import com.example.ui.screens.PetSanctuaryTabScreen
import com.example.ui.screens.QuestsTabScreen
import com.example.ui.screens.RewardsChestTabScreen
import com.example.ui.screens.StreaksAndBadgesTabScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.QuestRoyalBlue
import com.example.ui.theme.StarGoldBright
import com.example.ui.theme.StreakFlameCoral
import com.example.ui.viewmodel.QuestViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

enum class QuestDestination(val label: String) {
    QUESTS("Daily Quests"),
    PET_SANCTUARY("Pet World"),
    STREAKS_BADGES("Streaks & Badges"),
    REWARDS("Parent Gifts"),
    PARENTAL_DASHBOARD("Parents")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        QuestKidsMainScreen(
            currentUserId = user.uid,
            defaultDisplayName = user.displayName ?: "Star Explorer"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestKidsMainScreen(
    currentUserId: String,
    defaultDisplayName: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: QuestViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                QuestViewModel(
                    repository = QuestRepository(db),
                    currentUserId = currentUserId,
                    defaultDisplayName = defaultDisplayName
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val rewardsState by viewModel.rewardsState.collectAsStateWithLifecycle()
    val redemptionsState by viewModel.redemptionsState.collectAsStateWithLifecycle()
    val selectedRoutine by viewModel.selectedRoutine.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val petEvolutionMilestone by viewModel.petEvolutionMilestone.collectAsStateWithLifecycle()
    val petPulseTick by viewModel.petVisualPulseTick.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(QuestDestination.QUESTS) }
    var showAddTaskDialog by rememberSaveable { mutableStateOf(false) }
    var showAddRewardDialog by rememberSaveable { mutableStateOf(false) }
    var showProfileDialog by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(actionError) {
        actionError?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    if (currentTab != QuestDestination.QUESTS) {
        BackHandler {
            currentTab = QuestDestination.QUESTS
        }
    }

    val profile = (profileState as? UiState.Success)?.data ?: KidProfile(
        userId = currentUserId,
        kidName = defaultDisplayName.substringBefore(" ").ifBlank { "Star Explorer" }
    )
    val tasks = (tasksState as? UiState.Success)?.data ?: emptyList()
    val rewards = (rewardsState as? UiState.Success)?.data ?: emptyList()
    val redemptions = (redemptionsState as? UiState.Success)?.data ?: emptyList()
    val pendingRedemptionsCount = remember(redemptions) {
        redemptions.count { RedemptionStatus.fromKey(it.status) == RedemptionStatus.PENDING }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(
                        onClick = { currentTab = QuestDestination.PARENTAL_DASHBOARD },
                        modifier = Modifier.testTag("top_bar_parental_dashboard_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (pendingRedemptionsCount > 0) {
                                    Badge(
                                        containerColor = StreakFlameCoral,
                                        contentColor = Color.White
                                    ) {
                                        Text("$pendingRedemptionsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AdminPanelSettings,
                                contentDescription = "Parental Dashboard"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            signOut(
                                credentialManager = credentialManager,
                                onSignOutComplete = {},
                                scope = scope
                            )
                        },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (currentTab == QuestDestination.QUESTS || currentTab == QuestDestination.REWARDS) {
                FloatingActionButton(
                    onClick = {
                        if (currentTab == QuestDestination.QUESTS) {
                            showAddTaskDialog = true
                        } else {
                            showAddRewardDialog = true
                        }
                    },
                    containerColor = StarGoldBright,
                    contentColor = Color(0xFF182238),
                    modifier = Modifier.testTag("fab_add_item")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (currentTab == QuestDestination.QUESTS) {
                            "Add new daily quest"
                        } else {
                            "Add new reward prize"
                        }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == QuestDestination.QUESTS,
                    onClick = { currentTab = QuestDestination.QUESTS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == QuestDestination.QUESTS) {
                                Icons.Filled.Checklist
                            } else {
                                Icons.Outlined.Checklist
                            },
                            contentDescription = "Daily Quests"
                        )
                    },
                    label = { Text("Quests") },
                    modifier = Modifier.testTag("nav_quests")
                )
                NavigationBarItem(
                    selected = currentTab == QuestDestination.PET_SANCTUARY,
                    onClick = { currentTab = QuestDestination.PET_SANCTUARY },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == QuestDestination.PET_SANCTUARY) {
                                Icons.Filled.Pets
                            } else {
                                Icons.Outlined.Pets
                            },
                            contentDescription = "Pet Sanctuary"
                        )
                    },
                    label = { Text("Pet World") },
                    modifier = Modifier.testTag("nav_pet_sanctuary")
                )
                NavigationBarItem(
                    selected = currentTab == QuestDestination.STREAKS_BADGES,
                    onClick = { currentTab = QuestDestination.STREAKS_BADGES },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == QuestDestination.STREAKS_BADGES) {
                                Icons.Filled.EmojiEvents
                            } else {
                                Icons.Outlined.EmojiEvents
                            },
                            contentDescription = "Streaks & Badges"
                        )
                    },
                    label = { Text("Badges") },
                    modifier = Modifier.testTag("nav_streaks_badges")
                )
                NavigationBarItem(
                    selected = currentTab == QuestDestination.REWARDS,
                    onClick = { currentTab = QuestDestination.REWARDS },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == QuestDestination.REWARDS) {
                                Icons.Filled.Redeem
                            } else {
                                Icons.Outlined.Redeem
                            },
                            contentDescription = "Parent Gifts"
                        )
                    },
                    label = { Text("Gifts") },
                    modifier = Modifier.testTag("nav_rewards")
                )
                NavigationBarItem(
                    selected = currentTab == QuestDestination.PARENTAL_DASHBOARD,
                    onClick = { currentTab = QuestDestination.PARENTAL_DASHBOARD },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingRedemptionsCount > 0) {
                                    Badge(
                                        containerColor = StreakFlameCoral,
                                        contentColor = Color.White
                                    ) {
                                        Text("$pendingRedemptionsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == QuestDestination.PARENTAL_DASHBOARD) {
                                    Icons.Filled.AdminPanelSettings
                                } else {
                                    Icons.Outlined.AdminPanelSettings
                                },
                                contentDescription = "Parental Dashboard"
                            )
                        }
                    },
                    label = { Text("Parents") },
                    modifier = Modifier.testTag("nav_parent_dashboard")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CelebrationFanfareCard(
                celebration = celebration,
                onDismiss = viewModel::dismissCelebration
            )

            if (tasksState is UiState.Loading && tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = QuestRoyalBlue,
                        modifier = Modifier.size(44.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (currentTab) {
                        QuestDestination.QUESTS -> {
                            QuestsTabScreen(
                                profile = profile,
                                tasks = tasks,
                                todayDate = viewModel.todayDate,
                                selectedRoutine = selectedRoutine,
                                petPulseTick = petPulseTick,
                                onSelectRoutine = viewModel::selectRoutineFilter,
                                onToggleTask = viewModel::onTaskCompletedUpdateStars,
                                onDeleteTask = viewModel::deleteQuestTask,
                                onOpenAddTask = { showAddTaskDialog = true },
                                onEditProfile = { showProfileDialog = true },
                                onOpenPetSanctuary = { currentTab = QuestDestination.PET_SANCTUARY },
                                onQuickCarePet = viewModel::careForPet
                            )
                        }
                        QuestDestination.PET_SANCTUARY -> {
                            PetSanctuaryTabScreen(
                                profile = profile,
                                petPulseTick = petPulseTick,
                                onCareForPet = viewModel::careForPet,
                                onCompleteMiniGame = viewModel::completePetMiniGame,
                                onSelectPet = viewModel::selectPetCompanion,
                                onOpenRewardsChest = { currentTab = QuestDestination.REWARDS }
                            )
                        }
                        QuestDestination.STREAKS_BADGES -> {
                            StreaksAndBadgesTabScreen(
                                profile = profile,
                                tasks = tasks,
                                todayDate = viewModel.todayDate,
                                onAwardBonusStreakDay = viewModel::awardBonusStreakDay
                            )
                        }
                        QuestDestination.REWARDS -> {
                            RewardsChestTabScreen(
                                profile = profile,
                                rewards = rewards,
                                redemptions = redemptions,
                                onClaimReward = viewModel::claimReward,
                                onDeleteReward = viewModel::deleteReward,
                                onOpenAddReward = { showAddRewardDialog = true },
                                onOpenParentalDashboard = { currentTab = QuestDestination.PARENTAL_DASHBOARD }
                            )
                        }
                        QuestDestination.PARENTAL_DASHBOARD -> {
                            ParentalDashboardScreen(
                                profile = profile,
                                redemptions = redemptions,
                                rewards = rewards,
                                onApproveRedemption = viewModel::approveRedemption,
                                onRejectRedemption = viewModel::rejectRedemption,
                                onOpenNewRewardDialog = { showAddRewardDialog = true },
                                onOpenNewQuestDialog = { showAddTaskDialog = true },
                                onSimulateNextStreakDay = viewModel::awardBonusStreakDay
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { title, routine, icon, stars ->
                viewModel.addQuestTask(title, routine, icon, stars)
            }
        )
    }

    if (showAddRewardDialog) {
        AddRewardDialog(
            onDismiss = { showAddRewardDialog = false },
            onAddReward = { title, icon, cost ->
                viewModel.addReward(title, icon, cost)
            }
        )
    }

    if (showProfileDialog) {
        AvatarCustomizerDialog(
            profile = profile,
            onDismiss = { showProfileDialog = false },
            onSave = { name, avatarId ->
                viewModel.updateProfile(name, avatarId)
            }
        )
    }

    PetEvolutionCelebrationDialog(
        event = petEvolutionMilestone,
        onDismiss = viewModel::dismissPetEvolutionMilestone
    )
}
