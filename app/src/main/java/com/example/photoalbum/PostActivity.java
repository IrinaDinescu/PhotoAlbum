package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.Group;
import com.example.photoalbum.clase.Post;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Uri imageUri;
    private String imageUrl;

    private ImageView close;
    private ImageView imageAdded;
    private TextView post;

    private FirebaseAuth fAuth;
    private DatabaseReference RootRef;
    private StorageReference storageRef;
    private DatabaseReference PostsRef;
    private StorageReference PostStorageRef;

    private String publisherName;
    private String postType;
    private String publisherID;

    private String userId;

    private static final int GALLERY_PICK = 1;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        InitializeFields();

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                trateazaClose();
            }
        });

        imageAdded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                trateazaClickImagineAdd();
            }
        });

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });

    }

    private void upload() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");

        if(imageUri != null) {






            String pictureName = String.valueOf(System.currentTimeMillis()) + "." + getFileExtension(imageUri);
            StorageReference newStorageReference = PostStorageRef.child(pictureName);

            newStorageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Date currentTime = Calendar.getInstance().getTime();

                            DatabaseReference newDataBaseReference = PostsRef.push();

                            String postID = newDataBaseReference.getKey();

                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String imageDownloadURL = uri.toString();

                                    Post post = new Post(pictureName,imageDownloadURL, publisherID, postID, userId, currentTime.toString(), postType);

                                    PostsRef.child(postID).setValue(post);
                                }
                            });


                            pd.dismiss();
                            Toast.makeText(PostActivity.this,"Upload succesfull!", Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {

                    Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {

                    pd.show();

                }
            });




        }else{

            Toast.makeText(PostActivity.this,"Please upload a photo from gallery first!", Toast.LENGTH_SHORT).show();

        }

    }

    private String getFileExtension(Uri uri){

        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void trateazaClickImagineAdd(){

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            imageUri = selectedImage;
            imageAdded.setImageURI(selectedImage);

        }
    }

    private void trateazaClose(){

        if(postType.equals("group")){

            finish();
        }else{

            finish();
        }

    }

    private void InitializeFields() {

        close = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        post = findViewById(R.id.post);



        postType = getIntent().getExtras().get("postType").toString();
        Log.v("postType", postType);


        fAuth = FirebaseAuth.getInstance();
        userId = fAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();


        if(postType.equals("group")){
            publisherName = getIntent().getExtras().get("Name").toString();
            publisherID = getIntent().getExtras().get("ID").toString();

            PostsRef = RootRef.child("Groups Posts").child(publisherID);
            PostStorageRef = storageRef.child("Groups").child("Posts").child(publisherID);
        }else{
            PostsRef = RootRef.child("Users Posts").child(userId);
            PostStorageRef = storageRef.child("Users").child("Posts").child(userId);
        }



    }


}