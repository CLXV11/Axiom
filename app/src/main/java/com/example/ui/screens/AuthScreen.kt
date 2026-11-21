package com.example.ui.screens

import android.app.Activity
import android.util.Log
import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private enum class AuthCardView {
  MAIN,
  FORGOT_PASSWORD,
  RECOVER_ACCOUNT
}

internal fun mapFirebaseAuthError(error: Throwable, isArabic: Boolean): String {
  val code = (error as? FirebaseAuthException)?.errorCode.orEmpty()
  return mapFirebaseAuthErrorCode(code, error.localizedMessage, isArabic)
}

internal fun mapFirebaseAuthErrorCode(
  code: String,
  fallbackMessage: String?,
  isArabic: Boolean
): String {
  return when (code) {
    "ERROR_USER_NOT_FOUND" ->
      if (isArabic) "لا يوجد حساب مسجّل بهذا البريد الإلكتروني."
      else "No account found with this email address."
    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" ->
      if (isArabic) "البريد الإلكتروني أو كلمة السر غير صحيحة."
      else "Incorrect email or password."
    "ERROR_EMAIL_ALREADY_IN_USE" ->
      if (isArabic) "هذا البريد الإلكتروني مسجّل مسبقاً. جرّب تسجيل الدخول أو استرجاع الحساب."
      else "This email is already registered. Try signing in or recovering your account."
    "ERROR_INVALID_EMAIL" ->
      if (isArabic) "صيغة البريد الإلكتروني غير صحيحة."
      else "Invalid email address format."
    "ERROR_WEAK_PASSWORD" ->
      if (isArabic) "كلمة السر ضعيفة، استخدم 6 أحرف على الأقل."
      else "Password is too weak. Use at least 6 characters."
    "ERROR_OPERATION_NOT_ALLOWED" ->
      if (isArabic) "تسجيل الدخول بالبريد غير مفعّل حالياً؛ يمكنك الدخول أو الاسترجاع عبر Google."
      else "Email/password sign-in is not enabled on this project; sign in or recover via Google."
    "ERROR_TOO_MANY_REQUESTS" ->
      if (isArabic) "محاولات كثيرة متكررة، يرجى المحاولة بعد قليل."
      else "Too many attempts. Please try again in a moment."
    "ERROR_NETWORK_REQUEST_FAILED" ->
      if (isArabic) "تعذر الاتصال بالإنترنت. تحقق من الشبكة ثم أعد المحاولة."
      else "Network error. Check your connection and try again."
    else -> fallbackMessage ?: if (isArabic) "تعذر إتمام العملية" else "Authentication failed"
  }
}

@Composable
fun AuthScreen(
  userRepository: UserRepository,
  onAuthSuccess: (UserProfile) -> Unit,
  startInProfileSetup: Boolean = false,
  isCheckingAutoSignIn: Boolean = false,
  currentPreset: AppThemePreset = AppThemePreset.DISCORD_DARK,
  currentAmoled: Boolean = true,
  @Suppress("UNUSED_PARAMETER") currentAccent: AccentColorChoice = AccentColorChoice.DEFAULT,
  @Suppress("UNUSED_PARAMETER") onThemeChanged: ((AppThemePreset, Boolean, AccentColorChoice) -> Unit)? = null,
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
  var infoMessage by remember { mutableStateOf<String?>(null) }
  var needsProfileSetup by remember(startInProfileSetup) { mutableStateOf(startInProfileSetup) }

  var cardView by remember { mutableStateOf(AuthCardView.MAIN) }
  var isCreateAccountMode by remember { mutableStateOf(false) }
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var confirmPasswordInput by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var displayNameInput by remember { mutableStateOf("") }
  var handleInput by remember { mutableStateOf("") }
  var bioInput by remember { mutableStateOf("") }
  var professionInput by remember { mutableStateOf("") }
  var selectedAvatarUrl by remember { mutableStateOf("") }
  var googleDefaultPhotoUrl by remember { mutableStateOf("") }
  var startInQuietMode by remember { mutableStateOf(false) }

  BackHandler(enabled = !needsProfileSetup && cardView != AuthCardView.MAIN) {
    cardView = AuthCardView.MAIN
    errorMessage = null
    infoMessage = null
  }

  LaunchedEffect(startInProfileSetup) {
    if (startInProfileSetup) {
      val fbUser = Firebase.auth.currentUser
      if (fbUser != null) {
        if (displayNameInput.isBlank()) {
          displayNameInput = fbUser.displayName?.takeIf { it.isNotBlank() }
            ?: fbUser.email?.substringBefore("@").orEmpty()
        }
        if (handleInput.isBlank()) {
          handleInput = AuthOnboardingDefaults.suggestHandle(
            displayNameInput.ifBlank { fbUser.displayName },
            fbUser.uid
          )
        }
        val photo = fbUser.photoUrl?.toString().orEmpty()
        googleDefaultPhotoUrl = photo
        if (selectedAvatarUrl.isBlank() && photo.isNotBlank()) {
          selectedAvatarUrl = photo
        }
      }
    }
  }

  suspend fun finishFirebaseUserAuth(firebaseUser: com.google.firebase.auth.FirebaseUser) {
    val existingProfile = userRepository.getUserProfile(firebaseUser.uid)
    if (existingProfile != null) {
      isLoading = false
      onAuthSuccess(existingProfile)
    } else {
      val fallbackName = firebaseUser.displayName?.takeIf { it.isNotBlank() }
        ?: firebaseUser.email?.substringBefore("@").orEmpty()
      displayNameInput = fallbackName
      handleInput = AuthOnboardingDefaults.suggestHandle(fallbackName, firebaseUser.uid)
      val photo = firebaseUser.photoUrl?.toString().orEmpty()
      googleDefaultPhotoUrl = photo
      selectedAvatarUrl = photo
      needsProfileSetup = true
      isLoading = false
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
    } catch (_: Exception) {
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
    infoMessage = null

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
            finishFirebaseUserAuth(firebaseUser)
          } else {
            isLoading = false
            errorMessage = if (isArabic) "تعذر العثور على بيانات الحساب" else "Authentication failed: User is null"
          }
        } else {
          isLoading = false
          errorMessage = if (isArabic) "صيغة بيانات الدخول غير مدعومة" else "Unexpected credential format"
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
        errorMessage = mapFirebaseAuthError(e, isArabic)
      }
    }
  }

  fun submitEmailPasswordAuth() {
    val trimmedEmail = emailInput.trim()
    if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
      errorMessage = strings.authInvalidEmailError
      infoMessage = null
      return
    }
    if (passwordInput.length < 6) {
      errorMessage = strings.authWeakPasswordError
      infoMessage = null
      return
    }
    if (isCreateAccountMode && passwordInput != confirmPasswordInput) {
      errorMessage = strings.authPasswordMismatchError
      infoMessage = null
      return
    }

    isLoading = true
    errorMessage = null
    infoMessage = null

    coroutineScope.launch {
      try {
        val authResult = if (isCreateAccountMode) {
          Firebase.auth.createUserWithEmailAndPassword(trimmedEmail, passwordInput).await()
        } else {
          Firebase.auth.signInWithEmailAndPassword(trimmedEmail, passwordInput).await()
        }
        val firebaseUser = authResult.user
        if (firebaseUser != null) {
          finishFirebaseUserAuth(firebaseUser)
        } else {
          isLoading = false
          errorMessage = if (isArabic) "تعذر إتمام تسجيل الدخول" else "Authentication failed"
        }
      } catch (e: Exception) {
        Log.e("Auth", "Email auth failed", e)
        isLoading = false
        errorMessage = mapFirebaseAuthError(e, isArabic)
      }
    }
  }

  fun sendRecoveryOrResetEmail() {
    val trimmedEmail = emailInput.trim()
    if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
      errorMessage = strings.authInvalidEmailError
      infoMessage = null
      return
    }

    isLoading = true
    errorMessage = null
    infoMessage = null

    coroutineScope.launch {
      try {
        Firebase.auth.sendPasswordResetEmail(trimmedEmail).await()
        isLoading = false
        infoMessage = strings.authResetLinkSent
      } catch (e: Exception) {
        Log.e("Auth", "Password reset failed", e)
        isLoading = false
        errorMessage = mapFirebaseAuthError(e, isArabic)
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
    if (!isLightTheme) {
      Image(
        painter = painterResource(id = R.drawable.img_luxury_intro_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .fillMaxSize()
          .alpha(if (currentAmoled || currentPreset == AppThemePreset.OLED_MIDNIGHT) 0.20f else 0.34f)
      )
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              bgColor.copy(alpha = if (isLightTheme) 0.94f else 0.78f),
              bgColor.copy(alpha = if (isLightTheme) 0.98f else 0.68f),
              bgColor.copy(alpha = 0.96f)
            )
          )
        )
        .background(
          Brush.radialGradient(
            colors = listOf(
              primaryColor.copy(alpha = if (isLightTheme) 0.10f else 0.18f),
              secondaryColor.copy(alpha = 0.04f),
              Color.Transparent
            ),
            center = Offset(480f, 240f),
            radius = 900f
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
        .padding(horizontal = 20.dp, vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Minimal Top Bar: Brand Identity + Language Switcher Only
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 480.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
            color = colors.surface,
            border = BorderStroke(1.2.dp, primaryColor.copy(alpha = 0.5f))
          ) {
            Image(
              painter = painterResource(id = R.drawable.kora_icon),
              contentDescription = strings.appName,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }
          Text(
            text = "KORA",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontSize = 24.sp,
              letterSpacing = 3.5.sp,
              fontWeight = FontWeight.ExtraBold
            ),
            color = colors.onBackground
          )
        }

        if (onLanguageToggle != null) {
          Surface(
            shape = RoundedCornerShape(999.dp),
            color = colors.surface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.65f)),
            modifier = Modifier
              .minimumInteractiveComponentSize()
              .clip(RoundedCornerShape(999.dp))
              .clickable(onClick = onLanguageToggle)
              .testTag("auth_language_toggle")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Translate,
                contentDescription = strings.language,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = if (isArabic) "EN" else "العربية",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      if (isCheckingAutoSignIn && !needsProfileSetup) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
          shape = RoundedCornerShape(26.dp),
          color = colors.surface.copy(alpha = 0.92f),
          border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(36.dp),
              color = primaryColor,
              strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = strings.checkingAccount,
              style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
              color = colors.onSurfaceVariant
            )
          }
        }
      } else if (!needsProfileSetup) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 480.dp),
          shape = RoundedCornerShape(26.dp),
          color = colors.surface.copy(alpha = if (isLightTheme) 0.96f else 0.90f),
          border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f))
        ) {
          AnimatedContent(
            targetState = cardView,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
            label = "auth_card_view"
          ) { currentView ->
            when (currentView) {
              AuthCardView.MAIN -> {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = if (isCreateAccountMode) strings.authCreateAccountTab else strings.authSignInTab,
                    style = MaterialTheme.typography.headlineSmall.copy(
                      fontSize = 25.sp,
                      fontWeight = FontWeight.Bold
                    ),
                    color = colors.onSurface,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = strings.authSignInSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  // Clean Mode Toggle: Sign In vs Create Account
                  Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surfaceVariant.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      AuthModeSegmentButton(
                        selected = !isCreateAccountMode,
                        label = strings.authSignInTab,
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = {
                          isCreateAccountMode = false
                          errorMessage = null
                          infoMessage = null
                        },
                        modifier = Modifier
                          .weight(1f)
                          .testTag("auth_mode_sign_in")
                      )
                      AuthModeSegmentButton(
                        selected = isCreateAccountMode,
                        label = strings.authCreateAccountTab,
                        icon = Icons.Default.PersonAdd,
                        onClick = {
                          isCreateAccountMode = true
                          errorMessage = null
                          infoMessage = null
                        },
                        modifier = Modifier
                          .weight(1f)
                          .testTag("auth_mode_sign_up")
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(18.dp))

                  OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(strings.authEmailLabel, fontSize = 15.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                      )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                      keyboardType = KeyboardType.Email,
                      imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("auth_email_input")
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text(strings.authPasswordLabel, fontSize = 15.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                      )
                    },
                    trailingIcon = {
                      IconButton(
                        onClick = { passwordVisible = !passwordVisible },
                        modifier = Modifier.minimumInteractiveComponentSize()
                      ) {
                        Icon(
                          imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                          contentDescription = if (passwordVisible) strings.authHidePasswordDesc else strings.authShowPasswordDesc,
                          modifier = Modifier.size(23.dp)
                        )
                      }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                      keyboardType = KeyboardType.Password,
                      imeAction = if (isCreateAccountMode) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                      onDone = { if (!isCreateAccountMode && !isLoading) submitEmailPasswordAuth() }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("auth_password_input")
                  )

                  AnimatedVisibility(visible = isCreateAccountMode) {
                    Column {
                      Spacer(modifier = Modifier.height(14.dp))
                      OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text(strings.authConfirmPasswordLabel, fontSize = 15.sp) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        leadingIcon = {
                          Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(23.dp)
                          )
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                          keyboardType = KeyboardType.Password,
                          imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                          onDone = { if (!isLoading) submitEmailPasswordAuth() }
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                          .fillMaxWidth()
                          .testTag("auth_confirm_password_input")
                      )
                    }
                  }

                  AnimatedVisibility(visible = !isCreateAccountMode) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                      contentAlignment = Alignment.CenterEnd
                    ) {
                      TextButton(
                        onClick = {
                          errorMessage = null
                          infoMessage = null
                          cardView = AuthCardView.FORGOT_PASSWORD
                        },
                        modifier = Modifier
                          .minimumInteractiveComponentSize()
                          .testTag("auth_forgot_password_button")
                      ) {
                        Icon(
                          imageVector = Icons.Default.LockReset,
                          contentDescription = null,
                          modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = strings.authForgotPassword,
                          style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                          fontWeight = FontWeight.SemiBold
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(if (isCreateAccountMode) 18.dp else 10.dp))

                  Button(
                    onClick = { submitEmailPasswordAuth() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = primaryColor,
                      contentColor = colors.onPrimary
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(56.dp)
                      .testTag("auth_primary_submit_button"),
                    enabled = !isLoading
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.5.dp
                      )
                      Spacer(modifier = Modifier.width(12.dp))
                      Text(
                        text = if (isCreateAccountMode) strings.authCreatingAccount else strings.signingIn,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                      )
                    } else {
                      Icon(
                        imageVector = if (isCreateAccountMode) Icons.Default.PersonAdd else Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(
                        text = if (isCreateAccountMode) strings.authCreateAccountTab else strings.authSignInTab,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    HorizontalDivider(
                      modifier = Modifier.weight(1f),
                      color = colors.outlineVariant.copy(alpha = 0.6f)
                    )
                    Text(
                      text = strings.authOrDivider,
                      style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                      color = colors.onSurfaceVariant,
                      modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                      modifier = Modifier.weight(1f),
                      color = colors.outlineVariant.copy(alpha = 0.6f)
                    )
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  OutlinedButton(
                    onClick = { executeGoogleSignIn(clearStateFirst = false) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, colors.outline.copy(alpha = 0.75f)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(54.dp)
                      .testTag("google_sign_in_button"),
                    enabled = !isLoading
                  ) {
                    Icon(
                      imageVector = Icons.Default.AccountCircle,
                      contentDescription = null,
                      tint = primaryColor,
                      modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = strings.signInWithGoogle,
                      style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                      fontWeight = FontWeight.Bold,
                      color = colors.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  OutlinedButton(
                    onClick = {
                      errorMessage = null
                      infoMessage = null
                      cardView = AuthCardView.RECOVER_ACCOUNT
                    },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.45f)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(52.dp)
                      .testTag("auth_recover_account_button"),
                    enabled = !isLoading
                  ) {
                    Icon(
                      imageVector = Icons.Default.Restore,
                      contentDescription = null,
                      tint = primaryColor,
                      modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = strings.authRecoverAccount,
                      style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                      fontWeight = FontWeight.Bold,
                      color = primaryColor
                    )
                  }

                  AuthStatusBanners(
                    errorMessage = errorMessage,
                    infoMessage = infoMessage
                  )
                }
              }

              AuthCardView.FORGOT_PASSWORD -> {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  AuthSubViewHeader(
                    icon = Icons.Default.LockReset,
                    title = strings.authForgotPasswordTitle,
                    description = strings.authForgotPasswordDesc,
                    backLabel = strings.authBackToSignIn,
                    onBack = {
                      errorMessage = null
                      infoMessage = null
                      cardView = AuthCardView.MAIN
                    }
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(strings.authEmailLabel, fontSize = 15.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                      )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                      keyboardType = KeyboardType.Email,
                      imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                      onDone = { if (!isLoading) sendRecoveryOrResetEmail() }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("auth_forgot_email_input")
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  Button(
                    onClick = { sendRecoveryOrResetEmail() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = primaryColor,
                      contentColor = colors.onPrimary
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(56.dp)
                      .testTag("auth_forgot_submit_button"),
                    enabled = !isLoading
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.5.dp
                      )
                      Spacer(modifier = Modifier.width(12.dp))
                      Text(
                        text = strings.authSendingResetLink,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                      )
                    } else {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(
                        text = strings.authSendResetLink,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }

                  AuthStatusBanners(
                    errorMessage = errorMessage,
                    infoMessage = infoMessage
                  )
                }
              }

              AuthCardView.RECOVER_ACCOUNT -> {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  AuthSubViewHeader(
                    icon = Icons.Default.ManageAccounts,
                    title = strings.authRecoverAccountTitle,
                    description = strings.authRecoverAccountDesc,
                    backLabel = strings.authBackToSignIn,
                    onBack = {
                      errorMessage = null
                      infoMessage = null
                      cardView = AuthCardView.MAIN
                    }
                  )

                  Spacer(modifier = Modifier.height(20.dp))

                  OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(strings.authEmailLabel, fontSize = 15.sp) },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    leadingIcon = {
                      Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                      )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                      keyboardType = KeyboardType.Email,
                      imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                      onDone = { if (!isLoading) sendRecoveryOrResetEmail() }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("auth_recovery_email_input")
                  )

                  Spacer(modifier = Modifier.height(18.dp))

                  Button(
                    onClick = { sendRecoveryOrResetEmail() },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                      containerColor = primaryColor,
                      contentColor = colors.onPrimary
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(56.dp)
                      .testTag("auth_recovery_send_button"),
                    enabled = !isLoading
                  ) {
                    if (isLoading) {
                      CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.onPrimary,
                        strokeWidth = 2.5.dp
                      )
                      Spacer(modifier = Modifier.width(12.dp))
                      Text(
                        text = strings.authSendingResetLink,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                        fontWeight = FontWeight.Bold
                      )
                    } else {
                      Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                      )
                      Spacer(modifier = Modifier.width(10.dp))
                      Text(
                        text = strings.authRecoverViaEmailBtn,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  OutlinedButton(
                    onClick = { executeGoogleSignIn(clearStateFirst = true) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, colors.outline.copy(alpha = 0.75f)),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(54.dp)
                      .testTag("auth_recovery_google_button"),
                    enabled = !isLoading
                  ) {
                    Icon(
                      imageVector = Icons.Default.SwitchAccount,
                      contentDescription = null,
                      tint = primaryColor,
                      modifier = Modifier.size(23.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                      text = strings.authRecoverViaGoogleBtn,
                      style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                      fontWeight = FontWeight.Bold,
                      color = colors.onSurface
                    )
                  }

                  AuthStatusBanners(
                    errorMessage = errorMessage,
                    infoMessage = infoMessage
                  )
                }
              }
            }
          }
        }
      } else {
        AuthProfileSetupCard(
          strings = strings,
          isArabic = isArabic,
          isLightTheme = isLightTheme,
          isLoading = isLoading,
          errorMessage = errorMessage,
          displayNameInput = displayNameInput,
          onDisplayNameChange = { displayNameInput = it },
          handleInput = handleInput,
          onHandleChange = { handleInput = it },
          professionInput = professionInput,
          onProfessionChange = { professionInput = it },
          bioInput = bioInput,
          onBioChange = { bioInput = it },
          selectedAvatarUrl = selectedAvatarUrl,
          googleDefaultPhotoUrl = googleDefaultPhotoUrl,
          onSelectAvatar = { selectedAvatarUrl = it },
          startInQuietMode = startInQuietMode,
          onQuietModeChange = { startInQuietMode = it },
          onSubmitProfile = { submitProfileSetup() },
          onSwitchAccount = onSwitchAccount
        )
      }
    }
  }
}

@Composable
private fun AuthModeSegmentButton(
  selected: Boolean,
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.colorScheme
  Surface(
    shape = RoundedCornerShape(13.dp),
    color = if (selected) colors.primary else Color.Transparent,
    modifier = modifier
      .height(50.dp)
      .clip(RoundedCornerShape(13.dp))
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        modifier = Modifier.size(21.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.5.sp),
        fontWeight = FontWeight.Bold,
        color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun AuthSubViewHeader(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  description: String,
  backLabel: String,
  onBack: () -> Unit
) {
  val colors = MaterialTheme.colorScheme
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TextButton(
        onClick = onBack,
        modifier = Modifier
          .minimumInteractiveComponentSize()
          .testTag("auth_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = backLabel,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = backLabel,
          style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Box(
      modifier = Modifier
        .size(60.dp)
        .clip(CircleShape)
        .background(colors.primary.copy(alpha = 0.16f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = colors.primary,
        modifier = Modifier.size(30.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmall.copy(
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold
      ),
      color = colors.onSurface,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 15.sp,
        lineHeight = 22.sp
      ),
      color = colors.onSurfaceVariant,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun AuthStatusBanners(
  errorMessage: String?,
  infoMessage: String?
) {
  val colors = MaterialTheme.colorScheme
  if (errorMessage != null) {
    Spacer(modifier = Modifier.height(16.dp))
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = colors.errorContainer.copy(alpha = 0.28f),
      border = BorderStroke(1.dp, colors.error.copy(alpha = 0.55f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = errorMessage,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
        color = colors.error,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
      )
    }
  }

  if (infoMessage != null) {
    Spacer(modifier = Modifier.height(16.dp))
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = Color(0xFF10B981).copy(alpha = 0.16f),
      border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.55f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = Color(0xFF10B981),
          modifier = Modifier.size(22.dp)
        )
        Text(
          text = infoMessage,
          style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.5.sp),
          color = colors.onSurface,
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}
