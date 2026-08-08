package id.my.matahati.audit.ui.stock

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.audit.data.StockCategoryData
import id.my.matahati.audit.data.StockItemData
import id.my.matahati.audit.data.viewmodel.StockViewModel
import id.my.matahati.audit.data.viewmodel.StockUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(
    viewModel: StockViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    val primaryColor = Color(0xFFB63352)
    val backColor = Color(0xFFF8F9FB)

    val isAnyDialogOpen = uiState.isAddCategoryDialogOpen || 
            uiState.isEditCategoryDialogOpen || 
            uiState.isDeleteCategoryDialogOpen ||
            uiState.isAddItemDialogOpen ||
            uiState.isDeleteItemDialogOpen

    LaunchedEffect(Unit) {
        viewModel.fetchCategories()
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddCategoryDialog() },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(100.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Kategori") }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backColor)
                    .padding(innerPadding)
            ) {
                if (uiState.categories.isEmpty() && !uiState.isLoading) {
                    EmptyStockState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 0.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.categories, key = { it.id }) { category ->
                            StockCategoryCard(
                                category = category,
                                onEditCategory = { viewModel.openEditCategoryDialog(category) },
                                onDeleteCategory = { viewModel.openDeleteCategoryDialog(category) },
                                onAddItem = { viewModel.openAddItemDialog(category) },
                                onDeleteItem = { viewModel.openDeleteItemDialog(it) },
                                onReorder = { itemIds -> viewModel.reorderItems(category.id, itemIds) }
                            )
                        }
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
        StockAnimatedDialog(visible = uiState.isAddCategoryDialogOpen, onDismiss = { viewModel.closeAddCategoryDialog() }) {
            AddEditCategoryDialog(
                title = "Tambah Kategori",
                onDismiss = { viewModel.closeAddCategoryDialog() },
                onConfirm = { name, desc -> viewModel.addCategory(name, desc) }
            )
        }

        StockAnimatedDialog(visible = uiState.isEditCategoryDialogOpen, onDismiss = { viewModel.closeEditCategoryDialog() }) {
            AddEditCategoryDialog(
                title = "Edit Kategori",
                initialName = uiState.selectedCategory?.name ?: "",
                initialDesc = uiState.selectedCategory?.description ?: "",
                onDismiss = { viewModel.closeEditCategoryDialog() },
                onConfirm = { name, desc ->
                    uiState.selectedCategory?.id?.let { id ->
                        viewModel.updateCategory(id, name, desc)
                    }
                }
            )
        }

        StockAnimatedDialog(visible = uiState.isDeleteCategoryDialogOpen, onDismiss = { viewModel.closeDeleteCategoryDialog() }) {
            DeleteConfirmDialog(
                title = "Hapus Kategori",
                message = "Apakah Anda yakin ingin menghapus kategori '${uiState.selectedCategory?.name}'?",
                onDismiss = { viewModel.closeDeleteCategoryDialog() },
                onConfirm = {
                    uiState.selectedCategory?.id?.let { id ->
                        viewModel.deleteCategory(id)
                    }
                }
            )
        }

        StockAnimatedDialog(visible = uiState.isAddItemDialogOpen, onDismiss = { viewModel.closeAddItemDialog() }) {
            AddItemDialog(
                categoryName = uiState.selectedCategory?.name ?: "",
                onDismiss = { viewModel.closeAddItemDialog() },
                onConfirm = { name ->
                    uiState.selectedCategory?.id?.let { id ->
                        viewModel.addItem(id, name)
                    }
                }
            )
        }

        StockAnimatedDialog(visible = uiState.isDeleteItemDialogOpen, onDismiss = { viewModel.closeDeleteItemDialog() }) {
            DeleteConfirmDialog(
                title = "Hapus Barang",
                message = "Apakah Anda yakin ingin menghapus barang '${uiState.selectedItem?.name}'?",
                onDismiss = { viewModel.closeDeleteItemDialog() },
                onConfirm = {
                    uiState.selectedItem?.id?.let { id ->
                        viewModel.deleteItem(id)
                    }
                }
            )
        }
    }
}

@Composable
fun StockCategoryCard(
    category: StockCategoryData,
    onEditCategory: () -> Unit,
    onDeleteCategory: () -> Unit,
    onAddItem: () -> Unit,
    onDeleteItem: (StockItemData) -> Unit,
    onReorder: (List<Int>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFFB63352)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name ?: "",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor
                    )
                    if (!category.description.isNullOrEmpty()) {
                        Text(
                            text = category.description ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = "${category.items?.size ?: 0} Barang",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditCategory) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = onDeleteCategory) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935))
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                    
                    val items = category.items ?: emptyList()
                    if (items.isEmpty()) {
                        Text(
                            text = "Belum ada barang di kategori ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        // Drag & Drop Items List
                        var itemsList by remember(items) { mutableStateOf(items) }
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsList.forEachIndexed { index, item ->
                                StockItemRow(
                                    index = index + 1,
                                    item = item,
                                    onDelete = { onDeleteItem(item) },
                                    onDrag = { from, to ->
                                        val mutable = itemsList.toMutableList()
                                        val element = mutable.removeAt(from)
                                        mutable.add(to, element)
                                        itemsList = mutable
                                        onReorder(mutable.map { it.id })
                                    },
                                    currentIndex = index,
                                    totalItems = itemsList.size
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onAddItem,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = primaryColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tambah Barang")
                    }
                }
            }
        }
    }
}

@Composable
fun StockItemRow(
    index: Int,
    item: StockItemData,
    onDelete: () -> Unit,
    onDrag: (Int, Int) -> Unit,
    currentIndex: Int,
    totalItems: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1F1F1), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.width(24.dp),
            color = Color.Gray
        )
        Text(
            text = item.name ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Manual reorder buttons for stability if full drag is complex
            IconButton(
                onClick = { if (currentIndex > 0) onDrag(currentIndex, currentIndex - 1) },
                enabled = currentIndex > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = if (currentIndex > 0) Color.Gray else Color.LightGray)
            }
            IconButton(
                onClick = { if (currentIndex < totalItems - 1) onDrag(currentIndex, currentIndex + 1) },
                enabled = currentIndex < totalItems - 1,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = if (currentIndex < totalItems - 1) Color.Gray else Color.LightGray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EmptyStockState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Belum ada kategori stock.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
    }
}

@Composable
fun StockAnimatedDialog(visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    if (visible) {
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
                    exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f, animationSpec = tween(300))
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun AddEditCategoryDialog(
    title: String,
    initialName: String = "",
    initialDesc: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDesc) }
    var nameError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            
            TextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Nama Kategori") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = { if (nameError) Text("Nama wajib diisi") },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5))
            )
            
            TextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Deskripsi") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5))
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (name.isBlank()) nameError = true else onConfirm(name, desc) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                ) { Text("Simpan") }
            }
        }
    }
}

@Composable
fun AddItemDialog(
    categoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Tambah Barang", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text("Kategori: $categoryName", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            TextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Nama Barang") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = { if (nameError) Text("Nama wajib diisi") },
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5))
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { if (name.isBlank()) nameError = true else onConfirm(name) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352))
                ) { Text("Simpan") }
            }
        }
    }
}

@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Batal", color = Color.Gray) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) { Text("Hapus") }
            }
        }
    }
}
