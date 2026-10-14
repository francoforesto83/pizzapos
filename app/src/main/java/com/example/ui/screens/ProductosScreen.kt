package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ProductEntity
import com.example.ui.theme.PizzaBorder
import com.example.ui.theme.PizzaCardBg
import com.example.ui.theme.PizzaDarkBg
import com.example.ui.theme.PizzaGreen
import com.example.ui.theme.PizzaOrange
import com.example.ui.theme.PizzaPanelBg
import com.example.ui.theme.PizzaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PosViewModel
import com.example.util.CurrencyUtils

@Composable
fun ProductosScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    // Group products by category
    val groupedProducts = remember(allProducts) {
        allProducts.groupBy { it.category }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PizzaDarkBg)
            .padding(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🍕 Catálogo de Productos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${allProducts.size} productos disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PizzaOrange,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("add_new_product_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nuevo Producto", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grouped List
        if (allProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🍕", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay productos en el catálogo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                groupedProducts.forEach { (category, productsInCategory) ->
                    item(key = "cat_header_$category") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = PizzaOrange,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PizzaPanelBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${productsInCategory.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    items(productsInCategory, key = { it.id }) { product ->
                        ProductListItemCard(
                            product = product,
                            onDelete = { productToDelete = product }
                        )
                    }
                }
            }
        }
    }

    // Modal Add Product Dialog
    if (showAddDialog) {
        val existingCategories = remember(allProducts) {
            allProducts.map { it.category }.distinct()
        }
        AddProductDialog(
            existingCategories = existingCategories,
            onDismiss = { showAddDialog = false },
            onSave = { name, price, category, emoji ->
                viewModel.addNewProduct(name, price, category, emoji)
                showAddDialog = false
            }
        )
    }

    // Confirmation dialog to delete product
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = {
                Text(
                    text = "Eliminar Producto",
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar \"${product.name}\"? Esta acción no se puede deshacer.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaRed)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = PizzaCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ProductListItemCard(
    product: ProductEntity,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_item_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PizzaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Emoji and Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PizzaPanelBg)
                        .border(1.dp, PizzaBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = product.emoji, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Price and Delete Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = CurrencyUtils.formatAr(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = PizzaOrange,
                    fontWeight = FontWeight.Black
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_product_button_${product.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar producto",
                        tint = PizzaRed.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    existingCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, category: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(existingCategories.firstOrNull() ?: "Pizzas") }
    var emoji by remember { mutableStateOf("🍕") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quickEmojis = listOf("🍕", "🥟", "🧀", "🫓", "🥧", "🥤", "💧", "🍺", "🍷", "🍟", "🥗", "🍰", "🍔")
    val defaultCategories = listOf("Pizzas", "Empanadas", "Extras", "Bebidas", "Postres")
    val allCategories = (defaultCategories + existingCategories).distinct()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PizzaCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_product_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "＋ Nuevo Producto",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Emoji Picker (Quick list + text input)
                Text(
                    text = "Emoji del Producto",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickEmojis.forEach { itemEmoji ->
                        val isSelected = itemEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PizzaOrange.copy(alpha = 0.25f) else PizzaPanelBg)
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) PizzaOrange else PizzaBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { emoji = itemEmoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = itemEmoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nombre del Producto") },
                    placeholder = { Text("Ej: Fugazzeta Especial") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaOrange,
                        unfocusedBorderColor = PizzaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = PizzaPanelBg,
                        unfocusedContainerColor = PizzaPanelBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price Field
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' || char == ',' }) {
                            priceText = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Precio ($ ARS)") },
                    placeholder = { Text("Ej: 3400") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaOrange,
                        unfocusedBorderColor = PizzaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = PizzaPanelBg,
                        unfocusedContainerColor = PizzaPanelBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Field & Suggestions
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allCategories.forEach { cat ->
                        val isSelected = cat.equals(category, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PizzaOrange else PizzaPanelBg)
                                .border(1.dp, if (isSelected) PizzaOrange else PizzaBorder, RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("O escribí una categoría") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_category_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaOrange,
                        unfocusedBorderColor = PizzaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = PizzaPanelBg,
                        unfocusedContainerColor = PizzaPanelBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = PizzaRed
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val cleanName = name.trim()
                            val cleanPrice = priceText.replace(",", ".").toDoubleOrNull()
                            if (cleanName.isBlank()) {
                                errorMessage = "Ingresá el nombre del producto"
                                return@Button
                            }
                            if (cleanPrice == null || cleanPrice <= 0) {
                                errorMessage = "Ingresá un precio válido"
                                return@Button
                            }
                            onSave(cleanName, cleanPrice, category.ifBlank { "Pizzas" }, emoji.ifBlank { "🍕" })
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_new_product_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PizzaOrange,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Guardar Producto", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
