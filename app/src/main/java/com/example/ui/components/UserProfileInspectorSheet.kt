package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.UserRepository
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordBorder
import com.example.ui.theme.DiscordDarkDeepest
import com.example.ui.theme.DiscordDarkElevated
import com.example.ui.theme.DiscordDarkPrimary
import com.example.ui.theme.DiscordDarkSecondary
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.theme.DiscordTextNormal
import com.example.ui.theme.DiscordTextPrimary
import com.example.ui.theme.StatusOnline
import com.example.ui.theme.StatusQuiet
import com.example.ui.util.LocalAppStrings
import com.example.ui.util.formatHandleForDisplay
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileInspectorSheet(
  seedUser: UserProfile,
  userRepository: UserRepository,
  currentUid: String,
  postsCount: Int = 0,
  fluxCount: Int = 0,
  onDismiss: () -> Unit,
  onMessageClick: (UserProfile) -> Unit,
  onVoiceCallClick: (UserProfile) -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val observedUser by userRepository.observeUserProfile(seedUser.id).collectAsState(initial = seedUser)
  LaunchedEffect(seedUser.id) {
    runCatching { userRepository.getUserProfile(seedUser.id) }
  }

  val user = observedUser ?: seedUser
  val isSelf = user.id == currentUid
  val formattedHandle = remember(user.handle) { formatHandleForDisplay(user.handle) }
  val presenceLabel = remember(user.quietMode, user.lastSeen, strings) {
    formatPresenceLabel(
      isQuiet = user.quietMode,
      lastSeen = user.lastSeen,
      onlineLabel = strings.online,
      quietLabel = strings.quietMode,
      recentlyLabel = strings.inspectProfileLastSeenRecently,
      lastSeenAtFormat = strings.inspectProfileLastSeenAt
    )
  }

  val tierAccent = remember(user.verifiedTier) {
    VerificationTier.fromId(user.verifiedTier).accentColor
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = DiscordDarkPrimary,
    contentColor = DiscordTextPrimary,
    dragHandle = null,
    modifier = Modifier.testTag("user_profile_inspector_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 620.dp)
        .align(Alignment.CenterHorizontally)
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
    ) {
      // Discord-inspired Hero Cover Banner (or equipped Achievement Wall) + Overlapping Avatar
      Box(modifier = Modifier.fillMaxWidth()) {
        if (!user.equippedWall.isNullOrBlank()) {
          AchievementWallView(
            styleKey = user.equippedWall,
            scrimAlpha = 0.24f,
            modifier = Modifier
              .fillMaxWidth()
              .height(114.dp)
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(108.dp)
              .background(
                Brush.linearGradient(
                  listOf(
                    colors.primary.copy(alpha = 0.85f),
                    tierAccent.copy(alpha = 0.72f),
                    DiscordDarkDeepest
                  )
                )
              )
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(999.dp),
            color = DiscordDarkDeepest.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
          ) {
            Text(
              text = strings.inspectProfileMemberTitle,
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = DiscordTextPrimary,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(DiscordDarkDeepest.copy(alpha = 0.72f))
              .testTag("close_profile_inspector_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = strings.cancel,
              tint = DiscordTextPrimary
            )
          }
        }

        // Overlapping Profile Avatar with Frame & Live Presence
        Box(
          modifier = Modifier
            .padding(start = 20.dp, top = 62.dp)
            .clip(CircleShape)
            .background(DiscordDarkPrimary)
            .padding(4.dp)
        ) {
          AvatarView(
            name = user.displayName.ifBlank { "Member" },
            avatarUrl = user.avatarUrl,
            size = 86.dp,
            frameStyleKey = user.equippedFrame,
            showPresence = true,
            isOnline = !user.quietMode,
            isQuiet = user.quietMode
          )
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Primary Identity Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = DiscordDarkSecondary,
          border = BorderStroke(1.dp, DiscordBorder.copy(alpha = 0.75f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = user.displayName.ifBlank { "Kora Member" },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DiscordTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              if (user.isVerified) {
                Spacer(modifier = Modifier.width(8.dp))
                VerifiedBadge(
                  size = 20.dp,
                  tierKey = user.verifiedTier,
                  showDialogOnClick = true
                )
              }
              if (user.isProfessionalVerified) {
                Spacer(modifier = Modifier.width(6.dp))
                VerifiedProfessionalBadge(
                  user = user,
                  size = 19.dp,
                  showDialogOnClick = true,
                  onMessageClick = if (isSelf) null else {
                    { onMessageClick(user) }
                  }
                )
              }
            }

            // Copyable Handle Row
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                onClick = {
                  val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  cm.setPrimaryClip(ClipData.newPlainText("Kora Handle", formattedHandle))
                  Toast.makeText(context, strings.inspectProfileHandleCopied, Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(8.dp),
                color = DiscordDarkDeepest,
                border = BorderStroke(1.dp, DiscordBorder.copy(alpha = 0.6f))
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Text(
                    text = formattedHandle,
                    style = MaterialTheme.typography.labelLarge,
                    color = DiscordTextNormal,
                    fontWeight = FontWeight.Medium
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = strings.copy,
                    tint = DiscordTextMuted,
                    modifier = Modifier.size(13.dp)
                  )
                }
              }

              // Presence Pill
              Surface(
                shape = RoundedCornerShape(999.dp),
                color = (if (user.quietMode) StatusQuiet else StatusOnline).copy(alpha = 0.14f),
                border = BorderStroke(
                  1.dp,
                  (if (user.quietMode) StatusQuiet else StatusOnline).copy(alpha = 0.45f)
                )
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(7.dp)
                      .clip(CircleShape)
                      .background(if (user.quietMode) StatusQuiet else StatusOnline)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = presenceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (user.quietMode) StatusQuiet else StatusOnline,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }

            // Equipped Role Nameplate or Profession Pill
            if (!user.equippedNameplate.isNullOrBlank()) {
              RoleNameplateView(
                styleKey = user.equippedNameplate,
                title = user.profession.ifBlank { user.displayName },
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            if (user.isProfessionalVerified) {
              VerifiedProfessionalPill(
                user = user,
                showDialogOnClick = true,
                onMessageClick = if (isSelf) null else {
                  { onMessageClick(user) }
                }
              )
            } else if (user.profession.isNotBlank() && user.equippedNameplate.isNullOrBlank()) {
              Text(
                text = if (user.professionalOrg.isNotBlank()) {
                  "${user.profession} • ${user.professionalOrg}"
                } else {
                  user.profession
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
                fontWeight = FontWeight.SemiBold
              )
            }

            if (user.openToNetworking) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(StatusOnline.copy(alpha = 0.11f))
                  .padding(horizontal = 9.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Handshake,
                  contentDescription = null,
                  tint = StatusOnline,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = strings.openToNetworking,
                  style = MaterialTheme.typography.labelSmall,
                  color = StatusOnline,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        // About Me Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = DiscordDarkSecondary,
          border = BorderStroke(1.dp, DiscordBorder.copy(alpha = 0.75f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = strings.settingsAboutMeLabel,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = DiscordTextMuted,
              letterSpacing = 0.8.sp
            )
            Text(
              text = user.bio.ifBlank { strings.inspectProfileBioEmpty },
              style = MaterialTheme.typography.bodyMedium,
              color = if (user.bio.isNotBlank()) DiscordTextNormal else DiscordTextMuted,
              lineHeight = 20.sp
            )
          }
        }

        // Community Activity Stats Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = DiscordDarkSecondary,
          border = BorderStroke(1.dp, DiscordBorder.copy(alpha = 0.75f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Text(
              text = strings.inspectProfileStatsTitle,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = DiscordTextMuted,
              letterSpacing = 0.8.sp
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              InspectorStatTile(
                icon = Icons.Outlined.PostAdd,
                value = postsCount.toString(),
                label = strings.inspectProfilePostsCount,
                accentColor = DiscordBlurple,
                modifier = Modifier.weight(1f)
              )
              InspectorStatTile(
                icon = Icons.Outlined.Movie,
                value = fluxCount.toString(),
                label = strings.inspectProfileFluxCount,
                accentColor = Color(0xFF00A8FC),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }

        // Action CTAs or Self-Profile Notice
        if (isSelf) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = DiscordDarkElevated,
            border = BorderStroke(1.dp, DiscordBorder.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = strings.inspectProfileSelfBadge,
              style = MaterialTheme.typography.bodySmall,
              color = DiscordTextMuted,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            )
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onMessageClick(user) },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
              ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
              modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .testTag("inspector_message_button")
            ) {
              Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = strings.inspectProfileDirectMessage,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
              )
            }

            OutlinedButton(
              onClick = { onVoiceCallClick(user) },
              shape = RoundedCornerShape(14.dp),
              border = BorderStroke(1.dp, StatusOnline.copy(alpha = 0.7f)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = StatusOnline.copy(alpha = 0.14f),
                contentColor = StatusOnline
              ),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
              modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 48.dp)
                .testTag("inspector_call_button")
            ) {
              Icon(
                imageVector = Icons.Default.Call,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = strings.inspectProfileCall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
      }
    }
  }
}

@Composable
private fun InspectorStatTile(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  value: String,
  label: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = DiscordDarkDeepest,
    border = BorderStroke(1.dp, DiscordBorder.copy(alpha = 0.55f)),
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(accentColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(17.dp)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = value,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = DiscordTextPrimary
        )
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = DiscordTextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

private fun formatPresenceLabel(
  isQuiet: Boolean,
  lastSeen: Timestamp?,
  onlineLabel: String,
  quietLabel: String,
  recentlyLabel: String,
  lastSeenAtFormat: String
): String {
  if (isQuiet) return quietLabel
  val date = lastSeen?.toDate() ?: return onlineLabel
  val diffMs = System.currentTimeMillis() - date.time
  return when {
    diffMs < 5 * 60_000L -> onlineLabel
    diffMs < 60 * 60_000L -> recentlyLabel
    else -> {
      val formatted = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(date)
      String.format(lastSeenAtFormat, formatted)
    }
  }
}
