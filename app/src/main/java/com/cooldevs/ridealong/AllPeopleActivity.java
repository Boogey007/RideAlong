package com.cooldevs.ridealong;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.cooldevs.ridealong.Interface.IFirebaseLoadDone;
import com.cooldevs.ridealong.Interface.IRecycItemListerner;
import com.cooldevs.ridealong.Model.MyResponse;
import com.cooldevs.ridealong.Model.Request;
import com.cooldevs.ridealong.Model.User;
import com.cooldevs.ridealong.Remote.FBMessenger;
import com.cooldevs.ridealong.Utils.Commonx;
import com.cooldevs.ridealong.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import com.cooldevs.ridealong.R;

public class AllPeopleActivity extends AppCompatActivity implements IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter,searchAdapter;
    RecyclerView rec_all_user;
    IFirebaseLoadDone fbLoadFinsih;


    MaterialSearchBar searchBar;
    List<String> suggestList = new ArrayList<>();

    FBMessenger fbmessenger;
    CompositeDisposable compositeDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);

        fbmessenger = Commonx.getFBService();

        searchBar = (MaterialSearchBar) findViewById(R.id.m_search_bar);
        searchBar.setCardViewElevation(5);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 List<String> suggest = new ArrayList<>();
                 for(String search:suggestList){
                     // simple search progressive lookup
                     if(search.toLowerCase().contains(searchBar.getText().toLowerCase()))
                         suggest.add(search);
                 }
                 searchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });
        searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled){

                    if(adapter !=null){
                       // next v2 is to make this not show all users
                        rec_all_user.setAdapter(adapter);
                    }
                }

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        rec_all_user = (RecyclerView) findViewById(R.id.recycler_all_people);
        rec_all_user.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rec_all_user.setLayoutManager(layoutManager);
        rec_all_user.addItemDecoration(new DividerItemDecoration(this,((LinearLayoutManager) layoutManager).getOrientation()));
        // next v2 is to make this not show all users

        fbLoadFinsih = this;
        getUsers();
        getSeacrh();

    }


    private void getUsers() {

        // can really get tid of this or maybe only get users that you dont have on friends list
        Query query = FirebaseDatabase.getInstance().getReference().child(Commonx.USER_INFORMATION);
        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userViewHolder, int i, @NonNull final User user) {
                    if(user.getUid().equals(Commonx.loggedUser.getUid())){

                        if(user.getImage()!=null){
                            Picasso.get().load(Commonx.loggedUser.getImage()).into(userViewHolder.recycler_profile_image);}

                            userViewHolder.txt_user_email.setText(new StringBuilder(user.getEmail()).append(" (me)"));
                           // userViewHolder.txt_user_phoneNumber.setText(new StringBuilder(user.getPhone()));

                            userViewHolder.itemView.setClickable(false);
                            userViewHolder.txt_user_email.setTypeface(userViewHolder.txt_user_email.getTypeface(), Typeface.ITALIC);
                    }

                    else {



                        if(user.getImage()!=null){
                        Picasso.get().load(user.getImage()).into(userViewHolder.recycler_profile_image);}

                        userViewHolder.txt_user_email.setText(new StringBuilder(user.getEmail()));



                    }

                    userViewHolder.setiRecycItemListerner(new IRecycItemListerner() {
                        @Override
                        public void onItemClickListener(View view, int position) {

                            showDialogRequest(user);

                        }
                    });





            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_user,parent,false);

            return new UserViewHolder(itemView);
            }
        };

        adapter.startListening();
       rec_all_user.setAdapter(adapter);

    }



    private void showDialogRequest(final User user) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this,R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);


        alertDialog.setTitle("Send Request");
        alertDialog.setMessage("Do you want to send friend request to "+user.getEmail()+"?");


        alertDialog.setIcon(R.drawable.ic_person_add_black_24dp);

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });

        alertDialog.setPositiveButton(" Send ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //adding to accepted list
                DatabaseReference acceptList = FirebaseDatabase.getInstance()
                        .getReference(Commonx.USER_INFORMATION)
                        .child(Commonx.loggedUser.getUid())
                        .child(Commonx.ACCEPT_LIST);

                acceptList.orderByKey().equalTo(user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if(dataSnapshot.getValue()==null){
                                    sendFriendRequest(user); //already not added
                                }
                                else
                                    Toast.makeText(AllPeopleActivity.this, "You are already friend with "+user.getEmail(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

            }
        });

        alertDialog.show();

    }

    private void sendFriendRequest(final User user) {

        //get user token

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Commonx.TOKENS);

        tokens.orderByKey().equalTo(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()==null)
                            Toast.makeText(AllPeopleActivity.this, "Token error", Toast.LENGTH_SHORT).show(); //cancel
                        else{
                            //create req

                            Request request = new Request();

                            //create data
                            final Map<String,String> dataSend = new HashMap<>();
                            dataSend.put(Commonx.FROM_UID,Commonx.loggedUser.getUid());

                            dataSend.put(Commonx.FROM_NAME,Commonx.loggedUser.getEmail());
                            dataSend.put(Commonx.TO_UID,user.getUid());

                            dataSend.put(Commonx.TO_NAME,user.getEmail());


                            request.setTo(dataSnapshot.child(user.getUid()).getValue(String.class));

                            request.setData(dataSend);

                            //send data

                            compositeDisposable.add(fbmessenger.sendFriendRequestToUser(request)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<MyResponse>() {
                                        @Override
                                        public void accept(MyResponse myResponse) throws Exception {
                                            if(myResponse.success==1){
                                                DatabaseReference fr = FirebaseDatabase.getInstance().getReference(Commonx.USER_INFORMATION)
                                                        .child(dataSend.get(Commonx.TO_UID)).child(Commonx.FRIEND_REQUEST);
                                                User userx = new User();
                                                userx.setUid(dataSend.get(Commonx.FROM_UID));
                                                userx.setEmail(dataSend.get(Commonx.FROM_NAME));
                                                //userx.setPhone(dataSend.get(Commonx.FROM_PHONE));

                                                fr.child(userx.getUid()).setValue(userx);

                                                Toast.makeText(AllPeopleActivity.this, "Request Sent!", Toast.LENGTH_SHORT).show();

                                            }

                                        }

                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) throws Exception {

                                            Toast.makeText(AllPeopleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    }));

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    @Override
    protected void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        if(searchAdapter!=null){
            searchAdapter.stopListening();
        }
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null)
            adapter.startListening();
        if(searchAdapter!=null)
            searchAdapter.startListening();

    }

    private void getSeacrh() {
        final List<String> lsUserEmail = new ArrayList<>();
        DatabaseReference listOfUsers =FirebaseDatabase.getInstance().getReference(Commonx.USER_INFORMATION);

        listOfUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapShot:dataSnapshot.getChildren()){
                    User user=userSnapShot.getValue(User.class);
                    lsUserEmail.add(user.getEmail());
                }
                fbLoadFinsih.onFirebaseLoadUserNameDone(lsUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                fbLoadFinsih.onFirebaseLoadFailed(databaseError.getMessage());

            }
        });
    }

    private void startSearch(String txt_search) {

        // TODO: Switch this with a partial guid to pair users instead of email
        // https://stackoverflow.com/questions/49071905/firebase-search-with-firebaserecycleradapter-firebase-ui-3-2-2/51902636
        // https://stackoverflow.com/questions/57030713/firebaserecycleradapter-in-firebaserecycleradapter-cannot-be-applied-to
        Query query = FirebaseDatabase.getInstance()
                .getReference(Commonx.USER_INFORMATION)
                .orderByChild("email")
                .startAt(txt_search);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query,User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder userVH, int i, @NonNull final User user) {
                if(user.getEmail().equals(Commonx.loggedUser.getEmail())){


                    if(user.getImage()!=null){
                        Picasso.get().load(Commonx.loggedUser.getImage()).into(userVH.recycler_profile_image);}
                    userVH.txt_user_email.setText(new StringBuilder(user.getEmail()).append(" (me)"));
                    userVH.txt_user_email.setTypeface(userVH.txt_user_email.getTypeface(), Typeface.ITALIC);
                }

                else {

                    if(user.getImage()!=null){
                        Picasso.get().load(user.getImage()).into(userVH.recycler_profile_image);}

                    userVH.txt_user_email.setText(new StringBuilder(user.getEmail()));
                }

                userVH.setiRecycItemListerner(new IRecycItemListerner() {
                    @Override
                    public void onItemClickListener(View view, int position) {
                        showDialogRequest(user);

                    }
                });
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.layout_user,parent,false);

                return new UserViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        rec_all_user.setAdapter(searchAdapter);

    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> lstEmail) { }

    @Override
    public void onFirebaseLoadFailed(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }
}
