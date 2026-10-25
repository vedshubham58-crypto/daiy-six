package com.vedshubham.dailysix

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vedshubham.dailysix.ads.Ads
import com.vedshubham.dailysix.ads.BannerAd
import com.vedshubham.dailysix.billing.Billing
import com.vedshubham.dailysix.data.GameStore
import com.vedshubham.dailysix.game.PuzzleGenerator
import com.vedshubham.dailysix.ui.DailySixTheme
import com.vedshubham.dailysix.ui.GameScreen
import com.vedshubham.dailysix.ui.GameViewModel
import com.vedshubham.dailysix.ui.HomeScreen
import com.vedshubham.dailysix.ui.Mode
import com.vedshubham.dailysix.ui.formatTime
import java.time.LocalDate
import kotlinx.coroutines.delay

/** Full-screen ad after every 2nd practice puzzle, never during the daily puzzle. */
private const val PRACTICE_PUZZLES_PER_AD = 2

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Ads.setUp(this)
        val store = GameStore.get(this)

        setContent {
            DailySixTheme {
                val vm: GameViewModel = viewModel()
                val isPro by store.isPro.collectAsState()
                val stats by store.stats.collectAsState()
                val adsReady by Ads.ready.collectAsState()
                val privacy by Ads.privacyOptionsRequired.collectAsState()
                val price by Billing.price.collectAsState()
                var inGame by rememberSaveable { mutableStateOf(false) }
                val lifecycle = LocalLifecycleOwner.current.lifecycle

                // Pick up a new day (and a broken streak) whenever the app comes back.
                LaunchedEffect(Unit) {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { store.refresh() }
                }
                // Restore the game after the system recreates the activity.
                LaunchedEffect(Unit) {
                    if (inGame && vm.puzzle == null) vm.startDaily()
                }

                fun nextPractice() {
                    val showAd = !isPro && vm.mode == Mode.PRACTICE && vm.solved &&
                        vm.practiceSolvedCount % PRACTICE_PUZZLES_PER_AD == 0
                    if (showAd) Ads.showInterstitial(this@MainActivity) { vm.startPractice() } else vm.startPractice()
                    inGame = true
                }

                BackHandler(enabled = inGame) { inGame = false }

                Scaffold(
                    bottomBar = {
                        if (!isPro && adsReady) {
                            Box(Modifier.navigationBarsPadding()) { BannerAd() }
                        }
                    },
                ) { padding ->
                    if (inGame && vm.puzzle != null) {
                        LaunchedEffect(vm.puzzle, vm.solved) {
                            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                while (true) {
                                    delay(1000)
                                    vm.tick()
                                }
                            }
                        }
                        GameScreen(
                            vm = vm,
                            isPro = isPro,
                            streak = stats.streak,
                            onBack = { inGame = false },
                            onHint = {
                                if (isPro || vm.freeHintAvailable) {
                                    vm.useHint()
                                } else if (!Ads.showRewarded(this@MainActivity) { vm.useHint() }) {
                                    Toast.makeText(this@MainActivity, "No video ready yet. Try again in a moment.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onNextPractice = ::nextPractice,
                            onShare = { shareResult(vm.seconds, stats.streak) },
                            modifier = Modifier.padding(padding),
                        )
                    } else {
                        val today = LocalDate.now()
                        HomeScreen(
                            stats = stats,
                            todayTime = store.dailyTime(today),
                            todayStarted = store.dailyProgress(today) != null,
                            isPro = isPro,
                            proPrice = price,
                            showPrivacy = privacy,
                            onPlayDaily = {
                                vm.startDaily()
                                inGame = true
                            },
                            onPractice = {
                                vm.startPractice()
                                inGame = true
                            },
                            onBuyPro = {
                                if (!Billing.buyPro(this@MainActivity)) {
                                    Toast.makeText(this@MainActivity, "Google Play isn't ready yet. Try again in a moment.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onPrivacy = { Ads.showPrivacyOptions(this@MainActivity) },
                            modifier = Modifier.padding(padding),
                        )
                    }
                }
            }
        }
    }

    private fun shareResult(seconds: Int, streak: Int) {
        val number = PuzzleGenerator.dailyNumber(LocalDate.now())
        val text = "Daily Six #$number solved in ${formatTime(seconds)} 🔥$streak\n" +
            "https://play.google.com/store/apps/details?id=com.vedshubham.dailysix"
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(send, "Share result"))
    }
}
