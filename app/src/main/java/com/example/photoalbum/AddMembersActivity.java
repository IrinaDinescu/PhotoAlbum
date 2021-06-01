package com.example.photoalbum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_add_members);

        initializare();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                adaugaMembriiGrup();
              //  inchideIntent();
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

                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {

                        if(!snapshot.exists()){

                            membershipRef.child("name").setValue(groupName);
                            membershipRef.child("status").setValue("member");

                            Log.v("AddMember", memberID + " membru adaugat");


                        }else{
                            Log.v("AddMember", memberID + " este deja membru in grup");
                        }
                    }

                    @Override
                    public void onCancelled(@androidx.annotation.NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                    }
                };
                membershipRef.addValueEventListener(eventListener);

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

                         if(snapshot.hasChild("profileImageUri")){

                             String userImage = snapshot.child("profileImageUri").getValue().toString();
                             String userName = snapshot.child("name").getValue().toString();

                             holder.userName.setText(userName);
                             if(userImage.length() > 0){

                                 Picasso.get().load(userImage).placeholder(R.drawable.user_profile_image).into(holder.profileImage);
                             }
                         }else{

                             String userName = snapshot.child("name").getValue().toString();

                             holder.userName.setText(userName);

                         }

                         holder.checkBox.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View v) {

                                 if( holder.checkBox.isChecked()){

                                     String user_to_add_ID =  getRef(position).getKey();
                                 //    Toast.makeText(AddMembersActivity.this, "Add " + user_to_add_ID, Toast.LENGTH_SHORT ).show();

                                     membersToAdd.add(user_to_add_ID);

                                 }

                                 if( !holder.checkBox.isChecked()){

                                     String user_to_add_ID =  getRef(position).getKey();
                                  //   Toast.makeText(AddMembersActivity.this, "Remove " + user_to_add_ID, Toast.LENGTH_SHORT ).show();

                                     membersToAdd.remove(user_to_add_ID);

                                 }






                             }
                         });

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

        TextView userName;
        CircleImageView profileImage;
        CheckBox checkBox;

        public FriendsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            userName = itemView.findViewById(R.id.users_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            checkBox = itemView.findViewById(R.id.user_is_checked);



        }
    }
}