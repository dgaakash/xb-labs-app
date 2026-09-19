package com.xblabs.app.ui.admin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.CallRecord
import com.xblabs.app.data.models.CallStatusOutcome
import com.xblabs.app.data.models.Client

@Composable
fun AdminAnalyticsScreen(
    clients: List<Client>,
    callRecords: List<CallRecord>
) {
    val context = LocalContext.current
    val totalClients = clients.size
    val totalCalls = callRecords.size
    val contactedClientsCount = clients.count { it.lastContactedAt != null }

    val contactRate = if (totalClients > 0) (contactedClientsCount.toFloat() / totalClients * 100) else 0f

    val didntPickUpCount = callRecords.count { it.statusOutcome == CallStatusOutcome.DIDNT_PICK_UP }
    val maybeCount = callRecords.count { it.statusOutcome == CallStatusOutcome.MAYBE_INTERESTED }
    val interestedCount = callRecords.count { it.statusOutcome == CallStatusOutcome.INTERESTED }
    val notInterestedCount = callRecords.count { it.statusOutcome == CallStatusOutcome.NOT_INTERESTED }

    val didntPickUpPct = if (totalCalls > 0) (didntPickUpCount.toFloat() / totalCalls * 100) else 0f
    val maybePct = if (totalCalls > 0) (maybeCount.toFloat() / totalCalls * 100) else 0f
    val interestedPct = if (totalCalls > 0) (interestedCount.toFloat() / totalCalls * 100) else 0f
    val notInterestedPct = if (totalCalls > 0) (notInterestedCount.toFloat() / totalCalls * 100) else 0f

    fun exportDataAsJson() {
        val jsonStringBuilder = StringBuilder()
        jsonStringBuilder.append("[\n")
        clients.forEachIndexed { idx, c ->
            jsonStringBuilder.append("  {\n")
            jsonStringBuilder.append("    \"id\": \"${c.id}\",\n")
            jsonStringBuilder.append("    \"businessName\": \"${c.businessName}\",\n")
            jsonStringBuilder.append("    \"phone\": \"${c.phone}\",\n")
            jsonStringBuilder.append("    \"assignedTo\": \"${c.assignedEmployeeName ?: ""}\",\n")
            jsonStringBuilder.append("    \"status\": \"${c.currentStatus.name}\"\n")
            jsonStringBuilder.append("  }${if (idx < clients.size - 1) "," else ""}\n")
        }
        jsonStringBuilder.append("]")

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("XB Labs Client Data Export", jsonStringBuilder.toString()))
        Toast.makeText(context, "Exported ${clients.size} records to Clipboard (JSON)", Toast.LENGTH_LONG).show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analytics & Export", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Button(
                    onClick = { exportDataAsJson() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Data (JSON)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Sales Conversion Analytics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(14.dp))

                    StatRow("Total Imported Leads", "$totalClients", Color.White)
                    StatRow("Leads Contacted", "$contactedClientsCount", Color(0xFF38BDF8))
                    StatRow("Contact Coverage Rate", String.format("%.1f%%", contactRate), Color(0xFF38BDF8))
                    StatRow("Total Call Outcomes", "$totalCalls", Color.White)

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                    StatRow("Didn't Pick Up Rate", String.format("%.1f%% (%d calls)", didntPickUpPct, didntPickUpCount), Color(0xFFF59E0B))
                    StatRow("Maybe Interested Rate", String.format("%.1f%% (%d calls)", maybePct, maybeCount), Color(0xFFF59E0B))
                    StatRow("Interested Conversion Rate", String.format("%.1f%% (%d calls)", interestedPct, interestedCount), Color(0xFF10B981))
                    StatRow("Not Interested Rate", String.format("%.1f%% (%d calls)", notInterestedPct, notInterestedCount), Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
