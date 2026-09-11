package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

/** 自绘圆形头像控件：Canvas 画彩色圆底 + 白色描边 + 表情字符，可选选中高亮环。 */
public class CircleAvatarView extends View {

    private String emoji = "\uD83D\uDC64";
    private int bgColor = 0xFFBDBDBD;
    private boolean highlight = false;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleAvatarView(Context context) {
        super(context);
        init();
    }

    public CircleAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(3f);
        ringPaint.setColor(0xFFFFFFFF);
        highlightPaint.setStyle(Paint.Style.STROKE);
        highlightPaint.setStrokeWidth(6f);
        highlightPaint.setColor(ContextCompat.getColor(getContext(), R.color.avatar_select));
    }

    public void setAvatar(String emoji, int bgColor) {
        this.emoji = emoji == null ? "" : emoji;
        this.bgColor = bgColor;
        setContentDescription("头像" + this.emoji);
        invalidate();
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        radius -= (highlight ? 6f : 3f);

        bgPaint.setColor(bgColor);
        canvas.drawCircle(cx, cy, radius, bgPaint);

        canvas.drawCircle(cx, cy, radius - 1.5f, ringPaint);

        if (highlight) {
            canvas.drawCircle(cx, cy, radius + 3f, highlightPaint);
        }

        if (!emoji.isEmpty()) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(radius * 1.35f);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(emoji, cx, baseline, textPaint);
        }
    }
}
