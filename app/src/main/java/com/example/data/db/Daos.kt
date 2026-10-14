package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.TableOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isAvailable = 1 ORDER BY category ASC, name ASC")
    fun getAvailableProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteById(productId: Long)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int
}

@Dao
interface TableOrderDao {
    @Query("SELECT * FROM table_orders")
    fun getAllOrders(): Flow<List<TableOrderEntity>>

    @Query("SELECT * FROM table_orders WHERE orderKey = :key LIMIT 1")
    fun getOrderByKey(key: String): Flow<TableOrderEntity?>

    @Query("SELECT * FROM table_orders WHERE orderKey = :key LIMIT 1")
    suspend fun getOrderByKeySync(key: String): TableOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: TableOrderEntity)

    @Query("DELETE FROM table_orders WHERE orderKey = :key")
    suspend fun deleteByKey(key: String)

    @Query("UPDATE table_orders SET itemsJson = '[]', notes = '', isOccupied = 0 WHERE orderKey = :key")
    suspend fun clearOrder(key: String)

    @Query("DELETE FROM table_orders")
    suspend fun clearAll()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY id DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getSalesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}
