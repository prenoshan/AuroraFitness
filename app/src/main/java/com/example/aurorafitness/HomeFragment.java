package com.example.aurorafitness;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    //variables
    TextView tvWelcomeUser;

    FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getActivity().setTitle("Home");

        tvWelcomeUser = view.findViewById(R.id.tvWelcomeOne);

        mAuth = FirebaseAuth.getInstance();

        //sets the username for the user from their email
        String username = mAuth.getCurrentUser().getEmail();

        int usernameIndex = username.indexOf('@');

        //gets all characters before the '@' sign in the email
        String userToDisplay = username.substring(0, usernameIndex);

        //capatalises the first letter of the users username
        String captaliseUsername = userToDisplay.substring(0, 1).toUpperCase() + userToDisplay.substring(1);

        tvWelcomeUser.setText("Welcome " + captaliseUsername);

        return view;
    }

}
