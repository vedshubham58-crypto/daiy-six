package com.vedshubham.dailysix.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.vedshubham.dailysix.data.GameStore
import com.vedshubham.dailysix.game.CELLS
import com.vedshubham.dailysix.game.Puzzle
import com.vedshubham.dailysix.game.PuzzleGenerator
import com.vedshubham.dailysix.game.conflicts
import java.time.LocalDate

enum class Mode { DAILY, PRACTICE }

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val store = GameStore.get(app)

    var mode by mutableStateOf(Mode.DAILY)
        private set
    var puzzle by mutableStateOf<Puzzle?>(null)
        private set
    var date: LocalDate = LocalDate.now()
        private set
    val board = mutableStateListOf<Int>()
    var selected by mutableIntStateOf(-1)
        private set
    var seconds by mutableIntStateOf(0)
        private set
    var solved by mutableStateOf(false)
        private set
    var hintsUsed by mutableIntStateOf(0)
        private set

    /** Total practice puzzles solved, used to space out full-screen ads. */
    var practiceSolvedCount = 0
        private set

    val freeHintAvailable get() = hintsUsed == 0

    fun conflicts(): Set<Int> = conflicts(board)

    fun startDaily() {
        date = LocalDate.now()
        val p = PuzzleGenerator.daily(date)
        mode = Mode.DAILY
        puzzle = p
        selected = -1
        hintsUsed = 0
        val doneIn = store.dailyTime(date)
        val saved = store.dailyProgress(date)
        board.clear()
        when {
            doneIn != null -> {
                board.addAll(p.solution)
                seconds = doneIn
                solved = true
            }
            saved != null -> {
                board.addAll(saved.board)
                seconds = saved.seconds
                solved = false
            }
            else -> {
                board.addAll(p.givens)
                seconds = 0
                solved = false
            }
        }
    }

    fun startPractice() {
        val p = PuzzleGenerator.practice()
        mode = Mode.PRACTICE
        puzzle = p
        board.clear()
        board.addAll(p.givens)
        selected = -1
        seconds = 0
        solved = false
        hintsUsed = 0
    }

    fun select(index: Int) {
        selected = if (selected == index) -1 else index
    }

    fun enter(value: Int) {
        val p = puzzle ?: return
        if (solved || selected !in 0 until CELLS || p.isGiven(selected)) return
        board[selected] = if (board[selected] == value) 0 else value
        afterMove()
    }

    fun erase() {
        val p = puzzle ?: return
        if (solved || selected !in 0 until CELLS || p.isGiven(selected)) return
        board[selected] = 0
        afterMove()
    }

    /** Fills the selected cell (or the first empty/wrong one) with the right number. */
    fun useHint() {
        val p = puzzle ?: return
        if (solved) return
        val target = selected.takeIf { it in 0 until CELLS && board[it] != p.solution[it] }
            ?: (0 until CELLS).firstOrNull { board[it] != p.solution[it] }
            ?: return
        board[target] = p.solution[target]
        selected = target
        hintsUsed++
        afterMove()
    }

    fun tick() {
        if (solved || puzzle == null) return
        seconds++
        if (mode == Mode.DAILY && seconds % 5 == 0) saveProgress()
    }

    private fun afterMove() {
        val p = puzzle ?: return
        if (board.toList() == p.solution) {
            solved = true
            selected = -1
            when (mode) {
                Mode.DAILY -> store.recordDailySolved(date, seconds)
                Mode.PRACTICE -> practiceSolvedCount = store.recordPracticeSolved()
            }
        } else if (mode == Mode.DAILY) {
            saveProgress()
        }
    }

    private fun saveProgress() = store.saveDailyProgress(date, board.toList(), seconds)
}

fun formatTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
