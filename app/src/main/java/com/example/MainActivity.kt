package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.repository.ChatRepository
import com.example.data.repository.FluxRepository
import com.example.data.repository.PostRepository
import com.example.data.repository.StoreRepository
import com.example.data.repository.UserRepository
import com.example.ui.components.KoraMainScaffold
import com.example.ui.components.NavigationTab
import com.example.ui.components.ThemeSwitcherSheet
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check with launch intent before any UI, ViewModels, or Firebase operations
    configureAppCheck(this, intent)

    enableEdgeToEdge()

    val isFirebaseReady = FirebaseApp.getApps(this).isNotEmpty()
    val userRepository = if (isFirebaseReady) UserRepository(this) else null
    val chatRepository = if (isFirebaseReady) ChatRepository(this) else null
    val postRepository = if (isFirebaseReady) PostRepository(this) else null
    val prefs = PreferencesManager(this)

    setContent {
      var currentLanguage by remember { mutableStateOf(prefs.language) }
      var currentPreset by remember { mutableStateOf(prefs.themePreset) }
      var isAmoled by remember { mutableStateOf(prefs.amoledPureBlack) }
      var currentAccent by remember { mutableStateOf(prefs.accentChoice) }
      var themeMode by remember { mutableStateOf(prefs.themeMode) }

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
          if (!isFirebaseReady || userRepository == null || chatRepository == null || postRepository == null) {
            MissingFirebaseConfigScreen(
              onLanguageToggle = {
                val next = if (currentLanguage == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
                currentLanguage = next
                prefs.language = next
              }
            )
          } else {
            val credentialManager = remember { CredentialManager.create(this@MainActivity) }
            val coroutineScope = rememberCoroutineScope()

            val currentUser by Firebase.auth.authStateFlow()
              .collectAsState(initial = Firebase.auth.currentUser)
            var isCheckingSilentAuth by remember {
              mutableStateOf(Firebase.auth.currentUser == null)
            }

            // Cold-start silent Google Sign-In attempt for pre-authorized accounts only
            LaunchedEffect(Unit) {
              if (Firebase.auth.currentUser == null) {
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
                  // No previously authorized Google account; show manual sign-in button
                } catch (_: GetCredentialCancellationException) {
                  // Cancelled silently
                } catch (_: Exception) {
                  // Fallback cleanly to manual Sign in with Google button
                } finally {
                  isCheckingSilentAuth = false
                }
              } else {
                isCheckingSilentAuth = false
              }
            }

            val signOutAction: () -> Unit = {
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
                onAuthSuccess = { /* Reactive authStateFlow transitions automatically */ },
                isCheckingAutoSignIn = isCheckingSilentAuth
              )
            } else {
            key(signedInUser.uid) {
              var profileState by remember { mutableStateOf<ProfileLoadState>(ProfileLoadState.Loading) }
              var reloadTrigger by remember { mutableIntStateOf(0) }

              LaunchedEffect(signedInUser.uid, reloadTrigger) {
                profileState = ProfileLoadState.Loading
                try {
                  val loaded = userRepository.getUserProfile(signedInUser.uid)
                  profileState = if (loaded != null) {
                    ProfileLoadState.Ready(loaded)
                  } else {
                    ProfileLoadState.NeedsSetup
                  }
                } catch (e: Exception) {
                  profileState = ProfileLoadState.Error(
                    e.localizedMessage ?: strings.errorOccurred
                  )
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
                    onAuthSuccess = { saved -> profileState = ProfileLoadState.Ready(saved) },
                    onSwitchAccount = signOutAction
                  )
                }

                is ProfileLoadState.Ready -> {
                  KoraAppRoot(
                    userRepository = userRepository,
                    chatRepository = chatRepository,
                    postRepository = postRepository,
                    currentLanguage = currentLanguage,
                    onLanguageChanged = { newLang ->
                      currentLanguage = newLang
                      prefs.language = newLang
                    },
                    themeMode = themeMode,
                    currentPreset = currentPreset,
                    currentAmoled = isAmoled,
                    currentAccent = currentAccent,
                    onThemeChanged = { newPreset, newAmoled, newAccent ->
                      currentPreset = newPreset
                      isAmoled = newAmoled
                      currentAccent = newAccent
                      prefs.themePreset = newPreset
                      prefs.amoledPureBlack = newAmoled
                      prefs.accentChoice = newAccent
                    },
                    onThemeModeChanged = { newMode ->
                      themeMode = newMode
                      prefs.themeMode = newMode
                    },
                    onSignOut = signOutAction
                  )
                }
              }
            }
            }
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
  currentLanguage: AppLanguage,
  onLanguageChanged: (AppLanguage) -> Unit,
  themeMode: String,
  currentPreset: AppThemePreset,
  currentAmoled: Boolean,
  currentAccent: AccentColorChoice,
  onThemeChanged: (AppThemePreset, Boolean, AccentColorChoice) -> Unit,
  onThemeModeChanged: (String) -> Unit,
  onSignOut: () -> Unit = {}
) {
  val context = LocalContext.current
  val storeRepository = remember { StoreRepository(context) }
  val fluxRepository = remember { FluxRepository(context) }

  var currentTab by remember { mutableStateOf(NavigationTab.CHATS) }
  var activeConversation by remember { mutableStateOf<Conversation?>(null) }
  var showNewChatDialog by remember { mutableStateOf(false) }
  var showStore by remember { mutableStateOf(false) }
  var showQuickThemeSheet by remember { mutableStateOf(false) }

  // Heartbeat update
  LaunchedEffect(Unit) {
    userRepository.updateLastSeen()
  }

  fun toggleLanguage() {
    val next = if (currentLanguage == AppLanguage.EN) AppLanguage.AR else AppLanguage.EN
    onLanguageChanged(next)
  }

  if (showStore) {
    BackHandler { showStore = false }
    DecorationStoreScreen(
      storeRepository = storeRepository,
      userRepository = userRepository,
      onBack = { showStore = false }
    )
  } else if (activeConversation != null) {
    BackHandler { activeConversation = null }
    ChatDetailScreen(
      conversation = activeConversation!!,
      chatRepository = chatRepository,
      postRepository = postRepository,
      userRepository = userRepository,
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
          onOpenStore = { showStore = true }
        )

        NavigationTab.FLUX -> com.example.ui.screens.FluxScreen(
          fluxRepository = fluxRepository,
          userRepository = userRepository,
          onLanguageToggle = { toggleLanguage() }
        )

        NavigationTab.FEED -> FeedScreen(
          postRepository = postRepository,
          userRepository = userRepository,
          onLanguageToggle = { toggleLanguage() }
        )

        NavigationTab.MEMORY -> MemoryVaultScreen(
          postRepository = postRepository,
          onLanguageToggle = { toggleLanguage() }
        )

        NavigationTab.PROFILE -> ProfileScreen(
          userRepository = userRepository,
          storeRepository = storeRepository,
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
            onLanguageChanged = onLanguageChanged
          )
        }
      }

      if (showNewChatDialog) {
        NewChatDialog(
          userRepository = userRepository,
          chatRepository = chatRepository,
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
}
