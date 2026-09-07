package com.example.myapplication;

/** 单个格子的数据模型：是否有雷、是否翻开、标记状态、周围雷数。 */
public class Cell {

    public static final int MARK_NONE = 0;
    public static final int MARK_FLAG = 1;
    public static final int MARK_QUESTION = 2;

    private boolean mine;
    private boolean revealed;
    private int mark = MARK_NONE;
    private int adjacentMines = 0;

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }

    public boolean isFlagged() {
        return mark == MARK_FLAG;
    }

    public boolean isQuestioned() {
        return mark == MARK_QUESTION;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}
