package com.example.appaiep1

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import kotlin.math.roundToInt
import android.content.Intent


data class Producto(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    val category: String = "",
    val requiresColdChain: Boolean = false,
    val ventas: Int = 0
)

data class ItemCarrito(
    val id: String,
    val nombre: String,
    val precio: Int,
    var cantidad: Int,
    val requiereFrio: Boolean
) : java.io.Serializable

class MenuActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var tvLocation: TextView
    private lateinit var rvProductos: RecyclerView
    private lateinit var btnGetLocation: Button
    private lateinit var btnVerCarrito: Button
    private val carrito = mutableListOf<ItemCarrito>()

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var userLocation: LatLng? = null
    private val tiendaLocation = LatLng(37.4879, -122.2283)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Inicialización
        auth = FirebaseAuth.getInstance()
        database = com.google.firebase.ktx.Firebase.database
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Referencias a vistas
        tvLocation = findViewById(R.id.tvLocation)
        rvProductos = findViewById(R.id.recyclerProductos)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        btnVerCarrito = findViewById(R.id.btnVerCarrito)

        rvProductos.layoutManager = LinearLayoutManager(this)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        tvLocation.text = "Bienvenido, ${currentUser.email}\nObteniendo ubicación..."
        btnGetLocation.setOnClickListener { getCurrentLocation() }
        btnVerCarrito.setOnClickListener { abrirCarrito() }

        getCurrentLocation()
        cargarProductos()
        escucharTemperaturaCamion()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                userLocation = LatLng(lat, lon)
                tvLocation.text = "Ubicación actual:\nLat: $lat\nLng: $lon"
                saveLocationToFirebase(lat, lon)
            } else {
                tvLocation.text = "No se pudo obtener la ubicación."
            }
        }
    }

    private fun saveLocationToFirebase(lat: Double, lon: Double) {
        val userId = auth.currentUser?.uid ?: return
        val ref = database.getReference("users/$userId/location")
        val data = mapOf(
            "latitude" to lat,
            "longitude" to lon,
            "timestamp" to System.currentTimeMillis()
        )
        ref.setValue(data)
    }

    private fun cargarProductos() {
        val ref = database.getReference("productos")

        ref.get().addOnSuccessListener { snapshot ->
            val listaProductos = mutableListOf<Producto>()
            for (child in snapshot.children) {
                val producto = child.getValue(Producto::class.java)
                if (producto != null) listaProductos.add(producto)
            }

            if (listaProductos.isEmpty()) {
                Toast.makeText(this, "No hay productos disponibles.", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // 🔸 Mostrar todos los productos en el RecyclerView
            rvProductos.adapter = ProductoAdapter(listaProductos) { producto ->
                agregarAlCarrito(producto)
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Error cargando productos.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun agregarAlCarrito(producto: Producto) {
        val existente = carrito.find { it.id == producto.id }
        if (existente != null) {
            existente.cantidad++
        } else {
            carrito.add(
                ItemCarrito(
                    producto.id,
                    producto.name,
                    producto.price,
                    1,
                    producto.requiresColdChain
                )
            )
        }
        Toast.makeText(this, "${producto.name} agregado al carrito.", Toast.LENGTH_SHORT).show()
    }

    private fun abrirCarrito() {
        if (carrito.isEmpty()) {
            Toast.makeText(this, "Carrito vacío.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CarritoActivity::class.java)
        intent.putExtra("carrito", ArrayList(carrito))
        userLocation?.let {
            intent.putExtra("lat", it.latitude)
            intent.putExtra("lon", it.longitude)
        }
        startActivity(intent)
    }


    private fun calcularDespacho(total: Int, distanciaKm: Double): Int {
        return when {
            total >= 50000 -> 0
            total in 25000..49999 -> (150 * distanciaKm).roundToInt()
            else -> (300 * distanciaKm).roundToInt()
        }
    }

    private fun calcularDistanciaKm(p1: LatLng, p2: LatLng): Double {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            p1.latitude, p1.longitude,
            p2.latitude, p2.longitude,
            results
        )
        return results[0].toDouble() / 1000
    }

    private fun escucharTemperaturaCamion() {
        val ref = database.getReference("camion/temperatura")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperatura = snapshot.getValue(Double::class.java) ?: return
                if (temperatura > -10) {
                    mostrarAlarma(temperatura)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun mostrarAlarma(temp: Double) {
        val resId = resources.getIdentifier("alert_sound", "raw", packageName)
        if (resId != 0) {
            try {
                val sonido = MediaPlayer.create(this, resId)
                sonido?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Toast.makeText(this, "⚠️ ¡Temperatura alta: $temp°C!", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        }
    }
}
