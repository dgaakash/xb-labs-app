package com.xblabs.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.ImportBatch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImportHistoryScreen(
    importBatches: List<ImportBatch>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color(0xFF38BDF8))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Import History Batches", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (importBatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No import batches recorded yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(importBatches) { batch ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(batch.sourceName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Text(dateFormat.format(Date(batch.createdAt)), fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Detected", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("${batch.totalRecords}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Imported", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("${batch.importedRecords}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Duplicates", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("${batch.duplicateRecords}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Invalid", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("${batch.invalidRecords}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                }
                            }

                            if (batch.distributionSummary.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Distribution: ${batch.distributionSummary}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            }
                        }
                    }
                }
            }
        }
    }
}
