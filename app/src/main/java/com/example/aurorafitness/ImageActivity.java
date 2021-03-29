package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {

    //variables used
    private ImageView imageView;
    private DatabaseReference dbref;
    private FirebaseAuth mAuth;

    private ArrayList<String> urls;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        this.setTitle("Your Selected Image");

        //initialises variables
        dbref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        imageView = findViewById(R.id.imageView);

        //gets the position and url of the image in the image array from the gallery activity
        Intent intent = getIntent();

        pos = intent.getExtras().getInt("id");

        urls = intent.getExtras().getStringArrayList("array");

        //glide library loads the image into the imageview
        Glide.with(this)
                .load(Uri.parse(urls.get(pos)))
                .into(imageView);
    }

    public void deleteImage(View view){

        //delete method from firebase that gets the image loaded and deletes it from the database
        dbref.child(mAuth.getCurrentUser().getUid()).child("Images").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){

                    //deletes the image from the database
                    for(DataSnapshot ds: dataSnapshot.getChildren()){

                        if(ds.getValue().equals(urls.get(pos))){

                            ds.getRef().removeValue();
                        }
                    }

                    //deletes the image from firebase storage
                    StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(urls.get(pos));

                    imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Log.d("DeleteImage", "Deleted Successfully");

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Log.d("DeleteImage","Couldn't delete image from storage");

                        }
                    });

                    finish();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
