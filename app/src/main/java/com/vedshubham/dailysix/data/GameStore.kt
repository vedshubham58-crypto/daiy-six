package com.vedshubham.dailysix.data

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Stats(
    val streak: Int,
    val bestStreak: Int,
    val dailiesSolved: Int,
    val practiceSolved: Int,
)

data class SavedProgress(val board: List<Int>, val seconds: Int)

/** Everything the game remembers on the phone: streaks, today's progress and Pro status. */
class GameStore private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("daily_six", Context.MODE_PRIVATE)

    private val _isPro = MutableStateFlow(prefs.getBoolean(KEY_PRO, false))
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    private val _stats = MutableStateFlow(readStats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    fun setPro(value: Boolean) {
        prefs.edit().putBoolean(KEY_PRO, value).apply()
        _isPro.value = value
    }

    /** Seconds taken to solve that day's puzzle, or null if not solved yet. */
    fun dailyTime(date: LocalDate): Int? =
        prefs.getInt("time_$date", -1).takeIf { it >= 0 }

    fun saveDailyProgress(date: LocalDate, board: List<Int>, seconds: Int) {
        prefs.edit()
            .putString(KEY_PROGRESS_DATE, date.toString())
            .putString(KEY_PROGRESS_BOARD, board.joinToString(""))
            .putInt(KEY_PROGRESS_SECONDS, seconds)
            .apply()
    }

    fun dailyProgress(date: LocalDate): SavedProgress? {
        if (prefs.getString(KEY_PROGRESS_DATE, null) != date.toString()) return null
        val board = prefs.getString(KEY_PROGRESS_BOARD, null) ?: return null
        if (board.length != 36) return null
        return SavedProgress(board.map { it.digitToInt() }, prefs.getInt(KEY_PROGRESS_SECONDS, 0))
    }

    fun recordDailySolved(date: LocalDate, seconds: Int) {
        if (dailyTime(date) != null) return
        val day = date.toEpochDay()
        val last = prefs.getLong(KEY_LAST_DAY, Long.MIN_VALUE)
        val streak = if (last == day - 1) prefs.getInt(KEY_STREAK, 0) + 1 else 1
        prefs.edit()
            .putInt("time_$date", seconds)
            .putLong(KEY_LAST_DAY, day)
            .putInt(KEY_STREAK, streak)
            .putInt(KEY_BEST, maxOf(streak, prefs.getInt(KEY_BEST, 0)))
            .putInt(KEY_DAILIES, prefs.getInt(KEY_DAILIES, 0) + 1)
            .apply()
        _stats.value = readStats()
    }

    /** Returns how many practice puzzles have been solved in total. */
    fun recordPracticeSolved(): Int {
        val count = prefs.getInt(KEY_PRACTICE, 0) + 1
        prefs.edit().putInt(KEY_PRACTICE, count).apply()
        _stats.value = readStats()
        return count
    }

    /** Re-check the streak, e.g. when the app is reopened on a new day. */
    fun refresh() {
        _stats.value = readStats()
    }

    private fun readStats(): Stats {
        val today = LocalDate.now().toEpochDay()
        val last = prefs.getLong(KEY_LAST_DAY, Long.MIN_VALUE)
        // A streak survives until the end of the day after the last solve.
        val streak = if (last >= today - 1) prefs.getInt(KEY_STREAK, 0) else 0
        return Stats(
            streak = streak,
            bestStreak = prefs.getInt(KEY_BEST, 0),
            dailiesSolved = prefs.getInt(KEY_DAILIES, 0),
            practiceSolved = prefs.getInt(KEY_PRACTICE, 0),
        )
    }

    companion object {
        private const val KEY_PRO = "pro"
        private const val KEY_LAST_DAY = "last_daily_day"
        private const val KEY_STREAK = "streak"
        private const val KEY_BEST = "best_streak"
        private const val KEY_DAILIES = "dailies_solved"
        private const val KEY_PRACTICE = "practice_solved"
        private const val KEY_PROGRESS_DATE = "progress_date"
        private const val KEY_PROGRESS_BOARD = "progress_board"
        private const val KEY_PROGRESS_SECONDS = "progress_seconds"

        @Volatile private var instance: GameStore? = null

        fun get(context: Context): GameStore =
            instance ?: synchronized(this) {
                instance ?: GameStore(context.applicationContext).also { instance = it }
            }
    }
}
