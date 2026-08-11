package id.my.matahati.audit

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.viewmodel.StockOpnameHasilViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme
import java.text.SimpleDateFormat
import java.util.*

class StockOpnameHasilActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            matahati_AuditTheme {
                StockOpnameHasilScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockOpnameHasilScreen(
    viewModel: StockOpnameHasilViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = remember { sessionManager.getUser()?.id ?: -1 }
    
    val primaryColor = Color(0xFFB63352)
    val backColor = Color(0xFFF8F9FB)

    LaunchedEffect(Unit) {
        viewModel.fetchOpnames(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Stok Opname", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = backColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter Section
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Department Selector
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedDepartment?.name ?: "Pilih Departemen",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Departemen") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            uiState.departments.forEach { dept ->
                                DropdownMenuItem(
                                    text = { Text(dept.name) },
                                    onClick = {
                                        viewModel.selectDepartment(dept, userId)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StockDatePickerField(
                            label = "Dari Tanggal",
                            value = uiState.dateFrom,
                            modifier = Modifier.weight(1f),
                            onDateSelected = { viewModel.updateDates(it, uiState.dateTo, userId) }
                        )
                        StockDatePickerField(
                            label = "Sampai Tanggal",
                            value = uiState.dateTo,
                            modifier = Modifier.weight(1f),
                            onDateSelected = { viewModel.updateDates(uiState.dateFrom, it, userId) }
                        )
                    }
                }
            }

            // Results List
            Text(
                text = "Dokumen Stok Opname",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isLoading && uiState.opnames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else if (uiState.selectedDepartment == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Business, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Silakan pilih departemen terlebih dahulu", color = Color.Gray)
                    }
                }
            } else if (uiState.opnames.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data stok opname untuk departemen ini.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.opnames, key = { it.id }) { opname ->
                        StockOpnameDocumentItem(
                            opname = opname,
                            onClick = { viewModel.fetchOpnameDetail(opname.id, userId) }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    uiState.selectedOpnameDetail?.let { detail ->
        StockOpnameReportDetailDialog(
            detail = detail,
            userId = userId,
            onDismiss = { viewModel.clearDetail() }
        )
    }

    // Error Snackbar
    uiState.errorMessage?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
}

@Composable
fun StockOpnameDocumentItem(
    opname: StockOpnameHistoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFB63352).copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFFB63352),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = opname.submittedAt ?: opname.startedAt ?: "-",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = opname.documentId ?: "-",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusChip(status = opname.status ?: "Draft", isSolid = true)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun StockDatePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    if (value.isNotEmpty()) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(value)?.let { calendar.time = it }
        } catch (_: Exception) {}
    }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) },
        modifier = modifier.clickable {
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    val cal = Calendar.getInstance()
                    cal.set(y, m, d)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    onDateSelected(sdf.format(cal.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StockOpnameReportDetailDialog(
    detail: StockOpnameDetailData,
    userId: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val header = detail.header
    val primaryColor = Color(0xFFB63352)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(header.documentId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } },
                    actions = {
                        IconButton(onClick = {
                            val url = "https://audit-api.matahaticafe.com/api/stock/opname/${header.id}/export-pdf?nid_auditor=${userId}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.Print, contentDescription = "Download PDF")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = primaryColor,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).background(Color(0xFFF8F9FB)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                InfoItem(modifier = Modifier.weight(1f), icon = Icons.Default.Person, label = "Auditor", value = header.auditorName)
                                InfoItem(modifier = Modifier.weight(1f), icon = Icons.Default.CalendarToday, label = "Tanggal", value = header.auditDate ?: "-")
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                InfoItem(modifier = Modifier.weight(1f), icon = Icons.Default.Business, label = "Departemen", value = header.departmentName)
                                InfoItem(modifier = Modifier.weight(1f), icon = Icons.Default.Info, label = "Status", value = header.status)
                            }
                        }
                    }
                }

                if (header.status == "Submitted") {
                    item {
                        Text(text = "VERIFIKASI", style = MaterialTheme.typography.labelMedium, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Auditor Card
                            Card(
                                modifier = Modifier.weight(1f).height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("Auditor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(header.auditorName, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            // Photo Card
                            Card(
                                modifier = Modifier.weight(1.2f).height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                AsyncImage(
                                    model = header.verificationPhoto,
                                    contentDescription = "Verification",
                                    modifier = Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Auditee Card
                            Card(
                                modifier = Modifier.weight(1f).height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("Auditee / PIC", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(header.auditeeName ?: "-", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                items(detail.categories) { category ->
                    Text(text = category.name, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleSmall, color = primaryColor, fontWeight = FontWeight.Bold)
                    category.items.forEachIndexed { index, item ->
                        ResultOpnameItemCard(item = item, index = index + 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultOpnameItemCard(item: StockOpnameItem, index: Int) {
    val resp = item.response
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(text = "$index.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recorded", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(resp?.qtyStock ?: "0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Actual", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(resp?.qtyReal ?: "0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Selisih", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    val diff = resp?.diff?.toDoubleOrNull() ?: 0.0
                    Text(resp?.diff ?: "0", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (diff < 0) Color.Red else if (diff > 0) Color(0xFF4CAF50) else Color.Gray)
                }
            }

            if (resp?.remark?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = resp.remark, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FB), RoundedCornerShape(8.dp)).padding(8.dp))
            }

            if (item.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.photos.forEach { photo ->
                        AsyncImage(model = photo.photoPath, contentDescription = null, modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    }
                }
            }
        }
    }
}
