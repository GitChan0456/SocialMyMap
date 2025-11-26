package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class BoardFragment extends Fragment {

    public static final int REQUEST_CODE_WRITE_POST = 101;
    public static final int REQUEST_CODE_POST_DETAIL = 102;
    private List<Post> posts;
    private PostAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        RecyclerView rvPosts = view.findViewById(R.id.rv_posts_fragment);

        // 가짜 데이터 생성
        posts = new ArrayList<>();
        List<Comment> comments1 = new ArrayList<>(Arrays.asList(new Comment("새로운유저", "반가워요!", "2025-11-26 15:01")));
        posts.add(new Post("첫 방문입니다. 잘 부탁드려요!", "안녕하세요, 새로 가입했습니다.", "새로운유저", "2025-11-26 15:00", 15, comments1));

        List<Comment> comments2 = new ArrayList<>(Arrays.asList(new Comment("맛집탐방러", "오! 저도 오늘 날씨 좋아서 공원 다녀왔어요.", "2025-11-26 14:50"), new Comment("길찾기달인", "미세먼지도 없어서 좋네요~", "2025-11-26 14:55")));
        posts.add(new Post("오늘 날씨 정말 좋네요!", "다들 점심 맛있게 드셨나요?", "날씨좋아", "2025-11-26 14:40", 42, comments2));

        // RecyclerView 설정
        adapter = new PostAdapter(posts, this);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_WRITE_POST) {
                // 글쓰기 결과 처리
                String title = data.getStringExtra("title");
                String content = data.getStringExtra("content");
                String timestamp = data.getStringExtra("timestamp");

                SharedPreferences prefs = getActivity().getSharedPreferences("user_prefs", getActivity().MODE_PRIVATE);
                String author = prefs.getString("user_nickname", "익명");

                Post newPost = new Post(title, content, author, timestamp, 0, new ArrayList<>());
                posts.add(0, newPost);
                adapter.notifyItemInserted(0);

            } else if (requestCode == REQUEST_CODE_POST_DETAIL) {
                // 상세보기 결과 처리 (조회수 증가)
                int position = data.getIntExtra("position", -1);
                if (position != -1) {
                    Post post = posts.get(position);
                    post.views++; // 조회수 1 증가
                    adapter.notifyItemChanged(position); // 해당 아이템만 새로고침
                }
            }
        }
    }
}