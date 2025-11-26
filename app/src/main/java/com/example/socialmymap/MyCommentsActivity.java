package com.example.socialmymap;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyCommentsActivity extends AppCompatActivity {

    public static final String EXTRA_AUTHOR = "extra_author";

    private final List<MyCommentItem> items = new ArrayList<>();
    private MyCommentsAdapter adapter;
    private CommunityDao communityDao;
    private String author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_comments);
        setTitle("내 댓글");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        author = getIntent().getStringExtra(EXTRA_AUTHOR);
        if (author == null) {
            finish();
            return;
        }

        communityDao = new CommunityDao(this);
        RecyclerView rv = findViewById(R.id.rv_my_comments);
        adapter = new MyCommentsAdapter(items);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        loadComments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadComments();
    }

    private void loadComments() {
        items.clear();
        items.addAll(communityDao.getCommentsByAuthor(author));
        adapter.notifyDataSetChanged();
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
