package com.example.photoalbum;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class FriendsFragment extends Fragment {

    private View FriendsView;
    private RecyclerView myFriendsList;

    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;





    public FriendsFragment() {
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
        FriendsView =  inflater.inflate(R.layout.fragment_friends, container, false);

        myFriendsList = (RecyclerView) FriendsView.findViewById(R.id.friends_list);
        myFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");



        return FriendsView;
    }

    ;

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
            protected void onBindViewHolder(@NonNull @NotNull FriendsFragment.FriendsViewHolder holder, int position, @NonNull @NotNull User model) {

                String userIDs = getRef(position).getKey();

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                            String userName = snapshot.child("name").getValue().toString();

                        String imageName = userIDs + ".png";

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



                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });




            }

            @NonNull
            @NotNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {

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

        public FriendsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            userName = itemView.findViewById(R.id.users_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);

        }
    }
}