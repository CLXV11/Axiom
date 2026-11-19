package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KoraTopBar
import com.example.ui.components.ThemePreviewCard
import com.example.ui.components.ThemeSwitcherSheet
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.KoraTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings

@Composable
fun SettingsScreen(
  currentThemeMode: String = "dark",
  currentPreset: AppThemePreset = AppThemePreset.DISCORD_DARK,
  currentAmoled: Boolean = false,
  currentAccent: AccentColorChoice = AccentColorChoice.DEFAULT,
  onThemeChanged: (AppThemePreset, Boolean, AccentColorChoice) -> Unit = { _, _, _ -> },
  onThemeModeChanged: (String) -> Unit = {},
  onLanguageChanged: (AppLanguage) -> Unit
) {
  val strings = LocalAppStrings.current
  val currentLanguage = LocalAppLanguage.current
  val isArabic = currentLanguage == AppLanguage.AR

  var showThemeSheet by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      KoraTopBar(title = strings.settingsTitle)
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      val colors = MaterialTheme.colorScheme
      val cardShape = RoundedCornerShape(16.dp)
      val cardBorder = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f))

      // Theme Studio Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(colors.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Palette,
                  contentDescription = null,
                  tint = colors.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.themeSwitcherTitle,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = colors.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = if (isArabic) currentPreset.nameAr else currentPreset.nameEn,
                  style = MaterialTheme.typography.labelSmall,
                  color = colors.primary,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
              onClick = { showThemeSheet = true },
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
              ),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = strings.quickThemeSwitcher,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(14.dp))

          // Live Interactive Theme Preview
          Text(
            text = strings.themePreview,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(6.dp))
          ThemePreviewCard(
            preset = currentPreset,
            amoledPureBlack = currentAmoled,
            accentChoice = currentAccent
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Presets Grid / Row
          Text(
            text = strings.themePresets,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppThemePreset.entries.forEach { preset ->
              val isSelected = currentPreset == preset
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                  else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    onThemeChanged(preset, currentAmoled, currentAccent)
                    onThemeModeChanged(if (!preset.isDark) "light" else if (preset == AppThemePreset.SYSTEM) "system" else "dark")
                  }
                  .testTag("settings_theme_${preset.id}")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Dual color circle
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(CircleShape)
                      .background(preset.backgroundColor)
                      .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                  ) {
                    Box(
                      modifier = Modifier
                        .size(15.dp)
                        .clip(CircleShape)
                        .background(preset.primaryColor)
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = if (isArabic) preset.nameAr else preset.nameEn,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = if (isArabic) preset.subtitleAr else preset.subtitleEn,
                      style = KoraTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  if (isSelected) {
                    Box(
                      modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Custom Accent Selector
          Text(
            text = strings.customAccentColor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(AccentColorChoice.entries) { choice ->
              val isSelected = currentAccent == choice
              val chipColor = if (choice == AccentColorChoice.DEFAULT) currentPreset.primaryColor else choice.color
              val checkTint = if (chipColor.luminance() > 0.52f) Color(0xFF0B0F19) else Color.White

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                  .clickable {
                    onThemeChanged(currentPreset, currentAmoled, choice)
                  }
                  .padding(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(chipColor)
                    .border(
                      width = if (isSelected) 2.5.dp else 1.dp,
                      color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                      shape = CircleShape
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = "Selected",
                      tint = checkTint,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = if (isArabic) choice.nameAr else choice.nameEn,
                  style = KoraTheme.typography.labelSmall.copy(fontSize = 9.sp),
                  color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  softWrap = false
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // AMOLED Mode Switch
          if (currentPreset.isDark) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color.Black),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.DarkMode,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = strings.amoledMode,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = strings.amoledModeDesc,
                  style = KoraTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Switch(
                checked = currentAmoled,
                onCheckedChange = { checked ->
                  onThemeChanged(currentPreset, checked, currentAccent)
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = MaterialTheme.colorScheme.primary
                )
              )
            }
          }
        }
      }

      // Language Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Translate,
              contentDescription = null,
              tint = colors.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.language,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.45f))

          // English option
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageChanged(AppLanguage.EN) }
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = strings.english,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = if (currentLanguage == AppLanguage.EN) FontWeight.SemiBold else FontWeight.Normal
            )
            RadioButton(
              selected = currentLanguage == AppLanguage.EN,
              onClick = { onLanguageChanged(AppLanguage.EN) },
              modifier = Modifier.testTag("lang_en_radio")
            )
          }

          HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))

          // Arabic option
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onLanguageChanged(AppLanguage.AR) }
              .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = strings.arabic,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = if (currentLanguage == AppLanguage.AR) FontWeight.SemiBold else FontWeight.Normal
            )
            RadioButton(
              selected = currentLanguage == AppLanguage.AR,
              onClick = { onLanguageChanged(AppLanguage.AR) },
              modifier = Modifier.testTag("lang_ar_radio")
            )
          }
        }
      }

      // About Kora Card
      Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = colors.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = strings.aboutKoraTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = strings.aboutKoraDesc,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // Quick-Access Theme Switcher Bottom Sheet
    if (showThemeSheet) {
      ThemeSwitcherSheet(
        currentPreset = currentPreset,
        currentAmoled = currentAmoled,
        currentAccent = currentAccent,
        onThemeChanged = { preset, amoled, accent ->
          onThemeChanged(preset, amoled, accent)
          onThemeModeChanged(if (!preset.isDark) "light" else if (preset == AppThemePreset.SYSTEM) "system" else "dark")
        },
        onDismiss = { showThemeSheet = false }
      )
    }
  }
}
