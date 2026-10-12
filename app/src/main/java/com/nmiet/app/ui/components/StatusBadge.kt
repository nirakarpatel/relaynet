package com.nmiet.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nmiet.app.ui.theme.AccentCyan
import com.nmiet.app.ui.theme.OfflineGray
import com.nmiet.app.ui.theme.PrimaryBlue
import com.nmiet.app.ui.theme.SuccessGreen
import com.nmiet.app.ui.theme.WarningAmber

/**
 * Top Status Banner displayed across all screens as mandated by spec:
 * - Green: "● Online (Cloud Gateway Active)"
 * - Amber: "● Local Mesh Connected (Peer: Node-02)"
 * - Red: "● Offline Mode (Serving from Room SQLite Cache)"
 */
@Composable
fun NetworkStatusBanner(
    isOnline: Boolean,
    connectedPeerName: String? = null,
    modifier: Modifier = Modifier
) {
    val (statusText, bgColor, textColor, dotColor) = when {
        isOnline -> Quadruple(
            "● Online (Cloud Gateway Active)",
            SuccessGreen.copy(alpha = 0.15f),
            SuccessGreen,
            SuccessGreen
        )
        connectedPeerName != null -> Quadruple(
            "● Local Mesh Connected (Peer: $connectedPeerName)",
            WarningAmber.copy(alpha = 0.15f),
            WarningAmber,
            WarningAmber
        )
        else -> Quadruple(
            "● Offline Mode (Serving from Room SQLite Cache)",
            MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText.removePrefix("● "),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun OnlineOfflinePill(
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isOnline) SuccessGreen.copy(alpha = 0.15f) else OfflineGray.copy(alpha = 0.15f)
    val dotColor = if (isOnline) SuccessGreen else OfflineGray
    val text = if (isOnline) "ONLINE" else "OFFLINE"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = dotColor
        )
    }
}

@Composable
fun DemoModeBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(WarningAmber.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "DEMO MODE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = WarningAmber
        )
    }
}

@Composable
fun StoredOfflineBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SuccessGreen.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "Downloaded / Ready Offline",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = SuccessGreen
        )
    }
}

@Composable
fun ReceivedViaNearbyBadge(peerName: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AccentCyan.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (peerName != null) "Via: $peerName" else "Received via Peer",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentCyan
        )
    }
}

fun formatRelativeTime(timestamp: Long): String {
    val diffMillis = System.currentTimeMillis() - timestamp
    val minutes = diffMillis / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
    }
}
