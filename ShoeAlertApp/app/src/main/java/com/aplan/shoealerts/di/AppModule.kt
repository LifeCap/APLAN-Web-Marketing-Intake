package com.aplan.shoealerts.di

import android.content.Context
import androidx.room.Room
import com.aplan.shoealerts.data.database.*
import com.aplan.shoealerts.network.ScraperRateLimiter
import com.aplan.shoealerts.network.scrapers.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideDealDao(db: AppDatabase): DealDao = db.dealDao()
    @Provides fun providePreferenceDao(db: AppDatabase): SearchPreferenceDao = db.searchPreferenceDao()
    @Provides fun provideAlertLogDao(db: AppDatabase): AlertLogDao = db.alertLogDao()
    @Provides fun providePriceHistoryDao(db: AppDatabase): PriceHistoryDao = db.priceHistoryDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = BaseScraper.buildClient()

    @Provides @Singleton fun provideRateLimiter(): ScraperRateLimiter = ScraperRateLimiter()

    @Provides @Singleton
    fun provideAmazonScraper(c: OkHttpClient, r: ScraperRateLimiter) = AmazonScraper(c, r)
    @Provides @Singleton
    fun providePoshmarkScraper(c: OkHttpClient, r: ScraperRateLimiter) = PoshmarkScraper(c, r)
    @Provides @Singleton
    fun provideSheinScraper(c: OkHttpClient, r: ScraperRateLimiter) = SheinScraper(c, r)
    @Provides @Singleton
    fun provideEbayScraper(c: OkHttpClient, r: ScraperRateLimiter) = EbayScraper(c, r)
    @Provides @Singleton
    fun provideWalmartScraper(c: OkHttpClient, r: ScraperRateLimiter) = WalmartScraper(c, r)
}
