package com.example.photoalbum;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.photoalbum.clase.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class RequestFragment extends Fragment {

    private View RequestFragmnetView;
    private RecyclerView myRequestList;

    private DatabaseReference FriendRequestRef, UserRef, FriendsRef;
    private FirebaseAuth mAuth;

    private String currentUserId;


    public RequestFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmnetView =  inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();


        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Requests");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        myRequestList = (RecyclerView) RequestFragmnetView.findViewById(R.id.friend_request_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));



        return RequestFragmnetView;



    }


    @Override
    public void onStart() {

        Log.v("reguest_test", "tessssss");
        Log.v("request_test", "test " + currentUserId);

        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(FriendRequestRef.child(currentUserId), User.class)
                .build();


        FirebaseRecyclerAdapter<User, RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull @NotNull RequestFragment.RequestViewHolder holder, int position, @NonNull @NotNull User model) {

                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_decline_btn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                if(snapshot.exists()){

                                    String type = snapshot.getValue().toString();

                                    if(type.equals("received")){

                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                                if(snapshot.hasChild("profileImageUri")){


                                                    final String requestProfileImage = snapshot.child("profileImageUri").getValue().toString();

                                                    if(requestProfileImage.length() >0 ){
                                                        Picasso.get().load(requestProfileImage).into(holder.profileImage);
                                                    }

                                                }

                                                final String requestUserName = snapshot.child("name").getValue().toString();
                                                holder.userName.setText(requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {

                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Decline"
                                                                };

                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName + " Friend Request");

                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                if(which == 0){

                                                                    FriendsRef.child(currentUserId).child(list_user_id).child("Friends")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                            if(task.isSuccessful()){

                                                                                FriendsRef.child(list_user_id).child(currentUserId).child("Friends")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                                        if(task.isSuccessful()){

                                                                                            FriendRequestRef.child(currentUserId).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                                                            if(task.isSuccessful()){

                                                                                                                FriendRequestRef.child(list_user_id).child(currentUserId)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                                                                                if(task.isSuccessful()){

                                                                                                                                    Toast.makeText(getContext(), "You two are now friends!", Toast.LENGTH_SHORT).show();


                                                                                                                                }

                                                                                                                            }
                                                                                                                        });


                                                                                                            }

                                                                                                        }
                                                                                                    });


                                                                                        }

                                                                                    }
                                                                                });


                                                                            }

                                                                        }
                                                                    });





                                                                }
                                                                if(which == 1){

                                                                    FriendRequestRef.child(currentUserId).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                                    if(task.isSuccessful()){

                                                                                        FriendRequestRef.child(list_user_id).child(currentUserId)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                                                                        if(task.isSuccessful()){

                                                                                                            Toast.makeText(getContext(), "Friend Request Declined!", Toast.LENGTH_SHORT).show();


                                                                                                        }

                                                                                                    }
                                                                                                });


                                                                                    }

                                                                                }
                                                                            });





                                                                }

                                                            }
                                                        });

                                                        builder.show();


                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                            }
                                        });

                                    }else{

                                        holder.Layout_hide();;

                                    }




                                }
                            }



                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });


                    }

                    @NonNull
                    @NotNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;


                    }
                };


        myRequestList.setAdapter(adapter);
        adapter.startListening();


    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{

        final LinearLayout layout;
        final LinearLayout.LayoutParams params;

        TextView userName;
        CircleImageView profileImage;
        Button AcceptButton, DeclineButton;

        public RequestViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            layout =(LinearLayout)itemView.findViewById(R.id.users_linear_layout);
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            userName = itemView.findViewById(R.id.users_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            DeclineButton = itemView.findViewById(R.id.request_decline_btn);


        }

        public void Layout_hide() {
            params.height = 0;
            //itemView.setLayoutParams(params); //This One.
            layout.setLayoutParams(params);   //Or This one.

        }
    }
}


