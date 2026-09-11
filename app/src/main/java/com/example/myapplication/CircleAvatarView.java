package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.io.InputStream;

/**
 * 自绘圆形头像控件：
 * 1) 无图片时绘制彩色圆底 + 表情字符；
 * 2) 设置相册图片后按圆形裁剪绘制本地图片。
 */
public class CircleAvatarView extends View {

    private String emoji = "\uD83D\uDC64";
    private int bgColor = 0xFFBDBDBD;
    private boolean highlight = false;
    private Bitmap imageBitmap;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path clipPath = new Path();

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

    /** 使用内置表情头像。 */
    public void setAvatar(String emoji, int bgColor) {
        this.emoji = emoji == null ? "" : emoji;
        this.bgColor = bgColor;
        this.imageBitmap = null;
        setContentDescription("\u5934\u50cf" + this.emoji);
        invalidate();
    }

    /** 使用相册图片作为头像（uri 为空则忽略）。 */
    public void setImageUri(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return;
        }
        Bitmap bitmap = decodeSampled(Uri.parse(uriString));
        if (bitmap != null) {
            this.imageBitmap = bitmap;
            setContentDescription("\u5934\u50cf\u56fe\u7247");
            invalidate();
        }
    }

    public String getImageUriString() {
        return imageBitmap == null ? "" : "bitmap";
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
        invalidate();
    }

    private Bitmap decodeSampled(Uri uri) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            InputStream in1 = getContext().getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(in1, null, bounds);
            if (in1 != null) {
                in1.close();
            }
            int target = getWidth() > 0 ? getWidth() : 256;
            int sample = 1;
            while (bounds.outWidth / sample > target * 2 && bounds.outHeight / sample > target * 2) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            InputStream in2 = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in2, null, opts);
            if (in2 != null) {
                in2.close();
            }
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) / 2f;
        radius -= (highlight ? 6f : 3f);

        if (imageBitmap != null) {
            clipPath.reset();
            clipPath.addCircle(cx, cy, radius, Path.Direction.CW);
            canvas.save();
            canvas.clipPath(clipPath);
            float scale = Math.max(radius * 2f / imageBitmap.getWidth(),
                    radius * 2f / imageBitmap.getHeight());
            float dw = imageBitmap.getWidth() * scale;
            float dh = imageBitmap.getHeight() * scale;
            RectF dst = new RectF(cx - dw / 2f, cy - dh / 2f, cx + dw / 2f, cy + dh / 2f);
            canvas.drawBitmap(imageBitmap, null, dst, imagePaint);
            canvas.restore();
        } else {
            bgPaint.setColor(bgColor);
            canvas.drawCircle(cx, cy, radius, bgPaint);
        }

        canvas.drawCircle(cx, cy, radius - 1.5f, ringPaint);

        if (highlight) {
            canvas.drawCircle(cx, cy, radius + 3f, highlightPaint);
        }

        if (imageBitmap == null && !emoji.isEmpty()) {
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(radius * 1.35f);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float baseline = cy - (fm.ascent + fm.descent) / 2f;
            canvas.drawText(emoji, cx, baseline, textPaint);
        }
    }
}
