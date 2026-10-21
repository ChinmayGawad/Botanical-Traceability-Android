package com.florachain.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.BotanicalEmerald
import com.florachain.designsystem.FloraChainTheme

data class NodeInfo(
    val name: String,
    val icon: ImageVector,
    val status: String
)

@Composable
fun ConsensusNodeStrip(
    modifier: Modifier = Modifier
) {
    val colors = FloraChainTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "ConsensusPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val nodes = remember {
        listOf(
            NodeInfo("Farm", Icons.Default.Agriculture, "Genesis"),
            NodeInfo("Mill", Icons.Default.PrecisionManufacturing, "<45°C"),
            NodeInfo("Lab", Icons.Default.Biotech, "HPLC"),
            NodeInfo("Cold", Icons.Default.LocalShipping, "4.2°C"),
            NodeInfo("Retail", Icons.Default.Storefront, "Verified")
        )
    }

    CyberGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsing Consensus LED (Draw-phase deferred, 0 recomposition)
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha * 0.35f
                            }
                            .background(BotanicalEmerald, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(BotanicalEmerald, CircleShape)
                    )
                }

                Text(
                    text = "5-NODE LIVE CONSENSUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    letterSpacing = 0.8.sp
                )
            }

            Surface(
                color = colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "100% IN SYNC",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Interconnected 5-node pipeline row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            nodes.forEachIndexed { index, node ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (colors.isDark) colors.surfaceElevated.copy(alpha = 0.7f)
                                else colors.surfaceElevated
                            )
                            .border(
                                1.dp,
                                colors.primary.copy(alpha = 0.45f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = node.icon,
                            contentDescription = node.name,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = node.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Text(
                        text = node.status,
                        fontSize = 8.sp,
                        color = colors.textMuted
                    )
                }

                // Connecting arrow or line between nodes (Draw-phase deferred)
                if (index < nodes.size - 1) {
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(10.dp)
                            .graphicsLayer {
                                alpha = pulseAlpha * 0.7f
                            }
                            .background(colors.primary)
                    )
                }
            }
        }
    }
}
