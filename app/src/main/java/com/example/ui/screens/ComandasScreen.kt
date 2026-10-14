package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderItem
import com.example.data.model.ProductEntity
import com.example.ui.components.CheckoutDialog
import com.example.ui.theme.PizzaBorder
import com.example.ui.theme.PizzaBorderLight
import com.example.ui.theme.PizzaCardBg
import com.example.ui.theme.PizzaDarkBg
import com.example.ui.theme.PizzaGreen
import com.example.ui.theme.PizzaOrange
import com.example.ui.theme.PizzaOrangeDark
import com.example.ui.theme.PizzaPanelBg
import com.example.ui.theme.PizzaRed
import com.example.ui.theme.PizzaYellow
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CurrentOrderState
import com.example.ui.viewmodel.OrderType
import com.example.ui.viewmodel.PosViewModel
import com.example.util.CurrencyUtils
import com.example.util.KitchenTicketPrinter

@Composable
fun ComandasScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val orderState by viewModel.orderState.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val occupiedTables by viewModel.occupiedTables.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var selectedMobileTab by rememberSaveable { mutableIntStateOf(1) } // 0: Origen, 1: Catálogo, 2: Pedido
    val context = LocalContext.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(PizzaDarkBg)
    ) {
        val isWide = maxWidth >= 760.dp

        if (isWide) {
            // Three columns side by side for tablets / wide screens
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Section A: Origen
                Box(
                    modifier = Modifier
                        .width(230.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PizzaPanelBg)
                        .border(1.dp, PizzaBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    OrderOriginSection(
                        orderState = orderState,
                        occupiedTables = occupiedTables,
                        onSelectType = { viewModel.selectOrderType(it) },
                        onSelectTable = { viewModel.selectTable(it) },
                        onUpdateDelivery = { name, address, phone ->
                            viewModel.updateDeliveryInfo(name, address, phone)
                        }
                    )
                }

                // Section B: Catálogo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PizzaCardBg)
                        .border(1.dp, PizzaBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    CatalogSection(
                        products = filteredProducts,
                        categories = availableCategories,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelect = { viewModel.setSelectedCategory(it) },
                        onProductClick = { viewModel.addProduct(it) }
                    )
                }

                // Section C: Pedido Actual
                Box(
                    modifier = Modifier
                        .width(340.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PizzaPanelBg)
                        .border(1.dp, PizzaBorder, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    CurrentOrderSection(
                        orderState = orderState,
                        onIncreaseQty = { viewModel.increaseQuantity(it) },
                        onDecreaseQty = { viewModel.decreaseQuantity(it) },
                        onRemoveItem = { viewModel.removeOrderItem(it) },
                        onNotesChange = { viewModel.updateNotes(it) },
                        onClearOrder = { viewModel.clearCurrentOrder() },
                        onPrintKitchen = {
                            if (orderState.items.isEmpty()) {
                                Toast.makeText(context, "El pedido está vacío", Toast.LENGTH_SHORT).show()
                            } else {
                                val success = KitchenTicketPrinter.printTicket(
                                    context = context,
                                    origin = orderState.originDisplay,
                                    items = orderState.items,
                                    notes = orderState.notes
                                )
                                if (success) {
                                    Toast.makeText(context, "🖨 Enviando ticket a cocina...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onCheckout = {
                            if (orderState.items.isEmpty()) {
                                Toast.makeText(context, "Agregá productos al pedido para cobrar", Toast.LENGTH_SHORT).show()
                            } else {
                                showCheckoutDialog = true
                            }
                        }
                    )
                }
            }
        } else {
            // Adaptive Compact Mobile Layout with Sub-Tabs & Sticky Quick-Bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Mobile Segmented Top Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedMobileTab,
                    containerColor = PizzaDarkBg,
                    contentColor = PizzaOrange,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedMobileTab]),
                            color = PizzaOrange,
                            height = 3.dp
                        )
                    },
                    divider = { HorizontalDivider(color = PizzaBorder) }
                ) {
                    Tab(
                        selected = selectedMobileTab == 0,
                        onClick = { selectedMobileTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (orderState.type == OrderType.MESA) Icons.Default.TableBar else Icons.Default.DeliveryDining,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (orderState.type == OrderType.MESA) "Mesa ${orderState.selectedTable}" else "Delivery",
                                    fontWeight = if (selectedMobileTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedMobileTab == 0) PizzaOrange else TextSecondary
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedMobileTab == 1,
                        onClick = { selectedMobileTab = 1 },
                        text = {
                            Text(
                                text = "🍕 Catálogo",
                                fontWeight = if (selectedMobileTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedMobileTab == 1) PizzaOrange else TextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedMobileTab == 2,
                        onClick = { selectedMobileTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🛒 Pedido",
                                    fontWeight = if (selectedMobileTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedMobileTab == 2) PizzaOrange else TextSecondary
                                )
                                if (orderState.itemCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(PizzaOrange)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${orderState.itemCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedMobileTab) {
                        0 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                OrderOriginSection(
                                    orderState = orderState,
                                    occupiedTables = occupiedTables,
                                    onSelectType = { viewModel.selectOrderType(it) },
                                    onSelectTable = {
                                        viewModel.selectTable(it)
                                        // Auto advance to catalog after choosing table
                                        selectedMobileTab = 1
                                    },
                                    onUpdateDelivery = { name, address, phone ->
                                        viewModel.updateDeliveryInfo(name, address, phone)
                                    }
                                )
                            }
                        }
                        1 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                CatalogSection(
                                    products = filteredProducts,
                                    categories = availableCategories,
                                    selectedCategory = selectedCategory,
                                    searchQuery = searchQuery,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onCategorySelect = { viewModel.setSelectedCategory(it) },
                                    onProductClick = { viewModel.addProduct(it) }
                                )
                            }
                        }
                        2 -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            ) {
                                CurrentOrderSection(
                                    orderState = orderState,
                                    onIncreaseQty = { viewModel.increaseQuantity(it) },
                                    onDecreaseQty = { viewModel.decreaseQuantity(it) },
                                    onRemoveItem = { viewModel.removeOrderItem(it) },
                                    onNotesChange = { viewModel.updateNotes(it) },
                                    onClearOrder = { viewModel.clearCurrentOrder() },
                                    onPrintKitchen = {
                                        if (orderState.items.isEmpty()) {
                                            Toast.makeText(context, "El pedido está vacío", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val success = KitchenTicketPrinter.printTicket(
                                                context = context,
                                                origin = orderState.originDisplay,
                                                items = orderState.items,
                                                notes = orderState.notes
                                            )
                                            if (success) {
                                                Toast.makeText(context, "🖨 Enviando ticket a cocina...", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    onCheckout = {
                                        if (orderState.items.isEmpty()) {
                                            Toast.makeText(context, "Agregá productos al pedido para cobrar", Toast.LENGTH_SHORT).show()
                                        } else {
                                            showCheckoutDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Sticky quick-order footer on Mobile when on Origen or Catálogo with active items
                if (selectedMobileTab != 2 && orderState.items.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PizzaPanelBg)
                            .border(1.dp, PizzaOrange.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${orderState.originDisplay} • ${orderState.itemCount} items",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = CurrencyUtils.formatAr(orderState.total),
                                style = MaterialTheme.typography.titleLarge,
                                color = PizzaOrange,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { selectedMobileTab = 2 },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
                            ) {
                                Text("Ver Pedido", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = { showCheckoutDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PizzaOrange, contentColor = Color.White)
                            ) {
                                Text("Cobrar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            orderState = orderState,
            onDismiss = { showCheckoutDialog = false },
            onConfirmPayment = { method ->
                viewModel.confirmPayment(method)
                showCheckoutDialog = false
            }
        )
    }
}

// ----------------------------------------------------
// SECTION A: ORIGEN (MESA / DELIVERY)
// ----------------------------------------------------
@Composable
fun OrderOriginSection(
    orderState: CurrentOrderState,
    occupiedTables: Set<Int>,
    onSelectType: (OrderType) -> Unit,
    onSelectTable: (Int) -> Unit,
    onUpdateDelivery: (name: String, address: String, phone: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "Origen del Pedido",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle buttons: Mesa / Delivery
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PizzaCardBg)
                .border(1.dp, PizzaBorder, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val isMesa = orderState.type == OrderType.MESA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isMesa) PizzaOrange else Color.Transparent)
                    .clickable { onSelectType(OrderType.MESA) }
                    .padding(vertical = 8.dp)
                    .testTag("toggle_mesa"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🍽 Mesa",
                        color = if (isMesa) Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isMesa) FontWeight.Black else FontWeight.Medium
                    )
                }
            }

            val isDelivery = orderState.type == OrderType.DELIVERY
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isDelivery) PizzaOrange else Color.Transparent)
                    .clickable { onSelectType(OrderType.DELIVERY) }
                    .padding(vertical = 8.dp)
                    .testTag("toggle_delivery"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🛵 Delivery",
                        color = if (isDelivery) Color.White else TextSecondary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isDelivery) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (orderState.type == OrderType.MESA) {
            // Mesas Grid (1 to 12)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seleccionar Mesa",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PizzaYellow)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Ocupada", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items((1..12).toList()) { tableNum ->
                    val isSelected = orderState.selectedTable == tableNum
                    val isOccupied = occupiedTables.contains(tableNum)

                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) PizzaOrange else PizzaBorder,
                        label = "table_border"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) PizzaOrange.copy(alpha = 0.22f) else PizzaCardBg,
                        label = "table_bg"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { onSelectTable(tableNum) }
                            .padding(vertical = 12.dp, horizontal = 6.dp)
                            .testTag("table_button_$tableNum"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$tableNum",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = if (isSelected) PizzaOrange else TextPrimary,
                                    fontWeight = FontWeight.Black
                                )
                                if (isOccupied) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(PizzaYellow)
                                    )
                                }
                            }
                            Text(
                                text = if (isOccupied) "Ocupada" else "Libre",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOccupied) PizzaYellow else TextMuted
                            )
                        }
                    }
                }
            }
        } else {
            // Delivery Form (Cliente, Dirección, Teléfono)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Datos del Cliente",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = orderState.deliveryCustomer,
                    onValueChange = {
                        onUpdateDelivery(it, orderState.deliveryAddress, orderState.deliveryPhone)
                    },
                    label = { Text("Nombre Cliente") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PizzaOrange) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_customer_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaOrange,
                        unfocusedBorderColor = PizzaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = PizzaOrange,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = PizzaCardBg,
                        unfocusedContainerColor = PizzaCardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = orderState.deliveryAddress,
                    onValueChange = {
                        onUpdateDelivery(orderState.deliveryCustomer, it, orderState.deliveryPhone)
                    },
                    label = { Text("Dirección de Entrega") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PizzaOrange) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_address_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaOrange,
                        unfocusedBorderColor = PizzaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = PizzaOrange,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = PizzaCardBg,
                        unfocusedContainerColor = PizzaCardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = orderState.deliveryPhone,
                    onValueChange = {
                        onUpdateDelivery(orderState.deliveryCustomer, orderState.deliveryAddress, it)
                    },
                    label = { Text("Teléfono") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PizzaOrange) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PizzaOrange,
                        unfocusedBorderColor = PizzaBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = PizzaOrange,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = PizzaCardBg,
                        unfocusedContainerColor = PizzaCardBg
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

// ----------------------------------------------------
// SECTION B: CATÁLOGO DE PRODUCTOS
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogSection(
    products: List<ProductEntity>,
    categories: List<String>,
    selectedCategory: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onProductClick: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar producto...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PizzaOrange) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalog_search_bar"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PizzaOrange,
                unfocusedBorderColor = PizzaBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = PizzaPanelBg,
                unfocusedContainerColor = PizzaPanelBg
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Categories Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = {
                        Text(
                            text = category,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PizzaOrange,
                        selectedLabelColor = Color.White,
                        containerColor = PizzaPanelBg,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) PizzaOrange else PizzaBorder
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("category_chip_$category")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Products Grid
        if (products.isEmpty()) {
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
                        text = "No se encontraron productos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 135.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = PizzaPanelBg
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Large Emoji
            Text(
                text = product.emoji,
                fontSize = 36.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Product Name
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PizzaCardBg)
                    .border(1.dp, PizzaBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Formatted Price in Argentine Pesos
            Text(
                text = CurrencyUtils.formatAr(product.price),
                style = MaterialTheme.typography.titleMedium,
                color = PizzaOrange,
                fontWeight = FontWeight.Black
            )
        }
    }
}

// ----------------------------------------------------
// SECTION C: PEDIDO ACTUAL
// ----------------------------------------------------
@Composable
fun CurrentOrderSection(
    orderState: CurrentOrderState,
    onIncreaseQty: (Long) -> Unit,
    onDecreaseQty: (Long) -> Unit,
    onRemoveItem: (Long) -> Unit,
    onNotesChange: (String) -> Unit,
    onClearOrder: () -> Unit,
    onPrintKitchen: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Order Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = orderState.originDisplay,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${orderState.itemCount} items en comanda",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (orderState.items.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearOrder,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PizzaRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PizzaRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("clear_order_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Limpiar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = PizzaBorder)
        Spacer(modifier = Modifier.height(8.dp))

        // Order Items List
        if (orderState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(42.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pedido vacío",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seleccioná productos del catálogo",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(orderState.items, key = { it.productId }) { item ->
                    OrderItemRow(
                        item = item,
                        onIncrease = { onIncreaseQty(item.productId) },
                        onDecrease = { onDecreaseQty(item.productId) },
                        onRemove = { onRemoveItem(item.productId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Notes / Observations TextField
        OutlinedTextField(
            value = orderState.notes,
            onValueChange = onNotesChange,
            placeholder = { Text("Observaciones (ej: sin cebolla...)", color = TextMuted, fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("order_notes_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PizzaOrange,
                unfocusedBorderColor = PizzaBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = PizzaCardBg,
                unfocusedContainerColor = PizzaCardBg
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Total Row in prominent Orange
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PizzaCardBg)
                .border(1.dp, PizzaBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = CurrencyUtils.formatAr(orderState.total),
                style = MaterialTheme.typography.headlineLarge,
                color = PizzaOrange,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Enviar a Cocina & Cobrar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onPrintKitchen,
                modifier = Modifier
                    .weight(1f)
                    .testTag("send_to_kitchen_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, tint = PizzaOrange, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cocina", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .weight(1.3f)
                    .testTag("checkout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PizzaOrange,
                    contentColor = Color.White
                )
            ) {
                Text("💳 Cobrar", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun OrderItemRow(
    item: OrderItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PizzaCardBg)
            .border(1.dp, PizzaBorder, RoundedCornerShape(10.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Emoji
        Text(text = item.emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))

        // Item details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${CurrencyUtils.formatAr(item.price)} c/u",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        // Stepper controls: [-] [qty] [+]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PizzaPanelBg)
                    .border(1.dp, PizzaBorder, RoundedCornerShape(6.dp))
                    .clickable(onClick = onDecrease),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Restar", tint = TextPrimary, modifier = Modifier.size(14.dp))
            }

            Text(
                text = "${item.quantity}",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(22.dp),
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(PizzaOrange.copy(alpha = 0.2f))
                    .border(1.dp, PizzaOrange.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable(onClick = onIncrease),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Sumar", tint = PizzaOrange, modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Line Total
        Text(
            text = CurrencyUtils.formatAr(item.lineTotal),
            style = MaterialTheme.typography.labelLarge,
            color = PizzaOrange,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(68.dp),
            textAlign = TextAlign.End
        )
    }
}
