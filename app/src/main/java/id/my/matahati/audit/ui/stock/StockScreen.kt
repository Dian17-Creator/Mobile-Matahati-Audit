package id.my.matahati.audit.ui.stock

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import id.my.matahati.audit.AuditProses
import id.my.matahati.audit.StockCategoryActivity
import id.my.matahati.audit.StockDepartemenActivity
import id.my.matahati.audit.StockOpnameActivity
import id.my.matahati.audit.data.RecentActivityData
import id.my.matahati.audit.data.viewmodel.StockViewModel
import id.my.matahati.audit.navigation.Screen

@Composable
fun StockScreen(
    navController: NavHostController,
    viewModel: StockViewModel = viewModel()
) {
    val context = LocalContext.current
    val backgroundColor = MaterialTheme.colorScheme.background

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.fetchDashboardSummary()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Pilih menu untuk mengelola stock barang dan pemetaan departemen.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 0.dp, start = 10.dp, end = 10.dp)
        )

        // Summary Stats Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StockStatCard(
                title = "TOTAL KATEGORI",
                value = uiState.totalKategoriStok,
                icon = Icons.Default.Category,
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFE3F2FD) // Soft Blue
            )
            StockStatCard(
                title = "TOTAL BARANG",
                value = uiState.totalBarang,
                icon = Icons.Default.Inventory2,
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFF1F8E9) // Soft Green
            )
            StockStatCard(
                title = "STOK OPNAME",
                value = uiState.totalStokOpname,
                icon = Icons.AutoMirrored.Filled.Assignment,
                modifier = Modifier.weight(1f),
                containerColor = Color(0xFFFFF3E0) // Soft Orange
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StockMenuCard(
                title = "Kategori &\nBarang",
                icon = Icons.Default.Inventory2,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (navController.currentDestination?.route == Screen.Stock.route) {
                        context.startActivity(Intent(context, StockCategoryActivity::class.java))
                    }
                }
            )
            StockMenuCard(
                title = "Pemetaan\nDepartemen",
                icon = Icons.Default.BusinessCenter,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (navController.currentDestination?.route == Screen.Stock.route) {
                        context.startActivity(Intent(context, StockDepartemenActivity::class.java))
                    }
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StockMenuCard(
                title = "Stok\nOpname",
                icon = Icons.AutoMirrored.Filled.Assignment,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (navController.currentDestination?.route == Screen.Stock.route) {
                        val intent = Intent(context, id.my.matahati.audit.StockOpnameActivity::class.java)
                        context.startActivity(intent)
                    }
                }
            )
            StockMenuCard(
                title = "Hasil Stok\nOpname",
                icon = Icons.Default.Assessment,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (navController.currentDestination?.route == Screen.Stock.route) {
                        context.startActivity(Intent(context, id.my.matahati.audit.StockOpnameHasilActivity::class.java))
                    }
                }
            )
        }

        RecentActivitySection(activities = uiState.recentActivities) { activityId ->
            if (navController.currentDestination?.route == Screen.Stock.route) {
                context.startActivity(
                    Intent(context, id.my.matahati.audit.StockOpnameActivity::class.java).apply {
                        putExtra("audit_id", activityId)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun RecentActivitySection(activities: List<RecentActivityData>, onActivityClick: (Int) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayActivities = if (isExpanded) activities else activities.take(3)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Terbaru",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            if (activities.size > 3) {
                Text(
                    text = if (isExpanded) "Sembunyikan" else "Lihat Semua",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB63352),
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }
        }
        
        if (activities.isEmpty()) {
            EmptyRecentActivity()
        } else {
            displayActivities.forEach { activity ->
                ActivityItem(
                    title = activity.title, 
                    subtitle = activity.subtitle, 
                    status = activity.status, 
                    statusColor = if (activity.status == "Selesai" || activity.status == "Submitted") Color(0xFF4CAF50) else Color(0xFF2196F3),
                    onClick = { onActivityClick(activity.id) }
                )
            }
        }
    }
}

@Composable
fun EmptyRecentActivity() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada proses stok opname",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = "Proses stok opname yang dibuat akan muncul di sini.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ActivityItem(title: String, subtitle: String, status: String, statusColor: Color, onClick: () -> Unit) {
    val isFinished = status == "Selesai" || status == "Submitted"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFinished) Icons.Default.CheckCircle else Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = statusColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun StockStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, Color(0xFFB63352).copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun StockMenuCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val primaryColor = Color(0xFFB63352)
    
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = primaryColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.Black,
                lineHeight = 18.sp
            )
        }
    }
}
