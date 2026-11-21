package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings

enum class NameFontOption(
  val id: String,
  val labelAr: String,
  val labelEn: String,
  val fontFamily: FontFamily
) {
  DEFAULT("default", "الافتراضي", "Classic", FontFamily.Default),
  CAIRO("cairo", "القاهرة", "Cairo", FontFamily(Font(R.font.cairo))),
  AMIRI("amiri", "الأميري", "Amiri", FontFamily(Font(R.font.amiri))),
  TAJAWAL("tajawal", "تجوّل", "Tajawal", FontFamily(Font(R.font.tajawal))),
  PLAYFAIR("playfair", "تحريري", "Playfair", FontFamily(Font(R.font.playfair_display))),
  PACIFICO("pacifico", "توقيع", "Pacifico", FontFamily(Font(R.font.pacifico))),
  OUTFIT("outfit", "هندسي", "Outfit", FontFamily(Font(R.font.outfit)));

  companion object {
    fun fromId(raw: String?): NameFontOption =
      values().firstOrNull { it.id == raw } ?: DEFAULT
  }
}

enum class NameThemeOption(
  val id: String,
  val labelAr: String,
  val labelEn: String,
  val gradientColors: List<Color>,
  val glowColor: Color?
) {
  DEFAULT(
    id = "default",
    labelAr = "كلاسيكي",
    labelEn = "Classic",
    gradientColors = emptyList(),
    glowColor = null
  ),
  ROYAL_GOLD(
    id = "royal_gold",
    labelAr = "ذهب ملكي",
    labelEn = "Royal Gold",
    gradientColors = listOf(Color(0xFFFFF3C4), Color(0xFFF6C453), Color(0xFFD97706)),
    glowColor = Color(0xFFF6C453)
  ),
  CYBER_NEON(
    id = "cyber_neon",
    labelAr = "نيون سيبراني",
    labelEn = "Cyber Neon",
    gradientColors = listOf(Color(0xFF67E8F9), Color(0xFF60A5FA), Color(0xFFC084FC)),
    glowColor = Color(0xFF38BDF8)
  ),
  VELVET_ROSE(
    id = "velvet_rose",
    labelAr = "وردي مخملي",
    labelEn = "Velvet Rose",
    gradientColors = listOf(Color(0xFFF9A8D4), Color(0xFFF43F5E), Color(0xFFFB7185)),
    glowColor = Color(0xFFF43F5E)
  ),
  EMERALD_AURORA(
    id = "emerald_aurora",
    labelAr = "شفق زمردي",
    labelEn = "Emerald Aurora",
    gradientColors = listOf(Color(0xFF6EE7B7), Color(0xFF10B981), Color(0xFF2DD4BF)),
    glowColor = Color(0xFF10B981)
  ),
  CRIMSON_EMBER(
    id = "crimson_ember",
    labelAr = "جمر قرمزي",
    labelEn = "Crimson Ember",
    gradientColors = listOf(Color(0xFFFCA5A5), Color(0xFFEF4444), Color(0xFFF59E0B)),
    glowColor = Color(0xFFEF4444)
  ),
  ARCTIC_ICE(
    id = "arctic_ice",
    labelAr = "جليد قطبي",
    labelEn = "Arctic Ice",
    gradientColors = listOf(Color(0xFFF8FAFC), Color(0xFF93C5FD), Color(0xFF818CF8)),
    glowColor = Color(0xFF93C5FD)
  );

  companion object {
    fun fromId(raw: String?): NameThemeOption =
      values().firstOrNull { it.id == raw } ?: DEFAULT
  }
}

/**
 * Renders ONLY a user's display name with their chosen bundled font & color/glow theme.
 * App typography elsewhere stays untouched.
 */
@Composable
fun StyledDisplayName(
  name: String,
  nameFontId: String? = "default",
  nameThemeId: String? = "default",
  style: TextStyle = MaterialTheme.typography.titleMedium,
  fallbackColor: Color = MaterialTheme.colorScheme.onSurface,
  maxLines: Int = 1,
  overflow: TextOverflow = TextOverflow.Ellipsis,
  textAlign: TextAlign? = null,
  modifier: Modifier = Modifier
) {
  val fontOption = remember(nameFontId) { NameFontOption.fromId(nameFontId) }
  val themeOption = remember(nameThemeId) { NameThemeOption.fromId(nameThemeId) }

  val styled = remember(style, fontOption, themeOption, fallbackColor) {
    val brush = if (themeOption.gradientColors.size >= 2) {
      Brush.linearGradient(colors = themeOption.gradientColors)
    } else null
    val shadow = themeOption.glowColor?.let { glow ->
      Shadow(
        color = glow.copy(alpha = 0.42f),
        offset = Offset(0f, 1.5f),
        blurRadius = 10f
      )
    }
    if (brush != null) {
      style.copy(
        fontFamily = fontOption.fontFamily,
        brush = brush,
        shadow = shadow
      )
    } else {
      style.copy(
        fontFamily = fontOption.fontFamily,
        color = fallbackColor
      )
    }
  }

  Text(
    text = name,
    style = styled,
    maxLines = maxLines,
    overflow = overflow,
    textAlign = textAlign,
    modifier = modifier
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameStyleCustomizerSheet(
  displayName: String,
  handle: String,
  initialFontId: String,
  initialThemeId: String,
  onSave: (fontId: String, themeId: String) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val isArabic = LocalAppLanguage.current == AppLanguage.AR
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var selectedFontId by remember(initialFontId) { mutableStateOf(NameFontOption.fromId(initialFontId).id) }
  var selectedThemeId by remember(initialThemeId) { mutableStateOf(NameThemeOption.fromId(initialThemeId).id) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surface,
    contentColor = colors.onSurface,
    modifier = Modifier.testTag("name_style_customizer_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 620.dp)
        .align(Alignment.CenterHorizontally)
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = strings.nameStyleTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.onSurface
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = strings.cancel,
            tint = colors.onSurfaceVariant
          )
        }
      }

      // Live Display Name Preview Banner
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.65f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = strings.nameStylePreviewLabel,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant
          )
          StyledDisplayName(
            name = displayName.ifBlank { strings.appName },
            nameFontId = selectedFontId,
            nameThemeId = selectedThemeId,
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 26.sp
            ),
            textAlign = TextAlign.Center
          )
          Text(
            text = handle,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
          )
        }
      }

      // Font Selector (applies ONLY to display name)
      Text(
        text = strings.nameFontSection,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        NameFontOption.values().forEach { fontOpt ->
          val active = selectedFontId == fontOpt.id
          Surface(
            onClick = { selectedFontId = fontOpt.id },
            shape = RoundedCornerShape(12.dp),
            color = if (active) colors.primary.copy(alpha = 0.16f) else colors.background,
            border = BorderStroke(
              width = if (active) 1.5.dp else 1.dp,
              color = if (active) colors.primary else colors.outlineVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier
              .defaultMinSize(minHeight = 52.dp)
              .testTag("name_font_chip_${fontOpt.id}")
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = if (isArabic) "أبجد Kora" else "Kora Abc",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontFamily = fontOpt.fontFamily,
                  fontWeight = FontWeight.Bold
                ),
                color = if (active) colors.primary else colors.onSurface
              )
              Text(
                text = if (isArabic) fontOpt.labelAr else fontOpt.labelEn,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) colors.primary else colors.onSurfaceVariant
              )
            }
          }
        }
      }

      HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))

      // Color & Glow Themes Selector
      Text(
        text = strings.nameThemeSection,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = colors.onSurface
      )
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NameThemeOption.values().toList().chunked(2).forEach { rowItems ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            rowItems.forEach { themeOpt ->
              val active = selectedThemeId == themeOpt.id
              Surface(
                onClick = { selectedThemeId = themeOpt.id },
                shape = RoundedCornerShape(12.dp),
                color = if (active) colors.primary.copy(alpha = 0.14f) else colors.background,
                border = BorderStroke(
                  width = if (active) 1.5.dp else 1.dp,
                  color = if (active) colors.primary else colors.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                  .weight(1f)
                  .defaultMinSize(minHeight = 48.dp)
                  .testTag("name_theme_card_${themeOpt.id}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(20.dp)
                      .clip(CircleShape)
                      .background(
                        if (themeOpt.gradientColors.size >= 2) {
                          Brush.linearGradient(themeOpt.gradientColors)
                        } else {
                          Brush.linearGradient(listOf(colors.onSurface, colors.onSurfaceVariant))
                        }
                      )
                      .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                  )
                  Text(
                    text = if (isArabic) themeOpt.labelAr else themeOpt.labelEn,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    color = if (active) colors.primary else colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                  )
                  if (active) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = colors.primary,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
            if (rowItems.size == 1) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Button(
        onClick = { onSave(selectedFontId, selectedThemeId) },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = colors.primary,
          contentColor = colors.onPrimary
        ),
        contentPadding = PaddingValues(vertical = 13.dp),
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 48.dp)
          .testTag("save_name_style_button")
      ) {
        Text(
          text = strings.confirm,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
    }
  }
}
