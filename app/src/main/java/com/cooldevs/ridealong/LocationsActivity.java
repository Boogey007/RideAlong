package com.cooldevs.ridealong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.TextView;
import android.widget.Toast;

import com.cooldevs.ridealong.Utils.Commonx;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import androidx.appcompat.app.AppCompatActivity;

public class LocationsActivity extends AppCompatActivity implements OnMapReadyCallback{
    Button park;
    TextView currentDriver;
    TextView selectedCar, locationText;

    private DatabaseReference mDatabase;
    private String username;

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        park = findViewById(R.id.parkButton);
        currentDriver = findViewById(R.id.currentDriver);
        selectedCar = findViewById(R.id.selectedCar);
        locationText = findViewById(R.id.carLoc);
        String carID = getIntent().getExtras().getString("car");
        username = getIntent().getExtras().getString("user");

        mDatabase = FirebaseDatabase.getInstance()
                        .getReference(Commonx.USER_INFORMATION)
                       .child(Commonx.loggedUser.getUid())
                       .child(Commonx.Car).child(carID);

        Query car = FirebaseDatabase.getInstance()
                .getReference(Commonx.USER_INFORMATION)
                .child(Commonx.loggedUser.getUid())
                .child(Commonx.Car).child(carID);

        //updates the username and location on UI once the Park button is clicked and current location gets updated
        car.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selectedCar.setText("Car: "+dataSnapshot.child("name").getValue().toString());

                Log.d("TAGLoc",dataSnapshot.getValue().toString());
                currentDriver.setText("Parked by: " + username);
                Log.d("TAGUser",username);

                currentLocation = new Location("");
                Double lat = (Double)dataSnapshot.child("latitude").getValue();
                Double lon = (Double)dataSnapshot.child("longitude").getValue();
                currentLocation.setLatitude(lat);
                currentLocation.setLongitude(lon);
                locationText.setText("Lat: "+currentLocation.getLatitude() + " " + "Long: "+currentLocation.getLongitude());
                Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.myMap);
                assert supportMapFragment != null;
                supportMapFragment.getMapAsync(LocationsActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //fetches the current location on click of Park button
        park.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLocation();
            }
        });
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    mDatabase.child("latitude").setValue(currentLocation.getLatitude());
                    mDatabase.child("longitude").setValue(currentLocation.getLongitude());
                    mDatabase.child("parkedBy").setValue(username);
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("I am here!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(markerOptions);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }
}