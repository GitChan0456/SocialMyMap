package com.example.socialmymap;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WritePostActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private EditText etTitle, etContent;
    private CommunityDao communityDao;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);

        Toolbar toolbar = findViewById(R.id.toolbar_write_post);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTitle = findViewById(R.id.et_write_title);
        etContent = findViewById(R.id.et_write_content);
        communityDao = new CommunityDao(this);
        locationHelper = new LocationHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.write_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_submit) {
            submitPost();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void submitPost() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        // 위치 정보 수집
        Task<Location> locationTask = locationHelper.getCurrentLocation();
        if (locationTask != null) {
            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    // 백그라운드에서 역지오코딩
                    new Thread(() -> {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        String region = locationHelper.getRegionFromLocation(lat, lng);

                        runOnUiThread(() -> {
                            savePostWithLocation(title, content, region, lat, lng);
                        });
                    }).start();
                } else {
                    // 위치를 가져오지 못한 경우 기본값으로 저장
                    savePostWithLocation(title, content, "미분류", 0, 0);
                }
            }).addOnFailureListener(e -> {
                // 위치 가져오기 실패 시 기본값으로 저장
                savePostWithLocation(title, content, "미분류", 0, 0);
            });
        } else {
            // 위치 서비스를 사용할 수 없는 경우 기본값으로 저장
            savePostWithLocation(title, content, "미분류", 0, 0);
        }
    }

    private void savePostWithLocation(String title, String content, String region,
            double latitude, double longitude) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String author = prefs.getString("user_nickname", "익명");
        String authorId = prefs.getString("user_id", "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        Post post = new Post(authorId, title, content, author, timestamp);
        post.region = region;
        post.latitude = latitude;
        post.longitude = longitude;

        communityDao.insertPost(post);

        Toast.makeText(this, "게시글 작성 완료 (" + region + ")", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                submitPost();
            } else {
                // 권한이 거부된 경우 기본값으로 저장
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();
                savePostWithLocation(title, content, "미분류", 0, 0);
            }
        }
    }
}
