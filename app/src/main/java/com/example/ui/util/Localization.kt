package com.example.ui.util

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val displayName: String, val layoutDirection: LayoutDirection) {
  EN("en", "English", LayoutDirection.Ltr),
  AR("ar", "العربية", LayoutDirection.Rtl)
}

/**
 * Wraps @handle in Unicode Left-to-Right Isolate characters (\u2066...\u2069)
 * so handles always render as "@username" instead of "username@" in RTL Arabic UI.
 */
fun formatHandleForDisplay(handle: String?): String {
  val trimmed = handle?.trim().orEmpty()
  if (trimmed.isBlank()) return "\u2066@user\u2069"
  val normalized = if (trimmed.startsWith("@")) trimmed else "@$trimmed"
  return "\u2066$normalized\u2069"
}

interface KoraStrings {
  // Brand & Welcome
  val appName: String
  val tagline: String
  val signInWithGoogle: String
  val welcomeBack: String
  val createProfile: String
  val enterDisplayName: String
  val enterHandle: String
  val enterBio: String
  val saveProfile: String
  val signingIn: String

  // Navigation
  val navChats: String
  val navFeed: String
  val navMemory: String
  val navProfile: String
  val navSettings: String

  // Chats
  val conversations: String
  val searchChats: String
  val directChats: String
  val groupChats: String
  val newChat: String
  val newGroup: String
  val noChatsYet: String
  val startFirstConversation: String
  val typeMessage: String
  val send: String
  val reply: String
  val replyingTo: String
  val edit: String
  val delete: String
  val copy: String
  val messageCopied: String
  val saveToMemory: String
  val pinChat: String
  val unpinChat: String
  val archiveChat: String
  val unarchiveChat: String
  val muteChat: String
  val unmuteChat: String
  val edited: String
  val messageDeleted: String

  // Social Feed
  val feedTitle: String
  val whatsOnYourMind: String
  val publish: String
  val audiencePublic: String
  val audienceCircles: String
  val filterAll: String
  val filterCircles: String
  val contextThreads: String
  val viewThread: String
  val writeAComment: String
  val noPostsYet: String
  val beTheFirstToShare: String
  val likes: String
  val comments: String

  // Personal Memory Vault
  val memoryTitle: String
  val memorySubtitle: String
  val noSavedMemories: String
  val memoryTip: String
  val searchMemory: String
  val addPrivateNote: String
  val notePlaceholder: String
  val saveNote: String
  val memorySaved: String

  // Profile & Presence
  val profileTitle: String
  val quietPresence: String
  val quietModeDesc: String
  val online: String
  val quietMode: String
  val invisible: String
  val readReceipts: String
  val readReceiptsDesc: String
  val signOut: String

  // Settings
  val settingsTitle: String
  val appearance: String
  val themeLight: String
  val themeDark: String
  val themeSystem: String
  val language: String
  val english: String
  val arabic: String
  val cancel: String
  val confirm: String
  val ok: String
  val errorOccurred: String
  val retry: String

  // Decoration Store
  val storeTitle: String
  val customDecoration: String
  val buyCoins: String
  val earnCoins: String
  val avatarFrames: String
  val roleNameplates: String
  val achievementWalls: String
  val chatBubbles: String
  val allCategories: String
  val preview: String
  val equip: String
  val equipped: String
  val unequip: String
  val buyFor: String
  val notEnoughCoins: String
  val purchasedSuccess: String
  val dailyCheckIn: String
  val dailyCheckInDesc: String
  val claimCoins: String
  val coinsClaimed: String
  val shopBannerText: String
  val buyCoinsDesc: String
  val packStarter: String
  val packPopular: String
  val packElite: String

  // Verification Badge
  val verifiedAccount: String
  val verificationBadge: String
  val verifiedBadgeSubtitle: String
  val verifiedStatusActive: String
  val verifiedStatusInactive: String
  val toggleVerification: String
  val verificationDetails: String
  val verifiedDialogTitle: String
  val verifiedDialogMessage: String

  // Typing Indicator
  val isTyping: String
  val typingSingle: String
  val typingMultiple: String

  // Flux Short Videos
  val navFlux: String
  val fluxTitle: String
  val fluxSubtitle: String
  val newFlux: String
  val publishFlux: String
  val selectVideo: String
  val fluxCaptionPlaceholder: String
  val fluxAudioPlaceholder: String
  val noFluxYet: String
  val beFirstToPublishFlux: String
  val fluxPublished: String
  val fluxCommentsTitle: String
  val addFluxComment: String

  // Professional Theme Switcher
  val themeSwitcherTitle: String
  val themePresets: String
  val customAccentColor: String
  val amoledMode: String
  val amoledModeDesc: String
  val themePreview: String
  val livePreviewChannel: String
  val livePreviewSampleMessage: String
  val quickThemeSwitcher: String

  // Profile Avatar Customization
  val changeAvatarTitle: String
  val changeAvatarSubtitle: String
  val chooseFromGallery: String
  val curatedAvatars: String
  val customImageUrl: String
  val enterImageUrlPlaceholder: String
  val applyUrl: String
  val resetToInitial: String
  val avatarUpdatedSuccess: String
  val previewAvatar: String

  // Searchable Contact Directory
  val contactDirectoryTitle: String
  val contactDirectorySubtitle: String
  val searchByNameOrProfession: String
  val allProfessions: String
  val contactsCount: String
  val clearSearch: String
  val noContactsFound: String
  val noContactsFoundDesc: String

  // Verified Professional Networking
  val verifiedProTitle: String
  val verifiedProBadge: String
  val verifiedProSubtitle: String
  val openToNetworking: String
  val professionalCredentialsTitle: String
  val verifiedProDialogDesc: String
  val filterVerifiedProOnly: String

  // Single AI Assistant Chatbot
  val aiAssistantName: String
  val aiAssistantSubtitle: String
  val aiWelcomeTitle: String
  val aiWelcomeDesc: String
  val aiPromptOne: String
  val aiPromptTwo: String
  val aiPromptThree: String
  val aiClearChat: String
  val aiBotBadge: String
  val aiThinking: String
  val aiErrorBannerTitle: String

  // Multi-user Cloud Session & Profile Fields
  val enterProfession: String
  val checkingAccount: String
  val syncingProfile: String
  val openToNetworkingToggle: String
  val profileUpdated: String

  // Additional UI polish labels
  val editProfile: String
  val quietFocusActive: String
  val standardAlerts: String
  val getPack: String
  val coinsUnit: String
  val fromLabel: String
  val privateNoteLabel: String
  val loading: String
  val aboutKoraTitle: String
  val aboutKoraDesc: String
  val activeChatsCount: String
  val publishedSuccess: String
  val groupNameLabel: String
  val selectedLabel: String

  // Store Quests & Server Wallet
  val questsTab: String
  val questsSubtitle: String
  val claimQuest: String
  val questClaimed: String
  val questProfileTitle: String
  val questProfileDesc: String
  val questVerifiedTitle: String
  val questVerifiedDesc: String
  val questFluxTitle: String
  val questFluxDesc: String
  val questAvatarTitle: String
  val questAvatarDesc: String
  val dailyCooldownFormat: String

  // Verification, Chat, & Flux Save/Gallery Feedback
  val verificationEnableSuccess: String
  val verificationDisableSuccess: String
  val verificationError: String
  val chatDeliveryError: String
  val fluxFilterAll: String
  val fluxFilterSaved: String
  val noSavedFluxYet: String
  val noSavedFluxDesc: String
  val videoSavedToast: String
  val videoUnsavedToast: String
  val saveToGallery: String
  val savedToGalleryToast: String
  val gallerySaveFailedToast: String
  val fluxLinkCopied: String
  val fluxAmbientStyle: String
  val videoSelectedDevice: String
  val tapToChooseVideo: String
  val noCommentsYet: String

  // Voice Calls & Rich In-Chat Media / Inline Video Player
  val startVoiceCall: String
  val voiceCallLabel: String
  val callQualityHdBadge: String
  val callRinging: String
  val callIncoming: String
  val callConnecting: String
  val callActive: String
  val callEnded: String
  val callDeclined: String
  val callBusyOrFailed: String
  val acceptCall: String
  val declineCall: String
  val endCall: String
  val muteMic: String
  val unmuteMic: String
  val speakerOn: String
  val speakerOff: String
  val minimizeCall: String
  val returnToCall: String
  val micPermissionRequired: String
  val attachMedia: String
  val attachPhoto: String
  val attachHdVideo: String
  val attachFile: String
  val uploadingMediaProgress: String
  val downloadingMedia: String
  val mediaUploadFailed: String
  val mediaDownloadFailed: String
  val fileTooLargeLimit: String
  val tapToPlayInline: String
  val hdBadge: String
  val fullscreenVideo: String
  val openFileAction: String
  val saveFileAction: String
  val savedImageToast: String
  val savedFileToast: String
  val noAppToOpenFile: String

  // New Message Notifications
  val newMessageChannelName: String
  val newMessageChannelDesc: String
  val newMessageNotificationTitle: String
  val newMessageFromTitle: String
  val tapToOpenChat: String

  // Luxury Splash & Youthful Onboarding
  val splashEditorialLine: String
  val splashTapToSkip: String
  val welcomeKicker: String
  val welcomeHeadline: String
  val welcomeSubheadline: String
  val pickYourVibe: String
  val amoledChipLabel: String
  val featureVoiceHdTitle: String
  val featureVoiceHdDesc: String
  val featureCinemaTitle: String
  val featureCinemaDesc: String
  val featurePulseTitle: String
  val featurePulseDesc: String
  val switchGoogleAccount: String
  val privacySecureBadge: String
  val chooseAvatarOptional: String
  val googlePhotoOption: String
  val quickRoleLabel: String
  val quickRoleCreator: String
  val quickRoleDeveloper: String
  val quickRoleDesigner: String
  val quickRoleGamer: String
  val quickRoleStudent: String
  val quickRoleFounder: String
  val startQuietModeToggle: String
  val startQuietModeDesc: String
  val setupStepSubtitle: String

  // Archived Chats, Multi-Emoji Reactions & Flux Upload
  val filterArchived: String
  val reactAction: String
  val emojiPickerTitle: String
  val weirdReactionsTab: String
  val classicReactionsTab: String
  val signalReactionsTab: String
  val moreReactionsDesc: String
  val postCommentsTitle: String
  val noPostCommentsYet: String
  val uploadingFluxVideoProgress: String

  // Comprehensive Settings Hub & Concise Flux Labels
  val fluxSaveShort: String
  val settingsAccountSection: String
  val settingsEditIdentity: String
  val settingsEditIdentityDesc: String
  val settingsAccountEmailLabel: String
  val settingsPrivacySection: String
  val settingsNotificationsSection: String
  val settingsInAppBanners: String
  val settingsInAppBannersDesc: String
  val settingsSoundAlerts: String
  val settingsSoundAlertsDesc: String
  val settingsMediaFluxSection: String
  val settingsFluxStartMuted: String
  val settingsFluxStartMutedDesc: String
  val settingsFluxAutoDownload: String
  val settingsFluxAutoDownloadDesc: String
  val settingsCallSpeakerDefault: String
  val settingsCallSpeakerDefaultDesc: String
  val settingsCompactLayout: String
  val settingsCompactLayoutDesc: String
  val settingsClearCache: String
  val settingsClearCacheDesc: String
  val settingsCacheClearedToast: String
  val settingsCacheSizeFormat: String
  val settingsAboutMeLabel: String
  val settingsFullSettingsCardTitle: String
  val settingsFullSettingsCardDesc: String
  val settingsOpenButton: String

  // Cross-User Profile Inspector
  val inspectProfileAction: String
  val inspectProfileBioEmpty: String
  val inspectProfilePostsCount: String
  val inspectProfileFluxCount: String
  val inspectProfileStatsTitle: String
  val inspectProfileSelfBadge: String
  val inspectProfileHandleCopied: String
  val inspectProfileDirectMessage: String
  val inspectProfileCall: String
  val inspectProfileLastSeenRecently: String
  val inspectProfileLastSeenAt: String
  val inspectProfileMemberTitle: String

  // Flux Sync & Error Recovery
  val refreshFluxFeed: String
  val fluxVideoLoadFailed: String
  val fluxTapToLoadVideo: String
  val fluxVideoTooLargeInline: String

  // Messenger-Beating Chat Upgrades
  val voiceNoteLabel: String
  val recordingVoiceNote: String
  val cancelRecording: String
  val sendVoiceNote: String
  val createPollAction: String
  val pollQuestionLabel: String
  val pollQuestionPlaceholder: String
  val pollOptionLabel: String
  val addPollOption: String
  val sendPoll: String
  val pollVotesCountFormat: String
  val pinMessageAction: String
  val unpinMessageAction: String
  val pinnedMessageBannerTitle: String
  val messagePinnedToast: String
  val messageUnpinnedToast: String
  val forwardAction: String
  val forwardToTitle: String
  val forwardedFromLabel: String
  val messageForwardedToast: String
  val searchMessagesPlaceholder: String
  val searchResultsCountFormat: String
  val spoilerToggleLabel: String
  val spoilerToggleDesc: String
  val spoilerHiddenTitle: String
  val spoilerTapToReveal: String
  val chatInfoTitle: String
  val sharedMediaTabPhotos: String
  val sharedMediaTabVideos: String
  val sharedMediaTabAudio: String
  val sharedMediaTabFiles: String
  val sharedMediaTabPolls: String
  val noSharedMediaYet: String
  val membersCountFormat: String

  // Focused Auth (Sign In / Create Account / Forgot Password / Recover Account)
  val authSignInTab: String
  val authCreateAccountTab: String
  val authEmailLabel: String
  val authPasswordLabel: String
  val authConfirmPasswordLabel: String
  val authForgotPassword: String
  val authRecoverAccount: String
  val authOrDivider: String
  val authCreatingAccount: String
  val authForgotPasswordTitle: String
  val authForgotPasswordDesc: String
  val authSendResetLink: String
  val authSendingResetLink: String
  val authResetLinkSent: String
  val authRecoverAccountTitle: String
  val authRecoverAccountDesc: String
  val authRecoverViaEmailBtn: String
  val authRecoverViaGoogleBtn: String
  val authBackToSignIn: String
  val authShowPasswordDesc: String
  val authHidePasswordDesc: String
  val authInvalidEmailError: String
  val authWeakPasswordError: String
  val authPasswordMismatchError: String
  val authSignInSubtitle: String
  val chatDateToday: String
  val chatDateYesterday: String
}

object EnglishStrings : KoraStrings {
  override val appName = "Kora"
  override val tagline = "Editorial communication & thoughtful social network"
  override val signInWithGoogle = "Sign in with Google"
  override val welcomeBack = "Welcome to Kora"
  override val createProfile = "Complete Profile"
  override val enterDisplayName = "Display Name"
  override val enterHandle = "Unique Handle (e.g. @sarah)"
  override val enterBio = "Bio or current focus"
  override val saveProfile = "Continue"
  override val signingIn = "Signing in..."
  override val navChats = "Messages"
  override val navFeed = "Feed"
  override val navMemory = "Memory"
  override val navProfile = "Profile"
  override val navSettings = "Settings"
  override val conversations = "Conversations"
  override val searchChats = "Search conversations..."
  override val directChats = "Direct"
  override val groupChats = "Groups"
  override val newChat = "New Message"
  override val newGroup = "New Group"
  override val noChatsYet = "No conversations yet"
  override val startFirstConversation = "Search for a user by handle to start an exchange."
  override val typeMessage = "Type a message..."
  override val send = "Send"
  override val reply = "Reply"
  override val replyingTo = "Replying to"
  override val edit = "Edit"
  override val delete = "Delete"
  override val copy = "Copy text"
  override val messageCopied = "Message copied to clipboard"
  override val saveToMemory = "Save to Memory"
  override val pinChat = "Pin Chat"
  override val unpinChat = "Unpin Chat"
  override val archiveChat = "Archive Chat"
  override val unarchiveChat = "Unarchive"
  override val muteChat = "Mute"
  override val unmuteChat = "Unmute"
  override val edited = "(edited)"
  override val messageDeleted = "This message was deleted"
  override val feedTitle = "Social Feed"
  override val whatsOnYourMind = "Share a thought, inquiry, or update..."
  override val publish = "Publish"
  override val audiencePublic = "Public"
  override val audienceCircles = "Circles Only"
  override val filterAll = "All Feed"
  override val filterCircles = "Close Circles"
  override val contextThreads = "Context Thread"
  override val viewThread = "View Thread"
  override val writeAComment = "Add to discussion..."
  override val noPostsYet = "No publications in this circle"
  override val beTheFirstToShare = "Write a thoughtful post to start the timeline."
  override val likes = "Likes"
  override val comments = "Threads"
  override val memoryTitle = "Personal Memory"
  override val memorySubtitle = "Saved insights, references & private notes"
  override val noSavedMemories = "Your memory vault is empty"
  override val memoryTip = "Long-press any message in a chat and select 'Save to Memory'."
  override val searchMemory = "Search your saved knowledge..."
  override val addPrivateNote = "Add Private Note"
  override val notePlaceholder = "Personal thoughts, takeaways, or reminders (only visible to you)..."
  override val saveNote = "Save Note"
  override val memorySaved = "Saved to personal memory"
  override val profileTitle = "Identity & Presence"
  override val quietPresence = "Quiet Presence"
  override val quietModeDesc = "Signal focus to peers while keeping notifications subdued"
  override val online = "Active"
  override val quietMode = "Quiet Mode"
  override val invisible = "Invisible"
  override val readReceipts = "Read Receipts"
  override val readReceiptsDesc = "Allow others to see when you have read messages"
  override val signOut = "Sign Out"
  override val settingsTitle = "Preferences"
  override val appearance = "Appearance"
  override val themeLight = "Light Mode"
  override val themeDark = "Dark Mode"
  override val themeSystem = "System Default"
  override val language = "Language"
  override val english = "English"
  override val arabic = "العربية (Arabic)"
  override val cancel = "Cancel"
  override val confirm = "Confirm"
  override val ok = "OK"
  override val errorOccurred = "An unexpected error occurred"
  override val retry = "Retry"
  override val storeTitle = "Custom Decorations"
  override val customDecoration = "Custom Decoration"
  override val buyCoins = "Buy Coins"
  override val earnCoins = "Earn Coins"
  override val avatarFrames = "Avatar Frames"
  override val roleNameplates = "Role Nameplates"
  override val achievementWalls = "Achievement Walls"
  override val chatBubbles = "Chat Bubbles"
  override val allCategories = "All"
  override val preview = "Preview"
  override val equip = "Equip"
  override val equipped = "Equipped"
  override val unequip = "Unequip"
  override val buyFor = "Buy for"
  override val notEnoughCoins = "Insufficient coins balance"
  override val purchasedSuccess = "Item acquired and equipped!"
  override val dailyCheckIn = "Daily Check-In"
  override val dailyCheckInDesc = "Collect daily coins to unlock bespoke aesthetic frames"
  override val claimCoins = "Claim +250 Coins"
  override val coinsClaimed = "Claimed Today"
  override val shopBannerText = "Show your identity with style"
  override val buyCoinsDesc = "Instant coin top-ups for luxury avatar frames and badges"
  override val packStarter = "Novice Cache (500 Coins)"
  override val packPopular = "Artisan Vault (2,000 Coins)"
  override val packElite = "Imperial Hoard (5,000 Coins)"
  override val verifiedAccount = "Verified Account"
  override val verificationBadge = "Blue Verification Badge"
  override val verifiedBadgeSubtitle = "Official identity verification & trust"
  override val verifiedStatusActive = "Verified Account"
  override val verifiedStatusInactive = "Unverified"
  override val toggleVerification = "Enable Blue Verification Badge"
  override val verificationDetails = "The blue badge authenticates notable creators and verified members, granting distinct visual trust and priority across Kora."
  override val verifiedDialogTitle = "Official Verification"
  override val verifiedDialogMessage = "This account is officially verified by Kora. Its authentic identity has been validated according to high community standards."
  override val isTyping = "is typing..."
  override val typingSingle = "%s is typing..."
  override val typingMultiple = "Several members are typing..."
  override val navFlux = "Flux"
  override val fluxTitle = "Flux"
  override val fluxSubtitle = "Short videos & community clips"
  override val newFlux = "Post Flux"
  override val publishFlux = "Publish Flux"
  override val selectVideo = "Choose Video"
  override val fluxCaptionPlaceholder = "Write a caption & tags... #kora #flux #design"
  override val fluxAudioPlaceholder = "Soundtrack or original audio name"
  override val noFluxYet = "No Flux Videos Yet"
  override val beFirstToPublishFlux = "Be the first to share an inspiring Flux clip!"
  override val fluxPublished = "Flux clip published successfully!"
  override val fluxCommentsTitle = "Flux Comments"
  override val addFluxComment = "Add a comment..."
  override val themeSwitcherTitle = "Theme Studio"
  override val themePresets = "Theme Presets"
  override val customAccentColor = "Custom Accent Color"
  override val amoledMode = "AMOLED Pure Black"
  override val amoledModeDesc = "Pure pitch black for deeper contrast and OLED battery savings"
  override val themePreview = "Live Theme Preview"
  override val livePreviewChannel = "kora-lounge"
  override val livePreviewSampleMessage = "This is how your Kora conversations, headers, and badges look!"
  override val quickThemeSwitcher = "Themes"
  override val changeAvatarTitle = "Change Avatar"
  override val changeAvatarSubtitle = "Pick from gallery, Kora curated portraits, or custom URL"
  override val chooseFromGallery = "Choose from Gallery"
  override val curatedAvatars = "Curated Kora Avatars"
  override val customImageUrl = "Custom Image URL"
  override val enterImageUrlPlaceholder = "https://example.com/avatar.png"
  override val applyUrl = "Apply Link"
  override val resetToInitial = "Reset to Default Letter"
  override val avatarUpdatedSuccess = "Profile picture updated successfully!"
  override val previewAvatar = "Preview Avatar"
  override val contactDirectoryTitle = "Contact Directory"
  override val contactDirectorySubtitle = "Find colleagues and members by name or profession"
  override val searchByNameOrProfession = "Search by name or profession..."
  override val allProfessions = "All Professions"
  override val contactsCount = "%d contacts found"
  override val clearSearch = "Clear Search"
  override val noContactsFound = "No Contacts Found"
  override val noContactsFoundDesc = "Try searching with a different name or profession keyword"
  override val verifiedProTitle = "Verified Professional"
  override val verifiedProBadge = "Verified Pro"
  override val verifiedProSubtitle = "Authenticated credentials & domain expertise"
  override val openToNetworking = "Open for Networking & Projects"
  override val professionalCredentialsTitle = "Verified Professional Status"
  override val verifiedProDialogDesc = "This member holds authenticated professional status across the Kora networking directory. Identity, role specialization, and domain competence have been validated for trusted peer networking."
  override val filterVerifiedProOnly = "Verified Pros"
  override val aiAssistantName = "Kora AI"
  override val aiAssistantSubtitle = "AI Assistant • Gemini"
  override val aiWelcomeTitle = "How can I help you today?"
  override val aiWelcomeDesc = "Ask anything, draft messages, brainstorm ideas, or summarize notes in a calm workspace."
  override val aiPromptOne = "Summarize my ideas cleanly"
  override val aiPromptTwo = "Draft a polite professional message"
  override val aiPromptThree = "Give me 3 creative project ideas"
  override val aiClearChat = "New Conversation"
  override val aiBotBadge = "AI"
  override val aiThinking = "Thinking..."
  override val aiErrorBannerTitle = "Could not reach Kora AI"
  override val enterProfession = "Profession / Specialty (optional)"
  override val checkingAccount = "Checking session..."
  override val syncingProfile = "Syncing your cloud profile..."
  override val openToNetworkingToggle = "Open for professional networking"
  override val profileUpdated = "Profile updated"
  override val editProfile = "Edit Profile"
  override val quietFocusActive = "Active focus"
  override val standardAlerts = "Standard alerts"
  override val getPack = "Top Up"
  override val coinsUnit = "Coins"
  override val fromLabel = "From"
  override val privateNoteLabel = "Private Note"
  override val loading = "Loading..."
  override val aboutKoraTitle = "About Kora"
  override val aboutKoraDesc = "Crafted for thoughtful conversations with quiet presence, focused threads, and instant cloud synchronization."
  override val activeChatsCount = "%d active"
  override val publishedSuccess = "Published"
  override val groupNameLabel = "Group Name"
  override val selectedLabel = "Selected"
  override val questsTab = "Quests"
  override val questsSubtitle = "Complete real profile & community milestones to earn coins"
  override val claimQuest = "Claim"
  override val questClaimed = "Claimed"
  override val questProfileTitle = "Complete Your Profile"
  override val questProfileDesc = "Add a bio or profession so members can discover you."
  override val questVerifiedTitle = "Verify Your Identity"
  override val questVerifiedDesc = "Activate your official verification badge on your profile."
  override val questFluxTitle = "Publish a Flux Clip"
  override val questFluxDesc = "Share your first video or ambient clip on Flux."
  override val questAvatarTitle = "Customize Your Avatar"
  override val questAvatarDesc = "Set a custom profile photo or curated avatar."
  override val dailyCooldownFormat = "Available in %s"
  override val verificationEnableSuccess = "Verification badge activated and synced to your profile"
  override val verificationDisableSuccess = "Verification badge disabled"
  override val verificationError = "Could not update verification badge"
  override val chatDeliveryError = "Could not deliver message"
  override val fluxFilterAll = "All"
  override val fluxFilterSaved = "Saved"
  override val noSavedFluxYet = "No Saved Clips Yet"
  override val noSavedFluxDesc = "Tap the bookmark button on any Flux clip to save it here."
  override val videoSavedToast = "Video saved to your collection"
  override val videoUnsavedToast = "Removed from saved videos"
  override val saveToGallery = "Save to Gallery"
  override val savedToGalleryToast = "Video saved to device gallery"
  override val gallerySaveFailedToast = "Could not export video to gallery"
  override val fluxLinkCopied = "Flux link copied to clipboard"
  override val fluxAmbientStyle = "Ambient Visual Style"
  override val videoSelectedDevice = "Video selected from device"
  override val tapToChooseVideo = "Tap to choose video from device"
  override val noCommentsYet = "No comments yet. Be the first!"
  override val startVoiceCall = "Start voice call"
  override val voiceCallLabel = "Voice Call"
  override val callQualityHdBadge = "HD Opus • 48kHz"
  override val callRinging = "Ringing..."
  override val callIncoming = "Incoming voice call"
  override val callConnecting = "Connecting secure audio..."
  override val callActive = "Connected"
  override val callEnded = "Call ended"
  override val callDeclined = "Call declined"
  override val callBusyOrFailed = "Could not connect call"
  override val acceptCall = "Accept"
  override val declineCall = "Decline"
  override val endCall = "End Call"
  override val muteMic = "Mute"
  override val unmuteMic = "Unmute"
  override val speakerOn = "Speaker"
  override val speakerOff = "Earpiece"
  override val minimizeCall = "Minimize"
  override val returnToCall = "Tap to return to call"
  override val micPermissionRequired = "Microphone permission is required for voice calls"
  override val attachMedia = "Send photo, video or file"
  override val attachPhoto = "Photo"
  override val attachHdVideo = "HD Video"
  override val attachFile = "Document / File"
  override val uploadingMediaProgress = "Sending %d%%"
  override val downloadingMedia = "Loading media..."
  override val mediaUploadFailed = "Could not send attachment"
  override val mediaDownloadFailed = "Could not load attachment"
  override val fileTooLargeLimit = "Selected file exceeds the 25 MB limit"
  override val tapToPlayInline = "Play"
  override val hdBadge = "HD"
  override val fullscreenVideo = "Fullscreen"
  override val openFileAction = "Open"
  override val saveFileAction = "Save"
  override val savedImageToast = "Photo saved to device gallery"
  override val savedFileToast = "File saved to Downloads"
  override val noAppToOpenFile = "No installed app can open this file"
  override val newMessageChannelName = "New Messages"
  override val newMessageChannelDesc = "Instant notifications when you receive a new message"
  override val newMessageNotificationTitle = "You have a new message"
  override val newMessageFromTitle = "New message from %s"
  override val tapToOpenChat = "Tap to open chat"
  override val splashEditorialLine = "Quiet Luxury • Real Connection"
  override val splashTapToSkip = "Tap to enter"
  override val welcomeKicker = "VOICE • CINEMA • CIRCLES"
  override val welcomeHeadline = "Your space, dialed in."
  override val welcomeSubheadline = "Crystal-clear 48kHz voice calls, inline HD video cinema, and calm social circles in obsidian glass."
  override val pickYourVibe = "Pick your vibe"
  override val amoledChipLabel = "OLED Black"
  override val featureVoiceHdTitle = "HD Voice"
  override val featureVoiceHdDesc = "48kHz Opus calls with crisp clarity and instant echo cancellation."
  override val featureCinemaTitle = "In-Chat Cinema"
  override val featureCinemaDesc = "Watch HD clips, share photos, and exchange files right inside chat."
  override val featurePulseTitle = "Alerts & Flux"
  override val featurePulseDesc = "Instant message notifications paired with short-form Flux reels."
  override val switchGoogleAccount = "Choose another Google account"
  override val privacySecureBadge = "Secured with Google Identity & Firebase Cloud"
  override val chooseAvatarOptional = "Choose your look"
  override val googlePhotoOption = "Default"
  override val quickRoleLabel = "Quick vibe / Specialty"
  override val quickRoleCreator = "Creator"
  override val quickRoleDeveloper = "Developer"
  override val quickRoleDesigner = "Designer"
  override val quickRoleGamer = "Gamer"
  override val quickRoleStudent = "Student"
  override val quickRoleFounder = "Founder"
  override val startQuietModeToggle = "Start in Quiet Focus Mode"
  override val startQuietModeDesc = "Keep alerts soft while you settle into Kora"
  override val setupStepSubtitle = "Customize your look and vibe before entering"
  override val filterArchived = "Archived"
  override val reactAction = "React"
  override val emojiPickerTitle = "Expressive & Weird Reactions"
  override val weirdReactionsTab = "Weird & Surreal"
  override val classicReactionsTab = "Classic"
  override val signalReactionsTab = "Signals & Vibes"
  override val moreReactionsDesc = "Choose from all iPhone-style expressive & weird emojis"
  override val postCommentsTitle = "Comments"
  override val noPostCommentsYet = "No comments yet — start the conversation"
  override val uploadingFluxVideoProgress = "Uploading video %d%%"
  override val fluxSaveShort = "Save"
  override val settingsAccountSection = "Account & Identity"
  override val settingsEditIdentity = "Edit Profile Details"
  override val settingsEditIdentityDesc = "Display name, @handle, bio, and specialty"
  override val settingsAccountEmailLabel = "Signed-in Account"
  override val settingsPrivacySection = "Privacy & Presence"
  override val settingsNotificationsSection = "Notifications & Calls"
  override val settingsInAppBanners = "In-App Message Banners"
  override val settingsInAppBannersDesc = "Show floating preview banner when a message arrives"
  override val settingsSoundAlerts = "Alert Sounds & Vibration"
  override val settingsSoundAlertsDesc = "Play sound and haptics for new messages"
  override val settingsMediaFluxSection = "Flux, Calls & Storage"
  override val settingsFluxStartMuted = "Start Flux Videos Muted"
  override val settingsFluxStartMutedDesc = "Keep audio muted when opening Flux reels"
  override val settingsFluxAutoDownload = "Auto-Load Flux Videos"
  override val settingsFluxAutoDownloadDesc = "Automatically buffer shared cloud Flux clips"
  override val settingsCallSpeakerDefault = "Default to Speakerphone"
  override val settingsCallSpeakerDefaultDesc = "Enable speaker automatically when joining voice calls"
  override val settingsCompactLayout = "Compact Chat List"
  override val settingsCompactLayoutDesc = "Tighter vertical density across conversation rows"
  override val settingsClearCache = "Clear Media Cache"
  override val settingsClearCacheDesc = "Free temporary Flux videos and downloaded files"
  override val settingsCacheClearedToast = "Cleared %.1f MB of cached media"
  override val settingsCacheSizeFormat = "%.1f MB used"
  override val settingsAboutMeLabel = "ABOUT ME"
  override val settingsFullSettingsCardTitle = "All Settings"
  override val settingsFullSettingsCardDesc = "Account, privacy, calls, notifications & media"
  override val settingsOpenButton = "Manage"
  override val inspectProfileAction = "Inspect Profile"
  override val inspectProfileBioEmpty = "No bio added yet."
  override val inspectProfilePostsCount = "Posts"
  override val inspectProfileFluxCount = "Flux Clips"
  override val inspectProfileStatsTitle = "Community Activity"
  override val inspectProfileSelfBadge = "This is your public profile card"
  override val inspectProfileHandleCopied = "Handle copied to clipboard"
  override val inspectProfileDirectMessage = "Message"
  override val inspectProfileCall = "HD Voice Call"
  override val inspectProfileLastSeenRecently = "Last seen recently"
  override val inspectProfileLastSeenAt = "Last seen %s"
  override val inspectProfileMemberTitle = "Profile Inspector"
  override val refreshFluxFeed = "Refresh Feed"
  override val fluxVideoLoadFailed = "Could not load video"
  override val fluxTapToLoadVideo = "Tap to load video"
  override val fluxVideoTooLargeInline = "Video exceeds the 25 MB upload limit. Pick a shorter clip."
  override val voiceNoteLabel = "Voice Note"
  override val recordingVoiceNote = "Recording voice note..."
  override val cancelRecording = "Cancel"
  override val sendVoiceNote = "Send Voice"
  override val createPollAction = "Create Poll"
  override val pollQuestionLabel = "Poll Question"
  override val pollQuestionPlaceholder = "Ask something..."
  override val pollOptionLabel = "Option %d"
  override val addPollOption = "Add Option"
  override val sendPoll = "Send Poll"
  override val pollVotesCountFormat = "%d votes"
  override val pinMessageAction = "Pin Message"
  override val unpinMessageAction = "Unpin Message"
  override val pinnedMessageBannerTitle = "Pinned Message"
  override val messagePinnedToast = "Message pinned to top of chat"
  override val messageUnpinnedToast = "Pinned message removed"
  override val forwardAction = "Forward"
  override val forwardToTitle = "Forward Message To"
  override val forwardedFromLabel = "Forwarded from %s"
  override val messageForwardedToast = "Message forwarded"
  override val searchMessagesPlaceholder = "Search in conversation..."
  override val searchResultsCountFormat = "%d / %d"
  override val spoilerToggleLabel = "Spoiler"
  override val spoilerToggleDesc = "Blur message until tapped"
  override val spoilerHiddenTitle = "Spoiler Message"
  override val spoilerTapToReveal = "Tap to reveal"
  override val chatInfoTitle = "Chat Info & Media"
  override val sharedMediaTabPhotos = "Photos"
  override val sharedMediaTabVideos = "Videos"
  override val sharedMediaTabAudio = "Voice"
  override val sharedMediaTabFiles = "Files"
  override val sharedMediaTabPolls = "Polls"
  override val noSharedMediaYet = "Nothing shared here yet"
  override val membersCountFormat = "%d members"

  override val authSignInTab = "Sign In"
  override val authCreateAccountTab = "Create Account"
  override val authEmailLabel = "Email Address"
  override val authPasswordLabel = "Password"
  override val authConfirmPasswordLabel = "Confirm Password"
  override val authForgotPassword = "Forgot password?"
  override val authRecoverAccount = "Recover Account"
  override val authOrDivider = "or"
  override val authCreatingAccount = "Creating account..."
  override val authForgotPasswordTitle = "Forgot Password"
  override val authForgotPasswordDesc = "Enter your registered email and we will send you a password reset link."
  override val authSendResetLink = "Send Reset Link"
  override val authSendingResetLink = "Sending link..."
  override val authResetLinkSent = "Reset link sent to your email. Check your inbox or spam folder."
  override val authRecoverAccountTitle = "Recover Account"
  override val authRecoverAccountDesc = "Restore access to your account via email or your linked Google account."
  override val authRecoverViaEmailBtn = "Recover via Email"
  override val authRecoverViaGoogleBtn = "Restore with Google"
  override val authBackToSignIn = "Back to Sign In"
  override val authShowPasswordDesc = "Show password"
  override val authHidePasswordDesc = "Hide password"
  override val authInvalidEmailError = "Please enter a valid email address."
  override val authWeakPasswordError = "Password must be at least 6 characters."
  override val authPasswordMismatchError = "Passwords do not match."
  override val authSignInSubtitle = "Sign in, create an account, or recover access."
  override val chatDateToday = "Today"
  override val chatDateYesterday = "Yesterday"
}

object ArabicStrings : KoraStrings {
  override val appName = "كورا"
  override val tagline = "منصة تواصل راقية وشبكة اجتماعية هادئة"
  override val signInWithGoogle = "تسجيل الدخول باستخدام Google"
  override val welcomeBack = "مرحبًا بك في كورا"
  override val createProfile = "إكمال الملف التعريفي"
  override val enterDisplayName = "الاسم الظاهر"
  override val enterHandle = "المعرّف الفريد (مثال: @ahmed)"
  override val enterBio = "نبذة شخصية أو مجال الاهتمام"
  override val saveProfile = "متابعة"
  override val signingIn = "جارٍ تسجيل الدخول..."
  override val navChats = "الرسائل"
  override val navFeed = "الموجز"
  override val navMemory = "الذاكرة"
  override val navProfile = "الملف"
  override val navSettings = "الإعدادات"
  override val conversations = "المحادثات"
  override val searchChats = "البحث في المحادثات..."
  override val directChats = "خاصة"
  override val groupChats = "مجموعات"
  override val newChat = "محادثة جديدة"
  override val newGroup = "مجموعة جديدة"
  override val noChatsYet = "لا توجد محادثات حتى الآن"
  override val startFirstConversation = "ابحث عن مستخدم عبر المعرّف لبدء تواصل فوري."
  override val typeMessage = "اكتب رسالة..."
  override val send = "إرسال"
  override val reply = "رد"
  override val replyingTo = "ردًا على"
  override val edit = "تعديل"
  override val delete = "حذف"
  override val copy = "نسخ النص"
  override val messageCopied = "تم نسخ الرسالة إلى الحافظة"
  override val saveToMemory = "حفظ في الذاكرة"
  override val pinChat = "تثبيت المحادثة"
  override val unpinChat = "إلغاء التثبيت"
  override val archiveChat = "أرشفة"
  override val unarchiveChat = "إلغاء الأرشفة"
  override val muteChat = "كتم التنبيهات"
  override val unmuteChat = "إلغاء الكتم"
  override val edited = "(معدلة)"
  override val messageDeleted = "تم حذف هذه الرسالة"
  override val feedTitle = "الموجز الاجتماعي"
  override val whatsOnYourMind = "شارك فكرة أو استفسارًا أو ملاحظة..."
  override val publish = "نشر"
  override val audiencePublic = "عام"
  override val audienceCircles = "الدوائر المقربة"
  override val filterAll = "الكل"
  override val filterCircles = "الدوائر المقربة"
  override val contextThreads = "خيوط النقاش"
  override val viewThread = "عرض النقاش"
  override val writeAComment = "أضف تعليقًا للنقاش..."
  override val noPostsYet = "لا توجد منشورات في هذه الدائرة"
  override val beTheFirstToShare = "كن أول من يشارك فكرة قيّمة لإثراء الموجز."
  override val likes = "إعجابات"
  override val comments = "خيوط"
  override val memoryTitle = "ذاكرة الرسائل"
  override val memorySubtitle = "الرسائل المحفوظة والملاحظات الشخصية الخاصة"
  override val noSavedMemories = "خزينة الذاكرة فارغة"
  override val memoryTip = "اضغط مطولًا على أي رسالة في المحادثة واختر 'حفظ في الذاكرة'."
  override val searchMemory = "ابحث في معرفتك المحفوظة..."
  override val addPrivateNote = "إضافة ملاحظة خاصة"
  override val notePlaceholder = "ملاحظات شخصية واستخلاصات (مرئية لك فقط)..."
  override val saveNote = "حفظ الملاحظة"
  override val memorySaved = "تم الحفظ في ذاكرتك الخاصة"
  override val profileTitle = "الهوية والحضور"
  override val quietPresence = "الوضع الهادئ"
  override val quietModeDesc = "إشعار زملائك بالتركيز مع كتم التنبيهات المزعجة"
  override val online = "نشط"
  override val quietMode = "وضع التركيز"
  override val invisible = "غير مرئي"
  override val readReceipts = "مؤشرات القراءة"
  override val readReceiptsDesc = "السماح للآخرين بمعرفة وقت قراءتك للرسائل"
  override val signOut = "تسجيل الخروج"
  override val settingsTitle = "التفضيلات"
  override val appearance = "المظهر"
  override val themeLight = "الوضع الفاتح"
  override val themeDark = "الوضع الداكن"
  override val themeSystem = "تلقائي حسب النظام"
  override val language = "اللغة"
  override val english = "English (الإنجليزية)"
  override val arabic = "العربية"
  override val cancel = "إلغاء"
  override val confirm = "تأكيد"
  override val ok = "حسناً"
  override val errorOccurred = "حدث خطأ غير متوقع"
  override val retry = "إعادة المحاولة"
  override val storeTitle = "متجر الزينة"
  override val customDecoration = "الزينة المخصصة"
  override val buyCoins = "شراء العملات"
  override val earnCoins = "كسب العملات"
  override val avatarFrames = "إطار"
  override val roleNameplates = "لوحات أسماء الأدوار"
  override val achievementWalls = "جدار الإنجازات"
  override val chatBubbles = "فقاعات المحادثة"
  override val allCategories = "الكل"
  override val preview = "معاينة"
  override val equip = "تجهيز"
  override val equipped = "مجهّز حالياً"
  override val unequip = "إلغاء التجهيز"
  override val buyFor = "شراء بـ"
  override val notEnoughCoins = "رصيد العملات غير كافٍ لهذا العنصر"
  override val purchasedSuccess = "تم شراء العنصر وتجهيزه بنجاح!"
  override val dailyCheckIn = "تسجيل الدخول اليومي"
  override val dailyCheckInDesc = "استلم مكافأتك اليومية المجانية من العملات لفتح إطارات فاخرة"
  override val claimCoins = "استلم +250 عملة"
  override val coinsClaimed = "تم استلام مكافأة اليوم"
  override val shopBannerText = "أبرز هويتك بأسلوبك الراقي والفريد"
  override val buyCoinsDesc = "شحن فوري لرصيد العملات لفتح الإطارات واللوحات الفاخرة"
  override val packStarter = "باقة المبتدئ (500 عملة)"
  override val packPopular = "باقة الحِرفي (2,000 عملة)"
  override val packElite = "خزينة الإمبراطور (5,000 عملة)"
  override val verifiedAccount = "حساب موثق"
  override val verificationBadge = "شارة التوثيق"
  override val verifiedBadgeSubtitle = "توثيق رسمي للهوية وموثوقية الحساب"
  override val verifiedStatusActive = "حساب موثق رسمياً"
  override val verifiedStatusInactive = "غير مفعل"
  override val toggleVerification = "تفعيل شارة التوثيق"
  override val verificationDetails = "تمنحك شارة التوثيق ظهوراً مميزاً وثقة رسمية وأولوية واضحة في المحادثات وقوائم الموجز على منصة كورا."
  override val verifiedDialogTitle = "توثيق رسمي ومعتمد"
  override val verifiedDialogMessage = "هذا الحساب تم التحقق من هويته ومطابقته لمعايير الأمان والإنتاجية العالية في منصة كورا."
  override val isTyping = "يكتب الآن..."
  override val typingSingle = "%s يكتب الآن..."
  override val typingMultiple = "عدة أشخاص يكتبون الآن..."
  override val navFlux = "فلوكس"
  override val fluxTitle = "فلوكس"
  override val fluxSubtitle = "فيديوهات قصيرة ومقاطع مجتمعية"
  override val newFlux = "نشر فلوكس"
  override val publishFlux = "نشر مقطع فلوكس"
  override val selectVideo = "اختيار فيديو"
  override val fluxCaptionPlaceholder = "اكتب وصفاً مع الوسوم... #فلوكس #كورا #تصميم"
  override val fluxAudioPlaceholder = "اسم المسار الصوتي أو الصوت الأصلي"
  override val noFluxYet = "لا توجد فيديوهات فلوكس بعد"
  override val beFirstToPublishFlux = "كن أول من ينشر مقطع فلوكس مميز!"
  override val fluxPublished = "تم نشر مقطع الفلوكس بنجاح!"
  override val fluxCommentsTitle = "تعليقات فلوكس"
  override val addFluxComment = "أضف تعليقاً..."
  override val themeSwitcherTitle = "استوديو السمات"
  override val themePresets = "السمات الجاهزة"
  override val customAccentColor = "لون التمييز المخصص"
  override val amoledMode = "سواد AMOLED النقي"
  override val amoledModeDesc = "سواد عميق 100% يريح العين ويوفر طاقة البطارية"
  override val themePreview = "معاينة السمة الحية"
  override val livePreviewChannel = "kora-lounge"
  override val livePreviewSampleMessage = "هكذا ستبدو محادثات كورا والعناوين وشارات التوثيق مع هذه السمة!"
  override val quickThemeSwitcher = "السمات"
  override val changeAvatarTitle = "تغيير الصورة"
  override val changeAvatarSubtitle = "اختر من معرض الصور أو مجموعة كورا الفاخرة أو رابط خارجي"
  override val chooseFromGallery = "اختيار من المعرض"
  override val curatedAvatars = "صور كورا الرمزية الفاخرة"
  override val customImageUrl = "رابط صورة مخصص"
  override val enterImageUrlPlaceholder = "https://example.com/avatar.png"
  override val applyUrl = "تطبيق الرابط"
  override val resetToInitial = "إعادة تعيين للحرف الافتراضي"
  override val avatarUpdatedSuccess = "تم تحديث صورة الملف الشخصي بنجاح!"
  override val previewAvatar = "معاينة الصورة"
  override val contactDirectoryTitle = "دليل جهات الاتصال"
  override val contactDirectorySubtitle = "ابحث عن الأعضاء والزملاء بالاسم أو المهنة والتخصص"
  override val searchByNameOrProfession = "ابحث بالاسم أو المهنة أو التخصص..."
  override val allProfessions = "جميع المهن"
  override val contactsCount = "%d جهات اتصال متطابقة"
  override val clearSearch = "مسح البحث"
  override val noContactsFound = "لم يتم العثور على جهات اتصال"
  override val noContactsFoundDesc = "جرّب البحث باسم آخر أو مسمى مهني مختلف"
  override val verifiedProTitle = "محترف موثق ومعتمد"
  override val verifiedProBadge = "محترف موثق"
  override val verifiedProSubtitle = "خبرة مهنية واعتماد رسمي موثق"
  override val openToNetworking = "متاح للتواصل المهني وفرص العمل"
  override val professionalCredentialsTitle = "حالة الاعتماد والتوثيق المهني"
  override val verifiedProDialogDesc = "يحمل هذا العضو شارة المحترف الموثق في شبكة كورا للتواصل المهني. تم التحقق من هويته ومسماه المهني وتخصصه لضمان شبكة علاقات عمل موثوقة وعالية المستوى."
  override val filterVerifiedProOnly = "محترفون موثقون"
  override val aiAssistantName = "مساعد كورا الذكي"
  override val aiAssistantSubtitle = "مساعد ذكي • Gemini"
  override val aiWelcomeTitle = "كيف يمكنني مساعدتك اليوم؟"
  override val aiWelcomeDesc = "اسألني عن أي شيء، أو اطلب صياغة رسائل، أو تلخيص أفكار في مساحة هادئة ومريحة للعين."
  override val aiPromptOne = "لخّص لي أفكاري بشكل مرتب"
  override val aiPromptTwo = "اكتب لي رسالة احترافية بأسلوب راقٍ"
  override val aiPromptThree = "اقترح 3 أفكار إبداعية لمشروعي"
  override val aiClearChat = "محادثة جديدة"
  override val aiBotBadge = "AI"
  override val aiThinking = "يفكر الآن..."
  override val aiErrorBannerTitle = "تعذر الاتصال بالمساعد الذكي"
  override val enterProfession = "المهنة أو التخصص (اختياري)"
  override val checkingAccount = "جارٍ التحقق من الجلسة..."
  override val syncingProfile = "جارٍ مزامنة ملفك السحابي..."
  override val openToNetworkingToggle = "الظهور كمتاح للتواصل المهني"
  override val profileUpdated = "تم تحديث الملف الشخصي"
  override val editProfile = "تعديل الملف الشخصي"
  override val quietFocusActive = "التركيز الهادئ نشط"
  override val standardAlerts = "التنبيهات الاعتيادية"
  override val getPack = "شحن"
  override val coinsUnit = "عملة"
  override val fromLabel = "من"
  override val privateNoteLabel = "ملاحظة خاصة"
  override val loading = "جارٍ التحميل..."
  override val aboutKoraTitle = "عن منصة كورا"
  override val aboutKoraDesc = "صُممت لتواصل راقٍ وهادئ بخيوط نقاش منظمة ومزامنة سحابية فورية خالية من الضوضاء."
  override val activeChatsCount = "%d محادثات نشطة"
  override val publishedSuccess = "تم النشر"
  override val groupNameLabel = "اسم المجموعة"
  override val selectedLabel = "محدد"
  override val questsTab = "المهام"
  override val questsSubtitle = "أكمل المهام الحقيقية في حسابك والمجتمع لكسب العملات"
  override val claimQuest = "استلام"
  override val questClaimed = "تم الاستلام"
  override val questProfileTitle = "أكمل ملفك الشخصي"
  override val questProfileDesc = "أضف نبذة شخصية أو تخصصك المهني ليظهر للآخرين."
  override val questVerifiedTitle = "وثّق حسابك"
  override val questVerifiedDesc = "فعّل شارة التوثيق الرسمية في شاشة الملف الشخصي."
  override val questFluxTitle = "انشر مقطع فلوكس"
  override val questFluxDesc = "شارك أول مقطع لك في تبويب فلوكس."
  override val questAvatarTitle = "خصّص صورتك الشخصية"
  override val questAvatarDesc = "اختر صورة مخصصة أو رمزية لحسابك."
  override val dailyCooldownFormat = "متاح بعد %s"
  override val verificationEnableSuccess = "تم تفعيل شارة التوثيق ومزامنتها مع حسابك"
  override val verificationDisableSuccess = "تم إلغاء تفعيل شارة التوثيق"
  override val verificationError = "تعذر تحديث حالة التوثيق"
  override val chatDeliveryError = "تعذر إرسال الرسالة"
  override val fluxFilterAll = "الكل"
  override val fluxFilterSaved = "المحفوظة"
  override val noSavedFluxYet = "لا توجد مقاطع محفوظة"
  override val noSavedFluxDesc = "اضغط على أيقونة الحفظ في أي مقطع فلوكس ليظهر هنا."
  override val videoSavedToast = "تم حفظ الفيديو في قائمتك"
  override val videoUnsavedToast = "تمت إزالة الفيديو من المحفوظات"
  override val saveToGallery = "حفظ بالمعرض"
  override val savedToGalleryToast = "تم حفظ الفيديو في معرض الجهاز"
  override val gallerySaveFailedToast = "تعذر حفظ الفيديو في المعرض"
  override val fluxLinkCopied = "تم نسخ رابط المقطع"
  override val fluxAmbientStyle = "نسق بصري متفاعل"
  override val videoSelectedDevice = "تم اختيار الفيديو من الجهاز"
  override val tapToChooseVideo = "اضغط لاختيار فيديو من الجهاز"
  override val noCommentsYet = "لا توجد تعليقات بعد. كن أول من يعلّق!"
  override val startVoiceCall = "بدء مكالمة صوتية"
  override val voiceCallLabel = "مكالمة صوتية"
  override val callQualityHdBadge = "صوت نقي HD • 48kHz"
  override val callRinging = "جاري الرنين..."
  override val callIncoming = "مكالمة صوتية واردة"
  override val callConnecting = "جارٍ تأمين الاتصال الصوتي..."
  override val callActive = "متصل الآن"
  override val callEnded = "انتهت المكالمة"
  override val callDeclined = "تم رفض المكالمة"
  override val callBusyOrFailed = "تعذر إتمام الاتصال"
  override val acceptCall = "رد"
  override val declineCall = "رفض"
  override val endCall = "إنهاء"
  override val muteMic = "كتم الميكروفون"
  override val unmuteMic = "إلغاء الكتم"
  override val speakerOn = "مكبر الصوت"
  override val speakerOff = "سماعة الأذن"
  override val minimizeCall = "تصغير"
  override val returnToCall = "اضغط للعودة إلى المكالمة"
  override val micPermissionRequired = "يلزم منح صلاحية الميكروفون لإجراء المكالمة الصوتية"
  override val attachMedia = "إرسال صورة أو فيديو أو ملف"
  override val attachPhoto = "صورة"
  override val attachHdVideo = "فيديو عالي الجودة"
  override val attachFile = "ملف أو مستند"
  override val uploadingMediaProgress = "جارٍ الإرسال %d%%"
  override val downloadingMedia = "جارٍ تحميل الوسائط..."
  override val mediaUploadFailed = "تعذر إرسال المرفق"
  override val mediaDownloadFailed = "تعذر تحميل المرفق"
  override val fileTooLargeLimit = "حجم الملف يتجاوز الحد المسموح (25 ميجابايت)"
  override val tapToPlayInline = "تشغيل"
  override val hdBadge = "HD"
  override val fullscreenVideo = "ملء الشاشة"
  override val openFileAction = "فتح"
  override val saveFileAction = "حفظ"
  override val savedImageToast = "تم حفظ الصورة في معرض الجهاز"
  override val savedFileToast = "تم حفظ الملف في التنزيلات"
  override val noAppToOpenFile = "لا يوجد تطبيق مثبت لفتح هذا النوع من الملفات"
  override val newMessageChannelName = "الرسائل الجديدة"
  override val newMessageChannelDesc = "تنبيهات فورية عند استلام رسالة جديدة من أصدقائك"
  override val newMessageNotificationTitle = "لديك رسالة جديدة"
  override val newMessageFromTitle = "لديك رسالة جديدة من %s"
  override val tapToOpenChat = "اضغط لفتح المحادثة"
  override val splashEditorialLine = "فخامة هادئة • تواصل حقيقي"
  override val splashTapToSkip = "اضغط للدخول"
  override val welcomeKicker = "مكالمات نقية • سينما • دوائر هادئة"
  override val welcomeHeadline = "مساحتك الخاصة، بأسلوبك."
  override val welcomeSubheadline = "مكالمات صوتية فائقة النقاء، تشغيل فيديو داخل المحادثة، وتجربة عصرية خالية من الضوضاء."
  override val pickYourVibe = "اختر جوّك"
  override val amoledChipLabel = "سواد OLED"
  override val featureVoiceHdTitle = "مكالمات HD"
  override val featureVoiceHdDesc = "صوت نقي بتردد 48kHz مع عزل ضوضاء فوري بين الطرفين."
  override val featureCinemaTitle = "سينما المحادثة"
  override val featureCinemaDesc = "شغّل الفيديوهات عالية الجودة وأرسل الصور والملفات داخل الدردشة."
  override val featurePulseTitle = "فلوكس وإشعارات"
  override val featurePulseDesc = "تنبيهات فورية لكل رسالة جديدة مع مقاطع فلوكس عصرية."
  override val switchGoogleAccount = "اختر حساب Google آخر"
  override val privacySecureBadge = "دخول آمن عبر Google والمزامنة السحابية الفورية"
  override val chooseAvatarOptional = "اختر مظهرك الشخصي"
  override val googlePhotoOption = "الافتراضي"
  override val quickRoleLabel = "اهتمامك أو تخصصك (اختيار سريع)"
  override val quickRoleCreator = "صانع محتوى"
  override val quickRoleDeveloper = "مطوّر"
  override val quickRoleDesigner = "مصمم"
  override val quickRoleGamer = "جيمر"
  override val quickRoleStudent = "طالب"
  override val quickRoleFounder = "رائد أعمال"
  override val startQuietModeToggle = "البدء بوضع التركيز الهادئ"
  override val startQuietModeDesc = "تخفيف حدة التنبيهات منذ لحظة دخولك"
  override val setupStepSubtitle = "رتّب هويتك وأسلوبك قبل دخول كورا"
  override val filterArchived = "المؤرشفة"
  override val reactAction = "تفاعل"
  override val emojiPickerTitle = "تفاعلات غريبة ومميزة"
  override val weirdReactionsTab = "تفاعلات غريبة"
  override val classicReactionsTab = "كلاسيكية"
  override val signalReactionsTab = "إشارات وأجواء"
  override val moreReactionsDesc = "اختر من إموجيات متنوعة وغريبة بأسلوب الآيفون"
  override val postCommentsTitle = "التعليقات"
  override val noPostCommentsYet = "لا توجد تعليقات بعد — كن أول المعلّقين"
  override val uploadingFluxVideoProgress = "جارٍ رفع الفيديو %d%%"
  override val fluxSaveShort = "حفظ"
  override val settingsAccountSection = "الحساب والهوية"
  override val settingsEditIdentity = "تعديل بيانات الحساب"
  override val settingsEditIdentityDesc = "الاسم، المعرّف، النبذة الشخصية، والتخصص"
  override val settingsAccountEmailLabel = "الحساب المسجّل"
  override val settingsPrivacySection = "الخصوصية والحضور"
  override val settingsNotificationsSection = "الإشعارات والمكالمات"
  override val settingsInAppBanners = "إشعارات الرسائل المنبثقة"
  override val settingsInAppBannersDesc = "عرض شريط تنبيه عائم عند وصول رسالة جديدة"
  override val settingsSoundAlerts = "صوت واهتزاز التنبيهات"
  override val settingsSoundAlertsDesc = "تفعيل الصوت والاهتزاز للرسائل الواردة"
  override val settingsMediaFluxSection = "فلوكس والوسائط والتخزين"
  override val settingsFluxStartMuted = "بدء فلوكس بدون صوت"
  override val settingsFluxStartMutedDesc = "كتم الصوت تلقائياً عند فتح مقاطع فلوكس"
  override val settingsFluxAutoDownload = "تحميل تلقائي لمقاطع فلوكس"
  override val settingsFluxAutoDownloadDesc = "تجهيز وتشغيل المقاطع السحابية تلقائياً"
  override val settingsCallSpeakerDefault = "مكبر الصوت تلقائياً"
  override val settingsCallSpeakerDefaultDesc = "تشغيل مكبر الصوت عند بدء أو استقبال مكالمة"
  override val settingsCompactLayout = "عرض مضغوط للمحادثات"
  override val settingsCompactLayoutDesc = "مسافات أكثر إحكاماً لعرض محادثات أكثر"
  override val settingsClearCache = "مسح ذاكرة التخزين المؤقت"
  override val settingsClearCacheDesc = "حذف الفيديوهات والملفات المؤقتة لتوفير المساحة"
  override val settingsCacheClearedToast = "تم مسح %.1f ميجابايت من الملفات المؤقتة"
  override val settingsCacheSizeFormat = "%.1f ميجابايت مستخدمة"
  override val settingsAboutMeLabel = "نبذة عني"
  override val settingsFullSettingsCardTitle = "الإعدادات الشاملة"
  override val settingsFullSettingsCardDesc = "تعديل الحساب، الخصوصية، المكالمات، الإشعارات والوسائط"
  override val settingsOpenButton = "تعديل"
  override val inspectProfileAction = "فحص الحساب"
  override val inspectProfileBioEmpty = "لا توجد نبذة شخصية بعد."
  override val inspectProfilePostsCount = "المنشورات"
  override val inspectProfileFluxCount = "مقاطع فلوكس"
  override val inspectProfileStatsTitle = "النشاط في المجتمع"
  override val inspectProfileSelfBadge = "هذه بطاقة ملفك الشخصي كما يراها الآخرون"
  override val inspectProfileHandleCopied = "تم نسخ المعرّف"
  override val inspectProfileDirectMessage = "مراسلة"
  override val inspectProfileCall = "مكالمة HD"
  override val inspectProfileLastSeenRecently = "آخر ظهور منذ وقت قريب"
  override val inspectProfileLastSeenAt = "آخر ظهور %s"
  override val inspectProfileMemberTitle = "ملف العضو"
  override val refreshFluxFeed = "تحديث الفيديوهات"
  override val fluxVideoLoadFailed = "تعذر تحميل الفيديو"
  override val fluxTapToLoadVideo = "اضغط لتحميل الفيديو"
  override val fluxVideoTooLargeInline = "حجم الفيديو يتجاوز حد 25 ميجابايت. اختر مقطعاً أقصر."
  override val voiceNoteLabel = "رسالة صوتية"
  override val recordingVoiceNote = "جارٍ تسجيل رسالة صوتية..."
  override val cancelRecording = "إلغاء"
  override val sendVoiceNote = "إرسال الصوت"
  override val createPollAction = "إنشاء استطلاع"
  override val pollQuestionLabel = "سؤال الاستطلاع"
  override val pollQuestionPlaceholder = "اطرح سؤالاً للأعضاء..."
  override val pollOptionLabel = "الخيار %d"
  override val addPollOption = "إضافة خيار"
  override val sendPoll = "إرسال الاستطلاع"
  override val pollVotesCountFormat = "%d أصوات"
  override val pinMessageAction = "تثبيت الرسالة"
  override val unpinMessageAction = "إلغاء التثبيت"
  override val pinnedMessageBannerTitle = "رسالة مثبتة"
  override val messagePinnedToast = "تم تثبيت الرسالة أعلى المحادثة"
  override val messageUnpinnedToast = "تمت إزالة التثبيت"
  override val forwardAction = "إعادة توجيه"
  override val forwardToTitle = "تحويل الرسالة إلى"
  override val forwardedFromLabel = "محوّلة من %s"
  override val messageForwardedToast = "تمت إعادة توجيه الرسالة"
  override val searchMessagesPlaceholder = "ابحث داخل المحادثة..."
  override val searchResultsCountFormat = "%d / %d"
  override val spoilerToggleLabel = "إخفاء المحتوى"
  override val spoilerToggleDesc = "تمويه الرسالة حتى يتم النقر عليها"
  override val spoilerHiddenTitle = "محتوى مخفي"
  override val spoilerTapToReveal = "اضغط للعرض"
  override val chatInfoTitle = "معلومات المحادثة والوسائط"
  override val sharedMediaTabPhotos = "الصور"
  override val sharedMediaTabVideos = "الفيديو"
  override val sharedMediaTabAudio = "الصوتيات"
  override val sharedMediaTabFiles = "الملفات"
  override val sharedMediaTabPolls = "الاستطلاعات"
  override val noSharedMediaYet = "لا توجد عناصر مشتركة هنا بعد"
  override val membersCountFormat = "%d أعضاء"

  override val authSignInTab = "تسجيل دخول"
  override val authCreateAccountTab = "إنشاء حساب"
  override val authEmailLabel = "البريد الإلكتروني"
  override val authPasswordLabel = "كلمة السر"
  override val authConfirmPasswordLabel = "تأكيد كلمة السر"
  override val authForgotPassword = "هل نسيت كلمة السر؟"
  override val authRecoverAccount = "استرجاع حساب"
  override val authOrDivider = "أو"
  override val authCreatingAccount = "جارٍ إنشاء الحساب..."
  override val authForgotPasswordTitle = "نسيت كلمة السر"
  override val authForgotPasswordDesc = "أدخل بريدك الإلكتروني المسجّل وسنرسل لك رابطاً لإعادة تعيين كلمة السر."
  override val authSendResetLink = "إرسال رابط الاستعادة"
  override val authSendingResetLink = "جارٍ الإرسال..."
  override val authResetLinkSent = "تم إرسال رابط استعادة كلمة السر إلى بريدك الإلكتروني. تحقق من البريد الوارد."
  override val authRecoverAccountTitle = "استرجاع حساب"
  override val authRecoverAccountDesc = "استعد الوصول إلى حسابك عبر البريد الإلكتروني المسجّل أو عبر حساب Google المرتبط."
  override val authRecoverViaEmailBtn = "استرجاع عبر البريد الإلكتروني"
  override val authRecoverViaGoogleBtn = "استرجاع عبر حساب Google"
  override val authBackToSignIn = "العودة لتسجيل الدخول"
  override val authShowPasswordDesc = "إظهار كلمة السر"
  override val authHidePasswordDesc = "إخفاء كلمة السر"
  override val authInvalidEmailError = "يرجى إدخال بريد إلكتروني صحيح."
  override val authWeakPasswordError = "يجب أن تتكون كلمة السر من 6 أحرف على الأقل."
  override val authPasswordMismatchError = "كلمتا السر غير متطابقتين."
  override val authSignInSubtitle = "سجّل دخولك أو أنشئ حساباً جديداً أو استرجع حسابك."
  override val chatDateToday = "اليوم"
  override val chatDateYesterday = "أمس"
}

val LocalAppStrings = compositionLocalOf<KoraStrings> { EnglishStrings }
val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }
