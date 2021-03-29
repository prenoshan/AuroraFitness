package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private EditText edPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        this.setTitle("Your Profile");

        edPassword = findViewById(R.id.edCurrentPassword);

    }

    public void changeUserCredentials(View view){

        //checks if there is an internet connection
        if (!CheckInternet.isNetworkAvailable(this)) {

            Snackbar.make(findViewById(android.R.id.content), "No Internet", Snackbar.LENGTH_LONG).show();

        }

        //checks if the password is not null
        else if(edPassword.getText().toString().equals("")){

            Snackbar.make(findViewById(android.R.id.content),"Choose either to reset your email or password",Snackbar.LENGTH_LONG).show();

        }

        else{

            final String password = edPassword.getText().toString().trim();

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(!password.equals("")){

                final Snackbar updatePassSnack = Snackbar.make(findViewById(android.R.id.content),"Updating password...",Snackbar.LENGTH_INDEFINITE);

                updatePassSnack.show();

                //firebase method to reset a users password
                user.updatePassword(password)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()){

                                    updatePassSnack.dismiss();

                                    Snackbar.make(findViewById(android.R.id.content),"Password updated",Snackbar.LENGTH_LONG).show();

                                }

                                else{

                                    Snackbar.make(findViewById(android.R.id.content),"Couldn't update password",Snackbar.LENGTH_LONG).show();

                                }

                            }
                        });

            }

        }

    }

    public void viewCurrentInfo(View view){

        startActivity(new Intent(this, ViewCurrentInfo.class));

    }

    public void viewTargetInfo(View view){

        startActivity(new Intent(this, ViewTargetInfo.class));

    }

    public void updateEmail(View view){

        startActivity(new Intent(this, UpdateEmailActivity.class));

    }



}
