package com.example.restoranfiktif.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.restoranfiktif.data.PreferenceManager
import com.example.restoranfiktif.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    preferenceManager: PreferenceManager,
    onThemeChange: (Boolean) -> Unit
) {
    var profile by remember { mutableStateOf(preferenceManager.getProfile()) }
    var isDark by remember { mutableStateOf(preferenceManager.isDarkTheme()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Restoran") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.EditProfile.route) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ProfileItem(label = "Nama Restoran", value = profile.name)
            ProfileItem(label = "Alamat", value = profile.address)
            ProfileItem(label = "Deskripsi", value = profile.description)
            ProfileItem(label = "Jam Buka", value = profile.openingHours)

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Tema Gelap", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Switch(
                    checked = isDark,
                    onCheckedChange = {
                        isDark = it
                        onThemeChange(it)
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
