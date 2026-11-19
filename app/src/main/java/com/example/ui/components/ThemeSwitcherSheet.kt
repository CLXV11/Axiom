package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.ui.theme.AccentColorChoice
import com.example.ui.theme.AppThemePreset
import com.example.ui.theme.KoraTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSwitcherSheet(
  currentPreset: AppThemePreset,
  currentAmoled: Boolean,
  currentAccent: AccentColorChoice,
  onThemeChanged: (AppThemePreset, Boolean, AccentColorChoice) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val strings = LocalAppStrings.current
  val currentLanguage = LocalAppLanguage.current
  val isArabic = currentLanguage == AppLanguage.AR
  val colors = MaterialTheme.colorScheme

  var selectedPreset by remember { mutableStateOf(currentPreset) }
  var isAmoled by remember { mutableStateOf(currentAmoled) }
  var selectedAccent by remember { mutableStateOf(currentAccent) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surface,
    contentColor = colors.onSurface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 10.dp)
        .navigationBarsPadding()
        .imePadding()
    ) {
      // Sheet Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
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
          Column {
            Text(
              text = strings.themeSwitcherTitle,
              style = KoraTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = colors.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = if (isArabic) selectedPreset.subtitleAr else selectedPreset.subtitleEn,
              style = KoraTheme.typography.labelSmall,
              color = colors.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = colors.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Live Theme Preview Card
      Text(
        text = strings.themePreview,
        style = KoraTheme.typography.labelMedium,
        color = colors.onSurfaceVariant,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))
      ThemePreviewCard(
        preset = selectedPreset,
        amoledPureBlack = isAmoled,
        accentChoice = selectedAccent
      )

      Spacer(modifier = Modifier.height(20.dp))

      // Section 1: Presets
      Text(
        text = strings.themePresets,
        style = KoraTheme.typography.titleSmall,
        color = colors.onSurface,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(10.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AppThemePreset.entries.forEach { preset ->
          val isSelected = selectedPreset == preset
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSelected) colors.primary.copy(alpha = 0.14f) else colors.surfaceVariant.copy(alpha = 0.65f)
            ),
            border = androidx.compose.foundation.BorderStroke(
              width = if (isSelected) 1.5.dp else 1.dp,
              color = if (isSelected) colors.primary else colors.outlineVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                selectedPreset = preset
                onThemeChanged(preset, isAmoled, selectedAccent)
              }
              .testTag("theme_preset_${preset.id}")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(preset.backgroundColor)
                  .border(1.5.dp, colors.outline, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Box(
                  modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(preset.primaryColor)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isArabic) preset.nameAr else preset.nameEn,
                  style = KoraTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) colors.primary else colors.onSurface
                )
                Text(
                  text = if (isArabic) preset.subtitleAr else preset.subtitleEn,
                  style = KoraTheme.typography.labelSmall,
                  color = colors.onSurfaceVariant
                )
              }

              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = colors.onPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section 2: Custom Accent Color Selector
      Text(
        text = strings.customAccentColor,
        style = KoraTheme.typography.titleSmall,
        color = colors.onSurface,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        items(AccentColorChoice.entries) { choice ->
          val isSelected = selectedAccent == choice
          val chipColor = if (choice == AccentColorChoice.DEFAULT) selectedPreset.primaryColor else choice.color
          val checkTint = if (chipColor.luminance() > 0.52f) Color(0xFF0B0F19) else Color.White

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
              .clickable {
                selectedAccent = choice
                onThemeChanged(selectedPreset, isAmoled, choice)
              }
              .padding(4.dp)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(chipColor)
                .border(
                  width = if (isSelected) 2.5.dp else 1.dp,
                  color = if (isSelected) colors.onSurface else colors.outline,
                  shape = CircleShape
                ),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Selected",
                  tint = checkTint,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (isArabic) choice.nameAr else choice.nameEn,
              style = KoraTheme.typography.labelSmall.copy(fontSize = 10.sp),
              color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section 3: AMOLED Pure Black Mode (Only relevant for dark themes)
      if (selectedPreset.isDark) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.65f)),
          border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.55f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(1.dp, colors.outline, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.amoledMode,
                style = KoraTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
              )
              Text(
                text = strings.amoledModeDesc,
                style = KoraTheme.typography.labelSmall,
                color = colors.onSurfaceVariant
              )
            }

            Switch(
              checked = isAmoled,
              onCheckedChange = { checked ->
                isAmoled = checked
                onThemeChanged(selectedPreset, checked, selectedAccent)
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onPrimary,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.onSurfaceVariant,
                uncheckedTrackColor = colors.background
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }

      // Close / Confirm Button
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.primary,
          contentColor = colors.onPrimary
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
      ) {
        Text(strings.confirm, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
