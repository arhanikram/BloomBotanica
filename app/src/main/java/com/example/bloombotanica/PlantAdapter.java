package com.example.bloombotanica;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private List<Plant> plantList;

    public PlantAdapter(List<Plant> plantList) {
        this.plantList = plantList;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        holder.plantName.setText(plant.getName());
        holder.statusIndicator.setImageResource(plant.getStatusIcon());
    }

    @Override
    public int getItemCount() {
        return plantList.size();
    }

    public void addPlant(Plant plant) {
        plantList.add(plant);
        notifyDataSetChanged();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        TextView plantName;
        ImageView statusIndicator;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.plant_name);
            statusIndicator = itemView.findViewById(R.id.status_indicator);
        }
    }
}

