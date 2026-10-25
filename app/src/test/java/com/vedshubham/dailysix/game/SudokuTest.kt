package com.vedshubham.dailysix.game

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SudokuTest {

    @Test
    fun dailyPuzzleIsTheSameForEveryone() {
        val day = LocalDate.of(2026, 11, 5)
        assertEquals(PuzzleGenerator.daily(day).givens, PuzzleGenerator.daily(day).givens)
    }

    @Test
    fun differentDaysGiveDifferentPuzzles() {
        val a = PuzzleGenerator.daily(LocalDate.of(2026, 11, 5))
        val b = PuzzleGenerator.daily(LocalDate.of(2026, 11, 6))
        assertTrue(a.givens != b.givens)
    }

    @Test
    fun generatedPuzzlesAreValidAndUnique() {
        var day = LocalDate.of(2026, 11, 1)
        repeat(400) {
            val p = PuzzleGenerator.daily(day)
            assertTrue("solution has conflicts", conflicts(p.solution).isEmpty())
            assertTrue(p.solution.all { it in 1..SIZE })
            for (i in 0 until CELLS) {
                if (p.isGiven(i)) assertEquals(p.solution[i], p.givens[i])
            }
            assertEquals("puzzle must have one answer", 1, Solver.countSolutions(p.givens))
            assertTrue(p.givens.count { it != 0 } in 8..16)
            day = day.plusDays(1)
        }
    }

    @Test
    fun conflictsAreDetected() {
        val board = MutableList(CELLS) { 0 }
        board[0] = 3
        board[5] = 3 // same row
        board[7] = 4
        assertEquals(setOf(0, 5), conflicts(board))
    }

    @Test
    fun dailyNumberStartsAtOne() {
        assertEquals(1L, PuzzleGenerator.dailyNumber(LAUNCH_DAY))
        assertEquals(10L, PuzzleGenerator.dailyNumber(LAUNCH_DAY.plusDays(9)))
    }
}
