package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KoraTheme
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import kotlin.math.cos
import kotlin.math.sin

/**
 * Luxurious Verification Tiers with rich multi-stop gradients, metallic highlights, and distinction.
 */
enum class VerificationTier(
  val id: String,
  val titleEn: String,
  val titleAr: String,
  val subtitleEn: String,
  val subtitleAr: String,
  val descriptionEn: String,
  val descriptionAr: String,
  val gradientColors: List<Color>,
  val accentColor: Color,
  val highlightColor: Color,
  val checkmarkColor: Color = Color.White
) {
  BLUE(
    id = "BLUE",
    titleEn = "Sapphire Blue",
    titleAr = "الياقوت الأزرق الملكي",
    subtitleEn = "Official Creator & Public Figure",
    subtitleAr = "توثيق رسمي للمبدعين والشخصيات المعتمدة",
    descriptionEn = "Authenticates notable creators, thought leaders, and recognized public figures across Kora with pristine azure brilliance.",
    descriptionAr = "تمنح للمبدعين وقادة الفكر والشخصيات المعتمدة وفق معايير التوثيق القياسية الرفيعة لمنصة كورا.",
    gradientColors = listOf(Color(0xFF38BDF8), Color(0xFF1D9BF0), Color(0xFF0284C7), Color(0xFF0369A1)),
    accentColor = Color(0xFF1D9BF0),
    highlightColor = Color(0xFFBAE6FD)
  ),
  GOLD(
    id = "GOLD",
    titleEn = "Imperial Sovereign Gold 24K",
    titleAr = "الذهب الإمبراطوري 24K",
    subtitleEn = "Executive, Founders & Enterprise",
    subtitleAr = "توثيق القيادات العليا والمؤسسات والشركاء",
    descriptionEn = "The highest echelon of prestige, reserved for platform architects, founding executives, and premier corporate entities with molten gold brilliance.",
    descriptionAr = "أعلى درجات الفخامة والمكانة، مخصصة للمؤسسين والقيادات التنفيذية والشركاء الرسميين والمؤسسات الكبرى ببريق الذهب الخالص.",
    gradientColors = listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF92400E)),
    accentColor = Color(0xFFF59E0B),
    highlightColor = Color(0xFFFEF3C7)
  ),
  ONYX(
    id = "ONYX",
    titleEn = "Obsidian Stealth Pro",
    titleAr = "العقيق الأسود الفاخر (أونيكس)",
    subtitleEn = "Minimalist Elite & Stealth Luxury",
    subtitleAr = "شارة النخبة المينيمالية شديدة الفخامة",
    descriptionEn = "A dark titanium and obsidian seal for elite technologists, system architects, and minimalist design purists.",
    descriptionAr = "ختم تيتانيوم مصقول بعقيق أسود داكن مصمم خصيصاً للنخبة التقنية ومهندسي الأنظمة وعشاق الفخامة الهادئة.",
    gradientColors = listOf(Color(0xFF4B5563), Color(0xFF1F2937), Color(0xFF111827), Color(0xFF030712)),
    accentColor = Color(0xFF374151),
    highlightColor = Color(0xFFE5E7EB),
    checkmarkColor = Color(0xFFF9FAFB)
  ),
  EMERALD(
    id = "EMERALD",
    titleEn = "Verdant Imperial Jade",
    titleAr = "الزمرد الإمبراطوري (اليشم)",
    subtitleEn = "Ecosystem Pioneer & Sustainability Leader",
    subtitleAr = "شارة الرواد وبناة المجتمعات الحية",
    descriptionEn = "Radiant emerald and imperial jade tones signifying vibrant community leadership, enduring trust, and sustained impact.",
    descriptionAr = "تدرجات الزمرد واليشم الملكي المتوهج تعكس الثقة العالية والأثر المجتمعي المستدام والريادة الملهمة.",
    gradientColors = listOf(Color(0xFF6EE7B7), Color(0xFF10B981), Color(0xFF059669), Color(0xFF064E3B)),
    accentColor = Color(0xFF10B981),
    highlightColor = Color(0xFFA7F3D0)
  ),
  AMETHYST(
    id = "AMETHYST",
    titleEn = "Royal Velvet Amethyst",
    titleAr = "الجمشت البنفسجي الملكي",
    subtitleEn = "Artisan & Creative Director",
    subtitleAr = "شارة المصممين النخبة والمديرين الإبداعيين",
    descriptionEn = "Rich imperial purple celebrating bespoke aesthetics, digital craftsmanship, and extraordinary artistic mastery.",
    descriptionAr = "لون أرجواني ملكي فاخر يجسد الحرفية الرقمية الرفيعة والتميز الإبداعي لخبراء التصميم والابتكار البصري.",
    gradientColors = listOf(Color(0xFFD8B4FE), Color(0xFFA855F7), Color(0xFF7E22CE), Color(0xFF4A044E)),
    accentColor = Color(0xFFA855F7),
    highlightColor = Color(0xFFF3E8FF)
  ),
  ROSE_GOLD(
    id = "ROSE_GOLD",
    titleEn = "Champagne Rose Gold",
    titleAr = "الذهب الوردي والشمبانيا",
    subtitleEn = "High-End Patron & Cultural Curator",
    subtitleAr = "شارة الرعاة النخبة والأناقة الاستثنائية",
    descriptionEn = "Lustrous blush gold crafted for discerning collectors, refined stylists, and sophisticated tastemakers.",
    descriptionAr = "بريق الذهب الوردي الفاتن للمهتمين بالجماليات الراقية والرعاة المتميزين لمنصة كورا.",
    gradientColors = listOf(Color(0xFFFFCCD5), Color(0xFFFB7185), Color(0xFFE11D48), Color(0xFF881337)),
    accentColor = Color(0xFFFB7185),
    highlightColor = Color(0xFFFFF1F2)
  ),
  PLATINUM(
    id = "PLATINUM",
    titleEn = "Celestial Titanium Platinum",
    titleAr = "البلاتين السماوي والتيتانيوم",
    subtitleEn = "Core Infrastructure & Trusted Systems",
    subtitleAr = "شارة البنية التحتية والأنظمة المعتمدة",
    descriptionEn = "Pristine mirror-finish platinum verifying certified secure systems, security validators, and core protocols.",
    descriptionAr = "بريق بلاتيني فائق النقاء يؤكد موثوقية الأنظمة المشفرة ومحققي الأمان والبروتوكولات الرسمية.",
    gradientColors = listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569)),
    accentColor = Color(0xFF94A3B8),
    highlightColor = Color(0xFFFFFFFF),
    checkmarkColor = Color(0xFF0F172A)
  );

  companion object {
    fun fromId(id: String?): VerificationTier {
      return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: BLUE
    }
  }
}

val VerifiedBadgeBlue = Color(0xFF1D9BF0)
val VerifiedBadgeDarkBlue = Color(0xFF1A8CD8)

/**
 * High-End Verification Badge Composable.
 * Renders an authentic scalloped starburst rosette with multi-stop specular gradients,
 * metallic bevel highlights, and a crisp vector checkmark.
 */
@Composable
fun VerifiedBadge(
  modifier: Modifier = Modifier,
  size: Dp = 16.dp,
  showDialogOnClick: Boolean = false,
  tier: VerificationTier = VerificationTier.BLUE,
  tierKey: String? = null,
  customBadgeColor: Color? = null
) {
  val resolvedTier = if (tierKey != null) VerificationTier.fromId(tierKey) else tier
  val strings = LocalAppStrings.current
  val currentLang = LocalAppLanguage.current
  val isArabic = currentLang == AppLanguage.AR

  var showDetailsDialog by remember { mutableStateOf(false) }
  val interactionSource = remember { MutableInteractionSource() }

  val title = if (isArabic) resolvedTier.titleAr else resolvedTier.titleEn

  Box(
    modifier = modifier
      .size(size)
      .semantics {
        contentDescription = "$title - ${strings.verifiedAccount}"
      }
      .testTag("verified_badge")
      .then(
        if (showDialogOnClick) {
          Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = { showDetailsDialog = true }
          )
        } else {
          Modifier
        }
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      drawScallopedRosetteBadge(
        size = this.size,
        tier = resolvedTier,
        customColor = customBadgeColor
      )
    }
  }

  if (showDetailsDialog) {
    VerifiedBadgeInfoDialog(
      tier = resolvedTier,
      onDismiss = { showDetailsDialog = false }
    )
  }
}

/**
 * Draws the authentic 8-point scalloped rosette seal with rich gradient, inner specular ring,
 * and high-precision vector checkmark.
 */
private fun DrawScope.drawScallopedRosetteBadge(
  size: Size,
  tier: VerificationTier,
  customColor: Color?
) {
  val w = size.width
  val h = size.height
  val center = Offset(w / 2f, h / 2f)
  val r = w / 2f

  // 1. Compute 8-lobed scalloped rosette path
  val path = Path()
  val count = 8
  val step = (Math.PI * 2 / count).toFloat()
  val outerRadius = r
  val innerRadius = r * 0.82f

  for (i in 0 until count) {
    val angle = i * step
    val midAngle = angle + step / 2f
    val nextAngle = (i + 1) * step

    val p1x = center.x + outerRadius * cos(angle)
    val p1y = center.y + outerRadius * sin(angle)
    val midX = center.x + innerRadius * cos(midAngle)
    val midY = center.y + innerRadius * sin(midAngle)
    val p2x = center.x + outerRadius * cos(nextAngle)
    val p2y = center.y + outerRadius * sin(nextAngle)

    if (i == 0) {
      path.moveTo(p1x, p1y)
    }

    path.quadraticTo(
      center.x + (outerRadius * 0.98f) * cos(angle + step * 0.25f),
      center.y + (outerRadius * 0.98f) * sin(angle + step * 0.25f),
      midX,
      midY
    )
    path.quadraticTo(
      center.x + (outerRadius * 0.98f) * cos(midAngle + step * 0.25f),
      center.y + (outerRadius * 0.98f) * sin(midAngle + step * 0.25f),
      p2x,
      p2y
    )
  }
  path.close()

  // 2. Base Rosette Gradient Fill
  val gradientBrush = if (customColor != null) {
    Brush.linearGradient(
      listOf(customColor, customColor.copy(alpha = 0.8f)),
      start = Offset(0f, 0f),
      end = Offset(w, h)
    )
  } else {
    Brush.linearGradient(
      colors = tier.gradientColors,
      start = Offset(0f, 0f),
      end = Offset(w, h)
    )
  }
  drawPath(path = path, brush = gradientBrush)

  // 3. Inner Specular Bevel Ring
  val innerBevelRadius = r * 0.72f
  val bevelBrush = Brush.verticalGradient(
    colors = listOf(
      tier.highlightColor.copy(alpha = 0.55f),
      Color.Transparent,
      Color.Black.copy(alpha = 0.25f)
    )
  )
  drawCircle(
    brush = bevelBrush,
    radius = innerBevelRadius,
    center = center,
    style = Stroke(width = (w * 0.05f).coerceAtLeast(0.8f))
  )

  // 4. Vector Checkmark
  val checkPath = Path().apply {
    moveTo(w * 0.29f, h * 0.52f)
    lineTo(w * 0.44f, h * 0.67f)
    lineTo(w * 0.73f, h * 0.35f)
  }
  val strokeWidth = (w * 0.13f).coerceAtLeast(1.5f)
  drawPath(
    path = checkPath,
    color = tier.checkmarkColor,
    style = Stroke(
      width = strokeWidth,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )
}

/**
 * Inspection modal displaying authentic verification tier details, official certificate seal,
 * and high-resolution gradient showcase.
 */
@Composable
fun VerifiedBadgeInfoDialog(
  tier: VerificationTier = VerificationTier.BLUE,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  val currentLang = LocalAppLanguage.current
  val isArabic = currentLang == AppLanguage.AR

  val title = if (isArabic) tier.titleAr else tier.titleEn
  val subtitle = if (isArabic) tier.subtitleAr else tier.subtitleEn
  val description = if (isArabic) tier.descriptionAr else tier.descriptionEn

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.ok, fontWeight = FontWeight.Bold, color = tier.accentColor)
      }
    },
    icon = {
      Box(
        modifier = Modifier
          .size(54.dp)
          .shadow(6.dp, CircleShape, spotColor = tier.accentColor)
          .clip(CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(54.dp)) {
          drawScallopedRosetteBadge(
            size = this.size,
            tier = tier,
            customColor = null
          )
        }
      }
    },
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelMedium,
          color = tier.accentColor,
          fontWeight = FontWeight.SemiBold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Gradient & Metallic Specular Swatch
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = tier.accentColor,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isArabic) "المواصفات الفنية للدرجة" else "Tier Specifications",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
              }

              // Color gradient pill preview
              Box(
                modifier = Modifier
                  .size(width = 44.dp, height = 12.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Brush.linearGradient(tier.gradientColors))
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (isArabic)
                "شارة موثوقة بتقنية التدرج متعدد المستويات، مقاومة للتزييف ومسجلة رسميًا على بنية كورا السحابية الموثوقة."
              else
                "Cryptographically certified status backed by Kora multi-device secure ledger with anti-spoofing distinction.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  )
}
