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
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.ImageAdapter;
import com.example.photoalbum.clase.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

public class MyProfileActivity extends AppCompatActivity implements ImageAdapter.OnImageListener {


    private BottomNavigationView bottomNavigationView;
    private CircleImageView profileImage;
    private String currenUserID;
    private String userName;


    private StorageReference storageReference;
    private FirebaseAuth fAuth;

    private TextView tv_name;

    private RecyclerView mRecylerView;
    private ImageAdapter mAdapter;

    private DatabaseReference mDatabaseRef;
    private List<Post> mPosts;

    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initializare();

        RetrieveImages();
    }

    private void initializare() {

        mPosts = new ArrayList<>();

        mRecylerView = findViewById(R.id.recycler_view_images_myProfile);

        layoutManager = new LinearLayoutManager(this);
        mRecylerView.setLayoutManager(layoutManager);

        fAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();

        bottomNavigationView = findViewById(R.id.nav_menu_id_profile);
        profileImage = findViewById(R.id.my_profile_image);

        currenUserID = FirebaseAuth.getInstance().getUid();

        tv_name = findViewById(R.id.myProfile_name_text);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users Posts").child(currenUserID);

        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull @NotNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.my_profile_nav_menu_editor:

                        Intent iEditor = new Intent(MyProfileActivity.this, EditorActivity.class);
                        startActivity(iEditor);

                        break;
                    case R.id.my_profile_nav_menu_post:

                        Intent iPost = new Intent(MyProfileActivity.this, PostActivity.class);
                        iPost.putExtra("postType", "user");
                        startActivity(iPost);
                }

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                preiaImagineDinGalerie();
            }
        });

        incarcaImagineInCircleImage();

        retrieveUserNameFromFirebase();


    }



    private void retrieveUserNameFromFirebase() {



        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currenUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                userName = snapshot.child("name").getValue().toString();
                tv_name.setText(userName);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void preiaImagineDinGalerie() {

        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
       // openGalleryIntent.putExtra("groupName", currentGroupName);
        startActivityForResult(openGalleryIntent,1000);

    }

    private void incarcaImagineInCircleImage() {

        String imageName = currenUserID + ".png";

        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = storageReference.child("Users").child("Profile").child(imageName);

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get()
                        .load(uri)
                        .into(profileImage);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();

                uploadImageToFirebase(imageUri);


            }
        }

    }

    private void uploadImageToFirebase(Uri imageUri) {

        String imageName = currenUserID + ".png";
        final StorageReference fileRef = storageReference.getRoot().child("Users").child("Profile").child(imageName);

        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Toast.makeText(Profile.this, "Image Uploaded", Toast.LENGTH_SHORT).show();

                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MyProfileActivity.this, "Image failed to upload", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void RetrieveImages() {

        mPosts.clear();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot postSnapshot : snapshot.getChildren()){

                    Post post = postSnapshot.getValue(Post.class);

                    boolean isOk = true;

                    for(Post p: mPosts){
                        if(post.getPostId().equals(p.getPostId())){
                            isOk = false;
                        }
                    }
                    if(isOk){
                        mPosts.add(post);
                    }
                }

                mAdapter = new ImageAdapter(MyProfileActivity.this, mPosts);
                mRecylerView.setAdapter(mAdapter);

            }



            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                Toast.makeText(MyProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onImageClick(int position) {
        mPosts.get(position);

    }
}