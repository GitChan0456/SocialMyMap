package com.example.socialmymap;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private final List<Post> postList;
    private final Fragment fragment;

    public PostAdapter(List<Post> postList, Fragment fragment) {
        this.postList = postList;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvTitle.setText(post.title);
        holder.tvContent.setText(post.content);
        holder.tvAuthor.setText(post.author);
        holder.tvViews.setText(String.valueOf(post.views));
        holder.tvComments.setText(String.valueOf(post.commentCount));

        // 지역 정보 표시 (locality만 추출)
        if (post.region != null && !post.region.isEmpty() && !post.region.equals("미분류")) {
            String simpleRegion = extractLocalityFromRegion(post.region);
            holder.tvRegion.setText(simpleRegion);
            holder.tvRegion.setVisibility(View.VISIBLE);
        } else {
            holder.tvRegion.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), PostDetailActivity.class);
            intent.putExtra("post_id", post.id);
            intent.putExtra("position", position);
            holder.itemView.getContext().startActivity(intent);
        });
    }

    /**
     * 전체 주소에서 locality(시) 부분만 추출
     * 예: "충청북도 청주시 서원구" → "청주시"
     */
    private String extractLocalityFromRegion(String fullRegion) {
        if (fullRegion == null || fullRegion.isEmpty()) {
            return fullRegion;
        }
        
        // 공백으로 분리
        String[] parts = fullRegion.split(" ");
        
        // "시"로 끝나는 부분 찾기
        for (String part : parts) {
            if (part.endsWith("시")) {
                return part;
            }
        }
        
        // "시"가 없으면 마지막 부분 반환 (예: "서울특별시" → "서울특별시")
        return parts.length > 0 ? parts[parts.length - 1] : fullRegion;
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvViews, tvComments, tvRegion;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            tvViews = itemView.findViewById(R.id.tv_post_views);
            tvComments = itemView.findViewById(R.id.tv_post_comments);
            tvRegion = itemView.findViewById(R.id.tv_post_region);
        }
    }
}
