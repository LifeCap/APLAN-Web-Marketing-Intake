package com.aplan.shoealerts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aplan.shoealerts.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var smsNumberDraft by remember(settings.defaultSmsNumber) {
        mutableStateOf(settings.defaultSmsNumber)
    }
    var intervalExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // ── Alert Preferences ──────────────────────────────────────────
            item { SectionHeader(Icons.Default.Notifications, "Alert Preferences") }

            item {
                SettingsCard {
                    Column {
                        Text(
                            "Price Alert Threshold: \$${String.format("%.0f", settings.defaultPriceThreshold)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Alert when price drops at or below this amount",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = settings.defaultPriceThreshold.toFloat(),
                            onValueChange = { viewModel.setPriceThreshold(it.toDouble()) },
                            valueRange = 1f..100f,
                            steps = 98,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("\$1", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("\$100", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                SettingsCard {
                    SwitchRow(
                        icon = Icons.Default.FilterList,
                        title = "Show Only Under-Threshold Deals",
                        subtitle = "Deals screen defaults to the price-drop filter",
                        checked = settings.showOnlyUnderThreshold,
                        onCheckedChange = { viewModel.setShowOnlyUnderThreshold(it) }
                    )
                }
            }

            // ── SMS Alerts ─────────────────────────────────────────────────
            item { SectionHeader(Icons.Default.Sms, "SMS Alerts") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Enter your phone number to receive deal alerts as text messages. " +
                            "Enable SMS alerts per search profile in the Search tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = smsNumberDraft,
                            onValueChange = { smsNumberDraft = it },
                            label = { Text("SMS Phone Number") },
                            placeholder = { Text("+1XXXXXXXXXX") },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (smsNumberDraft != settings.defaultSmsNumber) {
                                    TextButton(onClick = { viewModel.setSmsNumber(smsNumberDraft) }) {
                                        Text("Save")
                                    }
                                }
                            },
                            supportingText = {
                                Text("Include country code, e.g. +1 for US numbers")
                            }
                        )
                    }
                }
            }

            // ── Background Search ──────────────────────────────────────────
            item { SectionHeader(Icons.Default.Schedule, "Background Search") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "How often ShoeAlert checks for new deals in the background",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ExposedDropdownMenuBox(
                            expanded = intervalExpanded,
                            onExpandedChange = { intervalExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = intervalLabel(settings.defaultSearchIntervalHours),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Check Interval") },
                                leadingIcon = { Icon(Icons.Default.Timer, null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(intervalExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = intervalExpanded,
                                onDismissRequest = { intervalExpanded = false }
                            ) {
                                listOf(1, 2, 4, 6, 12, 24).forEach { h ->
                                    DropdownMenuItem(
                                        text = { Text(intervalLabel(h)) },
                                        onClick = {
                                            viewModel.setSearchInterval(h)
                                            intervalExpanded = false
                                        },
                                        trailingIcon = {
                                            if (settings.defaultSearchIntervalHours == h)
                                                Icon(Icons.Default.Check, null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Appearance ─────────────────────────────────────────────────
            item { SectionHeader(Icons.Default.Palette, "Appearance") }

            item {
                SettingsCard {
                    Column {
                        SwitchRow(
                            icon = Icons.Default.DarkMode,
                            title = "Dark Theme",
                            subtitle = "Use dark background across the app",
                            checked = settings.useDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        SwitchRow(
                            icon = Icons.Default.ColorLens,
                            title = "Dynamic Color",
                            subtitle = "Use colors from your wallpaper (Android 12+)",
                            checked = settings.useDynamicColor,
                            onCheckedChange = { viewModel.setDynamicColor(it) }
                        )
                    }
                }
            }

            // ── About ───────────────────────────────────────────────────────
            item { SectionHeader(Icons.Default.Info, "About") }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AboutRow("App", "ShoeAlert")
                        AboutRow("Version", "1.0.0")
                        AboutRow("Sources", "Amazon · Poshmark · Shein · eBay · Walmart")
                        AboutRow("Focus", "Enclosed-toe, rubber-soled shoes under your price threshold")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun intervalLabel(hours: Int) = when (hours) {
    1 -> "Every hour"
    24 -> "Once a day"
    else -> "Every $hours hours"
}
