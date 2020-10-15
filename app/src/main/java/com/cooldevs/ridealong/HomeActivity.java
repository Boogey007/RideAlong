package com.cooldevs.ridealong;

import android.app.PendingIntent;
import android.content.Intent;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;

import com.google.android.material.navigation.NavigationView;
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import com.cooldevs.ridealong.R;

public class HomeActivity extends AppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener, IFirebaseLoadDone {

  FirebaseRecyclerAdapter <User, AllFriendViewHolder> adapter, searchAdapter;
  RecyclerView recycler_friend_list;
  IFirebaseLoadDone firebaseLoadDone;
  MaterialSearchBar searchBar;
  List < String > suggestList = new ArrayList < >();
  DatabaseReference publicLocation;
  LocationRequest locationRequest;
  FusedLocationProviderClient fusedLocationProviderClient;
  TextView friend_list_empty;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("Friends");

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {@Override
      public void onClick(View view) {

        Commonx.trackingUser = Commonx.loggedUser;

        startActivity(new Intent(HomeActivity.this, TrackingActivity.class));
        Toast.makeText(HomeActivity.this, "Locating your current position", Toast.LENGTH_LONG).show();
      }
    });

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    navigationView.setItemIconTintList(null);

    View headerView = navigationView.getHeaderView(0);

    TextView txt_user_logged = (TextView) headerView.findViewById(R.id.txt_logged_emaail);
    txt_user_logged.setText(Commonx.loggedUser.getEmail());

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
  }

  private void LoadFriends() {

    // Need to make this to where it can be code specific somehow to not show everyone
    Query query = FirebaseDatabase.getInstance().getReference(Commonx.USER_INFORMATION).child(Commonx.loggedUser.getUid()).child(Commonx.ACCEPT_LIST);

    FirebaseRecyclerOptions < User > options = new FirebaseRecyclerOptions.Builder < User > ().setQuery(query, User.class).build();

    adapter = new FirebaseRecyclerAdapter < User,AllFriendViewHolder > (options) {
      
      @Override
      protected void onBindViewHolder(@NonNull final AllFriendViewHolder allFriendViewHolder, int i, @NonNull final User user) {
        allFriendViewHolder.all_friends_txt_user_email.setText(new StringBuilder(user.getEmail()));

        DatabaseReference referencex = FirebaseDatabase.getInstance().getReference().child(Commonx.USER_INFORMATION);

        Query newx = referencex.orderByChild("uid").equalTo(user.getUid());

        newx.addValueEventListener(new ValueEventListener() {@Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if (getItemCount() > 0) 
              friend_list_empty.setText("Click the user to locate");

            if (dataSnapshot.exists()) {
              for (DataSnapshot userx: dataSnapshot.getChildren()) {

                if (userx.child("image").exists()) {
                  Picasso.get().load(userx.child("image").getValue().toString()).into(allFriendViewHolder.all_friends_profile_image);
                }

              }

            }

          }

          @Override
          public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        allFriendViewHolder.setiRecycItemListerner(new IRecycItemListerner() {@Override
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

      @NonNull@Override
      public AllFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_all_friends, parent, false);

        return new AllFriendViewHolder(itemView);
      }

      @NonNull@Override
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

    } else if (id == R.id.nav_sign_out) {

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
}