package com.example.ui.screens

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.util.AppLanguage
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

@Composable
fun AuthScreen(
  userRepository: UserRepository,
  onAuthSuccess: (UserProfile) -> Unit,
  startInProfileSetup: Boolean = false,
  isCheckingAutoSignIn: Boolean = false,
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

  var displayNameInput by remember { mutableStateOf("") }
  var handleInput by remember { mutableStateOf("") }
  var bioInput by remember { mutableStateOf("") }
  var professionInput by remember { mutableStateOf("") }

  fun suggestHandle(name: String?, uid: String): String {
    val sanitized = (name ?: "")
      .lowercase()
      .replace(Regex("[^a-z0-9_]"), "")
      .take(18)
    val base = if (sanitized.isNotBlank()) sanitized else "user_${uid.take(6)}"
    return "@$base"
  }

  LaunchedEffect(startInProfileSetup) {
    if (startInProfileSetup) {
      val fbUser = Firebase.auth.currentUser
      if (fbUser != null) {
        if (displayNameInput.isBlank()) {
          displayNameInput = fbUser.displayName?.takeIf { it.isNotBlank() } ?: ""
        }
        if (handleInput.isBlank()) {
          handleInput = suggestHandle(fbUser.displayName, fbUser.uid)
        }
      }
    }
  }

  fun executeGoogleSignIn() {
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
              handleInput = suggestHandle(firebaseUser.displayName, firebaseUser.uid)
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
        avatarUrl = fbUser.photoUrl?.toString().orEmpty(),
        quietMode = false,
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

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .imePadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 32.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
        .clip(RoundedCornerShape(24.dp)),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Monogram Brand Icon
        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Forum,
            contentDescription = "Kora",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = strings.appName,
          style = MaterialTheme.typography.headlineLarge,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = strings.tagline,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        if (isCheckingAutoSignIn && !needsProfileSetup) {
          CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 2.5.dp
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = strings.checkingAccount,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else if (!needsProfileSetup) {
          // Sign In Action
          Button(
            onClick = { executeGoogleSignIn() },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("google_sign_in_button"),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(12.dp))
              Text(strings.signingIn)
            } else {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(strings.signInWithGoogle, fontWeight = FontWeight.SemiBold)
              }
            }
          }
        } else {
          // Profile Setup Form
          Text(
            text = strings.createProfile,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(16.dp))

          OutlinedTextField(
            value = displayNameInput,
            onValueChange = { displayNameInput = it },
            label = { Text(strings.enterDisplayName) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("display_name_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = handleInput,
            onValueChange = { handleInput = it },
            label = { Text(strings.enterHandle) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("handle_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = bioInput,
            onValueChange = { bioInput = it },
            label = { Text(strings.enterBio) },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth().testTag("bio_input")
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = professionInput,
            onValueChange = { professionInput = it },
            label = { Text(strings.enterProfession) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("profession_input")
          )

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = { submitProfileSetup() },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("save_profile_button"),
            enabled = !isLoading
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
              )
            } else {
              Text(strings.saveProfile, fontWeight = FontWeight.SemiBold)
            }
          }

          if (onSwitchAccount != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSwitchAccount) {
              Text(strings.signOut)
            }
          }
        }

        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = errorMessage!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
