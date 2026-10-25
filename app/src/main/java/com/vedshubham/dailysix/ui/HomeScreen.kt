package com.vedshubham.dailysix.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vedshubham.dailysix.data.Stats
import com.vedshubham.dailysix.game.PuzzleGenerator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    stats: Stats,
    todayTime: Int?,
    todayStarted: Boolean,
    isPro: Boolean,
    proPrice: String?,
    showPrivacy: Boolean,
    onPlayDaily: () -> Unit,
    onPractice: () -> Unit,
    onBuyPro: () -> Unit,
    onPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Daily Six", fontSize = 40.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text(
            "#${PuzzleGenerator.dailyNumber(today)} · " +
                today.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBox("🔥 ${stats.streak}", "day streak")
                StatBox("${stats.bestStreak}", "best streak")
                StatBox("${stats.dailiesSolved}", "solved")
            }
        }

        if (todayTime != null) {
            Text(
                "Today's puzzle solved in ${formatTime(todayTime)} ✓\nA new one arrives at midnight.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
            OutlinedButton(onClick = onPlayDaily, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("View today's puzzle")
            }
        } else {
            Button(onClick = onPlayDaily, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Text(if (todayStarted) "Continue today's puzzle" else "Play today's puzzle", fontSize = 18.sp)
            }
        }
        OutlinedButton(onClick = onPractice, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("Practice puzzle")
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("How to play", fontWeight = FontWeight.Bold)
                Text(
                    "Fill the grid so every row, every column and every 2×3 box has the numbers 1 to 6 exactly once. " +
                        "Tap a square, then tap a number.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (!isPro) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily Six Pro", fontWeight = FontWeight.Bold)
                    Text("No ads, unlimited hints, and you support a solo developer.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = onBuyPro, modifier = Modifier.fillMaxWidth()) {
                        Text(if (proPrice != null) "Go Pro · $proPrice" else "Go Pro")
                    }
                }
            }
        } else {
            Text("⭐ Pro: thanks for your support!", color = MaterialTheme.colorScheme.secondary)
        }

        if (showPrivacy) {
            TextButton(onClick = onPrivacy) { Text("Privacy settings") }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
