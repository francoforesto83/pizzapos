package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ProductEntity
import com.example.data.model.SaleEntity
import com.example.data.model.TableOrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProductEntity::class, TableOrderEntity::class, SaleEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PizzaPosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun tableOrderDao(): TableOrderDao
    abstract fun saleDao(): SaleDao

    companion object {
        @Volatile
        private var INSTANCE: PizzaPosDatabase? = null

        val INITIAL_PRODUCTS = listOf(
            ProductEntity(name = "Muzzarella Grande", price = 2800.0, category = "Pizzas", emoji = "🍕"),
            ProductEntity(name = "Muzzarella Chica", price = 1600.0, category = "Pizzas", emoji = "🍕"),
            ProductEntity(name = "Napolitana Grande", price = 3200.0, category = "Pizzas", emoji = "🍕"),
            ProductEntity(name = "Fugazzeta Grande", price = 3400.0, category = "Pizzas", emoji = "🍕"),
            ProductEntity(name = "Especial Grande", price = 3800.0, category = "Pizzas", emoji = "🍕"),
            ProductEntity(name = "Provolone", price = 4200.0, category = "Pizzas", emoji = "🧀"),
            ProductEntity(name = "Empanada Carne", price = 450.0, category = "Empanadas", emoji = "🥟"),
            ProductEntity(name = "Empanada Jamón y Queso", price = 450.0, category = "Empanadas", emoji = "🥟"),
            ProductEntity(name = "Empanada Humita", price = 450.0, category = "Empanadas", emoji = "🥟"),
            ProductEntity(name = "Fainá Porción", price = 600.0, category = "Extras", emoji = "🫓"),
            ProductEntity(name = "Tarta Verdura", price = 1200.0, category = "Extras", emoji = "🥧"),
            ProductEntity(name = "Coca-Cola 500ml", price = 800.0, category = "Bebidas", emoji = "🥤"),
            ProductEntity(name = "Agua 500ml", price = 500.0, category = "Bebidas", emoji = "💧"),
            ProductEntity(name = "Cerveza Lata", price = 1200.0, category = "Bebidas", emoji = "🍺"),
            ProductEntity(name = "Vino Copa", price = 1000.0, category = "Bebidas", emoji = "🍷")
        )

        fun getDatabase(context: Context, scope: CoroutineScope): PizzaPosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PizzaPosDatabase::class.java,
                    "pizzapos_database.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial products on DB creation
                            INSTANCE?.let { database ->
                                scope.launch(Dispatchers.IO) {
                                    database.productDao().insertAll(INITIAL_PRODUCTS)
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
