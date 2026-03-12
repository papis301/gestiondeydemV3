package com.example.gestiondeydemv3

import ClientViewModel
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
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
        mutableStateOf(AdminDestinations.CLIENTS)
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
                    AdminDestinations.CLIENTS -> ClientsScreen()
                    AdminDestinations.DRIVERS -> DriversScreen()
                    AdminDestinations.PARTNERS -> PartnersScreen()
                    AdminDestinations.NOTIFICATIONS -> CreateNotificationScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnersScreen(
    viewModel: PartnerViewModel = viewModel()
) {

    val loading by viewModel.loading
    val partners = viewModel.partners

    var showDialog by remember { mutableStateOf(false) }
    var selectedPartnerHistory by remember { mutableStateOf<Partner?>(null) }

    // 👉 Si on ouvre l'historique
    if (selectedPartnerHistory != null) {

        PartnerHistoryScreen(
            partner = selectedPartnerHistory!!,
            viewModel = viewModel,
            onBack = { selectedPartnerHistory = null }
        )

        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🤝 Partenaires", style = MaterialTheme.typography.headlineMedium)
                Text("Total : ${partners.size}")
            }

            IconButton(onClick = { viewModel.loadPartners() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Ajouter un partenaire")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

        } else {

            LazyColumn {

                items(partners, key = { it.id }) { partner ->

                    PartnerItem(
                        partner = partner,
                        viewModel = viewModel,
                        onHistoryClick = {
                            selectedPartnerHistory = it
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddPartnerDialog(
            onDismiss = { showDialog = false },
            onCreate = { nom, email, password, telephone, adresse, commission ->

                viewModel.createPartner(
                    nom,
                    email,
                    password,
                    telephone,
                    adresse,
                    commission
                )

                showDialog = false
            }
        )
    }
}

@Composable
fun AddPartnerDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String, Int) -> Unit
) {

    var nom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("20") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                onCreate(
                    nom,
                    email,
                    password,
                    telephone,
                    adresse,
                    commission.toIntOrNull() ?: 20
                )
            }) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        title = { Text("Nouveau partenaire") },
        text = {
            Column {

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (login)") }
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mot de passe") }
                )

                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone") }
                )

                OutlinedTextField(
                    value = adresse,
                    onValueChange = { adresse = it },
                    label = { Text("Adresse") }
                )

                OutlinedTextField(
                    value = commission,
                    onValueChange = { commission = it },
                    label = { Text("Commission %") }
                )
            }
        }
    )
}

@Composable
fun PartnerItem(
    partner: Partner,
    viewModel: PartnerViewModel,
    onHistoryClick: (Partner) -> Unit
) {

    var showDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                "🏢 ${partner.nom}",
                fontWeight = FontWeight.Bold
            )

            Text("📧 ${partner.email}")
            Text("📞 ${partner.telephone}")
            Text("📍 ${partner.adresse}")

            Spacer(modifier = Modifier.height(6.dp))

            Text("💰 Commission : ${partner.commissionPercent}%")

            Text(
                "💳 Solde : ${partner.solde} FCFA",
                color = if (partner.solde > 0)
                    Color(0xFF2E7D32)
                else
                    Color.Red
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Modifier le solde")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Modifier")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = { onHistoryClick(partner) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Historique")
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = { viewModel.deletePartner(partner.id) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Supprimer partenaire")
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "📅 Créé le : ${partner.createdAt}",
                color = Color.Gray
            )
        }
    }

    if (showDialog) {
        UpdatePartnerSoldeDialog(
            partner = partner,
            viewModel = viewModel,
            onDismiss = { showDialog = false }
        )
    }

    if (showEditDialog) {
        EditPartnerDialog(
            partner = partner,
            viewModel = viewModel,
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun EditPartnerDialog(
    partner: Partner,
    viewModel: PartnerViewModel,
    onDismiss: () -> Unit
) {

    var nom by remember { mutableStateOf(partner.nom) }
    var email by remember { mutableStateOf(partner.email) }
    var telephone by remember { mutableStateOf(partner.telephone) }
    var adresse by remember { mutableStateOf(partner.adresse) }
    var commission by remember { mutableStateOf(partner.commissionPercent.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                viewModel.updatePartner(
                    id = partner.id,
                    nom = nom,
                    email = email,
                    telephone = telephone,
                    adresse = adresse,
                    commission = commission.toIntOrNull() ?: 20
                )
                onDismiss()
            }) {
                Text("Modifier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        title = { Text("Modifier partenaire") },
        text = {
            Column {

                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text("Nom") }
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )

                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone") }
                )

                OutlinedTextField(
                    value = adresse,
                    onValueChange = { adresse = it },
                    label = { Text("Adresse") }
                )

                OutlinedTextField(
                    value = commission,
                    onValueChange = { commission = it },
                    label = { Text("Commission %") }
                )
            }
        }
    )
}

@Composable
fun UpdatePartnerSoldeDialog(
    partner: Partner,
    viewModel: PartnerViewModel,
    onDismiss: () -> Unit
) {

    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("add") } // add ou remove

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Modifier le solde")
        },
        confirmButton = {
            Button(
                onClick = {

                    val value = amount.toIntOrNull()

                    if (value != null && value > 0) {
                        viewModel.updatePartnerSolde(
                            partnerId = partner.id,
                            amount = value,
                            type = type
                        )
                        onDismiss()
                    }
                }
            ) {
                Text("Valider")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        text = {
            Column {

                Text("Partenaire : ${partner.nom}")
                Spacer(modifier = Modifier.height(8.dp))

                Text("Solde actuel : ${partner.solde} FCFA")

                Spacer(modifier = Modifier.height(12.dp))

                // Choix action
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    FilterChip(
                        selected = type == "add",
                        onClick = { type = "add" },
                        label = { Text("Ajouter") }
                    )

                    FilterChip(
                        selected = type == "remove",
                        onClick = { type = "remove" },
                        label = { Text("Retirer") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Montant") },
                    singleLine = true
                )
            }
        }
    )
}

@Composable
fun ClientItem(client: Client) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text("👤 ${client.name}", fontWeight = FontWeight.Bold)
            Text("📞 ${client.phone}")
            Text("📅 Inscrit le : ${client.createdAt}")

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


@Composable
fun ClientsScreen(
    viewModel: ClientViewModel = viewModel()
) {
    val loading by viewModel.loading
    val clients = viewModel.clients
    val totalClients = clients.size

    // AUTO REFRESH
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.fetchClients()
            kotlinx.coroutines.delay(30000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("👥 Clients", style = MaterialTheme.typography.headlineMedium)
                Text("Total inscrits : $totalClients")
            }

            IconButton(onClick = { viewModel.fetchClients() }) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(clients, key = { it.id }) { client ->
                    ClientItem(client)
                }
            }
        }
    }
}



enum class AdminDestinations(
    val label: String,
    val icon: ImageVector,
) {
    CLIENTS("Clients", Icons.Default.Person),
    DRIVERS("Chauffeurs", Icons.Default.Person),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    PARTNERS("Partenaires", Icons.Default.AccountBox)
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
    val totalDrivers = drivers.size   // ✅ Compteur dynamique

    var search by remember { mutableStateOf("") }
    var selectedDriver by remember { mutableStateOf<Driver?>(null) }
    var selectedDriverDetails by remember { mutableStateOf<Driver?>(null) }
    val currentFilter by viewModel.filterStatus

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
                    text = "👥 Affichés : $totalDrivers",
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

        // ================= FILTER =================
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            FilterChip(
                selected = currentFilter == "all",
                onClick = { viewModel.filterStatus.value = "all" },
                label = { Text("Tous") }
            )

            FilterChip(
                selected = currentFilter == "active",
                onClick = { viewModel.filterStatus.value = "active" },
                label = { Text("Actifs") }
            )

            FilterChip(
                selected = currentFilter == "blocked",
                onClick = { viewModel.filterStatus.value = "blocked" },
                label = { Text("Bloqués") }
            )
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
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
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
                        },
                        onDriverClick = {
                            selectedDriverDetails = it
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

    // ================= DIALOG DETAILS =================
    selectedDriverDetails?.let { driver ->
        DriverDetailsDialog(
            driver = driver,
            viewModel = viewModel,
            onDismiss = { selectedDriverDetails = null }
        )
    }
}

@Composable
fun DriverDetailsDialog(
    driver: Driver,
    viewModel: DriverViewModel,
    onDismiss: () -> Unit
){

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column {

                Button(
                    onClick = {
                        viewModel.toggleBlockDriver(driver.id)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (driver.bloque == 1)
                            Color(0xFF4CAF50) // vert = débloquer
                        else
                            Color.Red // rouge = bloquer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (driver.bloque == 1)
                            "Débloquer le chauffeur"
                        else
                            "Bloquer le chauffeur"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Fermer")
                }
            }
        },
        title = {
            Text("👤 Détails Chauffeur")
        },
        text = {
            Column {

                Text("📞 Téléphone : ${driver.phone}")
                Spacer(modifier = Modifier.height(6.dp))

                Text("💰 Solde : ${driver.solde} FCFA")
                Spacer(modifier = Modifier.height(6.dp))

                Text("🚗 Véhicule : ${driver.typeVehicule}")
                Spacer(modifier = Modifier.height(6.dp))

                Text("📊 Total courses : ${driver.totalCourses}")
                Spacer(modifier = Modifier.height(6.dp))

                Text("⭐ Rating : ${driver.ratingAverage}")
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    if (driver.isOnline == 1)
                        "🟢 En ligne"
                    else
                        "🔴 Hors ligne"
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    if (driver.bloque == 1)
                        "⛔ Bloqué par admin"
                    else
                        "✅ Actif"
                )
                Spacer(modifier = Modifier.height(6.dp))

                Text("📄 Documents : ${driver.docsStatus}")
                Spacer(modifier = Modifier.height(6.dp))

                Text("📅 Inscrit le : ${driver.createdAt}")
            }
        }
    )
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
    onUpdateSolde: (Driver) -> Unit,
    onDriverClick: (Driver) -> Unit
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
            .padding(vertical = 6.dp)
        .clickable { onDriverClick(driver) },
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

@Composable
fun PartnerHistoryScreen(
    partner: Partner,
    viewModel: PartnerViewModel = viewModel(),
    onBack: () -> Unit
) {

    val transactions = viewModel.transactions

    LaunchedEffect(Unit) {
        viewModel.loadPartnerTransactions(partner.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Historique - ${partner.nom}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {

            items(transactions) { t ->

                TransactionItem(t)
            }
        }
    }
}

@Composable
fun TransactionItem(t: PartnerTransaction) {

    val color = if (t.type == "add")
        Color(0xFF2E7D32)
    else
        Color.Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = if (t.type == "add")
                    "➕ Ajout de solde"
                else
                    "➖ Retrait de solde",
                color = color,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Montant : ${t.amount} FCFA")

            Text("Solde : ${t.oldSolde} → ${t.newSolde}")

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = t.createdAt,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


