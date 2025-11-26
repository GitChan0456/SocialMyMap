package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MyPageActivity extends AppCompatActivity {

    private UserDao userDao;
    private CommunityDao communityDao;
    private TextView tvNickname, tvUserEmail, tvPostCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        setTitle("마이페이지");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userDao = new UserDao(this);
        userDao.open();
        communityDao = new CommunityDao(this);

        tvNickname = findViewById(R.id.tv_nickname);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvPostCount = findViewById(R.id.tv_post_count);
        TextView tvLogout = findViewById(R.id.tv_logout);
        TextView tvDeleteAccount = findViewById(R.id.tv_delete_account);
        TextView tvProfileEdit = findViewById(R.id.tv_profile_edit);
        TextView tvManageFavorites = findViewById(R.id.tv_manage_favorites);
        findViewById(R.id.layout_my_posts).setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, MyPostsActivity.class);
            intent.putExtra(MyPostsActivity.EXTRA_AUTHOR, tvNickname.getText().toString());
            startActivity(intent);
        });

        tvProfileEdit.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        });

        tvManageFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(MyPageActivity.this, FavoritesActivity.class);
            startActivity(intent);
        });

        tvLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        });

        tvDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("회원 탈퇴")
                    .setMessage("모든 정보가 삭제됩니다. 정말 탈퇴하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        String userId = prefs.getString("user_id", null);
                        if (userId != null) {
                            userDao.deleteUser(userId);

                            SharedPreferences.Editor editor = prefs.edit();
                            editor.clear();
                            editor.apply();

                            Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String nickname = prefs.getString("user_nickname", "사용자");
        String email = prefs.getString("user_email", null);

        tvNickname.setText(nickname);
        if (email == null || email.isEmpty()) {
            tvUserEmail.setText("이메일 정보를 입력해 주세요.");
        } else {
            tvUserEmail.setText(email);
        }

        // 내가 작성한 커뮤니티 글 수
        int postCount = communityDao.countPostsByAuthor(nickname);
        tvPostCount.setText(String.valueOf(postCount));
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
