package com.example.bloombotanica.adapters;

import static com.example.bloombotanica.dialogs.DeleteImageDialog.showDeleteConfirmationDialog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bloombotanica.R;
import com.example.bloombotanica.ui.FullscreenImageActivity;
import com.example.bloombotanica.ui.LogDetailActivity;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imagePaths;
    private Context context;
    private OnImageDeleteListener onImageDeleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public ImageAdapter(Context context, List<String> imagePaths, OnImageDeleteListener onImageDeleteListener) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.onImageDeleteListener = onImageDeleteListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        // Check if imagePath is valid
        if (imagePath != null && !imagePath.isEmpty()) {
            // Load the image using Glide
            Glide.with(context)
                    .load(imagePath)
                    .into(holder.imageView);
        } else {
            // Handle invalid or empty image paths
            holder.imageView.setImageResource(R.drawable.baseline_photo_camera_24); // Default placeholder image
            Toast.makeText(context, "Image missing!", Toast.LENGTH_SHORT).show();
        }

        holder.imageView.setOnClickListener(v -> {
           Intent intent = new Intent(context, FullscreenImageActivity.class);
           intent.putStringArrayListExtra("imagePaths", new ArrayList<>(imagePaths));
           intent.putExtra("position", position);
           context.startActivity(intent);
        });

        holder.imageView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(context, position, (LogDetailActivity) context);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_preview);
        }
    }

    public void updateImagePaths(List<String> newImagePaths) {
        imagePaths.clear();
        imagePaths.addAll(newImagePaths);
        notifyDataSetChanged();
    }
}
