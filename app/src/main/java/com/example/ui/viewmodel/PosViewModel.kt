package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.PizzaPosDatabase
import com.example.data.model.OrderItem
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.TableOrderEntity
import com.example.data.repository.PizzaPosRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OrderType {
    MESA,
    DELIVERY
}

data class CurrentOrderState(
    val type: OrderType = OrderType.MESA,
    val selectedTable: Int = 1,
    val deliveryCustomer: String = "",
    val deliveryAddress: String = "",
    val deliveryPhone: String = "",
    val items: List<OrderItem> = emptyList(),
    val notes: String = ""
) {
    val total: Double get() = items.sumOf { it.lineTotal }
    val itemCount: Int get() = items.sumOf { it.quantity }

    val originDisplay: String
        get() = when (type) {
            OrderType.MESA -> "Mesa $selectedTable"
            OrderType.DELIVERY -> {
                if (deliveryCustomer.isNotBlank()) "Delivery - $deliveryCustomer" else "Delivery"
            }
        }

    val fullOriginSummary: String
        get() = when (type) {
            OrderType.MESA -> "Mesa $selectedTable"
            OrderType.DELIVERY -> {
                val parts = mutableListOf<String>()
                if (deliveryCustomer.isNotBlank()) parts.add(deliveryCustomer)
                if (deliveryAddress.isNotBlank()) parts.add(deliveryAddress)
                if (deliveryPhone.isNotBlank()) parts.add("Tel: $deliveryPhone")
                if (parts.isEmpty()) "Delivery" else "Delivery - " + parts.joinToString(" • ")
            }
        }
}

class PosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PizzaPosRepository
    private val _snackbarMessages = MutableSharedFlow<String>()
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    init {
        val db = PizzaPosDatabase.getDatabase(application, viewModelScope)
        repository = PizzaPosRepository(
            productDao = db.productDao(),
            tableOrderDao = db.tableOrderDao(),
            saleDao = db.saleDao()
        )
        // Ensure initial seed
        viewModelScope.launch {
            repository.checkAndSeedInitialProducts()
            loadCurrentOrder()
        }
    }

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<TableOrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orderState = MutableStateFlow(CurrentOrderState())
    val orderState: StateFlow<CurrentOrderState> = _orderState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Todos")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Filtered products
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        allProducts,
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesCategory = (category == "Todos" || product.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Categories list with "Todos"
    val availableCategories: StateFlow<List<String>> = allProducts.combine(_selectedCategory) { products, _ ->
        val cats = products.map { it.category }.distinct().sorted()
        listOf("Todos") + cats
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Todos"))

    // Occupied tables set (1..12)
    val occupiedTables: StateFlow<Set<Int>> = allOrders.combine(_orderState) { orders, current ->
        val set = mutableSetOf<Int>()
        orders.forEach { order ->
            if (order.orderType == "MESA" && order.isOccupied && order.getItems().isNotEmpty()) {
                set.add(order.tableNumber)
            }
        }
        // Also reflect in-memory current table if it has items
        if (current.type == OrderType.MESA && current.items.isNotEmpty()) {
            set.add(current.selectedTable)
        }
        set
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    private fun getCurrentKey(type: OrderType, table: Int): String {
        return if (type == OrderType.MESA) "MESA_$table" else "DELIVERY"
    }

    fun selectOrderType(type: OrderType) {
        if (_orderState.value.type == type) return
        viewModelScope.launch {
            persistCurrentOrder()
            _orderState.value = _orderState.value.copy(type = type)
            loadCurrentOrder()
        }
    }

    fun selectTable(tableNumber: Int) {
        if (_orderState.value.type == OrderType.MESA && _orderState.value.selectedTable == tableNumber) return
        viewModelScope.launch {
            persistCurrentOrder()
            _orderState.value = _orderState.value.copy(
                type = OrderType.MESA,
                selectedTable = tableNumber
            )
            loadCurrentOrder()
        }
    }

    fun updateDeliveryInfo(name: String, address: String, phone: String) {
        _orderState.value = _orderState.value.copy(
            deliveryCustomer = name,
            deliveryAddress = address,
            deliveryPhone = phone
        )
        persistCurrentOrderAsync()
    }

    fun updateNotes(notes: String) {
        _orderState.value = _orderState.value.copy(notes = notes)
        persistCurrentOrderAsync()
    }

    fun addProduct(product: ProductEntity) {
        val currentItems = _orderState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.productId == product.id }
        if (index != -1) {
            val existing = currentItems[index]
            currentItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(
                OrderItem(
                    productId = product.id,
                    name = product.name,
                    price = product.price,
                    quantity = 1,
                    emoji = product.emoji,
                    category = product.category
                )
            )
        }
        _orderState.value = _orderState.value.copy(items = currentItems)
        persistCurrentOrderAsync()
    }

    fun increaseQuantity(productId: Long) {
        val currentItems = _orderState.value.items.map { item ->
            if (item.productId == productId) item.copy(quantity = item.quantity + 1) else item
        }
        _orderState.value = _orderState.value.copy(items = currentItems)
        persistCurrentOrderAsync()
    }

    fun decreaseQuantity(productId: Long) {
        val currentItems = _orderState.value.items.mapNotNull { item ->
            if (item.productId == productId) {
                if (item.quantity > 1) item.copy(quantity = item.quantity - 1) else null
            } else {
                item
            }
        }
        _orderState.value = _orderState.value.copy(items = currentItems)
        persistCurrentOrderAsync()
    }

    fun removeOrderItem(productId: Long) {
        val currentItems = _orderState.value.items.filterNot { it.productId == productId }
        _orderState.value = _orderState.value.copy(items = currentItems)
        persistCurrentOrderAsync()
    }

    fun clearCurrentOrder() {
        val state = _orderState.value
        val key = getCurrentKey(state.type, state.selectedTable)
        _orderState.value = state.copy(
            items = emptyList(),
            notes = "",
            deliveryCustomer = if (state.type == OrderType.DELIVERY) "" else state.deliveryCustomer,
            deliveryAddress = if (state.type == OrderType.DELIVERY) "" else state.deliveryAddress,
            deliveryPhone = if (state.type == OrderType.DELIVERY) "" else state.deliveryPhone
        )
        viewModelScope.launch {
            repository.clearOrder(key)
            _snackbarMessages.emit("Pedido limpiado")
        }
    }

    private suspend fun loadCurrentOrder() {
        val state = _orderState.value
        val key = getCurrentKey(state.type, state.selectedTable)
        val entity = repository.getOrderByKeySync(key)
        if (entity != null) {
            _orderState.value = state.copy(
                items = entity.getItems(),
                notes = entity.notes,
                deliveryCustomer = entity.customerName,
                deliveryAddress = entity.customerAddress,
                deliveryPhone = entity.customerPhone
            )
        } else {
            _orderState.value = state.copy(
                items = emptyList(),
                notes = "",
                deliveryCustomer = if (state.type == OrderType.DELIVERY) "" else state.deliveryCustomer,
                deliveryAddress = if (state.type == OrderType.DELIVERY) "" else state.deliveryAddress,
                deliveryPhone = if (state.type == OrderType.DELIVERY) "" else state.deliveryPhone
            )
        }
    }

    private suspend fun persistCurrentOrder() {
        val state = _orderState.value
        val key = getCurrentKey(state.type, state.selectedTable)
        val hasItems = state.items.isNotEmpty()

        val entity = TableOrderEntity(
            orderKey = key,
            orderType = state.type.name,
            tableNumber = state.selectedTable,
            customerName = state.deliveryCustomer,
            customerAddress = state.deliveryAddress,
            customerPhone = state.deliveryPhone,
            itemsJson = OrderItem.listToJson(state.items),
            notes = state.notes,
            isOccupied = hasItems,
            updatedAt = System.currentTimeMillis()
        )
        repository.saveOrder(entity)
    }

    private fun persistCurrentOrderAsync() {
        viewModelScope.launch {
            persistCurrentOrder()
        }
    }

    fun confirmPayment(paymentMethod: String) {
        val state = _orderState.value
        if (state.items.isEmpty()) return

        viewModelScope.launch {
            val origin = state.fullOriginSummary
            repository.recordSale(
                origin = origin,
                items = state.items,
                notes = state.notes,
                paymentMethod = paymentMethod
            )

            // Free table or clear delivery order
            val key = getCurrentKey(state.type, state.selectedTable)
            repository.clearOrder(key)

            _orderState.value = state.copy(
                items = emptyList(),
                notes = "",
                deliveryCustomer = "",
                deliveryAddress = "",
                deliveryPhone = ""
            )

            _snackbarMessages.emit("✓ Venta registrada")
        }
    }

    fun addNewProduct(name: String, price: Double, category: String, emoji: String) {
        viewModelScope.launch {
            val product = ProductEntity(
                name = name.trim(),
                price = price,
                category = category.trim().ifBlank { "Pizzas" },
                emoji = emoji.trim().ifBlank { "🍕" }
            )
            repository.insertProduct(product)
            _snackbarMessages.emit("✓ Producto guardado")
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            _snackbarMessages.emit("Producto eliminado")
        }
    }

    fun deleteAllSales() {
        viewModelScope.launch {
            repository.deleteAllSales()
            _snackbarMessages.emit("Historial de ventas borrado")
        }
    }
}
