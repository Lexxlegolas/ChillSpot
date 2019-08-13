package com.example.chillspot.viewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chillspot.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsViewHolder extends RecyclerView.ViewHolder
{
    public CircleImageView postProfileImage;
    public TextView postUserName, postDate, postTime, postDescription;
    public ImageView postImage;

    public PostsViewHolder(@NonNull View itemView)
    {
        super(itemView);

        postProfileImage = itemView.findViewById(R.id.post_profile_image);

        postUserName = itemView.findViewById(R.id.post_user_name);
        postDate = itemView.findViewById(R.id.post_date);
        postTime = itemView.findViewById(R.id.post_time);
        postDescription = itemView.findViewById(R.id.post_desc);

        postImage = itemView.findViewById(R.id.post_image);
    }
}
