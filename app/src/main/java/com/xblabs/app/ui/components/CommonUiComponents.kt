package com.xblabs.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*

@Composable
fun CrmMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = Color(0xFF38BDF8),
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = iconColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ClientStatusChip(status: ClientStatus) {
    val (bg, text, label) = when (status) {
        ClientStatus.NEW -> Triple(Color(0xFF3B82F6).copy(alpha = 0.2f), Color(0xFF60A5FA), "New")
        ClientStatus.IN_PROGRESS -> Triple(Color(0xFF0EA5E9).copy(alpha = 0.2f), Color(0xFF38BDF8), "In Progress")
        ClientStatus.FOLLOW_UP -> Triple(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFFBBF24), "Follow-Up")
        ClientStatus.INTERESTED -> Triple(Color(0xFF10B981).copy(alpha = 0.2f), Color(0xFF34D399), "Interested")
        ClientStatus.NOT_INTERESTED -> Triple(Color(0xFF64748B).copy(alpha = 0.2f), Color(0xFF94A3B8), "Not Interested")
        ClientStatus.COMPLETED -> Triple(Color(0xFF8B5CF6).copy(alpha = 0.2f), Color(0xFFA78BFA), "Completed")
        ClientStatus.ARCHIVED -> Triple(Color(0xFF475569).copy(alpha = 0.2f), Color(0xFF94A3B8), "Archived")
        ClientStatus.CLOSED -> Triple(Color(0xFFEF4444).copy(alpha = 0.2f), Color(0xFFF87171), "Closed")
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(1.dp, text.copy(alpha = 0.4f))
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun FollowUpBadge(attemptNumber: Int) {
    val text = "Follow-up $attemptNumber / 3"
    val color = when (attemptNumber) {
        1 -> Color(0xFF38BDF8)
        2 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PriorityBadge(priority: Priority) {
    if (priority == Priority.NORMAL) return
    val (color, text) = if (priority == Priority.HIGH) Pair(Color(0xFFEF4444), "HIGH PRIORITY") else Pair(Color(0xFF64748B), "LOW")

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun NotificationCenterDialog(
    notifications: List<Notification>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onNotificationClick: (Notification) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Notifications", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onMarkAllRead) {
                    Text(text = "Mark all read", color = Color(0xFF38BDF8), fontSize = 12.sp)
                }
            }
        },
        text = {
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No notifications yet", color = Color(0xFF94A3B8), fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNotificationClick(notif) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (notif.isRead) Color(0xFF0F172A) else Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (notif.isRead) Color(0xFF334155) else Color(0xFF38BDF8).copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = notif.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (notif.isRead) Color(0xFFCBD5E1) else Color.White
                                    )
                                    if (!notif.isRead) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF38BDF8))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = Color(0xFF94A3B8))
            }
        },
        containerColor = Color(0xFF1E293B)
    )
}
