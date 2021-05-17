package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID;
    private String senderUserID;
    private String Current_State;

    private CircleImageView userProfileImage;
    private TextView userProfileName;
    private Button AddFriendButton;
    private Button DeclineFriendRequestButton;

    private DatabaseReference UserRef, FriendRequestRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");


        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();



        userProfileImage = (CircleImageView) findViewById((R.id.visit_profile_image));
        userProfileName = (TextView) findViewById(R.id.visit_profile_name);
        AddFriendButton = (Button) findViewById(R.id.add_friend_button);
        DeclineFriendRequestButton = (Button) findViewById(R.id.decline_friend_button);
        Current_State = "new";

        RetrieveUserInfo();



    }

    private void RetrieveUserInfo() {

        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists() && (snapshot.hasChild("profileImageUri"))){


                    String userName = snapshot.child("name").getValue().toString();

                    String userImage = snapshot.child("profileImageUri").getValue().toString();



                    if(userImage.length() > 0){

                        Log.v("user_image", "test " +  userImage.toString());
                        Picasso.get().load(userImage).into(userProfileImage);

                    }
                    userProfileName.setText(userName);

                    ManageFriendRequest();
                }else{

                    String userName = snapshot.child("name").getValue().toString();

                    userProfileName.setText(userName);

                    ManageFriendRequest();

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    private void ManageFriendRequest() {

        FriendRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(snapshot.hasChild(receiverUserID)){

                            String request_type = snapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if(request_type.equals("sent")){

                                Current_State = "request_sent";
                                AddFriendButton.setText("Cancel Friend Request");
                            }
                            else if(request_type.equals("received")){

                                Current_State = "request_received";
                                AddFriendButton.setText("Accept Friend Request");

                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);

                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        CancelFriendRequest();
                                    }
                                });

                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

        if(!senderUserID.equals(receiverUserID)){


            AddFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AddFriendButton.setEnabled(false);

                    if(Current_State.equals("new")){

                        SendFriendRequest();
                    }

                    if(Current_State.equals("request_sent")){
                        CancelFriendRequest();
                    }
                }
            });


        }else{
            AddFriendButton.setVisibility(View.INVISIBLE);
        }
    }

    private void CancelFriendRequest() {

        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                        if(task.isSuccessful()){

                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                AddFriendButton.setEnabled(true);
                                                Current_State = "new";
                                                AddFriendButton.setText("Add Friend");
                                            }


                                        }
                                    });

                        }

                    }
                });
    }

    private void SendFriendRequest() {

        FriendRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                        if(task.isSuccessful()){

                            FriendRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                AddFriendButton.setEnabled(true);
                                                Current_State = "request_sent";
                                                AddFriendButton.setText("Cancel Friend Request");
                                            }

                                        }
                                    });
                        }

                    }
                });

    }

}