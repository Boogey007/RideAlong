package com.cooldevs.ridealong;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.cooldevs.ridealong.Interface.IFirebaseLoadDone;
import com.cooldevs.ridealong.Interface.IRecycItemListerner;
import com.cooldevs.ridealong.Model.User;
import com.cooldevs.ridealong.Service.MyLocationReceiver;
import com.cooldevs.ridealong.Utils.Commonx;
import com.cooldevs.ridealong.ViewHolder.AllFriendViewHolder;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

import com.cooldevs.ridealong.R;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {

  FirebaseRecyclerAdapter<User, AllFriendViewHolder> adapter, searchAdapter;
  RecyclerView recycler_friend_list;
  IFirebaseLoadDone firebaseLoadDone;
  MaterialSearchBar searchBar;
  List<String> suggestList = new ArrayList<>();
  DatabaseReference publicLocation;
  LocationRequest locationRequest;
  FusedLocationProviderClient fusedLocationProviderClient;
  TextView friend_list_empty;
  EditText num;
  String location_url, ph_num;
  Button temp_btn;
  boolean check = false;
  FusedLocationProviderClient fusedLocationProviderClient2;
  String current_user;
  FirebaseUser firebaseUser;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("Friends");

    fusedLocationProviderClient2 = LocationServices.getFusedLocationProviderClient(this);
    //Get current user for verification though fb
    firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    current_user = firebaseUser.getEmail();

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        Commonx.trackingUser = Commonx.loggedUser;

        startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
        Toast.makeText(HomeActivity.this, "Locating your current position", Toast.LENGTH_LONG).show();
      }
    });

   // this nav was mad hard to build .. im sticking to html
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    // reason for swtching  wayyy to diff to do simple stuff
    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    navigationView.setItemIconTintList(ColorStateList.valueOf(Color.WHITE));
    navigationView.setItemTextColor(ColorStateList.valueOf(Color.WHITE));

    View headerView = navigationView.getHeaderView(0);
    // build top nav
    TextView txt_user_logged = (TextView) headerView.findViewById(R.id.txt_logged_email);
    Log.d("TAG1", "onCreate: " + Commonx.loggedUser.getEmail());
    txt_user_logged.setText(Commonx.loggedUser.getEmail());

    TextView txt_user_loggedName = (TextView) headerView.findViewById(R.id.txt_logged_name);
    Log.d("TAG3", "onCreate: " + Commonx.loggedUser.getClass().toString());
    txt_user_loggedName.setText(Commonx.loggedUser.getPhone());


    CircleImageView profileImageView = headerView.findViewById(R.id.user_profile_image);
    Picasso.get().load(Commonx.loggedUser.getImage()).placeholder(R.drawable.ic_person_outline_black_24dp).into(profileImageView);

    recycler_friend_list = (RecyclerView) findViewById(R.id.recycler_friend_list);
    recycler_friend_list.setHasFixedSize(true);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recycler_friend_list.setLayoutManager(layoutManager);
    recycler_friend_list.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));
    friend_list_empty = (TextView) findViewById(R.id.friend_list_is_empty);

    publicLocation = FirebaseDatabase.getInstance().getReference(Commonx.PUBLIC_LOCATION);
    updateLocation();

    LoadFriends();

    //Emergency notifier button
    FloatingActionButton emergency_fab = (FloatingActionButton) findViewById(R.id.emergency_fab);
    emergency_fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        boolean res = checkLocationPermissions();
        if(res){
          checkLocationPermissions();
        }
        else {
          System.out.println("Sending Message!!!!!!!");
          fetchLocation();
        }
      }
    });
  }

  //Check the location permissions
  protected boolean checkLocationPermissions(){
    final Context context = this;
    LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    boolean gpsEnabled = false;
    boolean networkEnabled = false;

    try {
      gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch(Exception ex) {}

    try {
      networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch(Exception ex) {}

    if(!gpsEnabled && !networkEnabled) {
      new AlertDialog.Builder(context)
              .setMessage("Please enable GPS and Network")
              .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                  context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
              })
              .setNegativeButton("Close",null)
              .show();
      return check = true;
    }
    else{
      return check = false;
    }
  }

  //Fetch the location to be passed with sms alert
  protected void fetchLocation(){
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
      if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
              == PackageManager.PERMISSION_GRANTED){
        fusedLocationProviderClient2.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                  @Override
                  public void onSuccess(Location location) {
                    if(location != null){
                      Double latitude_cor = location.getLatitude();
                      Double longitude_cor = location.getLongitude();
                      location_url = "http://maps.google.com/maps?q=loc:"+latitude_cor+","+longitude_cor;
                    }
                  }
                });
        sendSms();
      }
      else {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
      }
    }
  }

  //Check for the user in DB and fetch his emergency_contact_list
  protected void sendSms(){
    int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
    if(permissionCheck == PackageManager.PERMISSION_GRANTED && permissionCheck1 == PackageManager.PERMISSION_GRANTED ){
      FirebaseDatabase db = FirebaseDatabase.getInstance();
      final DatabaseReference tempRef = db.getReference("UserInformation");
      tempRef.addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
          for(DataSnapshot ds : dataSnapshot.getChildren()){
            if(ds.child("email").getValue().toString().equals(current_user)) {
              DatabaseReference current_ref = ds.child("emergency_contact_list").getRef();
              current_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                  for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ph_num = ds.getValue().toString();
                    sendAlertMessage(ph_num);
                  }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
              });
            }
          }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
      });
    }

    else{

      if( permissionCheck1 == 0 )
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);

      if( permissionCheck == 0 )
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);

    }
  }

  // Send sms notification
  protected void sendAlertMessage(String phone_num){
    String phnNo = phone_num;
    Resources res = getResources();
    String msg = res.getString(R.string.emergency_msg) + location_url;
    SmsManager manager = SmsManager.getDefault();
    manager.sendTextMessage(phnNo, null, msg, null, null);
    Toast.makeText(getApplicationContext(), "Emergency Notification Sent!", Toast.LENGTH_LONG).show();
  }

  //Check permissions for sending sms
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode){
      case 0:
        if(grantResults.length>=0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
          this.sendAlertMessage(ph_num);
        }
        else{
          Toast.makeText(this, "You don't have permissions.",Toast.LENGTH_LONG).show();
        }
    }
  }

  //Loads the accepted friends in the friend list
  private void LoadFriends() {

    // Need to make this to where it can be code specific somehow to not show everyone
    Query query = FirebaseDatabase.getInstance().getReference(Commonx.USER_INFORMATION).child(Commonx.loggedUser.getUid()).child(Commonx.ACCEPT_LIST);

    FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();

    adapter = new FirebaseRecyclerAdapter<User, AllFriendViewHolder>(options) {

      @Override
      protected void onBindViewHolder(@NonNull final AllFriendViewHolder allFriendViewHolder, int i, @NonNull final User user) {
        allFriendViewHolder.all_friends_txt_user_email.setText(new StringBuilder(user.getEmail()));

        DatabaseReference referencex = FirebaseDatabase.getInstance().getReference().child(Commonx.USER_INFORMATION);

        Query newx = referencex.orderByChild("uid").equalTo(user.getUid());

        newx.addValueEventListener(new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (getItemCount() > 0)
              friend_list_empty.setText("Click the user to locate");

            if (dataSnapshot.exists()) {
              for (DataSnapshot userx : dataSnapshot.getChildren()) {

                if (userx.child("image").exists()) {
                  Picasso.get().load(userx.child("image").getValue().toString()).into(allFriendViewHolder.all_friends_profile_image);
                }

              }

            }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) {
          }
        });

        //Displaying the tracking view on clicking friends
        allFriendViewHolder.setiRecycItemListerner(new IRecycItemListerner() {
          @Override
          public void onItemClickListener(View view, int position) {

            Commonx.trackingUser = user;

            startActivity(new Intent(HomeActivity.this, TrackingActivity.class));

          }
        });

        allFriendViewHolder.all_friends_locate_image.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View view) {
            Commonx.userProfile = user;
            startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
          }
        });
      }

      @NonNull
      @Override
      public AllFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_all_friends, parent, false);

        return new AllFriendViewHolder(itemView);
      }

      @NonNull
      @Override
      public User getItem(int position) {
        return super.getItem(position);
      }
    };

    adapter.startListening();
    recycler_friend_list.setAdapter(adapter);
  }

  @Override
  protected void onStop() {

    if (adapter != null) adapter.stopListening();
    if (searchAdapter != null) searchAdapter.stopListening();
    super.onStop();
  }

  @Override
  protected void onResume() {

    super.onResume();

    if (adapter != null) adapter.startListening();
    if (searchAdapter != null) searchAdapter.startListening();
  }

  private void updateLocation() {

    buildLocationRequest();
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return;
    }
    fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
  }

  private PendingIntent getPendingIntent() {

    Intent intent = new Intent(HomeActivity.this, MyLocationReceiver.class);
    intent.setAction(MyLocationReceiver.ACTION);

    return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

  private void buildLocationRequest() {

    locationRequest = new LocationRequest();
    locationRequest.setSmallestDisplacement(10f);
    locationRequest.setFastestInterval(3000);
    locationRequest.setInterval(5000);
    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
      finishAffinity();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.activity_home_drawer, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("StatementWithEmptyBody")@Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();

    if (id == R.id.nav_find_people) {

      // TODO: implement some sort of code to use here...
      Intent showallusers = new Intent(HomeActivity.this, AllPeopleActivity.class);
      startActivity(showallusers);

    } else if (id == R.id.nav_add_people) {
      Intent showfriendreq = new Intent(HomeActivity.this, FriendRequestActivity.class);
      startActivity(showfriendreq);

    } else if (id == R.id.nav_cars) {
      Intent Cars = new Intent(HomeActivity.this, CarsActivity.class);
      startActivity(Cars);

    }
    else if (id == R.id.nav_sign_out) {

      Commonx.loggedUser = null;

      AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener < Void > () {

        @Override
        public void onComplete(@NonNull Task < Void > task) {


        }
      });
      finish();

      startActivity(new Intent(HomeActivity.this, FinalMainActivity.class));
      Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

    }
    else if (id == R.id.nav_user_settings)
      startActivity(new Intent(HomeActivity.this, SettingsActivity.class));

    else if( id == R.id.add_emergency_contact)
    {
      final AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this);
      View mView = getLayoutInflater().inflate(R.layout.add_emergency_dialog,null);
      final EditText txt_inputText = (EditText)mView.findViewById(R.id.txt_input);
      Button btn_cancel = (Button)mView.findViewById(R.id.btn_cancel);
      Button btn_okay = (Button)mView.findViewById(R.id.btn_okay);
      alert.setView(mView);
      num = txt_inputText;
      temp_btn = btn_okay;
      final AlertDialog alertDialog = alert.create();
      alertDialog.setCanceledOnTouchOutside(false);
      btn_cancel.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          alertDialog.dismiss();
        }
      });
      btn_okay.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Pattern pattern = Pattern.compile("^[0-9]{10}$");
          Matcher matcher = pattern.matcher(num.getText().toString());
          boolean b = matcher.matches();
          if(b){
            addContact();
            alertDialog.dismiss();
            Toast.makeText(getApplicationContext(), "Contact Added Successfully!", Toast.LENGTH_SHORT).show();
          }
          else{
            Toast.makeText(getApplicationContext(), "Invalid Number!", Toast.LENGTH_SHORT).show();
          }
        }
      });
      alertDialog.show();
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    drawer.closeDrawer(GravityCompat.START);
    return true;
  }

  @Override
  public void onFirebaseLoadUserNameDone(List < String > lstEmail) {

    searchBar.setLastSuggestions((lstEmail));
  }

  @Override
  public void onFirebaseLoadFailed(String message) {

    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  protected void addContact() {
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    final DatabaseReference myRef = db.getReference("UserInformation");
    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for(DataSnapshot ds : dataSnapshot.getChildren()){
          if(ds.child("email").getValue().toString().equals(current_user)) {
            DatabaseReference current_ref = ds.getRef();
            current_ref.child("emergency_contact_list").push().setValue(num.getText().toString().trim());
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError) {

      }
    });
  }
}