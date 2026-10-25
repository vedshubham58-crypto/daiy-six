package com.vedshubham.dailysix.game

import java.time.LocalDate
import java.util.Random

/** 6x6 grid split into six boxes of 2 rows x 3 columns. Cells are indexed 0..35, row by row. */
const val SIZE = 6
const val BOX_ROWS = 2
const val BOX_COLS = 3
const val CELLS = SIZE * SIZE

/** Day 1 of the game. Used to number daily puzzles ("Daily Six #12"). */
val LAUNCH_DAY: LocalDate = LocalDate.of(2026, 11, 1)

class Puzzle(val id: String, val givens: List<Int>, val solution: List<Int>) {
    fun isGiven(index: Int) = givens[index] != 0
}

fun row(index: Int) = index / SIZE
fun col(index: Int) = index % SIZE
fun box(index: Int) = (row(index) / BOX_ROWS) * (SIZE / BOX_COLS) + col(index) / BOX_COLS

/** True when the two cells share a row, column or box. */
fun sameUnit(a: Int, b: Int) = row(a) == row(b) || col(a) == col(b) || box(a) == box(b)

private val peers: List<IntArray> = List(CELLS) { i ->
    (0 until CELLS).filter { it != i && sameUnit(it, i) }.toIntArray()
}

/** Indexes of filled cells whose value repeats somewhere in their row, column or box. */
fun conflicts(board: List<Int>): Set<Int> {
    val result = HashSet<Int>()
    for (i in 0 until CELLS) {
        val v = board[i]
        if (v != 0 && peers[i].any { board[it] == v }) result.add(i)
    }
    return result
}

object Solver {
    /** Counts solutions, stopping once [limit] is reached. A good puzzle has exactly one. */
    fun countSolutions(board: List<Int>, limit: Int = 2): Int {
        val grid = board.toIntArray()
        return search(grid, limit)
    }

    private fun candidates(grid: IntArray, i: Int): Int {
        var used = 0
        for (p in peers[i]) used = used or (1 shl grid[p])
        return used.inv() and 0b1111110 // bits 1..6
    }

    private fun search(grid: IntArray, limit: Int): Int {
        var best = -1
        var bestMask = 0
        var bestCount = 10
        for (i in 0 until CELLS) {
            if (grid[i] != 0) continue
            val mask = candidates(grid, i)
            val count = Integer.bitCount(mask)
            if (count == 0) return 0
            if (count < bestCount) {
                best = i; bestMask = mask; bestCount = count
            }
        }
        if (best == -1) return 1
        var found = 0
        for (v in 1..SIZE) {
            if (bestMask and (1 shl v) == 0) continue
            grid[best] = v
            found += search(grid, limit - found)
            if (found >= limit) break
        }
        grid[best] = 0
        return found
    }
}

object PuzzleGenerator {
    /** Everyone gets the same puzzle on the same date, with no server needed. */
    fun daily(date: LocalDate): Puzzle =
        generate(seed = date.toEpochDay() * 7919L + 104729L, id = "daily-$date", targetGivens = 13)

    fun practice(seed: Long = System.nanoTime()): Puzzle =
        generate(seed = seed, id = "practice-$seed", targetGivens = 14)

    fun dailyNumber(date: LocalDate): Long = date.toEpochDay() - LAUNCH_DAY.toEpochDay() + 1

    fun generate(seed: Long, id: String, targetGivens: Int): Puzzle {
        val rnd = Random(seed)
        val solution = solvedGrid(rnd)
        val puzzle = solution.toMutableList()
        var givens = CELLS
        for (i in (0 until CELLS).shuffled(rnd)) {
            if (givens <= targetGivens) break
            val saved = puzzle[i]
            puzzle[i] = 0
            if (Solver.countSolutions(puzzle) == 1) givens-- else puzzle[i] = saved
        }
        return Puzzle(id, puzzle, solution)
    }

    /** A valid full grid built from a fixed pattern, then shuffled in ways that keep it valid. */
    private fun solvedGrid(rnd: Random): List<Int> {
        val bands = SIZE / BOX_ROWS
        val stacks = SIZE / BOX_COLS
        val rows = (0 until bands).shuffled(rnd).flatMap { b ->
            (0 until BOX_ROWS).shuffled(rnd).map { b * BOX_ROWS + it }
        }
        val cols = (0 until stacks).shuffled(rnd).flatMap { s ->
            (0 until BOX_COLS).shuffled(rnd).map { s * BOX_COLS + it }
        }
        val digits = (1..SIZE).shuffled(rnd)
        return List(CELLS) { i ->
            val r = rows[row(i)]
            val c = cols[col(i)]
            digits[(BOX_COLS * (r % BOX_ROWS) + r / BOX_ROWS + c) % SIZE]
        }
    }
}
