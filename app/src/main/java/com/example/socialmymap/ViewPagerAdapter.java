package com.example.socialmymap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BoardFragment();
            case 1:
                return new ChatFragment();
            case 2:
                return new FriendsFragment();
            default:
                return new BoardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3; // 게시판, 채팅, 친구 3개의 탭
    }
}
