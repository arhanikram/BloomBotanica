
package com.example.bloombotanica.adapters;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.models.PlantCare;
import com.example.bloombotanica.models.UserPlant;

import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {
    private List<UserPlant> userPlantList;
    private OnPlantLongClickListener longClickListener;
    private OnItemClickListener itemClickListener;
    private PlantCareDatabase plantcaredat;
    private String plantCommonName;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public interface OnPlantLongClickListener {
        void onPlantLongClick(View view, int position);
    }

    public PlantAdapter(List<UserPlant> userPlantList, PlantCareDatabase plantcaredat, OnPlantLongClickListener longClickListener) {
        this.userPlantList = userPlantList;
        this.longClickListener = longClickListener;
        this.plantcaredat = plantcaredat;

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
        new Thread(() -> {
            PlantCare plantCare = plantcaredat.plantCareDao().getPlantCareById(userPlant.getPlantCareId());
            new Handler(Looper.getMainLooper()).post(() -> {
                holder.plantCommonName.setText(plantCare.getCommonName());
            });

        }).start();

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
        TextView plantCommonName;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.card_plant_name);
            plantCommonName = itemView.findViewById(R.id.plant_common_name);
        }
    }
}
