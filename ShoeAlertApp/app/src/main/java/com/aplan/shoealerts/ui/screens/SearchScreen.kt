package com.aplan.shoealerts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aplan.shoealerts.data.model.*
import com.aplan.shoealerts.viewmodel.SearchViewModel
import com.aplan.shoealerts.worker.DealSearchWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var editingPref by remember { mutableStateOf<SearchPreference?>(null) }
    var snackMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Preferences") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New Search") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.preferences.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("No searches yet", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to create a search profile with your shoe size, style, and price alert preferences.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.preferences, key = { it.id }) { pref ->
                    SearchPreferenceCard(
                        preference = pref,
                        isSearching = uiState.isSearching,
                        onEdit = { editingPref = pref },
                        onDelete = { viewModel.deletePreference(pref.id) },
                        onToggleActive = { viewModel.togglePreferenceActive(pref.id, !pref.isActive) },
                        onSearchNow = {
                            viewModel.runManualSearch(pref) { count ->
                                snackMessage = if (count > 0) "Found $count new deals!" else "No new deals found"
                            }
                            DealSearchWorker.scheduleImmediateSearch(context)
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog || editingPref != null) {
        SearchPreferenceDialog(
            existing = editingPref,
            onDismiss = {
                showAddDialog = false
                editingPref = null
            },
            onSave = { pref ->
                viewModel.savePreference(pref)
                DealSearchWorker.schedulePeriodicWork(context, pref.searchIntervalHours)
                showAddDialog = false
                editingPref = null
                snackMessage = "Search \"${pref.name}\" saved!"
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPreferenceCard(
    preference: SearchPreference,
    isSearching: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onSearchNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (preference.isActive)
                MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preference.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Size ${preference.shoeSize} • ${preference.style.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = preference.isActive, onCheckedChange = { onToggleActive() })
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(Icons.Default.AttachMoney, "Alert: \$${String.format("%.0f", preference.maxPriceThreshold)}")
                InfoChip(Icons.Default.Schedule, "Every ${preference.searchIntervalHours}h")
                if (preference.smsAlerts) InfoChip(Icons.Default.Sms, "SMS")
                if (preference.pushNotifications) InfoChip(Icons.Default.Notifications, "Push")
            }

            Spacer(Modifier.height(8.dp))

            // Enabled sources
            val sources = preference.enabledSourcesList()
            Text(
                text = "Sources: ${sources.joinToString(", ") { it.displayName }}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onSearchNow,
                    enabled = preference.isActive && !isSearching,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Search Now")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPreferenceDialog(
    existing: SearchPreference?,
    onDismiss: () -> Unit,
    onSave: (SearchPreference) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "My Shoe Search") }
    var shoeSize by remember { mutableStateOf(existing?.shoeSize ?: "") }
    var maxPrice by remember { mutableStateOf(existing?.maxPriceThreshold?.toString() ?: "15.0") }
    var style by remember { mutableStateOf(existing?.style ?: ShoeStyle.ANY) }
    var gender by remember { mutableStateOf(existing?.gender ?: "unisex") }
    var brand by remember { mutableStateOf(existing?.brand ?: "") }
    var condition by remember { mutableStateOf(existing?.condition ?: "any") }
    var pushEnabled by remember { mutableStateOf(existing?.pushNotifications ?: true) }
    var smsEnabled by remember { mutableStateOf(existing?.smsAlerts ?: false) }
    var smsNumber by remember { mutableStateOf(existing?.smsPhoneNumber ?: "") }
    var newDealAlerts by remember { mutableStateOf(existing?.newDealAlerts ?: true) }
    var priceDropAlerts by remember { mutableStateOf(existing?.priceDropAlerts ?: true) }
    var intervalHours by remember { mutableStateOf(existing?.searchIntervalHours?.toString() ?: "4") }
    var enabledSources by remember {
        mutableStateOf(
            existing?.enabledSourcesList()?.toMutableSet()
                ?: ShoppingSource.values().toMutableSet()
        )
    }
    var styleExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        title = { Text(if (existing == null) "New Shoe Search" else "Edit Search") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Search Name") },
                        leadingIcon = { Icon(Icons.Default.Label, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = shoeSize,
                        onValueChange = { shoeSize = it },
                        label = { Text("Shoe Size (e.g. 9, 10.5, W8)") },
                        leadingIcon = { Icon(Icons.Default.Straighten, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Max Price Alert (\$)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Get alerted when price drops below this") }
                    )
                }
                item {
                    ExposedDropdownMenuBox(
                        expanded = styleExpanded,
                        onExpandedChange = { styleExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = style.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Shoe Style") },
                            leadingIcon = { Icon(Icons.Default.Style, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(styleExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = styleExpanded, onDismissRequest = { styleExpanded = false }) {
                            ShoeStyle.values().forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.displayName) },
                                    onClick = { style = s; styleExpanded = false }
                                )
                            }
                        }
                    }
                }
                item {
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = gender.replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                            listOf("unisex", "womens", "mens").forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.replaceFirstChar { it.uppercase() }) },
                                    onClick = { gender = g; genderExpanded = false }
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand (optional)") },
                        leadingIcon = { Icon(Icons.Default.Bookmark, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Text("Sources to Search", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    ShoppingSource.values().forEach { source ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = source in enabledSources,
                                onCheckedChange = { checked ->
                                    enabledSources = enabledSources.toMutableSet().also {
                                        if (checked) it.add(source) else it.remove(source)
                                    }
                                }
                            )
                            Text(source.displayName)
                        }
                    }
                }
                item {
                    Text("Alert Settings", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = newDealAlerts, onCheckedChange = { newDealAlerts = it })
                        Text("Alert on new deals")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = priceDropAlerts, onCheckedChange = { priceDropAlerts = it })
                        Text("Alert on price drops")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = pushEnabled, onCheckedChange = { pushEnabled = it })
                        Text("Push notifications")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = smsEnabled, onCheckedChange = { smsEnabled = it })
                        Text("SMS text alerts")
                    }
                }
                if (smsEnabled) {
                    item {
                        OutlinedTextField(
                            value = smsNumber,
                            onValueChange = { smsNumber = it },
                            label = { Text("SMS Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            supportingText = { Text("Include country code: +1XXXXXXXXXX") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = intervalHours,
                        onValueChange = { intervalHours = it },
                        label = { Text("Check Every (hours)") },
                        leadingIcon = { Icon(Icons.Default.Timer, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Minimum: 1 hour (recommended: 4-6)") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pref = SearchPreference(
                        id = existing?.id ?: 0,
                        name = name.ifBlank { "Shoe Search" },
                        shoeSize = shoeSize.trim(),
                        style = style,
                        maxPriceThreshold = maxPrice.toDoubleOrNull() ?: 15.0,
                        pushNotifications = pushEnabled,
                        smsAlerts = smsEnabled,
                        smsPhoneNumber = smsNumber.trim(),
                        newDealAlerts = newDealAlerts,
                        priceDropAlerts = priceDropAlerts,
                        searchIntervalHours = intervalHours.toIntOrNull()?.coerceAtLeast(1) ?: 4,
                        enabledSources = enabledSources.joinToString(",") { it.name },
                        gender = gender,
                        brand = brand.trim(),
                        condition = condition
                    )
                    onSave(pref)
                },
                enabled = shoeSize.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
