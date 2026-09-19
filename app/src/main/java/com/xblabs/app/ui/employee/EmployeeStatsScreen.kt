package com.xblabs.app.ui.employee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.xblabs.app.data.models.CallRecord
import com.xblabs.app.data.models.CallStatusOutcome
import com.xblabs.app.data.models.Client
import com.xblabs.app.data.models.User

@Composable
fun EmployeeStatsScreen(
    employee: User,
    clients: List<Client>,
    callRecords: List<CallRecord>
) {
    val myCalls = callRecords.filter { it.employeeId == employee.id }
    val myClients = clients.filter { it.assignedEmployeeId == employee.id }

    val totalCalls = myCalls.size
    val interestedCalls = myCalls.count { it.statusOutcome == CallStatusOutcome.INTERESTED }
    val didntPickUpCalls = myCalls.count { it.statusOutcome == CallStatusOutcome.DIDNT_PICK_UP }
    val maybeCalls = myCalls.count { it.statusOutcome == CallStatusOutcome.MAYBE_INTERESTED }
    val notInterestedCalls = myCalls.count { it.statusOutcome == CallStatusOutcome.NOT_INTERESTED }

    val conversionRate = if (totalCalls > 0) (interestedCalls.toFloat() / totalCalls * 100) else 0f

    val primaryColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF38BDF8)
    val cardBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val cardBorder = if (employee.isPinkPrincess) Color(0xFF4A204B) else Color(0xFF334155)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (employee.isPinkPrincess) "My Performance Stats ✨" else "My Call Statistics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Call Performance Breakdown", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))

                    StatLine("Total Assigned Clients", "${myClients.size}")
                    StatLine("Total Call Actions", "$totalCalls")
                    StatLine("Interested Conversion Rate", String.format("%.1f%%", conversionRate), Color(0xFF10B981))

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = cardBorder)

                    StatLine("Didn't Pick Up Calls", "$didntPickUpCalls", Color(0xFFF59E0B))
                    StatLine("Maybe Interested Calls", "$maybeCalls", Color(0xFF38BDF8))
                    StatLine("Interested Conversion Calls", "$interestedCalls", Color(0xFF10B981))
                    StatLine("Not Interested Calls", "$notInterestedCalls", Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String, valColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valColor)
    }
}
