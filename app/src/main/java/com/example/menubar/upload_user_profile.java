package com.example.menubar;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class upload_user_profile extends AppCompatActivity {

    ImageView uploadImage;
    Button regButton;
    EditText e1,e2;
    String imgUrl;
    Uri uri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_user_profile);

        uploadImage=findViewById(R.id.uploadprofile);
        e1=findViewById(R.id.username);
        e2=findViewById(R.id.password);



    }
}