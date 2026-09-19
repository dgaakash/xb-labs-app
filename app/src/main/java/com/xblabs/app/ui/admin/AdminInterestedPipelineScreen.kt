package com.xblabs.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.Client
import com.xblabs.app.data.models.ClientStatus

@Composable
fun AdminInterestedPipelineScreen(
    clients: List<Client>
) {
    val interestedClients = clients.filter { it.currentStatus == ClientStatus.INTERESTED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Interested Prospects Pipeline", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("${interestedClients.size} hot leads identified", fontSize = 12.sp, color = Color(0xFF94A3B8))

        Spacer(modifier = Modifier.height(16.dp))

        if (interestedClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No interested clients recorded yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(interestedClients) { client ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(client.businessName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f)
                                ) {
                                    Text("HIGH PRIORITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Phone: ${client.phone} • Cat: ${client.category}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text("Assigned Employee: ${client.assignedEmployeeName ?: "Unassigned"}", fontSize = 12.sp, color = Color(0xFF38BDF8))
                            Text("Address: ${client.address}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}
