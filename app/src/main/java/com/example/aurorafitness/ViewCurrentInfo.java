package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class ViewCurrentInfo extends AppCompatActivity {

    //variables
    private ListView lvCurrentInfo;
    private ArrayList<String> currentInfo;

    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_current_info);

        this.setTitle("Your Current Info");

        //initialises the variables
        lvCurrentInfo = findViewById(R.id.lvCurrentProgress);
        currentInfo = new ArrayList<>();

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(this)){

            Snackbar.make(findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        else {

            final Snackbar gettingInfoSnack = Snackbar.make(findViewById(android.R.id.content),"Getting your data", Snackbar.LENGTH_INDEFINITE);

            gettingInfoSnack.show();

            //timer to delay on fast internet connections
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    //firebase method to retrieve a user's current information
                    myRef.child(mAuth.getCurrentUser().getUid()).child("Current Info").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){

                                CurrentInfo currentInfoObj = dataSnapshot.getValue(CurrentInfo.class);

                                //populates array list with a user's current information
                                currentInfo.add("Current Weight " + currentInfoObj.getCurrentWeight());
                                currentInfo.add("Current Height " + currentInfoObj.getCurrentHeight());

                            }

                            else{

                                currentInfo.add("No data available please go back and capture some");

                            }

                            //sets an array adapter to be used by the list view
                            ArrayAdapter<String> currentInfoAdapter = new ArrayAdapter<>(ViewCurrentInfo.this,android.R.layout.simple_expandable_list_item_1,currentInfo);

                            lvCurrentInfo.setAdapter(currentInfoAdapter);

                            gettingInfoSnack.dismiss();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Failed to read value
                            Log.w("Retrieve Error", "Failed to read value.", databaseError.toException());

                        }
                    });

                }
            }, 2000);

        }



    }
}
