package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友列表界面：ListView 展示好友，支持添加好友、长按删除好友，
 * 好友头像可从相册上传（OpenDocument + 持久化读取权限）。
 */
public class FriendActivity extends AppCompatActivity {

    private AccountManager accountManager;
    private FriendStore friendStore;
    private List<Friend> friends;
    private FriendAdapter adapter;
    private TextView tvTitle;

    private LinearLayout dialogAvatarRow;
    private CircleAvatarView dialogPreview;
    private final List<CircleAvatarView> dialogAvatarViews = new ArrayList<>();
    private int dialogSelected = 0;
    private String dialogPickedUri = "";

    private final ActivityResultLauncher<String[]> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onDialogImagePicked);

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
                    accountManager.getSessionAvatarUri(),
                    "我的好友");
        } else {
            userInfoBar.setUser("未登录", "\uD83D\uDC64", 0xFFBDBDBD, null, "我的好友");
        }
        userInfoBar.clearActions();
        userInfoBar.addAction("返回", v -> finish());

        friendStore = new FriendStore(this);
        friends = new ArrayList<>(friendStore.getFriends());
        adapter = new FriendAdapter(this, friends);
        tvTitle = findViewById(R.id.tvFriendTitle);

        ListView listView = findViewById(R.id.friendList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) ->
                Toast.makeText(this, "点击了好友：" + friends.get(position).name, Toast.LENGTH_SHORT).show());
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(position);
            return true;
        });

        ((MaterialButton) findViewById(R.id.btnAddFriend)).setOnClickListener(v -> showAddFriendDialog());
        refreshTitle();
    }

    private void refreshTitle() {
        tvTitle.setText("我的好友（" + friends.size() + "）");
    }

    private void confirmDelete(int position) {
        Friend f = friends.get(position);
        new AlertDialog.Builder(this)
                .setTitle("删除好友")
                .setMessage("确定删除好友“" + f.name + "”吗？")
                .setPositiveButton("删除", (d, w) -> {
                    friendStore.deleteFriend(f.id);
                    friends.remove(position);
                    adapter.notifyDataSetChanged();
                    refreshTitle();
                    Toast.makeText(this, "已删除好友：" + f.name, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showAddFriendDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_friend, null);
        EditText etName = view.findViewById(R.id.etFriendName);
        EditText etSignature = view.findViewById(R.id.etFriendSignature);
        dialogAvatarRow = view.findViewById(R.id.dialogAvatarRow);
        dialogPreview = view.findViewById(R.id.dialogAvatarPreview);
        MaterialButton btnPick = view.findViewById(R.id.btnPickFriendAvatar);

        dialogSelected = 0;
        dialogPickedUri = "";
        dialogAvatarViews.clear();
        dialogAvatarRow.removeAllViews();
        dialogPreview.setAvatar(Avatars.emojiAt(0), Avatars.colorAt(0));

        int size = dp(44);
        int margin = dp(4);
        for (int i = 0; i < Avatars.size(); i++) {
            final int index = i;
            CircleAvatarView av = new CircleAvatarView(this);
            av.setAvatar(Avatars.emojiAt(i), Avatars.colorAt(i));
            av.setHighlight(i == 0);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(margin, margin, margin, margin);
            av.setLayoutParams(lp);
            av.setOnClickListener(v -> {
                dialogSelected = index;
                dialogPickedUri = "";
                dialogPreview.setAvatar(Avatars.emojiAt(index), Avatars.colorAt(index));
                for (int k = 0; k < dialogAvatarViews.size(); k++) {
                    dialogAvatarViews.get(k).setHighlight(k == index);
                }
            });
            dialogAvatarRow.addView(av);
            dialogAvatarViews.add(av);
        }

        btnPick.setOnClickListener(v -> galleryLauncher.launch(new String[]{"image/*"}));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("添加好友")
                .setView(view)
                .setPositiveButton("添加", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "请输入好友昵称", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String signature = etSignature.getText().toString().trim();
                    if (signature.isEmpty()) {
                        signature = "扫雷好友";
                    }
                    Friend f = new Friend(System.currentTimeMillis(), name,
                            Avatars.emojiAt(dialogSelected), Avatars.colorAt(dialogSelected),
                            dialogPickedUri, signature, true);
                    friendStore.addFriend(f);
                    friends.add(0, f);
                    adapter.notifyDataSetChanged();
                    refreshTitle();
                    Toast.makeText(this, "已添加好友：" + name, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }));

        dialog.setOnDismissListener(d -> {
            dialogAvatarRow = null;
            dialogPreview = null;
            dialogAvatarViews.clear();
        });
        dialog.show();
    }

    /** 相册选图回调（添加好友头像）。 */
    private void onDialogImagePicked(Uri uri) {
        if (uri == null) {
            return;
        }
        try {
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {
        }
        dialogPickedUri = uri.toString();
        for (CircleAvatarView av : dialogAvatarViews) {
            av.setHighlight(false);
        }
        if (dialogPreview != null) {
            dialogPreview.setImageUri(dialogPickedUri);
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
