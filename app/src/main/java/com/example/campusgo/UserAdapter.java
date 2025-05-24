package com.example.campusgo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<Users> userList;

    public UserAdapter(List<Users> userList) {
        this.userList = (userList != null) ? userList : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String email = userList.get(position).getEmail();
        if (email == null || email.trim().isEmpty()) {
            holder.emailTextView.setText("");
        } else {
            holder.emailTextView.setText(email);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Method to update user list dynamically
    public void updateUserList(List<Users> newUsers) {
        userList.clear();
        userList.addAll(newUsers);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView emailTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            emailTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
