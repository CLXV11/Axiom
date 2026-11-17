package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.UserProfile
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.StatusOnline
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings
import kotlin.math.cos
import kotlin.math.sin

val ProBadgeTeal = Color(0xFF0D9488)
val ProBadgeCyan = Color(0xFF06B6D4)
val ProBadgeIndigo = Color(0xFF6366F1)
val ProBadgeAmber = Color(0xFFF59E0B)

/**
 * Verified Professional Badge Icon Composable for contact list items.
 * Renders an authenticated professional seal with metallic gradient, certified shield,
 * and an interactive credential inspection modal.
 */
@Composable
fun VerifiedProfessionalBadge(
  user: UserProfile,
  modifier: Modifier = Modifier,
  size: Dp = 16.dp,
  showDialogOnClick: Boolean = true,
  onMessageClick: (() -> Unit)? = null
) {
  val strings = LocalAppStrings.current
  val currentLang = LocalAppLanguage.current
  val isArabic = currentLang == AppLanguage.AR
  var showDialog by remember { mutableStateOf(false) }

  val proDesc = "${strings.verifiedProTitle}: ${user.profession}"

  Box(
    modifier = modifier
      .size(size)
      .semantics { contentDescription = proDesc }
      .testTag("verified_pro_badge_${user.id}")
      .then(
        if (showDialogOnClick) {
          Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = { showDialog = true }
          )
        } else {
          Modifier
        }
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      drawProfessionalSealBadge(size = this.size)
    }
  }

  if (showDialog) {
    VerifiedProfessionalDialog(
      user = user,
      onDismiss = { showDialog = false },
      onMessageClick = {
        showDialog = false
        onMessageClick?.invoke()
      }
    )
  }
}

/**
 * Distinctive authenticated professional pill badge combining the certified seal,
 * profession title, and networking beacon.
 */
@Composable
fun VerifiedProfessionalPill(
  user: UserProfile,
  modifier: Modifier = Modifier,
  showDialogOnClick: Boolean = true,
  onMessageClick: (() -> Unit)? = null
) {
  val strings = LocalAppStrings.current
  var showDialog by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(
        Brush.linearGradient(
          listOf(
            ProBadgeCyan.copy(alpha = 0.15f),
            DiscordBlurple.copy(alpha = 0.15f)
          )
        )
      )
      .border(
        width = 1.dp,
        brush = Brush.linearGradient(
          listOf(
            ProBadgeCyan.copy(alpha = 0.45f),
            DiscordBlurple.copy(alpha = 0.35f)
          )
        ),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable(enabled = showDialogOnClick) { showDialog = true }
      .padding(horizontal = 7.dp, vertical = 2.5.dp)
      .testTag("verified_pro_pill_${user.id}")
  ) {
    // 1. Certified Shield Rosette
    VerifiedProfessionalBadge(
      user = user,
      size = 13.dp,
      showDialogOnClick = false
    )

    Spacer(modifier = Modifier.width(5.dp))

    // 2. Profession Title
    Text(
      text = user.profession,
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF38BDF8),
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp
    )

    // 3. Authenticated "PRO" badge
    Spacer(modifier = Modifier.width(5.dp))
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(4.dp))
        .background(
          Brush.linearGradient(
            listOf(Color(0xFF0284C7), DiscordBlurple)
          )
        )
        .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
      Text(
        text = strings.verifiedProBadge,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 8.5.sp,
        letterSpacing = 0.5.sp
      )
    }

    // 4. Open to networking active green indicator
    if (user.openToNetworking) {
      Spacer(modifier = Modifier.width(5.dp))
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(StatusOnline)
      )
    }
  }

  if (showDialog) {
    VerifiedProfessionalDialog(
      user = user,
      onDismiss = { showDialog = false },
      onMessageClick = {
        showDialog = false
        onMessageClick?.invoke()
      }
    )
  }
}

/**
 * Draws a multi-lobed hexagonal certified professional seal with a subtle specular bevel
 * and inner briefcase/shield checkmark icon.
 */
private fun DrawScope.drawProfessionalSealBadge(size: Size) {
  val w = size.width
  val h = size.height
  val center = Offset(w / 2f, h / 2f)
  val r = w / 2f

  // 1. Compute 6-point certified starburst seal
  val path = Path()
  val count = 6
  val step = (Math.PI * 2 / count).toFloat()
  val outerRadius = r
  val innerRadius = r * 0.82f

  for (i in 0 until count) {
    val angle = i * step - (Math.PI / 2).toFloat()
    val midAngle = angle + step / 2f
    val nextAngle = (i + 1) * step - (Math.PI / 2).toFloat()

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
      center.x + (outerRadius * 0.95f) * cos(angle + step * 0.25f),
      center.y + (outerRadius * 0.95f) * sin(angle + step * 0.25f),
      midX,
      midY
    )
    path.quadraticTo(
      center.x + (outerRadius * 0.95f) * cos(midAngle + step * 0.25f),
      center.y + (outerRadius * 0.95f) * sin(midAngle + step * 0.25f),
      p2x,
      p2y
    )
  }
  path.close()

  // 2. Cyan to Deep Indigo Specular Gradient
  val gradientBrush = Brush.linearGradient(
    colors = listOf(
      Color(0xFF38BDF8),
      Color(0xFF0284C7),
      Color(0xFF4F46E5),
      Color(0xFF312E81)
    ),
    start = Offset(0f, 0f),
    end = Offset(w, h)
  )
  drawPath(path = path, brush = gradientBrush)

  // 3. Inner Metallic Specular Ring
  val innerBevelRadius = r * 0.70f
  val bevelBrush = Brush.verticalGradient(
    colors = listOf(
      Color(0xFFBAE6FD).copy(alpha = 0.8f),
      Color.Transparent,
      Color.Black.copy(alpha = 0.3f)
    )
  )
  drawCircle(
    brush = bevelBrush,
    radius = innerBevelRadius,
    center = center,
    style = Stroke(width = (w * 0.08f).coerceAtLeast(1f))
  )

  // 4. Centered Sharp Vector Checkmark
  val checkPath = Path().apply {
    moveTo(w * 0.30f, h * 0.50f)
    lineTo(w * 0.44f, h * 0.65f)
    lineTo(w * 0.72f, h * 0.35f)
  }
  val strokeWidth = (w * 0.14f).coerceAtLeast(1.5f)
  drawPath(
    path = checkPath,
    color = Color.White,
    style = Stroke(
      width = strokeWidth,
      cap = StrokeCap.Round,
      join = StrokeJoin.Round
    )
  )
}

/**
 * Inspection modal detailing authenticated professional verification status,
 * authenticating organization, domain competence, and networking availability.
 */
@Composable
fun VerifiedProfessionalDialog(
  user: UserProfile,
  onDismiss: () -> Unit,
  onMessageClick: (() -> Unit)? = null
) {
  val strings = LocalAppStrings.current
  val isArabic = LocalAppLanguage.current == AppLanguage.AR

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      if (onMessageClick != null) {
        Button(
          onClick = onMessageClick,
          colors = ButtonDefaults.buttonColors(containerColor = DiscordBlurple)
        ) {
          Icon(Icons.Default.Handshake, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isArabic) "بدء تواصل مهني" else "Connect & Message", fontWeight = FontWeight.Bold)
        }
      } else {
        TextButton(onClick = onDismiss) {
          Text(strings.ok, fontWeight = FontWeight.Bold, color = DiscordBlurple)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    },
    icon = {
      Box(
        modifier = Modifier
          .size(56.dp)
          .shadow(8.dp, CircleShape, spotColor = ProBadgeCyan)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(Color(0xFF0284C7), DiscordBlurple)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.WorkspacePremium,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(32.dp)
        )
      }
    },
    title = {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = strings.verifiedProTitle,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = user.displayName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFF38BDF8)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Professional Role Card
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = user.profession.ifEmpty { "Verified Specialist" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            if (user.professionalOrg.isNotBlank()) {
              Spacer(modifier = Modifier.height(6.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = user.professionalOrg,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }

        // Open to Networking Status
        if (user.openToNetworking) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(StatusOnline.copy(alpha = 0.12f))
              .padding(horizontal = 10.dp, vertical = 7.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(StatusOnline)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = strings.openToNetworking,
              style = MaterialTheme.typography.labelMedium,
              color = StatusOnline,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // Description
        Text(
          text = strings.verifiedProDialogDesc,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 18.sp
        )
      }
    }
  )
}
