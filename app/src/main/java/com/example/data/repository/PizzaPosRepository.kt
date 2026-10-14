package com.example.data.repository

import com.example.data.db.PizzaPosDatabase
import com.example.data.db.ProductDao
import com.example.data.db.SaleDao
import com.example.data.db.TableOrderDao
import com.example.data.model.OrderItem
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.TableOrderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PizzaPosRepository(
    private val productDao: ProductDao,
    private val tableOrderDao: TableOrderDao,
    private val saleDao: SaleDao
) {
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val allOrders: Flow<List<TableOrderEntity>> = tableOrderDao.getAllOrders()
    val allSales: Flow<List<SaleEntity>> = saleDao.getAllSales()

    suspend fun checkAndSeedInitialProducts() = withContext(Dispatchers.IO) {
        if (productDao.count() == 0) {
            productDao.insertAll(PizzaPosDatabase.INITIAL_PRODUCTS)
        }
    }

    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        productDao.insert(product)
    }

    suspend fun deleteProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.delete(product)
    }

    suspend fun deleteProductById(productId: Long) = withContext(Dispatchers.IO) {
        productDao.deleteById(productId)
    }

    fun getOrderByKey(key: String): Flow<TableOrderEntity?> = tableOrderDao.getOrderByKey(key)

    suspend fun getOrderByKeySync(key: String): TableOrderEntity? = withContext(Dispatchers.IO) {
        tableOrderDao.getOrderByKeySync(key)
    }

    suspend fun saveOrder(order: TableOrderEntity) = withContext(Dispatchers.IO) {
        tableOrderDao.upsertOrder(order)
    }

    suspend fun clearOrder(orderKey: String) = withContext(Dispatchers.IO) {
        tableOrderDao.clearOrder(orderKey)
    }

    suspend fun recordSale(
        origin: String,
        items: List<OrderItem>,
        notes: String,
        paymentMethod: String
    ): SaleEntity = withContext(Dispatchers.IO) {
        val total = items.sumOf { it.lineTotal }
        val summary = items.joinToString(", ") { "${it.quantity}x ${it.name}" }
        val itemsJson = OrderItem.listToJson(items)
        val currentCount = saleDao.getSalesCount()
        val saleNumber = (currentCount + 1).toLong()

        val sale = SaleEntity(
            saleNumber = saleNumber,
            origin = origin,
            itemsSummary = summary,
            itemsJson = itemsJson,
            notes = notes,
            paymentMethod = paymentMethod,
            total = total,
            timestamp = System.currentTimeMillis()
        )
        val id = saleDao.insertSale(sale)
        sale.copy(id = id)
    }

    suspend fun deleteAllSales() = withContext(Dispatchers.IO) {
        saleDao.deleteAllSales()
    }
}
