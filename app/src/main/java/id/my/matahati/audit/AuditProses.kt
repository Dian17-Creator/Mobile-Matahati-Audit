package id.my.matahati.audit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import id.my.matahati.audit.data.*
import id.my.matahati.audit.data.viewmodel.AuditExecutionViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme
import id.my.matahati.audit.component.verticalScrollbar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AuditProses : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auditId = intent.getIntExtra("audit_id", -1)
        setContent {
            matahati_AuditTheme {
                AuditExecutionScreen(
                    auditId = auditId,
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditExecutionScreen(
    auditId: Int = -1,
    viewModel: AuditExecutionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = remember { sessionManager.getUser()?.nid ?: -1 }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.initialize(auditId)
    }

    val primaryColor = Color(0xFFB63352)
    val backColor = Color(0xFFF8F9FB)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Auto-scroll logic
    LaunchedEffect(uiState.highlightedQuestionId) {
        uiState.highlightedQuestionId?.let { id ->
            val categories = uiState.auditDetail?.categories ?: emptyList()
            var totalIndex = 1 // Offset for Header
            var found = false
            
            for (category in categories) {
                totalIndex++ // Category Header
                val qIndex = category.questions.indexOfFirst { it.id == id }
                if (qIndex != -1) {
                    totalIndex += qIndex
                    found = true
                    break
                }
                if (uiState.expandedCategoryIds.contains(category.id)) {
                    totalIndex += category.questions.size
                }
            }
            
            if (found) {
                coroutineScope.launch {
                    listState.animateScrollToItem(totalIndex)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Audit Proses",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
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
            if (uiState.auditDetail == null) {
                StartAuditSection(
                    departments = uiState.departments,
                    selectedDepartment = uiState.selectedDepartment,
                    existingDraftId = uiState.existingDraftId,
                    isLoading = uiState.isLoading,
                    onSelect = { viewModel.selectDepartment(it) },
                    onStart = { viewModel.startAudit(userId) }
                )
            } else {
                AuditExecutionContent(
                    uiState = uiState,
                    listState = listState,
                    viewModel = viewModel
                )
            }
        }

        if (uiState.isLoading && uiState.auditDetail == null) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp), color = primaryColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartAuditSection(
    departments: List<DepartmentData>,
    selectedDepartment: DepartmentData?,
    existingDraftId: Int?,
    isLoading: Boolean,
    onSelect: (DepartmentData) -> Unit,
    onStart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val brandColor = Color(0xFFB63352)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Assignment,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Mulai Audit Baru",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Pilih departemen untuk memulai proses audit.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedDepartment?.name ?: "Pilih Departemen",
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
                        departments.forEach { department ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = department.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                },
                                onClick = {
                                    onSelect(department)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedDepartment != null && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (existingDraftId != null) Color(0xFF2196F3) else Color(0xFFB63352)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(if (existingDraftId != null) "Lanjutkan Audit" else "Mulai Audit", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditExecutionContent(
    uiState: id.my.matahati.audit.data.viewmodel.AuditExecutionUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    viewModel: AuditExecutionViewModel
) {
    val container = uiState.auditDetail ?: return
    val audit = container.audit
    val isReadOnly = audit.status == "Submitted"
    val isAnyDialogOpen = uiState.isUploading || uiState.isSubmitting
    
    var showSubmitDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<AuditPhotoDetail?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (isAnyDialogOpen || selectedPhoto != null || showSubmitDialog) 16.dp else 0.dp)
        ) {
            // Audit Header
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Departemen", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = audit.departmentName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        StatusChip(status = audit.status, isSolid = true)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auditor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = audit.auditorName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "No. Dokumen", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = audit.documentId, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    
                    if (!isReadOnly) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showSubmitDialog = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selesaikan Audit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                var questionGlobalIndex = 1
                container.categories.forEach { category ->
                    item(key = "cat_${category.id}") {
                        CategoryHeader(
                            category = category,
                            isExpanded = uiState.expandedCategoryIds.contains(category.id),
                            onToggle = { viewModel.toggleCategory(category.id) }
                        )
                    }

                    if (uiState.expandedCategoryIds.contains(category.id)) {
                        category.questions.forEach { question ->
                            val currentIdx = questionGlobalIndex++
                            item(key = "q_${question.id}") {
                                QuestionExecutionCard(
                                    question = question,
                                    displayIndex = currentIdx,
                                    isHighlighted = uiState.highlightedQuestionId == question.id,
                                    isReadOnly = isReadOnly,
                                    onAnswerChanged = { score, notes -> viewModel.onAnswerChanged(question.id, score, notes) },
                                    onUploadPhoto = { file -> 
                                        question.response?.id?.let { respId ->
                                            viewModel.uploadPhoto(respId, file)
                                        }
                                    },
                                    onPhotoClick = { selectedPhoto = it }
                                )
                            }
                        }
                    } else {
                        // Increment index even if collapsed to maintain consistent numbering
                        questionGlobalIndex += category.questions.size
                    }
                }
            }
        }

        if (uiState.isUploading || uiState.isSaving) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = if (uiState.isUploading) "Mengunggah Foto..." else "Menyimpan...", color = Color.White)
                }
            }
        }
    }

    if (showSubmitDialog) {
        SubmitAuditDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { name, photo -> 
                viewModel.submitAudit(name, photo)
                showSubmitDialog = false
            }
        )
    }

    selectedPhoto?.let { photo ->
        PhotoDetailDialog(
            photo = photo,
            isReadOnly = isReadOnly,
            onDismiss = { selectedPhoto = null },
            onSave = { obs, rec -> 
                viewModel.updatePhotoDetail(photo.id, obs, rec)
                selectedPhoto = null
            },
            onDelete = {
                viewModel.deletePhoto(photo.id)
                selectedPhoto = null
            }
        )
    }
}

@Composable
fun StatusChip(status: String, isSolid: Boolean = false) {
    val color = when (status) {
        "Submitted" -> Color(0xFF4CAF50)
        "Draft" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Surface(
        color = if (isSolid) color else color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = if (isSolid) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (isSolid) Color.White else color
        )
    }
}

@Composable
fun CategoryHeader(
    category: AuditCategoryDetail,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        color = Color(0xFFB63352).copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFB63352)
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color(0xFFB63352)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionExecutionCard(
    question: AuditQuestionDetail,
    displayIndex: Int,
    isHighlighted: Boolean,
    isReadOnly: Boolean,
    onAnswerChanged: (String?, String?) -> Unit,
    onUploadPhoto: (File) -> Unit,
    onPhotoClick: (AuditPhotoDetail) -> Unit
) {
    val context = LocalContext.current
    val currentResponse = question.response
    var notes by remember(question.id) { mutableStateOf(currentResponse?.remark ?: "") }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Sync notes if changed externally (e.g. from autosave response or other updates)
    LaunchedEffect(currentResponse?.remark) {
        if (currentResponse?.remark != notes) {
            notes = currentResponse?.remark ?: ""
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) onUploadPhoto(file)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri?.let { uri ->
                val file = uriToFile(context, uri)
                if (file != null) onUploadPhoto(file)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Ambil Foto") },
            text = { Text("Pilih sumber foto temuan.") },
            confirmButton = {
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val file = File(context.cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showImageSourceDialog = false
                }) {
                    Text("Kamera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showImageSourceDialog = false
                }) {
                    Text("Galeri")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFFFFF9C4) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = Color(0xFFB83257)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = displayIndex.toString().padStart(2, '0'),
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Question Text
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                // Score Selection
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val scores = listOf("N/A", "0", "0.5", "1", "1.5", "2")
                    scores.forEach { score ->
                        // Normalize comparison for decimal strings (e.g. "1.0" should match "1")
                        val isSelected = remember(currentResponse?.score, score) {
                            val respScore = currentResponse?.score
                            if (respScore == null || score == "N/A") {
                                respScore == score
                            } else {
                                try {
                                    respScore.toDouble() == score.toDouble()
                                } catch (e: Exception) {
                                    respScore == score
                                }
                            }
                        }
                        
                        ScoreChip(
                            score = score,
                            isSelected = isSelected,
                            enabled = !isReadOnly,
                            onClick = { onAnswerChanged(score, notes) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { 
                        notes = it
                        onAnswerChanged(currentResponse?.score, it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan / Temuan") },
                    placeholder = { Text("Tuliskan detail temuan di sini...") },
                    enabled = !isReadOnly,
                    shape = RoundedCornerShape(12.dp)
                )

                // Photo Grid
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Foto Temuan (${question.photos.size}/10)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    
                    if (question.photos.size < 10 && !isReadOnly) {
                        IconButton(onClick = { showImageSourceDialog = true }) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color(0xFFB63352))
                        }
                    }
                }
                
//                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.photos.forEach { photo ->
                        AsyncImage(
                            model = photo.photoPath,
                            contentDescription = null,
                            modifier = Modifier
                                .size(53.5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray)
                                .clickable { onPhotoClick(photo) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreChip(score: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val color = when (score) {
        "N/A" -> Color(0xFF9E9E9E)
        "0" -> Color(0xFFF44336)
        "0.5" -> Color(0xFFFF9800)
        "1" -> Color(0xFFFFEB3B)
        "1.5" -> Color(0xFF2196F3)
        "2" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(score, fontWeight = FontWeight.Bold) },
        enabled = enabled,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color,
            selectedLabelColor = if (score == "1") Color.Black else Color.White,
            containerColor = color.copy(alpha = 0.1f),
            labelColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = isSelected,
            borderColor = color.copy(alpha = 0.5f),
            selectedBorderColor = color
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailDialog(
    photo: AuditPhotoDetail,
    isReadOnly: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var observation by remember { mutableStateOf(photo.remark ?: "") }
    var recommendation by remember { mutableStateOf(photo.action ?: "") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Detail Foto Temuan", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } },
                    actions = {
                        if (!isReadOnly) {
                            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                        }
                    }
                )
            },
            bottomBar = {
                if (!isReadOnly) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp
                    ) {
                        Button(
                            onClick = { onSave(observation, recommendation) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                        ) {
                            Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                AsyncImage(
                    model = photo.photoPath,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                val brandColor = Color(0xFFB63352)
                
                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Hasil Pengamatan") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isReadOnly,
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandColor,
                        focusedLabelColor = brandColor,
                        cursorColor = brandColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = recommendation,
                    onValueChange = { recommendation = it },
                    label = { Text("Rekomendasi") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isReadOnly,
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandColor,
                        focusedLabelColor = brandColor,
                        cursorColor = brandColor
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SubmitAuditDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, File) -> Unit
) {
    var auditeeName by remember { mutableStateOf("") }
    val context = LocalContext.current
    var photoFile by remember { mutableStateOf<File?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { photoFile = uriToFile(context, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraUri?.let { uri ->
                photoFile = uriToFile(context, uri)
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "verify_image_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk mengambil foto verifikasi.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Pilih Foto Verifikasi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            text = { Text("Ambil foto dari kamera atau pilih dari galeri untuk melengkapi verifikasi audit.") },
            confirmButton = {
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val file = File(context.cacheDir, "verify_image_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showSourceDialog = false
                }) {
                    Text("Kamera", color = Color(0xFFB63352), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    galleryLauncher.launch("image/*")
                    showSourceDialog = false
                }) {
                    Text("Galeri", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Verifikasi & Selesaikan",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.Black
                )
                
                Text(
                    text = "Lengkapi data perwakilan dan foto verifikasi sebelum menyelesaikan audit ini.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                val brandColor = Color(0xFFB63352)

                OutlinedTextField(
                    value = auditeeName,
                    onValueChange = { auditeeName = it },
                    label = { Text("Nama Perwakilan") },
                    placeholder = { Text("Nama PIC Departemen") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandColor,
                        focusedLabelColor = brandColor,
                        cursorColor = brandColor
                    ),
                    singleLine = true
                )
                
                if (photoFile != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = photoFile,
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(32.dp)
                                .clickable { photoFile = null },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { showSourceDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        color = brandColor.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.2f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = brandColor,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ambil Foto Verifikasi",
                                style = MaterialTheme.typography.labelLarge,
                                color = brandColor
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Batal", color = Color.Gray)
                    }

                    Button(
                        onClick = { photoFile?.let { onSubmit(auditeeName, it) } },
                        enabled = auditeeName.isNotBlank() && photoFile != null && !isSubmitting,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Selesaikan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Utility to convert Uri to File, fix orientation, and compress
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        
        // Read Exif orientation
        val exifInputStream = context.contentResolver.openInputStream(uri)
        val orientation = exifInputStream?.use {
            val exif = ExifInterface(it)
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Rotate bitmap if needed
        val rotatedBitmap = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }

        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.flush()
        outputStream.close()
        
        if (rotatedBitmap != bitmap) bitmap.recycle()
        
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = android.graphics.Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}
