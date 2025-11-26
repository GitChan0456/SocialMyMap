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

    public static final String EXTRA_AUTHOR_ID = "extra_author_id";
    public static final String EXTRA_AUTHOR_NICK = "extra_author_nick";

    private final List<Post> myPosts = new ArrayList<>();
    private PostAdapter adapter;
    private CommunityDao communityDao;
    private String authorId;
    private String authorNick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        setTitle("내가 쓴 글");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        authorId = getIntent().getStringExtra(EXTRA_AUTHOR_ID);
        authorNick = getIntent().getStringExtra(EXTRA_AUTHOR_NICK);
        if (authorId == null && authorNick == null) {
            finish();
            return;
        }

        communityDao = new CommunityDao(this);

        RecyclerView rv = findViewById(R.id.rv_my_posts);
        adapter = new PostAdapter(myPosts, null); // Fragment null: 어댑터가 직접 context로 이동
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
        myPosts.addAll(communityDao.getPostsByAuthor(authorId, authorNick));
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
