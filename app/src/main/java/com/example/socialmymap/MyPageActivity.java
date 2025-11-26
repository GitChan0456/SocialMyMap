package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MyPageActivity extends AppCompatActivity {

    private UserDao userDao;
    private CommunityDao communityDao;
    private TextView tvNickname, tvUserEmail, tvMyPostsCount, tvMyCommentsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        setTitle("마이페이지");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 시스템 인셋을 그대로 패딩에 반영 (상단 겹침 허용 시 조정 가능)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scroll_view_my_page), (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        userDao = new UserDao(this);
        userDao.open();
        communityDao = new CommunityDao(this);

        tvNickname = findViewById(R.id.tv_nickname);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvMyPostsCount = findViewById(R.id.tv_my_posts_count);
        tvMyCommentsCount = findViewById(R.id.tv_my_comments_count);
        TextView tvLogout = findViewById(R.id.tv_logout);
        TextView tvDeleteAccount = findViewById(R.id.tv_delete_account);
        TextView tvProfileEdit = findViewById(R.id.tv_profile_edit);
        View tvMyPosts = findViewById(R.id.layout_my_posts);
        View tvMyComments = findViewById(R.id.layout_my_comments);

        tvProfileEdit.setOnClickListener(v -> startActivity(new Intent(this, ProfileEditActivity.class)));

        tvMyPosts.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String userId = prefs.getString("user_id", "");
            String nickname = prefs.getString("user_nickname", "");
            Intent intent = new Intent(this, MyPostsActivity.class);
            intent.putExtra(MyPostsActivity.EXTRA_AUTHOR_ID, userId);
            intent.putExtra(MyPostsActivity.EXTRA_AUTHOR_NICK, nickname);
            startActivity(intent);
        });

        tvMyComments.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String userId = prefs.getString("user_id", "");
            String nickname = prefs.getString("user_nickname", "");
            Intent intent = new Intent(this, MyCommentsActivity.class);
            intent.putExtra(MyCommentsActivity.EXTRA_AUTHOR_ID, userId);
            intent.putExtra(MyCommentsActivity.EXTRA_AUTHOR_NICK, nickname);
            startActivity(intent);
        });

        tvLogout.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
        });

        tvDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("회원 탈퇴")
                    .setMessage("모든 정보가 삭제됩니다. 탈퇴하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        String userId = prefs.getString("user_id", null);
                        if (userId != null) {
                            userDao.deleteUser(userId);
                            prefs.edit().clear().apply();
                            startActivity(new Intent(this, LoginActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
        String userId = prefs.getString("user_id", "");
        String nickname = prefs.getString("user_nickname", "사용자");
        String email = prefs.getString("user_email", null);

        tvNickname.setText(nickname);
        tvUserEmail.setText(email == null || email.isEmpty() ? "이메일 정보를 입력해 주세요." : email);

        // id 우선, 닉네임도 함께 조회 (이전 데이터 호환)
        int postCount = communityDao.countPostsByAuthor(userId, nickname);
        tvMyPostsCount.setText(String.valueOf(postCount));

        int commentCount = communityDao.countCommentsByAuthor(userId, nickname);
        tvMyCommentsCount.setText(String.valueOf(commentCount));
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
