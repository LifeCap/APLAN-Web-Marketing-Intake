# ShoeAlert — Android Deal Finder

An Android application for finding deals on enclosed-toe, rubber-soled shoes across multiple shopping sites.

## Features

- **Multi-site Scraping**: Searches Amazon, Poshmark, Shein, eBay, and Walmart simultaneously
- **Price Threshold Alerts**: Get notified when shoes drop below your set price (default $15)
- **New Deal Alerts**: Get notified the moment a new matching deal is found
- **Push Notifications**: Android system notifications with deal details and direct links
- **SMS Alerts**: Optional SMS text messages to your phone number
- **Background Search**: Runs automatically every 1–24 hours via WorkManager (survives device reboots)
- **Multiple Search Profiles**: Save different searches for different shoe sizes/styles/budgets
- **Favorites**: Save deals you like for easy access later
- **Alert History**: Full log of every alert sent (push and SMS)
- **Tablet Optimized**: Adaptive layout works on phones and tablets

## Shoe Search Criteria (Configurable)

- **Size**: Any numeric shoe size (US sizing, e.g. 9, 10.5, W8)
- **Style**: Sneaker, Loafer, Oxford, Closed-Toe Sandal, Moccasin, Boat Shoe, Slip-On, or Any
- **Gender**: Mens, Womens, or Unisex
- **Brand**: Optional brand filter
- **Condition**: New, Used, or Any
- **Price threshold**: Default $15 — alerts fire when price is at or below this

## Architecture

```
ShoeAlertApp/
├── data/
│   ├── database/         # Room DB (deals, preferences, alert logs)
│   ├── model/            # Deal, SearchPreference, AlertLog
│   └── repository/       # DealRepository — single source of truth
├── network/scrapers/     # HTML scrapers for each site (Jsoup + OkHttp)
├── notifications/        # NotificationHelper (push) + SmsHelper (SMS)
├── worker/               # DealSearchWorker (WorkManager) + BootReceiver
├── viewmodel/            # DealsViewModel, SearchViewModel, AlertsViewModel
├── ui/
│   ├── screens/          # DealsScreen, SearchScreen, AlertsScreen
│   ├── components/       # DealCard, SourceBadge, InfoChip
│   └── navigation/       # Bottom-nav AppNavigation
└── di/                   # Hilt dependency injection module
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| DI | Hilt |
| Database | Room |
| Background | WorkManager |
| Networking | OkHttp + Retrofit |
| HTML Parsing | Jsoup |
| Image Loading | Coil |

## Build

1. Open in Android Studio Hedgehog or newer
2. Sync Gradle
3. Run on a device or emulator (API 26+)

## Permissions

| Permission | Purpose |
|-----------|---------|
| `INTERNET` | Fetch deal pages from shopping sites |
| `POST_NOTIFICATIONS` | Show deal alert push notifications (Android 13+) |
| `SEND_SMS` | Send SMS alerts (only if user enables SMS alerts) |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule background search after reboot |
| `FOREGROUND_SERVICE` | Keep background search running reliably |

## Notes

- Scraping of Amazon, Poshmark, Shein, eBay, and Walmart is done via their public search pages. Results may vary as site layouts change. This app is intended for personal use.
- SMS alerts require the `SEND_SMS` permission to be granted by the user.
- Background search respects a minimum interval of 1 hour. Setting it to 4–6 hours is recommended to be respectful of site traffic.
