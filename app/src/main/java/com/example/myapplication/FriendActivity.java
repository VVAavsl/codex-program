package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

/** 好友列表界面：使用 ListView 展示好友（头像/昵称/签名/状态）。 */
public class FriendActivity extends AppCompatActivity {

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        accountManager = new AccountManager(this);
        UserInfoBar userInfoBar = findViewById(R.id.userInfoBar);
        if (accountManager.isLoggedIn()) {
            userInfoBar.setUser(accountManager.getSessionUsername(),
                    accountManager.getSessionEmoji(),
                    accountManager.getSessionColor(),
                    "我的好友");
        } else {
            userInfoBar.setUser("未登录", "\uD83D\uDC64", 0xFFBDBDBD, "我的好友");
        }
        userInfoBar.clearActions();
        userInfoBar.addAction("返回", v -> finish());

        List<Friend> friends = Friend.demoFriends();
        TextView title = findViewById(R.id.tvFriendTitle);
        title.setText("我的好友（" + friends.size() + "）");

        FriendAdapter adapter = new FriendAdapter(this, friends);
        ListView listView = findViewById(R.id.friendList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Friend f = friends.get(position);
            Toast.makeText(this, "点击了好友：" + f.name, Toast.LENGTH_SHORT).show();
        });
    }
}
