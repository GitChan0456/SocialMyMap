package com.example.socialmymap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private RecyclerView rvFriends;
    private TextView tvEmpty;
    private FriendsAdapter adapter;
    private List<Friend> friendList;
    private FriendsDao friendsDao;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        rvFriends = view.findViewById(R.id.rv_friends);
        tvEmpty = view.findViewById(R.id.tv_empty_friends);

        // 현재 사용자 ID
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        currentUserId = prefs.getString("user_id", "");

        // FriendsDao 초기화
        friendsDao = new FriendsDao(requireContext());
        friendsDao.open();

        // 어댑터 설정
        friendList = new ArrayList<>();
        adapter = new FriendsAdapter(friendList,
                (friend, position) -> {
                    // 친구 삭제 (Fragment에서는 Toast 사용)
                    boolean success = friendsDao.deleteFriend(friend.id);
                    if (success) {
                        friendList.remove(position);
                        adapter.notifyItemRemoved(position);
                        android.widget.Toast.makeText(getContext(), "친구가 삭제되었습니다.", android.widget.Toast.LENGTH_SHORT)
                                .show();
                        updateEmptyView();
                    } else {
                        android.widget.Toast.makeText(getContext(), "삭제 실패", android.widget.Toast.LENGTH_SHORT).show();
                    }
                },
                (friend) -> {
                    // 채팅 시작
                    android.content.Intent intent = new android.content.Intent(getActivity(), ChatRoomActivity.class);
                    intent.putExtra("friend_user_id", friend.friendUserId);
                    intent.putExtra("friend_name", friend.friendNickname);
                    startActivity(intent);
                });

        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFriends.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
    public void onDestroy() {
        if (friendsDao != null) {
            friendsDao.close();
        }
        super.onDestroy();
    }
}
