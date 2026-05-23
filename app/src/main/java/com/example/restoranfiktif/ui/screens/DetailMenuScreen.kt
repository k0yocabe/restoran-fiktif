package com.example.restoranfiktif.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.restoranfiktif.data.MenuData
import com.example.restoranfiktif.data.PreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMenuScreen(navController: NavController, menuId: Int, preferenceManager: PreferenceManager) {
    val menuItem = remember { MenuData.listMenu.find { it.id == menuId } }
    var rating by remember { mutableIntStateOf(0) }
    var cartUpdateKey by remember { mutableStateOf(0) }
    var showCartSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (menuItem == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Text("Menu tidak ditemukan", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    // Refresh cart data whenever cartUpdateKey changes
    val cartData = remember(cartUpdateKey) { preferenceManager.getCart() }
    val quantity = cartData[menuId.toString()] ?: 0
    val totalCartItems = cartData.values.sum()
    val totalPrice = cartData.entries.sumOf { (id, qty) ->
        val item = MenuData.listMenu.find { it.id.toString() == id }
        val priceInt = item?.price?.replace("Rp ", "")?.replace(".", "")?.toInt() ?: 0
        priceInt * qty
    }

    Scaffold(
        bottomBar = {
            if (totalCartItems > 0 && !showCartSheet) {
                CartSummaryBar(
                    totalItems = totalCartItems,
                    totalPrice = totalPrice,
                    onCartClick = { showCartSheet = true },
                    onCheckoutClick = { navController.navigate(com.example.restoranfiktif.ui.Screen.Cart.route) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(bottom = padding.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                )
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                        }
                        
                        Text(
                            text = "Detail Menu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.Fastfood,
                        contentDescription = null,
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.Center)
                            .padding(top = 40.dp),
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = menuItem.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = menuItem.category, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${menuItem.rating} • Populer", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        QuantityController(
                            quantity = quantity,
                            onAdd = {
                                preferenceManager.addToCart(menuId)
                                cartUpdateKey++
                            },
                            onRemove = {
                                preferenceManager.removeFromCart(menuId)
                                cartUpdateKey++
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = menuItem.price, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = menuItem.description, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 22.sp)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Berikan Rating:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Row {
                        for (i in 1..5) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp).clickable { rating = i },
                                tint = if (i <= rating) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            if (showCartSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCartSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    CartSheetContent(
                        preferenceManager = preferenceManager,
                        navController = navController,
                        onUpdate = { cartUpdateKey++ }
                    )
                }
            }
        }
    }
}

@Composable
fun QuantityController(quantity: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    if (quantity == 0) {
        Surface(
            modifier = Modifier.size(32.dp).clickable { onAdd() },
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(4.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimary)
        }
    } else {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.height(36.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Text(text = quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp), color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CartSummaryBar(totalItems: Int, totalPrice: Int, onCartClick: () -> Unit, onCheckoutClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.clickable { onCartClick() }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp)
                ) {
                    Text(text = totalItems.toString(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.wrapContentSize(Alignment.Center))
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 12.dp)) {
                Text(text = "Total Harga", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "Rp ${String.format("%,d", totalPrice).replace(",", ".")}", 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 16.sp, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Button(
                onClick = onCheckoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Text("Checkout", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CartSheetContent(preferenceManager: PreferenceManager, navController: NavController, onUpdate: () -> Unit) {
    val cartData = preferenceManager.getCart()
    val cartItems = MenuData.listMenu.filter { it.id.toString() in cartData.keys }
        .map { it to (cartData[it.id.toString()] ?: 0) }
    
    val totalItems = cartData.values.sum()
    val totalPrice = cartData.entries.sumOf { (id, qty) ->
        val item = MenuData.listMenu.find { it.id.toString() == id }
        val priceInt = item?.price?.replace("Rp ", "")?.replace(".", "")?.toInt() ?: 0
        priceInt * qty
    }

    Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Text(
            "Keranjang Saya", 
            fontWeight = FontWeight.Bold, 
            fontSize = 18.sp, 
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(modifier = Modifier.weight(1f, fill = false).padding(horizontal = 16.dp)) {
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Keranjang kosong", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(cartItems) { (item, qty) ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)))), 
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Fastfood, null, tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(item.price, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                            QuantityController(
                                quantity = qty,
                                onAdd = { 
                                    preferenceManager.addToCart(item.id)
                                    onUpdate() 
                                },
                                onRemove = { 
                                    preferenceManager.removeFromCart(item.id)
                                    onUpdate() 
                                }
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (totalItems > 0) {
            CartSummaryBar(
                totalItems = totalItems,
                totalPrice = totalPrice,
                onCartClick = { },
                onCheckoutClick = { 
                    navController.navigate(com.example.restoranfiktif.ui.Screen.Cart.route)
                }
            )
        }
    }
}
