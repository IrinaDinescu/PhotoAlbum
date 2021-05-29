package com.example.photoalbum;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.photoalbum.clase.Group;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class GroupsFragment extends Fragment {

    private View groupsFragmentView;
    private RecyclerView myGroupsList;
    //    private ArrayAdapter<String> arrayAdapter;
   // private ArrayList<String> list_of_groups = new ArrayList<>();

    private DatabaseReference MembershipsRef;
    private DatabaseReference GroupsRef;
    private FirebaseAuth mAuth;

    private String currentUsserID;

    public GroupsFragment() {

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


        groupsFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        myGroupsList = (RecyclerView) groupsFragmentView.findViewById(R.id.groups_recycler_view);
        myGroupsList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();

        if(mAuth !=null){
            currentUsserID = mAuth.getCurrentUser().getUid();
        }


        MembershipsRef = FirebaseDatabase.getInstance().getReference().child("Memberships").child(currentUsserID);
        GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");



        return groupsFragmentView;








    }


    @Override
    public void onStart() {
        super.onStart();

        RetrieveAndDisplayGroups();
    }

    private void RetrieveAndDisplayGroups(){

        String groupName;

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Group>()
                .setQuery(MembershipsRef, Group.class)
                .build();

        FirebaseRecyclerAdapter<Group, GroupsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Group, GroupsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull @NotNull GroupsFragment.GroupsViewHolder holder, int position, @NonNull @NotNull Group model) {


                String groupID = getRef(position).getKey();

                GroupsRef.child(groupID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                        if(snapshot.hasChild("name")){
                            if(snapshot.child("name").getValue() != null){
                                String groupName = snapshot.child("name").getValue().toString();
                                holder.groupName.setText(groupName);
                            }
                        }

                    }



                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String currentGroupID = getRef(position).getKey();
                        DatabaseReference currentGroupRef = GroupsRef.child(currentGroupID);

                        currentGroupRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                String currentGroupName = snapshot.child("name").getValue().toString();

                                Intent GroupIntent = new Intent(getContext(), GroupActivity.class);
                                GroupIntent.putExtra("groupName", currentGroupName);
                                GroupIntent.putExtra("groupID", currentGroupRef.getKey().toString());

                                startActivity(GroupIntent);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });






                    }
                });

            }

            @NonNull
            @NotNull
            @Override
            public GroupsViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup viewGroup, int viewType) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                GroupsViewHolder viewHolder = new GroupsViewHolder(view);
                return  viewHolder;
            }
        };

        myGroupsList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class GroupsViewHolder extends RecyclerView.ViewHolder{

        TextView groupName;
        CircleImageView groupProfileImage;

        public GroupsViewHolder(@NonNull @NotNull View itemView) {

            super(itemView);

            //------> TODO sa schimb cu un group view
            groupName = itemView.findViewById(R.id.users_profile_name);
            groupProfileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}