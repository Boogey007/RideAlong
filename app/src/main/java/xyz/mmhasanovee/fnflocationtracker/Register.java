package com.example.ridealong;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private EditText mEmail, mPass, mPhone, mName;
    private Button mRegister;
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private static final String USER = "user";
    private static final String TAG = "RegisterActivity";
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail = (EditText) findViewById(R.id.etemail);
        mPass = (EditText) findViewById(R.id.pass);
        mRegister = (Button) findViewById(R.id.reg);
        mName = (EditText) findViewById(R.id.etname);
        mPhone = (EditText) findViewById(R.id.etmob);
        mRegister = (Button) findViewById(R.id.btnegister);

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference(USER);
        mAuth = FirebaseAuth.getInstance();

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                String password = mPass.getText().toString();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(getApplicationContext(), "Enter email and password!",Toast.LENGTH_LONG).show();
                    return;
                }
                String fullname = mName.getText().toString();
                String phone = mPhone.getText().toString();
                user = new User(email,password,fullname,phone);
                registerUser(email,password);
            }
        });

    }
    public void registerUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }
    public void updateUI(FirebaseUser currentuser){
        String keyId = mDatabase.push().getKey();
        mDatabase.child(keyId).setValue(user);
        Intent loginIntent = new Intent(this,MainActivity.class);
        startActivity(loginIntent);
    }
}