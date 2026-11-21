package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.model.Conversation
import com.example.data.model.UserProfile
import com.example.data.repository.ChatMediaRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.FluxRepository
import com.example.data.repository.PostRepository
import com.example.data.repository.StoreRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.VoiceCallRepository
import com.example.notifications.IncomingMessageAlert
import com.example.notifications.KoraMessageNotificationManager
import com.example.ui.components.KoraMainScaffold
import com.example.ui.components.LuxurySplashScreen
import com.example.ui.components.NavigationTab
import com.example.ui.components.NewMessageBanner
import com.example.ui.components.ThemeSwitcherSheet
import com.example.ui.components.UserProfileInspectorSheet
import com.example.ui.components.VoiceCallController
import com.example.ui.components.VoiceCallOverlay
import com.example.ui.components.VoiceCallUiPhase
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatListScreen
import com.example.ui.screens.DecorationStoreScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.MemoryVaultScreen
import com.example.ui.screens.MissingFirebaseConfigScreen
import com.example.ui.screens.NewChatDialog
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.KoraTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.ArabicStrings
import com.example.ui.util.EnglishStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.PreferencesManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
    trySend(firebaseAuth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

private sealed interface ProfileLoadState {
  data object Loading : ProfileLoadState
  data object NeedsSetup : ProfileLoadState
  data class Ready(val profile: UserProfile) : ProfileLoadState
  data class Error(val message: String) : ProfileLoadState
}

class MainActivity : ComponentActivity() {
  private val pendingNotificationConvoId = MutableStateFlow<String?>(null)

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    intent.getStringExtra(KoraMessageNotificationManager.EXTRA_CONVERSATION_ID)
      ?.takeIf { it.isNotBlank() }
      ?.let { pendingNotificationConvoId.value = it }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check with launch intent before any UI, ViewModels, or Firebase operations
    configureAppCheck(this, intent)
    intent?.getStringExtra(KoraMessageNotificationManager.EXTRA_CONVERSATION_ID)
      ?.takeIf { it.isNotBlank() }
      ?.let { pendingNotificationConvoId.value = it }

    enableEdgeToEdge()

    val isFirebaseReady = FirebaseApp.getApps(this).isNotEmpty()
    val userRepository = if (isFirebaseReady) UserRepository(this) else null
    val chatRepository = if (isFirebaseReady) ChatRepository(this) else null
    val postRepository = if (isFirebaseReady) PostRepository(this) else null
    val prefs = PreferencesManager(this)

    setContent {
      val openConvoIdFromNotification by pendingNotificationConvoId.collectAsState()
      var currentLanguage by remember { mutableStateOf(prefs.language) }
      var currentPreset by remember { mutableStateOf(prefs.themePreset) }
      var isAmoled by remember { mutableStateOf(prefs.amoledPureBlack) }
      var currentAccent by remember { mutableStateOf(prefs.accentChoice) }
      var themeMode by remember { mutableStateOf(prefs.themeMode) }
      var compactMode by remember { mutableStateOf(prefs.compactMode) }
      var inAppBannersEnabled by remember { mutableStateOf(prefs.inAppBannersEnabled) }
      var notificationSoundEnabled by remember { mutableStateOf(prefs.notificationSoundEnabled) }
      var fluxStartMuted by remember { mutableStateOf(prefs.fluxStartMuted) }
      var fluxAutoDownload by remember { mutableStateOf(prefs.fluxAutoDownload) }
      var callSpeakerDefault by remember { mutableStateOf(prefs.callSpeakerDefault) }
      var showLuxuryIntro by remember { mutableStateOf(true) }

      val toggleAuthLanguage: () -> Unit = {
        val next = if (currentLanguage == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
        currentLanguage = next
        prefs.language = next
      }

      val applyAuthTheme: (AppThemePreset, Boolean, AccentColorChoice) -> Unit = { newPreset, newAmoled, newAccent ->
        currentPreset = newPreset
        isAmoled = newAmoled
        currentAccent = newAccent
        prefs.themePreset = newPreset
        prefs.amoledPureBlack = newAmoled
        prefs.accentChoice = newAccent
      }

      val strings = if (currentLanguage == AppLanguage.AR) ArabicStrings else EnglishStrings

      CompositionLocalProvider(
        LocalAppStrings provides strings,
        LocalAppLanguage provides currentLanguage,
        LocalLayoutDirection provides currentLanguage.layoutDirection
      ) {
        KoraTheme(
          themePreset = currentPreset,
          amoledPureBlack = isAmoled,
          accentChoice = currentAccent
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
          if (!isFirebaseReady || userRepository == null || chatRepository == null || postRepository == null) {
            MissingFirebaseConfigScreen(
              onLanguageToggle = toggleAuthLanguage
            )
          } else {
            val credentialManager = remember { CredentialManager.create(this@MainActivity) }
            val coroutineScope = rememberCoroutineScope()

            val currentUser by Firebase.auth.authStateFlow()
              .collectAsState(initial = Firebase.auth.currentUser)

            // Non-blocking cold-start silent Google Sign-In attempt for pre-authorized accounts
            LaunchedEffect(Unit) {
              if (Firebase.auth.currentUser == null) {
                withTimeoutOrNull(1800L) {
                  try {
                    val clientIdRes = resources.getIdentifier("default_web_client_id", "string", packageName)
                    val clientId = if (clientIdRes != 0) getString(clientIdRes) else ""
                    if (clientId.isNotBlank()) {
                      val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(clientId)
                        .setAutoSelectEnabled(true)
                        .build()
                      val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                      val result = credentialManager.getCredential(this@MainActivity, request)
                      val credential = result.credential
                      if (credential is CustomCredential &&
                        credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                      ) {
                        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        Firebase.auth.signInWithCredential(authCredential).await()
                      }
                    }
                  } catch (_: NoCredentialException) {
                    // No previously authorized Google account
                  } catch (_: GetCredentialCancellationException) {
                    // Cancelled silently
                  } catch (_: Exception) {
                    // Fallback cleanly to manual Sign in with Google button
                  }
                }
              }
            }

            val signOutAction: () -> Unit = {
              prefs.clearAllSessionCaches(this@MainActivity)
              coroutineScope.launch {
                withContext(NonCancellable) {
                  runCatching {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                  }
                  Firebase.auth.signOut()
                }
              }
            }

            val signedInUser = currentUser
            if (signedInUser == null) {
              AuthScreen(
                userRepository = userRepository,
                onAuthSuccess = { saved ->
                  prefs.saveCachedProfile(saved)
                },
                isCheckingAutoSignIn = false,
                currentPreset = currentPreset,
                currentAmoled = isAmoled,
                currentAccent = currentAccent,
                onThemeChanged = applyAuthTheme,
                onLanguageToggle = toggleAuthLanguage
              )
            } else {
            key(signedInUser.uid) {
              val cachedProfile = remember(signedInUser.uid) {
                prefs.getCachedProfile(signedInUser.uid)
              }
              var profileState by remember {
                mutableStateOf<ProfileLoadState>(
                  cachedProfile?.let { ProfileLoadState.Ready(it) } ?: ProfileLoadState.Loading
                )
              }
              var reloadTrigger by remember { mutableIntStateOf(0) }

              LaunchedEffect(signedInUser.uid, reloadTrigger) {
                if (profileState !is ProfileLoadState.Ready) {
                  profileState = ProfileLoadState.Loading
                }
                try {
                  val loaded = userRepository.getUserProfile(signedInUser.uid)
                  if (loaded != null) {
                    prefs.saveCachedProfile(loaded)
                    profileState = ProfileLoadState.Ready(loaded)
                  } else {
                    prefs.clearCachedProfile()
                    profileState = ProfileLoadState.NeedsSetup
                  }
                } catch (e: Exception) {
                  if (profileState !is ProfileLoadState.Ready) {
                    profileState = ProfileLoadState.Error(
                      e.localizedMessage ?: strings.errorOccurred
                    )
                  }
                }
              }

              when (val state = profileState) {
                is ProfileLoadState.Loading -> {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                      Spacer(modifier = Modifier.height(16.dp))
                      Text(
                        text = strings.syncingProfile,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }

                is ProfileLoadState.Error -> {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .background(MaterialTheme.colorScheme.background)
                      .padding(24.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Surface(
                      shape = RoundedCornerShape(20.dp),
                      color = MaterialTheme.colorScheme.surface,
                      tonalElevation = 2.dp,
                      modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()
                    ) {
                      Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                      ) {
                        Text(
                          text = strings.errorOccurred,
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                          text = state.message,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                          onClick = { reloadTrigger++ },
                          modifier = Modifier.fillMaxWidth()
                        ) {
                          Text(strings.retry)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                          onClick = signOutAction,
                          modifier = Modifier.fillMaxWidth()
                        ) {
                          Text(strings.signOut)
                        }
                      }
                    }
                  }
                }

                is ProfileLoadState.NeedsSetup -> {
                  AuthScreen(
                    userRepository = userRepository,
                    startInProfileSetup = true,
                    onAuthSuccess = { saved ->
                      prefs.saveCachedProfile(saved)
                      profileState = ProfileLoadState.Ready(saved)
                    },
                    currentPreset = currentPreset,
                    currentAmoled = isAmoled,
                    currentAccent = currentAccent,
                    onThemeChanged = applyAuthTheme,
                    onLanguageToggle = toggleAuthLanguage,
                    onSwitchAccount = signOutAction
                  )
                }

                is ProfileLoadState.Ready -> {
                  KoraAppRoot(
                    userRepository = userRepository,
                    chatRepository = chatRepository,
                    postRepository = postRepository,
                    pendingOpenConversationId = openConvoIdFromNotification,
                    onConsumePendingConversationId = { pendingNotificationConvoId.value = null },
                    currentLanguage = currentLanguage,
                    onLanguageChanged = { newLang ->
                      currentLanguage = newLang
                      prefs.language = newLang
                    },
                    themeMode = themeMode,
                    currentPreset = currentPreset,
                    currentAmoled = isAmoled,
                    currentAccent = currentAccent,
                    onThemeChanged = applyAuthTheme,
                    onThemeModeChanged = { newMode ->
                      themeMode = newMode
                      prefs.themeMode = newMode
                    },
                    compactMode = compactMode,
                    onCompactModeChanged = { enabled ->
                      compactMode = enabled
                      prefs.compactMode = enabled
                    },
                    inAppBannersEnabled = inAppBannersEnabled,
                    onInAppBannersChanged = { enabled ->
                      inAppBannersEnabled = enabled
                      prefs.inAppBannersEnabled = enabled
                    },
                    notificationSoundEnabled = notificationSoundEnabled,
                    onNotificationSoundChanged = { enabled ->
                      notificationSoundEnabled = enabled
                      prefs.notificationSoundEnabled = enabled
                    },
                    fluxStartMuted = fluxStartMuted,
                    onFluxStartMutedChanged = { enabled ->
                      fluxStartMuted = enabled
                      prefs.fluxStartMuted = enabled
                    },
                    fluxAutoDownload = fluxAutoDownload,
                    onFluxAutoDownloadChanged = { enabled ->
                      fluxAutoDownload = enabled
                      prefs.fluxAutoDownload = enabled
                    },
                    callSpeakerDefault = callSpeakerDefault,
                    onCallSpeakerDefaultChanged = { enabled ->
                      callSpeakerDefault = enabled
                      prefs.callSpeakerDefault = enabled
                    },
                    onSignOut = signOutAction
                  )
                }
              }
            }
            }
          }

          LuxurySplashScreen(
            visible = showLuxuryIntro,
            onFinished = { showLuxuryIntro = false }
          )
          }
        }
      }
    }
  }
}

@Composable
fun KoraAppRoot(
  userRepository: UserRepository,
  chatRepository: ChatRepository,
  postRepository: PostRepository,
  pendingOpenConversationId: String? = null,
  onConsumePendingConversationId: () -> Unit = {},
  currentLanguage: AppLanguage,
  onLanguageChanged: (AppLanguage) -> Unit,
  themeMode: String,
  currentPreset: AppThemePreset,
  currentAmoled: Boolean,
  currentAccent: AccentColorChoice,
  onThemeChanged: (AppThemePreset, Boolean, AccentColorChoice) -> Unit,
  onThemeModeChanged: (String) -> Unit,
  compactMode: Boolean = false,
  onCompactModeChanged: (Boolean) -> Unit = {},
  inAppBannersEnabled: Boolean = true,
  onInAppBannersChanged: (Boolean) -> Unit = {},
  notificationSoundEnabled: Boolean = true,
  onNotificationSoundChanged: (Boolean) -> Unit = {},
  fluxStartMuted: Boolean = false,
  onFluxStartMutedChanged: (Boolean) -> Unit = {},
  fluxAutoDownload: Boolean = true,
  onFluxAutoDownloadChanged: (Boolean) -> Unit = {},
  callSpeakerDefault: Boolean = false,
  onCallSpeakerDefaultChanged: (Boolean) -> Unit = {},
  onSignOut: () -> Unit = {}
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val currentUid = userRepository.requireUserId()
  val storeRepository = remember(currentUid) { StoreRepository(context) }
  val fluxRepository = remember(currentUid) { FluxRepository(context) }
  val chatMediaRepository = remember(currentUid) { ChatMediaRepository(context) }
  val voiceCallRepository = remember(currentUid) { VoiceCallRepository(context) }
  val callScope = rememberCoroutineScope()

  val cachedProfile = remember(currentUid) { userRepository.getCachedProfile(currentUid) }
  val currentUserProfile by userRepository.observeUserProfile(currentUid)
    .collectAsState(initial = cachedProfile)
  val latestCallSpeakerDefault by rememberUpdatedState(callSpeakerDefault)

  val voiceCallController = remember(currentUid) {
    VoiceCallController(
      context = context,
      currentUid = currentUid,
      currentUserNameProvider = {
        currentUserProfile?.displayName?.takeIf { it.isNotBlank() } ?: "Member"
      },
      currentAvatarUrlProvider = { currentUserProfile?.avatarUrl },
      repository = voiceCallRepository,
      scope = callScope,
      defaultSpeakerProvider = { latestCallSpeakerDefault }
    )
  }
  val voiceCallState by voiceCallController.uiState.collectAsState()

  DisposableEffect(storeRepository, voiceCallController) {
    onDispose {
      storeRepository.close()
      voiceCallController.release()
    }
  }

  var currentTab by remember { mutableStateOf(NavigationTab.CHATS) }
  var activeConversation by remember { mutableStateOf<Conversation?>(null) }
  var showNewChatDialog by remember { mutableStateOf(false) }
  var showStore by remember { mutableStateOf(false) }
  var showQuickThemeSheet by remember { mutableStateOf(false) }
  var inAppMessageAlert by remember { mutableStateOf<IncomingMessageAlert?>(null) }
  var inspectedUserProfile by remember { mutableStateOf<UserProfile?>(null) }

  var pendingInspectorCall by remember { mutableStateOf<(() -> Unit)?>(null) }
  val inspectorMicLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    if (granted) {
      pendingInspectorCall?.invoke()
    } else {
      Toast.makeText(context, strings.micPermissionRequired, Toast.LENGTH_LONG).show()
    }
    pendingInspectorCall = null
  }

  // Request Android 13+ POST_NOTIFICATIONS runtime permission once cleanly
  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* handled gracefully if denied */ }

  LaunchedEffect(currentUid) {
    KoraMessageNotificationManager.ensureNotificationChannel(context, strings)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      !KoraMessageNotificationManager.hasNotificationPermission(context)
    ) {
      notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  val conversations by chatRepository.observeConversations().collectAsState(initial = emptyList())
  val directoryUsers by userRepository.observeDirectoryUsers()
    .collectAsState(initial = userRepository.getDirectoryUsers())
  val allPosts by postRepository.observePosts("all").collectAsState(initial = emptyList())
  val allFluxVideos by fluxRepository.observeVideos().collectAsState(initial = emptyList())

  // Open target conversation when user taps a system message notification
  LaunchedEffect(pendingOpenConversationId, conversations) {
    val targetId = pendingOpenConversationId ?: return@LaunchedEffect
    val matched = conversations.firstOrNull { it.id == targetId }
    if (matched != null) {
      showStore = false
      currentTab = NavigationTab.CHATS
      activeConversation = matched
      onConsumePendingConversationId()
    }
  }

  // Dismiss OS notification and in-app banner whenever user enters that conversation
  LaunchedEffect(activeConversation?.id) {
    activeConversation?.id?.let { openedId ->
      KoraMessageNotificationManager.cancelNotificationForConversation(context, openedId)
      if (inAppMessageAlert?.conversation?.id == openedId) {
        inAppMessageAlert = null
      }
    }
  }

  // Real-time incoming message detector -> System Notification + In-App Alert Banner
  val latestActiveConvoId by rememberUpdatedState(activeConversation?.id)
  val latestStrings by rememberUpdatedState(strings)
  val latestDirectory by rememberUpdatedState(directoryUsers)
  val latestBannersEnabled by rememberUpdatedState(inAppBannersEnabled)
  val latestSoundEnabled by rememberUpdatedState(notificationSoundEnabled)

  LaunchedEffect(currentUid, chatRepository) {
    var baselineInitialized = false
    val lastSeenTimestamps = mutableMapOf<String, Long>()
    val lastSeenSignatures = mutableMapOf<String, String>()

    chatRepository.observeConversations().collect { convoList ->
      val newIncoming = KoraMessageNotificationManager.detectNewIncomingMessages(
        currentUid = currentUid,
        activeConversationId = latestActiveConvoId,
        baselineInitialized = baselineInitialized,
        lastSeenTimestamps = lastSeenTimestamps,
        lastSeenSignatures = lastSeenSignatures,
        conversations = convoList,
        aiConversationId = ChatRepository.AI_CHATBOT_CONVO_ID
      )
      baselineInitialized = true

      for (convo in newIncoming) {
        val senderProfile = latestDirectory.find { it.id == convo.lastMessageSenderId }
          ?: runCatching { userRepository.getUserProfile(convo.lastMessageSenderId) }.getOrNull()
        val senderName = senderProfile?.displayName?.takeIf { it.isNotBlank() }
          ?: convo.title.takeIf { it.isNotBlank() && convo.type == "direct" }
          ?: ""
        val preview = convo.lastMessageText.ifBlank { latestStrings.newMessageNotificationTitle }

        KoraMessageNotificationManager.postNewMessageNotification(
          context = context,
          conversationId = convo.id,
          senderName = senderName,
          messagePreview = preview,
          strings = latestStrings,
          soundEnabled = latestSoundEnabled
        )

        if (latestBannersEnabled) {
          inAppMessageAlert = IncomingMessageAlert(
            conversation = convo,
            senderName = senderName,
            senderAvatarUrl = senderProfile?.avatarUrl,
            messagePreview = preview
          )
        }
      }
    }
  }

  LaunchedEffect(inAppMessageAlert) {
    if (inAppMessageAlert != null) {
      delay(4800L)
      inAppMessageAlert = null
    }
  }

  // Heartbeat update & server wallet initialization
  LaunchedEffect(currentUid, storeRepository) {
    userRepository.updateLastSeen()
    storeRepository.initializeWalletIfNeeded()
  }

  fun toggleLanguage() {
    val next = if (currentLanguage == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
    onLanguageChanged(next)
  }

  Box(modifier = Modifier.fillMaxSize()) {
  if (showStore) {
    BackHandler { showStore = false }
    DecorationStoreScreen(
      storeRepository = storeRepository,
      userRepository = userRepository,
      fluxRepository = fluxRepository,
      onBack = { showStore = false }
    )
  } else if (activeConversation != null) {
    BackHandler { activeConversation = null }
    ChatDetailScreen(
      conversation = activeConversation!!,
      chatRepository = chatRepository,
      chatMediaRepository = chatMediaRepository,
      postRepository = postRepository,
      userRepository = userRepository,
      conversations = conversations,
      onSwitchToConversation = { convo -> activeConversation = convo },
      onInspectUser = { inspectedUserProfile = it },
      onStartVoiceCall = { peerId, peerName, peerAvatarUrl, peerVerified, peerVerifiedTier ->
        voiceCallController.startOutgoingCall(
          conversationId = activeConversation!!.id,
          peerId = peerId,
          peerName = peerName,
          peerAvatarUrl = peerAvatarUrl,
          peerVerified = peerVerified,
          peerVerifiedTier = peerVerifiedTier
        )
      },
      onBack = { activeConversation = null }
    )
  } else {
    KoraMainScaffold(
      currentTab = currentTab,
      onTabSelected = { currentTab = it }
    ) {
      when (currentTab) {
        NavigationTab.CHATS -> ChatListScreen(
          chatRepository = chatRepository,
          userRepository = userRepository,
          onOpenConversation = { convo -> activeConversation = convo },
          onNewChatClicked = { showNewChatDialog = true },
          onLanguageToggle = { toggleLanguage() },
          onOpenStore = { showStore = true },
          onOpenSettings = { currentTab = NavigationTab.SETTINGS },
          onInspectUser = { inspectedUserProfile = it },
          compactMode = compactMode
        )

        NavigationTab.FLUX -> com.example.ui.screens.FluxScreen(
          fluxRepository = fluxRepository,
          userRepository = userRepository,
          onLanguageToggle = { toggleLanguage() },
          onInspectUser = { inspectedUserProfile = it },
          initialMuted = fluxStartMuted,
          autoLoadVideos = fluxAutoDownload
        )

        NavigationTab.FEED -> FeedScreen(
          postRepository = postRepository,
          userRepository = userRepository,
          onLanguageToggle = { toggleLanguage() },
          onInspectUser = { inspectedUserProfile = it }
        )

        NavigationTab.MEMORY -> MemoryVaultScreen(
          postRepository = postRepository,
          onLanguageToggle = { toggleLanguage() }
        )

        NavigationTab.PROFILE -> ProfileScreen(
          userRepository = userRepository,
          storeRepository = storeRepository,
          postRepository = postRepository,
          fluxRepository = fluxRepository,
          onOpenStore = { showStore = true },
          onOpenSettings = { currentTab = NavigationTab.SETTINGS },
          onOpenThemeStudio = { showQuickThemeSheet = true },
          onLanguageToggle = { toggleLanguage() },
          onSignOut = onSignOut
        )

        NavigationTab.SETTINGS -> {
          BackHandler { currentTab = NavigationTab.PROFILE }
          SettingsScreen(
            currentThemeMode = themeMode,
            currentPreset = currentPreset,
            currentAmoled = currentAmoled,
            currentAccent = currentAccent,
            onThemeChanged = onThemeChanged,
            onThemeModeChanged = onThemeModeChanged,
            onLanguageChanged = onLanguageChanged,
            userRepository = userRepository,
            compactMode = compactMode,
            onCompactModeChanged = onCompactModeChanged,
            inAppBannersEnabled = inAppBannersEnabled,
            onInAppBannersChanged = onInAppBannersChanged,
            notificationSoundEnabled = notificationSoundEnabled,
            onNotificationSoundChanged = onNotificationSoundChanged,
            fluxStartMuted = fluxStartMuted,
            onFluxStartMutedChanged = onFluxStartMutedChanged,
            fluxAutoDownload = fluxAutoDownload,
            onFluxAutoDownloadChanged = onFluxAutoDownloadChanged,
            callSpeakerDefault = callSpeakerDefault,
            onCallSpeakerDefaultChanged = onCallSpeakerDefaultChanged,
            onBackClick = { currentTab = NavigationTab.PROFILE },
            onSignOut = onSignOut
          )
        }
      }

      if (showNewChatDialog) {
        NewChatDialog(
          userRepository = userRepository,
          chatRepository = chatRepository,
          existingConversations = conversations,
          onDismiss = { showNewChatDialog = false },
          onConversationCreated = { convo ->
            showNewChatDialog = false
            activeConversation = convo
          }
        )
      }

      if (showQuickThemeSheet) {
        ThemeSwitcherSheet(
          currentPreset = currentPreset,
          currentAmoled = currentAmoled,
          currentAccent = currentAccent,
          onThemeChanged = onThemeChanged,
          onDismiss = { showQuickThemeSheet = false }
        )
      }
    }
  }

  // In-app Floating New Message Notification Banner (hidden during active call UI or when disabled)
  NewMessageBanner(
    alert = inAppMessageAlert,
    visible = inAppBannersEnabled && inAppMessageAlert != null && voiceCallState.phase == VoiceCallUiPhase.IDLE,
    onOpenChat = { alert ->
      inAppMessageAlert = null
      showStore = false
      currentTab = NavigationTab.CHATS
      activeConversation = alert.conversation
    },
    onDismiss = { inAppMessageAlert = null }
  )

  // Persistent App-Wide Voice Call Overlay (Incoming Banner, Minimized Pill, Fullscreen Call)
  VoiceCallOverlay(controller = voiceCallController)

  // App-wide Cross-User Profile Inspector Modal Bottom Sheet
  inspectedUserProfile?.let { target ->
    val targetPosts = remember(allPosts, target.id) {
      allPosts.filter { it.authorId == target.id }
    }
    val targetFluxVideos = remember(allFluxVideos, target.id) {
      allFluxVideos.filter { it.authorId == target.id }
    }
    UserProfileInspectorSheet(
      seedUser = target,
      userRepository = userRepository,
      currentUid = currentUid,
      postsCount = targetPosts.size,
      fluxCount = targetFluxVideos.size,
      userPosts = targetPosts,
      userFluxVideos = targetFluxVideos,
      onDismiss = { inspectedUserProfile = null },
      onMessageClick = { peer ->
        inspectedUserProfile = null
        callScope.launch {
          val res = chatRepository.findOrCreateDirectConversation(
            targetUserId = peer.id,
            title = peer.displayName.ifBlank { "Member" },
            isVerified = peer.isVerified,
            verifiedTier = peer.verifiedTier
          )
          if (res.isSuccess) {
            val convoId = res.getOrThrow()
            val resolvedConvo = conversations.find { it.id == convoId } ?: Conversation(
              id = convoId,
              type = "direct",
              title = peer.displayName.ifBlank { "Member" },
              participantIds = listOf(currentUid, peer.id),
              isVerified = peer.isVerified,
              verifiedTier = peer.verifiedTier
            )
            showStore = false
            currentTab = NavigationTab.CHATS
            activeConversation = resolvedConvo
          } else {
            Toast.makeText(
              context,
              res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
              Toast.LENGTH_SHORT
            ).show()
          }
        }
      },
      onVoiceCallClick = { peer ->
        val launchCall = {
          inspectedUserProfile = null
          callScope.launch {
            val res = chatRepository.findOrCreateDirectConversation(
              targetUserId = peer.id,
              title = peer.displayName.ifBlank { "Member" },
              isVerified = peer.isVerified,
              verifiedTier = peer.verifiedTier
            )
            if (res.isSuccess) {
              val convoId = res.getOrThrow()
              voiceCallController.startOutgoingCall(
                conversationId = convoId,
                peerId = peer.id,
                peerName = peer.displayName.ifBlank { "Member" },
                peerAvatarUrl = peer.avatarUrl,
                peerVerified = peer.isVerified,
                peerVerifiedTier = peer.verifiedTier
              )
            } else {
              Toast.makeText(
                context,
                res.exceptionOrNull()?.localizedMessage ?: strings.errorOccurred,
                Toast.LENGTH_SHORT
              ).show()
            }
          }
          Unit
        }
        val hasMic = ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasMic) {
          launchCall()
        } else {
          pendingInspectorCall = launchCall
          inspectorMicLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
      }
    )
  }
  }
}
