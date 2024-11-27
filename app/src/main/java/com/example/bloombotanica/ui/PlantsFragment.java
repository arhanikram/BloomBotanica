package com.example.bloombotanica.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.VibrationEffect;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;
import android.content.Context;

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

// TODO: When database is implemented, add progress bars for plants with pending tasks
// and update the RecyclerView to prioritize plants with tasks at the top.
// - Use a field like `isTaskPending` in the database to track pending tasks.
// - Show a progress bar in each plant's card if tasks are pending.
// - Query the database to order plants by task completion status.

public class PlantsFragment extends Fragment implements PlantAdapter.OnPlantLongClickListener, DeletePlantDialog.DeletePlantListener, AddPlantDialogFragment.OnPlantAddedListener {

    private UserPlantDatabase userPlantDatabase;
    private PlantAdapter plantAdapter;
    private List<UserPlant> userPlantList = new ArrayList<>();
    private FloatingActionButton addPlantFab;
    private View dimOverlay;
    private ImageView trashBin;
    private BottomNavigationView bottomNavigationView;
    private TextView dateText;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plants, container, false);

        //initialize user plant database
        userPlantDatabase = UserPlantDatabase.getInstance(requireContext());

        //Set Date
        dateText = view.findViewById(R.id.date_text);
        setDate();

        //set up trash bin view
        trashBin = view.findViewById(R.id.trash_bin);
        trashBin.setVisibility(View.GONE);

        //find dim overlay
       // dimOverlay = requireActivity().findViewById(R.id.dim_overlay);

        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);

        // Set up RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        plantAdapter = new PlantAdapter(userPlantList, this);
        recyclerView.setAdapter(plantAdapter);

        loadUserPlants();

        // Set up FloatingActionButton
        addPlantFab = view.findViewById(R.id.addPlantFab);
        addPlantFab.setOnClickListener(v -> {
           showAddPlantDialog();
        });

        plantAdapter.setOnItemClickListener((position) -> {
            UserPlant selectedPlant = userPlantList.get(position);
            Intent intent = new Intent(getContext(), UserPlantProfileActivity.class);
            intent.putExtra("userPlantId", selectedPlant.getId());
            startActivity(intent);
        });

        return view;
    }


    @SuppressLint("NotifyDataSetChanged")
    void loadUserPlants() {
        new Thread(() -> {
            List<UserPlant> userPlants = userPlantDatabase.userPlantDao().getAllUserPlants();

            int initialSize = userPlantList.size();
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

        //start drag and drop
        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDragAndDrop(data, shadowBuilder, view, 0);

        //handle drop on trash bin
        trashBin.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                showDeleteConfirmationDialog(position);

            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                hideTrashBin();
                hideDimOverlay();
                showBnv(); //hide bottom nav view

            } if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
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
        //animate trashbin entrance
        trashBin.setVisibility(View.VISIBLE);
        trashBin.setAlpha(0f);
        trashBin.animate().alpha(1f).setDuration(300).start();
    }
    private void hideTrashBin() {
        //animate trashbin exit
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
    }

    private void setDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE\nMMMM d", Locale.getDefault());
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        dateText.setText(currentDate);
    }

    @Override
    public void onPlantAdded(UserPlant newPlant) {
        userPlantList.add(newPlant);
        plantAdapter.notifyItemInserted(userPlantList.size() - 1);
        loadUserPlants();
        Log.d("PlantsFragment", "onPlantAdded: New plant added to RecyclerView");
    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserPlants(); // Reload the list from the database to reflect any changes
    }


}