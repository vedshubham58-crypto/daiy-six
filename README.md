# Daily Six

A daily 6×6 number puzzle for Android. Everyone gets the same puzzle each day, with a streak counter, a timer and unlimited practice puzzles.

**Earns money from:** AdMob banner, full-screen ads between practice puzzles, rewarded "watch an ad for a hint", and a one-time **Pro** upgrade (no ads, unlimited hints) sold through Google Play.

## Get the test app on your phone
1. On your phone, open https://github.com/vedshubham58-crypto/daiy-six/releases/download/test-build/daily-six-test.apk
2. Open the downloaded file (allow "install unknown apps" when asked).

The test app shows Google's test ads, which is expected.

## Build it yourself
Open the folder in Android Studio and press Run, or run `./gradlew assembleDebug`.

## Going live
See [LAUNCH_GUIDE.md](LAUNCH_GUIDE.md).

## Code map
| Folder | What's in it |
|---|---|
| `game/` | Puzzle generator, solver and rules (pure Kotlin, unit tested) |
| `data/` | Streaks, progress and Pro status saved on the phone |
| `ads/` | AdMob: consent popup, banner, interstitial and rewarded ads |
| `billing/` | Google Play Billing for the Pro upgrade |
| `ui/` | Home and game screens (Jetpack Compose) |
