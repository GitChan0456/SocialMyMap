package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunityActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_POST = 101;
    private List<Post> posts;
    private PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        Toolbar toolbar = findViewById(R.id.toolbar_community);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvPosts = findViewById(R.id.rv_posts);
        FloatingActionButton fabWritePost = findViewById(R.id.fab_write_post);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        fabWritePost.setOnClickListener(v -> {
            Intent intent = new Intent(this, WritePostActivity.class);
            startActivityForResult(intent, REQUEST_CODE_WRITE_POST);
        });

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_board) {
                Toast.makeText(this, "게시판 탭", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.nav_chat) {
                Toast.makeText(this, "채팅 탭", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        // 가짜 데이터 생성
        posts = new ArrayList<>();
        List<Comment> comments1 = new ArrayList<>(Arrays.asList(new Comment("새로운유저", "반가워요!", "2025-11-26 15:01")));
        posts.add(new Post("첫 방문입니다. 잘 부탁드려요!", "안녕하세요, 새로 가입했습니다.", "새로운유저", "2025-11-26 15:00", 15, comments1));

        List<Comment> comments2 = new ArrayList<>(Arrays.asList(new Comment("맛집탐방러", "오! 저도 오늘 날씨 좋아서 공원 다녀왔어요.", "2025-11-26 14:50"), new Comment("길찾기달인", "미세먼지도 없어서 좋네요~", "2025-11-26 14:55")));
        posts.add(new Post("오늘 날씨 정말 좋네요!", "다들 점심 맛있게 드셨나요?", "날씨좋아", "2025-11-26 14:40", 42, comments2));
        
        // RecyclerView 설정
        adapter = new PostAdapter(posts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_WRITE_POST && resultCode == RESULT_OK && data != null) {
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            String timestamp = data.getStringExtra("timestamp");

            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String author = prefs.getString("user_nickname", "익명");

            Post newPost = new Post(title, content, author, timestamp, 0, new ArrayList<>());
            posts.add(0, newPost);
            adapter.notifyItemInserted(0);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}