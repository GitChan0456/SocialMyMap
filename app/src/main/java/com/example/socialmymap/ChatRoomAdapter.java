package com.example.socialmymap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private final List<ChatRoom> chatRoomList;
    private final OnChatRoomClickListener clickListener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatRoom chatRoom);
    }

    public ChatRoomAdapter(List<ChatRoom> chatRoomList, OnChatRoomClickListener clickListener) {
        this.chatRoomList = chatRoomList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatRoom chatRoom = chatRoomList.get(position);

        holder.tvFriendNickname.setText(chatRoom.friendNickname);
        holder.tvLastMessage.setText(chatRoom.lastMessage);
        holder.tvTime.setText(formatTime(chatRoom.lastMessageTime));

        if (chatRoom.unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(chatRoom.unreadCount));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onChatRoomClick(chatRoom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    private String formatTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // 1분 미만
            return "방금 전";
        } else if (diff < 3600000) { // 1시간 미만
            return (diff / 60000) + "분 전";
        } else if (diff < 86400000) { // 1일 미만
            SimpleDateFormat sdf = new SimpleDateFormat("a h:mm", Locale.KOREAN);
            return sdf.format(new Date(timestamp));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("M월 d일", Locale.KOREAN);
            return sdf.format(new Date(timestamp));
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendNickname;
        TextView tvLastMessage;
        TextView tvTime;
        TextView tvUnreadCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendNickname = itemView.findViewById(R.id.tv_friend_nickname);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}
