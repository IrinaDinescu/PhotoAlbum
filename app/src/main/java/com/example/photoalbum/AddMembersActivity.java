package com.example.photoalbum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.okhttp.internal.DiskLruCache;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.photoalbum.R.layout.activity_add_members;

public class AddMembersActivity extends AppCompatActivity {

    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference FriendsRef, UsersRef;
    private RecyclerView myFriendsList;

    private String currentGroupID;
    private String currentUserID;

    private DatabaseReference RootRef;

    private List<String> membersToAdd;

    private FloatingActionButton addButton;

    private String groupName;

    private String userStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_add_members);

        initializare();



        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(userStatus.equals("admin")){
                    adaugaMembriiGrup();

                }else{
                    Toast.makeText(AddMembersActivity.this, "Trebuie sa fiti admin pentru a putea adauga membrii!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void inchideIntent() {

        finish();
    }

    private void adaugaMembriiGrup() {

        if(membersToAdd.size() > 0){

            for(String memberID : membersToAdd){

                DatabaseReference membershipRef = RootRef.child("Memberships").child(memberID).child(currentGroupID);

                membershipRef.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {

                        if(!snapshot.exists()){

                            membershipRef.child("name").setValue(groupName);
                            membershipRef.child("status").setValue("member");

                        }else{


                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                    }
                });

//                membershipRef.addValueEventListener(eventListener);
//                membershipRef.removeEventListener(eventListener);

                Toast.makeText(AddMembersActivity.this, "Membrii adaugati cu success!", Toast.LENGTH_SHORT).show();
             //   membershipRef.removeEventListener(eventListener);


            }

        }else{
            Toast.makeText(AddMembersActivity.this, "Nu ati selectat niciun prieten!", Toast.LENGTH_SHORT).show();
        }




    }

    public void initializare(){

        myFriendsList = (RecyclerView) findViewById(R.id.add_members_list);
        myFriendsList.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        currentGroupID = getIntent().getExtras().get("GroupID").toString();
        userStatus = getIntent().getExtras().get("userStatus").toString();

        membersToAdd = new ArrayList();

        addButton = findViewById(R.id.floatingActionButton_add);

        RootRef = FirebaseDatabase.getInstance().getReference();

        groupName = getIntent().getExtras().get("GroupName").toString();


    }


     @Override
    public void onStart() {

         super.onStart();

         FirebaseRecyclerOptions options =
                 new FirebaseRecyclerOptions.Builder<User>()
                         .setQuery(FriendsRef, User.class)
                         .build();

         FirebaseRecyclerAdapter<User, FriendsViewHolder> adapter
                 = new FirebaseRecyclerAdapter<User, FriendsViewHolder>(options) {
             @Override
             protected void onBindViewHolder(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull AddMembersActivity.FriendsViewHolder holder, int position, @androidx.annotation.NonNull @org.jetbrains.annotations.NotNull User model) {

                 String userIDs = getRef(position).getKey();
                 holder.checkBox.setVisibility(View.VISIBLE);

                 UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                         if(snapshot.exists() && snapshot != null) {

                             Object potentialMemberIDsnapshot = snapshot.child("uid").getValue();
                             if(potentialMemberIDsnapshot != null){

                                 String potentialMemberID = potentialMemberIDsnapshot.toString();
                                 String imageName = potentialMemberID + ".png";


                                 DatabaseReference groupMembershipRefForUser = FirebaseDatabase.getInstance().getReference().child("Memberships").child(potentialMemberID);

                                 Object userNameSnap = snapshot.child("name").getValue();

                                 if(userNameSnap != null){

                                     String userName = userNameSnap.toString();

                                     if (groupMembershipRefForUser != null) {

                                         DatabaseReference thisGroupMembershipRef = groupMembershipRefForUser.child(currentGroupID);

                                         thisGroupMembershipRef.addValueEventListener(new ValueEventListener() {
                                             @Override
                                             public void onDataChange(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {

                                                 if (!snapshot.exists()) {

                                                     Log.v("AddMemberProfile", imageName);

                                                     StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                                     StorageReference profileRef = storageReference.child("Users").child("Profile").child(imageName);

                                                     profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                         @Override
                                                         public void onSuccess(Uri uri) {

                                                             Picasso.get()
                                                                     .load(uri)
                                                                     .into(holder.profileImage);

                                                         }
                                                     });

                                                     holder.userName.setText(userName);


                                                     holder.checkBox.setOnClickListener(new View.OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {

                                                             if (holder.checkBox.isChecked()) {

                                                                 String user_to_add_ID = getRef(position).getKey();
                                                                 membersToAdd.add(user_to_add_ID);

                                                             }

                                                             if (!holder.checkBox.isChecked()) {

                                                                 String user_to_add_ID = getRef(position).getKey();
                                                                 membersToAdd.remove(user_to_add_ID);

                                                             }
                                                         }
                                                     });

                                                 } else {

                                                     holder.Layout_hide();
                                                 }
                                             }

                                             @Override
                                             public void onCancelled(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                             }
                                         });


                                     }

                                 }
                             }


                         }
                     }

                     @Override
                     public void onCancelled(@NonNull @NotNull DatabaseError error) {

                         Toast.makeText(AddMembersActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();


                     }
                 });

             }

             @androidx.annotation.NonNull
             @org.jetbrains.annotations.NotNull
             @Override
             public FriendsViewHolder onCreateViewHolder(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull ViewGroup viewGroup, int viewType) {

                 View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup,false);
                 FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                 return viewHolder;


             }
         };

         myFriendsList.setAdapter(adapter);
         adapter.startListening();



     }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{


        final LinearLayout layout;
        final LinearLayout.LayoutParams params;

        TextView userName;
        CircleImageView profileImage;
        CheckBox checkBox;

        public FriendsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            layout =(LinearLayout)itemView.findViewById(R.id.users_linear_layout);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            userName = itemView.findViewById(R.id.users_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            checkBox = itemView.findViewById(R.id.user_is_checked);



        }

        public void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            layout.setLayoutParams(params);   //Or This one.

        }
    }


}