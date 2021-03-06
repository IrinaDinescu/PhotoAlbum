package com.example.photoalbum;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;

import org.jetbrains.annotations.NotNull;

public class EditorActivity extends AppCompatActivity {

    Button btPick;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        initializare();
    }

    private void initializare() {

        btPick = findViewById(R.id.editor_bt_pick);
        imageView = findViewById(R.id.editor_iamge_view);

        btPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermission();
            }
        });
    }

    private void checkPermission() {

        int permission = ActivityCompat.checkSelfPermission(EditorActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            pickImage();
        }else {

            if (permission != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(EditorActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        100);
            }else{
                pickImage();
            }

        }
    }

    private void pickImage() {

        Intent intent =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(intent, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 100 && grantResults.length >0 && grantResults[0]
        == PackageManager.PERMISSION_GRANTED){
            pickImage();
        }else{
            Toast.makeText(getApplicationContext(),
                    "Permission Denied",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            Uri uri = data.getData();
            switch (requestCode){
                case 100:
                    Intent intent = new Intent(EditorActivity.this,
                            DsPhotoEditorActivity.class);
                    intent.setData(uri);
                    intent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY,
                            "Images");
                    intent.putExtra(DsPhotoEditorConstants.DS_TOOL_BAR_BACKGROUND_COLOR,
                            Color.parseColor("#FF6200EE"));
                    intent.putExtra(DsPhotoEditorConstants.DS_MAIN_BACKGROUND_COLOR,
                            Color.parseColor("#FFFFFF"));
                    intent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_TOOLS_TO_HIDE,
                            new int[]{DsPhotoEditorActivity.TOOL_WARMTH,
                            DsPhotoEditorActivity.TOOL_PIXELATE});
                    startActivityForResult(intent, 101);
                    break;
                case 101:
                    imageView.setImageURI(uri);
                    Toast.makeText(getApplicationContext(),
                            "Photo saved", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }
}