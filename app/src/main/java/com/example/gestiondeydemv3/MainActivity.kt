package com.example.gestiondeydemv3

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import com.example.gestiondeydemv3.ui.theme.GestionDeydemV3Theme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestionDeydemV3Theme {
                AdminDashboardApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun AdminDashboardApp() {

    var currentDestination by rememberSaveable {
        mutableStateOf(AdminDestinations.DASHBOARD)
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AdminDestinations.entries.forEach { destination ->
                item(
                    icon = {
                        Icon(
                            destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentDestination) {
                    AdminDestinations.DASHBOARD -> DashboardScreen()
                    AdminDestinations.DRIVERS -> DriversScreen()
                    AdminDestinations.PROFILE -> AdminProfileScreen()
                    AdminDestinations.NOTIFICATIONS -> CreateNotificationScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNotificationScreen() {

    val context = LocalContext.current // 🔹 CONTEXT COMPOSABLE

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var actionUrl by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var type by remember { mutableStateOf("info") }
    var isActive by remember { mutableStateOf(true) }

    val types = listOf("urgent", "update", "info")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("🔔 Envoyer une notification",
            style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = type,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                types.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            type = it
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = actionUrl,
            onValueChange = { actionUrl = it },
            label = { Text("Lien (optionnel)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notification active")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isActive,
                onCheckedChange = { isActive = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                sendNotification(
                    context,
                    title, message, type, actionUrl, isActive
                )
            }
        ) {
            Text("📤 Envoyer")
        }
    }
}

// ✅ Maintenant le contexte est passé correctement
fun sendNotification(
    context: android.content.Context,
    title: String,
    message: String,
    type: String,
    actionUrl: String,
    isActive: Boolean
) {
    val url = "https://pisco.alwaysdata.net/create_notification.php"

    val request = object : StringRequest(
        Method.POST,
        url,
        { response ->
            // succès
            Toast.makeText(context, "Notification envoyée ✅", Toast.LENGTH_SHORT).show()
        },
        { error ->
            // erreur
            Toast.makeText(context, "Erreur: ${error.message}", Toast.LENGTH_LONG).show()
        }
    ) {
        override fun getParams(): Map<String, String> {
            return mapOf(
                "title" to title,
                "message" to message,
                "type" to type,
                "action_url" to actionUrl,
                "is_active" to if (isActive) "1" else "0"
            )
        }
    }

    Volley.newRequestQueue(context).add(request)
}





enum class AdminDestinations(
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD("Dashboard", Icons.Default.Home),
    DRIVERS("Chauffeurs", Icons.Default.Person),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    PROFILE("Profil", Icons.Default.AccountBox),
}

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("📊 Dashboard Admin", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("• Chauffeurs en attente : 12")
        Text("• Chauffeurs validés : 45")
        Text("• Chauffeurs rejetés : 3")
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriversScreen(
    viewModel: DriverViewModel = viewModel()
) {
    val loading by viewModel.loading
    val drivers = viewModel.filteredDrivers()
    val totalDrivers = viewModel.drivers.size

    var search by remember { mutableStateOf("") }
    var selectedDriver by remember { mutableStateOf<Driver?>(null) }

    // ================= AUTO REFRESH 30s =================
//    LaunchedEffect(Unit) {
//        while (true) {
//            viewModel.loadDrivers()
//            kotlinx.coroutines.delay(30_000)
//        }
//    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // ================= HEADER =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🚖 Chauffeurs",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "👥 Inscrits : $totalDrivers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = { viewModel.loadDrivers() },
                enabled = !loading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ================= SEARCH =================
        SearchBar(
            query = search,
            onQueryChange = {
                search = it
                viewModel.searchQuery.value = it
            },
            onSearch = {},
            active = false,
            onActiveChange = {},
            placeholder = { Text("Rechercher par numéro") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth()
        ) {}

        Spacer(modifier = Modifier.height(12.dp))

        // ================= CONTENT =================
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(
                    items = drivers,
                    key = { it.id }
                ) { driver ->
                    DriverItem(
                        driver = driver,
                        onApprove = {
                            viewModel.approveDriver(driver.id)
                        },
                        onUpdateSolde = {
                            selectedDriver = it
                        }
                    )
                }
            }
        }
    }

    // ================= DIALOG SOLDE =================
    selectedDriver?.let { driver ->
        UpdateSoldeDialog(
            driver = driver,
            onDismiss = { selectedDriver = null },
            onConfirm = { newSolde ->
                viewModel.updateSolde(driver.id, newSolde)
                selectedDriver = null
            }
        )
    }
}






@Composable
fun UpdateSoldeDialog(
    driver: Driver,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var solde by remember { mutableStateOf(driver.solde.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le solde") },
        text = {
            Column {
                Text("📞 ${driver.phone}")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = solde,
                    onValueChange = { solde = it },
                    label = { Text("Nouveau solde (FCFA)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(solde.toIntOrNull() ?: driver.solde)
            }) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}



@Composable
fun DriverItem(
    driver: Driver,
    onApprove: (Driver) -> Unit,
    onUpdateSolde: (Driver) -> Unit
) {

    val isActive =
        driver.docsStatus == "approved" && driver.status == "active"

    val statusText = when {
        driver.bloque == 1 ->
            "⛔ Chauffeur bloqué"

        driver.docsStatus == "rejected" ->
            "❌ Documents rejetés"

        driver.docsStatus == "pending" || driver.docsStatus == "send" ->
            "📄 Documents en attente"

        isActive ->
            "✅ Chauffeur actif"

        else ->
            "⚠️ Compte inactif"
    }

    val statusColor = when {
        isActive -> Color(0xFF4CAF50)
        driver.bloque == 1 -> Color.Red
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text("📞 ${driver.phone}", fontWeight = FontWeight.Bold)
            Text("💰 Solde : ${driver.solde} FCFA")
            Text(if (driver.isOnline == 1) "🟢 En ligne" else "🔴 Hors ligne")

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = statusText,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                // 👉 Bouton approuver docs
                if (driver.docsStatus == "pending" || driver.docsStatus == "send") {
                    Button(
                        onClick = { onApprove(driver) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Approuver docs")
                    }
                }

                OutlinedButton(
                    onClick = { onUpdateSolde(driver) }
                ) {
                    Text("💰 Modifier solde")
                }
            }
        }
    }
}




@Composable
fun AdminProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("👤 Profil Admin", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Nom : Administrateur")
        Text("Email : admin@deydem.com")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { /* logout */ }) {
            Text("Déconnexion")
        }
    }
}
