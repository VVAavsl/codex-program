package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

/** SharedPreferences 封装：三档最佳时间、总胜利次数、默认难度。 */
public class SharedPrefsManager {

    private static final String PREFS_NAME = "minesweeper_prefs";
    private static final String[] BEST_KEYS = {
            "best_time_easy", "best_time_medium", "best_time_hard"
    };
    private static final String KEY_TOTAL_WINS = "total_wins";
    private static final String KEY_DEFAULT_DIFFICULTY = "default_difficulty";

    private final SharedPreferences sp;

    public SharedPrefsManager(Context context) {
        sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** 返回某难度最佳时间（秒），0 表示尚无纪录。 */
    public int getBestTime(int difficulty) {
        return sp.getInt(BEST_KEYS[difficulty], 0);
    }

    /** 若更短则更新纪录，返回是否创造了新纪录。 */
    public boolean saveBestTimeIfBetter(int difficulty, int seconds) {
        int old = getBestTime(difficulty);
        if (old == 0 || seconds < old) {
            sp.edit().putInt(BEST_KEYS[difficulty], seconds).apply();
            return true;
        }
        return false;
    }

    public int getTotalWins() {
        return sp.getInt(KEY_TOTAL_WINS, 0);
    }

    public void addWin() {
        sp.edit().putInt(KEY_TOTAL_WINS, getTotalWins() + 1).apply();
    }

    public int getDefaultDifficulty() {
        return sp.getInt(KEY_DEFAULT_DIFFICULTY, MainActivity.DIFF_EASY);
    }

    public void setDefaultDifficulty(int difficulty) {
        sp.edit().putInt(KEY_DEFAULT_DIFFICULTY, difficulty).apply();
    }
}
