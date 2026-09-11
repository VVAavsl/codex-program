package com.example.myapplication;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 好友数据模型，支持内置表情头像或相册图片头像。 */
public class Friend {

    public final long id;
    public final String name;
    public final String emoji;
    public final int color;
    public final String imageUri;
    public final String signature;
    public final boolean online;

    public Friend(long id, String name, String emoji, int color, String imageUri,
                  String signature, boolean online) {
        this.id = id;
        this.name = name;
        this.emoji = emoji;
        this.color = color;
        this.imageUri = imageUri;
        this.signature = signature;
        this.online = online;
    }

    public Friend(String name, String emoji, int color, String signature, boolean online) {
        this(Math.abs((name + signature).hashCode()), name, emoji, color, "", signature, online);
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("name", name);
            o.put("emoji", emoji);
            o.put("color", color);
            o.put("uri", imageUri == null ? "" : imageUri);
            o.put("signature", signature);
            o.put("online", online);
        } catch (Exception ignored) {
        }
        return o;
    }

    public static Friend fromJson(JSONObject o) {
        return new Friend(
                o.optLong("id", System.currentTimeMillis()),
                o.optString("name", "好友"),
                o.optString("emoji", Avatars.emojiAt(0)),
                o.optInt("color", Avatars.colorAt(0)),
                o.optString("uri", ""),
                o.optString("signature", ""),
                o.optBoolean("online", false));
    }

    public static List<Friend> demoFriends() {
        List<Friend> list = new ArrayList<>();
        list.add(new Friend("小雷", Avatars.emojiAt(0), Avatars.colorAt(0), "高手扫雷，绝不踩雷", true));
        list.add(new Friend("阿明", Avatars.emojiAt(1), Avatars.colorAt(1), "中级难度 40 秒通关", true));
        list.add(new Friend("喵喵", Avatars.emojiAt(3), Avatars.colorAt(3), "喵 ~ 一起玩扫雷吗", true));
        list.add(new Friend("皮皮", Avatars.emojiAt(4), Avatars.colorAt(4), "正在挑战高级难度", false));
        list.add(new Friend("花花", Avatars.emojiAt(2), Avatars.colorAt(2), "新手求带 ~", true));
        list.add(new Friend("星星", Avatars.emojiAt(7), Avatars.colorAt(7), "喜欢收集最佳纪录", false));
        return list;
    }
}
