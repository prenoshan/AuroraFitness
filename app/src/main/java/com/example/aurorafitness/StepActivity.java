package com.example.aurorafitness;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class StepActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    //variables
    private TextView tvSteps;
    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Steps you have taken: ";
    private int numSteps;
    private String  currentSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step);

        this.setTitle("Step Counter");

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        tvSteps = findViewById(R.id.tv_steps);

        SharedPreferences sp = getSharedPreferences("prefs",Activity.MODE_PRIVATE);

        currentSteps = sp.getString("steps", "0");

        tvSteps.setText(currentSteps);

    }

    //onclick event that registers the step counter to start counting steps
    public void startCounting(View view){

        sensorManager.registerListener(StepActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

    }

    //onclick event that unregisters the step counter
    public void stopCounting(View view){

        sensorManager.unregisterListener(StepActivity.this);

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void step(long timeNs) {
        numSteps++;
        tvSteps.setText(TEXT_NUM_STEPS + numSteps);

        SharedPreferences sp = getSharedPreferences("prefs",Activity.MODE_PRIVATE);

        SharedPreferences.Editor editor = sp.edit();

        editor.putString("steps", tvSteps.getText().toString());

        editor.apply();

    }

}
