package com.example.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

/**
 * 自定义复合控件：用户信息条（头像 + 用户名 + 副标题 + 右侧操作）。
 * 在登录页、扫雷主界面、好友列表等所有 Activity 中统一使用。
 */
public class UserInfoBar extends LinearLayout {

    private CircleAvatarView avatarView;
    private TextView tvName;
    private TextView tvSub;
    private LinearLayout actionContainer;

    public UserInfoBar(Context context) {
        super(context);
        init(context);
    }

    public UserInfoBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UserInfoBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(10);
        setPadding(dp(14), pad, dp(6), pad);
        setBackgroundColor(ContextCompat.getColor(context, R.color.user_bar_bg));
        LayoutInflater.from(context).inflate(R.layout.view_user_info_bar, this, true);
        avatarView = findViewById(R.id.avatarView);
        tvName = findViewById(R.id.tvName);
        tvSub = findViewById(R.id.tvSub);
        actionContainer = findViewById(R.id.actionContainer);
    }

    /** 显示某用户：头像 + 昵称 + 副标题（内置表情头像）。 */
    public void setUser(String name, String emoji, int color, String subtitle) {
        setUser(name, emoji, color, "", subtitle);
    }

    /** 显示某用户：imageUri 非空时优先使用相册图片头像。 */
    public void setUser(String name, String emoji, int color, String imageUri, String subtitle) {
        if (imageUri != null && !imageUri.isEmpty()) {
            avatarView.setImageUri(imageUri);
        } else {
            avatarView.setAvatar(emoji, color);
        }
        tvName.setText(name == null || name.isEmpty() ? "未登录" : name);
        tvSub.setText(subtitle == null ? "" : subtitle);
    }

    public void setSubtitle(String subtitle) {
        tvSub.setText(subtitle == null ? "" : subtitle);
    }

    public void clearActions() {
        actionContainer.removeAllViews();
    }

    public void addAction(String text, View.OnClickListener listener) {
        addAction(text, ContextCompat.getColor(getContext(), R.color.user_action), listener);
    }

    public void addAction(String text, int color, View.OnClickListener listener) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(6), dp(10), dp(6));
        tv.setOnClickListener(listener);
        actionContainer.addView(tv);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
