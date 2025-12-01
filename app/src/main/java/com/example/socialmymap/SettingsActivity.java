package com.example.socialmymap;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;

public class SettingsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 300;

    private Switch switchDarkMode;
    private SharedPreferences preferences;
    private SharedPreferences userPrefs;
    private LocationHelper locationHelper;
    private TextView tvHomeRegion;
    private LinearLayout layoutHomeRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("설정");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        preferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        locationHelper = new LocationHelper(this);

        switchDarkMode = findViewById(R.id.switch_dark_mode);
        tvHomeRegion = findViewById(R.id.tv_home_region);
        layoutHomeRegion = findViewById(R.id.layout_home_region);

        // 저장된 우리 동네 표시
        String homeRegion = userPrefs.getString("home_region", null);
        if (homeRegion != null && !homeRegion.isEmpty()) {
            tvHomeRegion.setText(homeRegion);
        } else {
            tvHomeRegion.setText("설정되지 않음");
        }

        // 우리 동네 설정 클릭 리스너
        layoutHomeRegion.setOnClickListener(v -> showHomeRegionDialog());

        // 현재 야간 모드 설정 불러오기
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        // 야간 모드 스위치 리스너
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            recreate();
        });
    }

    private void showHomeRegionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("우리 동네 설정")
                .setMessage("현재 위치를 우리 동네로 설정하시겠습니까?")
                .setPositiveButton("설정", (dialog, which) -> {
                    setHomeRegionFromCurrentLocation();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void setHomeRegionFromCurrentLocation() {
        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST);
            return;
        }

        Toast.makeText(this, "위치 가져오는 중...", Toast.LENGTH_SHORT).show();

        Task<Location> locationTask = locationHelper.getCurrentLocation();
        if (locationTask != null) {
            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    new Thread(() -> {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        String region = locationHelper.getRegionFromLocation(lat, lng);

                        runOnUiThread(() -> {
                            if (region != null && !region.equals("미분류")) {
                                // 우리 동네 저장
                                SharedPreferences.Editor editor = userPrefs.edit();
                                editor.putString("home_region", region);
                                editor.putString("home_lat", String.valueOf(lat));
                                editor.putString("home_lng", String.valueOf(lng));
                                editor.apply();

                                tvHomeRegion.setText(region);
                                Toast.makeText(this, "우리 동네가 '" + region + "'로 설정되었습니다",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "주소를 가져올 수 없습니다",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                } else {
                    Toast.makeText(this, "현재 위치를 가져올 수 없습니다", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "위치 가져오기 실패", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "위치 서비스를 사용할 수 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setHomeRegionFromCurrentLocation();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다", Toast.LENGTH_SHORT).show();
            }
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
}