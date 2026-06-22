package com.aplan.shoealerts.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aplan.shoealerts.ui.components.DealCard
import com.aplan.shoealerts.viewmodel.DealsFilter
import com.aplan.shoealerts.viewmodel.DealsViewModel
import com.aplan.shoealerts.worker.DealSearchWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    viewModel: DealsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shoe Deals") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = {
                        DealSearchWorker.scheduleImmediateSearch(context)
                        viewModel.refresh()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Search Now")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    DealsFilter.ALL to "All Deals",
                    DealsFilter.UNDER_THRESHOLD to "Under $15",
                    DealsFilter.FAVORITES to "Favorites",
                    DealsFilter.BY_SOURCE to "By Store"
                )
                items(filters) { (filter, label) ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(label) },
                        leadingIcon = when (filter) {
                            DealsFilter.ALL -> {{ Icon(Icons.Default.List, null, Modifier.size(16.dp)) }}
                            DealsFilter.UNDER_THRESHOLD -> {{ Icon(Icons.Default.LocalOffer, null, Modifier.size(16.dp)) }}
                            DealsFilter.FAVORITES -> {{ Icon(Icons.Default.Favorite, null, Modifier.size(16.dp)) }}
                            DealsFilter.BY_SOURCE -> {{ Icon(Icons.Default.Store, null, Modifier.size(16.dp)) }}
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Searching for deals...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (uiState.deals.isEmpty()) {
                EmptyDealsState(context)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.deals, key = { it.id }) { deal ->
                        DealCard(
                            deal = deal,
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onDelete = { viewModel.deleteDeal(it) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // Error snackbar
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun EmptyDealsState(context: Context) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No deals found yet",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Set up a search in the Search tab, then tap the refresh button or wait for the background search to run.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = {
                DealSearchWorker.scheduleImmediateSearch(context)
            }) {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Search Now")
            }
        }
    }
}
