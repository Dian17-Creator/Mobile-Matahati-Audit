package id.my.matahati.audit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.audit.data.CategoryData
import id.my.matahati.audit.data.viewmodel.AuditCategoryViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme

class AuditPertanyaan : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            matahati_AuditTheme {
                AuditPertanyaanScreen(
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
fun AuditPertanyaanScreen(
    viewModel: AuditCategoryViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchCategories()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val primaryColor = Color(0xFFB63352)
    val backColor = MaterialTheme.colorScheme.background

    // Check if any dialog is open for blur effect
    val isAnyDialogOpen = uiState.isAddDialogOpen || uiState.isEditDialogOpen || uiState.isDeleteDialogOpen

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
                .blur(if (isAnyDialogOpen) 16.dp else 0.dp), // Apply blur when dialog is open
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Kategori Audit",
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
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddDialog() },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(100.dp), // Pill shape
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
                    EmptyState()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.categories) { category ->
                            CategoryCard(
                                category = category,
                                onClick = {
                                    val intent = Intent(context, AuditDetailPertanyaanActivity::class.java).apply {
                                        putExtra("category_id", category.id)
                                        putExtra("category_name", category.name)
                                        putExtra("category_description", category.description)
                                    }
                                    context.startActivity(intent)
                                },
                                onEdit = { viewModel.openEditDialog(category) },
                                onDelete = { viewModel.openDeleteDialog(category) }
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
            }
        }

        // Animated Dialogs
        AnimatedDialog(
            visible = uiState.isAddDialogOpen,
            onDismiss = { viewModel.closeAddDialog() }
        ) {
            AddEditCategoryDialogContent(
                title = "Tambah Kategori",
                onDismiss = { viewModel.closeAddDialog() },
                onConfirm = { name, desc -> viewModel.addCategory(name, desc) }
            )
        }

        AnimatedDialog(
            visible = uiState.isEditDialogOpen,
            onDismiss = { viewModel.closeEditDialog() }
        ) {
            AddEditCategoryDialogContent(
                title = "Edit Kategori",
                initialName = uiState.selectedCategory?.name ?: "",
                initialDesc = uiState.selectedCategory?.description ?: "",
                onDismiss = { viewModel.closeEditDialog() },
                onConfirm = { name, desc ->
                    uiState.selectedCategory?.id?.let { id ->
                        viewModel.updateCategory(id, name, desc)
                    }
                }
            )
        }

        AnimatedDialog(
            visible = uiState.isDeleteDialogOpen,
            onDismiss = { viewModel.closeDeleteDialog() }
        ) {
            DeleteConfirmationDialogContent(
                onDismiss = { viewModel.closeDeleteDialog() },
                onConfirm = {
                    uiState.selectedCategory?.id?.let { id ->
                        viewModel.deleteCategory(id)
                    }
                }
            )
        }
    }
}

@Composable
fun AnimatedDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Dimmed background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
                
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
fun CategoryCard(
    category: CategoryData,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFB63352)
                )
                if (!category.description.isNullOrEmpty()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.QuestionAnswer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${category.questionCount} Pertanyaan",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                }
                
                FilledIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada kategori audit.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AddEditCategoryDialogContent(
    title: String,
    initialName: String = "",
    initialDesc: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var desc by remember { mutableStateOf(initialDesc) }
    var nameError by remember { mutableStateOf(false) }

    val bgcolor = Color(0xFFFFF5F4)
    val primaryColor = Color(0xFFB63352)

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = bgcolor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Column() {
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = {
                        Text("Nama Kategori")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError,
                    supportingText = {
                        if (nameError) {
                            Text("Nama wajib diisi")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(

                        // Background
                        focusedContainerColor = Color(0xFFF3F0F5),
                        unfocusedContainerColor = Color(0xFFF3F0F5),
                        disabledContainerColor = Color(0xFFF3F0F5),
                        errorContainerColor = Color(0xFFF3F0F5),

                        // Text
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray,
                        errorTextColor = Color.Black,

                        // Label
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.Gray,
                        errorLabelColor = Color.Red,

                        // Cursor
                        cursorColor = primaryColor,

                        // Indicator
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,

                        // Placeholder
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
                
                TextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 85.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(

                        // Background
                        focusedContainerColor = Color(0xFFF3F0F5),
                        unfocusedContainerColor = Color(0xFFF3F0F5),
                        disabledContainerColor = Color(0xFFF3F0F5),
                        errorContainerColor = Color(0xFFF3F0F5),

                        // Text
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        disabledTextColor = Color.Gray,
                        errorTextColor = Color.Black,

                        // Label
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = Color.Gray,
                        errorLabelColor = Color.Red,

                        // Cursor
                        cursorColor = primaryColor,

                        // Indicator
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,

                        // Placeholder
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF909090),
                        contentColor = Color.White
                    )
                ) {
                    Text("Batal")
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            nameError = true
                        } else {
                            onConfirm(name, desc)
                        }
                    },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB63352),
                        contentColor = Color.White
                    )
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialogContent(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Hapus Kategori",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            
            Text(
                text = "Kategori akan dihapus secara permanen. Apakah Anda ingin melanjutkan?",
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF909090),
                        contentColor = Color.White
                    )
                ) {
                    Text("Batal")
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    )
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
