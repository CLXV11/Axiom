package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.util.LocalAppStrings

/**
 * Curated Apple-style & surreal expressive reaction palettes for messages and posts.
 */
object KoraReactionPalette {
  val quickBarEmojis = listOf("❤️", "😂", "😮", "🔥", "🤯", "🫠", "🤌")

  val weirdEmojis = listOf(
    "🤯", "🫠", "🤌", "🦄", "🛸", "🪩",
    "👁️", "💀", "👾", "⚡", "🧠", "🌪️",
    "🎭", "🦖", "🗿", "🔮", "🧿", "🫧",
    "🤡", "🐸", "🫀", "🦾", "🫥", "🫡"
  )

  val classicEmojis = listOf(
    "❤️", "🩷", "😂", "🥹", "😍", "😮",
    "😢", "😡", "👏", "🙌", "🔥", "💯",
    "✨", "💎", "🫶", "🤝", "🎉", "👑",
    "🚀", "🌶️", "🍓", "🦋", "🌈", "⚡"
  )

  val signalEmojis = listOf(
    "👍", "👎", "👀", "🫡", "🤔", "🤫",
    "😴", "🥳", "🥶", "🥵", "🤩", "😇",
    "🥷", "🧙‍♂️", "🐉", "🌙", "☄️", "🛸"
  )
}

/**
 * iPhone iMessage-inspired floating pill tapback bar with quick weird & classic emojis plus a "+" expand button.
 */
@Composable
fun IosFloatingReactionBar(
  selectedEmojis: Set<String>,
  onEmojiSelected: (String) -> Unit,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme

  Surface(
    shape = RoundedCornerShape(999.dp),
    color = Color(0xFF151822).copy(alpha = 0.96f),
    border = BorderStroke(
      1.dp,
      Brush.linearGradient(
        listOf(
          Color.White.copy(alpha = 0.24f),
          colors.primary.copy(alpha = 0.45f),
          Color.White.copy(alpha = 0.14f)
        )
      )
    ),
    shadowElevation = 10.dp,
    modifier = modifier
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      KoraReactionPalette.quickBarEmojis.forEach { emoji ->
        val isSelected = selectedEmojis.contains(emoji)
        val scale by animateFloatAsState(
          targetValue = if (isSelected) 1.14f else 1.0f,
          animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
          ),
          label = "tapback_scale"
        )
        Box(
          modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
              if (isSelected) colors.primary.copy(alpha = 0.32f)
              else Color.White.copy(alpha = 0.06f)
            )
            .then(
              if (isSelected) {
                Modifier.border(1.2.dp, colors.primary, CircleShape)
              } else {
                Modifier
              }
            )
            .clickable { onEmojiSelected(emoji) }
            .testTag("quick_reaction_$emoji"),
          contentAlignment = Alignment.Center
        ) {
          GlossyAppleEmoji(
            emoji = emoji,
            size = 24.dp
          )
        }
      }

      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.14f))
          .clickable(onClick = onMoreClick)
          .testTag("more_reactions_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = strings.moreReactionsDesc,
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

/**
 * Interactive FlowRow of multi-emoji reaction pills shown under messages and posts.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionChipsRow(
  reactions: Map<String, List<String>>,
  currentUid: String,
  onToggleReaction: (String) -> Unit,
  modifier: Modifier = Modifier,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.Start
) {
  val activeEntries = remember(reactions) {
    reactions.entries
      .filter { it.value.isNotEmpty() }
      .sortedByDescending { it.value.size }
  }
  if (activeEntries.isEmpty()) return

  val colors = MaterialTheme.colorScheme

  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    activeEntries.forEach { (emoji, uids) ->
      val reactedByMe = uids.contains(currentUid)
      Surface(
        onClick = { onToggleReaction(emoji) },
        shape = RoundedCornerShape(999.dp),
        color = if (reactedByMe) {
          colors.primary.copy(alpha = 0.2f)
        } else {
          colors.surfaceVariant.copy(alpha = 0.72f)
        },
        border = BorderStroke(
          width = if (reactedByMe) 1.2.dp else 1.dp,
          color = if (reactedByMe) {
            colors.primary.copy(alpha = 0.78f)
          } else {
            colors.outlineVariant.copy(alpha = 0.45f)
          }
        ),
        modifier = Modifier
          .defaultMinSize(minHeight = 30.dp)
          .testTag("reaction_chip_$emoji")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          GlossyAppleEmoji(
            emoji = emoji,
            size = 16.dp
          )
          Text(
            text = uids.size.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (reactedByMe) FontWeight.ExtraBold else FontWeight.SemiBold,
              fontSize = 11.5.sp
            ),
            color = if (reactedByMe) colors.primary else colors.onSurfaceVariant
          )
        }
      }
    }
  }
}

/**
 * Full bottom-sheet emoji picker categorized into Weird, Classic, and Signals with Apple-like crisp tiles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeirdEmojiPickerBottomSheet(
  selectedEmojis: Set<String>,
  onEmojiSelected: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val strings = LocalAppStrings.current
  val colors = MaterialTheme.colorScheme
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  val tabs = listOf(
    strings.weirdReactionsTab to KoraReactionPalette.weirdEmojis,
    strings.classicReactionsTab to KoraReactionPalette.classicEmojis,
    strings.signalReactionsTab to KoraReactionPalette.signalEmojis
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surface,
    contentColor = colors.onSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 600.dp)
        .align(Alignment.CenterHorizontally)
        .navigationBarsPadding()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = strings.emojiPickerTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = colors.onSurface
        )
        IconButton(onClick = onDismiss) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = strings.cancel,
            tint = colors.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        tabs.forEachIndexed { idx, (label, _) ->
          val active = selectedTabIndex == idx
          FilterChip(
            selected = active,
            onClick = { selectedTabIndex = idx },
            label = {
              Text(
                text = label,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = colors.primary.copy(alpha = 0.18f),
              selectedLabelColor = colors.primary
            ),
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))
      Spacer(modifier = Modifier.height(12.dp))

      val activeEmojis = tabs[selectedTabIndex].second
      LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        contentPadding = PaddingValues(bottom = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
      ) {
        items(activeEmojis) { emoji ->
          val isSelected = selectedEmojis.contains(emoji)
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(
                if (isSelected) colors.primary.copy(alpha = 0.22f)
                else colors.surfaceVariant.copy(alpha = 0.48f)
              )
              .then(
                if (isSelected) {
                  Modifier.border(1.5.dp, colors.primary, RoundedCornerShape(14.dp))
                } else {
                  Modifier.border(1.dp, colors.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                }
              )
              .clickable { onEmojiSelected(emoji) }
              .testTag("emoji_grid_item_$emoji"),
            contentAlignment = Alignment.Center
          ) {
            GlossyAppleEmoji(
              emoji = emoji,
              size = 30.dp
            )
          }
        }
      }
    }
  }
}
