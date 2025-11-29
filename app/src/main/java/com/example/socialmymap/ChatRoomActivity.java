package com.example.socialmymap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends AppCompatActivity {

    private String friendUserId;
    private String friendNickname;
    private String currentUserId;
    private String currentUserNickname;

    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private Button btnSendMessage;

    private ChatDao chatDao;
    private ChatRoom chatRoom;
    private MessageAdapter messageAdapter;
    private List<Message> messages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Intent에서 친구 정보 받기
        friendUserId = getIntent().getStringExtra("friend_user_id");
        friendNickname = getIntent().getStringExtra("friend_name");

        if (friendUserId == null || friendNickname == null) {
            Toast.makeText(this, "채팅방 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(friendNickname);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 현재 사용자 정보
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");
        currentUserNickname = prefs.getString("user_nickname", "사용자");

        // View 초기화
        rvMessages = findViewById(R.id.rv_messages);
        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);

        // ChatDao 초기화
        chatDao = new ChatDao(this);
        chatDao.open();

        // 채팅방 가져오기 또는 생성
        chatRoom = chatDao.getOrCreateChatRoom(currentUserId, friendUserId, friendNickname);

        // 메시지 목록 설정
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        // 기존 메시지 로드
        loadMessages();

        // 전송 버튼 리스너
        btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        messages.clear();
        messages.addAll(chatDao.getMessages(chatRoom.id, currentUserId));
        messageAdapter.notifyDataSetChanged();

        // 최신 메시지로 스크롤
        if (!messages.isEmpty()) {
            rvMessages.scrollToPosition(messages.size() - 1);
        }
    }

    private void sendMessage() {
        String messageText = etMessageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // 메시지 객체 생성
        Message message = new Message();
        message.chatRoomId = chatRoom.id;
        message.senderId = currentUserId;
        message.senderNickname = currentUserNickname;
        message.messageText = messageText;
        message.timestamp = System.currentTimeMillis();
        message.isMine = true;

        // DB에 저장
        long messageId = chatDao.insertMessage(message);
        message.id = messageId;

        // 채팅방의 마지막 메시지 업데이트
        chatDao.updateChatRoomLastMessage(chatRoom.id, messageText, message.timestamp);

        // 화면에 추가
        messages.add(message);
        messageAdapter.notifyItemInserted(messages.size() - 1);
        rvMessages.scrollToPosition(messages.size() - 1);

        // 입력창 비우기
        etMessageInput.setText("");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (chatDao != null) {
            chatDao.close();
        }
        super.onDestroy();
    }
}
