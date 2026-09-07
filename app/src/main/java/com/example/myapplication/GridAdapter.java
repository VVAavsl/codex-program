package com.example.myapplication;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/** RecyclerView 适配器：把 MineField 每个格子绘制成一个小 TextView。 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.CellHolder> {

    public interface OnCellActionListener {
        void onCellClick(int row, int col);

        void onCellLongClick(int row, int col);
    }

    private final MineField field;
    private final int cellSizePx;
    private final OnCellActionListener listener;

    public GridAdapter(MineField field, int cellSizePx, OnCellActionListener listener) {
        this.field = field;
        this.cellSizePx = cellSizePx;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CellHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cell, parent, false);
        ViewGroup.LayoutParams lp = tv.getLayoutParams();
        lp.width = cellSizePx;
        lp.height = cellSizePx;
        return new CellHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull CellHolder holder, int position) {
        int row = position / field.getCols();
        int col = position % field.getCols();
        Cell cell = field.getCell(row, col);
        TextView tv = holder.tv;
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, cellSizePx * 0.46f);
        tv.setText("");
        tv.setTextColor(0xFF111111);

        if (cell.isRevealed()) {
            if (cell.isMine()) {
                if (field.isGameOver() && !field.isWon()) {
                    tv.setBackgroundResource(R.drawable.bg_cell_mine);
                    tv.setText("\uD83D\uDCA3");
                } else {
                    tv.setBackgroundResource(R.drawable.bg_cell_revealed);
                }
            } else {
                if (field.isGameOver() && field.isWon()) {
                    tv.setBackgroundResource(R.drawable.bg_cell_win);
                } else {
                    tv.setBackgroundResource(R.drawable.bg_cell_revealed);
                }
                int n = cell.getAdjacentMines();
                if (n > 0) {
                    tv.setText(String.valueOf(n));
                    tv.setTextColor(numberColor(n));
                }
            }
        } else {
            tv.setBackgroundResource(R.drawable.bg_cell_hidden);
            if (cell.isFlagged()) {
                tv.setText("\uD83D\uDEA9");
            } else if (cell.isQuestioned()) {
                tv.setText("\u2753");
            }
        }

        tv.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCellClick(row, col);
            }
        });
        tv.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCellLongClick(row, col);
            }
            return true;
        });
        // 按压缩放反馈
        tv.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(60L).start();
            } else if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(90L).start();
            }
            return false;
        });
    }

    /** 数字颜色：1蓝 2绿 3红 4深蓝 5深红 6青 7黑 8灰。 */
    private int numberColor(int n) {
        switch (n) {
            case 1:  return 0xFF0000FF;
            case 2:  return 0xFF008000;
            case 3:  return 0xFFFF0000;
            case 4:  return 0xFF000080;
            case 5:  return 0xFF800000;
            case 6:  return 0xFF008080;
            case 7:  return 0xFF000000;
            case 8:  return 0xFF808080;
            default: return 0xFF111111;
        }
    }

    public void refreshAll() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return field.getRows() * field.getCols();
    }

    static class CellHolder extends RecyclerView.ViewHolder {
        final TextView tv;

        CellHolder(TextView tv) {
            super(tv);
            this.tv = tv;
        }
    }
}
