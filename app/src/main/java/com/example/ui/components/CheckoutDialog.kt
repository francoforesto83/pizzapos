package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.PizzaBorder
import com.example.ui.theme.PizzaCardBg
import com.example.ui.theme.PizzaDarkBg
import com.example.ui.theme.PizzaGreen
import com.example.ui.theme.PizzaOrange
import com.example.ui.theme.PizzaPanelBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CurrentOrderState
import com.example.util.CurrencyUtils

@Composable
fun CheckoutDialog(
    orderState: CurrentOrderState,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentMethod: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("Efectivo") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PizzaCardBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("checkout_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💳 Cobrar Pedido",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = orderState.originDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PizzaOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Items summary mini box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PizzaPanelBg)
                        .border(1.dp, PizzaBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Detalle del pedido (${orderState.itemCount} items):",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyColumn(
                            modifier = Modifier.height(if (orderState.items.size > 3) 110.dp else 70.dp)
                        ) {
                            items(orderState.items) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.quantity}x ${item.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = CurrencyUtils.formatAr(item.lineTotal),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        if (orderState.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Obs: ${orderState.notes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Method Selector
                Text(
                    text = "Método de Pago",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMethodButton(
                        title = "Efectivo",
                        icon = Icons.Default.LocalAtm,
                        isSelected = selectedMethod == "Efectivo",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMethod = "Efectivo" }
                    )
                    PaymentMethodButton(
                        title = "Tarjeta",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedMethod == "Tarjeta",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMethod = "Tarjeta" }
                    )
                    PaymentMethodButton(
                        title = "Transf.",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedMethod == "Transferencia",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMethod = "Transferencia" }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Total display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PizzaDarkBg)
                        .border(1.dp, PizzaOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL A COBRAR",
                        style = MaterialTheme.typography.labelLarge,
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

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_payment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PizzaBorder)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onConfirmPayment(selectedMethod) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_payment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PizzaGreen,
                            contentColor = Color.White
                        )
                    ) {
                        Text("✓ Confirmar Cobro", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) PizzaOrange.copy(alpha = 0.18f) else PizzaPanelBg,
        label = "pm_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) PizzaOrange else PizzaBorder,
        label = "pm_border"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) PizzaOrange else TextSecondary,
        label = "pm_color"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) TextPrimary else TextSecondary,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
        )
    }
}
