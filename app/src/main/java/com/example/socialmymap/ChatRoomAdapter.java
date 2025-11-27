package com.example.socialmymap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private final List<ChatFragment.ChatRoomItem> chatRoomList;

    public ChatRoomAdapter(List<ChatFragment.ChatRoomItem> chatRoomList) {
        this.chatRoomList = chatRoomList;
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
        ChatFragment.ChatRoomItem item = chatRoomList.get(position);

        holder.tvFriendNickname.setText(item.friendNickname);
        holder.tvLastMessage.setText(item.lastMessage);
        holder.tvTime.setText(item.time);

        if (item.unreadCount > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(item.unreadCount));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        // TODO: 채팅방 클릭 리스너 추가
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
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
