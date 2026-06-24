package com.aplan.shoealerts.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.material3.GlanceTheme
import androidx.glance.text.*
import com.aplan.shoealerts.MainActivity
import com.aplan.shoealerts.data.model.Deal
import com.aplan.shoealerts.data.repository.DealRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ShoeAlertWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun dealRepository(): DealRepository
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .dealRepository()
        val deals = repository.getCheapestDeals(3)
        provideContent {
            GlanceTheme {
                WidgetContent(deals)
            }
        }
    }

    @Composable
    private fun WidgetContent(deals: List<Deal>) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Text(
                text = "ShoeAlert",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary,
                    fontSize = 14.sp
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            if (deals.isEmpty()) {
                Text(
                    text = "No deals yet — open app to search",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            } else {
                deals.forEach { deal ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = deal.title.take(28),
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onSurface
                            ),
                            maxLines = 1
                        )
                        Text(
                            text = "\$${String.format("%.2f", deal.price)}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = GlanceTheme.colors.primary
                            )
                        )
                    }
                }
            }
        }
    }
}
