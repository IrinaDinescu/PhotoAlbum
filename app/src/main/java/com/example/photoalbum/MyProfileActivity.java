package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.jetbrains.annotations.NotNull;

public class MyProfileActivity extends AppCompatActivity {


    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        initializare();
    }

    private void initializare() {

        bottomNavigationView = findViewById(R.id.nav_menu_id_profile);

        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull @NotNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.my_profile_nav_menu_editor:

                        Intent iEditor = new Intent(MyProfileActivity.this, EditorActivity.class);
                        startActivity(iEditor);

                        break;
                }

            }
        });
    }
}