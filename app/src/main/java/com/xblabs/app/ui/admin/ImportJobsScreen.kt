package com.xblabs.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.Client
import com.xblabs.app.data.models.ImportBatch
import com.xblabs.app.data.models.User
import com.xblabs.app.parser.BusinessReportParser
import com.xblabs.app.parser.ImportPreviewResult
import com.xblabs.app.parser.ParsedLeadRecord

@Composable
fun ImportJobsScreen(
    adminUser: User,
    existingClients: List<Client>,
    onImportConfirmed: (List<com.xblabs.app.parser.ParsedLeadRecord>, String) -> ImportBatch
) {
    var rawInputText by remember { mutableStateOf("") }
    var previewResult by remember { mutableStateOf<ImportPreviewResult?>(null) }
    var showPreviewModal by remember { mutableStateOf(false) }
    var lastBatchSuccess by remember { mutableStateOf<ImportBatch?>(null) }

    val sampleReportText = """
Business Name: Sethi's The Cake Shop
Category: Cake Shop
Rating: 4.8
Number of Reviews: 142
Phone: 08882468831
Website: N/A
Address: 2648, Hudson Ln, GTB Nagar, New Delhi, Delhi 110009
Maps URL: https://maps.google.com/?cid=1029384

---

Business Name: Hudson Cafe
Category: Cafe
Rating: 4.6
Number of Reviews: 320
Phone: 09871234567
Website: https://hudsoncafe.in
Address: 2524, Hudson Ln, Delhi 110009
Maps URL: https://maps.google.com/?cid=5647382

---

Business Name: The Yellow Door Bistro
Category: Restaurant
Rating: 4.5
Number of Reviews: 89
Phone: 08882468831
Website: N/A
Address: 12, Vijay Nagar, Delhi
Maps URL: https://maps.google.com/?cid=887766

---

Business Name: Invalid Lead Without Phone
Category: Bakery
Rating: 4.0
Number of Reviews: 10
Phone: N/A
Website: N/A
Address: Delhi
Maps URL: N/A
    """.trimIndent()

    val sampleJsonText = """
[
  {
    "businessName": "Sethi's The Cake Shop",
    "category": "Cake Shop",
    "rating": 4.8,
    "reviewCount": 142,
    "phone": "08882468831",
    "website": "N/A",
    "address": "2648, Hudson Ln, GTB Nagar, New Delhi, Delhi 110009",
    "mapsUrl": "https://maps.google.com/?cid=1029384"
  },
  {
    "businessName": "Hudson Cafe",
    "category": "Cafe",
    "rating": 4.6,
    "reviewCount": 320,
    "phone": "09871234567",
    "website": "https://hudsoncafe.in",
    "address": "2524, Hudson Ln, Delhi 110009",
    "mapsUrl": "https://maps.google.com/?cid=5647382"
  },
  {
    "businessName": "The Yellow Door Bistro",
    "category": "Restaurant",
    "rating": 4.5,
    "reviewCount": 89,
    "phone": "08882468831",
    "website": "N/A",
    "address": "12, Vijay Nagar, Delhi",
    "mapsUrl": "https://maps.google.com/?cid=887766"
  }
]
    """.trimIndent()

    fun runPreview() {
        if (rawInputText.isBlank()) return
        val res = BusinessReportParser.parseInputText(rawInputText, existingClients)
        previewResult = res
        showPreviewModal = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, tint = Color(0xFF38BDF8))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Business Leads", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Row {
                            TextButton(
                                onClick = { rawInputText = sampleReportText }
                            ) {
                                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sample Text", fontSize = 12.sp, color = Color(0xFF38BDF8))
                            }
                            TextButton(
                                onClick = { rawInputText = sampleJsonText }
                            ) {
                                Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sample JSON", fontSize = 12.sp, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Paste business leads below in Key-Value Text Report format or JSON format (array or object). Auto-detects structure & handles duplicates.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = rawInputText,
                        onValueChange = { rawInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        placeholder = { Text("Paste business leads here...", color = Color(0xFF64748B)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { rawInputText = ""; previewResult = null; lastBatchSuccess = null },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Clear")
                        }

                        Button(
                            onClick = { runPreview() },
                            enabled = rawInputText.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Preview Import", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (lastBatchSuccess != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Successful!", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Imported ${lastBatchSuccess!!.importedRecords} clients. Equal distribution summary: ${lastBatchSuccess!!.distributionSummary}",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }

    // Import Preview Dialog
    if (showPreviewModal && previewResult != null) {
        val res = previewResult!!
        AlertDialog(
            onDismissRequest = { showPreviewModal = false },
            title = {
                Text("Import Preview Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Detected", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${res.totalCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Valid", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${res.validCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Duplicates", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${res.duplicateCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Invalid", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("${res.invalidCount}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Records Preview:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(res.records) { rec ->
                            val statusColor = when {
                                !rec.isValid -> Color(0xFFEF4444)
                                rec.isDuplicate -> Color(0xFFF59E0B)
                                else -> Color(0xFF10B981)
                            }
                            val tagText = when {
                                !rec.isValid -> "INVALID (${rec.invalidReason})"
                                rec.isDuplicate -> "DUPLICATE (${rec.duplicateReason})"
                                else -> "WILL IMPORT"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = rec.businessName.ifBlank { "[No Name]" },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(text = tagText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                    }
                                    Text(
                                        text = "Phone: ${rec.phone} • Cat: ${rec.category} • Site: ${rec.displayWebsite}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val batch = onImportConfirmed(res.records, "Text Batch")
                        lastBatchSuccess = batch
                        showPreviewModal = false
                        rawInputText = ""
                        previewResult = null
                    },
                    enabled = res.validCount > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Import ${res.validCount} Valid Jobs", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPreviewModal = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}

private val ParsedLeadRecord.displayWebsite: String
    get() = if (website.isBlank()) "N/A" else website
