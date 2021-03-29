package com.example.aurorafitness;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public class TrackChangesFragment extends Fragment implements View.OnClickListener {

    //variables
    private TextView tvSettings, tvWeight;

    private EditText edWeight;

    private Button btnTracker, btnViewProgress;

    private Switch swSettings;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private TrackWeight trackWeight;

    private Snackbar storingSnack;

    private ArrayList<String> datesTracked;

    private static final double IMPERIAL_WEIGHT = 2.2046;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track_changes, container, false);

        getActivity().setTitle("Track Your Weight");

        //initialises the variables
        btnTracker = view.findViewById(R.id.btnTrackWeight);
        btnViewProgress = view.findViewById(R.id.btnViewProgress);

        btnTracker.setOnClickListener(this);
        btnViewProgress.setOnClickListener(this);

        trackWeight = new TrackWeight();

        datesTracked = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        tvSettings = view.findViewById(R.id.tvSettings);
        tvWeight = view.findViewById(R.id.tvWeight);

        edWeight = view.findViewById(R.id.edDailyWeight);

        swSettings = view.findViewById(R.id.switchMetric);

        //switch on check listener to format the weight based on the scale system
        swSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //checks if the scale system is imperial
                if (isChecked) {

                    tvSettings.setText("Imperial");
                    tvWeight.setText("Pounds");

                    if (!edWeight.getText().toString().equals("")) {

                        double imperialWeight = Double.parseDouble(edWeight.getText().toString()) * IMPERIAL_WEIGHT;

                        edWeight.setText(String.format(Locale.ENGLISH, "%.2f", imperialWeight));

                    }
                }

                //checks if the system is metric
                else {

                    tvSettings.setText("Metric");
                    tvWeight.setText("Kgs");

                    if (!edWeight.getText().toString().equals("")) {

                        double metricWeight = Double.parseDouble(edWeight.getText().toString()) / IMPERIAL_WEIGHT;

                        edWeight.setText(String.format(Locale.ENGLISH, "%.2f", metricWeight));

                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onClick(View view) {

        //handles the on clicks for each view
        if(view.getId() == R.id.btnTrackWeight){

            checkAndStoreWeight();

        }

        else if(view.getId() == R.id.btnViewProgress){

            startActivity(new Intent(getActivity(), ProgressActivity.class));

        }

    }

    public void checkAndStoreWeight(){

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(getActivity())){

            Snackbar.make(getActivity().findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        //checks if all fields are populated
        else if(edWeight.getText().toString().equals("")){

            Snackbar.make(getActivity().findViewById(android.R.id.content),"All fields are required",Snackbar.LENGTH_LONG).show();

        }

        else{

            storingSnack = Snackbar.make(getActivity().findViewById(android.R.id.content),"Storing your data",Snackbar.LENGTH_INDEFINITE);

            storingSnack.show();

            //firebase method to check and insert every weight change
            myRef.child(mAuth.getCurrentUser().getUid()).child("Weight Tracker").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    getValues();

                    if(dataSnapshot.exists()){

                        datesTracked.clear();

                        //stores the dates that a user has tracked their weight for
                        for(DataSnapshot ds : dataSnapshot.getChildren()){

                            datesTracked.add(ds.child("currentDate").getValue().toString());

                        }

                        //checks if a user has tracked their weight for the current day
                        if(datesTracked.contains(trackWeight.getCurrentDate())){

                            storingSnack.dismiss();

                            Snackbar.make(getActivity().findViewById(android.R.id.content),"You have already recorded a weight for today",Snackbar.LENGTH_LONG).show();

                        }

                        else{

                            //inserts the weight change into firebase if it has not been tracked for the current day
                            insertToFirebase();

                        }

                    }

                    //if no weight changes have been recorded then insert a weight change
                    else {

                        insertToFirebase();

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Failed to read value
                    Log.w("Retrieve Error", "Failed to read values", databaseError.toException());
                }
            });

        }

    }

    public void getValues(){

        //method to store the values for the weight a user has set
        if(swSettings.isChecked()){

            trackWeight.setWeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edWeight.getText().toString())) + " Pounds");

        }

        else{

            trackWeight.setWeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edWeight.getText().toString())) + " Kgs");

        }

        //gets the current date to be tracked for
        Date todayDate = Calendar.getInstance().getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String todayString = formatter.format(todayDate);

        trackWeight.setCurrentDate(todayString);

    }

    public void insertToFirebase(){

        //timer to delay the insert on fast internet connections so users can see the insert feedback
               new Timer().schedule(new TimerTask() {
                   @Override
                   public void run() {

                       //firebase method to insert a weight change into firebase database
                       myRef.child(mAuth.getCurrentUser().getUid()).child("Weight Tracker").child(UUID.randomUUID().toString()).setValue(trackWeight)
                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {

                                       if(task.isSuccessful()){

                                           storingSnack.dismiss();

                                           Snackbar.make(getActivity().findViewById(android.R.id.content),"Your data has been stored successfully",Snackbar.LENGTH_LONG).show();

                                       }

                                       else{

                                           storingSnack.dismiss();

                                           Snackbar.make(getActivity().findViewById(android.R.id.content),"Error storing your data",Snackbar.LENGTH_LONG).show();

                                       }

                                   }
                               });

                   }
               }, 2000);

    }

}
