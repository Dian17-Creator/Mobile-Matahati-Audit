package id.my.matahati.audit

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
import id.my.matahati.audit.data.QuestionData
import id.my.matahati.audit.data.viewmodel.AuditQuestionViewModel
import id.my.matahati.audit.ui.theme.matahati_AuditTheme

class AuditDetailPertanyaanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val categoryId = intent.getIntExtra("category_id", -1)
        val categoryName = intent.getStringExtra("category_name") ?: "Pertanyaan"
        val categoryDesc = intent.getStringExtra("category_description") ?: ""

        setContent {
            matahati_AuditTheme {
                AuditDetailPertanyaanScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    categoryDesc = categoryDesc,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditDetailPertanyaanScreen(
    categoryId: Int,
    categoryName: String,
    categoryDesc: String,
    viewModel: AuditQuestionViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val primaryColor = Color(0xFFB63352)
    val backColor = MaterialTheme.colorScheme.background

    val isAnyDialogOpen = uiState.isAddDialogOpen || uiState.isEditDialogOpen || uiState.isDeleteDialogOpen

    LaunchedEffect(categoryId) {
        if (categoryId != -1) {
            viewModel.fetchQuestions(categoryId)
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
                        Column {
                            Text(
                                text = "List Pertanyaan - $categoryName",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
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
                    modifier = Modifier.padding(bottom = 16.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Tambah Pertanyaan") }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backColor)
                    .padding(innerPadding)
            ) {
                if (uiState.questions.isEmpty() && !uiState.isLoading) {
                    EmptyQuestionsState()
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
                        itemsIndexed(uiState.questions) { index, question ->
                            QuestionCard(
                                index = index + 1,
                                question = question,
                                isFirst = index == 0,
                                isLast = index == uiState.questions.size - 1,
                                onMoveUp = { viewModel.moveUp(categoryId, index) },
                                onMoveDown = { viewModel.moveDown(categoryId, index) },
                                onEdit = { viewModel.openEditDialog(question) },
                                onDelete = { viewModel.openDeleteDialog(question) }
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

        // Dialogs
        AnimatedDetailDialog(visible = uiState.isAddDialogOpen, onDismiss = { viewModel.closeAddDialog() }) {
            AddEditQuestionDialogContent(
                title = "Tambah Pertanyaan",
                onDismiss = { viewModel.closeAddDialog() },
                onConfirm = { text -> viewModel.addQuestion(categoryId, text) }
            )
        }

        AnimatedDetailDialog(visible = uiState.isEditDialogOpen, onDismiss = { viewModel.closeEditDialog() }) {
            AddEditQuestionDialogContent(
                title = "Edit Pertanyaan",
                initialText = uiState.selectedQuestion?.question ?: "",
                onDismiss = { viewModel.closeEditDialog() },
                onConfirm = { text ->
                    uiState.selectedQuestion?.id?.let { id ->
                        viewModel.updateQuestion(categoryId, id, text)
                    }
                }
            )
        }

        AnimatedDetailDialog(visible = uiState.isDeleteDialogOpen, onDismiss = { viewModel.closeDeleteDialog() }) {
            DeleteQuestionConfirmationContent(
                onDismiss = { viewModel.closeDeleteDialog() },
                onConfirm = {
                    uiState.selectedQuestion?.id?.let { id ->
                        viewModel.deleteQuestion(categoryId, id)
                    }
                }
            )
        }
    }
}

@Composable
fun AnimatedDetailDialog(visible: Boolean, onDismiss: () -> Unit, content: @Composable () -> Unit) {
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
fun QuestionCard(
    index: Int,
    question: QuestionData,
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
                // Question Text
                Text(
                    text = question.question,
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
fun EmptyQuestionsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.QuestionAnswer,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Belum ada pertanyaan.",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Gray
        )
        Text(
            text = "Tambahkan pertanyaan pertama untuk kategori ini.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun AddEditQuestionDialogContent(
    title: String,
    initialText: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }
    var error by remember { mutableStateOf(false) }

    val bgcolor = Color(0xFFFFF5F4)
    val primaryColor = Color(0xFFB63352)

    Surface(
        modifier = Modifier.fillMaxWidth(0.9f),
        shape = RoundedCornerShape(24.dp),
        color = bgcolor,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

            TextField(
                value = text,
                onValueChange = { text = it; error = false },
                label = { Text("Pertanyaan") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                isError = error,
                supportingText = { if (error) Text("Pertanyaan tidak boleh kosong") },
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF909090),
                        contentColor = Color.White
                    )
                ) { Text("Batal") }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Button(
                    onClick = { if (text.isBlank()) error = true else onConfirm(text) },
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB63352), contentColor = Color.White)
                ) { Text("Simpan", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun DeleteQuestionConfirmationContent(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(0.85f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = "Hapus Pertanyaan", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            Text(text = "Apakah Anda yakin ingin menghapus pertanyaan ini?\nTindakan ini tidak dapat dibatalkan.", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FilledTonalButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF909090),
                        contentColor = Color.White
                    )
                ) { Text("Batal") }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White)
                ) { Text("Hapus", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
