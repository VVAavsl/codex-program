package com.example.myapplication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/** MineField 核心逻辑的 JVM 单元测试（不依赖 Android 环境）。 */
public class MineFieldTest {

    @Test
    public void firstClickIsAlwaysSafe() {
        MineField field = new MineField(9, 9, 10);
        // 首次翻开 (4,4)，布雷必须避开它及其周围 8 格
        int result = field.reveal(4, 4);
        assertEquals(MineField.REVEAL_OK, result);
        assertTrue(field.isMinesPlaced());
        for (int r = 3; r <= 5; r++) {
            for (int c = 3; c <= 5; c++) {
                assertFalse("首次点击区域出现雷", field.getCell(r, c).isMine());
            }
        }
        assertTrue(field.getRevealedCount() > 0);
    }

    @Test
    public void adjacencyNumbersAreCorrect() {
        // 3x3，四角有雷 -> 中心周围 4 雷，边上 2 雷
        MineField field = new MineField(3, 3, Arrays.asList(0, 2, 6, 8));
        assertEquals(4, field.getCell(1, 1).getAdjacentMines());
        assertEquals(2, field.getCell(0, 1).getAdjacentMines());
        assertEquals(2, field.getCell(1, 0).getAdjacentMines());
        assertEquals(0, field.getCell(0, 0).getAdjacentMines());
    }

    @Test
    public void floodRevealsConnectedBlanks() {
        // 5x5 单颗雷在 (4,4)，点 (0,0) 应几乎展开整盘
        MineField field = new MineField(5, 5, Arrays.asList(24));
        int result = field.reveal(0, 0);
        assertEquals(MineField.REVEAL_OK, result);
        assertTrue(field.isGameOver());
        assertTrue(field.isWon());
        // 除了雷之外全部翻开
        assertFalse(field.getCell(4, 4).isRevealed());
        assertTrue(field.getCell(4, 3).isRevealed());
        assertTrue(field.getCell(3, 4).isRevealed());
    }

    @Test
    public void stepOnMineLosesGame() {
        // 1x3：0 和 2 是雷，1 不是雷
        MineField field = new MineField(1, 3, Arrays.asList(0, 2));
        int result = field.reveal(0, 0);
        assertEquals(MineField.REVEAL_MINE, result);
        assertTrue(field.isGameOver());
        assertFalse(field.isWon());
        // 踩雷后“所有雷”被揭示（按需求文档 2.3.2），非雷格保持未翻开
        assertTrue(field.getCell(0, 0).isRevealed());
        assertTrue(field.getCell(0, 2).isRevealed());
        assertFalse(field.getCell(0, 1).isRevealed());
    }

    @Test
    public void markCycleAndFlagCount() {
        MineField field = new MineField(2, 2, 1);
        assertEquals(Cell.MARK_FLAG, field.cycleMark(0, 0));
        assertEquals(1, field.countFlags());
        assertEquals(Cell.MARK_QUESTION, field.cycleMark(0, 0));
        assertEquals(0, field.countFlags()); // 问号不计入剩余雷数
        assertEquals(Cell.MARK_NONE, field.cycleMark(0, 0));
        assertEquals(0, field.countFlags());
    }

    @Test
    public void revealedCellCannotBeReMarkedOrReRevealed() {
        // 对角两颗雷，(1,1) 是数字 2，翻开后不会触发展开
        MineField field = new MineField(3, 3, Arrays.asList(0, 8));
        assertEquals(MineField.REVEAL_OK, field.reveal(1, 1));
        assertTrue(field.getCell(1, 1).isRevealed());
        // 已翻开的格子再次点击无反应
        assertEquals(MineField.REVEAL_IGNORED, field.reveal(1, 1));
        // 已翻开的格子不能标记
        Cell cell = field.getCell(1, 1);
        assertEquals(Cell.MARK_NONE, field.cycleMark(1, 1));
        assertEquals(Cell.MARK_NONE, cell.getMark());
    }

    @Test
    public void restoreStateReconstructsGame() {
        MineField field = new MineField(3, 3, Arrays.asList(0, 8));
        field.reveal(0, 1);
        List<Integer> mines = field.mineIndices();
        List<Integer> revealed = field.revealedIndices();
        List<Integer> flags = field.markedIndices(Cell.MARK_FLAG);
        List<Integer> questions = field.markedIndices(Cell.MARK_QUESTION);
        MineField restored = new MineField(3, 3, mines);
        restored.restoreState(revealed, flags, questions, field.isGameOver(), field.isWon());
        assertTrue(restored.getCell(0, 1).isRevealed());
        assertEquals(field.getCell(0, 2).isRevealed(), restored.getCell(0, 2).isRevealed());
    }
}
