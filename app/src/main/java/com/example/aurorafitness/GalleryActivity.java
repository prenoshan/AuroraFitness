package com.example.aurorafitness;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GalleryActivity extends AppCompatActivity {

    //variables used
    private DatabaseReference myRef;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;

    private FloatingActionMenu fab;
    private RecyclerView gallery;

    private TextView galleryEmpty;

    private ArrayList<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        //initialises the variables
        galleryEmpty = findViewById(R.id.emptyGallery);
        fab = findViewById(R.id.fab);
        gallery = findViewById(R.id.gallery);

        this.setTitle("Your Fitness Gallery");

        myRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference(mAuth.getCurrentUser().getUid());

        imageUrls = new ArrayList<>();

        //sets on scroll listener to hide and show the floating action button when scrolling
        gallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if(dy > 0){

                    fab.hideMenu(true);

                }

                else if(dy < 0){

                    fab.showMenu(true);

                }

            }
        });

        Snackbar.make(findViewById(android.R.id.content), "Please wait while we load your images...", Snackbar.LENGTH_SHORT).show();

        myRef.child(mAuth.getCurrentUser().getUid()).child("Images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                imageUrls.clear();

                //gets all image urls from firebase for a user
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    imageUrls.add(String.valueOf(ds.getValue()));

                }

                final ImageAdapter imageAdapter = new ImageAdapter(imageUrls,GalleryActivity.this);

                //sets the layout to be used by the recycler view
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
                gallery.setLayoutManager(layoutManager);

                //sets the image adapter to be used by the recycler view
                gallery.setAdapter(imageAdapter);

                //displays no images found if there are no images
                if(imageAdapter.getItemCount() == 0){

                    galleryEmpty.setVisibility(View.VISIBLE);

                }

                else if(imageAdapter.getItemCount() > 0){

                    galleryEmpty.setVisibility(View.GONE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.d("retrieval error", databaseError.getMessage());

            }
        });


    }

    public void openGallery(View view){

        //intent to pick picture from gallery
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);

    }

    public void openCamera(View view){

        //intent to pick picture from camera
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        startActivityForResult(takePicture, 0);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        //creates a unique filename for the image to be stored in firebase storage
        final String fileName = UUID.randomUUID().toString();

        //to upload image from user's camera
        if(requestCode == 0 && resultCode == RESULT_OK){

            //gets image from camera
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            if(photo != null){

                //convert bitmap to byte array
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                byte[] photoBytes = baos.toByteArray();

                uploadCameraImageToFirebase(photoBytes, fileName);

            }

            else{

                Toast.makeText(this, "Error getting image to upload", Toast.LENGTH_SHORT).show();

            }


        }

        //to upload image from user's image gallery
        else if (requestCode == 1 && resultCode == RESULT_OK) {

            final Uri image = data.getData();

            //gets image to upload
            if(data.getData() != null){

                uploadGalleryImageToFirebase(image, fileName);

            }

            else{

                Toast.makeText(this, "Error getting image to upload", Toast.LENGTH_SHORT).show();

            }

        }

    }

    public void uploadCameraImageToFirebase(byte[] data, final String fileName){

        //shows a dialog for the progress of the file being uploaded
        final ProgressDialog uploadingDialog = new ProgressDialog(this);
        uploadingDialog.setTitle("Uploading");
        uploadingDialog.show();

        //storage reference inserts image into storage
        storageReference.child(fileName).putBytes(data).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                //gets the progress of the file being uploaded
                double uploadProgress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                uploadingDialog.setMessage("Uploading " + ((int) uploadProgress) + "%");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //storage reference used to get the download url of the image inserted and pushes it to firebase real-time database
                storageReference.child(fileName).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {

                            myRef.child(mAuth.getCurrentUser().getUid()).child("Images").push().setValue(task.getResult().toString());

                        }

                    }
                });

                uploadingDialog.dismiss();
            }
        });

    }

    public void uploadGalleryImageToFirebase(Uri imageUrl, final String fileName){

        final ProgressDialog uploadingDialog = new ProgressDialog(this);
        uploadingDialog.setTitle("Uploading");
        uploadingDialog.show();

        //storage reference inserts image into storage
        storageReference.child(fileName).putFile(imageUrl).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                double uploadProgress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                uploadingDialog.setMessage("Uploading " + ((int) uploadProgress) + "%");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //storage reference used to get the download url of the image inserted and pushes it to firebase real-time database
                storageReference.child(fileName).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()) {

                            myRef.child(mAuth.getCurrentUser().getUid()).child("Images").push().setValue(task.getResult().toString());

                        }

                    }
                });

                uploadingDialog.dismiss();
            }
        });

    }

}
