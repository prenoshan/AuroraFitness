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

public class CurrentInfoFragment extends Fragment implements View.OnClickListener {

    //constants
    private static final double IMPERIAL_WEIGHT = 2.2046;
    private static final double IMPERIAL_HEIGHT = 3.28;

    //variables
    private TextView tvSettings,tvWeight,tvHeight,tvViewCurrentInfo;
    private EditText edWeight,edHeight;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;

    private CurrentInfo currentInfo;

    private Switch swSettings;

    private Button btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_info, container, false);

        getActivity().setTitle("Physical Information");

        //initialising the variables
        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        btnSave = view.findViewById(R.id.btnTrackWeight);

        tvSettings = view.findViewById(R.id.tvSettings);
        tvWeight = view.findViewById(R.id.tvWeight);
        tvHeight = view.findViewById(R.id.tvHeight);
        tvViewCurrentInfo = view.findViewById(R.id.tvViewTargetInfo);

        tvViewCurrentInfo.setOnClickListener(this);

        edWeight = view.findViewById(R.id.edDailyWeight);
        edHeight = view.findViewById(R.id.edTargetSteps);

        currentInfo = new CurrentInfo();

        swSettings = view.findViewById(R.id.switchMetric);

        swSettings.setChecked(false);

        tvSettings.setText("Metric");

        //on check listener to change the input for weight and height based on the scale system
        swSettings.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                //if the scale system is imperial
                if(isChecked){

                    tvSettings.setText("Imperial");
                    tvWeight.setText("Pounds");
                    tvHeight.setText("Feet");

                    if(!edWeight.getText().toString().equals("") && !edHeight.getText().toString().equals("")){

                        //gets the weight and height the user enters
                        double imperialWeight = Double.parseDouble(edWeight.getText().toString()) * IMPERIAL_WEIGHT;
                        double imperialHeight = Double.parseDouble(edHeight.getText().toString()) * IMPERIAL_HEIGHT;

                        //formats the values to two decimals
                        edWeight.setText(String.format(Locale.ENGLISH,"%.2f", imperialWeight));
                        edHeight.setText(String.format(Locale.ENGLISH, "%.2f", imperialHeight));

                    }
                }

                //if the scale system is metric
                else {

                    tvSettings.setText("Metric");
                    tvWeight.setText("Kgs");
                    tvHeight.setText("Metres");

                    if (!edWeight.getText().toString().equals("") && !edHeight.getText().toString().equals("")) {

                        //gets the weight and height the user enters
                        double metricWeight = Double.parseDouble(edWeight.getText().toString()) / IMPERIAL_WEIGHT;
                        double metricHeight = Double.parseDouble(edHeight.getText().toString()) / IMPERIAL_HEIGHT;

                        //formats the values to two decimals
                        edWeight.setText(String.format(Locale.ENGLISH, "%.2f", metricWeight));
                        edHeight.setText(String.format(Locale.ENGLISH,"%.2f", metricHeight));

                    }
                }
            }
        });

        btnSave.setOnClickListener(this);

        return view;
    }


    public void getValues(){

        //method that sets the values for weight and height to be uploaded into firebase
        if(swSettings.isChecked()){

            //formats the values for weight and height to two decimal places
            currentInfo.setCurrentWeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edWeight.getText().toString())) + " Pounds");
            currentInfo.setCurrentHeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edHeight.getText().toString())) + " Feet");

        }

        else{

            currentInfo.setCurrentWeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edWeight.getText().toString())) + " Kgs");
            currentInfo.setCurrentHeight(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble(edHeight.getText().toString())) + " Metres");

        }

    }

    public void insertToFirebase(){

        //checks if there is an available internet connection
        if(!CheckInternet.isNetworkAvailable(getActivity())){

            Snackbar.make(getActivity().findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        //checks if all fields are populated
        else if(edWeight.getText().toString().equals("") || edHeight.getText().toString().equals("")){

            Snackbar.make(getActivity().findViewById(android.R.id.content),"All fields are required",Snackbar.LENGTH_LONG).show();

        }

        else{

            final Snackbar storingSnack = Snackbar.make(getActivity().findViewById(android.R.id.content),"Storing your data",Snackbar.LENGTH_INDEFINITE);

            storingSnack.show();

            //gets the values to be stored
            getValues();

            //firebase async method to set the current info for a user
            myRef.child(mAuth.getCurrentUser().getUid()).child("Current Info").setValue(currentInfo)
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

        //handles onclicks for each view
        if(view.getId() == R.id.btnTrackWeight){

            insertToFirebase();

        }

        else if(view.getId() == R.id.tvViewTargetInfo){


            startActivity(new Intent(getActivity(), ViewCurrentInfo.class));

        }

    }



}
