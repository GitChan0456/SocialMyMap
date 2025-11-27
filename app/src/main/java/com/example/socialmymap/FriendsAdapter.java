package com.example.socialmymap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.ViewHolder> {

    private final List<Friend> friendList;
    private final OnFriendDeleteListener deleteListener;
    private final OnFriendChatListener chatListener;

    public interface OnFriendDeleteListener {
        void onFriendDelete(Friend friend, int position);
    }

    public interface OnFriendChatListener {
        void onFriendChat(Friend friend);
    }

    public FriendsAdapter(List<Friend> friendList, OnFriendDeleteListener deleteListener,
            OnFriendChatListener chatListener) {
        this.friendList = friendList;
        this.deleteListener = deleteListener;
        this.chatListener = chatListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.tvNickname.setText(friend.friendNickname);

        // 날짜 포맷팅
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateStr = sdf.format(new Date(friend.createdAt));
        holder.tvDate.setText(dateStr);

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("친구 삭제")
                    .setMessage(friend.friendNickname + "님을 친구 목록에서 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        if (deleteListener != null) {
                            deleteListener.onFriendDelete(friend, position);
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        holder.btnChat.setOnClickListener(v -> {
            if (chatListener != null) {
                chatListener.onFriendChat(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNickname, tvDate;
        Button btnDelete, btnChat;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tv_friend_nickname);
            tvDate = itemView.findViewById(R.id.tv_friend_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_friend);
            btnChat = itemView.findViewById(R.id.btn_chat_friend);
        }
    }
}
