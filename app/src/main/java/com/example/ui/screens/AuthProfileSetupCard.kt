package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarView
import com.example.ui.components.PresetAvatars
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.StatusQuiet
import com.example.ui.util.KoraStrings

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
internal fun AuthProfileSetupCard(
  strings: KoraStrings,
  isArabic: Boolean,
  isLightTheme: Boolean,
  isLoading: Boolean,
  errorMessage: String?,
  displayNameInput: String,
  onDisplayNameChange: (String) -> Unit,
  handleInput: String,
  onHandleChange: (String) -> Unit,
  professionInput: String,
  onProfessionChange: (String) -> Unit,
  bioInput: String,
  onBioChange: (String) -> Unit,
  selectedAvatarUrl: String,
  googleDefaultPhotoUrl: String,
  onSelectAvatar: (String) -> Unit,
  startInQuietMode: Boolean,
  onQuietModeChange: (Boolean) -> Unit,
  onSubmitProfile: () -> Unit,
  onSwitchAccount: (() -> Unit)?
) {
  val colors = MaterialTheme.colorScheme
  val primaryColor = colors.primary

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .widthIn(max = 500.dp),
    shape = RoundedCornerShape(26.dp),
    color = colors.surface.copy(alpha = if (isLightTheme) 0.96f else 0.90f),
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
        style = MaterialTheme.typography.headlineSmall.copy(
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold
        ),
        color = colors.onSurface
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = strings.setupStepSubtitle,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
        color = colors.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(18.dp))

      AvatarView(
        name = displayNameInput.ifBlank { "K" },
        avatarUrl = selectedAvatarUrl,
        size = 82.dp,
        showPresence = true,
        isOnline = !startInQuietMode,
        isQuiet = startInQuietMode
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = strings.chooseAvatarOptional,
        style = MaterialTheme.typography.labelLarge,
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
              .size(54.dp)
              .clip(CircleShape)
              .clickable { onSelectAvatar(googleDefaultPhotoUrl) }
              .testTag("setup_avatar_default")
          ) {
            if (googleDefaultPhotoUrl.isNotBlank()) {
              AvatarView(
                name = displayNameInput.ifBlank { "K" },
                avatarUrl = googleDefaultPhotoUrl,
                size = 54.dp
              )
            } else {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.AccountCircle,
                  contentDescription = strings.googlePhotoOption,
                  tint = if (isDefaultSelected) primaryColor else colors.onSurfaceVariant,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
          }
        }

        items(PresetAvatars.list) { presetAvatar ->
          val isSelected = selectedAvatarUrl == presetAvatar.id
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Brush.linearGradient(presetAvatar.gradient))
              .border(
                width = if (isSelected) 2.5.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = CircleShape
              )
              .clickable { onSelectAvatar(presetAvatar.id) }
              .testTag("setup_avatar_${presetAvatar.id}"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = presetAvatar.icon,
              contentDescription = if (isArabic) presetAvatar.nameAr else presetAvatar.nameEn,
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      OutlinedTextField(
        value = displayNameInput,
        onValueChange = onDisplayNameChange,
        label = { Text(strings.enterDisplayName, fontSize = 15.sp) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("display_name_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = handleInput,
        onValueChange = onHandleChange,
        label = { Text(strings.enterHandle, fontSize = 15.sp) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("handle_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      Text(
        text = strings.quickRoleLabel,
        style = MaterialTheme.typography.labelLarge,
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
              onProfessionChange(if (isSelected) "" else roleLabel)
            },
            label = {
              Text(
                text = roleLabel,
                style = MaterialTheme.typography.labelLarge
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
        onValueChange = onProfessionChange,
        label = { Text(strings.enterProfession, fontSize = 15.sp) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("profession_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = bioInput,
        onValueChange = onBioChange,
        label = { Text(strings.enterBio, fontSize = 15.sp) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
        maxLines = 3,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("bio_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

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
              style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
              fontWeight = FontWeight.SemiBold,
              color = colors.onSurface
            )
            Text(
              text = strings.startQuietModeDesc,
              style = MaterialTheme.typography.bodySmall,
              color = colors.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Switch(
            checked = startInQuietMode,
            onCheckedChange = onQuietModeChange,
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
        onClick = onSubmitProfile,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("save_profile_button"),
        enabled = !isLoading
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = colors.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Text(
            text = strings.saveProfile,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      if (onSwitchAccount != null) {
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
          onClick = onSwitchAccount,
          modifier = Modifier.minimumInteractiveComponentSize()
        ) {
          Text(
            text = strings.signOut,
            style = MaterialTheme.typography.labelLarge
          )
        }
      }

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = errorMessage,
          style = MaterialTheme.typography.bodyMedium,
          color = colors.error,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
