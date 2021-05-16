package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.photoalbum.clase.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerList;
    private DatabaseReference UsersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        findFriendsRecyclerList = (RecyclerView) findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = (Toolbar) findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(UsersRef, User.class)
                .build();

        FirebaseRecyclerAdapter<User, FindFriendsViewHolder> adapter =
                new FirebaseRecyclerAdapter<User, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull @NotNull FindFriendsActivity.FindFriendsViewHolder holder, int position, @NonNull @NotNull User model) {

                        holder.userName.setText(model.getName());
                       // Picasso.get().load(model.getProfileImageUri()).into(holder.profileImage);

                    }

                    @NonNull
                    @NotNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {

                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                        return viewHolder;
                    }
                };

        findFriendsRecyclerList.setAdapter(adapter);

        adapter.startListening();


    }

    public  static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName;
        CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            userName = itemView.findViewById(R.id.users_profile_name);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}