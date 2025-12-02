package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private UserDao userDao;
    private EditText etUserId, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userDao = new UserDao(this);
        userDao.open();

        etUserId = findViewById(R.id.et_userid);
        etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(v -> {
            String userId = etUserId.getText().toString();
            String password = etPassword.getText().toString();

            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDao.loginUser(userId, password)) {
                UserData userData = userDao.getUserData(userId);
                HomeRegionData homeData = userDao.getHomeRegion(userId);
                
                SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("user_id", userId);
                editor.putString("user_nickname", userData.nickname);
                editor.putString("user_email", userData.email);
                
                // 우리 동네 정보도 로드
                if (homeData != null) {
                    editor.putString("home_region", homeData.region);
                    editor.putString("home_lat", String.valueOf(homeData.latitude));
                    editor.putString("home_lng", String.valueOf(homeData.longitude));
                }
                
                editor.putBoolean("is_logged_in", true);
                editor.apply();

                Toast.makeText(this, userData.nickname + "님 환영합니다!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "아이디 또는 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        userDao.close();
        super.onDestroy();
    }
}
