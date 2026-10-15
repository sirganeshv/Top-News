# Top Ten News (Android)

Top 10 stories in 10 tabs: Business, Economics, Political, Geographical,
National (India), Finance, Tech, Tamil Nadu, Chennai and Trichy.

Kotlin + Jetpack Compose, MVVM. News comes from Google News RSS (India edition),
so there's no API key. Swipe between tabs, pull down to refresh, tap a story
to open it in the browser. Light and dark themes follow the phone setting.

## Get the APK without installing anything (GitHub)
1. Create a new GitHub repository and upload everything in this folder
   (keep the `.github` folder).
2. Open the repository's **Actions** tab. The "Build APK" workflow runs on
   every push (or run it manually with "Run workflow").
3. When it finishes (about 5 minutes), open the run and download
   **TopTenNews-apk** under Artifacts. Unzip it to get `app-debug.apk`.
4. Copy it to your phone and install it (allow "Install unknown apps" when asked).

## Build with Android Studio
1. File > Open > select this folder. Let Gradle sync.
2. Build > Build App Bundle(s) / APK(s) > Build APK(s).
3. The APK is at `app/build/outputs/apk/debug/app-debug.apk`.

## Changing what a tab shows
Edit the search queries in `app/src/main/java/com/topten/news/Category.kt`.
`when:1d` limits results to the last day; widen it (e.g. `when:3d`) for
quieter topics.

## Before publishing on the Play Store
Google News RSS is meant for personal use. For a public release, switch
`NewsRepository` to a licensed news API (e.g. NewsData.io or GNews) served
through your own backend so the API key isn't in the app.
