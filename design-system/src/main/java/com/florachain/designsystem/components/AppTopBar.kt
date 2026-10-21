package com.florachain.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.BotanicalEmerald
import com.florachain.designsystem.FloraChainTheme
import com.florachain.designsystem.LocalThemeController

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.florachain.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false,
    onNavigateBack: () -> Unit = {},
    showNetworkPill: Boolean = false,
    showThemeToggle: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = FloraChainTheme.colors
    val themeController = LocalThemeController.current

    val themeIconRotation by animateFloatAsState(
        targetValue = if (themeController.isDark) 0f else 180f,
        animationSpec = tween(400),
        label = "ThemeRotation"
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!canNavigateBack && title == "FloraChain") {
                    Image(
                        painter = painterResource(id = R.drawable.ic_florachain_logo),
                        contentDescription = "FloraChain Logo",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontSize = 19.sp
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.primary
                    )
                }
            }
        },
        actions = {
            // Polygon Amoy Status Pill
            if (showNetworkPill) {
                Surface(
                    color = colors.primary.copy(alpha = if (colors.isDark) 0.15f else 0.12f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(BotanicalEmerald, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Polygon Amoy",
                            color = colors.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick Dark/Light Theme Toggle
            if (showThemeToggle) {
                IconButton(onClick = { themeController.toggleTheme() }) {
                    Icon(
                        imageVector = if (themeController.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (themeController.isDark) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = if (themeController.isDark) BotanicalEmerald else colors.primary,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                rotationZ = themeIconRotation
                            }
                    )
                }
            }

            actions()
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.background,
            titleContentColor = colors.textPrimary
        ),
        modifier = modifier
    )
}
