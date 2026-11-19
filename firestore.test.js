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

  // Owner can update allowed profile fields including verification & PLATINUM tier
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      profession: "Staff Engineer",
      isVerified: true,
      verifiedTier: "PLATINUM",
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
  await assertSucceeds(
    bobDb.collection("flux_videos").doc("flux_1").update({
      savedBy: [BOB_UID],
    })
  );
});

test("Store wallet enforces 500 starting coins, cooldowns, 1-time milestones, exact prices, and owner isolation", async () => {
  const rutPath = require.resolve("@firebase/rules-unit-testing");
  const firebaseCompat = require(require.resolve("firebase/compat/app", { paths: [rutPath] })).default;
  require(require.resolve("firebase/compat/firestore", { paths: [rutPath] }));
  const serverTimestamp = () => firebaseCompat.firestore.FieldValue.serverTimestamp();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceWallet = aliceDb.collection("users").doc(ALICE_UID).collection("wallet").doc("state");
  const bobViewAliceWallet = bobDb.collection("users").doc(ALICE_UID).collection("wallet").doc("state");

  // Inflated starting balance must be rejected
  await assertFails(
    aliceWallet.set({
      coinBalance: 99999,
      ownedItemIds: [],
      claimedMilestones: [],
      lastDailyClaimAt: null,
      equippedFrame: null,
      equippedNameplate: null,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Legitimate initial wallet with 500 coins succeeds
  await assertSucceeds(
    aliceWallet.set({
      coinBalance: 500,
      ownedItemIds: [],
      claimedMilestones: [],
      lastDailyClaimAt: null,
      equippedFrame: null,
      equippedNameplate: null,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Bob cannot read or modify Alice's wallet
  await assertFails(bobViewAliceWallet.get());
  await assertFails(bobViewAliceWallet.update({ coinBalance: 1000 }));

  // Alice can claim daily +250 coins once
  await assertSucceeds(
    aliceWallet.update({
      coinBalance: 750,
      lastDailyClaimAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Immediate repeat daily claim within 24h is rejected
  await assertFails(
    aliceWallet.update({
      coinBalance: 1000,
      lastDailyClaimAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Alice can claim one-time milestone 'm_flux' (+250)
  await assertSucceeds(
    aliceWallet.update({
      coinBalance: 1000,
      claimedMilestones: ["m_flux"],
      updatedAt: serverTimestamp(),
    })
  );

  // Duplicate claim of 'm_flux' or arbitrary coin inflation fails
  await assertFails(
    aliceWallet.update({
      coinBalance: 1250,
      claimedMilestones: ["m_flux"],
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(
    aliceWallet.update({
      coinBalance: 5000,
      updatedAt: serverTimestamp(),
    })
  );

  // Alice buys 'cat_bell' (exact price 200 -> 800 coins left)
  await assertSucceeds(
    aliceWallet.update({
      coinBalance: 800,
      ownedItemIds: ["cat_bell"],
      equippedFrame: "cat_bell",
      updatedAt: serverTimestamp(),
    })
  );

  // Underpaying for 'imperial_gold' (costs 400, paying 100) fails
  await assertFails(
    aliceWallet.update({
      coinBalance: 700,
      ownedItemIds: ["cat_bell", "imperial_gold"],
      equippedFrame: "imperial_gold",
      updatedAt: serverTimestamp(),
    })
  );

  // Alice can unequip owned frame, but cannot equip unowned frame
  await assertSucceeds(
    aliceWallet.update({
      equippedFrame: null,
      updatedAt: serverTimestamp(),
    })
  );
  await assertFails(
    aliceWallet.update({
      equippedFrame: "celestial_wings",
      updatedAt: serverTimestamp(),
    })
  );
});

test("Conversation media chunks and media messages enforce participant isolation, immutable chunks, and size caps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("conversations").doc("convo_media").set({
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

  // Alice uploads a media chunk for Bob
  const chunkRef = aliceDb
    .collection("conversations")
    .doc("convo_media")
    .collection("media")
    .doc("m_hdvideo1")
    .collection("chunks")
    .doc("chunk_0");

  await assertSucceeds(
    chunkRef.set({
      mediaId: "m_hdvideo1",
      senderId: ALICE_UID,
      index: 0,
      data: "AAAAIGZ0eXBtcDQyAAAAAG1wNDJpc29t",
      createdAt: new Date(),
    })
  );

  // Alice sends the accompanying HD video message
  await assertSucceeds(
    aliceDb.collection("conversations").doc("convo_media").collection("messages").doc("msg_video").set({
      conversationId: "convo_media",
      senderId: ALICE_UID,
      senderName: "Alice",
      isSenderVerified: false,
      senderVerifiedTier: "BLUE",
      content: "🎬 demo_hd.mp4",
      mediaType: "video",
      mediaId: "m_hdvideo1",
      mediaFileName: "demo_hd.mp4",
      mediaMimeType: "video/mp4",
      mediaSizeBytes: 1048576,
      mediaChunkCount: 3,
      mediaDurationMs: 12000,
      status: "sent",
      readBy: [ALICE_UID],
      isEdited: false,
      isDeleted: false,
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob (participant) can read the media chunk
  await assertSucceeds(
    bobDb
      .collection("conversations")
      .doc("convo_media")
      .collection("media")
      .doc("m_hdvideo1")
      .collection("chunks")
      .doc("chunk_0")
      .get()
  );

  // Media chunks are immutable
  await assertFails(chunkRef.update({ data: "tampered" }));
  await assertFails(chunkRef.delete());

  // Eve (non-participant) cannot read or upload chunks
  await assertFails(
    eveDb
      .collection("conversations")
      .doc("convo_media")
      .collection("media")
      .doc("m_hdvideo1")
      .collection("chunks")
      .doc("chunk_0")
      .get()
  );
  await assertFails(
    eveDb
      .collection("conversations")
      .doc("convo_media")
      .collection("media")
      .doc("m_hdvideo1")
      .collection("chunks")
      .doc("chunk_1")
      .set({
        mediaId: "m_hdvideo1",
        senderId: EVE_UID,
        index: 1,
        data: "ZmFrZQ==",
        createdAt: new Date(),
      })
  );
});

test("Voice call signaling isolates calls and ICE candidates strictly to the two participants", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const eveDb = testEnv.authenticatedContext(EVE_UID).firestore();

  const aliceCallRef = aliceDb.collection("calls").doc("call_101");
  await assertSucceeds(
    aliceCallRef.set({
      conversationId: "convo_media",
      callerId: ALICE_UID,
      callerName: "Alice",
      calleeId: BOB_UID,
      calleeName: "Bob",
      participantIds: [ALICE_UID, BOB_UID],
      status: "ringing",
      offerSdp: "v=0\r\no=- 1 2 IN IP4 127.0.0.1\r\n",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  // Bob can query incoming calls and answer
  await assertSucceeds(
    bobDb.collection("calls").where("participantIds", "array-contains", BOB_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("calls").doc("call_101").update({
      answerSdp: "v=0\r\no=- 2 2 IN IP4 127.0.0.1\r\n",
      status: "connecting",
      updatedAt: new Date(),
    })
  );

  // Participants can exchange ICE candidates
  await assertSucceeds(
    aliceCallRef.collection("candidates").doc("ice_1").set({
      senderId: ALICE_UID,
      sdpMid: "0",
      sdpMLineIndex: 0,
      candidate: "candidate:1 1 UDP 2122252543 192.168.1.5 5000 typ host",
      createdAt: new Date(),
    })
  );
  await assertSucceeds(
    bobDb.collection("calls").doc("call_101").collection("candidates").get()
  );

  // Eve cannot read call, hijack status, or inject ICE candidates
  await assertFails(eveDb.collection("calls").doc("call_101").get());
  await assertFails(
    eveDb.collection("calls").doc("call_101").update({
      status: "ended",
      updatedAt: new Date(),
    })
  );
  await assertFails(
    eveDb.collection("calls").doc("call_101").collection("candidates").doc("ice_eve").set({
      senderId: EVE_UID,
      sdpMid: "0",
      sdpMLineIndex: 0,
      candidate: "candidate:evil",
      createdAt: new Date(),
    })
  );
});

