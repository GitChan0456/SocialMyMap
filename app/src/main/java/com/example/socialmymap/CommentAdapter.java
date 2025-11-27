package com.example.socialmymap;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<Comment> commentList;
    private FriendsDao friendsDao;
    private OnFriendAddedListener friendAddedListener;

    public interface OnFriendAddedListener {
        void onFriendAdded();
    }

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    public void setFriendsDao(FriendsDao friendsDao) {
        this.friendsDao = friendsDao;
    }

    public void setOnFriendAddedListener(OnFriendAddedListener listener) {
        this.friendAddedListener = listener;
    }

    public void addComment(Comment comment) {
        commentList.add(comment);
        notifyItemInserted(commentList.size() - 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.tvAuthor.setText(comment.author);
        holder.tvContent.setText(comment.content);
        holder.tvTime.setText(comment.timestamp);

        // 친구 추가 버튼 설정
        Context context = holder.itemView.getContext();
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("user_id", "");

        // 자기 자신인 경우 버튼 숨기기
        if (currentUserId.equals(comment.authorId)) {
            holder.btnAddFriend.setVisibility(View.GONE);
        } else if (friendsDao != null && friendsDao.isFriend(currentUserId, comment.authorId)) {
            // 이미 친구인 경우
            holder.btnAddFriend.setText("친구");
            holder.btnAddFriend.setEnabled(false);
            holder.btnAddFriend.setAlpha(0.5f);
            holder.btnAddFriend.setVisibility(View.VISIBLE);
        } else {
            // 친구 추가 가능
            holder.btnAddFriend.setText("친구 추가");
            holder.btnAddFriend.setEnabled(true);
            holder.btnAddFriend.setAlpha(1.0f);
            holder.btnAddFriend.setVisibility(View.VISIBLE);

            holder.btnAddFriend.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("친구 추가")
                        .setMessage(comment.author + "님을 친구로 추가하시겠습니까?")
                        .setPositiveButton("추가", (dialog, which) -> {
                            if (friendsDao != null) {
                                boolean success = friendsDao.addFriend(currentUserId, comment.authorId, comment.author);
                                if (success) {
                                    Toast.makeText(context, "친구가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                    // 전체 댓글 목록을 다시 바인딩하여 같은 사용자의 모든 댓글 버튼 업데이트
                                    notifyDataSetChanged();
                                    // 부모 액티비티(게시글)의 친구 버튼도 업데이트
                                    if (friendAddedListener != null) {
                                        friendAddedListener.onFriendAdded();
                                    }
                                } else {
                                    Toast.makeText(context, "친구 추가에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvContent, tvTime;
        Button btnAddFriend;

        public ViewHolder(View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tv_comment_author);
            tvContent = itemView.findViewById(R.id.tv_comment_content);
            tvTime = itemView.findViewById(R.id.tv_comment_time);
            btnAddFriend = itemView.findViewById(R.id.btn_add_friend_comment);
        }
    }
}
