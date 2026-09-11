package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/** 好友列表适配器：ListView 数据源。 */
public class FriendAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final List<Friend> friends;

    public FriendAdapter(Context context, List<Friend> friends) {
        this.inflater = LayoutInflater.from(context);
        this.friends = friends;
    }

    @Override
    public int getCount() {
        return friends == null ? 0 : friends.size();
    }

    @Override
    public Friend getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_friend, parent, false);
            holder = new ViewHolder();
            holder.avatar = convertView.findViewById(R.id.friendAvatar);
            holder.name = convertView.findViewById(R.id.friendName);
            holder.signature = convertView.findViewById(R.id.friendSignature);
            holder.status = convertView.findViewById(R.id.friendStatus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Friend f = getItem(position);
        holder.avatar.setAvatar(f.emoji, f.color);
        holder.name.setText(f.name);
        holder.signature.setText(f.signature);
        if (f.online) {
            holder.status.setText("在线");
            holder.status.setTextColor(0xFF2E7D32);
        } else {
            holder.status.setText("离线");
            holder.status.setTextColor(0xFF9E9E9E);
        }
        return convertView;
    }

    static class ViewHolder {
        CircleAvatarView avatar;
        TextView name;
        TextView signature;
        TextView status;
    }
}
