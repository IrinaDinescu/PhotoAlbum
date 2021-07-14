package com.example.photoalbum.clase;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.photoalbum.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Post> mPosts;

    private View v;

    private OnImageListener mOnImageListener;

    public ImageAdapter(Context context, List<Post> posts){
        mContext = context;
        mPosts = posts;
    }



    @NonNull
    @NotNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {

        v = LayoutInflater.from(mContext).inflate(R.layout.image_item, parent, false);



        ImageViewHolder viewHolder = new ImageViewHolder(v);

        return viewHolder;
    }

    private void MyCustomAlertDialog(ImageAdapter.ImageViewHolder holder, int position) {

        final Dialog MyDialog = new Dialog(mContext);
       // MyDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        MyDialog.setContentView(R.layout.customdialog);


        Window window = MyDialog.getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }




        ImageView imageView = (ImageView) MyDialog.findViewById(R.id.customdialog_img_view);
        ImageView close = (ImageView) MyDialog.findViewById(R.id.customdialog_close);
        Button btn_download = (Button) MyDialog.findViewById(R.id.customdialog_btn_download);
        Button btn_delete = (Button) MyDialog.findViewById(R.id.customdialog_btn_delete);

        Post currentPost = mPosts.get(position);

        Log.v("AlertDialog", currentPost.toString());

        Picasso.get()
                .load(currentPost.getImageUrl())
                .fit()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into((ImageView) imageView);

      //  imageView.setImageResource(R.drawable.ic_baseline_image_24);

        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                AlertDialog dialog = new SpotsDialog(mContext);
                dialog.show();
                dialog.setMessage("Downloading...");

                String fileName = currentPost.getPictureName() + ".jpg";
                Picasso.get()
                        .load(currentPost.getImageUrl())
                        .into(new SaveImageHelper(mContext,
                                        dialog,
                                        mContext.getContentResolver(),
                                        fileName,
                                        "Image description"));

                dialog.dismiss();

            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String postId = currentPost.getPostId();

                String postType;
                if(currentPost.getPostType() != null){
                    postType = currentPost.getPostType().toString().trim();
                    if(postType.equals("user")){
                        String uid = FirebaseAuth.getInstance().getUid().toString();
                        StorageReference postRef = FirebaseStorage.getInstance().getReference().child("Users").child("Posts").child(uid).child(currentPost.getPictureName());

                        postRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("Users Posts").child(uid).child(currentPost.getPostId());

                                dataRef.removeValue();

                                Toast.makeText(mContext, "Image deleted!", Toast.LENGTH_SHORT).show();

                                MyDialog.dismiss();

                            }
                        });
                    }

                    if(postType.equals("group")){

                        Log.v("PostType", postType);

                        String publisherId = currentPost.getPublisherId();
                        StorageReference postRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Posts").child(publisherId).child(currentPost.getPictureName());

                        postRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("Groups Posts").child(publisherId).child(currentPost.getPostId());

                                dataRef.removeValue();

                                Toast.makeText(mContext, "Image deleted!", Toast.LENGTH_SHORT).show();

                                MyDialog.dismiss();



                            }
                        });
                    }


                }

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.dismiss();
            }
        });




        MyDialog.show();


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

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(mContext, "Alert Dialog", Toast.LENGTH_SHORT).show();

                MyCustomAlertDialog(holder, position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public ImageView imageView;
        OnImageListener onImageListener;

        CardView parentLayout;

        public ImageViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_upload);
            this.onImageListener = onImageListener;

            parentLayout = itemView.findViewById(R.id.parentLayoutTest2);

          //  itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onImageListener.onImageClick(getAdapterPosition());
        }
    }

    public interface OnImageListener{
        void onImageClick(int position);
    }

    private static Target getTarget(final String url){

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + url);
                        try{

                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };

        return  target;
    }

}
