package com.example.socialmymap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView rvChatRooms;
    private TextView tvEmptyChat;
    private ChatRoomAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChatRooms = view.findViewById(R.id.rv_chat_rooms);
        tvEmptyChat = view.findViewById(R.id.tv_empty_chat);

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        // 임시 데이터 (나중에 실제 DB 데이터로 교체)
        List<ChatRoomItem> dummyData = new ArrayList<>();

        // 더미 데이터 추가 (단일 항목)
        dummyData.add(new ChatRoomItem("Test", "안녕하세요! 테스트 메시지입니다.", "오후 2:30", 1));

        adapter = new ChatRoomAdapter(dummyData);
        rvChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatRooms.setAdapter(adapter);

        // 빈 상태 표시
        if (dummyData.isEmpty()) {
            rvChatRooms.setVisibility(View.GONE);
            tvEmptyChat.setVisibility(View.VISIBLE);
        } else {
            rvChatRooms.setVisibility(View.VISIBLE);
            tvEmptyChat.setVisibility(View.GONE);
        }
    }

    // 임시 데이터 클래스
    static class ChatRoomItem {
        String friendNickname;
        String lastMessage;
        String time;
        int unreadCount;

        public ChatRoomItem(String friendNickname, String lastMessage, String time, int unreadCount) {
            this.friendNickname = friendNickname;
            this.lastMessage = lastMessage;
            this.time = time;
            this.unreadCount = unreadCount;
        }
    }
}
