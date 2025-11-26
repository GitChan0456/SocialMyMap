package com.example.socialmymap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WritePostActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private CommunityDao communityDao;

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

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String author = prefs.getString("user_nickname", "익명");
        String authorId = prefs.getString("user_id", "");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String timestamp = sdf.format(new Date());

        Post post = new Post(authorId, title, content, author, timestamp);
        communityDao.insertPost(post);

        setResult(RESULT_OK);
        finish();
    }
}
