package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** 好友列表本地存储（SharedPreferences + JSON），支持增删。 */
public class FriendStore {

    private static final String PREFS = "friend_prefs";
    private static final String KEY = "friends";

    private final SharedPreferences sp;

    public FriendStore(Context context) {
        sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!sp.contains(KEY)) {
            saveAll(Friend.demoFriends());
        }
    }

    public List<Friend> getFriends() {
        List<Friend> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(sp.getString(KEY, "[]"));
            for (int i = 0; i < arr.length(); i++) {
                list.add(Friend.fromJson(arr.getJSONObject(i)));
            }
        } catch (Exception ignored) {
        }
        return list;
    }

    public void addFriend(Friend friend) {
        List<Friend> list = getFriends();
        list.add(0, friend);
        saveAll(list);
    }

    public void deleteFriend(long id) {
        List<Friend> list = getFriends();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id == id) {
                list.remove(i);
                break;
            }
        }
        saveAll(list);
    }

    private void saveAll(List<Friend> list) {
        JSONArray arr = new JSONArray();
        for (Friend f : list) {
            arr.put(f.toJson());
        }
        sp.edit().putString(KEY, arr.toString()).apply();
    }
}
