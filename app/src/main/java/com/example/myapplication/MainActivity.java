package com.example.myapplication;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 扫雷主界面：状态栏 + 棋盘 + 底部控制。 */
public class MainActivity extends AppCompatActivity implements GridAdapter.OnCellActionListener {

    public static final int DIFF_EASY = 0;
    public static final int DIFF_MEDIUM = 1;
    public static final int DIFF_HARD = 2;

    private static final int[] ROWS = {9, 16, 16};
    private static final int[] COLS = {9, 16, 30};
    private static final int[] MINE_COUNTS = {10, 40, 99};
    private static final String[] DIFF_LABELS = {"初级", "中级", "高级"};

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_AVATAR_EMOJI = "extra_avatar_emoji";
    public static final String EXTRA_AVATAR_COLOR = "extra_avatar_color";


    private static final String KEY_DIFF = "difficulty";
    private static final String KEY_PLACED = "placed";
    private static final String KEY_MINES = "mines";
    private static final String KEY_REVEALED = "revealed";
    private static final String KEY_FLAGS = "flags";
    private static final String KEY_QUESTIONS = "questions";
    private static final String KEY_STARTED = "started";
    private static final String KEY_OVER = "over";
    private static final String KEY_WON = "won";
    private static final String KEY_ELAPSED = "elapsed";

    private TextView tvDifficulty;
    private TextView tvMines;
    private TextView tvTime;
    private TextView tvBest;
    private Spinner spinnerDifficulty;
    private RecyclerView grid;

    private MineField field;
    private GridAdapter adapter;
    private GameTimer timer;
    private SharedPrefsManager prefs;
    private AccountManager accountManager;
    private UserInfoBar userInfoBar;

    private int difficulty = DIFF_EASY;
    private int cellSizePx = 40;
    private boolean started = false;
    private boolean finished = false;
    private boolean uiReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        accountManager = new AccountManager(this);
        userInfoBar = findViewById(R.id.userInfoBar);
        setupUserBar();

        prefs = new SharedPrefsManager(this);
        timer = new GameTimer();
        timer.setListener(this::updateTimerText);

        bindViews();
        difficulty = prefs.getDefaultDifficulty();
        if (savedInstanceState != null) {
            difficulty = savedInstanceState.getInt(KEY_DIFF, difficulty);
        }
        spinnerDifficulty.setSelection(difficulty);

        computeCellSize();
        if (savedInstanceState != null) {
            restoreGame(savedInstanceState);
        } else {
            startNewGame();
        }
        uiReady = true;
        updateBestDisplay();
    }

    private void bindViews() {
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvMines = findViewById(R.id.tvMines);
        tvTime = findViewById(R.id.tvTime);
        tvBest = findViewById(R.id.tvBest);
        grid = findViewById(R.id.grid);
        grid.setHasFixedSize(true);
        grid.setNestedScrollingEnabled(false);

        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        ArrayAdapter<String> diffAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DIFF_LABELS);
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(diffAdapter);
        spinnerDifficulty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!uiReady || position == difficulty) {
                    return;
                }
                changeDifficulty(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        MaterialButton btnRestart = findViewById(R.id.btnRestart);
        btnRestart.setOnClickListener(v -> startNewGame());
    }

    private void computeCellSize() {
        float density = getResources().getDisplayMetrics().density;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cols = COLS[difficulty];
        int maxCell = Math.round(46f * density);
        int minCell = Math.round(16f * density);
        int cell = screenWidth / cols;
        cell = Math.max(minCell, Math.min(cell, maxCell));
        cellSizePx = cell;
    }

    private void startNewGame() {
        field = new MineField(ROWS[difficulty], COLS[difficulty], MINE_COUNTS[difficulty]);
        started = false;
        finished = false;
        timer.reset();
        buildGrid();
        tvDifficulty.setText(DIFF_LABELS[difficulty]);
        updateMinesLabel();
        updateTimerText(0);
        updateBestDisplay();
    }

    private void buildGrid() {
        int rows = ROWS[difficulty];
        int cols = COLS[difficulty];
        ScrollView.LayoutParams lp = new ScrollView.LayoutParams(cols * cellSizePx, rows * cellSizePx);
        grid.setLayoutParams(lp);
        grid.setLayoutManager(new GridLayoutManager(this, cols));
        adapter = new GridAdapter(field, cellSizePx, this);
        grid.setAdapter(adapter);
        adapter.refreshAll();
    }

    private void changeDifficulty(int d) {
        if (d < DIFF_EASY || d > DIFF_HARD) {
            return;
        }
        difficulty = d;
        prefs.setDefaultDifficulty(d);
        spinnerDifficulty.setSelection(d);
        computeCellSize();
        if (uiReady) {
            startNewGame();
        }
    }

    // ---------- 点击交互 ----------

    @Override
    public void onCellClick(int row, int col) {
        if (finished || field == null) {
            return;
        }
        boolean firstReveal = !started;
        int result = field.reveal(row, col);
        if (firstReveal) {
            started = true;
            timer.start();
        }
        if (result == MineField.REVEAL_IGNORED) {
            return;
        }
        adapter.refreshAll();
        updateMinesLabel();
        if (field.isGameOver()) {
            finished = true;
            timer.stop();
            if (field.isWon()) {
                onWin();
            } else {
                onLose();
            }
        }
    }

    @Override
    public void onCellLongClick(int row, int col) {
        if (finished || field == null) {
            return;
        }
        field.cycleMark(row, col);
        adapter.refreshAll();
        updateMinesLabel();
    }

    // ---------- 用户信息 ----------

    private void setupUserBar() {
        String name = getIntent().getStringExtra(EXTRA_USERNAME);
        String emoji = getIntent().getStringExtra(EXTRA_AVATAR_EMOJI);
        int color = getIntent().getIntExtra(EXTRA_AVATAR_COLOR, -1);
        if ((name == null || name.isEmpty()) && accountManager.isLoggedIn()) {
            name = accountManager.getSessionUsername();
            emoji = accountManager.getSessionEmoji();
            color = accountManager.getSessionColor();
        }
        if (name == null || name.isEmpty()) {
            name = "未登录";
        }
        if (emoji == null || emoji.isEmpty()) {
            emoji = "\uD83D\uDC64";
        }
        if (color == -1) {
            color = 0xFFBDBDBD;
        }
        userInfoBar.setUser(name, emoji, color, "已登录 · 开始扫雷");
        userInfoBar.clearActions();
        userInfoBar.addAction("好友", v -> startActivity(new Intent(this, FriendActivity.class)));
        userInfoBar.addAction("退出", 0xFFC62828, v -> confirmLogout());
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定退出当前账号吗？")
                .setPositiveButton("退出", (d, w) -> {
                    accountManager.clearSession();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // ---------- 显示更新 ----------

    private void updateMinesLabel() {
        int remaining = MINE_COUNTS[difficulty] - field.countFlags();
        tvMines.setText("\uD83D\uDCA3 " + remaining);
    }

    private void updateTimerText(int seconds) {
        if (tvTime != null) {
            tvTime.setText(formatTime(seconds));
        }
    }

    private void updateBestDisplay() {
        int best = prefs.getBestTime(difficulty);
        tvBest.setText("最佳纪录：" + (best > 0 ? formatTime(best) : "--"));
    }

    private static String formatTime(int seconds) {
        return String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60);
    }

    // ---------- 胜负 ----------

    private void onWin() {
        int seconds = timer.getElapsedSeconds();
        boolean newRecord = prefs.saveBestTimeIfBetter(difficulty, seconds);
        prefs.addWin();
        updateBestDisplay();
        String message = "用时 " + formatTime(seconds) + (newRecord ? "\n\uD83C\uDFC6 新纪录！" : "");
        new AlertDialog.Builder(this)
                .setTitle("\uD83C\uDF89 胜利！")
                .setMessage(message)
                .setPositiveButton("再来一局", (d, w) -> startNewGame())
                .setNegativeButton("查看棋盘", null)
                .setCancelable(false)
                .show();
    }

    private void onLose() {
        String message = "踩到地雷了！\n用时 " + formatTime(timer.getElapsedSeconds());
        new AlertDialog.Builder(this)
                .setTitle("\uD83D\uDCA3 游戏结束")
                .setMessage(message)
                .setPositiveButton("再来一局", (d, w) -> startNewGame())
                .setNegativeButton("查看棋盘", null)
                .setCancelable(false)
                .show();
    }

    // ---------- 生命周期与状态保存 ----------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_DIFF, difficulty);
        outState.putInt(KEY_ELAPSED, timer.getElapsedSeconds());
        outState.putBoolean(KEY_STARTED, started);
        outState.putBoolean(KEY_OVER, finished);
        outState.putBoolean(KEY_WON, field != null && field.isWon());
        outState.putBoolean(KEY_PLACED, field != null && field.isMinesPlaced());
        if (field != null && field.isMinesPlaced()) {
            outState.putIntArray(KEY_MINES, toIntArray(field.mineIndices()));
            outState.putIntArray(KEY_REVEALED, toIntArray(field.revealedIndices()));
            outState.putIntArray(KEY_FLAGS, toIntArray(field.markedIndices(Cell.MARK_FLAG)));
            outState.putIntArray(KEY_QUESTIONS, toIntArray(field.markedIndices(Cell.MARK_QUESTION)));
        }
    }

    private void restoreGame(Bundle s) {
        started = s.getBoolean(KEY_STARTED, false);
        finished = s.getBoolean(KEY_OVER, false);
        boolean placed = s.getBoolean(KEY_PLACED, false);
        int rows = ROWS[difficulty];
        int cols = COLS[difficulty];
        if (placed) {
            List<Integer> mineList = toList(s.getIntArray(KEY_MINES));
            field = new MineField(rows, cols, mineList);
            field.restoreState(
                    toList(s.getIntArray(KEY_REVEALED)),
                    toList(s.getIntArray(KEY_FLAGS)),
                    toList(s.getIntArray(KEY_QUESTIONS)),
                    finished,
                    s.getBoolean(KEY_WON, false));
        } else {
            field = new MineField(rows, cols, MINE_COUNTS[difficulty]);
        }
        buildGrid();
        timer.setElapsedSeconds(s.getInt(KEY_ELAPSED, 0));
        if (started && !finished) {
            timer.start();
        }
        tvDifficulty.setText(DIFF_LABELS[difficulty]);
        updateMinesLabel();
        updateTimerText(timer.getElapsedSeconds());
        updateBestDisplay();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (started && !finished && timer != null && !timer.isRunning()) {
            timer.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (timer != null) {
            timer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.stop();
        }
        super.onDestroy();
    }

    private static List<Integer> toList(int[] array) {
        List<Integer> list = new ArrayList<>();
        if (array != null) {
            for (int value : array) {
                list.add(value);
            }
        }
        return list;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
