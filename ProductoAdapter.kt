package com.example.appaiep1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onAddClick: (Producto) -> Unit
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvProductName)
        val precio: TextView = view.findViewById(R.id.tvProductPrice)
        val imagen: ImageView = view.findViewById(R.id.ivProductImage)
        val btnAgregar: Button = view.findViewById(R.id.btnAgregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.nombre.text = producto.name
        holder.precio.text = "$${producto.price}"

        Glide.with(holder.itemView.context)
            .load(producto.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.imagen)

        holder.btnAgregar.setOnClickListener {
            onAddClick(producto)
            Toast.makeText(
                holder.itemView.context,
                "${producto.name} agregado al carrito",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getItemCount(): Int = productos.size
}