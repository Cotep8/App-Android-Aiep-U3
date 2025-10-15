package com.example.appaiep1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class CarritoAdapter(
    private val items: List<ItemCarrito>
) : RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder>() {

    inner class CarritoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivImage: ImageView = view.findViewById(R.id.ivCarritoImage)
        val tvNombre: TextView = view.findViewById(R.id.tvCarritoNombre)
        val tvCantidad: TextView = view.findViewById(R.id.tvCarritoCantidad)
        val tvSubtotal: TextView = view.findViewById(R.id.tvCarritoSubtotal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrito, parent, false)
        return CarritoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombre
        holder.tvCantidad.text = "Cantidad: ${item.cantidad}"
        holder.tvSubtotal.text = "Subtotal: $${item.precio * item.cantidad}"
        Glide.with(holder.itemView.context)
            .load(R.drawable.ic_launcher_background) // o imagen real si la guardas
            .into(holder.ivImage)
    }

    override fun getItemCount(): Int = items.size
}
