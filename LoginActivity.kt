package com.example.appaiep1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView

    // Permisos para ubicación
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Vincular vistas
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                tvError.text = "Por favor, ingresa email y contraseña."
                tvError.visibility = TextView.VISIBLE
                return@setOnClickListener
            }

            // Ocultar error previo
            tvError.visibility = TextView.GONE

            // Intentar login
            loginUser (email, password)
        }
    }

    private fun loginUser (email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) { task ->
                if (task.isSuccessful) {
                    // Login exitoso
                    val user = auth.currentUser
                    if (user != null) {
                        Toast.makeText(this, "Login exitoso: ${user.email}", Toast.LENGTH_SHORT).show()
                        // Obtener y guardar ubicación GPS
                        getAndSaveLocation(user.uid)
                    }
                } else {
                    // Error en login
                    tvError.text = "Error en login: ${task.exception?.message}"
                    tvError.visibility = TextView.VISIBLE
                    Toast.makeText(this, "Login fallido.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getAndSaveLocation(userId: String) {
        // Verificar y pedir permisos
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // Configurar LocationRequest (prioridad alta para precisión)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000) // 10 seg
            .setWaitForAccurateLocation(true)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    // Guardar en Realtime Database
                    saveLocationToDatabase(userId, location.latitude, location.longitude)
                    // Abrir MenuActivity
                    startActivity(Intent(this@LoginActivity, MenuActivity::class.java))
                    finish()
                } ?: run {
                    Toast.makeText(this@LoginActivity, "No se pudo obtener ubicación.", Toast.LENGTH_SHORT).show()
                }
                // Detener actualizaciones después de obtener una
                fusedLocationClient.removeLocationUpdates(this)
            }
        }

        // Solicitar ubicación
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun saveLocationToDatabase(userId: String, lat: Double, lng: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userRef = database.reference.child("users").child(userId).child("location")
                val locationData = mapOf(
                    "latitude" to lat,
                    "longitude" to lng,
                    "timestamp" to System.currentTimeMillis()
                )
                userRef.setValue(locationData).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Ubicación guardada en Firebase.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error al guardar ubicación: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun hasLocationPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permisos concedidos, obtener ubicación
                val user = auth.currentUser
                if (user != null) {
                    getAndSaveLocation(user.uid)
                }
            } else {
                Toast.makeText(this, "Permisos de ubicación denegados. No se puede guardar posición.", Toast.LENGTH_LONG).show()
                // Opcional: Abrir MenuActivity de todos modos
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
        }
    }
}
