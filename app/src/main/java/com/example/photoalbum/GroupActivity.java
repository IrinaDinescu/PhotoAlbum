package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.ImageAdapter;
import com.example.photoalbum.clase.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupActivity extends AppCompatActivity {

    private String currentGroupName;
    private String currentGroupID;

    private TextView groupNameEditText;
    private CircleImageView groupProfileImage;

    private Button  btnIncarcaImagine;

    String userId;
    FirebaseAuth fAuth;

    StorageReference storageReference;
    StorageReference groupStorageRef;
    StorageReference postsSorageRef;

    private RecyclerView mRecylerView;
    private ImageAdapter mAdapter;

    private DatabaseReference mDatabaseRef;
    private List<Post> mPosts;

    LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();



        groupNameEditText = (TextView) findViewById(R.id.group_name_text);
        groupProfileImage = (CircleImageView) findViewById(R.id.group_profile_image);


        groupNameEditText.setText(currentGroupName);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        postsSorageRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Posts").child(currentGroupID);

        userId = fAuth.getCurrentUser().getUid();

        btnIncarcaImagine = (Button) findViewById(R.id.add_image);


        btnIncarcaImagine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pornestePostActivity();
            }
        });


//        StorageReference profileRef = storageReference.getRoot().child("Groups").child(currentGroupName).child("Profile");
//        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Picasso.get().load(uri).into(groupProfileImage);
//            }
//        });

        groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open gallery

                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                openGalleryIntent.putExtra("groupName", currentGroupName);
                startActivityForResult(openGalleryIntent,1000);


            }
        });

        mRecylerView = findViewById(R.id.recycler_view_images_group);
        mRecylerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecylerView.setLayoutManager(layoutManager);

        mPosts = new ArrayList<>();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Groups Posts").child(currentGroupID);

//        RetrievePosts();
//
//        mAdapter = new ImageAdapter(GroupActivity.this, mPosts);
//
//        mRecylerView.setAdapter(mAdapter);

        RetrieveImages();
    }

    private void RetrievePosts() {

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot postSnapshot : snapshot.getChildren()){

                    Post post = postSnapshot.getValue(Post.class);
                    mPosts.add(post);

                    Log.v("GroupActivity", "test " + post.getImageUrl());
                }


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void RetrieveImages() {

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot postSnapshot : snapshot.getChildren()){

                    Post post = postSnapshot.getValue(Post.class);
                    mPosts.add(post);
                }

                mAdapter = new ImageAdapter(GroupActivity.this, mPosts);

                mRecylerView.setAdapter(mAdapter);


            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                Toast.makeText(GroupActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();

                // profileImage.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);


            }
        }

    }

    private void uploadImageToFirebase(Uri imageUri) {
        final StorageReference fileRef = storageReference.getRoot().child("Groups").child(currentGroupName).child("Profile");

        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Toast.makeText(Profile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(groupProfileImage);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupActivity.this, "Image ailed to upload", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void pornestePostActivity(){

        Intent postActivityIntent = new Intent(GroupActivity.this, PostActivity.class);

        postActivityIntent.putExtra("Name", currentGroupName);
        postActivityIntent.putExtra("ID", currentGroupID );
        postActivityIntent.putExtra("postType", "group");

        startActivity(postActivityIntent);

    }
}