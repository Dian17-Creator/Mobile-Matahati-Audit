package id.my.matahati.audit.ui.stock

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import id.my.matahati.audit.StockCategoryActivity
import id.my.matahati.audit.StockDepartemenActivity
import id.my.matahati.audit.data.viewmodel.StockViewModel

@Composable
fun StockScreen(
    viewModel: StockViewModel = viewModel()
) {
    val context = LocalContext.current
    val backgroundColor = Color(0xFFF8F9FB)
    val primaryColor = Color(0xFFB63352)

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Pilih menu untuk mengelola stock barang dan pemetaan departemen.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 0.dp, start = 10.dp, end = 10.dp)
        )

//        Spacer(modifier = Modifier.height(4.dp))

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

//        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StockMenuCard(
                title = "Kategori &\nBarang",
                icon = Icons.Default.Inventory2,
                modifier = Modifier.weight(1f),
                onClick = {
                    context.startActivity(Intent(context, StockCategoryActivity::class.java))
                }
            )
            StockMenuCard(
                title = "Pemetaan\nDepartemen",
                icon = Icons.Default.BusinessCenter,
                modifier = Modifier.weight(1f),
                onClick = {
                    context.startActivity(Intent(context, StockDepartemenActivity::class.java))
                }
            )
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
