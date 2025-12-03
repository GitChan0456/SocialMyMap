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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView rvChatRooms;
    private TextView tvEmptyChat;
    private ChatRoomListAdapter adapter;
    private List<ChatRoomItem> chatRoomList;
    private String currentUserId;
    private DatabaseReference chatsRef;

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

        // Firebase 초기화
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        // 어댑터 설정
        chatRoomList = new ArrayList<>();
        adapter = new ChatRoomListAdapter(chatRoomList, chatRoomItem -> {
            // 채팅방 클릭 시
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("roomId", chatRoomItem.roomId);
            intent.putExtra("otherUserName", chatRoomItem.otherUserName);
            startActivity(intent);
        });

        rvChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        rvChatRooms.setAdapter(adapter);

        // 채팅방 목록 로드
        loadChatRooms();

        return view;
    }

    private void loadChatRooms() {
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomList.clear();

                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    String roomId = roomSnapshot.getKey();

                    // roomId에서 현재 사용자가 포함되어 있는지 확인
                    if (roomId != null && roomId.contains(currentUserId)) {
                        // 상대방 ID 추출
                        String[] userIds = roomId.split("_");
                        String otherUserId = userIds[0].equals(currentUserId) ? userIds[1] : userIds[0];

                        // 마지막 메시지 가져오기
                        DataSnapshot messagesSnapshot = roomSnapshot.child("messages");
                        String lastMessage = "";
                        long lastMessageTime = 0;
                        String otherUserName = otherUserId; // 기본값

                        if (messagesSnapshot.exists()) {
                            for (DataSnapshot msgSnapshot : messagesSnapshot.getChildren()) {
                                ChatMessage msg = msgSnapshot.getValue(ChatMessage.class);
                                if (msg != null) {
                                    if (msg.timestamp > lastMessageTime) {
                                        lastMessage = msg.message;
                                        lastMessageTime = msg.timestamp;
                                    }
                                    // 상대방 이름 찾기
                                    if (!msg.senderId.equals(currentUserId)) {
                                        otherUserName = msg.senderName;
                                    }
                                }
                            }
                        }

                        // 채팅방 아이템 생성
                        ChatRoomItem item = new ChatRoomItem();
                        item.roomId = roomId;
                        item.otherUserId = otherUserId;
                        item.otherUserName = otherUserName;
                        item.lastMessage = lastMessage.isEmpty() ? "메시지를 보내보세요" : lastMessage;
                        item.lastMessageTime = lastMessageTime;

                        chatRoomList.add(item);
                    }
                }

                adapter.notifyDataSetChanged();
                updateEmptyView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                updateEmptyView();
            }
        });
    }

    private void updateEmptyView() {
        if (chatRoomList.isEmpty()) {
            rvChatRooms.setVisibility(View.GONE);
            tvEmptyChat.setVisibility(View.VISIBLE);
        } else {
            rvChatRooms.setVisibility(View.VISIBLE);
            tvEmptyChat.setVisibility(View.GONE);
        }
    }

    // 채팅방 아이템 클래스
    public static class ChatRoomItem {
        public String roomId;
        public String otherUserId;
        public String otherUserName;
        public String lastMessage;
        public long lastMessageTime;
    }
}
