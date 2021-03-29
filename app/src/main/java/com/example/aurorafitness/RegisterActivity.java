package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    //variables
    private EditText edUsername, edPassword, edConfirmPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edUsername = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();
        getWindow().setStatusBarColor(Color.rgb(53, 92, 125));
    }

    public void registerUser(View view){

        String email, password, confirmPassword;

        //sets the values required for user registration
        email = edUsername.getText().toString().trim();
        password = edPassword.getText().toString().trim();
        confirmPassword = edConfirmPassword.getText().toString().trim();

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(this)){

            Snackbar.make(findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        //checks if all fields are populated
        else if(email.equals("") || password.equals("") || confirmPassword.equals("")){

            Snackbar.make(findViewById(android.R.id.content),"All fields are required",Snackbar.LENGTH_LONG).show();

        }

        //checks the password length
        else if(password.length() < 6){

            Snackbar.make(findViewById(android.R.id.content),"Password must be at least 6 characters long",Snackbar.LENGTH_LONG).show();

        }

        //checks if the email is a valid email address
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            Snackbar.make(findViewById(android.R.id.content),"Please enter a valid email address",Snackbar.LENGTH_LONG).show();

        }

        //checks if the password and confirmed password is equal
        else if(!password.equals(confirmPassword)) {

            Snackbar.make(findViewById(android.R.id.content),"Passwords don't match",Snackbar.LENGTH_LONG).show();

        }

        else {

            final Snackbar creatingUserSnack = Snackbar.make(findViewById(android.R.id.content),"Creating user...",Snackbar.LENGTH_INDEFINITE);

            creatingUserSnack.show();

            //firebase method to register a new user
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                mAuth.signOut();

                                creatingUserSnack.dismiss();

                                Snackbar.make(findViewById(android.R.id.content),"User created successfully",Snackbar.LENGTH_LONG).show();

                            }

                            else{

                                Snackbar.make(findViewById(android.R.id.content),"Error creating user",Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    public void goToLoginAct(View view){

        startActivity(new Intent(this, LoginActivity.class));
    }
}
