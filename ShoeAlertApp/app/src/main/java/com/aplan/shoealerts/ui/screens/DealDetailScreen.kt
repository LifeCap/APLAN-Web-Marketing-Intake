package com.aplan.shoealerts.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.aplan.shoealerts.data.model.PriceHistory
import com.aplan.shoealerts.viewmodel.DealDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealDetailScreen(
    onBack: () -> Unit,
    viewModel: DealDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.US) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deal Details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    state.deal?.let { deal ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (deal.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (deal.isFavorite) "Unfavorite" else "Favorite",
                                tint = if (deal.isFavorite) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, deal.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${deal.title}\n\$${String.format("%.2f", deal.price)} on ${deal.source.displayName}\n${deal.productUrl}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share deal"))
                        }) {
                            Icon(Icons.Default.Share, "Share deal")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val deal = state.deal
        if (deal == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Deal not found", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (deal.imageUrl != null) {
                    AsyncImage(
                        model = deal.imageUrl,
                        contentDescription = deal.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Source + condition row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.aplan.shoealerts.ui.components.SourceBadge(deal.source)
                    deal.condition?.let { cond ->
                        if (cond.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = cond.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                // Price block
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "\$${String.format("%.2f", deal.price)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        deal.originalPrice?.let { orig ->
                            if (orig > deal.price) {
                                Text(
                                    text = "\$${String.format("%.2f", orig)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textDecoration = TextDecoration.LineThrough
                                )
                                val savings = ((orig - deal.price) / orig * 100).toInt()
                                Text(
                                    text = "$savings% off",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        state.lowestPrice?.let { lowest ->
                            if (lowest < deal.price) {
                                Text(
                                    text = "Lowest seen: \$${String.format("%.2f", lowest)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // Details grid
                DetailsRow("Size", deal.size ?: "—")
                deal.brand?.let { DetailsRow("Brand", it) }
                DetailsRow("Store", deal.source.displayName)
                DetailsRow("Found", dateFormat.format(Date(deal.foundAt)))

                Spacer(Modifier.height(20.dp))

                // Price history chart
                if (state.priceHistory.size >= 2) {
                    Text(
                        "Price History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    PriceHistoryChart(
                        history = state.priceHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp)
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Open in browser button
                Button(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deal.productUrl)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Open on ${deal.source.displayName}")
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PriceHistoryChart(history: List<PriceHistory>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas

        val prices = history.map { it.price }
        val minPrice = prices.min()
        val maxPrice = prices.max()
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.01)

        val w = size.width
        val h = size.height
        val stepX = w / (history.size - 1).toFloat()

        fun xOf(i: Int) = i * stepX
        fun yOf(price: Double) = (h * (1 - (price - minPrice) / priceRange)).toFloat()

        val path = Path().apply {
            moveTo(xOf(0), yOf(prices[0]))
            for (i in 1 until history.size) {
                lineTo(xOf(i), yOf(prices[i]))
            }
        }

        drawPath(path, color = lineColor, style = Stroke(width = 3.dp.toPx()))

        // Draw dots
        for (i in history.indices) {
            drawCircle(
                color = dotColor,
                radius = 4.dp.toPx(),
                center = Offset(xOf(i), yOf(prices[i]))
            )
        }
    }
}
