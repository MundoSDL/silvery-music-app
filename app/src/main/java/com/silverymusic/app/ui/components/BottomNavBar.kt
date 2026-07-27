package com.silverymusic.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.silverymusic.app.theme.SilveryTheme

enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    DISCOVER("Discover", Icons.Filled.Explore),
    LIBRARY("Library", Icons.Filled.LibraryMusic),
}

@Composable
fun BottomNavBar(
    // Nullable: the Search screen is still reachable from the top search bars,
    // and shows this bar with no tab highlighted rather than hiding it.
    selectedTab: BottomTab?,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val interactionSource = remember { MutableInteractionSource() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = false),
                        onClick = { onTabSelected(tab) },
                    )
                    .padding(top = 8.dp, bottom = 8.dp),
            ) {
                val tint = if (selected) MaterialTheme.colorScheme.onBackground else SilveryTheme.colors.navInactive
                Icon(imageVector = tab.icon, contentDescription = tab.label, tint = tint)
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                )
            }
        }
    }
}
