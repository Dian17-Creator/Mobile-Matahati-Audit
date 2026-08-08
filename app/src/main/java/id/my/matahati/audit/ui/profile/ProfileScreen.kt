package id.my.matahati.audit.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.my.matahati.audit.R
import id.my.matahati.audit.data.SessionManager

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val user = sessionManager.getUser()
    
    val primaryColor = Color(0xFFB63352)
    val backgroundColor = Color(0xFFF8F9FB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Avatar Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile3),
                contentDescription = "Avatar",
                modifier = Modifier.size(60.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = user?.name ?: "User",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            user?.email?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        // Account Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Informasi Akun",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = primaryColor
                )
                
                ProfileInfoRow(
                    painter = painterResource(id = R.drawable.ic_profile3),
                    label = "Nama Lengkap",
                    value = user?.name ?: "-"
                )
                
                ProfileInfoRow(
                    painter = painterResource(id = R.drawable.ic_mail),
                    label = "Email",
                    value = user?.email ?: "-"
                )

                ProfileInfoRow(
                    painter = painterResource(id = R.drawable.auditdept),
                    label = "Departemen",
                    value = user?.department_name ?: "-"
                )
                
//                ProfileInfoRow(
//                    icon = Icons.Default.LocationCity,
//                    label = "Perusahaan",
//                    value = user?.company ?: "-"
//                )
            }
        }

        // Logout Button
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Keluar Aplikasi",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ProfileInfoRow(
    painter: Painter,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
    }
}
