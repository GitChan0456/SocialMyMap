package com.example.socialmymap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etUserId, etPassword, etPasswordConfirm, etNickname, etEmail;
    private Button btnSignup;
    private ImageButton btnBack;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        userDao = new UserDao(this);
        userDao.open();

        etUserId = findViewById(R.id.et_signup_userid);
        etPassword = findViewById(R.id.et_signup_password);
        etPasswordConfirm = findViewById(R.id.et_signup_password_confirm);
        etNickname = findViewById(R.id.et_signup_nickname);
        etEmail = findViewById(R.id.et_signup_email); // email 필드 추가
        btnSignup = findViewById(R.id.btn_signup_submit);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> {
            String userId = etUserId.getText().toString();
            String password = etPassword.getText().toString();
            String passwordConfirm = etPasswordConfirm.getText().toString();
            String nickname = etNickname.getText().toString();
            String email = etEmail.getText().toString(); // email 값 가져오기

            // 아이디, 비밀번호, 닉네임은 필수
            if (userId.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "아이디, 비밀번호, 닉네임은 필수입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDao.insertUser(userId, password, nickname, email)) {
                Toast.makeText(this, "회원가입 성공! 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "이미 존재하는 아이디입니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        userDao.close();
        super.onDestroy();
    }
}
