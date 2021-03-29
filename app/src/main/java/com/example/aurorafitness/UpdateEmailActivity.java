package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UpdateEmailActivity extends AppCompatActivity {

    //variables
    private EditText edUsername, edPassword, edNewEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);

        this.setTitle("Change Your Email");

        //initialises the variables
        edUsername = findViewById(R.id.edOldEmail);
        edPassword = findViewById(R.id.edCurrentPassword);
        edNewEmail = findViewById(R.id.edNewEmail);
    }

    public void updateEmail(View view){

        //checks if there is an internet connection
        if(!CheckInternet.isNetworkAvailable(this)){

            Snackbar.make(findViewById(android.R.id.content),"No Internet",Snackbar.LENGTH_LONG).show();

        }

        //checks to see if the emails is valid
        else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(edUsername.getText().toString().trim()).matches() ||
                !android.util.Patterns.EMAIL_ADDRESS.matcher(edNewEmail.getText().toString().trim()).matches()){

            Snackbar.make(findViewById(android.R.id.content),"Please enter a valid email address",Snackbar.LENGTH_LONG).show();

        }

        else{

            //gets the current user that is logged in
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            //sets a credential for registering a new email
            AuthCredential credential = EmailAuthProvider.getCredential(edUsername.getText().toString().trim(), edPassword.getText().toString().trim());

            final Snackbar emailUpdateSnack = Snackbar.make(findViewById(android.R.id.content),"Updating email...",Snackbar.LENGTH_INDEFINITE);

            emailUpdateSnack.show();

            //firebase method to validate a user's credentials and register the user's new email address
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                //firebase method to update a user's email if their credentials are valid
                                user.updateEmail(edNewEmail.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        emailUpdateSnack.dismiss();

                                        Snackbar.make(findViewById(android.R.id.content),"Email Updated",Snackbar.LENGTH_LONG).show();


                                    }
                                });

                            }

                            else{

                                emailUpdateSnack.dismiss();

                                Snackbar.make(findViewById(android.R.id.content),"You have provided invalid credentials",Snackbar.LENGTH_LONG).show();

                            }

                        }
                    });
        }

    }
}
