# Launch guide: from code to live on Google Play

Steps marked **(you)** need your own accounts, so only you can do them. Everything else Claude does.

## 1. Try the test app (you, 5 min)
Install the test APK from GitHub Actions (see README) and play a few puzzles. Tell Claude anything you want changed.

## 2. Google Play developer account (you, ~1–2 days for approval)
1. Go to https://play.google.com/console/signup and sign in with the Google account you want to own the app.
2. Choose **Personal** account. Pay the one-time **$25** fee.
3. Verify your identity (government ID) and your phone number.
4. New personal accounts must run a **closed test with at least 12 testers for 14 days** before going to production. Start collecting 12 Gmail addresses (friends, family) now.

## 3. AdMob account (you, ~15 min, ads can take a day to start)
1. Go to https://admob.google.com and sign up with the same Google account.
2. **Apps > Add app > Android**. Say "No" to "is it listed on Play" for now. Name: Daily Six.
3. Copy the **App ID** (looks like `ca-app-pub-1234…~5678…`).
4. **Ad units > Add ad unit**, create three:
   - **Banner** (name: home_banner)
   - **Interstitial** (name: practice_interstitial)
   - **Rewarded** (name: hint_rewarded)
5. Send Claude the App ID and the three ad unit IDs (these are not secret). Claude puts them in `gradle.properties`.
6. **Payments**: add your address and bank details so AdMob can pay you (paid monthly once you pass $100).
7. **Privacy & messaging > GDPR**: create a consent message for the app and publish it. This is the popup European users see.

**Never tap your own real ads.** Google bans accounts for it. The test app uses test ads, so tap those freely.

## 4. Signing key (you + Claude, 10 min)
Google Play needs every upload signed with the same key. Claude gives you one command to create `upload-key.jks` on your computer. Keep that file and its password **backed up somewhere safe** (Google Drive + a USB stick). Claude then explains how to add it to GitHub as encrypted secrets so GitHub can build the release file for you.

## 5. Create the app in Play Console (you, 30 min)
1. **Create app**: name Daily Six, Game, Free.
2. **Monetize > Products > In-app products > Create**: product ID **`pro_upgrade`** (must be exactly this), name "Daily Six Pro", price $1.99, then **Activate**. (Play only lets you do this after the first build is uploaded to a test track.)
3. **App content**: privacy policy URL (Claude hosts `docs/privacy-policy.md`), ads = Yes, target audience = 13+, content rating questionnaire, data safety form (Claude gives you the answers).
4. **Store listing**: short and full description, icon, feature graphic, phone screenshots. Claude prepares all of these.

## 6. Closed test → production (you, 14+ days)
1. Upload the release `.aab` Claude's build makes to **Testing > Closed testing**, add your 12+ testers, and ask them to install and play.
2. After 14 days, apply for **production access** in the dashboard, then roll out to production.
3. Back in AdMob, link the app to its Play listing and add `app-ads.txt` (Claude provides the file).
