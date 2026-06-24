package com.aplan.shoealerts.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.aplan.shoealerts.viewmodel.DealsSortOrder
import com.aplan.shoealerts.viewmodel.DealsViewModel
import com.aplan.shoealerts.worker.DealSearchWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealsScreen(
    onDealClick: (Long) -> Unit,
    viewModel: DealsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (searchActive) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.setSearchQuery(it) },
                    onSearch = { searchActive = false },
                    active = false,
                    onActiveChange = {},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    placeholder = { Text("Search deals…") },
                    leadingIcon = {
                        IconButton(onClick = {
                            searchActive = false
                            viewModel.clearSearch()
                        }) { Icon(Icons.Default.ArrowBack, "Close search") }
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    }
                ) {}
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text("Shoe Deals")
                            if (uiState.deals.isNotEmpty()) {
                                Text(
                                    "${uiState.deals.size} deal${if (uiState.deals.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, "Sort")
                            }
                            DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                                val sorts = listOf(
                                    DealsSortOrder.NEWEST to "Newest First",
                                    DealsSortOrder.PRICE_ASC to "Price: Low to High",
                                    DealsSortOrder.PRICE_DESC to "Price: High to Low",
                                    DealsSortOrder.SOURCE to "By Store"
                                )
                                sorts.forEach { (sort, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = { viewModel.setSortOrder(sort); showSortMenu = false },
                                        leadingIcon = {
                                            if (uiState.sortOrder == sort)
                                                Icon(Icons.Default.Check, null)
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, "Search Now")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    DealsFilter.ALL              to "All",
                    DealsFilter.UNDER_THRESHOLD  to "Under \$${uiState.priceThreshold.toInt()}",
                    DealsFilter.FAVORITES        to "Favorites",
                    DealsFilter.BY_SOURCE        to "By Store"
                )
                items(filters) { (filter, label) ->
                    FilterChip(
                        selected = uiState.filter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(label) }
                    )
                }
            }

            if (uiState.lastSearchTimestamp > 0L) {
                Text(
                    text = "Last checked: ${formatLastChecked(uiState.lastSearchTimestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 4.dp)
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize()
            ) {
                if (!uiState.isRefreshing && uiState.deals.isEmpty()) {
                    EmptyDealsState(context, uiState.searchQuery)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.deals, key = { it.id }) { deal ->
                            DealCard(
                                deal = deal,
                                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                                onDelete = { viewModel.deleteDeal(it) },
                                onClick = { onDealClick(deal.id) }
                            )
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

private fun formatLastChecked(timestamp: Long): String {
    val diffMs = System.currentTimeMillis() - timestamp
    val diffMin = diffMs / 60_000
    return when {
        diffMin < 1   -> "just now"
        diffMin < 60  -> "$diffMin min ago"
        diffMin < 1440 -> "${diffMin / 60}h ago"
        else          -> "${diffMin / 1440}d ago"
    }
}

@Composable
private fun EmptyDealsState(context: Context, query: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (query.isNotBlank()) Icons.Default.SearchOff else Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (query.isNotBlank()) "No results for \"$query\"" else "No deals found yet",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (query.isNotBlank())
                    "Try a different search term or clear the filter."
                else
                    "Set up a search in the Search tab, then pull down to refresh or tap the button below.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (query.isBlank()) {
                Spacer(Modifier.height(24.dp))
                FilledTonalButton(onClick = { DealSearchWorker.scheduleImmediateSearch(context) }) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Search Now")
                }
            }
        }
    }
}
