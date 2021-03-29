package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aurorafitness.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    //variables
    private EditText edEmail,edPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        auth = FirebaseAuth.getInstance();

        //checks if the user is logged in and redirects them to the home page
        if(auth.getCurrentUser() != null){

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.rgb(53, 92, 125));

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);

    }

    public void login_click(View view){

        String email,password;

        //gets the email and password a user sets
        email = edEmail.getText().toString().trim();
        password = edPassword.getText().toString().trim();

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(this)){

            Snackbar.make(findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        else{

            final Snackbar loginSnack = Snackbar.make(findViewById(android.R.id.content),"Logging you in...",Snackbar.LENGTH_INDEFINITE);

            loginSnack.show();

            //firebase method to login a user
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                        loginSnack.dismiss();

                    }

                    else{


                        loginSnack.dismiss();

                        Snackbar.make(findViewById(android.R.id.content),"Login failed",Snackbar.LENGTH_LONG).show();

                    }

                }
            });
        }

    }

    public void goToReg(View view){

        startActivity(new Intent(this, RegisterActivity.class));

    }
}
