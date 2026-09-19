package com.xblabs.app.ui.employee

import androidx.compose.foundation.clickable
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
import com.xblabs.app.data.models.User

@Composable
fun EmployeeInterestedPipelineScreen(
    employee: User,
    clients: List<Client>,
    onSelectClient: (Client) -> Unit
) {
    val myInterested = clients.filter {
        it.assignedEmployeeId == employee.id && it.currentStatus == ClientStatus.INTERESTED
    }

    val primaryColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF10B981)
    val cardBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val cardBorder = if (employee.isPinkPrincess) Color(0xFF4A204B) else Color(0xFF334155)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("My Interested Prospects 🔥", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text("${myInterested.size} high priority prospects in pipeline", fontSize = 12.sp, color = Color(0xFF94A3B8))

        Spacer(modifier = Modifier.height(14.dp))

        if (myInterested.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (employee.isPinkPrincess) "No interested clients yet ✨ Keep making calls!" else "No interested clients recorded yet.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myInterested) { client ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectClient(client) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(client.businessName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Phone: ${client.phone} • Cat: ${client.category}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text("Address: ${client.address}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
        }
    }
}
