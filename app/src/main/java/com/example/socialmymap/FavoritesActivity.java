package com.example.socialmymap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private FavoritesDao favoritesDao;
    private FavoritesAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setTitle("즐겨찾기");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvEmpty = findViewById(R.id.tv_empty);
        RecyclerView recyclerView = findViewById(R.id.rv_favorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoritesAdapter(item -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("placeName", item.placeName);
            resultIntent.putExtra("address", item.address);
            if (item.lat != null && item.lng != null) {
                resultIntent.putExtra("lat", item.lat);
                resultIntent.putExtra("lng", item.lng);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        }, item -> {
            favoritesDao.deleteFavorite(item.id);
            loadData();
            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter);

        favoritesDao = new FavoritesDao(this);
        favoritesDao.open();

        loadData();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("로그인이 필요합니다.");
            adapter.submit(new ArrayList<>());
            return;
        }

        List<FavoritesDao.FavoriteItem> items = favoritesDao.getFavoritesByUser(userId);
        adapter.submit(items);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        favoritesDao.close();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    static class FavoritesAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {
        interface OnClick {
            void click(FavoritesDao.FavoriteItem item);
        }

        interface OnLongClick {
            void longClick(FavoritesDao.FavoriteItem item);
        }

        private final List<FavoritesDao.FavoriteItem> data = new ArrayList<>();
        private final OnClick onClick;
        private final OnLongClick onLongClick;

        FavoritesAdapter(OnClick onClick, OnLongClick onLongClick) {
            this.onClick = onClick;
            this.onLongClick = onLongClick;
        }

        void submit(List<FavoritesDao.FavoriteItem> items) {
            data.clear();
            data.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_favorite, parent, false);
            return new FavoritesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
            FavoritesDao.FavoriteItem item = data.get(position);
            holder.bind(item, onClick, onLongClick);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    static class FavoritesViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvPlaceName;
        private final TextView tvPlaceAddress;

        FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvPlaceAddress = itemView.findViewById(R.id.tv_place_address);
        }

        void bind(FavoritesDao.FavoriteItem item,
                  FavoritesAdapter.OnClick onClick,
                  FavoritesAdapter.OnLongClick onLongClick) {
            tvPlaceName.setText(item.placeName);
            tvPlaceAddress.setText(item.address != null ? item.address : "");

            itemView.setOnClickListener(v -> onClick.click(item));
            itemView.setOnLongClickListener(v -> {
                onLongClick.longClick(item);
                return true;
            });
        }
    }
}
