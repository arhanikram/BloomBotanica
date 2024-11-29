package com.example.bloombotanica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.UserPlant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private final List<UserPlant> userPlants;
    private final OnPlantLongClickListener longClickListener;
    private final OnItemClickListener itemClickListener;  // Added item click listener

    public PlantAdapter(List<UserPlant> userPlants, OnPlantLongClickListener longClickListener, OnItemClickListener itemClickListener) {
        this.userPlants = userPlants;
        this.longClickListener = longClickListener;
        this.itemClickListener = itemClickListener;  // Initialize item click listener
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        UserPlant userPlant = userPlants.get(position);
        holder.plantName.setText(userPlant.getNickname());
        holder.plantImage.setImageResource(R.drawable.bloombotanicalogo); // Placeholder image

        // Handle long click
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onPlantLongClick(v, position);
            return true;
        });

        // Handle regular click
        holder.itemView.setOnClickListener(v -> {
            itemClickListener.onItemClick(position); // Pass position on click
        });
    }

    @Override
    public int getItemCount() {
        return userPlants.size();
    }

    // Interface for long click listener
    public interface OnPlantLongClickListener {
        void onPlantLongClick(View view, int position);
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        public ImageView plantImage;
        public TextView plantName;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantImage = itemView.findViewById(R.id.plant_image);
            plantName = itemView.findViewById(R.id.plant_name);
        }
    }
}
