package com.example.appaiep1

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

class CarritoActivity : AppCompatActivity() {

    private lateinit var rvCarrito: RecyclerView
    private lateinit var tvResumen: TextView
    private lateinit var btnConfirmar: Button

    private lateinit var carrito: MutableList<ItemCarrito>
    private var userLocation: LatLng? = null
    private val tiendaLocation = LatLng(-33.4489, -70.6693) // Ejemplo: Santiago centro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)

        rvCarrito = findViewById(R.id.recyclerCarrito)
        tvResumen = findViewById(R.id.tvResumen)
        btnConfirmar = findViewById(R.id.btnConfirmarPedido)

        rvCarrito.layoutManager = LinearLayoutManager(this)

        // Recuperar datos del intent
        carrito = intent.getSerializableExtra("carrito") as? MutableList<ItemCarrito> ?: mutableListOf()
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        userLocation = LatLng(lat, lon)

        rvCarrito.adapter = CarritoAdapter(carrito)

        mostrarResumen()

        btnConfirmar.setOnClickListener {
            Toast.makeText(this, "✅ Pedido confirmado. ¡Gracias por tu compra!", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun mostrarResumen() {
        val totalCompra = carrito.sumOf { it.precio * it.cantidad }
        val distancia = if (userLocation != null)
            calcularDistanciaKm(userLocation!!, tiendaLocation)
        else 0.0
        val despacho = calcularDespacho(totalCompra, distancia)

        val mensaje = """
            Total productos: $$totalCompra
            Distancia: ${"%.1f".format(distancia)} km
            Despacho: $$despacho
            Total final: $${totalCompra + despacho}
        """.trimIndent()

        tvResumen.text = mensaje
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
}
