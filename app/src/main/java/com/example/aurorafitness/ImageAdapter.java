package com.example.aurorafitness;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.aurorafitness.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    //variables
    private ArrayList<String> imageUrls;
    private Context context;

    //constructor that sets the array and context for the image adapter
    public ImageAdapter(ArrayList<String> images, Context _context){

        imageUrls = images;
        context = _context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //renders an image view into the recycler view for every image in the array
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_layout, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        //loads every image into the image views that are rendered
        Glide
                .with(context)
                .load(imageUrls.get(position))
                .thumbnail(Glide.with(context).load(R.drawable.loading_image))
                .into(holder.galleryImages);

        //sets the scale type for each image
        holder.galleryImages.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //sets an on click listener to view a full view of each image
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ImageActivity.class);

                intent.putExtra("id", position);

                intent.putExtra("array", imageUrls);

                context.startActivity(intent);

            }
        });

    }

    //returns the number of image urls in the array
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView galleryImages;

        public ViewHolder(View view){

            super(view);

            galleryImages = view.findViewById(R.id.galleryImg);

        }

    }

}
