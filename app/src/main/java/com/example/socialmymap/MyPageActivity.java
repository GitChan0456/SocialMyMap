package com.example.socialmymap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MyPageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        setTitle("마이페이지");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvNickname = findViewById(R.id.tv_nickname);
        TextView tvUserEmail = findViewById(R.id.tv_user_email);

        // SharedPreferences에서 사용자 정보 가져오기
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String nickname = prefs.getString("user_nickname", "사용자");
        String email = prefs.getString("user_email", null); // 값이 없을 경우 null 반환

        tvNickname.setText(nickname);

        // 이메일 정보 유무에 따라 텍스트 설정
        if (email == null || email.isEmpty()) {
            tvUserEmail.setText("이메일 정보를 입력해주세요.");
        } else {
            tvUserEmail.setText(email);
        }
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