package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage

@Composable
fun MissingFirebaseConfigScreen(
  onLanguageToggle: () -> Unit
) {
  val isArabic = LocalAppLanguage.current == AppLanguage.AR

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 24.dp, vertical = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp),
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 2.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          FilledTonalIconButton(
            onClick = onLanguageToggle,
            modifier = Modifier
              .size(48.dp)
              .testTag("missing_config_language_toggle")
          ) {
            Icon(
              imageVector = Icons.Default.Translate,
              contentDescription = if (isArabic) "تغيير اللغة" else "Switch language"
            )
          }
        }

        Box(
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(32.dp)
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = if (isArabic) {
            "ملف إعدادات Firebase غير مدمج"
          } else {
            "Firebase Configuration Missing"
          },
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = if (isArabic) {
            "تم بناء هذا الـ APK بدون ملف app/google-services.json، لذلك لم يتم تهيئة Firebase أو تسجيل الدخول عبر Google."
          } else {
            "This APK was built without app/google-services.json, so Firebase and Google Sign-In are not initialized in this package."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        ConfigStepCard(
          stepNumber = "1",
          title = if (isArabic) "الطريقة الأسرع: البناء من AI Studio" else "Fastest: Build from AI Studio",
          body = if (isArabic) {
            "افتح قائمة الإعدادات في Google AI Studio واختر Generate APK للحصول على ملف APK جاهز وموقّع بملف Firebase الكامل."
          } else {
            "Use Settings -> Generate APK in Google AI Studio to export an APK with Firebase and signing already bundled."
          }
        )

        Spacer(modifier = Modifier.height(12.dp))

        ConfigStepCard(
          stepNumber = "2",
          title = if (isArabic) "أو عبر GitHub Secrets" else "Or via GitHub Secrets",
          body = if (isArabic) {
            "في مستودع GitHub افتح Settings -> Secrets and variables -> Actions وأضف GOOGLE_SERVICES_JSON و DEBUG_KEYSTORE_BASE64 ثم أعد تشغيل البناء."
          } else {
            "In your GitHub repository, open Settings -> Secrets and variables -> Actions, add GOOGLE_SERVICES_JSON and DEBUG_KEYSTORE_BASE64, then re-run Build Android APK."
          }
        )
      }
    }
  }
}

@Composable
private fun ConfigStepCard(
  stepNumber: String,
  title: String,
  body: String
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = stepNumber,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = body,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
