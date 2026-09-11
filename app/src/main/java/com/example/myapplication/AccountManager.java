package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

/**
 * 本地账号与登录会话管理。
 * 账号以 JSON 形式保存在 SharedPreferences 中；内置演示账号 demo/123456。
 */
public class AccountManager {

    public static final int RESULT_OK = 0;
    public static final int RESULT_NAME_EXISTS = 1;
    public static final int RESULT_NOT_FOUND = 2;
    public static final int RESULT_WRONG_PASSWORD = 3;

    private static final String PREFS_NAME = "account_prefs";
    private static final String KEY_ACCOUNTS = "accounts";
    private static final String KEY_SESSION_USER = "session_user";
    private static final String KEY_SESSION_EMOJI = "session_emoji";
    private static final String KEY_SESSION_COLOR = "session_color";

    private static final String FIELD_PWD = "pwd";
    private static final String FIELD_EMOJI = "emoji";
    private static final String FIELD_COLOR = "color";

    private final SharedPreferences sp;

    public AccountManager(Context context) {
        sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureDemoAccount();
    }

    private void ensureDemoAccount() {
        if (!accountExists("demo")) {
            // 仅创建演示账号，不写入登录会话
            createAccount("demo", "123456", Avatars.emojiAt(0), Avatars.colorAt(0));
        }
    }

    private JSONObject accounts() {
        try {
            String raw = sp.getString(KEY_ACCOUNTS, "{}");
            return new JSONObject(raw);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private void saveAccounts(JSONObject obj) {
        sp.edit().putString(KEY_ACCOUNTS, obj.toString()).apply();
    }

    public boolean accountExists(String username) {
        return accounts().has(username);
    }

    public int register(String username, String password, String emoji, int color) {
        int result = createAccount(username, password, emoji, color);
        if (result == RESULT_OK) {
            saveSession(username, emoji, color);
        }
        return result;
    }

    /** 只创建账号，不写登录会话（供演示账号预置等场景使用）。 */
    private int createAccount(String username, String password, String emoji, int color) {
        JSONObject all = accounts();
        if (all.has(username)) {
            return RESULT_NAME_EXISTS;
        }
        try {
            JSONObject acc = new JSONObject();
            acc.put(FIELD_PWD, password);
            acc.put(FIELD_EMOJI, emoji);
            acc.put(FIELD_COLOR, color);
            all.put(username, acc);
            saveAccounts(all);
            return RESULT_OK;
        } catch (Exception e) {
            return RESULT_NAME_EXISTS;
        }
    }

    /**
     * 登录：校验密码，成功后把本次选择的头像同步到该账号，
     * 并把用户名 + 头像写入会话，供扫雷游戏显示。
     */
    public int login(String username, String password, String emoji, int color) {
        JSONObject all = accounts();
        if (!all.has(username)) {
            return RESULT_NOT_FOUND;
        }
        try {
            JSONObject acc = all.getJSONObject(username);
            if (!acc.optString(FIELD_PWD).equals(password)) {
                return RESULT_WRONG_PASSWORD;
            }
            acc.put(FIELD_EMOJI, emoji);
            acc.put(FIELD_COLOR, color);
            all.put(username, acc);
            saveAccounts(all);
            saveSession(username, emoji, color);
            return RESULT_OK;
        } catch (Exception e) {
            return RESULT_NOT_FOUND;
        }
    }

    public void saveSession(String username, String emoji, int color) {
        sp.edit()
                .putString(KEY_SESSION_USER, username)
                .putString(KEY_SESSION_EMOJI, emoji)
                .putInt(KEY_SESSION_COLOR, color)
                .apply();
    }

    public boolean isLoggedIn() {
        String user = sp.getString(KEY_SESSION_USER, "");
        return user != null && !user.isEmpty();
    }

    public String getSessionUsername() {
        return sp.getString(KEY_SESSION_USER, "");
    }

    public String getSessionEmoji() {
        return sp.getString(KEY_SESSION_EMOJI, Avatars.emojiAt(0));
    }

    public int getSessionColor() {
        return sp.getInt(KEY_SESSION_COLOR, Avatars.colorAt(0));
    }

    public void clearSession() {
        sp.edit().remove(KEY_SESSION_USER).remove(KEY_SESSION_EMOJI).remove(KEY_SESSION_COLOR).apply();
    }
}
