package com.example.photoalbum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class MembersActivity extends AppCompatActivity {

    private String currentGroupID;
    private RecyclerView MembersList;

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
    }
}