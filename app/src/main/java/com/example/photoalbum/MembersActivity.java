package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.photoalbum.clase.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersActivity extends AppCompatActivity {

    private String currentGroupID;
    private RecyclerView MembersList;

    private DatabaseReference UserRef;

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



                        ValueEventListener eventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                if(!snapshot.exists()){
                                    holder.Layout_hide();
                                }else{

                                    String status = snapshot.child("status").getValue().toString();
                                    holder.status.setText(status);
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

    public static class MembersViewHolder extends RecyclerView.ViewHolder{

        final LinearLayout layout;
        final LinearLayout.LayoutParams params;

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

        }

        public void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            layout.setLayoutParams(params);   //Or This one.

        }
    }

}