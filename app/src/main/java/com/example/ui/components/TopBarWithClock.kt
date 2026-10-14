package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PizzaBorder
import com.example.ui.theme.PizzaCardBg
import com.example.ui.theme.PizzaDarkBg
import com.example.ui.theme.PizzaOrange
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TopBarWithClock(
    modifier: Modifier = Modifier
) {
    var currentTimeStr by remember {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        mutableStateOf(sdf.format(Date()))
    }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            currentTimeStr = sdf.format(Date())
            delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PizzaDarkBg)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("top_app_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo "🍕 PizzaPOS" with "POS" in orange
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp
                        )
                    ) {
                        append("🍕 Pizza")
                    }
                    withStyle(
                        SpanStyle(
                            color = PizzaOrange,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 22.sp
                        )
                    ) {
                        append("POS")
                    }
                },
                modifier = Modifier.testTag("app_logo")
            )

            // Clock Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PizzaCardBg)
                    .border(1.dp, PizzaBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("digital_clock")
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Reloj",
                    tint = PizzaOrange,
                    modifier = Modifier.padding(end = 2.dp)
                )
                Text(
                    text = currentTimeStr,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
