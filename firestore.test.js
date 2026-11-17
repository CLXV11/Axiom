const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

const ALICE_UID = "user_alice";
const BOB_UID = "user_bob";
const EVE_UID = "user_eve";

let testEnv;

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: emulatorHost,
      port: emulatorPort,
      rules,
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

test("Unauthenticated user cannot read or write user profiles, conversations, posts, or flux", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").limit(10).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      displayName: "Alice",
      handle: "@alice",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
  await assertFails(unauthDb.collection("conversations").where("participantIds", "array-contains", ALICE_UID).get());
  await assertFails(unauthDb.collection("posts").get());
  await assertFails(unauthDb.collection("flux_videos").get());
});

test("User can create, discover peers, and update own profile while ghost/admin/verified-pro spoofing is blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      displayName: "Alice Smith",
      handle: "@alicesmith",
      profession: "Android Engineer",
      isProfessionalVerified: false,
      openToNetworking: true,
      isVerified: false,
      verifiedTier: "GOLD",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob can discover Alice in directory
  await assertSucceeds(bobDb.collection("users").limit(50).get());

  // Owner can update allowed profile fields
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      profession: "Staff Engineer",
      updatedAt: new Date(),
    })
  );

  // Alice cannot self-grant isProfessionalVerified or inject ghost fields or PII
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isProfessionalVerified: true,
      updatedAt: new Date(),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      role: "admin",
      updatedAt: new Date(),
    })
  );

  // Bob cannot modify Alice's profile
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      displayName: "Hacked",
      updatedAt: new Date(),
    })
  );
});

test("Conversation participants can create chats, update summary/pins, exchange messages; non-participants are blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  // Alice creates direct chat with Bob
  await assertSucceeds(
    aliceDb.collection("conversations").doc("convo_1").set({
      type: "direct",
      title: "Bob",
      isVerified: false,
      verifiedTier: "BLUE",
      participantIds: [ALICE_UID, BOB_UID],
      lastMessageText: "",
      lastMessageSenderId: "",
      pinnedBy: [],
      archivedBy: [],
      mutedBy: [],
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Alice queries her own conversations (matches rule)
  await assertSucceeds(
    aliceDb.collection("conversations").where("participantIds", "array-contains", ALICE_UID).get()
  );

  // Unfiltered conversation list fails (rules are not filters)
  await assertFails(eveDb.collection("conversations").get());

  // Alice sends a message
  await assertSucceeds(
    aliceDb.collection("conversations").doc("convo_1").collection("messages").doc("msg_1").set({
      conversationId: "convo_1",
      senderId: ALICE_UID,
      senderName: "Alice",
      isSenderVerified: false,
      senderVerifiedTier: "GOLD",
      content: "Hello Bob!",
      status: "sent",
      readBy: [ALICE_UID],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Alice updates conversation summary
  await assertSucceeds(
    aliceDb.collection("conversations").doc("convo_1").update({
      lastMessageText: "Hello Bob!",
      lastMessageSenderId: ALICE_UID,
      lastMessageTimestamp: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob reads messages, marks read, and pins conversation
  await assertSucceeds(
    bobDb.collection("conversations").doc("convo_1").collection("messages").doc("msg_1").get()
  );
  await assertSucceeds(
    bobDb.collection("conversations").doc("convo_1").collection("messages").doc("msg_1").update({
      status: "read",
      readBy: [ALICE_UID, BOB_UID],
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("conversations").doc("convo_1").update({
      pinnedBy: [BOB_UID],
      updatedAt: new Date(),
    })
  );

  // Bob cannot edit Alice's message content
  await assertFails(
    bobDb.collection("conversations").doc("convo_1").collection("messages").doc("msg_1").update({
      content: "Tampered!",
      isEdited: true,
      updatedAt: new Date(),
    })
  );

  // Eve cannot read conversation or read/write messages
  await assertFails(eveDb.collection("conversations").doc("convo_1").get());
  await assertFails(
    eveDb.collection("conversations").doc("convo_1").collection("messages").doc("msg_1").get()
  );
  await assertFails(
    eveDb.collection("conversations").doc("convo_1").collection("messages").doc("msg_2").set({
      conversationId: "convo_1",
      senderId: EVE_UID,
      senderName: "Eve",
      content: "Sneaking in!",
      createdAt: new Date(),
    })
  );
});

test("Saved memory vault is strictly private to the owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_memories").doc("mem_1").set({
      userId: ALICE_UID,
      originalMessageId: "msg_1",
      conversationId: "convo_1",
      snippet: "Meeting tomorrow at 10am",
      senderName: "Alice",
      privateNote: "Prepare deck",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_memories").doc("mem_1").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_memories").get()
  );

  // Bob cannot read, list, or write Alice's saved memory
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("saved_memories").doc("mem_1").get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("saved_memories").get()
  );
});

test("Posts, likes, comments, and Flux clips enforce author ownership and valid state updates", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "@alice",
      content: "Hello Kora!",
      audience: "public",
      likedBy: [],
      likesCount: 0,
      commentsCount: 0,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob can like post and add comment, but cannot edit post content
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      likedBy: [BOB_UID],
      likesCount: 1,
    })
  );
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      content: "Defaced post",
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c_1").set({
      postId: "post_1",
      authorId: BOB_UID,
      authorName: "Bob",
      authorHandle: "@bob",
      isAuthorVerified: false,
      content: "Nice post!",
      createdAt: new Date(),
    })
  );

  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      commentsCount: 1,
    })
  );

  // Flux clip create + like by Bob
  await assertSucceeds(
    aliceDb.collection("flux_videos").doc("flux_1").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      authorHandle: "@alice",
      isAuthorVerified: false,
      authorVerifiedTier: "GOLD",
      caption: "First Flux #kora",
      audioTitle: "Original Audio",
      likesCount: 0,
      likedBy: [],
      commentsCount: 0,
      sharesCount: 0,
      savedBy: [],
      tags: ["#kora"],
      gradientTheme: "BLURPLE",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("flux_videos").doc("flux_1").update({
      likedBy: [BOB_UID],
      likesCount: 1,
    })
  );
});
