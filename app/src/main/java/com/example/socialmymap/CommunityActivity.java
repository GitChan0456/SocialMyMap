package com.example.socialmymap;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

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

        fabWritePost.setOnClickListener(v -> Toast.makeText(this, "글쓰기", Toast.LENGTH_SHORT).show());
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
        List<Post> posts = new ArrayList<>();
        posts.add(new Post("첫 방문입니다. 잘 부탁드려요!", "안녕하세요, 새로 가입했습니다. 이 게시판은 UI가 정말 깔끔하네...", "새로운유저", 15, 2));
        posts.add(new Post("오늘 날씨 정말 좋네요!", "다들 점심 맛있게 드셨나요? 날씨가 좋아서 산책하기 딱입니다.", "날씨좋아", 42, 5));
        posts.add(new Post("혹시 이 근처 맛집 아시는 분?", "회사 근처에서 저녁 먹으려고 하는데, 다들 자주 가는 맛집 있으...", "배고픈개발자", 120, 22));
        posts.add(new Post("주말에 같이 코딩할 사람 구해요", "프로젝트 같이 진행하실 분 찾습니다. 리액트 네이티브 경험자 우대...", "코딩중독", 7, 1));

        // RecyclerView 설정
        PostAdapter adapter = new PostAdapter(posts);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // 뒤로가기 버튼 동작
        return true;
    }
}
