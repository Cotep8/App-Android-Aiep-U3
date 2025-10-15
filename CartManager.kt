package com.example.appaiep1

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "",
    val requiresColdChain: Boolean = false
)

object CartManager {
    private val cartItems = mutableListOf<Pair<Product, Int>>() // Producto + cantidad

    fun addToCart(product: Product) {
        val existing = cartItems.find { it.first.id == product.id }
        if (existing != null) {
            val index = cartItems.indexOf(existing)
            cartItems[index] = existing.copy(second = existing.second + 1)
        } else {
            cartItems.add(Pair(product, 1))
        }
    }

    fun removeFromCart(product: Product) {
        val existing = cartItems.find { it.first.id == product.id }
        if (existing != null) {
            if (existing.second > 1) {
                val index = cartItems.indexOf(existing)
                cartItems[index] = existing.copy(second = existing.second - 1)
            } else {
                cartItems.remove(existing)
            }
        }
    }

    fun clearCart() {
        cartItems.clear()
    }

    fun getCartItems(): List<Pair<Product, Int>> = cartItems

    fun getTotalPrice(): Double {
        return cartItems.sumOf { it.first.price * it.second }
    }
}
