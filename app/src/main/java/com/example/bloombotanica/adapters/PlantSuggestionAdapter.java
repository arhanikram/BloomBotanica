package com.example.bloombotanica.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.models.PlantCare;

import java.util.ArrayList;
import java.util.List;

public class PlantSuggestionAdapter extends RecyclerView.Adapter<PlantSuggestionAdapter.SuggestionViewHolder> {

    private List<PlantCare> suggestions = new ArrayList<>();
    private final SuggestionClickListener listener;
    private String query;

    public interface SuggestionClickListener {
        void onSuggestionClick(String plantName);
    }

    public PlantSuggestionAdapter(SuggestionClickListener listener) {
        this.listener = listener;
    }

    public void updateSuggestions(List<PlantCare> newSuggestions, String query) {
        if (newSuggestions != null) {
            suggestions = newSuggestions;
        } else {
            suggestions.clear();
        }
        this.query = query;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant_suggestions, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        // Call getCommonOrScientificName with query to determine the correct name
        String plantName = suggestions.get(position).getCommonOrScientificName(query);
        holder.plantNameTextView.setText(plantName);
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(plantName));
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView plantNameTextView;

        SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            plantNameTextView = itemView.findViewById(R.id.plant_suggestion_name);
        }
    }
}
