package com.example.photoalbum.clase;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.example.photoalbum.R;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

    public ImageAdapter(Context context, List<Post> posts){
        mContext = context;
        mPosts = posts;
    }

    @NonNull
    @NotNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);
        ImageViewHolder viewHolder = new ImageViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ImageAdapter.ImageViewHolder holder, int position) {

        Post currentPost = mPosts.get(position);
        Log.v("ImageAdapter", "test " + currentPost.getImageUrl());

     //   Picasso.get().load(currentPost.getImageUrl()).into((ImageView) holder.itemView);
        Picasso.get()
                .load(currentPost.getImageUrl())
                .fit()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into((ImageView) holder.imageView);



    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{

        public ImageView imageView;

        public ImageViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_upload);


        }
    }

}
