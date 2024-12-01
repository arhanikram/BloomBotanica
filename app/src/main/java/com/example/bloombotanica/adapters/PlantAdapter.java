
package com.example.bloombotanica.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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

        // Check if the user has uploaded a custom image
        if (userPlant.getImagePath() != null && !userPlant.getImagePath().isEmpty()) {
            // Try to load the custom image from the saved path
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(userPlant.getImagePath());
                if (bitmap != null) {
                    holder.plantImageView.setImageBitmap(bitmap);
                } else {
                    // Fallback to default image if custom image can't be loaded
                    setDefaultPlantImage(holder, userPlant);
                }
            } catch (Exception e) {
                Log.e("PlantAdapter", "Error loading custom image", e);
                // Fallback to default image if there's an error
                setDefaultPlantImage(holder, userPlant);
            }
        } else {
            // No custom image, use default
            setDefaultPlantImage(holder, userPlant);
        }



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

    private void setDefaultPlantImage(PlantViewHolder holder, UserPlant userPlant) {
        // Set plant image dynamically using plant ID
        String imageResourceName = "a" + userPlant.getPlantCareId();  // Example: "a1", "a2", etc.
        int imageResId = holder.itemView.getContext().getResources()
                .getIdentifier(imageResourceName, "drawable", holder.itemView.getContext().getPackageName());

        if (imageResId != 0) {
            holder.plantImageView.setImageResource(imageResId);
        } else {
            // Fallback to a default plant image if no specific image is found
            holder.plantImageView.setImageResource(R.drawable.default_plant_image);
        }
    }

    @Override
    public int getItemCount() {
        return userPlantList.size();
    }

    public void updatePlants(List<UserPlant> newPlants) {
        this.userPlantList = newPlants;
        notifyDataSetChanged();
    }


    public static class PlantViewHolder extends RecyclerView.ViewHolder {
        TextView plantName;
        TextView plantCommonName;
        ImageView plantImageView;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            plantName = itemView.findViewById(R.id.card_plant_name);
            plantCommonName = itemView.findViewById(R.id.plant_common_name);
            plantImageView = itemView.findViewById(R.id.plant_image);
        }
    }
}
