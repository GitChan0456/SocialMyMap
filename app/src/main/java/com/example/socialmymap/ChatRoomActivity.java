package com.example.socialmymap;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ChatRoomActivity extends AppCompatActivity {

    private String friendName;
    private EditText etMessageInput;
    private Button btnSendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Intent에서 친구 이름 받기
        friendName = getIntent().getStringExtra("friend_name");
        if (friendName == null) {
            friendName = "알 수 없는 사용자";
        }
        setTitle(friendName);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etMessageInput = findViewById(R.id.et_message_input);
        btnSendMessage = findViewById(R.id.btn_send_message);

        btnSendMessage.setOnClickListener(v -> {
            String message = etMessageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                Toast.makeText(this, "메시지 전송: " + message, Toast.LENGTH_SHORT).show();
                etMessageInput.setText("");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
