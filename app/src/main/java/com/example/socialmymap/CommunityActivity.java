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
import java.util.Arrays;
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

        // --- 가짜 데이터 생성 (게시글별로 다른 댓글) ---
        List<Post> posts = new ArrayList<>();

        List<Comment> comments1 = new ArrayList<>(Arrays.asList(
                new Comment("새로운유저", "반가워요!", "1분 전")
        ));
        posts.add(new Post("첫 방문입니다. 잘 부탁드려요!", "안녕하세요, 새로 가입했습니다. 이 게시판은 UI가 정말 깔끔하네...", "새로운유저", 15, comments1));

        List<Comment> comments2 = new ArrayList<>(Arrays.asList(
                new Comment("맛집탐방러", "오! 저도 오늘 날씨 좋아서 공원 다녀왔어요.", "10분 전"),
                new Comment("길찾기달인", "미세먼지도 없어서 좋네요~", "5분 전")
        ));
        posts.add(new Post("오늘 날씨 정말 좋네요!", "다들 점심 맛있게 드셨나요? 날씨가 좋아서 산책하기 딱입니다.", "날씨좋아", 42, comments2));

        List<Comment> comments3 = new ArrayList<>(Arrays.asList(
                new Comment("배고픈개발자", "저도 궁금해요!", "20분 전"),
                new Comment("지도매니아", "사창사거리에 있는 OO치킨 맛있어요.", "15분 전"),
                new Comment("지도매니아", "사창사거리에 있는 OO치킨 맛있어요.", "15분 전"),
                new Comment("새로운유저", "치킨 땡기네요!", "1분 전"),
                new Comment("날씨좋아", "저도 한표요!", "방금 전")
        ));
        posts.add(new Post("혹시 이 근처 맛집 아시는 분?", "회사 근처에서 저녁 먹으려고 하는데, 다들 자주 가는 맛집 있으...", "배고픈개발자", 120, comments3));

        List<Comment> comments4 = new ArrayList<>(); // 댓글 없음
        posts.add(new Post("주말에 같이 코딩할 사람 구해요", "프로젝트 같이 진행하실 분 찾습니다. 리액트 네이티브 경험자 우대...", "코딩중독", 7, comments4));


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