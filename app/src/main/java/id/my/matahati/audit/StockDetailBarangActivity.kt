package id.my.matahati.audit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.audit.data.StockItemData
import id.my.matahati.audit.data.viewmodel.StockItemViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme

class StockDetailBarangActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val categoryId = intent.getIntExtra("category_id", -1)
        val categoryName = intent.getStringExtra("category_name") ?: "Barang"

        setContent {
            matahati_AuditTheme {
                StockDetailBarangScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailBarangScreen(
    categoryId: Int,
    categoryName: String,
    viewModel: StockItemViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val primaryColor = Color(0xFFB63352)
    val backColor = MaterialTheme.colorScheme.background

    val isAnyDialogOpen = uiState.isAddDialogOpen || uiState.isEditDialogOpen || uiState.isDeleteDialogOpen

    LaunchedEffect(categoryId) {
        if (categoryId != -1) {
            viewModel.fetchItems(categoryId)
        }
    }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .blur(if (isAnyDialogOpen) 16.dp else 0.dp),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Barang - $categoryName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddDialog() },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(100.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Barang") }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backColor)
                    .padding(innerPadding)
            ) {
                if (uiState.items.isEmpty() && !uiState.isLoading) {
                    EmptyBarangState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,
                            end = 8.dp,
                            top = 8.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.items) { index, item ->
                            BarangCard(
                                index = index + 1,
                                item = item,
                                isFirst = index == 0,
                                isLast = index == uiState.items.size - 1,
                                onMoveUp = { viewModel.moveUp(categoryId, index) },
                                onMoveDown = { viewModel.moveDown(categoryId, index) },
                                onEdit = { viewModel.openEditDialog(item) },
                                onDelete = { viewModel.openDeleteDialog(item) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
            }
        }

        // Dialogs
        BarangAnimatedDialog(visible = uiState.isAddDialogOpen, onDismiss = { viewModel.closeAddDialog() }) {
            AddEditBarangDialogContent(
                title = "Tambah Barang",
                onDismiss = { viewModel.closeAddDialog() },
                onConfirm = { name -> viewModel.addItem(categoryId, name) }
            )
        }

        BarangAnimatedDialog(visible = uiState.isEditDialogOpen, onDismiss = { viewModel.closeEditDialog() }) {
            AddEditBarangDialogContent(
                title = "Edit Barang",
                initialName = uiState.selectedItem?.name ?: "",
                onDismiss = { viewModel.closeEditDialog() },
                onConfirm = { name ->
                    // Edit item is not implemented in ViewModel yet based on user instructions (minimal fields)
                    // but we can add it if needed. For now, let's keep it consistent.
                }
            )
        }

        BarangAnimatedDialog(visible = uiState.isDeleteDialogOpen, onDismiss = { viewModel.closeDeleteDialog() }) {
            DeleteBarangConfirmationContent(
                onDismiss = { viewModel.closeDeleteDialog() },
                onConfirm = {
                    uiState.selectedItem?.id?.let { id ->
                        viewModel.deleteItem(categoryId, id)
                    }
                }
            )
        }
    }
}

@Composable
fun BarangCard(
    index: Int,
    item: StockItemData,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Number Badge
            Surface(
                modifier = Modifier.size(28.dp),
                shape = CircleShape,
                color = Color(0xFFB83257)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = index.toString().padStart(2, '0'),
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
                // Item Name
                Text(
                    text = item.name ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 22.sp
                    ),
                    color = Color.DarkGray,
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Move Up
                        IconButton(
                            onClick = onMoveUp,
                            enabled = !isFirst,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Move Up",
                                tint = if (!isFirst) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        }

                        // Move Down
                        IconButton(
                            onClick = onMoveDown,
                            enabled = !isLast,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Move Down",
                                tint = if (!isLast) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                        }

                        // Edit
                        FilledIconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Delete
                        FilledIconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFFE53935),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBarangState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Belum ada barang.", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.Gray)
    }
}

@Composable
fun BarangAnimatedDialog(visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    if (visible) {
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }
                AnimatedVisibility(visible = isVisible, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AddEditBarangDialogContent(
    title: String,
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var error by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFFFF5F4),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

            TextField(
                value = name,
                onValueChange = { name = it; error = false },
                label = { Text("Nama Barang") },
                modifier = Modifier.fillMaxWidth(),
                isError = error,
                supportingText = { if (error) Text("Nama wajib diisi") },
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF3F0F5),
                    unfocusedContainerColor = Color(0xFFF3F0F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { if (name.isBlank()) error = true else onConfirm(name) },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                ) { Text("Simpan", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun DeleteBarangConfirmationContent(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Hapus Barang", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = "Apakah Anda yakin ingin menghapus barang ini?", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Hapus", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
