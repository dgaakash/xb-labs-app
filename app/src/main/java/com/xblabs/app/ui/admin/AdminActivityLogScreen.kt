package com.xblabs.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.ActivityLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminActivityLogScreen(
    activityLogs: List<ActivityLog>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF38BDF8))
            Spacer(modifier = Modifier.width(8.dp))
            Text("System Audit Activity Log", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activityLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No activity logged yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            val dateFormat = SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault())

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activityLogs) { log ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${log.userName} • ${log.action}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )

                                Text(
                                    text = dateFormat.format(Date(log.timestamp)),
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            if (log.metadata.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = log.metadata, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }
            }
        }
    }
}
