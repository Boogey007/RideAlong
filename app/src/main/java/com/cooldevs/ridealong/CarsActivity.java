package com.cooldevs.ridealong;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cooldevs.ridealong.Utils.Commonx;

public class CarsActivity extends AppCompatActivity {
    Button addCar;
    ListView carsList;

    private DatabaseReference mDatabase;


    List<String> list ;
    HashMap<String, String> cars1;
    ArrayAdapter<String> adapter;
    String loggedUsername;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cars);

        addCar = findViewById(R.id.addCarButton);
        carsList = findViewById(R.id.carsList);

        list = new ArrayList<String>();
        cars1 = new HashMap<String, String>();



       adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                list );

        carsList.setAdapter(adapter);

        Query car = FirebaseDatabase.getInstance()
                .getReference(Commonx.USER_INFORMATION)
                .child(Commonx.loggedUser.getUid())
                .child(Commonx.Car);

        loggedUsername = Commonx.loggedUser.getEmail();

        //opening the location when any car name is clicked
        carsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = carsList.getItemAtPosition(position);
                String str = o.toString();
                Toast.makeText(CarsActivity.this, str, Toast.LENGTH_LONG).show();
                Intent i = new Intent(CarsActivity.this, LocationsActivity.class);
                i.putExtra("car", cars1.get(str));
                Log.d("TAG11",cars1.get(str));
                    i.putExtra("user", loggedUsername);
                    startActivity(i);
            }
        });

        car.addValueEventListener(new ValueEventListener() {

            //iterates through cars added by the user and populates the list of car names
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("TAG","testing");
                Log.d("TAG1",dataSnapshot.toString());

                adapter.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Log.d("TAG2",ds.toString());
                    //Log.d("TAG","testing");
                //for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    //gets name of car from Cars
                    String name = ds.child("name").getValue().toString();
                    Log.d("TAG",name);
                    Log.d("TAG3",ds.child("name").getKey());
                    cars1.put(name, ds.getKey());
                    Log.d("TAG4",ds.getKey());
                    Log.d("TAG5",ds.getValue().toString());

                    adapter.add(name);
                }
                if(cars1.containsValue("1") && cars1.size() > 1 ){
                    //mDatabase.child("Families").child(family).child("cars").child("1").removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //adding car on clicking Add Car button
       addCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i = new Intent(CarsActivity.this, AddCarActivity.class);
                    startActivity(i);
            }
        });
    }
}
