package com.example.bloombotanica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.UserPlant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<UserPlant> userPlantList;
    private OnPlantLongClickListener longClickListener;
    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnPlantLongClickListener {
        void onPlantLongClick(View view, int position);
    }

    public PlantAdapter(List<UserPlant> userPlantList, OnPlantLongClickListener longClickListener) {
        this.userPlantList = userPlantList;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.plant_item, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        UserPlant userPlant = userPlantList.get(position);
        holder.plantName.setText(userPlant.getNickname());

        //dynamic height calculation to make card square
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        int screenWidth = holder.itemView.getContext().getResources().getDisplayMetrics().widthPixels;
        int numColumns = 2;
        int itemSpacing = 16;

        int itemWidth = (screenWidth - (numColumns + 1) * itemSpacing) / numColumns;
        layoutParams.height = (int) (itemWidth * 0.80);
        holder.itemView.setLayoutParams(layoutParams);

        //set up long click listener for drag and drop
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onPlantLongClick(v, position);
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        });


    }

    @Override
    public int getItemCount() {
        return userPlantList.size();
    }

    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        TextView plantName;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.card_plant_name);
        }
    }
}
