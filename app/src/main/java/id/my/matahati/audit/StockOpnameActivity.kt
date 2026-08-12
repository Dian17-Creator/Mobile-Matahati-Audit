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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import id.my.matahati.audit.data.viewmodel.StockOpnameUiState
import id.my.matahati.audit.data.viewmodel.StockOpnameViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class StockOpnameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val auditId = intent.getIntExtra("audit_id", -1)
        setContent {
            matahati_AuditTheme {
                StockOpnameExecutionScreen(
                    auditId = auditId,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockOpnameExecutionScreen(
    auditId: Int = -1,
    viewModel: StockOpnameViewModel = viewModel(),
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
        viewModel.initialize(auditId, userId)
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

    // Auto-scroll logic for incomplete items
    LaunchedEffect(uiState.highlightedItemId) {
        uiState.highlightedItemId?.let { id ->
            val categories = uiState.opnameDetail?.categories ?: emptyList()
            
            var currentIndex = 0
            for (category in categories) {
                currentIndex++ // Category Header
                val itemIndex = category.items.indexOfFirst { it.id == id }
                if (itemIndex != -1) {
                    val totalIndex = currentIndex + itemIndex
                    coroutineScope.launch {
                        listState.animateScrollToItem(totalIndex)
                    }
                    break
                }
                if (uiState.expandedCategoryIds.contains(category.id)) {
                    currentIndex += category.items.size
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Stok Opname",
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
                if (uiState.opnameDetail == null) {
                    StartOpnameSection(
                        departments = uiState.departments,
                        selectedDepartment = uiState.selectedDepartment,
                        isLoading = uiState.isLoading,
                        onSelect = { viewModel.selectDepartment(it) },
                        onStart = { viewModel.startOpname(userId) }
                    )
                } else {
                    OpnameExecutionContent(
                        uiState = uiState,
                        listState = listState,
                        viewModel = viewModel,
                        userId = userId
                    )
                }
            }

            if (uiState.isLoading && uiState.opnameDetail == null) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp), color = primaryColor)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartOpnameSection(
    departments: List<DepartmentData>,
    selectedDepartment: DepartmentData?,
    isLoading: Boolean,
    onSelect: (DepartmentData) -> Unit,
    onStart: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
            text = "Mulai Stok Opname",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Pilih departemen untuk memulai proses stok opname.",
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
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    departments.forEach { department ->
                        DropdownMenuItem(
                            text = { Text(department.name) },
                            onClick = {
                                onSelect(department)
                                expanded = false
                            }
                        )
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text("Mulai Stok Opname", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OpnameExecutionContent(
    uiState: StockOpnameUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    viewModel: StockOpnameViewModel,
    userId: Int
) {
    val container = uiState.opnameDetail ?: return
    val header = container.header
    val isReadOnly = header.status == "Submitted"
    val isAnyDialogOpen = uiState.isUploading || uiState.isSubmitting
    
    var showSubmitDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<StockOpnamePhoto?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (isAnyDialogOpen || selectedPhoto != null || showSubmitDialog) 16.dp else 0.dp)
        ) {
            // Header Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = "Departemen", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = header.departmentName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        StatusChip(status = header.status, isSolid = true)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auditor", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = header.auditorName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "No. Dokumen", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = header.documentId, style = MaterialTheme.typography.bodyMedium)
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
                            Text("Selesaikan Stok Opname", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                var itemGlobalIndex = 1
                container.categories.forEach { category ->
                    item(key = "cat_${category.id}") {
                        OpnameCategoryHeader(
                            name = category.name,
                            isExpanded = uiState.expandedCategoryIds.contains(category.id),
                            onToggle = { viewModel.toggleCategory(category.id) }
                        )
                    }

                    if (uiState.expandedCategoryIds.contains(category.id)) {
                        category.items.forEach { opnameItem ->
                            val currentIdx = itemGlobalIndex++
                            item(key = "item_${opnameItem.id}") {
                                StockOpnameItemCard(
                                    item = opnameItem,
                                    displayIndex = currentIdx,
                                    isHighlighted = uiState.highlightedItemId == opnameItem.id,
                                    isReadOnly = isReadOnly,
                                    onChanged = { qtyStock, qtyReal, notes -> 
                                        viewModel.onItemChanged(opnameItem.id, qtyStock, qtyReal, notes) 
                                    },
                                    onUploadPhoto = { file -> 
                                        opnameItem.response?.id?.let { respId ->
                                            viewModel.uploadPhoto(respId, file, null, userId)
                                        }
                                    },
                                    onPhotoClick = { selectedPhoto = it }
                                )
                            }
                        }
                    } else {
                        itemGlobalIndex += category.items.size
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
        SubmitOpnameDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { name, photo -> 
                viewModel.submitOpname(name, photo, userId)
                showSubmitDialog = false
            }
        )
    }

    selectedPhoto?.let { photo ->
        OpnamePhotoDetailDialog(
            photo = photo,
            isReadOnly = isReadOnly,
            onDismiss = { selectedPhoto = null },
            onSave = { remark -> 
                viewModel.updatePhotoRemark(photo.id, remark, userId)
                selectedPhoto = null
            },
            onDelete = {
                viewModel.deletePhoto(photo.id, userId)
                selectedPhoto = null
            }
        )
    }
}

@Composable
fun OpnameCategoryHeader(
    name: String,
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
                text = name,
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
fun StockOpnameItemCard(
    item: StockOpnameItem,
    displayIndex: Int,
    isHighlighted: Boolean,
    isReadOnly: Boolean,
    onChanged: (String?, String?, String?) -> Unit,
    onUploadPhoto: (File) -> Unit,
    onPhotoClick: (StockOpnamePhoto) -> Unit
) {
    val context = LocalContext.current
    val currentResponse = item.response
    
    var qtyStock by remember(item.id) { mutableStateOf(currentResponse?.qtyStock?.toString() ?: "") }
    var qtyReal by remember(item.id) { mutableStateOf(currentResponse?.qtyReal?.toString() ?: "") }
    var notes by remember(item.id) { mutableStateOf(currentResponse?.remark ?: "") }
    
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(currentResponse?.qtyStock, currentResponse?.qtyReal, currentResponse?.remark) {
        val serverQtyStock = currentResponse?.qtyStock?.toString() ?: ""
        val serverQtyReal = currentResponse?.qtyReal?.toString() ?: ""
        if (serverQtyStock != qtyStock) qtyStock = serverQtyStock
        if (serverQtyReal != qtyReal) qtyReal = serverQtyReal
        if (currentResponse?.remark != notes) notes = currentResponse?.remark ?: ""
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uriToFile(context, it)?.let(onUploadPhoto) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) { cameraImageUri?.let { uriToFile(context, it)?.let(onUploadPhoto) } }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "stock_img_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Ambil Foto Barang") },
            text = { Text("Pilih sumber foto.") },
            confirmButton = {
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val file = File(context.cacheDir, "stock_img_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                    showImageSourceDialog = false
                }) { Text("Kamera") }
            },
            dismissButton = {
                TextButton(onClick = { galleryLauncher.launch("image/*"); showImageSourceDialog = false }) { Text("Galeri") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp).animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isHighlighted) Color(0xFFFFF9C4) else Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = Color(0xFFB83257)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = displayIndex.toString().padStart(2, '0'), style = androidx.compose.ui.text.TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = qtyStock,
                        onValueChange = { qtyStock = it; onChanged(it, qtyReal, notes) },
                        modifier = Modifier.weight(1f),
                        label = { Text(" recorded", fontSize = 10.sp) },
                        enabled = !isReadOnly,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = qtyReal,
                        onValueChange = { qtyReal = it; onChanged(qtyStock, it, notes) },
                        modifier = Modifier.weight(1f),
                        label = { Text(" actual", fontSize = 10.sp) },
                        enabled = !isReadOnly,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (currentResponse?.diff != null) {
                    val diff = currentResponse.diff ?: 0.0
                    val diffColor = if (diff < 0) Color.Red else if (diff > 0) Color(0xFF4CAF50) else Color.Gray
                    Text(
                        text = "Selisih: ${currentResponse.diff}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = diffColor
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it; onChanged(qtyStock, qtyReal, it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan") },
                    enabled = !isReadOnly,
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Foto (${item.photos.size}/5)", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    if (item.photos.size < 5 && !isReadOnly) {
                        IconButton(onClick = { showImageSourceDialog = true }) {
                            Icon(Icons.Default.AddAPhoto, null, tint = Color(0xFFB63352))
                        }
                    }
                }

                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item.photos.forEach { photo ->
                        AsyncImage(
                            model = photo.photoPath,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray).clickable { onPhotoClick(photo) },
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
fun OpnamePhotoDetailDialog(
    photo: StockOpnamePhoto,
    isReadOnly: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var remark by remember { mutableStateOf(photo.remark ?: "") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Detail Foto Barang", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) } },
                    actions = { if (!isReadOnly) { IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) } } }
                )
            },
            bottomBar = {
                if (!isReadOnly) {
                    Surface(modifier = Modifier.fillMaxWidth(), tonalElevation = 8.dp, shadowElevation = 8.dp) {
                        Button(
                            onClick = { onSave(remark) },
                            modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                        ) { Text("Simpan Perubahan", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(24.dp))
                AsyncImage(model = photo.photoPath, contentDescription = null, modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("Keterangan") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isReadOnly,
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SubmitOpnameDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, File) -> Unit
) {
    var auditeeName by remember { mutableStateOf("") }
    val context = LocalContext.current
    var photoFile by remember { mutableStateOf<File?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { photoFile = uriToFile(context, it) } }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success -> if (success) cameraUri?.let { photoFile = uriToFile(context, it) } }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val file = File(context.cacheDir, "verify_stock_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Pilih Foto Verifikasi", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
            confirmButton = {
                TextButton(onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val file = File(context.cacheDir, "verify_stock_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        cameraUri = uri
                        cameraLauncher.launch(uri)
                    } else { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                    showSourceDialog = false
                }) { Text("Kamera", color = Color(0xFFB63352), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { galleryLauncher.launch("image/*"); showSourceDialog = false }) { Text("Galeri", color = Color.Gray) }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = Color.White, tonalElevation = 8.dp) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(text = "Verifikasi & Selesaikan", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold))
                Text(text = "Lengkapi data perwakilan dan foto verifikasi.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, textAlign = TextAlign.Center)
                OutlinedTextField(value = auditeeName, onValueChange = { auditeeName = it }, label = { Text("Nama Perwakilan") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                
                if (photoFile != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))) {
                        AsyncImage(model = photoFile, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(32.dp).clickable { photoFile = null }, shape = CircleShape, color = Color.Black.copy(alpha = 0.5f)) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(6.dp))
                        }
                    }
                } else {
                    Surface(modifier = Modifier.fillMaxWidth().height(120.dp).clickable { showSourceDialog = true }, shape = RoundedCornerShape(16.dp), color = Color(0xFFB63352).copy(alpha = 0.05f), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.2f))) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.AddAPhoto, null, tint = Color(0xFFB63352), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Ambil Foto Verifikasi", color = Color(0xFFB63352))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp)) { Text("Batal", color = Color.Gray) }
                    Button(onClick = { photoFile?.let { onSubmit(auditeeName, it) } }, enabled = auditeeName.isNotBlank() && photoFile != null && !isSubmitting, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                        if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else Text("Selesaikan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Remove redundant uriToFile as it exists in AuditProses.kt in the same package
