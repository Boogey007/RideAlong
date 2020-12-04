package com.cooldevs.ridealong;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.shobhitpuri.custombuttons.GoogleSignInButton;

import com.cooldevs.ridealong.R;

public class FinalMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_main);

        GoogleSignInButton signInButtonx;
        signInButtonx = (GoogleSignInButton) findViewById(R.id.google_connect);
        Button register;
        //register = findViewById(R.id.sign_up);

        signInButtonx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(FinalMainActivity.this, MainActivity.class));
            }
        });

       // register.setOnClickListener(new View.OnClickListener() {
       //     @Override
      //      public void onClick(View view) {
        //        Log.i("TAG1", "onClick: BUTTON CLICKED");
        //        startActivity(new Intent(FinalMainActivity.this, RegisterActivity.class));
        //    }
       // });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}