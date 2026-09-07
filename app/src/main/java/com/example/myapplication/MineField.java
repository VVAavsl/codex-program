package com.example.myapplication;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 游戏核心逻辑：布雷（首次点击保护）、周围雷数计算、
 * 空白格 BFS 展开、标记循环、胜负判定。
 */
public class MineField {

    public static final int REVEAL_IGNORED = 0;
    public static final int REVEAL_OK = 1;
    public static final int REVEAL_MINE = 2;

    private final int rows;
    private final int cols;
    private final int mineCount;
    private final Cell[][] cells;
    private boolean minesPlaced = false;
    private boolean gameOver = false;
    private boolean won = false;
    private int revealedCount = 0;

    public MineField(int rows, int cols, int mineCount) {
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }
    }

    /** 从已保存的雷坐标列表恢复（用于旋转屏幕后保持同一局）。 */
    public MineField(int rows, int cols, List<Integer> mineIndices) {
        this(rows, cols, mineIndices.size());
        minesPlaced = true;
        for (int idx : mineIndices) {
            int r = idx / cols;
            int c = idx % cols;
            cells[r][c].setMine(true);
        }
        computeAdjacency();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMineCount() {
        return mineCount;
    }

    public Cell getCell(int r, int c) {
        return cells[r][c];
    }

    public boolean isMinesPlaced() {
        return minesPlaced;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isWon() {
        return won;
    }

    public int getRevealedCount() {
        return revealedCount;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    /** 布雷：排除首次点击格及其周围 8 格，随机放置 mineCount 颗雷。 */
    public void placeMines(int firstRow, int firstCol) {
        if (minesPlaced) {
            return;
        }
        List<Integer> candidates = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean nearFirst = Math.abs(r - firstRow) <= 1 && Math.abs(c - firstCol) <= 1;
                if (!nearFirst) {
                    candidates.add(r * cols + c);
                }
            }
        }
        Collections.shuffle(candidates, new Random());
        int placed = Math.min(mineCount, candidates.size());
        for (int i = 0; i < placed; i++) {
            int idx = candidates.get(i);
            cells[idx / cols][idx % cols].setMine(true);
        }
        computeAdjacency();
        minesPlaced = true;
    }

    private void computeAdjacency() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Cell cell = cells[r][c];
                if (cell.isMine()) {
                    cell.setAdjacentMines(0);
                    continue;
                }
                int n = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) {
                            continue;
                        }
                        int nr = r + dr;
                        int nc = c + dc;
                        if (inBounds(nr, nc) && cells[nr][nc].isMine()) {
                            n++;
                        }
                    }
                }
                cell.setAdjacentMines(n);
            }
        }
    }

    /**
     * 翻开一格。首次翻开时自动布雷；踩雷返回 REVEAL_MINE；
     * 空白格用 BFS 展开连通区域。
     */
    public int reveal(int r, int c) {
        if (gameOver || !inBounds(r, c)) {
            return REVEAL_IGNORED;
        }
        Cell cell = cells[r][c];
        if (cell.isRevealed() || cell.isFlagged() || cell.isQuestioned()) {
            return REVEAL_IGNORED;
        }
        if (!minesPlaced) {
            placeMines(r, c);
        }
        if (cell.isMine()) {
            gameOver = true;
            for (int rr = 0; rr < rows; rr++) {
                for (int cc = 0; cc < cols; cc++) {
                    if (cells[rr][cc].isMine()) {
                        cells[rr][cc].setRevealed(true);
                    }
                }
            }
            return REVEAL_MINE;
        }
        floodReveal(r, c);
        if (revealedCount == rows * cols - mineCount) {
            gameOver = true;
            won = true;
        }
        return REVEAL_OK;
    }

    private void floodReveal(int startRow, int startCol) {
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startRow, startCol});
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int r = p[0];
            int c = p[1];
            if (!inBounds(r, c)) {
                continue;
            }
            Cell cell = cells[r][c];
            if (cell.isRevealed() || cell.isMine() || cell.isFlagged() || cell.isQuestioned()) {
                continue;
            }
            cell.setRevealed(true);
            revealedCount++;
            if (cell.getAdjacentMines() == 0) {
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) {
                            continue;
                        }
                        int nr = r + dr;
                        int nc = c + dc;
                        if (inBounds(nr, nc)) {
                            Cell nb = cells[nr][nc];
                            if (!nb.isRevealed() && !nb.isMine() && !nb.isFlagged() && !nb.isQuestioned()) {
                                queue.add(new int[]{nr, nc});
                            }
                        }
                    }
                }
            }
        }
    }

    /** 循环切换标记：无标记 -> 旗 -> 问号 -> 无标记。返回新标记。 */
    public int cycleMark(int r, int c) {
        if (gameOver || !inBounds(r, c)) {
            return Cell.MARK_NONE;
        }
        Cell cell = cells[r][c];
        if (cell.isRevealed()) {
            return cell.getMark();
        }
        int next = (cell.getMark() + 1) % 3;
        cell.setMark(next);
        return next;
    }

    public int countFlags() {
        int n = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isFlagged()) {
                    n++;
                }
            }
        }
        return n;
    }

    public List<Integer> mineIndices() {
        List<Integer> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isMine()) {
                    list.add(r * cols + c);
                }
            }
        }
        return list;
    }

    public List<Integer> revealedIndices() {
        List<Integer> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isRevealed()) {
                    list.add(r * cols + c);
                }
            }
        }
        return list;
    }

    public List<Integer> markedIndices(int mark) {
        List<Integer> list = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].getMark() == mark) {
                    list.add(r * cols + c);
                }
            }
        }
        return list;
    }

    /** 旋转恢复时应用已翻开/标记状态。 */
    public void restoreState(List<Integer> revealedIdx, List<Integer> flagIdx,
                             List<Integer> questionIdx, boolean gameOver, boolean won) {
        for (int idx : revealedIdx) {
            if (idx >= 0 && idx < rows * cols) {
                cells[idx / cols][idx % cols].setRevealed(true);
            }
        }
        for (int idx : flagIdx) {
            if (idx >= 0 && idx < rows * cols) {
                Cell cell = cells[idx / cols][idx % cols];
                if (!cell.isRevealed()) {
                    cell.setMark(Cell.MARK_FLAG);
                }
            }
        }
        for (int idx : questionIdx) {
            if (idx >= 0 && idx < rows * cols) {
                Cell cell = cells[idx / cols][idx % cols];
                if (!cell.isRevealed() && !cell.isFlagged()) {
                    cell.setMark(Cell.MARK_QUESTION);
                }
            }
        }
        revealedCount = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (cells[r][c].isRevealed() && !cells[r][c].isMine()) {
                    revealedCount++;
                }
            }
        }
        this.gameOver = gameOver;
        this.won = won;
    }
}
