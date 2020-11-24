package com.cooldevs.ridealong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.cooldevs.ridealong.Utils.Commonx;

public class AddCarActivity extends AppCompatActivity {

    EditText enterCarName;
    Button addNewCarButton;
    //ImageButton back;
    String family;

    private DatabaseReference mDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcar);

        //back = findViewById(R.id.backArrowCarsButton);
        enterCarName = findViewById(R.id.enterCarName);
        addNewCarButton = findViewById(R.id.addNewCarButton);

        //mDatabase = FirebaseDatabase.getInstance().getReference();
        //family = getIntent().getExtras().getString("family");

        addNewCarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(enterCarName.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(), "Please enter a car name", Toast.LENGTH_SHORT).show();
                }
                else{
                    DatabaseReference car = FirebaseDatabase.getInstance()
                            .getReference(Commonx.USER_INFORMATION)
                            .child(Commonx.loggedUser.getUid())
                            .child(Commonx.Car);

                    String newId = car.push().getKey();
                    car.child(newId).child("name").setValue(enterCarName.getText().toString());
                    //String newId = car.getKey();
                    Log.d("carkey",newId);

                    car.child(newId).child("latitude").setValue(38.68);
                    car.child(newId).child("longitude").setValue(-101.07);
                    car.child(newId).child("parkedBy").setValue("");

                    /*String newId = mDatabase.child("Cars").push().getKey();
                    mDatabase.child("Cars").child(newId).child("name").setValue(enterCarName.getText().toString());
                    mDatabase.child("Cars").child(newId).child("latitude").setValue(0.1);
                    mDatabase.child("Cars").child(newId).child("longitude").setValue(0.1);
                    mDatabase.child("Cars").child(newId).child("parkedBy").setValue("");
                    mDatabase.child("Families").child(family).child("cars").child(newId).setValue(true);*/
                    Toast.makeText(getApplicationContext(), "Car successfully created", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });



    }
}
