# QuestKids Firestore Security Specification

## 1. Data Invariants
1. **Multi-User Isolation**: Every document in `/users/{userId}`, `/users/{userId}/tasks/{taskId}`, `/users/{userId}/rewards/{rewardId}`, and `/users/{userId}/redemptions/{redemptionId}` belongs strictly to `userId` (`request.auth.uid == userId`). Cross-user reads and writes are unconditionally denied.
2. **Schema & Key Whitelisting (`hasAll` & `hasOnly`)**:
   - `KidProfile` (`/users/{userId}`): Required keys `['userId', 'kidName', 'avatarId', 'totalStars', 'level', 'currentStreak', 'longestStreak', 'lastCompletedDate', 'completedDates', 'morningPerfectDates', 'weekendPerfectDates', 'unlockedBadges', 'claimedRewards', 'createdAt', 'updatedAt']`, optional pet keys `['petType', 'petName', 'petLevel', 'petXp', 'petHappiness', 'petTreats']`.
   - `QuestTask` (`/users/{userId}/tasks/{taskId}`): Required keys `['userId', 'title', 'routine', 'iconName', 'starReward', 'completedToday', 'lastCompletedDate', 'completionCount', 'createdAt', 'updatedAt']`.
   - `VisualReward` (`/users/{userId}/rewards/{rewardId}`): Required keys `['userId', 'title', 'iconName', 'starCost', 'timesClaimed', 'createdAt', 'updatedAt']`.
   - `GiftRedemptionRequest` (`/users/{userId}/redemptions/{redemptionId}`): Required keys `['userId', 'rewardId', 'rewardTitle', 'iconName', 'starCost', 'kidName', 'status', 'parentNote', 'createdAt', 'updatedAt']`.
3. **Enum & Range Constraints**:
   - `QuestTask.routine` must be one of `['MORNING', 'AFTERNOON', 'EVENING', 'WEEKEND']`.
   - `QuestTask.starReward` must be an `int` between `1` and `50`.
   - `VisualReward.starCost` and `GiftRedemptionRequest.starCost` must be an `int` between `5` and `500`.
   - `GiftRedemptionRequest.status` must be one of `['PENDING', 'APPROVED', 'REJECTED']`.
4. **Immutability**: `userId` and `createdAt` cannot be modified during `update` operations.
5. **Timestamp Integrity**: `createdAt` and `updatedAt` must be valid server timestamps (`<= request.time`).
