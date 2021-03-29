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

public class ProgressActivity extends AppCompatActivity {

    private ListView lvProgress;
    private ArrayList<String> progress;

    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        lvProgress = findViewById(R.id.lvCurrentProgress);

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        this.setTitle("Your Progress");

        progress = new ArrayList<>();

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(this)){

            Snackbar.make(findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        else {

            final Snackbar progressSnack = Snackbar.make(findViewById(android.R.id.content),"Getting your data", Snackbar.LENGTH_INDEFINITE);

            progressSnack.show();

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    //gets the weight changes that the user has recorded
                    myRef.child(mAuth.getCurrentUser().getUid()).child("Weight Tracker").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if(dataSnapshot.exists()){

                                for(DataSnapshot ds : dataSnapshot.getChildren()){

                                    TrackWeight trackWeight = ds.getValue(TrackWeight.class);

                                    String weightProgress = "";

                                    weightProgress = "Weight: " + trackWeight.getWeight() + ", Date recorded on: " + trackWeight.getCurrentDate();

                                    progress.add(weightProgress);

                                }

                            }

                            else{

                                progress.add("You have not tracked your weight yet");

                            }

                            //sets the array adapter for the listview
                            ArrayAdapter<String> progressAdapter = new ArrayAdapter<>(ProgressActivity.this,android.R.layout.simple_expandable_list_item_1,progress);

                            lvProgress.setAdapter(progressAdapter);

                            progressSnack.dismiss();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Failed to read value
                            Log.w("DB_ReadError", "Failed to read value.", databaseError.toException());

                        }
                    });

                }
            }, 2000);

        }

    }
}
