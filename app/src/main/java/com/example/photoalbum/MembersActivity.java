package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersActivity extends AppCompatActivity {

    private String currentGroupID;
    private RecyclerView MembersList;

    private DatabaseReference UserRef;

    private String currentUserID;
    private String currentUserGroupStatus;

    private String selectedMemberStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);

        initalizare();
    }

    private void initalizare() {

        MembersList = (RecyclerView) findViewById(R.id.members_list);
        MembersList.setLayoutManager(new LinearLayoutManager(this));

        currentGroupID = getIntent().getExtras().get("GroupID").toString();



        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        currentUserID = FirebaseAuth.getInstance().getUid();

        DatabaseReference userMembershipsRef = FirebaseDatabase.getInstance().getReference().child("Memberships").child(currentUserID).child(currentGroupID).child("status");

        userMembershipsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                currentUserGroupStatus = snapshot.getValue().toString().trim();
                Log.v("UserStatusINGroup", currentUserGroupStatus);

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });








    }

    @Override
    protected void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(UserRef, User.class)
                .build();

        FirebaseRecyclerAdapter<User, MembersViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, MembersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull @NotNull MembersActivity.MembersViewHolder holder, int position, @NonNull @NotNull User model) {

                        holder.userName.setText(model.getName());

                        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
                        String uid = model.getUid().toString();
                        DatabaseReference ref = RootRef.child("Memberships").child(uid).child(currentGroupID);

                        String imageName = model.getUid() + ".png";

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



                        holder.parentLayout.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                Toast.makeText(MembersActivity.this, "LongClickTest", Toast.LENGTH_SHORT).show();




                                MyCustomAlertDialog(uid, model.getName(), profileRef );

                                return false;
                            }
                        });

                        ValueEventListener eventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                if(!snapshot.exists()){
                                    holder.Layout_hide();
                                }else{




                                    if(snapshot.child("status").exists()){

                                        String status = snapshot.child("status").getValue().toString();
                                        holder.status.setText(status);
                                    }


                                }
                            }


                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        };

                        ref.addValueEventListener(eventListener);
                    }

                    @NonNull
                    @NotNull
                    @Override
                    public MembersViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.member_display_layout, viewGroup, false);
                        MembersViewHolder viewHolder = new MembersViewHolder(view);
                        return viewHolder;

                    }
                };

        MembersList.setAdapter(adapter);
        adapter.startListening();

    }

    private void MyCustomAlertDialog(String uid, String name, StorageReference profileRef) {

        final Dialog MyDialog = new Dialog(MembersActivity.this);
        MyDialog.setContentView(R.layout.customdialog_members_onlongclick);

        Window window = MyDialog.getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        CircleImageView profileImage = (CircleImageView) MyDialog.findViewById(R.id.memberdialog_profileImage);
        TextView tv_UserName = (TextView) MyDialog.findViewById(R.id.memberdialog_tv_userName);
        TextView tv_SeeProfile = (TextView) MyDialog.findViewById(R.id.memberdialog_tv_seeProfile);
        TextView tv_MakeAdmin = (TextView) MyDialog.findViewById(R.id.memberdialog_tv_makeAdmin);
        TextView tv_DeleteMember = (TextView) MyDialog.findViewById(R.id.memberdialog_tv_deleteMember);

        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get()
                        .load(uri)
                        .into(profileImage);

            }
        });

        tv_SeeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent profileIntent = new Intent(MembersActivity.this, ProfileActivity.class);
                profileIntent.putExtra("visit_user_id", uid);
                startActivity(profileIntent);


            }
        });

        tv_UserName.setText(name);

        if(!currentUserGroupStatus.equals("admin")){

            tv_MakeAdmin.setVisibility(View.INVISIBLE);
            tv_DeleteMember.setVisibility(View.INVISIBLE);
        }else{

            DatabaseReference memberStatusRef = FirebaseDatabase.getInstance().getReference().child("Memberships").child(uid).child(currentGroupID).child("status");

            memberStatusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {


                    if(snapshot.exists()){
                        selectedMemberStatus = snapshot.getValue().toString();

                        Log.v("SatusTestUid", uid);

                        Log.v("StatusTest", "Succes " + selectedMemberStatus);


                        if(selectedMemberStatus.equals("admin")){

                            tv_MakeAdmin.setText(" Remove Admin Status");
                        }

                    }else{

                        Log.v("SatusTestUid", uid);

                        Log.v("StatusTest", "Failed");
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });


        }


        tv_DeleteMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(MembersActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")


                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                DatabaseReference membershipRef = FirebaseDatabase.getInstance().getReference().child("Memberships").child(uid).child(currentGroupID);
                                membershipRef.removeValue();

                                Toast.makeText(MembersActivity.this, "Member Succesfully Deleted!", Toast.LENGTH_SHORT).show();

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();


            }
        });











        MyDialog.show();

    }

    public static class MembersViewHolder extends RecyclerView.ViewHolder{

        final LinearLayout layout;
        final LinearLayout.LayoutParams params;

        LinearLayout parentLayout ;

        TextView userName;
        TextView status;
        CircleImageView profileImage;



        public MembersViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            layout =(LinearLayout)itemView.findViewById(R.id.member_linear_layout);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);


            userName = itemView.findViewById(R.id.member_profile_name);
            profileImage = itemView.findViewById(R.id.member_profile_image);
            status = itemView.findViewById(R.id.member_status);

            parentLayout = itemView.findViewById(R.id.members_display_layoutTest2);

        }

        public void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            layout.setLayoutParams(params);   //Or This one.

        }
    }

}