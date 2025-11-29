package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private ChatDao chatDao;
    private List<ChatRoom> chatRoomList;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        rvChatRooms = view.findViewById(R.id.rv_chat_rooms);
        tvEmptyChat = view.findViewById(R.id.tv_empty_chat);

        // 현재 사용자 ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");

        // ChatDao 초기화
        chatDao = new ChatDao(requireContext());
        chatDao.open();

        // 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chatRoomList, chatRoom -> {
            // 채팅방 클릭 시
            Intent intent = new Intent(getActivity(), ChatRoomActivity.class);
            intent.putExtra("friend_user_id", chatRoom.friendUserId);
            intent.putExtra("friend_name", chatRoom.friendNickname);
            startActivity(intent);
        });

        rvChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatRooms.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadChatRooms();
    }

    private void loadChatRooms() {
        chatRoomList.clear();
        chatRoomList.addAll(chatDao.getAllChatRooms(currentUserId));
        adapter.notifyDataSetChanged();

        // 빈 상태 표시
        if (chatRoomList.isEmpty()) {
            rvChatRooms.setVisibility(View.GONE);
            tvEmptyChat.setVisibility(View.VISIBLE);
        } else {
            rvChatRooms.setVisibility(View.VISIBLE);
            tvEmptyChat.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        if (chatDao != null) {
            chatDao.close();
        }
        super.onDestroy();
    }
}
