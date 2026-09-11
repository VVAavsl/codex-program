package com.example.myapplication;

/** 内置头像目录：每套头像 = 表情字符 + 背景色，便于本地存储与跨页面传递。 */
public final class Avatars {

    public static final String[] EMOJIS = {
            "\uD83D\uDE00", "\uD83D\uDE0E", "\uD83E\uDD73", "\uD83D\uDC31",
            "\uD83D\uDC36", "\uD83E\uDD8A", "\uD83D\uDC3C", "\uD83D\uDE80"
    };

    public static final int[] COLORS = {
            0xFFEF5350, 0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726,
            0xFFAB47BC, 0xFF26A69A, 0xFF5C6BC0, 0xFF8D6E63
    };

    private Avatars() {
    }

    public static int size() {
        return EMOJIS.length;
    }

    public static String emojiAt(int index) {
        return EMOJIS[index % EMOJIS.length];
    }

    public static int colorAt(int index) {
        return COLORS[index % COLORS.length];
    }
}
