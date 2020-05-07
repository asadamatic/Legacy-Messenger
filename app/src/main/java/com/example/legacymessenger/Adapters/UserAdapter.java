package com.example.legacymessenger.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.legacymessenger.Data.User;
import com.example.legacymessenger.R;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    //Users List
    List<User> usersList;

    //Context
    Context context;

    public UserAdapter(Context context, List<User> usersList){

        this.usersList = usersList;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_recyclerview, parent, false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.userName.setText(usersList.get(position).getDisplayName());
        Glide.with(context).load(Uri.parse(usersList.get(position).getProfileImage())).into(holder.userImage);
    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }


    public void filteredList(List<User> filteredList) {

        usersList= filteredList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.userName);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }
}
