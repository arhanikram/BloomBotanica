package com.example.bloombotanica.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;

import java.util.List;

public class PlantGalleryAdapter extends RecyclerView.Adapter<PlantGalleryAdapter.PlantGalleryViewHolder> {

    private Context context;
    private List<Integer> plantImages;  // A list of drawable resource IDs

    // Constructor to initialize context and the plant images list
    public PlantGalleryAdapter(Context context, List<Integer> plantImages) {
        this.context = context;
        this.plantImages = plantImages;
    }

    @NonNull
    @Override
    public PlantGalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate a new view for each image in the gallery
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant_gallery, parent, false);
        return new PlantGalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantGalleryViewHolder holder, int position) {
        // Set the image for the ImageView in the ViewHolder
        holder.imageView.setImageResource(plantImages.get(position));
    }

    @Override
    public int getItemCount() {
        return plantImages != null ? plantImages.size() : 0;  // Check for null to avoid crashes
    }

    // ViewHolder class that holds the reference to the ImageView
    public static class PlantGalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PlantGalleryViewHolder(View itemView) {
            super(itemView);
            // Initialize ImageView from itemView
            imageView = itemView.findViewById(R.id.plant_image);  // Make sure to match the ID in XML
        }
    }
}
