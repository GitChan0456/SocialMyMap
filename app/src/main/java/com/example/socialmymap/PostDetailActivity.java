package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {

    private CommentAdapter adapter;
    private List<Comment> comments;
    private EditText etComment;
    private int position = -1;

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
        TextView tvViews = findViewById(R.id.tv_detail_views);
        RecyclerView rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);
        Button btnSubmitComment = findViewById(R.id.btn_submit_comment);

        Intent intent = getIntent();
        Post post = (Post) intent.getSerializableExtra("post");
        position = intent.getIntExtra("position", -1);

        if (post == null) {
            Toast.makeText(this, "게시글 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        comments = post.comments;
        if (comments == null) {
            comments = new ArrayList<>();
        }

        tvTitle.setText(post.title);
        tvAuthor.setText(post.author);
        tvContent.setText(post.content);
        tvTime.setText(post.timestamp);
        tvViews.setText(String.valueOf(post.views));

        adapter = new CommentAdapter(comments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        btnSubmitComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "댓글을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String currentUser = prefs.getString("user_nickname", "익명");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            Comment newComment = new Comment(currentUser, commentText, timestamp);
            adapter.addComment(newComment);
            etComment.setText("");
            rvComments.scrollToPosition(adapter.getItemCount() - 1);
        });

        // 결과 반환 로직 설정
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });
    }

    private void setResultAndFinish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("position", position);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResultAndFinish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}