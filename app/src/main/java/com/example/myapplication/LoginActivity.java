package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** 登录/注册界面：用户名 + 密码 + 头像选择（内置头像或相册上传）。 */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private TextView tvError;
    private TextView tvToggle;
    private TextView tvHint;
    private TextView tvAvatarState;
    private MaterialButton btnLogin;
    private MaterialButton btnPickAvatar;
    private UserInfoBar userInfoBar;
    private GridLayout avatarGrid;

    private final List<CircleAvatarView> avatarViews = new ArrayList<>();
    private final ActivityResultLauncher<String[]> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onAvatarPicked);

    private AccountManager accountManager;
    private boolean registerMode = false;
    private int selectedAvatar = 0;
    private String pickedAvatarUri = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        accountManager = new AccountManager(this);
        bindViews();
        buildAvatarGrid();
        setupListeners();
        updatePreview();
        updateModeUi();

        if (accountManager.isLoggedIn() && savedInstanceState == null) {
            enterGame();
        }
    }

    private void bindViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tvError = findViewById(R.id.tvError);
        tvToggle = findViewById(R.id.tvToggle);
        tvHint = findViewById(R.id.tvHint);
        tvAvatarState = findViewById(R.id.tvAvatarState);
        btnLogin = findViewById(R.id.btnLogin);
        btnPickAvatar = findViewById(R.id.btnPickAvatar);
        userInfoBar = findViewById(R.id.userInfoBar);
        avatarGrid = findViewById(R.id.avatarGrid);
    }

    private void buildAvatarGrid() {
        avatarGrid.removeAllViews();
        avatarViews.clear();
        int size = dp(56);
        int margin = dp(6);
        for (int i = 0; i < Avatars.size(); i++) {
            final int index = i;
            CircleAvatarView av = new CircleAvatarView(this);
            av.setAvatar(Avatars.emojiAt(i), Avatars.colorAt(i));
            av.setHighlight(i == selectedAvatar);
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
            lp.width = size;
            lp.height = size;
            lp.setMargins(margin, margin, margin, margin);
            lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            av.setLayoutParams(lp);
            av.setOnClickListener(v -> selectAvatar(index));
            avatarGrid.addView(av);
            avatarViews.add(av);
        }
    }

    private void selectAvatar(int index) {
        selectedAvatar = index;
        pickedAvatarUri = "";
        tvAvatarState.setVisibility(View.GONE);
        for (int i = 0; i < avatarViews.size(); i++) {
            avatarViews.get(i).setHighlight(i == index);
        }
        updatePreview();
    }

    /** 相册选图回调：保存持久化读取权限并立即预览。 */
    private void onAvatarPicked(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        pickedAvatarUri = uri.toString();
        for (CircleAvatarView av : avatarViews) {
            av.setHighlight(false);
        }
        tvAvatarState.setVisibility(View.VISIBLE);
        updatePreview();
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> submit());
        btnPickAvatar.setOnClickListener(v -> galleryLauncher.launch(new String[]{"image/*"}));
        tvToggle.setOnClickListener(v -> {
            registerMode = !registerMode;
            updateModeUi();
        });
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                updatePreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateModeUi() {
        if (registerMode) {
            btnLogin.setText("注册并登录");
            tvToggle.setText("已有账号？点此登录");
            tvHint.setText("注册新账号：用户名唯一，注册后自动登录");
        } else {
            btnLogin.setText("登录");
            tvToggle.setText("没有账号？点此注册新用户");
            tvHint.setText("演示账号：demo   密码：123456");
        }
        hideError();
        updatePreview();
    }

    private void updatePreview() {
        String name = etUsername.getText().toString().trim();
        if (name.isEmpty()) {
            name = "用户名";
        }
        String sub = registerMode ? "新用户 · 选择头像注册" : "登录后开始扫雷";
        if (!pickedAvatarUri.isEmpty()) {
            userInfoBar.setUser(name, "", 0, pickedAvatarUri, sub);
        } else {
            userInfoBar.setUser(name, Avatars.emojiAt(selectedAvatar), Avatars.colorAt(selectedAvatar), null, sub);
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void submit() {
        String name = etUsername.getText().toString().trim();
        String pwd = etPassword.getText().toString().trim();
        if (name.isEmpty() || pwd.isEmpty()) {
            showError("用户名和密码不能为空");
            return;
        }
        if (name.contains(" ") || name.length() > 16) {
            showError("用户名格式不正确（≤16 个字符，不含空格）");
            return;
        }

        String emoji = Avatars.emojiAt(selectedAvatar);
        int color = Avatars.colorAt(selectedAvatar);
        int result;
        if (registerMode) {
            result = accountManager.register(name, pwd, emoji, color, pickedAvatarUri);
            if (result == AccountManager.RESULT_NAME_EXISTS) {
                showError("该用户名已存在，请直接登录");
                return;
            }
        } else {
            result = accountManager.login(name, pwd, emoji, color, pickedAvatarUri);
            if (result == AccountManager.RESULT_NOT_FOUND) {
                showError("账号不存在，可点击下方“注册新用户”创建");
                return;
            }
            if (result == AccountManager.RESULT_WRONG_PASSWORD) {
                showError("密码错误，请重试");
                return;
            }
        }
        Toast.makeText(this, "欢迎，" + name + "！", Toast.LENGTH_SHORT).show();
        enterGame();
    }

    private void enterGame() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_USERNAME, accountManager.getSessionUsername());
        intent.putExtra(MainActivity.EXTRA_AVATAR_EMOJI, accountManager.getSessionEmoji());
        intent.putExtra(MainActivity.EXTRA_AVATAR_COLOR, accountManager.getSessionColor());
        intent.putExtra(MainActivity.EXTRA_AVATAR_URI, accountManager.getSessionAvatarUri());
        startActivity(intent);
        finish();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
