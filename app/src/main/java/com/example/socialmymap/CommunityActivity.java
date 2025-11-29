package com.example.socialmymap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CommunityActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_POST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        Toolbar toolbar = findViewById(R.id.toolbar_community);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton fabWritePost = findViewById(R.id.fab_write_post);

        // ViewPager2 어댑터 설정
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // ViewPager2 페이지 변경 리스너
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0: // 게시판 탭
                        bottomNav.getMenu().findItem(R.id.nav_board).setChecked(true);
                        fabWritePost.show(); // FAB 보이기
                        break;
                    case 1: // 채팅 탭
                        bottomNav.getMenu().findItem(R.id.nav_chat).setChecked(true);
                        fabWritePost.hide(); // FAB 숨기기
                        break;
                }
            }
        });

        // BottomNavigationView 아이템 선택 리스너
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_board) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.nav_chat) {
                viewPager.setCurrentItem(1);
                return true;
            }
            return false;
        });

        fabWritePost.setOnClickListener(v -> {
            Intent intent = new Intent(this, WritePostActivity.class);
            // BoardFragment의 onActivityResult를 호출하기 위해 Fragment를 통해 startActivityForResult
            // 호출
            getSupportFragmentManager().getFragments().get(0).startActivityForResult(intent, REQUEST_CODE_WRITE_POST);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
