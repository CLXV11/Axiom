package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.components.AvatarView
import com.example.ui.components.PresetAvatars
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.StatusQuiet
import com.example.ui.util.AppLanguage
import com.example.ui.util.KoraStrings
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class AuthFeatureHighlight(val icon: ImageVector) {
  VOICE_HD(Icons.Default.GraphicEq),
  CINEMA(Icons.Default.PlayCircle),
  FLUX_ALERTS(Icons.Default.Bolt);

  fun title(strings: KoraStrings): String = when (this) {
    VOICE_HD -> strings.featureVoiceHdTitle
    CINEMA -> strings.featureCinemaTitle
    FLUX_ALERTS -> strings.featurePulseTitle
  }

  fun description(strings: KoraStrings): String = when (this) {
    VOICE_HD -> strings.featureVoiceHdDesc
    CINEMA -> strings.featureCinemaDesc
    FLUX_ALERTS -> strings.featurePulseDesc
  }
}

data class QuickRoleOption(
  val id: String,
  val labelProvider: (KoraStrings) -> String
)

object AuthOnboardingDefaults {
  val themeVibes: List<AppThemePreset> = AppThemePreset.entries.filter { it != AppThemePreset.SYSTEM }

  val accentSwatches: List<AccentColorChoice> = listOf(
    AccentColorChoice.DEFAULT,
    AccentColorChoice.BLURPLE,
    AccentColorChoice.CYAN,
    AccentColorChoice.EMERALD,
    AccentColorChoice.GOLD,
    AccentColorChoice.NITRO_PINK
  )

  val quickRoles: List<QuickRoleOption> = listOf(
    QuickRoleOption("creator") { it.quickRoleCreator },
    QuickRoleOption("developer") { it.quickRoleDeveloper },
    QuickRoleOption("designer") { it.quickRoleDesigner },
    QuickRoleOption("gamer") { it.quickRoleGamer },
    QuickRoleOption("student") { it.quickRoleStudent },
    QuickRoleOption("founder") { it.quickRoleFounder }
  )

  fun suggestHandle(name: String?, uid: String): String {
    val sanitized = (name ?: "")
      .lowercase()
      .replace(Regex("[^a-z0-9_]"), "")
      .take(18)
    val base = if (sanitized.isNotBlank()) sanitized else "user_${uid.take(6)}"
    return "@$base"
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
  userRepository: UserRepository,
  onAuthSuccess: (UserProfile) -> Unit,
  startInProfileSetup: Boolean = false,
  isCheckingAutoSignIn: Boolean = false,
  currentPreset: AppThemePreset = AppThemePreset.DISCORD_DARK,
  currentAmoled: Boolean = false,
  currentAccent: AccentColorChoice = AccentColorChoice.DEFAULT,
  onThemeChanged: ((AppThemePreset, Boolean, AccentColorChoice) -> Unit)? = null,
  onLanguageToggle: (() -> Unit)? = null,
  onSwitchAccount: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val isArabic = LocalAppLanguage.current == AppLanguage.AR
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var needsProfileSetup by remember(startInProfileSetup) { mutableStateOf(startInProfileSetup) }

  var selectedFeature by remember { mutableStateOf(AuthFeatureHighlight.VOICE_HD) }

  var displayNameInput by remember { mutableStateOf("") }
  var handleInput by remember { mutableStateOf("") }
  var bioInput by remember { mutableStateOf("") }
  var professionInput by remember { mutableStateOf("") }
  var selectedAvatarUrl by remember { mutableStateOf("") }
  var googleDefaultPhotoUrl by remember { mutableStateOf("") }
  var startInQuietMode by remember { mutableStateOf(false) }

  LaunchedEffect(startInProfileSetup) {
    if (startInProfileSetup) {
      val fbUser = Firebase.auth.currentUser
      if (fbUser != null) {
        if (displayNameInput.isBlank()) {
          displayNameInput = fbUser.displayName?.takeIf { it.isNotBlank() } ?: ""
        }
        if (handleInput.isBlank()) {
          handleInput = AuthOnboardingDefaults.suggestHandle(fbUser.displayName, fbUser.uid)
        }
        val photo = fbUser.photoUrl?.toString().orEmpty()
        googleDefaultPhotoUrl = photo
        if (selectedAvatarUrl.isBlank() && photo.isNotBlank()) {
          selectedAvatarUrl = photo
        }
      }
    }
  }

  fun executeGoogleSignIn(clearStateFirst: Boolean = false) {
    val clientId = try {
      val clientIdRes = context.resources.getIdentifier(
        "default_web_client_id",
        "string",
        context.packageName
      )
      if (clientIdRes != 0) context.getString(clientIdRes) else ""
    } catch (e: Exception) {
      ""
    }
    if (clientId.isBlank()) {
      errorMessage = if (isArabic) {
        "إعدادات تسجيل الدخول عبر Google غير مكتملة في هذه النسخة."
      } else {
        "Google Web Client ID missing in configuration"
      }
      return
    }

    isLoading = true
    errorMessage = null

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    coroutineScope.launch {
      try {
        if (clearStateFirst) {
          runCatching { credentialManager.clearCredentialState(ClearCredentialStateRequest()) }
        }
        val result = credentialManager.getCredential(context as Activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          val authResult = Firebase.auth.signInWithCredential(authCredential).await()
          val firebaseUser = authResult.user

          if (firebaseUser != null) {
            val existingProfile = userRepository.getUserProfile(firebaseUser.uid)
            if (existingProfile != null) {
              isLoading = false
              onAuthSuccess(existingProfile)
            } else {
              displayNameInput = firebaseUser.displayName ?: ""
              handleInput = AuthOnboardingDefaults.suggestHandle(firebaseUser.displayName, firebaseUser.uid)
              val photo = firebaseUser.photoUrl?.toString().orEmpty()
              googleDefaultPhotoUrl = photo
              selectedAvatarUrl = photo
              needsProfileSetup = true
              isLoading = false
            }
          } else {
            isLoading = false
            errorMessage = "Authentication failed: User is null"
          }
        } else {
          isLoading = false
          errorMessage = "Unexpected credential format"
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w("Auth", "Sign in cancelled: ${e.message}", e)
        isLoading = false
      } catch (e: NoCredentialException) {
        Log.w("Auth", "No Google credentials on device", e)
        isLoading = false
        errorMessage = if (isArabic) {
          "لا يوجد حساب Google مسجّل على هذا الجهاز. أضف حساب Google من إعدادات الجهاز ثم أعد المحاولة."
        } else {
          "No Google account available on this device. Add a Google account in device Settings and try again."
        }
      } catch (e: Exception) {
        Log.e("Auth", "Sign in error", e)
        isLoading = false
        errorMessage = e.localizedMessage ?: "Sign in failed"
      }
    }
  }

  fun submitProfileSetup() {
    if (displayNameInput.isBlank() || handleInput.isBlank()) {
      errorMessage = if (isArabic) {
        "الاسم الظاهر والمعرّف مطلوبان"
      } else {
        "Display name and handle are required"
      }
      return
    }

    val fbUser = Firebase.auth.currentUser ?: return
    isLoading = true
    errorMessage = null

    coroutineScope.launch {
      val trimmedHandle = handleInput.trim().replace(" ", "")
      val cleanHandle = if (trimmedHandle.startsWith("@")) trimmedHandle else "@$trimmedHandle"
      val newProfile = UserProfile(
        id = fbUser.uid,
        displayName = displayNameInput.trim(),
        handle = cleanHandle,
        bio = bioInput.trim(),
        profession = professionInput.trim(),
        avatarUrl = selectedAvatarUrl.ifBlank { fbUser.photoUrl?.toString().orEmpty() },
        quietMode = startInQuietMode,
        readReceiptsEnabled = true
      )

      val result = userRepository.createOrUpdateProfile(newProfile)
      isLoading = false
      if (result.isSuccess) {
        onAuthSuccess(newProfile)
      } else {
        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to save profile"
      }
    }
  }

  val colors = MaterialTheme.colorScheme
  val primaryColor = colors.primary
  val secondaryColor = colors.secondary
  val bgColor = colors.background
  val isLightTheme = !currentPreset.isDark

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(bgColor)
  ) {
    // Architectural obsidian backdrop artwork on dark presets
    if (!isLightTheme) {
      Image(
        painter = painterResource(id = R.drawable.img_luxury_intro_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .alpha(if (currentAmoled || currentPreset == AppThemePreset.OLED_MIDNIGHT) 0.24f else 0.40f)
      )
    }

    // Soft atmospheric gradient scrim tuned to the active theme
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              bgColor.copy(alpha = if (isLightTheme) 0.94f else 0.72f),
              bgColor.copy(alpha = if (isLightTheme) 0.98f else 0.58f),
              bgColor.copy(alpha = 0.96f)
            )
          )
        )
        .background(
          Brush.radialGradient(
            colors = listOf(
              primaryColor.copy(alpha = if (isLightTheme) 0.12f else 0.22f),
              secondaryColor.copy(alpha = 0.05f),
              Color.Transparent
            ),
            center = Offset(480f, 260f),
            radius = 960f
          )
        )
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .navigationBarsPadding()
        .imePadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Youthful Control Strip: Brand Mark + Instant OLED & Language Switchers
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 520.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(11.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f))
          ) {
            Image(
              painter = painterResource(id = R.drawable.kora_icon),
              contentDescription = strings.appName,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
          Column {
            Text(
              text = "KORA",
              style = MaterialTheme.typography.titleMedium.copy(
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold
              ),
              color = colors.onBackground
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (onThemeChanged != null && currentPreset.isDark) {
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = if (currentAmoled) primaryColor.copy(alpha = 0.18f) else colors.surface.copy(alpha = 0.85f),
              border = BorderStroke(
                1.dp,
                if (currentAmoled) primaryColor.copy(alpha = 0.7f) else colors.outline.copy(alpha = 0.65f)
              ),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(999.dp))
                .clickable {
                  onThemeChanged(currentPreset, !currentAmoled, currentAccent)
                }
                .testTag("auth_amoled_toggle")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DarkMode,
                  contentDescription = strings.amoledChipLabel,
                  tint = if (currentAmoled) primaryColor else colors.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = strings.amoledChipLabel,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = if (currentAmoled) primaryColor else colors.onSurface
                )
              }
            }
          }

          if (onLanguageToggle != null) {
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = colors.surface.copy(alpha = 0.85f),
              border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.65f)),
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(999.dp))
                .clickable(onClick = onLanguageToggle)
                .testTag("auth_language_toggle")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Translate,
                  contentDescription = strings.language,
                  tint = primaryColor,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = if (isArabic) "EN" else "العربية",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = colors.onSurface
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(22.dp))

      if (isCheckingAutoSignIn && !needsProfileSetup) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
          shape = RoundedCornerShape(24.dp),
          color = colors.surface.copy(alpha = 0.90f),
          border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(30.dp),
              color = primaryColor,
              strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = strings.checkingAccount,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.onSurfaceVariant
            )
          }
        }
      } else if (!needsProfileSetup) {
        // ============================================================
        // Pre-Auth Editorial Welcome + Live Youthful Vibe Customizer
        // ============================================================
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
        ) {
          // Editorial Kicker Pill
          Surface(
            shape = RoundedCornerShape(999.dp),
            color = primaryColor.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.38f))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(primaryColor)
              )
              Text(
                text = strings.welcomeKicker,
                style = MaterialTheme.typography.labelSmall.copy(
                  letterSpacing = 1.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = primaryColor
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Text(
            text = strings.welcomeHeadline,
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.Bold,
              lineHeight = 38.sp
            ),
            color = colors.onBackground
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = strings.welcomeSubheadline,
            style = MaterialTheme.typography.bodyMedium.copy(
              lineHeight = 22.sp
            ),
            color = colors.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Interactive Feature Highlight Tabs + Animated Glass Spotlight
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            AuthFeatureHighlight.entries.forEach { feature ->
              val isSelected = selectedFeature == feature
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) primaryColor.copy(alpha = 0.16f) else colors.surface.copy(alpha = 0.72f),
                border = BorderStroke(
                  width = if (isSelected) 1.4.dp else 1.dp,
                  color = if (isSelected) primaryColor else colors.outlineVariant.copy(alpha = 0.55f)
                ),
                modifier = Modifier
                  .weight(1f)
                  .minimumInteractiveComponentSize()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { selectedFeature = feature }
                  .testTag("auth_feature_${feature.name.lowercase()}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title(strings),
                    tint = if (isSelected) primaryColor else colors.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = feature.title(strings),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.surface.copy(alpha = if (isLightTheme) 0.94f else 0.78f),
            border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f))
          ) {
            AnimatedContent(
              targetState = selectedFeature,
              transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(140))
              },
              label = "feature_spotlight"
            ) { feature ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.16f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = feature.title(strings),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = feature.description(strings),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }

          // Live Youthful Vibe / Theme Customizer right on Login Screen
          if (onThemeChanged != null) {
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp),
              color = colors.surface.copy(alpha = if (isLightTheme) 0.95f else 0.80f),
              border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.6f))
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 14.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Palette,
                      contentDescription = null,
                      tint = primaryColor,
                      modifier = Modifier.size(18.dp)
                    )
                    Text(
                      text = strings.pickYourVibe,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = colors.onSurface
                    )
                  }
                  Text(
                    text = if (isArabic) currentPreset.nameAr else currentPreset.nameEn,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                  contentPadding = PaddingValues(horizontal = 16.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  items(AuthOnboardingDefaults.themeVibes) { preset ->
                    val isSelected = currentPreset == preset
                    Surface(
                      shape = RoundedCornerShape(14.dp),
                      color = if (isSelected) primaryColor.copy(alpha = 0.16f) else colors.surfaceVariant.copy(alpha = 0.6f),
                      border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) primaryColor else colors.outline.copy(alpha = 0.4f)
                      ),
                      modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                          onThemeChanged(preset, currentAmoled, AccentColorChoice.DEFAULT)
                        }
                        .testTag("auth_vibe_${preset.id}")
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(preset.backgroundColor)
                            .border(1.5.dp, preset.primaryColor, CircleShape),
                          contentAlignment = Alignment.Center
                        ) {
                          Box(
                            modifier = Modifier
                              .size(7.dp)
                              .clip(CircleShape)
                              .background(preset.primaryColor)
                          )
                        }
                        Text(
                          text = if (isArabic) preset.nameAr else preset.nameEn,
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                          color = if (isSelected) primaryColor else colors.onSurface,
                          maxLines = 1
                        )
                      }
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                  contentPadding = PaddingValues(horizontal = 16.dp),
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  items(AuthOnboardingDefaults.accentSwatches) { accent ->
                    val isSelected = currentAccent == accent
                    val swatchColor = if (accent == AccentColorChoice.DEFAULT) {
                      currentPreset.primaryColor
                    } else {
                      accent.color
                    }
                    val checkTint = if (swatchColor.luminance() > 0.52f) Color(0xFF0B0F19) else Color.White

                    Box(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(
                          width = if (isSelected) 2.5.dp else 1.dp,
                          color = if (isSelected) colors.onSurface else colors.outline,
                          shape = CircleShape
                        )
                        .clickable {
                          onThemeChanged(currentPreset, currentAmoled, accent)
                        }
                        .testTag("auth_accent_${accent.id}"),
                      contentAlignment = Alignment.Center
                    ) {
                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.Check,
                          contentDescription = if (isArabic) accent.nameAr else accent.nameEn,
                          tint = checkTint,
                          modifier = Modifier.size(16.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Real Google Sign-In Action Dock
          Button(
            onClick = { executeGoogleSignIn(clearStateFirst = false) },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = primaryColor,
              contentColor = colors.onPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("google_sign_in_button"),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = colors.onPrimary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(strings.signingIn, fontWeight = FontWeight.SemiBold)
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = strings.signInWithGoogle,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = { executeGoogleSignIn(clearStateFirst = true) },
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.7f)),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("switch_google_account_button"),
            enabled = !isLoading
          ) {
            Icon(
              imageVector = Icons.Default.SwitchAccount,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
              tint = colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = strings.switchGoogleAccount,
              style = MaterialTheme.typography.labelLarge,
              color = colors.onSurface
            )
          }

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = colors.errorContainer.copy(alpha = 0.25f),
              border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.VerifiedUser,
              contentDescription = null,
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = strings.privacySecureBadge,
              style = MaterialTheme.typography.labelSmall,
              color = colors.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
        }
      } else {
        // ============================================================
        // Youthful Profile Setup Card
        // ============================================================
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
          shape = RoundedCornerShape(24.dp),
          color = colors.surface.copy(alpha = if (isLightTheme) 0.96f else 0.88f),
          border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = strings.createProfile,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = colors.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = strings.setupStepSubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = colors.onSurfaceVariant,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            AvatarView(
              name = displayNameInput.ifBlank { "K" },
              avatarUrl = selectedAvatarUrl,
              size = 78.dp,
              showPresence = true,
              isOnline = !startInQuietMode,
              isQuiet = startInQuietMode
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = strings.chooseAvatarOptional,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = colors.onSurfaceVariant,
              modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              item {
                val isDefaultSelected = !selectedAvatarUrl.startsWith("preset:")
                Surface(
                  shape = CircleShape,
                  color = if (isDefaultSelected) primaryColor.copy(alpha = 0.16f) else colors.surfaceVariant,
                  border = BorderStroke(
                    width = if (isDefaultSelected) 2.dp else 1.dp,
                    color = if (isDefaultSelected) primaryColor else colors.outline.copy(alpha = 0.5f)
                  ),
                  modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable { selectedAvatarUrl = googleDefaultPhotoUrl }
                    .testTag("setup_avatar_default")
                ) {
                  if (googleDefaultPhotoUrl.isNotBlank()) {
                    AvatarView(
                      name = displayNameInput.ifBlank { "K" },
                      avatarUrl = googleDefaultPhotoUrl,
                      size = 52.dp
                    )
                  } else {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = strings.googlePhotoOption,
                        tint = if (isDefaultSelected) primaryColor else colors.onSurfaceVariant,
                        modifier = Modifier.size(26.dp)
                      )
                    }
                  }
                }
              }

              items(PresetAvatars.list) { presetAvatar ->
                val isSelected = selectedAvatarUrl == presetAvatar.id
                Box(
                  modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(presetAvatar.gradient))
                    .border(
                      width = if (isSelected) 2.5.dp else 1.dp,
                      color = if (isSelected) Color.White else Color.Transparent,
                      shape = CircleShape
                    )
                    .clickable { selectedAvatarUrl = presetAvatar.id }
                    .testTag("setup_avatar_${presetAvatar.id}"),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = presetAvatar.icon,
                    contentDescription = if (isArabic) presetAvatar.nameAr else presetAvatar.nameEn,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedTextField(
              value = displayNameInput,
              onValueChange = { displayNameInput = it },
              label = { Text(strings.enterDisplayName) },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("display_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = handleInput,
              onValueChange = { handleInput = it },
              label = { Text(strings.enterHandle) },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("handle_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = strings.quickRoleLabel,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = colors.onSurfaceVariant,
              modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              AuthOnboardingDefaults.quickRoles.forEach { role ->
                val roleLabel = role.labelProvider(strings)
                val isSelected = professionInput.equals(roleLabel, ignoreCase = true)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    professionInput = if (isSelected) "" else roleLabel
                  },
                  label = {
                    Text(
                      text = roleLabel,
                      style = MaterialTheme.typography.labelMedium
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = primaryColor.copy(alpha = 0.18f),
                    selectedLabelColor = primaryColor
                  ),
                  modifier = Modifier.testTag("quick_role_${role.id}")
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = professionInput,
              onValueChange = { professionInput = it },
              label = { Text(strings.enterProfession) },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profession_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = bioInput,
              onValueChange = { bioInput = it },
              label = { Text(strings.enterBio) },
              maxLines = 3,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("bio_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Start in Quiet Mode option
            Surface(
              shape = RoundedCornerShape(16.dp),
              color = colors.surfaceVariant.copy(alpha = 0.55f),
              border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = strings.startQuietModeToggle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                  )
                  Text(
                    text = strings.startQuietModeDesc,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                  checked = startInQuietMode,
                  onCheckedChange = { startInQuietMode = it },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = StatusQuiet
                  ),
                  modifier = Modifier.testTag("setup_quiet_mode_switch")
                )
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
              onClick = { submitProfileSetup() },
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_profile_button"),
              enabled = !isLoading
            ) {
              if (isLoading) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  color = colors.onPrimary,
                  strokeWidth = 2.dp
                )
              } else {
                Text(strings.saveProfile, fontWeight = FontWeight.Bold)
              }
            }

            if (onSwitchAccount != null) {
              Spacer(modifier = Modifier.height(8.dp))
              TextButton(
                onClick = onSwitchAccount,
                modifier = Modifier.minimumInteractiveComponentSize()
              ) {
                Text(strings.signOut)
              }
            }

            if (errorMessage != null) {
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = errorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = colors.error,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }
    }
  }
}
