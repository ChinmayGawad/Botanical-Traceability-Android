package com.florachain.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme

@Composable
fun StageProgressChip(
    stageNumber: Int,
    stageTitle: String,
    isCompleted: Boolean,
    isCurrent: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = com.florachain.designsystem.FloraChainTheme.colors

    val bgColor = when {
        isCompleted -> colors.primary
        isCurrent -> colors.primaryContainer
        else -> colors.surfaceElevated
    }

    val textColor = when {
        isCompleted -> colors.onPrimary
        isCurrent -> colors.onPrimaryContainer
        else -> colors.textMuted
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = if (isCurrent) 0.3f else 1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$stageNumber.",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = textColor
        )
        Text(
            text = stageTitle,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = textColor
        )
    }
}
