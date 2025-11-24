package com.example.socialmymap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileEditActivity extends AppCompatActivity {

    private EditText etNickname, etEmail, etPassword, etPasswordConfirm;
    private Button btnSubmit;
    private UserDao userDao;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        setTitle("프로필 수정");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userDao = new UserDao(this);
        userDao.open();

        etNickname = findViewById(R.id.et_edit_nickname);
        etEmail = findViewById(R.id.et_edit_email);
        etPassword = findViewById(R.id.et_edit_password);
        etPasswordConfirm = findViewById(R.id.et_edit_password_confirm);
        btnSubmit = findViewById(R.id.btn_edit_submit);

        // 현재 사용자 정보 불러와서 EditText에 설정
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", null);
        String currentNickname = prefs.getString("user_nickname", "");
        String currentEmail = prefs.getString("user_email", "");

        etNickname.setText(currentNickname);
        etEmail.setText(currentEmail);

        btnSubmit.setOnClickListener(v -> {
            String newNickname = etNickname.getText().toString();
            String newEmail = etEmail.getText().toString();
            String newPassword = etPassword.getText().toString();
            String newPasswordConfirm = etPasswordConfirm.getText().toString();

            if (newNickname.isEmpty()) {
                Toast.makeText(this, "닉네임은 비워둘 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 새 비밀번호 입력 시에만 일치 여부 확인
            if (!newPassword.isEmpty() && !newPassword.equals(newPasswordConfirm)) {
                Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUserId != null) {
                int updatedRows = userDao.updateUser(currentUserId, newNickname, newEmail, newPassword);
                if (updatedRows > 0) {
                    // SharedPreferences도 업데이트
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("user_nickname", newNickname);
                    editor.putString("user_email", newEmail);
                    editor.apply();

                    Toast.makeText(this, "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish(); // 수정 완료 후 마이페이지로 돌아감
                } else {
                    Toast.makeText(this, "프로필 수정에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    protected void onDestroy() {
        userDao.close();
        super.onDestroy();
    }
}
