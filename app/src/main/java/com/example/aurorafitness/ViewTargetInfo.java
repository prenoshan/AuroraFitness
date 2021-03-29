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

public class ViewTargetInfo extends AppCompatActivity {

    //variables
    private ListView lvTargetGoals;
    private ArrayList<String> targetGoals;

    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_target_info);

        this.setTitle("Your Target Goals");

        //initialises the variables
        lvTargetGoals = findViewById(R.id.lvTargetGoals);
        targetGoals = new ArrayList<>();

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //checks if there is an internet connection
        if (!CheckInternet.isNetworkAvailable(this)) {

            Snackbar.make(findViewById(android.R.id.content), "No Internet", Snackbar.LENGTH_LONG).show();

        } else {

            final Snackbar gettingInfoSnack = Snackbar.make(findViewById(android.R.id.content), "Getting your data", Snackbar.LENGTH_INDEFINITE);

            gettingInfoSnack.show();

            //timer to delay read on fast internet connections
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    //firebase method to read all the target goals for a user
                    myRef.child(mAuth.getCurrentUser().getUid()).child("Target Goals").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                TargetGoals targetGoalsObj = dataSnapshot.getValue(TargetGoals.class);

                                //populates the array list with the user's target goals
                                targetGoals.add("Target Weight " + targetGoalsObj.getTargetWeight());
                                targetGoals.add("Target Steps " + targetGoalsObj.getTargetSteps());

                            } else {

                                targetGoals.add("No data available please go back and capture some");

                            }

                            //sets the array adapter to be used by the list view
                            ArrayAdapter<String> currentInfoAdapter = new ArrayAdapter<>(ViewTargetInfo.this, android.R.layout.simple_expandable_list_item_1, targetGoals);

                            lvTargetGoals.setAdapter(currentInfoAdapter);

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
