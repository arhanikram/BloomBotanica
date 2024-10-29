package com.example.bloombotanica;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

// TODO: When database is implemented, add progress bars for plants with pending tasks
// and update the RecyclerView to prioritize plants with tasks at the top.
// - Use a field like `isTaskPending` in the database to track pending tasks.
// - Show a progress bar in each plant's card if tasks are pending.
// - Query the database to order plants by task completion status.

public class PlantsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plants, container, false);

        // Mock data for testing
        List<Plant> plantList = new ArrayList<>();
        plantList.add(new Plant("Aloe Vera"));
        plantList.add(new Plant("Snake Plant"));
        plantList.add(new Plant("Fiddle Leaf Fig"));
        plantList.add(new Plant("Aloe Vera"));
        plantList.add(new Plant("Snake Plant"));
        plantList.add(new Plant("Fiddle Leaf Fig"));
        plantList.add(new Plant("Aloe Vera"));
        plantList.add(new Plant("Snake Plant"));
        plantList.add(new Plant("Fiddle Leaf Fig"));

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerView.setAdapter(new PlantAdapter(plantList));

        return view;
    }
}