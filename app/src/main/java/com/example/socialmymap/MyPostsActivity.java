package com.example.socialmymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {

    public static final String EXTRA_AUTHOR = "extra_author";

    private final List<Post> myPosts = new ArrayList<>();
    private PostAdapter adapter;
    private CommunityDao communityDao;
    private String author;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        setTitle("내가 쓴 글");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        author = getIntent().getStringExtra(EXTRA_AUTHOR);
        if (author == null) {
            finish();
            return;
        }

        communityDao = new CommunityDao(this);

        RecyclerView rv = findViewById(R.id.rv_my_posts);
        adapter = new PostAdapter(myPosts, null); // Fragment null: 클릭으로 상세 이동은 직접 처리
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        loadMyPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyPosts();
    }

    private void loadMyPosts() {
        myPosts.clear();
        myPosts.addAll(communityDao.getPostsByAuthor(author));
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
