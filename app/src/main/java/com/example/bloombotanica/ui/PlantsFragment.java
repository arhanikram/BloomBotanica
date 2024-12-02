package com.example.bloombotanica.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.database.PlantCareDatabase;
import com.example.bloombotanica.dialogs.AddPlantDialogFragment;
import com.example.bloombotanica.adapters.PlantAdapter;
import com.example.bloombotanica.R;
import com.example.bloombotanica.models.UserPlant;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.dialogs.DeletePlantDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PlantsFragment extends Fragment implements PlantAdapter.OnPlantLongClickListener, DeletePlantDialog.DeletePlantListener, AddPlantDialogFragment.OnPlantAddedListener {

    private UserPlantDatabase userPlantDatabase;
    private PlantAdapter plantAdapter;
    private List<UserPlant> userPlantList = new ArrayList<>();
    private FloatingActionButton addPlantFab;
    private View dimOverlay;
    private ImageView trashBin;
    private BottomNavigationView bottomNavigationView;
    private TextView dateText;
    private PlantCareDatabase plantCareDatabase;
    private EditText plantSearchfield;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plants, container, false);

        // Initialize user plant database
        userPlantDatabase = UserPlantDatabase.getInstance(requireContext());
        plantCareDatabase = PlantCareDatabase.getInstance(requireContext());

        // Set up trash bin view
        trashBin = view.findViewById(R.id.trash_bin);
        trashBin.setVisibility(View.GONE);

        // Find dim overlay
        dimOverlay = requireActivity().findViewById(R.id.dim_overlay);


        // Set up Bottom Navigation View
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);

// Set up RecyclerView with horizontal scroll
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);

        plantAdapter = new PlantAdapter(userPlantList, plantCareDatabase, this);
        // Pass the OnItemClickListener directly when initializing the adapter
        plantAdapter.setOnItemClickListener((position) -> {
            UserPlant selectedPlant = userPlantList.get(position);
            Intent intent = new Intent(getContext(), UserPlantProfileActivity.class);
            intent.putExtra("userPlantId", selectedPlant.getId());
            startActivity(intent);

        });

        recyclerView.setAdapter(plantAdapter);

        loadUserPlants();

        // Set up Floating Action Button
        addPlantFab = view.findViewById(R.id.addPlantFab);
        addPlantFab.setOnClickListener(v -> {
            showAddPlantDialog();
        });

        plantSearchfield = view.findViewById(R.id.plantSearchfield);
        plantSearchfield.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterPlants(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void filterPlants(String string) {

        if (string.isEmpty()) {
            plantAdapter.updatePlants(userPlantList);
            return;
        }

        List<UserPlant> filteredList = new ArrayList<>();

        for (UserPlant plant : userPlantList) {
            if (plant.getNickname().toLowerCase().contains(string.toLowerCase())) {
                filteredList.add(plant);
            }
        }

        plantAdapter.updatePlants(filteredList);
    }

    @SuppressLint("NotifyDataSetChanged")
    void loadUserPlants() {
        new Thread(() -> {
            List<UserPlant> userPlants = userPlantDatabase.userPlantDao().getAllUserPlants();

            // Update the UI on the main thread
            requireActivity().runOnUiThread(() -> {
                userPlantList.clear();
                userPlantList.addAll(userPlants);
                plantAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void showAddPlantDialog() {
        AddPlantDialogFragment addPlantDialog = new AddPlantDialogFragment();
        addPlantDialog.setTargetFragment(this, 0); // Set this fragment as the target
        addPlantDialog.show(getParentFragmentManager(), "AddPlantDialog");
        Log.d("PlantsFragment", "showAddPlantDialog called");
    }

    public void addPlant(UserPlant newPlant) {
        userPlantList.add(newPlant);
        plantAdapter.notifyItemInserted(userPlantList.size() - 1);
        Log.d("PlantsFragment", "addPlant called");
    }

    @Override
    public void onPlantLongClick(View view, int position) {
        showTrashBin();
        showDimOverlay();
        hideBnv();

        // Start drag-and-drop operation
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDragAndDrop(data, shadowBuilder, view, 0);

        // Handle drop on trash bin
        trashBin.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                //delete plant
                showDeleteConfirmationDialog(position);
                trashBinHoverOff();
            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                hideTrashBin();
                hideDimOverlay();
                showBnv(); // Show bottom navigation view after drag operation ends

            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                trashBinHoverOn();
                vibrateTrashBin();

            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                trashBinHoverOff();
            }
            return true;
        });
    }

    private void trashBinHoverOff() {
        trashBin.clearColorFilter();
        trashBin.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(100)
                .start();
    }

    private void vibrateTrashBin() {
        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            VibrationEffect effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.vibrate(effect);
        }
    }

    private void trashBinHoverOn() {
        trashBin.setColorFilter(Color.RED);
        trashBin.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(100)
                .start();
    }

    private void showBnv() {
        bottomNavigationView.setVisibility(View.VISIBLE);
    }

    private void hideBnv() {
        bottomNavigationView.setVisibility(View.INVISIBLE);
    }

    private void showDimOverlay() {
        dimOverlay.setVisibility(View.VISIBLE);
    }

    private void hideDimOverlay() {
        dimOverlay.setVisibility(View.INVISIBLE);
    }

    private void showTrashBin() {
        // Animate trash bin entrance
        trashBin.setVisibility(View.VISIBLE);
        trashBin.setAlpha(0f);
        trashBin.animate().alpha(1f).setDuration(300).start();
    }

    private void hideTrashBin() {
        // Animate trash bin exit
        trashBin.setAlpha(1f);
        trashBin.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            trashBin.setVisibility(View.GONE);
        }).start();
    }

    private void showDeleteConfirmationDialog(int position) {
        UserPlant plantToDelete = userPlantList.get(position);
        DeletePlantDialog.showDeleteConfirmationDialog(requireContext(), userPlantDatabase, plantToDelete, position, this);
    }

    @Override
    public void onDeleteComplete(int position) {
        userPlantList.remove(position);
        plantAdapter.notifyItemRemoved(position);
        plantAdapter.notifyItemRangeChanged(position, userPlantList.size());

//        loadUserPlants();
    }

    private void setDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE\nMMMM d", Locale.getDefault());
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        dateText.setText(currentDate);
    }

    @Override
    public void onPlantAdded(UserPlant newPlant) {
        userPlantList.add(newPlant);

        // Check if search is active and either update filtered or full list accordingly
        if (plantSearchfield.getText().toString().isEmpty()) {
            // If no search query, show the full list
            plantAdapter.updatePlants(userPlantList);
        } else {
            // Otherwise, apply the filter
            filterPlants(plantSearchfield.getText().toString());
        }

        RecyclerView recyclerView = getView().findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        if (layoutManager != null) {
            // Create a custom smooth scroller
            LinearSmoothScroller smoothScroller = new LinearSmoothScroller(requireContext()) {
                @Override
                protected int getVerticalSnapPreference() {
                    return SNAP_TO_END;  // Scroll to the end of the item
                }

                @Override
                public float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                    // Adjust the speed of the scroll
                    return 150f / (int) displayMetrics.densityDpi;  // Modify the speed if needed
                }
            };

            // Scroll to the last item
            smoothScroller.setTargetPosition(userPlantList.size() - 1);
            layoutManager.startSmoothScroll(smoothScroller);
        }
        Log.d("PlantsFragment", "onPlantAdded: New plant added to RecyclerView");
        loadUserPlants();
    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserPlants(); // Reload the list from the database to reflect any changes
    }
}
