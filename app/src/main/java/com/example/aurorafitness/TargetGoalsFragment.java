package com.example.aurorafitness;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class TargetGoalsFragment extends Fragment implements View.OnClickListener {

    //variables
    private EditText edWeight, edSteps;

    private Button btnSave;

    private TextView tvSettings, tvWeight, tvViewTargetGoals;

    private Switch swSettings;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;

    private TargetGoals targetGoals;

    private static final double IMPERIAL_WEIGHT = 2.2046;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_target_goals, container, false);

        getActivity().setTitle("Set Your Goals");

        //initialises variables
        mAuth = FirebaseAuth.getInstance();
        myRef = FirebaseDatabase.getInstance().getReference();

        targetGoals = new TargetGoals();

        tvSettings = view.findViewById(R.id.tvSettings);
        tvWeight = view.findViewById(R.id.tvWeight);
        tvViewTargetGoals = view.findViewById(R.id.tvViewTargetInfo);

        tvViewTargetGoals.setOnClickListener(this);

        edWeight = view.findViewById(R.id.edDailyWeight);
        edSteps = view.findViewById(R.id.edTargetSteps);

        btnSave = view.findViewById(R.id.btnTrackWeight);

        btnSave.setOnClickListener(this);

        swSettings = view.findViewById(R.id.switchMetric);

        //changes the target weight based on the scale system
        swSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {

                    tvSettings.setText("Imperial");
                    tvWeight.setText("Pounds");

                    if (!edWeight.getText().toString().equals("")) {

                        double imperialWeight = Double.parseDouble(edWeight.getText().toString()) * IMPERIAL_WEIGHT;

                        //formats the weight to two decimal places
                        edWeight.setText(String.format(Locale.ENGLISH, "%.2f", imperialWeight));

                    }
                } else {

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

    public void getValues(){

        //gets the values for the weight and steps to upload to firebase
        if(swSettings.isChecked()){

            targetGoals.setTargetWeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edWeight.getText().toString())) + " Pounds");

        }

        else{

            targetGoals.setTargetWeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edWeight.getText().toString())) + " Kgs");

        }

        targetGoals.setTargetSteps(Integer.parseInt(edSteps.getText().toString()));

    }

    public void insertToFirebase(){

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(getActivity())){

            Snackbar.make(getActivity().findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        //checks if all fields are populated
        else if(edWeight.getText().toString().equals("") || edSteps.getText().toString().equals("")){

            Snackbar.make(getActivity().findViewById(android.R.id.content),"All fields are required",Snackbar.LENGTH_LONG).show();

        }

        else{

            final Snackbar storingSnack = Snackbar.make(getActivity().findViewById(android.R.id.content),"Storing your data",Snackbar.LENGTH_INDEFINITE);

            storingSnack.show();

            getValues();

            //firebase method to upload the target weight and steps
            myRef.child(mAuth.getCurrentUser().getUid()).child("Target Goals").setValue(targetGoals)
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
    }

    @Override
    public void onClick(View view) {

        //handles the on clicks for each view
        if(view.getId() == R.id.btnTrackWeight){

            insertToFirebase();

        }

        else if(view.getId() == R.id.tvViewTargetInfo){

            startActivity(new Intent(getActivity(), ViewTargetInfo.class));

        }

    }
}
