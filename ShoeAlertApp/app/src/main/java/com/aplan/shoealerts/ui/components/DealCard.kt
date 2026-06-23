package com.aplan.shoealerts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.model.ShoppingSource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealCard(
    deal: Deal,
    onFavoriteToggle: (Deal) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.US) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            if (onClick != null) onClick()
            else runCatching { uriHandler.openUri(deal.productUrl) }
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (deal.imageUrl != null) {
                    AsyncImage(
                        model = deal.imageUrl,
                        contentDescription = deal.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Source badge
                SourceBadge(source = deal.source)

                Spacer(Modifier.height(4.dp))

                Text(
                    text = deal.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "\$${String.format("%.2f", deal.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    deal.originalPrice?.let { orig ->
                        if (orig > deal.price) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "\$${String.format("%.2f", orig)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textDecoration = TextDecoration.LineThrough
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    deal.size?.let { size ->
                        Text(
                            text = "Size: $size",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    deal.condition?.let { cond ->
                        Text(
                            text = cond.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = dateFormat.format(Date(deal.foundAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onFavoriteToggle(deal) }) {
                    Icon(
                        imageVector = if (deal.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (deal.isFavorite) "Unfavorite" else "Favorite",
                        tint = if (deal.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = { onDelete(deal.id) }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun SourceBadge(source: ShoppingSource, modifier: Modifier = Modifier) {
    val (color, label) = when (source) {
        ShoppingSource.AMAZON -> Pair(Color(0xFFFF9900), "Amazon")
        ShoppingSource.POSHMARK -> Pair(Color(0xFFE31C79), "Poshmark")
        ShoppingSource.SHEIN -> Pair(Color(0xFF000000), "Shein")
        ShoppingSource.EBAY -> Pair(Color(0xFF0064D2), "eBay")
        ShoppingSource.WALMART -> Pair(Color(0xFF0071CE), "Walmart")
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
