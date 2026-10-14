package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SaleEntity
import com.example.ui.theme.PizzaBlue
import com.example.ui.theme.PizzaBorder
import com.example.ui.theme.PizzaCardBg
import com.example.ui.theme.PizzaDarkBg
import com.example.ui.theme.PizzaGreen
import com.example.ui.theme.PizzaGreenBg
import com.example.ui.theme.PizzaOrange
import com.example.ui.theme.PizzaOrangeDark
import com.example.ui.theme.PizzaPanelBg
import com.example.ui.theme.PizzaPurple
import com.example.ui.theme.PizzaRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PosViewModel
import com.example.util.CurrencyUtils

@Composable
fun VentasScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.allSales.collectAsState()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Calculate today's sales
    val todaySales = sales.filter { CurrencyUtils.isToday(it.timestamp) }
    val todayTotal = todaySales.sumOf { it.total }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PizzaDarkBg)
            .padding(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📊 Historial de Ventas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${todaySales.size} ventas hoy (${sales.size} en total)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (sales.isNotEmpty()) {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PizzaRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PizzaRed.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("clear_sales_history_button")
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Borrar historial", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total del Día Orange Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("today_total_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PizzaOrange
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOTAL DEL DÍA",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = CurrencyUtils.formatAr(todayTotal),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${todaySales.size} tickets",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sales List
        if (sales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No hay ventas registradas aún",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Las ventas cobradas desde las comandas aparecerán aquí.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(sales, key = { it.id }) { sale ->
                    SaleItemCard(sale = sale)
                }
            }
        }
    }

    // Confirmation dialog to clear sales history
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Borrar historial de ventas",
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar todas las ventas registradas? Esta acción no se puede deshacer.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllSales()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaRed)
                ) {
                    Text("Borrar todo", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = PizzaCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun SaleItemCard(sale: SaleEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.saleNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PizzaCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Sale #, Origin, and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PizzaPanelBg)
                            .border(1.dp, PizzaBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${sale.saleNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sale.origin,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = CurrencyUtils.formatDate(sale.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items Summary
            Text(
                text = sale.itemsSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (sale.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nota: ${sale.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Payment method badge & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PaymentMethodBadge(method = sale.paymentMethod)

                Text(
                    text = CurrencyUtils.formatAr(sale.total),
                    style = MaterialTheme.typography.titleLarge,
                    color = PizzaOrange,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun PaymentMethodBadge(method: String) {
    val (badgeBg, badgeText, badgeBorder) = when (method.lowercase()) {
        "efectivo" -> Triple(PizzaGreen.copy(alpha = 0.15f), PizzaGreen, PizzaGreen.copy(alpha = 0.4f))
        "tarjeta" -> Triple(PizzaBlue.copy(alpha = 0.15f), PizzaBlue, PizzaBlue.copy(alpha = 0.4f))
        else -> Triple(PizzaPurple.copy(alpha = 0.15f), PizzaPurple, PizzaPurple.copy(alpha = 0.4f))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(badgeBg)
            .border(1.dp, badgeBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = method.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = badgeText,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp
        )
    }
}
