package id.my.matahati.audit

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import id.my.matahati.audit.data.viewmodel.AuditHasilViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme
import id.my.matahati.audit.component.verticalScrollbar
import java.text.SimpleDateFormat
import java.util.*

class AuditHasil : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            matahati_AuditTheme {
                AuditHasilScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuditHasilScreen(
    viewModel: AuditHasilViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val primaryColor = Color(0xFFB63352)
    val backColor = MaterialTheme.colorScheme.background

    LaunchedEffect(Unit) {
        viewModel.fetchAudits()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Audit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    val brandColor = Color(0xFFB63352)
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedDepartment?.name ?: "Pilih Departemen",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Departemen") },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.auditdept),
                                    contentDescription = null,
                                    tint = brandColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = brandColor,
                                focusedLabelColor = brandColor,
                                unfocusedLabelColor = Color.Gray,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 250.dp)
                                    .verticalScrollbar(scrollState)
                                    .verticalScroll(scrollState)
                            ) {
                                uiState.departments.forEach { dept ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = dept.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        },
                                        onClick = {
                                            viewModel.selectDepartment(dept)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DatePickerField(
                            label = "Dari Tanggal",
                            value = uiState.dateFrom,
                            modifier = Modifier.weight(1f),
                            onDateSelected = { viewModel.updateDates(it, uiState.dateTo) }
                        )
                        DatePickerField(
                            label = "Sampai Tanggal",
                            value = uiState.dateTo,
                            modifier = Modifier.weight(1f),
                            onDateSelected = { viewModel.updateDates(uiState.dateFrom, it) }
                        )
                    }
                }
            }

            // Results List
            Text(
                text = "Dokumen Audit",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.isLoading && uiState.audits.isEmpty()) {
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
            } else if (uiState.audits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Tidak ada data audit untuk departemen ini.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.audits, key = { it.id }) { audit ->
                        AuditDocumentItem(
                            audit = audit,
                            onClick = { viewModel.fetchAuditDetail(audit.id) },
                            onDelete = { viewModel.deleteAudit(audit.id) }
                        )
                    }
                }
            }
        }
    }

    // Detail Dialog
    uiState.selectedAuditDetail?.let { detail ->
        AuditReportDetailDialog(
            detail = detail,
            viewModel = viewModel,
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

    uiState.emailSuccessMessage?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearEmailSuccess()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditDocumentItem(
    audit: AuditHistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedDate = remember(audit.createdAt) {
        try {
            if (audit.createdAt == null) "-"
            else {
                // Handle ISO 8601 format from backend (e.g. 2026-07-29T13:29:20.000000Z)
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                isoFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = isoFormat.parse(audit.createdAt)
                if (date != null) {
                    val displayFormat = SimpleDateFormat("yyyy-MM-dd | HH:mm", Locale.getDefault())
                    displayFormat.timeZone = TimeZone.getTimeZone("UTC")
                    displayFormat.format(date)
                } else "-"
            }
        } catch (_: Exception) {
            val raw = audit.createdAt ?: return@remember "-"
            if (raw.contains("T")) {
                val datePart = raw.substringBefore("T")
                val timePart = raw.substringAfter("T").take(5)
                "$datePart | $timePart"
            } else {
                raw.take(16).replace(" ", " | ")
            }
        }
    }

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
            // Document Icon Box
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

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = audit.documentId ?: "-",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                StatusChip(status = audit.status ?: "Draft", isSolid = true)
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (audit.status != "Submitted") {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
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
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    // Parse current value to set calendar
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
        enabled = false, // Disable typing, only clickable via modifier
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun SendEmailDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    fun validateEmail(target: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    AlertDialog(
        onDismissRequest = if (isLoading) ({}) else onDismiss,
        title = { Text("Kirim Laporan via Email", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = if (it.isEmpty()) "Email wajib diisi"
                        else if (!validateEmail(it)) "Format email tidak valid"
                        else null
                    },
                    label = { Text("Email Penerima") },
                    placeholder = { Text("contoh@perusahaan.com") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } },
                    enabled = !isLoading,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isEmpty()) {
                        emailError = "Email wajib diisi"
                    } else if (!validateEmail(email)) {
                        emailError = "Format email tidak valid"
                    } else {
                        onSend(email)
                    }
                },
                enabled = !isLoading && emailError == null && email.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Kirim")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Batal")
            }
        },
        shape = RoundedCornerShape(24.dp),
        properties = DialogProperties(dismissOnBackPress = !isLoading, dismissOnClickOutside = !isLoading)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AuditReportDetailDialog(
    detail: AuditDetailContainer,
    viewModel: AuditHasilViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val audit = detail.audit
    val primaryColor = Color(0xFFB63352)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEmailDialog by remember { mutableStateOf(false) }

    if (showEmailDialog) {
        SendEmailDialog(
            isLoading = uiState.isEmailLoading,
            onDismiss = { showEmailDialog = false },
            onSend = { email ->
                viewModel.sendEmail(audit.id, email, null)
            }
        )
    }

    // Auto close email dialog on success
    LaunchedEffect(uiState.emailSuccessMessage) {
        if (uiState.emailSuccessMessage != null) {
            showEmailDialog = false
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(audit.documentId, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } },
                    actions = {
                        IconButton(onClick = { showEmailDialog = true }) {
                            Icon(Icons.Default.Email, contentDescription = "Send Email")
                        }
                        IconButton(onClick = {
                            val url = "https://audit-api.matahaticafe.com/api/audits/${audit.id}/export-pdf"
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
                // Info Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                InfoItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Person,
                                    label = "Auditor",
                                    value = audit.auditorName
                                )
                                InfoItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.CalendarToday,
                                    label = "Tanggal Audit",
                                    value = audit.auditDate ?: "-"
                                )
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                InfoItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Score,
                                    label = "Nilai Total",
                                    value = "${audit.totalScore} / ${audit.maxScore}"
                                )
                                InfoItem(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Percent,
                                    label = "Persentase",
                                    value = "${audit.percentage}%",
                                    valueColor = primaryColor
                                )
                            }
                        }
                    }
                }

                // Verification Section
                if (audit.status == "Submitted") {
                    item {
                        Text(
                            text = "VERIFIKASI AUDIT",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("Auditor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(audit.auditorName, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1.2f).height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                AsyncImage(
                                    model = audit.verificationPhoto,
                                    contentDescription = "Verification",
                                    modifier = Modifier.fillMaxSize().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Card(
                                modifier = Modifier.weight(1f).height(140.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("Auditee / PIC", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(audit.auditeeName ?: "-", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // Questions
                items(detail.categories) { category ->
                    Text(
                        text = category.name,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    category.questions.forEachIndexed { index, question ->
                        ResultQuestionCard(question = question, index = index + 1)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color.Gray
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 18.sp), fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultQuestionCard(question: AuditQuestionDetail, index: Int) {
    val currentResponse = question.response
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text(text = "$index.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                Text(text = question.question, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                
                val score = currentResponse?.score ?: "-"
                Surface(
                    color = getScoreColor(score).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(width = 40.dp, height = 28.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, getScoreColor(score).copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = score, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = getScoreColor(score))
                    }
                }
            }

            if (currentResponse?.remark?.isNotBlank() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FB), RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Icon(Icons.Default.Description, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = currentResponse.remark, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                }
            }

            if (question.photos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    question.photos.forEach { photo ->
                        AsyncImage(
                            model = photo.photoPath,
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

fun getScoreColor(score: String): Color {
    return when (score) {
        "N/A" -> Color(0xFF9E9E9E)
        "0", "0.0" -> Color(0xFFF44336)
        "0.5" -> Color(0xFFFF9800)
        "1", "1.0" -> Color(0xFFFBC02D)
        "1.5" -> Color(0xFF2196F3)
        "2", "2.0" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
}
