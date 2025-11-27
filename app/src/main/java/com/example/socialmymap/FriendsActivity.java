package com.example.socialmymap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView rvFriends;
    private TextView tvEmpty;
    private FriendsAdapter adapter;
    private final List<Friend> friendList = new ArrayList<>();
    private FriendsDao friendsDao;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        setTitle("친구 관리");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rvFriends = findViewById(R.id.rv_friends);
        tvEmpty = findViewById(R.id.tv_empty_friends);

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");

        friendsDao = new FriendsDao(this);
        friendsDao.open();

        adapter = new FriendsAdapter(friendList,
                (friend, position) -> {
                    // 친구 삭제
                    boolean success = friendsDao.deleteFriend(friend.id);
                    if (success) {
                        friendList.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(this, "친구가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        updateEmptyView();
                    } else {
                        Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                (friend) -> {
                    // 채팅 시작
                    android.content.Intent intent = new android.content.Intent(this, ChatRoomActivity.class);
                    intent.putExtra("friend_name", friend.friendNickname);
                    startActivity(intent);
                });

        rvFriends.setAdapter(adapter);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        loadFriends();
    }

    private void loadFriends() {
        friendList.clear();
        friendList.addAll(friendsDao.getFriends(currentUserId));
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (friendList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFriends.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFriends.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        friendsDao.close();
        super.onDestroy();
    }
}
