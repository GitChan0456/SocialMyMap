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
        List<Comment> comments = (List<Comment>) intent.getSerializableExtra("comments");

        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvContent.setText(content);
        tvTime.setText("5분 전"); // 임시 시간

        // 전달받은 댓글 리스트로 RecyclerView 설정
        if (comments != null && !comments.isEmpty()) {
            CommentAdapter adapter = new CommentAdapter(comments);
            rvComments.setAdapter(adapter);
            rvComments.setLayoutManager(new LinearLayoutManager(this));
        } else {
            // 댓글이 없을 경우의 처리 (예: "첫 댓글을 남겨보세요" 메시지 표시)
        }


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
