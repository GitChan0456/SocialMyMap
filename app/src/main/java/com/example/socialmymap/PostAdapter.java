package com.example.socialmymap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;
    private Fragment fragment;

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
        holder.tvComments.setText(String.valueOf(post.comments.size()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(fragment.getContext(), PostDetailActivity.class);
            intent.putExtra("post", post);
            intent.putExtra("position", position); // 게시글의 위치(position) 전달
            fragment.startActivityForResult(intent, BoardFragment.REQUEST_CODE_POST_DETAIL);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvAuthor, tvViews, tvComments;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_post_title);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            tvViews = itemView.findViewById(R.id.tv_post_views);
            tvComments = itemView.findViewById(R.id.tv_post_comments);
        }
    }
}

class Post implements Serializable {
    String title;
    String content;
    String author;
    String timestamp;
    int views;
    List<Comment> comments;

    public Post(String title, String content, String author, String timestamp, int views, List<Comment> comments) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.timestamp = timestamp;
        this.views = views;
        this.comments = comments;
    }
}