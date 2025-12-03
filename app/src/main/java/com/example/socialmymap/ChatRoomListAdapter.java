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

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ViewHolder> {

    private List<ChatFragment.ChatRoomItem> chatRoomList;
    private OnChatRoomClickListener listener;

    public interface OnChatRoomClickListener {
        void onChatRoomClick(ChatFragment.ChatRoomItem chatRoomItem);
    }

    public ChatRoomListAdapter(List<ChatFragment.ChatRoomItem> chatRoomList, OnChatRoomClickListener listener) {
        this.chatRoomList = chatRoomList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_room, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatFragment.ChatRoomItem item = chatRoomList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_chat_room_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        void bind(ChatFragment.ChatRoomItem item, OnChatRoomClickListener listener) {
            tvName.setText(item.otherUserName);
            tvLastMessage.setText(item.lastMessage);

            // 시간 포맷
            if (item.lastMessageTime > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                tvTime.setText(sdf.format(new Date(item.lastMessageTime)));
            } else {
                tvTime.setText("");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChatRoomClick(item);
                }
            });
        }
    }
}
