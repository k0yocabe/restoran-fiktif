package com.example.restoranfiktif.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.restoranfiktif.data.MenuData
import com.example.restoranfiktif.data.PreferenceManager
import com.example.restoranfiktif.ui.Screen
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, preferenceManager: PreferenceManager) {
    val context = LocalContext.current
    val profile = remember { preferenceManager.getProfile() }
    val carouselItems = listOf(
        Color(0xFFFFCDD2), // Red 100
        Color(0xFFBBDEFB), // Blue 100
        Color(0xFFC8E6C9), // Green 100
        Color(0xFFFFF9C4)  // Yellow 100
    )

    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 50
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    
    var favoriteUpdateKey by remember { mutableStateOf(0) }
    var cartUpdateKey by remember { mutableStateOf(0) }
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Double Back to Exit Logic
    var backPressedTime by remember { mutableLongStateOf(0L) }
    BackHandler {
        if (isSearchActive || searchQuery.isNotEmpty()) {
            isSearchActive = false
            searchQuery = ""
            focusManager.clearFocus()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - backPressedTime < 2000) {
                (context as? Activity)?.finish()
            } else {
                backPressedTime = currentTime
                Toast.makeText(context, "Tekan sekali lagi untuk keluar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val filteredMenu = remember(searchQuery) {
        if (searchQuery.trim().isEmpty()) {
            MenuData.listMenu
        } else {
            MenuData.listMenu.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            delay(100)
            focusRequester.requestFocus()
        }
    }

    val isExpanded = isScrolled || isSearchActive || searchQuery.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 110.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            item {
                Column {
                    Text(text = "Selamat Datang,", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (searchQuery.trim().isEmpty() && !isSearchActive) {
                item {
                    Text(text = "Rekomendasi Spesial", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))
                    LazyRow(modifier = Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(carouselItems) { color ->
                            Card(
                                modifier = Modifier.width(260.dp).fillMaxHeight(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize().background(color), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                        Text("Menu Promo", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text(text = "Menu Populer", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 12.dp))
                }
            } else {
                item {
                    Text(text = if (searchQuery.trim().isEmpty()) "Cari menu favoritmu..." else "Hasil Pencarian: \"$searchQuery\"", fontSize = 16.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 12.dp))
                }
            }

            // Grid layout for menu items (2 columns)
            val chunkedMenu = filteredMenu.chunked(2)
            items(chunkedMenu) { rowItems ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { item ->
                        key(item.id, favoriteUpdateKey, cartUpdateKey) {
                            MenuGridItem(
                                item = item,
                                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                                isFavorite = preferenceManager.isFavorite(item.id),
                                quantity = preferenceManager.getItemQuantity(item.id),
                                onFavoriteClick = {
                                    preferenceManager.toggleFavorite(item.id)
                                    favoriteUpdateKey++
                                },
                                onAddClick = {
                                    preferenceManager.addToCart(item.id)
                                    cartUpdateKey++
                                },
                                onRemoveClick = {
                                    preferenceManager.removeFromCart(item.id)
                                    cartUpdateKey++
                                },
                                onClick = { navController.navigate(Screen.DetailMenu.createRoute(item.id)) }
                            )
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            if (filteredMenu.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Menu tidak ditemukan", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }

        // Header with Search
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isExpanded) 0.95f else 0f),
            tonalElevation = if (isExpanded) 4.dp else 0.dp
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SearchTransition"
                ) { expanded ->
                    if (expanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().height(48.dp).focusRequester(focusRequester).onFocusChanged { if (it.isFocused) isSearchActive = true },
                            placeholder = { Text("Cari menu...", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { 
                                    if (searchQuery.isNotEmpty()) {
                                        searchQuery = "" 
                                    } else {
                                        isSearchActive = false
                                        focusManager.clearFocus()
                                    }
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSearchActive = true }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuGridItem(
    item: com.example.restoranfiktif.data.MenuItem,
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    quantity: Int,
    onFavoriteClick: () -> Unit,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f), MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                            )
                        )
                )
                
                Icon(
                    imageVector = Icons.Default.Fastfood,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.Center),
                    tint = Color.White.copy(alpha = 0.9f)
                )

                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFFFFB300))
                    Text(text = item.rating.toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                
                // Add/Quantity Button
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (quantity == 0) {
                        Surface(
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onAddClick() },
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                IconButton(onClick = onRemoveClick, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                                Text(
                                    text = quantity.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(onClick = onAddClick, modifier = Modifier.size(20.dp)) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.align(Alignment.TopStart).size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.price,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}
