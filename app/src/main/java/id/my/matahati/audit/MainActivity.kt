package id.my.matahati.audit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import id.my.matahati.audit.data.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val sessionManager = SessionManager(this)
        
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            startActivity(Intent(this, AuditHome::class.java))
        } else {
            startActivity(Intent(this, AuditLogin::class.java))
        }
        
        finish()
    }
}
