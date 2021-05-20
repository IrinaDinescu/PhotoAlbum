package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class GroupActivity extends AppCompatActivity {

    private String currentGroupName;
    private String currentGroupID;

    private TextView groupNameEditText;
    private ImageView groupProfileImage;

    private Button  btnIncarcaImagine;

    String userId;
    FirebaseAuth fAuth;

    StorageReference storageReference;
    StorageReference groupStorageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();



        groupNameEditText = (TextView) findViewById(R.id.group_name_text);
        groupProfileImage = (ImageView) findViewById(R.id.group_profile_image);


        groupNameEditText.setText(currentGroupName);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = fAuth.getCurrentUser().getUid();

        btnIncarcaImagine = (Button) findViewById(R.id.add_image);


        btnIncarcaImagine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pornestePostActivity();
            }
        });


        StorageReference profileRef = storageReference.getRoot().child("Groups").child(currentGroupName).child("Profile");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(groupProfileImage);
            }
        });

        groupProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // open gallery

                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                openGalleryIntent.putExtra("groupName", currentGroupName);
                startActivityForResult(openGalleryIntent,1000);


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