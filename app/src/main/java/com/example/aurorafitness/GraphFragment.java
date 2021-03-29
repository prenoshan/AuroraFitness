package com.example.aurorafitness;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class GraphFragment extends Fragment {

    //constants
    private static final double IMPERIAL_WEIGHT = 2.2046;

    //variables
    private TextView tvSetting;
    private BarChart currentLevelsChart;
    private DatabaseReference myRef;
    private ProgressBar progBar;
    private String currentUser;
    private String scaleSystemWeight, scaleSystemTargetWeight;
    private Switch switchSystem;
    private BarDataSet cwdataSet, twdataSet, csdataSet, tsdataSet;
    private BarData barData;
    private ArrayList<BarEntry> cWeight = new ArrayList<>();
    private ArrayList<BarEntry> tarWeight = new ArrayList<>();
    private ArrayList<BarEntry> cSteps = new ArrayList<>();
    private ArrayList<BarEntry> tarSteps = new ArrayList<>();
    private String targetWeight, targetSteps, currentWeight;

    public class MyDecimalValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return super.getFormattedValue(value);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        getActivity().setTitle("Graph Of Goals");

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        myRef = FirebaseDatabase.getInstance().getReference().child(currentUser);

        currentLevelsChart = view.findViewById(R.id.currentLevels);

        progBar = view.findViewById(R.id.progressBar);

        switchSystem = view.findViewById(R.id.switchSystem);

        tvSetting = view.findViewById(R.id.tvSystemSetting);

        tvSetting.setVisibility(View.GONE);

        switchSystem.setVisibility(View.GONE);

        currentLevelsChart.setNoDataText(null);

        progBar.setVisibility(View.VISIBLE);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                targetWeight = dataSnapshot.child("Target Goals").child("targetWeight").getValue().toString();

                targetSteps = dataSnapshot.child("Target Goals").child("targetSteps").getValue().toString();

                currentWeight = dataSnapshot.child("Current Info").child("currentWeight").getValue().toString();

                if(currentWeight.contains(" ")){

                    scaleSystemWeight = currentWeight.substring(currentWeight.lastIndexOf(" ") + 1);

                }

                if(targetWeight.contains(" ")){

                    scaleSystemTargetWeight = targetWeight.substring(targetWeight.lastIndexOf(" ") + 1);

                }

                if(scaleSystemWeight.equals("Pounds")){

                    Double weight = Double.valueOf(String.format(Locale.ENGLISH,"%.2f",Double.valueOf(currentWeight.substring(0, currentWeight.indexOf(" "))) / IMPERIAL_WEIGHT));

                    cWeight.add(new BarEntry(0f, Float.valueOf(weight.toString())));

                }

                else{

                    cWeight.add(new BarEntry(0f, Float.valueOf(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(currentWeight.substring(0, currentWeight.indexOf(" ")))))));
                }

                if(scaleSystemTargetWeight.equals("Pounds")){

                    Double target = Double.valueOf(String.format(Locale.ENGLISH,"%.2f",Double.valueOf(targetWeight.substring(0, targetWeight.indexOf(" "))) / IMPERIAL_WEIGHT));

                    tarWeight.add(new BarEntry(1f,Float.valueOf(target.toString())));

                }

                else {

                    tarWeight.add(new BarEntry(1f,Float.valueOf(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(targetWeight.substring(0, targetWeight.indexOf(" ")))))));

                }

                cSteps.add(new BarEntry(2f,Float.parseFloat("20")));
                tarSteps.add(new BarEntry(3f,Float.parseFloat(targetSteps)));

                cwdataSet = new BarDataSet(cWeight, "Current Weight (kgs)");
                twdataSet = new BarDataSet(tarWeight, "Target Weight (kgs)");

                csdataSet = new BarDataSet(cSteps, "Current Steps");
                tsdataSet = new BarDataSet(tarSteps, "Target Steps");

                cwdataSet.setValueTextSize(12f);
                cwdataSet.setColors(ColorTemplate.rgb("#29b6f6"));

                twdataSet.setValueTextSize(12f);
                twdataSet.setColors(ColorTemplate.rgb("#ff1744"));

                csdataSet.setValueTextSize(12f);
                csdataSet.setColors(ColorTemplate.rgb("#9c27b0"));

                tsdataSet.setValueTextSize(12f);
                tsdataSet.setColors(ColorTemplate.rgb("#00e676"));


                barData = new BarData(cwdataSet,twdataSet,csdataSet,tsdataSet);

                barData.setValueFormatter(new MyDecimalValueFormatter());

                barData.setBarWidth(0.8f);

                currentLevelsChart.setExtraOffsets(0,0,0,0);
                currentLevelsChart.getXAxis().setEnabled(false);
                currentLevelsChart.getLegend().setEnabled(true);
                currentLevelsChart.getLegend().setTextSize(18f);
                currentLevelsChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                currentLevelsChart.getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
                currentLevelsChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
                currentLevelsChart.setHighlightPerDragEnabled(false);
                currentLevelsChart.setHighlightPerTapEnabled(false);
                currentLevelsChart.setDescription(null);
                currentLevelsChart.getAxisLeft().setEnabled(false);
                currentLevelsChart.getAxisRight().setEnabled(false);

                currentLevelsChart.setData(barData);
                currentLevelsChart.invalidate();

                progBar.setVisibility(View.GONE);

                tvSetting.setVisibility(View.VISIBLE);
                switchSystem.setVisibility(View.VISIBLE);

                switchSystem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        //if the scale system is imperial
                        if(isChecked){

                            barData.clearValues();

                            cwdataSet.clear();
                            csdataSet.clear();
                            twdataSet.clear();
                            tsdataSet.clear();

                            cSteps.clear();
                            cWeight.clear();
                            tarWeight.clear();
                            tarSteps.clear();

                            tvSetting.setText("Pounds");

                            if(currentWeight.contains(" ")){

                                scaleSystemWeight = currentWeight.substring(currentWeight.lastIndexOf(" ") + 1);

                            }

                            if(targetWeight.contains(" ")){

                                scaleSystemTargetWeight = targetWeight.substring(targetWeight.lastIndexOf(" ") + 1);

                            }

                            if(scaleSystemWeight.equals("Pounds")){

                                Double weight = Double.valueOf(String.format(Locale.ENGLISH,"%.2f",Double.valueOf(currentWeight.substring(0, currentWeight.indexOf(" ")))));

                                cWeight.add(new BarEntry(0f, Float.valueOf(weight.toString())));

                            }

                            else{

                                cWeight.add(new BarEntry(0f, Float.valueOf(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(currentWeight.substring(0, currentWeight.indexOf(" "))) * IMPERIAL_WEIGHT))));
                            }

                            if(scaleSystemTargetWeight.equals("Pounds")){

                                Double target = Double.valueOf(String.format(Locale.ENGLISH,"%.2f",Double.valueOf(targetWeight.substring(0, targetWeight.indexOf(" ")))));

                                tarWeight.add(new BarEntry(1f,Float.valueOf(target.toString())));

                            }

                            else {

                                tarWeight.add(new BarEntry(1f,Float.valueOf(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(targetWeight.substring(0, targetWeight.indexOf(" "))) * IMPERIAL_WEIGHT))));

                            }

                            cwdataSet = new BarDataSet(cWeight, "Current Weight (Pounds)");
                            twdataSet = new BarDataSet(tarWeight, "Target Weight (Pounds)");

                            cSteps.add(new BarEntry(2f,Float.parseFloat("20")));
                            tarSteps.add(new BarEntry(3f,Float.parseFloat(targetSteps)));

                            cwdataSet.setValueTextSize(12f);
                            cwdataSet.setColors(ColorTemplate.rgb("#29b6f6"));

                            twdataSet.setValueTextSize(12f);
                            twdataSet.setColors(ColorTemplate.rgb("#ff1744"));

                            csdataSet.setValueTextSize(12f);
                            csdataSet.setColors(ColorTemplate.rgb("#9c27b0"));

                            tsdataSet.setValueTextSize(12f);
                            tsdataSet.setColors(ColorTemplate.rgb("#00e676"));

                            barData = new BarData(cwdataSet,twdataSet,csdataSet,tsdataSet);

                            barData.setBarWidth(0.8f);

                            barData.setValueFormatter(new MyDecimalValueFormatter());

                            currentLevelsChart.setData(barData);

                            currentLevelsChart.invalidate();

                        }

                        //if the scale system is metric
                        else {

                            barData.clearValues();

                            cwdataSet.clear();
                            csdataSet.clear();
                            twdataSet.clear();
                            tsdataSet.clear();

                            cSteps.clear();
                            cWeight.clear();
                            tarWeight.clear();
                            tarSteps.clear();

                            tvSetting.setText("Kgs");

                            if(currentWeight.contains(" ")){

                                scaleSystemWeight = currentWeight.substring(currentWeight.lastIndexOf(" ") + 1);

                            }

                            if(targetWeight.contains(" ")){

                                scaleSystemTargetWeight = targetWeight.substring(targetWeight.lastIndexOf(" ") + 1);

                            }

                            if(scaleSystemWeight.equals("Pounds")){

                                Double weight = Double.valueOf(String.format(Locale.ENGLISH,"%.2f",Double.valueOf(currentWeight.substring(0, currentWeight.indexOf(" "))) / IMPERIAL_WEIGHT));

                                cWeight.add(new BarEntry(0f, Float.valueOf(weight.toString())));

                            }

                            else{

                                cWeight.add(new BarEntry(0f, Float.valueOf(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(currentWeight.substring(0, currentWeight.indexOf(" ")))))));

                            }

                            if(scaleSystemTargetWeight.equals("Pounds")){

                                Double target = Double.valueOf(String.format(Locale.ENGLISH,"%.2f",Double.valueOf(targetWeight.substring(0, targetWeight.indexOf(" "))) / IMPERIAL_WEIGHT));

                                tarWeight.add(new BarEntry(1f,Float.valueOf(target.toString())));

                            }

                            else {

                                tarWeight.add(new BarEntry(1f,Float.valueOf(String.format(Locale.ENGLISH, "%.2f", Double.valueOf(targetWeight.substring(0, targetWeight.indexOf(" ")))))));

                            }

                            cSteps.add(new BarEntry(2f,Float.parseFloat("20")));
                            tarSteps.add(new BarEntry(3f,Float.parseFloat(targetSteps)));

                            cwdataSet = new BarDataSet(cWeight, "Current Weight (kgs)");
                            twdataSet = new BarDataSet(tarWeight, "Target Weight (kgs)");

                            cwdataSet.setValueTextSize(12f);
                            cwdataSet.setColors(ColorTemplate.rgb("#29b6f6"));

                            twdataSet.setValueTextSize(12f);
                            twdataSet.setColors(ColorTemplate.rgb("#ff1744"));

                            csdataSet.setValueTextSize(12f);
                            csdataSet.setColors(ColorTemplate.rgb("#9c27b0"));

                            tsdataSet.setValueTextSize(12f);
                            tsdataSet.setColors(ColorTemplate.rgb("#00e676"));

                            barData = new BarData(cwdataSet,twdataSet,csdataSet,tsdataSet);

                            barData.setBarWidth(0.8f);

                            barData.setValueFormatter(new MyDecimalValueFormatter());

                            currentLevelsChart.setData(barData);

                            currentLevelsChart.invalidate();

                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DatabaseReadError", "Failed to read values", error.toException());
            }
        });

        return view;
    }
}

