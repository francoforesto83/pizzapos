package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val category: String,
    val emoji: String = "🍕",
    val isAvailable: Boolean = true
)

data class OrderItem(
    val productId: Long,
    val name: String,
    val price: Double,
    val quantity: Int,
    val emoji: String,
    val category: String
) {
    val lineTotal: Double get() = price * quantity

    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("productId", productId)
            put("name", name)
            put("price", price)
            put("quantity", quantity)
            put("emoji", emoji)
            put("category", category)
        }
    }

    companion object {
        fun fromJsonObject(json: JSONObject): OrderItem {
            return OrderItem(
                productId = json.optLong("productId", 0L),
                name = json.optString("name", ""),
                price = json.optDouble("price", 0.0),
                quantity = json.optInt("quantity", 1),
                emoji = json.optString("emoji", "🍕"),
                category = json.optString("category", "Pizzas")
            )
        }

        fun listToJson(items: List<OrderItem>): String {
            val array = JSONArray()
            items.forEach { array.put(it.toJsonObject()) }
            return array.toString()
        }

        fun listFromJson(jsonStr: String?): List<OrderItem> {
            if (jsonStr.isNullOrBlank()) return emptyList()
            return try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<OrderItem>()
                for (i in 0 until array.length()) {
                    list.add(fromJsonObject(array.getJSONObject(i)))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}

@Entity(tableName = "table_orders")
data class TableOrderEntity(
    @PrimaryKey
    val orderKey: String, // e.g., "MESA_1" ... "MESA_12" or "DELIVERY"
    val orderType: String, // "MESA" or "DELIVERY"
    val tableNumber: Int = 1,
    val customerName: String = "",
    val customerAddress: String = "",
    val customerPhone: String = "",
    val itemsJson: String = "[]",
    val notes: String = "",
    val isOccupied: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getItems(): List<OrderItem> = OrderItem.listFromJson(itemsJson)
    val total: Double get() = getItems().sumOf { it.lineTotal }
}

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleNumber: Long,
    val origin: String, // "Mesa 3", "Delivery - Juan Pérez (Calle 123)", etc.
    val itemsSummary: String,
    val itemsJson: String,
    val notes: String = "",
    val paymentMethod: String, // "Efectivo", "Tarjeta", "Transferencia"
    val total: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getItems(): List<OrderItem> = OrderItem.listFromJson(itemsJson)
}
