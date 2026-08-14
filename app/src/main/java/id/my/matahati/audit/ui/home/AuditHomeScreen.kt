package id.my.matahati.audit.ui.home

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import id.my.matahati.audit.*
import id.my.matahati.audit.R
import id.my.matahati.audit.data.RecentActivityData
import id.my.matahati.audit.data.viewmodel.HomeViewModel
import id.my.matahati.audit.navigation.Screen

data class HomeMenu(
    val title: String,
    @DrawableRes val iconRes: Int
)

@Composable
fun AuditHomeScreen(
    username: String,
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFF8F9FB)

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

    val menus = listOf(
        HomeMenu("Kategori &\nPertanyaan", R.drawable.auditquest),
        HomeMenu("Pemetaan\nDepartemen", R.drawable.auditdept),
        HomeMenu("Audit", R.drawable.ic_audits),
        HomeMenu("Hasil Audit", R.drawable.ic_reportaudits)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WelcomeCard(username = username)

        SummaryStatsSection(
            totalKategori = uiState.totalKategori,
            totalPertanyaan = uiState.totalPertanyaan,
            totalAudit = uiState.totalAudit
        )

        MainMenuSection(menus = menus) { menuTitle ->
            if (navController.currentDestination?.route == Screen.Home.route) {
                when (menuTitle) {
                    "Kategori &\nPertanyaan" -> context.startActivity(Intent(context, AuditPertanyaan::class.java))
                    "Pemetaan\nDepartemen" -> context.startActivity(Intent(context, AuditDepartemen::class.java))
                    "Audit" -> context.startActivity(Intent(context, AuditProses::class.java))
                    "Hasil Audit" -> context.startActivity(Intent(context, AuditHasil::class.java))
                }
            }
        }

        RecentActivitySection(activities = uiState.recentActivities) { activityId ->
            if (navController.currentDestination?.route == Screen.Home.route) {
                context.startActivity(
                    Intent(context, AuditProses::class.java).apply {
                        putExtra("audit_id", activityId)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun WelcomeCard(username: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Selamat Datang,",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = username,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Semoga aktivitas audit hari ini berjalan lancar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB63352)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile2),
                    contentDescription = "Profile",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun SummaryStatsSection(totalKategori: String, totalPertanyaan: String, totalAudit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "TOTAL KATEGORI",
            value = totalKategori,
            icon = Icons.Default.Category,
            modifier = Modifier.weight(1f),
            containerColor = Color(0xFFE3F2FD) // Soft Blue
        )
        StatCard(
            title = "TOTAL PERTANYAAN",
            value = totalPertanyaan,
            icon = Icons.Default.QuestionAnswer,
            modifier = Modifier.weight(1.2f),
            containerColor = Color(0xFFF1F8E9) // Soft Green
        )
        StatCard(
            title = "AUDIT",
            value = totalAudit,
            icon = Icons.Default.Assignment,
            modifier = Modifier.weight(0.8f),
            containerColor = Color(0xFFFFF3E0) // Soft Orange
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
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
fun MainMenuSection(menus: List<HomeMenu>, onMenuClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuCard(menu = menus[0], modifier = Modifier.weight(1f), onClick = onMenuClick)
            MenuCard(menu = menus[1], modifier = Modifier.weight(1f), onClick = onMenuClick)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuCard(menu = menus[2], modifier = Modifier.weight(1f), onClick = onMenuClick)
            MenuCard(menu = menus[3], modifier = Modifier.weight(1f), onClick = onMenuClick)
        }
    }
}

@Composable
fun MenuCard(menu: HomeMenu, modifier: Modifier = Modifier, onClick: (String) -> Unit) {
    Card(
        modifier = modifier
            .clickable { onClick(menu.title) },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    .background(Color(0xFFB63352).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = menu.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFB63352)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = menu.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color.Black,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<RecentActivityData>, onActivityClick: (Int) -> Unit) {
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
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB63352),
                modifier = Modifier.clickable { }
            )
        }
        
        if (activities.isEmpty()) {
            EmptyRecentActivity()
        } else {
            activities.forEach { activity ->
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
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Belum ada proses audit",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            Text(
                text = "Proses audit yang dibuat akan muncul di sini.",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
