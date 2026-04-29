package com.frudoko.service;

import org.springframework.stereotype.Component;
import java.util.*;

/**
 * ──────────────────────────────────────────────────────────────────
 *  SudokuGenerator — generates valid 4×4 Frudoko puzzles
 *
 *  Fruit mapping:  1 = 🍎  2 = 🍌  3 = 🍇  4 = 🍊
 *
 *  Rules (standard 4×4 Sudoku):
 *    • Each row contains 1–4 exactly once
 *    • Each column contains 1–4 exactly once
 *    • Each 2×2 sub-grid contains 1–4 exactly once
 * ──────────────────────────────────────────────────────────────────
 */
@Component
public class SudokuGenerator {

    private static final int SIZE = 4;

    // Cells removed per difficulty level
    private static final Map<String, Integer> HOLES = Map.of(
            "EASY",   4,   // 12 cells given — easy for beginners
            "MEDIUM", 8,   //  8 cells given — moderate challenge
            "HARD",   11   //  5 cells given — minimal hints
    );

    // ── Public API ────────────────────────────────────────────────

    /**
     * Generate a fully solved, valid 4×4 grid.
     * Uses backtracking with shuffled candidates for variety.
     */
    public int[][] generateSolution() {
        int[][] grid = new int[SIZE][SIZE];
        backtrack(grid);
        return grid;
    }

    /**
     * Generate a puzzle by removing cells from a complete solution.
     *
     * @param solution the complete solution grid
     * @param level    "EASY" | "MEDIUM" | "HARD"
     * @return puzzle grid (0 = cell to fill by player)
     */
    public int[][] generatePuzzle(int[][] solution, String level) {
        int[][] puzzle = deepCopy(solution);
        int toRemove = HOLES.getOrDefault(level.toUpperCase(), 6);

        // Shuffle all cell positions, then blank out the first N
        List<int[]> cells = new ArrayList<>();
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                cells.add(new int[]{r, c});
        Collections.shuffle(cells);

        for (int i = 0; i < toRemove; i++)
            puzzle[cells.get(i)[0]][cells.get(i)[1]] = 0;

        return puzzle;
    }

    /**
     * Check if the player's current grid matches the solution exactly.
     */
    public boolean isSolved(int[][] current, int[][] solution) {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                if (current[r][c] != solution[r][c]) return false;
        return true;
    }

    /**
     * Validate whether a value at (row, col) breaks any Sudoku rule.
     * Used by the front-end check to highlight errors.
     */
    public boolean isValidMove(int[][] grid, int row, int col, int val) {
        if (val == 0) return true; // empty is always valid
        return isValidPlacement(grid, row, col, val);
    }

    // ── Backtracking solver ───────────────────────────────────────

    private boolean backtrack(int[][] grid) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c] == 0) {
                    // Shuffle 1–4 for random puzzle variety each call
                    List<Integer> candidates = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
                    Collections.shuffle(candidates);

                    for (int num : candidates) {
                        if (isValidPlacement(grid, r, c, num)) {
                            grid[r][c] = num;
                            if (backtrack(grid)) return true;
                            grid[r][c] = 0; // undo
                        }
                    }
                    return false; // no valid number found → backtrack
                }
            }
        }
        return true; // all cells filled
    }

    private boolean isValidPlacement(int[][] grid, int row, int col, int num) {
        // Check row uniqueness
        for (int c = 0; c < SIZE; c++)
            if (c != col && grid[row][c] == num) return false;

        // Check column uniqueness
        for (int r = 0; r < SIZE; r++)
            if (r != row && grid[r][col] == num) return false;

        // Check 2×2 box uniqueness
        int boxRow = (row / 2) * 2;
        int boxCol = (col / 2) * 2;
        for (int r = boxRow; r < boxRow + 2; r++)
            for (int c = boxCol; c < boxCol + 2; c++)
                if ((r != row || c != col) && grid[r][c] == num) return false;

        return true;
    }

    // ── Utility ───────────────────────────────────────────────────

    public int[][] deepCopy(int[][] src) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            System.arraycopy(src[i], 0, copy[i], 0, SIZE);
        return copy;
    }
}
