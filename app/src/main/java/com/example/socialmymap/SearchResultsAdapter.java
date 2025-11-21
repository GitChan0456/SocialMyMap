package com.example.socialmymap;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Address address);
    }

    private List<Address> addresses;
    private OnItemClickListener listener;

    public SearchResultsAdapter(List<Address> addresses, OnItemClickListener listener) {
        this.addresses = addresses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.list_item_search_result, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address address = addresses.get(position);
        holder.tvPlaceName.setText(address.getFeatureName() != null ? address.getFeatureName() : "이름 없음");
        holder.tvAddress.setText(address.getAddressLine(0));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(address));
    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvPlaceName;
        public TextView tvAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPlaceName = itemView.findViewById(R.id.tv_place_name);
            tvAddress = itemView.findViewById(R.id.tv_address);
        }
    }
}