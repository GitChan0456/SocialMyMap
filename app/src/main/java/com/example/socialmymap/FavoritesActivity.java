package com.example.socialmymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class FavoritesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setTitle("즐겨찾기");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout llSeowonUniv = findViewById(R.id.ll_seowon_univ);
        LinearLayout llMyHome = findViewById(R.id.ll_my_home);

        llSeowonUniv.setOnClickListener(v -> returnToMainActivity("서원대학교", "충북 청주시 서원구 무심서로 377-3"));
        llMyHome.setOnClickListener(v -> returnToMainActivity("우리집", "충북 청주시 서원구 1순환로 123"));
    }

    private void returnToMainActivity(String placeName, String address) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("placeName", placeName);
        resultIntent.putExtra("address", address);
        setResult(RESULT_OK, resultIntent);
        finish();
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