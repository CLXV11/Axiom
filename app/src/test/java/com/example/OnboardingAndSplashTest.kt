package com.example

import com.example.ui.screens.AuthFeatureHighlight
import com.example.ui.screens.AuthOnboardingDefaults
import com.example.ui.screens.mapFirebaseAuthError
import com.example.ui.screens.mapFirebaseAuthErrorCode
import com.example.ui.theme.AppThemePreset
import com.example.ui.util.ArabicStrings
import com.example.ui.util.EnglishStrings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingAndSplashTest {

  @Test
  fun testLocalizationSplashAndOnboardingStringsNonBlank() {
    listOf(EnglishStrings, ArabicStrings).forEach { strings ->
      assertTrue(strings.splashEditorialLine.isNotBlank())
      assertTrue(strings.splashTapToSkip.isNotBlank())
      assertTrue(strings.welcomeKicker.isNotBlank())
      assertTrue(strings.welcomeHeadline.isNotBlank())
      assertTrue(strings.welcomeSubheadline.isNotBlank())
      assertTrue(strings.pickYourVibe.isNotBlank())
      assertTrue(strings.amoledChipLabel.isNotBlank())
      assertTrue(strings.switchGoogleAccount.isNotBlank())
      assertTrue(strings.privacySecureBadge.isNotBlank())
      assertTrue(strings.chooseAvatarOptional.isNotBlank())
      assertTrue(strings.quickRoleLabel.isNotBlank())
      assertTrue(strings.startQuietModeToggle.isNotBlank())
      assertTrue(strings.startQuietModeDesc.isNotBlank())
      assertTrue(strings.setupStepSubtitle.isNotBlank())
      assertTrue(strings.authSignInTab.isNotBlank())
      assertTrue(strings.authCreateAccountTab.isNotBlank())
      assertTrue(strings.authEmailLabel.isNotBlank())
      assertTrue(strings.authPasswordLabel.isNotBlank())
      assertTrue(strings.authConfirmPasswordLabel.isNotBlank())
      assertTrue(strings.authForgotPassword.isNotBlank())
      assertTrue(strings.authRecoverAccount.isNotBlank())
      assertTrue(strings.authSendResetLink.isNotBlank())
      assertTrue(strings.authRecoverViaEmailBtn.isNotBlank())
      assertTrue(strings.authRecoverViaGoogleBtn.isNotBlank())
      assertTrue(strings.authBackToSignIn.isNotBlank())
    }
  }

  @Test
  fun testMapFirebaseAuthError_returnsLocalizedMessages() {
    assertTrue(mapFirebaseAuthErrorCode("ERROR_USER_NOT_FOUND", null, isArabic = true).contains("لا يوجد حساب"))
    assertTrue(mapFirebaseAuthErrorCode("ERROR_USER_NOT_FOUND", null, isArabic = false).contains("No account found"))
    assertTrue(mapFirebaseAuthErrorCode("ERROR_EMAIL_ALREADY_IN_USE", null, isArabic = true).contains("مسجّل"))
    assertTrue(mapFirebaseAuthErrorCode("ERROR_WEAK_PASSWORD", null, isArabic = false).contains("6 characters"))
    assertEquals(
      "Custom error",
      mapFirebaseAuthError(IllegalStateException("Custom error"), isArabic = false)
    )
  }

  @Test
  fun testAuthFeatureHighlightsHaveLocalizedTitlesAndDescriptions() {
    assertEquals(3, AuthFeatureHighlight.entries.size)
    AuthFeatureHighlight.entries.forEach { feature ->
      assertTrue(feature.title(EnglishStrings).isNotBlank())
      assertTrue(feature.description(EnglishStrings).isNotBlank())
      assertTrue(feature.title(ArabicStrings).isNotBlank())
      assertTrue(feature.description(ArabicStrings).isNotBlank())
    }
  }

  @Test
  fun testAuthOnboardingDefaultsThemeVibesExcludeSystem() {
    assertFalse(AuthOnboardingDefaults.themeVibes.contains(AppThemePreset.SYSTEM))
    assertTrue(AuthOnboardingDefaults.themeVibes.contains(AppThemePreset.DISCORD_DARK))
    assertTrue(AuthOnboardingDefaults.themeVibes.contains(AppThemePreset.OLED_MIDNIGHT))
    assertTrue(AuthOnboardingDefaults.accentSwatches.isNotEmpty())
  }

  @Test
  fun testAuthOnboardingQuickRoles() {
    assertEquals(6, AuthOnboardingDefaults.quickRoles.size)
    AuthOnboardingDefaults.quickRoles.forEach { role ->
      assertTrue(role.labelProvider(EnglishStrings).isNotBlank())
      assertTrue(role.labelProvider(ArabicStrings).isNotBlank())
    }
  }

  @Test
  fun testSuggestHandleFormatting() {
    assertEquals("@adamkora", AuthOnboardingDefaults.suggestHandle("Adam Kora!", "uid123456"))
    assertEquals("@user_abc123", AuthOnboardingDefaults.suggestHandle("سارة", "abc123xyz"))
    assertEquals("@user_999888", AuthOnboardingDefaults.suggestHandle(null, "999888777"))
  }
}
