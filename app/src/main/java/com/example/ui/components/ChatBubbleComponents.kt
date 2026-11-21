package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AddReaction
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.data.repository.ChatMediaRepository
import com.example.ui.theme.StatusOnline
import com.example.ui.util.LocalAppStrings
import java.io.File
import java.text.BreakIterator
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ChatMessageDisplayItem(
  val message: Message,
  val showDateDivider: Boolean,
  val dateLabel: String,
  val isFirstInGroup: Boolean,
  val isLastInGroup: Boolean,
  val jumboEmojiCount: Int
) {
  val isJumboEmoji: Boolean get() = jumboEmojiCount > 0
}

/**
 * Counts 1..3 visual grapheme clusters only when EVERY cluster is a pure emoji (no letters/digits).
 */
internal fun countPureJumboEmojis(text: String): Int {
  val trimmed = text.trim()
  if (trimmed.isEmpty() || trimmed.length > 28) return 0
  val iterator = BreakIterator.getCharacterInstance()
  iterator.setText(trimmed)
  var start = iterator.first()
  var end = iterator.next()
  var count = 0
  while (end != BreakIterator.DONE) {
    val cluster = trimmed.substring(start, end)
    if (cluster.isBlank()) return 0
    if (!isEmojiCluster(cluster)) return 0
    count++
    if (count > 3) return 0
    start = end
    end = iterator.next()
  }
  return count
}

private fun isEmojiCluster(cluster: String): Boolean {
  var hasPictographic = false
  var idx = 0
  while (idx < cluster.length) {
    val cp = cluster.codePointAt(idx)
    idx += Character.charCount(cp)
    if (cp == 0x200D || cp == 0xFE0F || cp == 0xFE0E || cp in 0x1F3FB..0x1F3FF) continue
    if (cp in 0x0020..0x007E) return false // Reject plain ASCII letters, numbers & punctuation
    val isPictograph = (cp in 0x1F300..0x1FAFF) ||
      (cp in 0x2600..0x27BF) ||
      (cp in 0x2300..0x23FF) ||
      (cp in 0x1F1E6..0x1F1FF)
    if (!isPictograph) return false
    hasPictographic = true
  }
  return hasPictographic
}

internal fun buildChatDisplayItems(
  messages: List<Message>,
  todayLabel: String,
  yesterdayLabel: String,
  locale: Locale = Locale.getDefault()
): List<ChatMessageDisplayItem> {
  if (messages.isEmpty()) return emptyList()
  val todayCal = Calendar.getInstance()
  val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
  val dayFormatter = SimpleDateFormat("d MMM yyyy", locale)

  fun sameDay(c1: Calendar, c2: Calendar): Boolean =
    c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
      c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)

  fun calendarFor(msg: Message): Calendar {
    val date = msg.createdAt?.toDate() ?: Date()
    return Calendar.getInstance().apply { time = date }
  }

  val calendars = messages.map { calendarFor(it) }
  val groupWindowMs = 3 * 60 * 1000L

  return messages.mapIndexed { idx, msg ->
    val cal = calendars[idx]
    val prev = messages.getOrNull(idx - 1)
    val prevCal = calendars.getOrNull(idx - 1)
    val next = messages.getOrNull(idx + 1)
    val nextCal = calendars.getOrNull(idx + 1)

    val showDateDivider = prevCal == null || !sameDay(cal, prevCal)
    val dateLabel = when {
      sameDay(cal, todayCal) -> todayLabel
      sameDay(cal, yesterdayCal) -> yesterdayLabel
      else -> dayFormatter.format(cal.time)
    }

    val sameAsPrev = prev != null &&
      prevCal != null &&
      sameDay(cal, prevCal) &&
      prev.senderId == msg.senderId &&
      kotlin.math.abs(cal.timeInMillis - prevCal.timeInMillis) <= groupWindowMs

    val sameAsNext = next != null &&
      nextCal != null &&
      sameDay(cal, nextCal) &&
      next.senderId == msg.senderId &&
      kotlin.math.abs(nextCal.timeInMillis - cal.timeInMillis) <= groupWindowMs

    val eligibleJumbo = !msg.isDeleted &&
      !msg.hasMediaAttachment() &&
      !msg.isPollMessage() &&
      msg.replyToText.isNullOrBlank() &&
      msg.forwardedFromName.isNullOrBlank() &&
      !msg.isSpoiler
    val jumboCount = if (eligibleJumbo) countPureJumboEmojis(msg.content) else 0

    ChatMessageDisplayItem(
      message = msg,
      showDateDivider = showDateDivider,
      dateLabel = dateLabel,
      isFirstInGroup = !sameAsPrev,
      isLastInGroup = !sameAsNext,
      jumboEmojiCount = jumboCount
    )
  }
}

@Composable
internal fun ChatDateDividerPill(
  label: String,
  modifier: Modifier = Modifier
) {
  val colors = MaterialTheme.colorScheme
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(999.dp),
      color = colors.surfaceVariant.copy(alpha = 0.68f)
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
        fontWeight = FontWeight.SemiBold,
        color = colors.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
      )
    }
  }
}

@Composable
internal fun TelegramChatTopBar(
  title: String,
  subtitle: String,
  avatarUrl: String?,
  frameStyleKey: String? = null,
  isAiChat: Boolean,
  isVerified: Boolean,
  verifiedTier: String?,
  isThinkingOrTyping: Boolean,
  canClearAiChat: Boolean,
  canStartVoiceCall: Boolean,
  canInspectOrViewInfo: Boolean = true,
  isSearchOpen: Boolean = false,
  onHeaderClick: () -> Unit = {},
  onToggleSearch: () -> Unit = {},
  onOpenChatInfo: () -> Unit = {},
  onStartVoiceCall: () -> Unit,
  onClearAiChat: () -> Unit,
  onBackClick: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme

  Surface(
    color = colors.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.statusBarsPadding()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .defaultMinSize(minHeight = 60.dp)
          .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.size(48.dp)
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = colors.onSurface,
            modifier = Modifier.size(22.dp)
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = canInspectOrViewInfo, onClick = onHeaderClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("chat_header_profile_button")
        ) {
          if (isAiChat) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                  Brush.linearGradient(
                    colors = listOf(colors.primary, colors.primary.copy(alpha = 0.72f))
                  )
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(21.dp)
              )
            }
          } else {
            AvatarView(
              name = title,
              avatarUrl = avatarUrl,
              size = 42.dp,
              frameStyleKey = frameStyleKey,
              showPresence = true,
              isOnline = true
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.5.sp),
                fontWeight = FontWeight.Bold,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
              if (isVerified) {
                Spacer(modifier = Modifier.width(5.dp))
                VerifiedBadge(
                  size = 15.dp,
                  tierKey = verifiedTier,
                  showDialogOnClick = true
                )
              }
              if (isAiChat) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = colors.primary.copy(alpha = 0.14f)
                ) {
                  Text(
                    text = strings.aiBotBadge,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 1.dp)
            ) {
              if (isThinkingOrTyping) {
                TypingDotsIndicator(
                  dotSize = 4.dp,
                  travelDistance = 2.dp,
                  dotColor = colors.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = subtitle,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                  color = colors.primary,
                  fontWeight = FontWeight.Medium,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(6.5.dp)
                    .clip(CircleShape)
                    .background(StatusOnline)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = subtitle,
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                  color = colors.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        if (canStartVoiceCall) {
          IconButton(
            onClick = onStartVoiceCall,
            modifier = Modifier
              .size(48.dp)
              .testTag("start_voice_call_button")
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = strings.startVoiceCall,
              tint = colors.onSurface,
              modifier = Modifier.size(21.dp)
            )
          }
        }

        IconButton(
          onClick = onToggleSearch,
          modifier = Modifier
            .size(48.dp)
            .testTag("chat_search_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = strings.searchMessagesPlaceholder,
            tint = if (isSearchOpen) colors.primary else colors.onSurfaceVariant,
            modifier = Modifier.size(21.dp)
          )
        }

        if (canInspectOrViewInfo) {
          IconButton(
            onClick = onOpenChatInfo,
            modifier = Modifier
              .size(48.dp)
              .testTag("chat_info_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = strings.chatInfoTitle,
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(21.dp)
            )
          }
        }

        if (canClearAiChat) {
          IconButton(
            onClick = onClearAiChat,
            modifier = Modifier
              .size(48.dp)
              .testTag("clear_ai_chat_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = strings.aiClearChat,
              tint = colors.onSurfaceVariant,
              modifier = Modifier.size(21.dp)
            )
          }
        }
      }

      HorizontalDivider(
        thickness = 0.6.dp,
        color = colors.outlineVariant.copy(alpha = 0.30f)
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TelegramMessageBubble(
  conversationId: String,
  currentUid: String,
  displayItem: ChatMessageDisplayItem,
  isMe: Boolean,
  isAiMessage: Boolean,
  isGroupChat: Boolean,
  isPinned: Boolean = false,
  isSearchHighlighted: Boolean = false,
  senderAvatarUrl: String?,
  senderFrameKey: String? = null,
  chatMediaRepository: ChatMediaRepository,
  isInlineVideoPlaying: Boolean,
  onInspectSender: () -> Unit = {},
  onVotePoll: (Int) -> Unit = {},
  onPlayInlineVideo: () -> Unit,
  onStopInlineVideo: () -> Unit,
  onOpenFullscreenVideo: (File) -> Unit,
  onToggleReaction: (String) -> Unit,
  onOpenReactionSheet: () -> Unit,
  onReply: () -> Unit,
  onCopy: () -> Unit,
  onForward: (() -> Unit)? = null,
  onTogglePin: (() -> Unit)? = null,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onSaveMemory: () -> Unit
) {
  val message = displayItem.message
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val layoutDirection = LocalLayoutDirection.current
  var showMenu by remember { mutableStateOf(false) }
  var isSpoilerRevealed by remember(message.id) { mutableStateOf(false) }

  val formattedTime = remember(message.createdAt) {
    val date = message.createdAt?.toDate()
    if (date != null) {
      try {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
      } catch (_: Exception) {
        ""
      }
    } else ""
  }

  val isDarkBackdrop = colors.background.luminance() < 0.5f
  val bubbleFill = when {
    isSearchHighlighted -> colors.primary.copy(alpha = 0.42f)
    isMe -> if (isDarkBackdrop) {
      lerp(Color(0xFF1F2740), colors.primary, 0.44f)
    } else {
      lerp(colors.surfaceVariant, colors.primary, 0.22f)
    }
    else -> if (isDarkBackdrop) {
      lerp(colors.surface, colors.onSurface, 0.08f)
    } else {
      colors.surfaceVariant
    }
  }

  // Telegram/WhatsApp stacked group corner geometry
  val bubbleShape = RoundedCornerShape(
    topStart = if (!isMe && !displayItem.isFirstInGroup) 6.dp else 18.dp,
    topEnd = if (isMe && !displayItem.isFirstInGroup) 6.dp else 18.dp,
    bottomStart = if (!isMe && displayItem.isLastInGroup) 4.dp else if (!isMe) 6.dp else 18.dp,
    bottomEnd = if (isMe && displayItem.isLastInGroup) 4.dp else if (isMe) 6.dp else 18.dp
  )

  val isEdgeMediaWithoutText = message.hasMediaAttachment() &&
    !message.isDeleted &&
    (message.mediaType == "image" || message.mediaType == "video") &&
    !(message.isSpoiler && !isSpoilerRevealed) &&
    (message.content.isBlank() ||
      message.content.startsWith("📷 ") ||
      message.content.startsWith("🎬 "))

  val tailOnRight = if (layoutDirection == LayoutDirection.Rtl) !isMe else isMe

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.Bottom
  ) {
    if (!isMe) {
      if (displayItem.isLastInGroup) {
        if (isAiMessage) {
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(colors.primary, colors.primary.copy(alpha = 0.72f))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = message.senderName,
              tint = Color.White,
              modifier = Modifier.size(15.dp)
            )
          }
        } else {
          Box(
            modifier = Modifier
              .clip(CircleShape)
              .clickable(onClick = onInspectSender)
              .testTag("message_avatar_inspect_${message.id}")
          ) {
            AvatarView(
              name = message.senderName,
              avatarUrl = senderAvatarUrl,
              size = 30.dp,
              frameStyleKey = senderFrameKey
            )
          }
        }
      } else {
        Spacer(modifier = Modifier.width(30.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
      modifier = Modifier.widthIn(max = 312.dp)
    ) {
      Box {
        if (displayItem.isJumboEmoji) {
          val emojiSizeSp = when (displayItem.jumboEmojiCount) {
            1 -> 44.sp
            2 -> 36.sp
            else -> 32.sp
          }
          Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
              )
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            Text(
              text = message.content.trim(),
              fontSize = emojiSizeSp,
              lineHeight = (emojiSizeSp.value + 6f).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = colors.surfaceVariant.copy(alpha = 0.72f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                if (isPinned) {
                  Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = strings.pinnedMessageBannerTitle,
                    modifier = Modifier.size(11.dp),
                    tint = Color(0xFFF59E0B)
                  )
                }
                if (formattedTime.isNotBlank()) {
                  Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                    color = colors.onSurfaceVariant
                  )
                }
                if (isMe) {
                  Icon(
                    imageVector = Icons.Default.DoneAll,
                    contentDescription = "Delivered",
                    modifier = Modifier.size(13.dp),
                    tint = Color(0xFF60A5FA)
                  )
                }
              }
            }
          }
        } else {
          val bubbleModifier = Modifier
            .widthIn(max = 308.dp)
            .drawBehind {
              if (displayItem.isLastInGroup) {
                val tailW = 6.dp.toPx()
                val tailH = 11.dp.toPx()
                val baseY = size.height
                val path = if (tailOnRight) {
                  Path().apply {
                    moveTo(size.width - 4.dp.toPx(), baseY)
                    lineTo(size.width + tailW, baseY)
                    quadraticTo(size.width, baseY - 2.dp.toPx(), size.width, baseY - tailH)
                    close()
                  }
                } else {
                  Path().apply {
                    moveTo(4.dp.toPx(), baseY)
                    lineTo(-tailW, baseY)
                    quadraticTo(0f, baseY - 2.dp.toPx(), 0f, baseY - tailH)
                    close()
                  }
                }
                drawPath(path = path, color = bubbleFill)
              }
            }
            .clip(bubbleShape)
            .combinedClickable(
              onClick = {
                if (message.isSpoiler && !isSpoilerRevealed && !message.isDeleted) {
                  isSpoilerRevealed = true
                }
              },
              onLongClick = { showMenu = true }
            )

          Surface(
            shape = bubbleShape,
            color = bubbleFill,
            border = when {
              isSearchHighlighted -> BorderStroke(1.5.dp, colors.primary)
              isPinned -> BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
              else -> null
            },
            modifier = bubbleModifier
          ) {
            Column(
              modifier = Modifier.padding(
                horizontal = if (isEdgeMediaWithoutText) 4.dp else 12.dp,
                vertical = if (isEdgeMediaWithoutText) 4.dp else 8.dp
              )
            ) {
              // Incoming sender badge row ONLY on first message of a group (or group chats)
              if (!isMe && (displayItem.isFirstInGroup || isGroupChat)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .padding(
                      start = if (isEdgeMediaWithoutText) 8.dp else 0.dp,
                      end = if (isEdgeMediaWithoutText) 8.dp else 0.dp,
                      top = if (isEdgeMediaWithoutText) 4.dp else 0.dp,
                      bottom = 4.dp
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = !isAiMessage, onClick = onInspectSender)
                ) {
                  Text(
                    text = if (isAiMessage) strings.aiAssistantName else message.senderName,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.5.sp),
                    fontWeight = FontWeight.Bold,
                    color = colors.primary
                  )
                  if (message.isSenderVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerifiedBadge(
                      size = 13.dp,
                      tierKey = message.senderVerifiedTier,
                      showDialogOnClick = true
                    )
                  }
                }
              }

              // Forwarded provenance pill
              if (!message.forwardedFromName.isNullOrBlank() && !message.isDeleted) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(bottom = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.Forward,
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = String.format(strings.forwardedFromLabel, message.forwardedFromName),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = colors.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                  )
                }
              }

              // Quoted Reply Strip
              if (!message.replyToText.isNullOrBlank()) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = if (isDarkBackdrop) 0.22f else 0.08f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .width(3.dp)
                      .height(26.dp)
                      .clip(RoundedCornerShape(2.dp))
                      .background(if (isMe) Color.White.copy(alpha = 0.85f) else colors.primary)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = message.replyToSender.orEmpty(),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = if (isMe) Color.White else colors.primary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = message.replyToText,
                      style = MaterialTheme.typography.labelSmall,
                      color = colors.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }

              // Spoiler Gate OR Message Content
              if (message.isSpoiler && !isSpoilerRevealed && !message.isDeleted) {
                Surface(
                  onClick = { isSpoilerRevealed = true },
                  shape = RoundedCornerShape(12.dp),
                  color = Color.Black.copy(alpha = if (isDarkBackdrop) 0.26f else 0.08f),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spoiler_cover_${message.id}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = Icons.Default.VisibilityOff,
                      contentDescription = strings.spoilerHiddenTitle,
                      tint = if (isMe) Color.White else colors.primary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = strings.spoilerHiddenTitle,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.onSurface
                      )
                      Text(
                        text = strings.spoilerTapToReveal,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant
                      )
                    }
                  }
                }
              } else {
                if (message.isPollMessage() && !message.isDeleted) {
                  PollMessageCard(
                    message = message,
                    currentUid = currentUid,
                    onVoteOption = onVotePoll
                  )
                }

                if (message.hasMediaAttachment() && !message.isDeleted) {
                  MediaMessageBubbleContent(
                    conversationId = conversationId,
                    message = message,
                    chatMediaRepository = chatMediaRepository,
                    isInlineVideoPlaying = isInlineVideoPlaying,
                    onPlayInlineVideo = onPlayInlineVideo,
                    onStopInlineVideo = onStopInlineVideo,
                    onOpenFullscreenVideo = onOpenFullscreenVideo,
                    isMeBubble = isMe,
                    cornerFooter = if (isEdgeMediaWithoutText) {
                      {
                        Surface(
                          shape = RoundedCornerShape(999.dp),
                          color = Color.Black.copy(alpha = 0.56f)
                        ) {
                          Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                          ) {
                            if (isPinned) {
                              Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = strings.pinnedMessageBannerTitle,
                                modifier = Modifier.size(10.dp),
                                tint = Color(0xFFF59E0B)
                              )
                            }
                            if (formattedTime.isNotBlank()) {
                              Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White
                              )
                            }
                            if (isMe) {
                              Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF60A5FA)
                              )
                            }
                          }
                        }
                      }
                    } else null
                  )
                }

                val isSyntheticMediaPlaceholder = message.hasMediaAttachment() &&
                  (message.content.startsWith("📷 ") ||
                    message.content.startsWith("🎬 ") ||
                    message.content.startsWith("🎤 ") ||
                    message.content.startsWith("📎 "))

                if (!message.isPollMessage() && !isSyntheticMediaPlaceholder && message.content.isNotBlank()) {
                  if (message.hasMediaAttachment() && !message.isDeleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                  }
                  Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                      fontSize = 15.5.sp,
                      lineHeight = 22.sp
                    ),
                    color = if (isMe && isDarkBackdrop) Color.White else colors.onSurface
                  )
                }
              }

              if (!isEdgeMediaWithoutText) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                  modifier = Modifier.align(Alignment.End),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  if (isPinned) {
                    Icon(
                      imageVector = Icons.Default.PushPin,
                      contentDescription = strings.pinnedMessageBannerTitle,
                      modifier = Modifier.size(11.dp),
                      tint = Color(0xFFF59E0B)
                    )
                  }
                  if (message.isEdited) {
                    Text(
                      text = strings.edited,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                      color = colors.onSurfaceVariant.copy(alpha = 0.78f)
                    )
                  }
                  if (formattedTime.isNotBlank()) {
                    Text(
                      text = formattedTime,
                      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                      color = if (isMe && isDarkBackdrop) {
                        Color.White.copy(alpha = 0.72f)
                      } else {
                        colors.onSurfaceVariant.copy(alpha = 0.80f)
                      }
                    )
                  }
                  if (isMe) {
                    Icon(
                      imageVector = Icons.Default.DoneAll,
                      contentDescription = "Delivered",
                      modifier = Modifier.size(14.dp),
                      tint = Color(0xFF60A5FA)
                    )
                  }
                }
              }
            }
          }
        }

        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false },
          modifier = Modifier.background(colors.surface)
        ) {
          if (!message.isDeleted) {
            val mySelectedEmojis = remember(message.reactions, currentUid) {
              message.reactions.filterValues { it.contains(currentUid) }.keys.toSet()
            }
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
              IosFloatingReactionBar(
                selectedEmojis = mySelectedEmojis,
                onEmojiSelected = { emoji ->
                  showMenu = false
                  onToggleReaction(emoji)
                },
                onMoreClick = {
                  showMenu = false
                  onOpenReactionSheet()
                }
              )
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.35f))
            DropdownMenuItem(
              text = { Text(strings.reactAction, color = colors.onSurface) },
              leadingIcon = {
                Icon(Icons.Outlined.AddReaction, contentDescription = null, tint = colors.primary)
              },
              onClick = {
                showMenu = false
                onOpenReactionSheet()
              }
            )
          }
          DropdownMenuItem(
            text = { Text(strings.copy, color = colors.onSurface) },
            leadingIcon = {
              Icon(Icons.Default.ContentCopy, contentDescription = null, tint = colors.onSurfaceVariant)
            },
            onClick = {
              showMenu = false
              onCopy()
            }
          )
          DropdownMenuItem(
            text = { Text(strings.reply, color = colors.onSurface) },
            leadingIcon = {
              Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, tint = colors.onSurfaceVariant)
            },
            onClick = {
              showMenu = false
              onReply()
            }
          )
          if (onForward != null) {
            DropdownMenuItem(
              text = { Text(strings.forwardAction, color = colors.onSurface) },
              leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null, tint = colors.onSurfaceVariant)
              },
              onClick = {
                showMenu = false
                onForward()
              }
            )
          }
          if (onTogglePin != null) {
            DropdownMenuItem(
              text = {
                Text(
                  text = if (isPinned) strings.unpinMessageAction else strings.pinMessageAction,
                  color = colors.onSurface
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B)
                )
              },
              onClick = {
                showMenu = false
                onTogglePin()
              }
            )
          }
          if (!isMe && !isAiMessage) {
            DropdownMenuItem(
              text = { Text(strings.inspectProfileAction, color = colors.onSurface) },
              leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = colors.primary)
              },
              onClick = {
                showMenu = false
                onInspectSender()
              }
            )
          }
          DropdownMenuItem(
            text = { Text(strings.saveToMemory, color = colors.onSurface) },
            leadingIcon = {
              Icon(Icons.Default.Bookmark, contentDescription = null, tint = colors.primary)
            },
            onClick = {
              showMenu = false
              onSaveMemory()
            }
          )
          if (isMe) {
            DropdownMenuItem(
              text = { Text(strings.edit, color = colors.onSurface) },
              leadingIcon = {
                Icon(Icons.Default.Edit, contentDescription = null, tint = colors.onSurfaceVariant)
              },
              onClick = {
                showMenu = false
                onEdit()
              }
            )
            DropdownMenuItem(
              text = { Text(strings.delete, color = colors.error) },
              leadingIcon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = colors.error)
              },
              onClick = {
                showMenu = false
                onDelete()
              }
            )
          }
        }
      }

      if (message.reactions.isNotEmpty() && !message.isDeleted) {
        ReactionChipsRow(
          reactions = message.reactions,
          currentUid = currentUid,
          onToggleReaction = onToggleReaction,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }
  }
}

@Composable
internal fun FloatingCapsuleComposer(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  isSendEnabled: Boolean,
  showAttachButton: Boolean,
  isAttachEnabled: Boolean,
  canRecordVoiceNote: Boolean = false,
  isRecordingVoice: Boolean = false,
  recordingElapsedMs: Long = 0L,
  isSpoilerMode: Boolean = false,
  onToggleSpoiler: () -> Unit = {},
  onStartRecording: () -> Unit = {},
  onCancelRecording: () -> Unit = {},
  onSendRecording: () -> Unit = {},
  onAttachClick: () -> Unit,
  onSend: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val useMicAction = canRecordVoiceNote && value.isBlank()

  val fabColor by animateColorAsState(
    targetValue = when {
      useMicAction || isSendEnabled -> colors.primary
      else -> colors.surfaceVariant
    },
    label = "composer_fab_bg"
  )
  val fabIconColor by animateColorAsState(
    targetValue = when {
      useMicAction || isSendEnabled -> colors.onPrimary
      else -> colors.onSurfaceVariant.copy(alpha = 0.55f)
    },
    label = "composer_fab_tint"
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 10.dp, vertical = 8.dp)
  ) {
    if (isRecordingVoice) {
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = colors.surfaceVariant,
        border = BorderStroke(1.dp, colors.error.copy(alpha = 0.45f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("voice_recording_bar")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onCancelRecording,
            modifier = Modifier
              .size(48.dp)
              .testTag("cancel_voice_recording_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = strings.cancelRecording,
              tint = colors.error
            )
          }

          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(colors.error)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${strings.recordingVoiceNote} • ${ChatMediaRepository.formatDuration(recordingElapsedMs)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          IconButton(
            onClick = onSendRecording,
            modifier = Modifier
              .size(48.dp)
              .testTag("send_voice_recording_button")
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = strings.sendVoiceNote,
                tint = colors.onPrimary,
                modifier = Modifier.size(19.dp)
              )
            }
          }
        }
      }
    } else {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Clean Telegram/WhatsApp floating input capsule
        Surface(
          shape = RoundedCornerShape(26.dp),
          color = colors.surfaceVariant.copy(alpha = 0.88f),
          border = if (isSpoilerMode) BorderStroke(1.2.dp, colors.primary) else null,
          modifier = Modifier.weight(1f)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(
                start = if (showAttachButton) 4.dp else 16.dp,
                end = if (showAttachButton) 4.dp else 16.dp,
                top = 3.dp,
                bottom = 3.dp
              ),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (showAttachButton) {
              IconButton(
                onClick = onToggleSpoiler,
                enabled = isAttachEnabled,
                modifier = Modifier
                  .size(44.dp)
                  .testTag("chat_spoiler_toggle")
              ) {
                Icon(
                  imageVector = if (isSpoilerMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = strings.spoilerToggleLabel,
                  tint = if (isSpoilerMode) colors.primary else colors.onSurfaceVariant,
                  modifier = Modifier.size(21.dp)
                )
              }
            }

            BasicTextField(
              value = value,
              onValueChange = onValueChange,
              textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                color = colors.onSurface,
                textAlign = TextAlign.Start
              ),
              cursorBrush = SolidColor(colors.primary),
              maxLines = 5,
              modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp, horizontal = 4.dp)
                .testTag("chat_message_input"),
              decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                  Text(
                    text = if (isSpoilerMode) "${strings.spoilerToggleLabel}: $placeholder" else placeholder,
                    style = MaterialTheme.typography.bodyLarge.copy(
                      fontSize = 15.5.sp,
                      textAlign = TextAlign.Start
                    ),
                    color = colors.onSurfaceVariant.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                  )
                }
                innerTextField()
              }
            )

            if (showAttachButton) {
              IconButton(
                onClick = onAttachClick,
                enabled = isAttachEnabled,
                modifier = Modifier
                  .size(44.dp)
                  .testTag("chat_attach_button")
              ) {
                Icon(
                  imageVector = Icons.Default.AttachFile,
                  contentDescription = strings.attachMedia,
                  tint = colors.onSurfaceVariant,
                  modifier = Modifier.size(22.dp)
                )
              }
            }
          }
        }

        // Independent 50.dp circular Mic / Send action button beside the floating capsule
        IconButton(
          onClick = if (useMicAction) onStartRecording else onSend,
          enabled = useMicAction || isSendEnabled,
          modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(fabColor)
            .testTag(if (useMicAction) "chat_record_voice_button" else "chat_send_button")
        ) {
          AnimatedContent(
            targetState = useMicAction,
            transitionSpec = {
              (fadeIn(tween(150)) + scaleIn(initialScale = 0.8f)) togetherWith
                (fadeOut(tween(100)) + scaleOut(targetScale = 0.8f))
            },
            label = "mic_send_morph"
          ) { isMic ->
            Icon(
              imageVector = if (isMic) Icons.Default.Mic else Icons.AutoMirrored.Filled.Send,
              contentDescription = if (isMic) strings.voiceNoteLabel else strings.send,
              tint = fabIconColor,
              modifier = Modifier.size(22.dp)
            )
          }
        }
      }
    }
  }
}
