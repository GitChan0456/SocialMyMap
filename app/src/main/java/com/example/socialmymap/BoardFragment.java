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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class BoardFragment extends Fragment {

    public static final int REQUEST_CODE_WRITE_POST = 101;
    public static final int REQUEST_CODE_POST_DETAIL = 102;

    private PostAdapter adapter;
    private final List<Post> posts = new ArrayList<>();
    private CommunityDao communityDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);

        communityDao = new CommunityDao(requireContext());

        RecyclerView rvPosts = view.findViewById(R.id.rv_posts_fragment);
        adapter = new PostAdapter(posts, this);
        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));

        loadPosts();
        return view;
    }

    private void loadPosts() {
        posts.clear();
        posts.addAll(communityDao.getAllPosts());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_WRITE_POST) {
            // 새 글 작성 완료 후 목록 새로고침
            loadPosts();
        } else if (requestCode == REQUEST_CODE_POST_DETAIL) {
            // 상세에서 돌아왔을 때 조회수/댓글수 반영
            loadPosts();
        }
    }

    /**
     * 외부에서 테스트용 더미 데이터를 넣고 싶을 때 사용할 수 있는 헬퍼.
     * 실제 사용 시에는 WritePostActivity에서 DB에 저장하므로 필요 없음.
     */
    private void insertDummyIfEmpty() {
        if (!communityDao.getAllPosts().isEmpty()) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", requireActivity().MODE_PRIVATE);
        String author = prefs.getString("user_nickname", "익명");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String now = sdf.format(new Date());

        communityDao.insertPost(new Post("첫 글", "샘플 내용입니다.", author, now));
        loadPosts();
    }
}
