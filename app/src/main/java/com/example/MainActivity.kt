package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TopBarWithClock
import com.example.ui.screens.ComandasScreen
import com.example.ui.screens.ProductosScreen
import com.example.ui.screens.VentasScreen
import com.example.ui.theme.PizzaBorder
import com.example.ui.theme.PizzaDarkBg
import com.example.ui.theme.PizzaOrange
import com.example.ui.theme.PizzaPanelBg
import com.example.ui.theme.PizzaPosTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.PosViewModel
import kotlinx.coroutines.flow.collectLatest

enum class MainScreen(val title: String, val icon: ImageVector, val tag: String) {
    COMANDAS("Comandas", Icons.Default.LocalPizza, "nav_comandas"),
    VENTAS("Ventas", Icons.Default.PointOfSale, "nav_ventas"),
    PRODUCTOS("Productos", Icons.Default.RestaurantMenu, "nav_productos")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PizzaPosTheme {
                val posViewModel: PosViewModel = viewModel()
                val snackbarHostState = remember { SnackbarHostState() }
                var currentScreen by remember { mutableStateOf(MainScreen.COMANDAS) }

                // Collect snackbar events from ViewModel
                LaunchedEffect(Unit) {
                    posViewModel.snackbarMessages.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Handle system back navigation to return to Comandas if on another screen
                BackHandler(enabled = currentScreen != MainScreen.COMANDAS) {
                    currentScreen = MainScreen.COMANDAS
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = PizzaDarkBg,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopBarWithClock()
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = PizzaPanelBg,
                            contentColor = TextPrimary,
                            modifier = Modifier
                                .border(1.dp, PizzaBorder)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar"),
                            tonalElevation = 8.dp
                        ) {
                            MainScreen.entries.forEach { screen ->
                                val isSelected = currentScreen == screen
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.title,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PizzaOrange,
                                        selectedTextColor = PizzaOrange,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = PizzaOrange.copy(alpha = 0.18f)
                                    ),
                                    modifier = Modifier.testTag(screen.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            MainScreen.COMANDAS -> ComandasScreen(viewModel = posViewModel)
                            MainScreen.VENTAS -> VentasScreen(viewModel = posViewModel)
                            MainScreen.PRODUCTOS -> ProductosScreen(viewModel = posViewModel)
                        }
                    }
                }
            }
        }
    }
}
