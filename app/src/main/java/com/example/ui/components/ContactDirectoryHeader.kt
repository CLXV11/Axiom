package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Badge
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DiscordBlurple
import com.example.ui.theme.DiscordDarkElevated
import com.example.ui.theme.DiscordTextMuted
import com.example.ui.util.AppLanguage
import com.example.ui.util.LocalAppLanguage
import com.example.ui.util.LocalAppStrings

data class ProfessionCategory(
  val id: String,
  val labelEn: String,
  val labelAr: String,
  val filterKeyword: String,
  val icon: ImageVector
)

val DefaultProfessionCategories = listOf(
  ProfessionCategory(
    id = "all",
    labelEn = "All",
    labelAr = "الكل",
    filterKeyword = "all",
    icon = Icons.Default.AllInclusive
  ),
  ProfessionCategory(
    id = "verified_pro",
    labelEn = "Verified Pros",
    labelAr = "محترفون موثقون",
    filterKeyword = "verified_pro",
    icon = Icons.Default.WorkspacePremium
  ),
  ProfessionCategory(
    id = "engineering",
    labelEn = "Engineering",
    labelAr = "هندسة برمجيات",
    filterKeyword = "engineer",
    icon = Icons.Default.Code
  ),
  ProfessionCategory(
    id = "design",
    labelEn = "Design",
    labelAr = "تصميم واجهات",
    filterKeyword = "design",
    icon = Icons.Default.Brush
  ),
  ProfessionCategory(
    id = "architecture",
    labelEn = "Architecture",
    labelAr = "بنية أنظمة",
    filterKeyword = "architect",
    icon = Icons.Default.AccountTree
  ),
  ProfessionCategory(
    id = "security",
    labelEn = "Security",
    labelAr = "أمان وسيرفرات",
    filterKeyword = "security",
    icon = Icons.Default.Security
  ),
  ProfessionCategory(
    id = "ai",
    labelEn = "AI & ML",
    labelAr = "ذكاء اصطناعي",
    filterKeyword = "ai",
    icon = Icons.Default.AutoAwesome
  ),
  ProfessionCategory(
    id = "operations",
    labelEn = "Operations",
    labelAr = "إدارة وعمليات",
    filterKeyword = "lead",
    icon = Icons.Default.Work
  )
)

/**
 * Searchable contact directory header that provides real-time filtering
 * by person name, handle, or professional role and domain.
 */
@Composable
fun ContactDirectoryHeader(
  searchQuery: String,
  onSearchQueryChanged: (String) -> Unit,
  selectedProfessionId: String,
  onProfessionSelected: (String) -> Unit,
  filteredCount: Int,
  totalCount: Int,
  modifier: Modifier = Modifier,
  categories: List<ProfessionCategory> = DefaultProfessionCategories,
  showCategoryPills: Boolean = true,
  onClearFilters: () -> Unit = {}
) {
  val strings = LocalAppStrings.current
  val isArabic = LocalAppLanguage.current == AppLanguage.AR
  val hasActiveFilters = searchQuery.isNotBlank() || selectedProfessionId != "all"

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // 1. Search Bar Capsule
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = {
          Text(
            text = strings.searchByNameOrProfession,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 13.5.sp,
            maxLines = 1
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = if (searchQuery.isNotBlank()) DiscordBlurple else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(
              onClick = { onSearchQueryChanged("") },
              modifier = Modifier.size(36.dp).testTag("clear_directory_search_button")
            ) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = strings.clearSearch,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
          focusedBorderColor = DiscordBlurple,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("contact_directory_search_input")
      )

      // 2. Profession Category Pills (Horizontal Scroll)
      if (showCategoryPills) {
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          categories.forEach { category ->
            val isSelected = selectedProfessionId.equals(category.id, ignoreCase = true)
            val label = if (isArabic) category.labelAr else category.labelEn

            FilterChip(
              selected = isSelected,
              onClick = {
                onProfessionSelected(if (isSelected && category.id != "all") "all" else category.id)
              },
              label = {
                Text(
                  text = label,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 12.sp
                )
              },
              leadingIcon = {
                Icon(
                  imageVector = category.icon,
                  contentDescription = null,
                  modifier = Modifier.size(15.dp),
                  tint = if (isSelected) DiscordBlurple else MaterialTheme.colorScheme.onSurfaceVariant
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = DiscordBlurple.copy(alpha = 0.16f),
                selectedLabelColor = DiscordBlurple,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                labelColor = MaterialTheme.colorScheme.onSurface
              ),
              border = if (isSelected) {
                FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = true,
                  borderColor = DiscordBlurple,
                  borderWidth = 1.5.dp
                )
              } else {
                FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = false,
                  borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                  borderWidth = 1.dp
                )
              },
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.testTag("profession_chip_${category.id}")
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Status Bar & Active Match Count
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = null,
            tint = if (hasActiveFilters) DiscordBlurple else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = String.format(strings.contactsCount, filteredCount),
            style = MaterialTheme.typography.labelSmall,
            color = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (hasActiveFilters) FontWeight.SemiBold else FontWeight.Normal
          )

          if (hasActiveFilters && filteredCount < totalCount) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "• ${totalCount} total",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          }
        }

        AnimatedVisibility(
          visible = hasActiveFilters,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Text(
            text = strings.clearSearch,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = DiscordBlurple,
            modifier = Modifier
              .clickable {
                onSearchQueryChanged("")
                onProfessionSelected("all")
                onClearFilters()
              }
              .padding(4.dp)
              .testTag("clear_all_directory_filters")
          )
        }
      }
    }
  }
}
