package com.example.socialmymap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etUserId, etPassword, etPasswordConfirm, etNickname, etEmail;
    private Button btnSignup, btnCheckUserId, btnCheckNickname;
    private ImageButton btnBack;
    private boolean isUserIdChecked = false;
    private boolean isNicknameChecked = false;
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
        etEmail = findViewById(R.id.et_signup_email);
        btnSignup = findViewById(R.id.btn_signup_submit);
        btnCheckUserId = findViewById(R.id.btn_check_userid);
        btnCheckNickname = findViewById(R.id.btn_check_nickname);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        // 아이디 입력 변경 시 중복확인 상태 초기화
        etUserId.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isUserIdChecked = false;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // 닉네임 입력 변경 시 중복확인 상태 초기화
        etNickname.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isNicknameChecked = false;
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        // 아이디 중복확인 버튼
        btnCheckUserId.setOnClickListener(v -> {
            String userId = etUserId.getText().toString();
            if (userId.isEmpty()) {
                Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userDao.isUserIdExists(userId)) {
                Toast.makeText(this, "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                isUserIdChecked = false;
            } else {
                Toast.makeText(this, "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                isUserIdChecked = true;
            }
        });

        // 닉네임 중복확인 버튼
        btnCheckNickname.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString();
            if (nickname.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (userDao.isNicknameExists(nickname)) {
                Toast.makeText(this, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
                isNicknameChecked = false;
            } else {
                Toast.makeText(this, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show();
                isNicknameChecked = true;
            }
        });

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

            // 중복확인 체크
            if (!isUserIdChecked) {
                Toast.makeText(this, "아이디 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isNicknameChecked) {
                Toast.makeText(this, "닉네임 중복확인을 해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(passwordConfirm)) {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 닉네임 중복 체크
            if (userDao.isNicknameExists(nickname)) {
                Toast.makeText(this, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show();
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
