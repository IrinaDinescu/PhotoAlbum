package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class GroupActivity extends AppCompatActivity {

    private String currentGroupName;
    private String currentGroupID;

    private TextView groupNameEditText;
    private CircleImageView groupProfileImage;

    String userId;
    FirebaseAuth fAuth;

    StorageReference storageReference;
    StorageReference groupStorageRef;
    StorageReference postsSorageRef;

    private RecyclerView mRecylerView;
    private ImageAdapter mAdapter;

    private DatabaseReference mDatabaseRef;
    private List<Post> mPosts;

    BottomNavigationView bottomNavigationView ;

    LinearLayoutManager layoutManager;

    private String uStatus;

    private  boolean isDeleted = false;

    private boolean isShouldBeDeleted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

       initializare();

        modificaImagineProfile();

        incarcaImagineInCircleImage();


        RetrieveImages();
    }

    private void modificaImagineProfile() {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userMembershipRef = rootRef.child("Memberships").child(userId).child(currentGroupID);

        userMembershipRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if(snapshot.child("status").exists()){

                    String userStatus = snapshot.child("status").getValue().toString().trim();

                    if(userStatus.equals("admin")){

                        uStatus = userStatus;

                        groupProfileImage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                preiaImagineDinGalerie();
                            }
                        });

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void preiaImagineDinGalerie() {

        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGalleryIntent.putExtra("groupName", currentGroupName);
        startActivityForResult(openGalleryIntent,1000);

    }

    private void incarcaImagineInCircleImage() {

       StorageReference storageReference = FirebaseStorage.getInstance().getReference();
       StorageReference profileRef = storageReference.child("Groups").child("Profile").child(currentGroupID);

       profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
           @Override
           public void onSuccess(Uri uri) {

               Picasso.get()
                       .load(uri)
                       .into(groupProfileImage);

           }
       });
    }

    private void initializare() {

        bottomNavigationView = findViewById(R.id.nav_menu_id);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_menu_settings:

                        myCystomDialog();

                        break;
                    case R.id.nav_menu_members:

                        Intent iMembers = new Intent(GroupActivity.this, MembersActivity.class);
                        iMembers.putExtra("GroupID",currentGroupID);
                        iMembers.putExtra("GroupName",currentGroupName);

                        startActivity(iMembers);

                        break;
                    case R.id.nav_menu_post:
                        Toast.makeText(GroupActivity.this, "Post", Toast.LENGTH_SHORT).show();
                        pornestePostActivity();
                        break;
                    case R.id.nav_menu_add_member:
                        Toast.makeText(GroupActivity.this, "Add member", Toast.LENGTH_SHORT).show();

                        Intent i = new Intent(GroupActivity.this, AddMembersActivity.class);
                        i.putExtra("GroupID",currentGroupID);
                        i.putExtra("GroupName",currentGroupName);
                        i.putExtra("userStatus", uStatus);

                        startActivity(i);

                        break;                }
                return true;
            }
        });


        currentGroupName = getIntent().getExtras().get("groupName").toString();
        currentGroupID = getIntent().getExtras().get("groupID").toString();



        groupNameEditText = (TextView) findViewById(R.id.group_name_text);
        groupProfileImage = (CircleImageView) findViewById(R.id.group_profile_image);


        groupNameEditText.setText(currentGroupName);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        postsSorageRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Posts").child(currentGroupID);

        userId = fAuth.getCurrentUser().getUid();

        mRecylerView = findViewById(R.id.recycler_view_images_group);
        mRecylerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecylerView.setLayoutManager(layoutManager);

        mPosts = new ArrayList<>();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Groups Posts").child(currentGroupID);

        uStatus = "member";


    }

    private void myCystomDialog() {

        final Dialog myDialog = new Dialog(GroupActivity.this);
        myDialog.setContentView(R.layout.dialog_group_settings);

        Window window = myDialog.getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        TextView tv_leaveGroup = (TextView) myDialog.findViewById(R.id.dialog_group_settings_tv_leaveGroup);
        TextView tv_changeGroupName = (TextView) myDialog.findViewById(R.id.dialog_group_settings_tv_changeName);
        TextView tv_deleteGroup = (TextView) myDialog.findViewById(R.id.dialog_group_settings_tv_deleteGroup);


        if(!uStatus.equals("admin")){

            tv_changeGroupName.setVisibility(View.INVISIBLE);
            tv_deleteGroup.setVisibility(View.INVISIBLE);

        }




        tv_leaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Leave Group");
                builder.setMessage("Are you sure you want to leave the group?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                parasesteGrup();
                                finish();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        tv_changeGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                AlertDialog.Builder alert = new AlertDialog.Builder(GroupActivity.this);


                final EditText edittext = new EditText(GroupActivity.this);
                alert.setMessage("Enter a new Group Name");
                alert.setTitle("Change Current Group Name");

                alert.setView(edittext);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        String YouEditTextValue = edittext.getText().toString();

                        DatabaseReference groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID).child("name");

                        groupNameRef.setValue(YouEditTextValue);

                        currentGroupName = YouEditTextValue;

                        groupNameEditText.setText(YouEditTextValue);

                        Toast.makeText(GroupActivity.this, YouEditTextValue, Toast.LENGTH_SHORT).show();


                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
            }
        });

        tv_deleteGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setCancelable(true);
                builder.setTitle("Delete Group");
                builder.setMessage("Are you sure you want to delete " + currentGroupName + " group?");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                stergeGrup();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        myDialog.show();
    }

    private void parasesteGrup() {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference membershipsRef = rootRef.child("Memberships");


        membershipsRef.child(userId).child(currentGroupID).removeValue();

        isShouldBeDeleted = true;

        membershipsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                    for(DataSnapshot ds : snapshot.getChildren()){

                        if(ds.child(currentGroupID).exists()){

                            isShouldBeDeleted = false;

                        }

                    }


                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

    }

    private void stergeGrup() {

        //sterge membershipurile pt fiecare utilizator
        DatabaseReference membershipsRef = FirebaseDatabase.getInstance().getReference().child("Memberships");

        membershipsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot ds : snapshot.getChildren()){

                    Log.v("DeleteGroupTest", ds.getKey());

                    if(ds.child(currentGroupID).exists()){

                        ds.child(currentGroupID).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

// Sterge Postarile din folderul aferent grupului
        StorageReference currentGroupStoragePostsRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Posts").child(currentGroupID);

        if(currentGroupStoragePostsRef != null){

            DatabaseReference groupPostsRef = FirebaseDatabase.getInstance().getReference().child("Groups Posts").child(currentGroupID);

            if(groupPostsRef != null){


                groupPostsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()){

                            if(ds.child("pictureName").exists()){

                                String pictureName = ds.child("pictureName").getValue().toString();
                                StorageReference storagePostRef = currentGroupStoragePostsRef.child(pictureName);

                                storagePostRef.delete();

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

            }

            groupPostsRef.removeValue();


            // currentGroupStoragePostsRef.delete();
        }


        // Sterge poza de profil a grupului
        StorageReference currentGroupStorageProfilePicRef = FirebaseStorage.getInstance().getReference().child("Groups").child("Profile").child(currentGroupID);

        if(currentGroupStorageProfilePicRef != null){

            currentGroupStorageProfilePicRef.delete();
        }

        isDeleted = true;




        finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

       if(isShouldBeDeleted){

           stergeGrup();
       }

       if(isDeleted){

           // Sterge referinta din baza de date pentru grupul curent
           DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID);

           if(databaseReference != null){

               databaseReference.removeValue();
           }
       }
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

                uploadImageToFirebase(imageUri);


            }
        }

    }

    private void uploadImageToFirebase(Uri imageUri) {
        final StorageReference fileRef = storageReference.getRoot().child("Groups").child("Profile").child(currentGroupID);

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