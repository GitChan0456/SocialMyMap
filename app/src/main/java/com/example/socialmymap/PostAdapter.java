package com.example.socialmymap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.Serializable;
import java.util.List;
import android.content.Context;
import android.content.Intent;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private List<Post> postList;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
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
        holder.tvComments.setText(String.valueOf(post.comments.size())); // 실제 댓글 개수로 설정

        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", post.title);
            intent.putExtra("content", post.content);
            intent.putExtra("author", post.author);
            intent.putExtra("comments", (Serializable) post.comments); // 댓글 리스트 전달
            context.startActivity(intent);
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

// Post 데이터 클래스 수정
class Post implements Serializable {
    String title;
    String content;
    String author;
    int views;
    List<Comment> comments; // 댓글 리스트 추가

    public Post(String title, String content, String author, int views, List<Comment> comments) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.views = views;
        this.comments = comments;
    }
}