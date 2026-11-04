const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validProfilePayload(userId, now) {
  return {
    userId,
    kidName: "Leo Explorer",
    avatarId: "star_captain",
    totalStars: 15,
    level: 2,
    currentStreak: 3,
    longestStreak: 5,
    lastCompletedDate: "2026-11-04",
    completedDates: ["2026-11-02", "2026-11-03", "2026-11-04"],
    morningPerfectDates: ["2026-11-04"],
    weekendPerfectDates: [],
    unlockedBadges: ["first_quest"],
    claimedRewards: [],
    createdAt: now,
    updatedAt: now,
  };
}

function validTaskPayload(userId, now) {
  return {
    userId,
    title: "Brush Teeth & Sparkle",
    routine: "MORNING",
    iconName: "toothbrush",
    starReward: 5,
    completedToday: false,
    lastCompletedDate: "",
    completionCount: 0,
    createdAt: now,
    updatedAt: now,
  };
}

function validRewardPayload(userId, now) {
  return {
    userId,
    title: "Extra Story Time",
    iconName: "book",
    starCost: 25,
    timesClaimed: 0,
    createdAt: now,
    updatedAt: now,
  };
}

test("1. Unauthenticated user cannot read profile or tasks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );
});

test("2. Authenticated user can create and read their own profile, tasks, and rewards", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .set(validProfilePayload(ALICE_UID, now))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_1")
      .set(validTaskPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("rewards")
      .doc("reward_1")
      .set(validRewardPayload(ALICE_UID, now))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("rewards").get()
  );
});

test("3. Cross-user isolation: Alice cannot read or write Bob's tasks or profile", async () => {
  const now = new Date(Date.now() - 1000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db
      .collection("users")
      .doc(BOB_UID)
      .set(validProfilePayload(BOB_UID, now));
    await db
      .collection("users")
      .doc(BOB_UID)
      .collection("tasks")
      .doc("bob_task")
      .set(validTaskPayload(BOB_UID, now));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb
      .collection("users")
      .doc(BOB_UID)
      .collection("tasks")
      .doc("bob_task")
      .get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("tasks").get()
  );
});

test("4. Dirty Dozen: Shadow field injection, identity spoofing, and invalid enums are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  // Identity spoofing
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_spoof")
      .set({ ...validTaskPayload(BOB_UID, now) })
  );

  // Shadow field injection
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_shadow")
      .set({ ...validTaskPayload(ALICE_UID, now), isAdmin: true })
  );

  // Invalid routine enum
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_enum")
      .set({ ...validTaskPayload(ALICE_UID, now), routine: "MIDNIGHT" })
  );

  // Negative starReward
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("tasks")
      .doc("task_neg")
      .set({ ...validTaskPayload(ALICE_UID, now), starReward: 0 })
  );
});

test("5. Immutable createdAt cannot be mutated on update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  const taskRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("tasks")
    .doc("task_imm");
  await assertSucceeds(taskRef.set(validTaskPayload(ALICE_UID, now)));

  // Valid update
  await assertSucceeds(
    taskRef.update({
      completedToday: true,
      completionCount: 1,
      lastCompletedDate: "2026-11-04",
      updatedAt: later,
    })
  );

  // Invalid mutation of createdAt
  await assertFails(
    taskRef.update({
      createdAt: later,
      updatedAt: later,
    })
  );
});

test("6. Parental Dashboard redemptions subcollection enforces valid status and immutable fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 5000);
  const later = new Date(Date.now() - 1000);

  const redemptionRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("redemptions")
    .doc("redemption_1");

  await assertSucceeds(
    redemptionRef.set({
      userId: ALICE_UID,
      rewardId: "reward_1",
      rewardTitle: "Choose Family Movie Night",
      iconName: "movie",
      starCost: 15,
      kidName: "Leo",
      status: "PENDING",
      parentNote: "Waiting for parent approval!",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Parent approval update succeeds
  await assertSucceeds(
    redemptionRef.update({
      status: "APPROVED",
      parentNote: "Approved for Friday night!",
      updatedAt: later,
    })
  );

  // Invalid status enum fails
  await assertFails(
    redemptionRef.update({
      status: "HACKED",
      updatedAt: later,
    })
  );

  // Mutating starCost on update fails
  await assertFails(
    redemptionRef.update({
      starCost: 999,
      updatedAt: later,
    })
  );
});

