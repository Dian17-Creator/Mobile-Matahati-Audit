package id.my.matahati.audit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.audit.data.DepartmentData
import id.my.matahati.audit.data.MappingCategory
import id.my.matahati.audit.data.viewmodel.AuditDepartmentViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme
import id.my.matahati.audit.component.verticalScrollbar

class AuditDepartemen : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            matahati_AuditTheme {
                AuditDepartemenScreen(
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
fun AuditDepartemenScreen(
    viewModel: AuditDepartmentViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val primaryColor = Color(0xFFB63352)
    val backColor = MaterialTheme.colorScheme.background

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

    // Calculate stats
    val totalPertanyaan = remember(uiState.categories) {
        uiState.categories.sumOf { it.questions.size }
    }
    val totalDipilih = uiState.selectedQuestionIds.size

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pemetaan Departemen",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (uiState.selectedDepartment != null) {
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                ) {
                    Box(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Button(
                            onClick = { viewModel.saveMapping() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Simpan Pemetaan", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        containerColor = backColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Department Selector
            DepartmentSelector(
                departments = uiState.departments,
                selectedDepartment = uiState.selectedDepartment,
                onSelect = { viewModel.selectDepartment(it) }
            )

            if (uiState.selectedDepartment == null) {
                EmptyMappingState()
            } else {
                // Bulk Actions
                BulkActionsBar(
                    onSelectAll = { viewModel.toggleAll(true) },
                    onClearAll = { viewModel.toggleAll(false) }
                )

                // Selection Counter
                Text(
                    text = "$totalDipilih dari $totalPertanyaan pertanyaan dipilih",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 12.dp, top = 6.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.categories) { category ->
                        CategoryMappingCard(
                            category = category,
                            selectedIds = uiState.selectedQuestionIds,
                            onToggleQuestion = { viewModel.toggleQuestion(it) },
                            onToggleCategory = { select -> viewModel.toggleCategory(category.id, select) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(0.dp))
                    }
                }
            }
        }

        if (uiState.isLoading && !uiState.isSaving) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentSelector(
    departments: List<DepartmentData>,
    selectedDepartment: DepartmentData?,
    onSelect: (DepartmentData) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val brandColor = Color(0xFFB63352)

    Box(modifier = Modifier.padding(16.dp)) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
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
}

@Composable
fun BulkActionsBar(onSelectAll: () -> Unit, onClearAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSelectAll,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(painterResource(id = R.drawable.selectall), contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Pilih Semua", fontSize = 12.sp)
        }
        OutlinedButton(
            onClick = onClearAll,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(painterResource(id = R.drawable.trash), contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hapus Semua", fontSize = 12.sp)
        }
    }
}

@Composable
fun CategoryMappingCard(
    category: MappingCategory,
    selectedIds: Set<Int>,
    onToggleQuestion: (Int) -> Unit,
    onToggleCategory: (Boolean) -> Unit
) {
    val allSelected = category.questions.all { selectedIds.contains(it.id) }
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFB63352)
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onToggleCategory(true) }) {
                        Text("Pilih", fontSize = 12.sp)
                    }
                    TextButton(onClick = { onToggleCategory(false) }) {
                        Text("Hapus", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
            
            category.questions.forEach { question ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleQuestion(question.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedIds.contains(question.id),
                        onCheckedChange = { onToggleQuestion(question.id) }
                    )
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMappingState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.BusinessCenter,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Pilih departemen untuk memulai pemetaan",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
