package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
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
    private final List<Comment> comments = new ArrayList<>();
    private EditText etComment;
    private int position = -1;
    private long postId = -1;
    private CommunityDao communityDao;
    private String author;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvTime;
    private TextView tvContent;
    private TextView tvViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        setTitle("게시글");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvTitle = findViewById(R.id.tv_detail_title);
        tvAuthor = findViewById(R.id.tv_detail_author);
        tvTime = findViewById(R.id.tv_detail_time);
        tvContent = findViewById(R.id.tv_detail_content);
        tvViews = findViewById(R.id.tv_detail_views);
        RecyclerView rvComments = findViewById(R.id.rv_comments);
        etComment = findViewById(R.id.et_comment);
        Button btnSubmitComment = findViewById(R.id.btn_submit_comment);

        Intent intent = getIntent();
        postId = intent.getLongExtra("post_id", -1);
        position = intent.getIntExtra("position", -1);

        if (postId == -1) {
            Toast.makeText(this, "게시글 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        communityDao = new CommunityDao(this);

        // 조회수 증가 후 다시 읽기
        communityDao.incrementViews(postId);
        loadPostAndComments();

        adapter = new CommentAdapter(comments);
        rvComments.setAdapter(adapter);
        rvComments.setLayoutManager(new LinearLayoutManager(this));

        btnSubmitComment.setOnClickListener(v -> {
            String commentText = etComment.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "댓글을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            String currentUser = prefs.getString("user_nickname", "익명");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            Comment newComment = new Comment(postId, currentUser, commentText, timestamp);
            communityDao.insertComment(newComment);

            etComment.setText("");
            loadComments();
            rvComments.scrollToPosition(adapter.getItemCount() - 1);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResultAndFinish();
            }
        });
    }

    private void loadPostAndComments() {
        Post post = communityDao.getPostById(postId);
        if (post == null) {
            Toast.makeText(this, "게시글 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTitle.setText(post.title);
        tvAuthor.setText(post.author);
        author = post.author;
        tvContent.setText(post.content);
        tvTime.setText(post.timestamp);
        tvViews.setText(String.valueOf(post.views));
        loadComments();
    }

    private void loadComments() {
        comments.clear();
        comments.addAll(communityDao.getComments(postId));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
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
        } else if (item.getItemId() == R.id.menu_delete_post) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("삭제")
                    .setMessage("이 게시글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (d, w) -> {
                        communityDao.deletePost(postId);
                        setResult(RESULT_OK);
                        finish();
                    })
                    .setNegativeButton("취소", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_post_detail, menu);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentUser = prefs.getString("user_nickname", "익명");
        boolean isAuthor = currentUser.equals(author);
        MenuItem deleteItem = menu.findItem(R.id.menu_delete_post);
        deleteItem.setVisible(isAuthor);
        return true;
    }
}
