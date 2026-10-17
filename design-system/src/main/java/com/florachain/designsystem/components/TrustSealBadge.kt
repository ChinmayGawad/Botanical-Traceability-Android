package com.florachain.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.ErrorRose
import com.florachain.designsystem.TrustGreen
import com.florachain.designsystem.WarningAmber

enum class TrustState {
    VERIFIED_ON_CHAIN,
    PENDING_SYNC,
    COMPROMISED,
    OFFLINE_RECORDED
}

@Composable
fun TrustSealBadge(
    state: TrustState,
    modifier: Modifier = Modifier,
    trustScore: Int = 100
) {
    val (bgColor, borderColor, textColor, icon, label) = when (state) {
        TrustState.VERIFIED_ON_CHAIN -> {
            Quint(
                Color(0x2210B981),
                TrustGreen,
                TrustGreen,
                Icons.Default.CheckCircle,
                "VERIFIED ON CHAIN ($trustScore%)"
            )
        }
        TrustState.PENDING_SYNC, TrustState.OFFLINE_RECORDED -> {
            Quint(
                Color(0x22F59E0B),
                WarningAmber,
                WarningAmber,
                Icons.Default.HourglassTop,
                "LOCAL PENDING SYNC"
            )
        }
        TrustState.COMPROMISED -> {
            Quint(
                Color(0x22F43F5E),
                ErrorRose,
                ErrorRose,
                Icons.Default.Warning,
                "COMPROMISED INTEGRITY"
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
