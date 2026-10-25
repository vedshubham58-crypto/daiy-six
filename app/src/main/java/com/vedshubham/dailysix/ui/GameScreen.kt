package com.vedshubham.dailysix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vedshubham.dailysix.game.BOX_COLS
import com.vedshubham.dailysix.game.BOX_ROWS
import com.vedshubham.dailysix.game.SIZE
import com.vedshubham.dailysix.game.sameUnit

@Composable
fun GameScreen(
    vm: GameViewModel,
    isPro: Boolean,
    streak: Int,
    onBack: () -> Unit,
    onHint: () -> Unit,
    onNextPractice: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val puzzle = vm.puzzle ?: return
    val conflicts = vm.conflicts()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                if (vm.mode == Mode.DAILY) "Today's puzzle" else "Practice",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            Text(formatTime(vm.seconds), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Grid(
            board = vm.board,
            isGiven = puzzle::isGiven,
            selected = vm.selected,
            conflicts = conflicts,
            onSelect = vm::select,
            modifier = Modifier.widthIn(max = 480.dp),
        )

        if (vm.solved) {
            SolvedCard(
                vm = vm,
                streak = streak,
                onBack = onBack,
                onNextPractice = onNextPractice,
                onShare = onShare,
            )
        } else {
            Row(Modifier.fillMaxWidth().widthIn(max = 480.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (n in 1..SIZE) {
                    FilledTonalButton(
                        onClick = { vm.enter(n) },
                        modifier = Modifier.weight(1f).height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    ) { Text("$n", fontSize = 26.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = vm::erase, modifier = Modifier.weight(1f).height(52.dp)) {
                    Text("Erase")
                }
                Button(onClick = onHint, modifier = Modifier.weight(1f).height(52.dp)) {
                    Text(
                        when {
                            isPro -> "Hint"
                            vm.freeHintAvailable -> "Hint (free)"
                            else -> "Hint ▶ ad"
                        },
                    )
                }
            }
            if (conflicts.isNotEmpty()) {
                Text(
                    "Red numbers clash with another number in the same row, column or box.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun Grid(
    board: List<Int>,
    isGiven: (Int) -> Boolean,
    selected: Int,
    conflicts: Set<Int>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val thin = colors.outline.copy(alpha = 0.35f)
    val thick = colors.onSurfaceVariant
    val selectedValue = if (selected >= 0) board[selected] else 0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .border(2.5.dp, thick, RoundedCornerShape(10.dp))
            .drawWithContent {
                drawContent()
                val cell = size.width / SIZE
                for (i in 1 until SIZE) {
                    val x = i * cell
                    val y = i * cell
                    val colThick = i % BOX_COLS == 0
                    val rowThick = i % BOX_ROWS == 0
                    drawLine(if (colThick) thick else thin, Offset(x, 0f), Offset(x, size.height), if (colThick) 5f else 2f)
                    drawLine(if (rowThick) thick else thin, Offset(0f, y), Offset(size.width, y), if (rowThick) 5f else 2f)
                }
            },
    ) {
        for (r in 0 until SIZE) {
            Row(Modifier.weight(1f)) {
                for (c in 0 until SIZE) {
                    val i = r * SIZE + c
                    val v = board[i]
                    val bg = when {
                        i == selected -> colors.primary.copy(alpha = 0.35f)
                        selectedValue != 0 && v == selectedValue -> colors.primary.copy(alpha = 0.18f)
                        selected >= 0 && sameUnit(i, selected) -> colors.surfaceVariant
                        else -> colors.surface
                    }
                    val fg = when {
                        i in conflicts && !isGiven(i) -> colors.error
                        isGiven(i) -> colors.onSurface
                        else -> colors.secondary
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(bg)
                            .clickable { onSelect(i) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (v != 0) {
                            Text(
                                "$v",
                                fontSize = 28.sp,
                                fontWeight = if (isGiven(i)) FontWeight.Black else FontWeight.Medium,
                                color = fg,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SolvedCard(
    vm: GameViewModel,
    streak: Int,
    onBack: () -> Unit,
    onNextPractice: () -> Unit,
    onShare: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("🎉 Solved!", fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("Time ${formatTime(vm.seconds)}", style = MaterialTheme.typography.titleMedium)
            if (vm.mode == Mode.DAILY) {
                Text("🔥 $streak day streak. Come back tomorrow for a new puzzle!")
                Button(onClick = onShare, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Text("  Share result")
                }
                OutlinedButton(onClick = onNextPractice, modifier = Modifier.fillMaxWidth()) {
                    Text("Play a practice puzzle")
                }
            } else {
                Button(onClick = onNextPractice, modifier = Modifier.fillMaxWidth()) {
                    Text("Next puzzle")
                }
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Home") }
        }
    }
}
