package com.example.socialmymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        setTitle("게시글");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView tvTitle = findViewById(R.id.tv_detail_title);
        TextView tvAuthor = findViewById(R.id.tv_detail_author);
        TextView tvTime = findViewById(R.id.tv_detail_time);
        TextView tvContent = findViewById(R.id.tv_detail_content);
        RecyclerView rvComments = findViewById(R.id.rv_comments);
        Button btnSubmitComment = findViewById(R.id.btn_submit_comment);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String content = intent.getStringExtra("content");

        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvContent.setText(content);
        tvTime.setText("5분 전"); // 임시 시간

        // 가짜 댓글 데이터 생성
        List<Comment> comments = new ArrayList<>();
        comments.add(new Comment("배고픈개발자", "저도 그 집 가봤는데 정말 맛있어요!", "3분 전"));
        comments.add(new Comment("새로운유저", "좋은 정보 감사합니다~", "1분 전"));

        // RecyclerView 설정
        CommentAdapter adapter = new CommentAdapter(comments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        // 댓글 등록 버튼 리스너
        btnSubmitComment.setOnClickListener(v -> {
            Toast.makeText(this, "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
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
}