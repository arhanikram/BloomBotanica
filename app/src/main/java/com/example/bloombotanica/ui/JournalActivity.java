package com.example.bloombotanica.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;

import android.content.ClipData;
import android.view.DragEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.JournalEntryAdapter;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.dialogs.DeleteLogDialog;
import com.example.bloombotanica.models.JournalEntry;
import com.example.bloombotanica.database.JournalEntryDao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JournalActivity extends AppCompatActivity implements JournalEntryAdapter.onLogLongClickListener, DeleteLogDialog.DeleteLogListener {
    private static final int REQUEST_LOG_DETAIL = 1;
    private RecyclerView recyclerView;
    private int plantId;
    private JournalEntryAdapter jeAdapter;
    private UserPlantDatabase userPlantDatabase;
    private FloatingActionButton addLogFab;
    private JournalEntryDao journalEntryDao;
    private List<JournalEntry> entries;
    private ImageView trashBin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_journal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        userPlantDatabase = UserPlantDatabase.getInstance(this);
        journalEntryDao = userPlantDatabase.journalEntryDao();

        trashBin = findViewById(R.id.trash_bin);
        trashBin.setVisibility(View.GONE);

        Toolbar toolbar = findViewById(R.id.journal_toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        plantId = getIntent().getIntExtra("plantId", -1);
        recyclerView = findViewById(R.id.journal_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        jeAdapter = new JournalEntryAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(jeAdapter);
        addLogFab = findViewById(R.id.add_log_fab);


        addLogFab.setOnClickListener(v -> openNewLogDetail());

        loadJournalEntries();
    }

    @Override
    public void onLogLongClick(View view, int position) {
        showTrashBin();

        ClipData data = ClipData.newPlainText("", "");
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDragAndDrop(data, shadowBuilder, view, 0);

        trashBin.setOnDragListener((v, event) -> {
            if (event.getAction() == DragEvent.ACTION_DROP) {
                //delete log
                deleteLogDialog(position);
                trashBinHoverOff();
            } else if (event.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                hideTrashBin();
            } if (event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                trashBinHoverOn();
                vibrateTrashBin();
            } else if (event.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                trashBinHoverOff();
            }
            return true;
        });
    }

    private void deleteLogDialog(int position) {
        JournalEntry logToDelete = entries.get(position);
        DeleteLogDialog.showDeleteConfirmationDialog(this, userPlantDatabase, logToDelete, position, this);
    }

    @Override
    public void onDeleteComplete(int position) {
        // When the deletion is complete, remove the log from the list and update the RecyclerView
        entries.remove(position);
        jeAdapter.notifyItemRemoved(position);
        jeAdapter.notifyItemRangeChanged(position, entries.size());
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
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); // Get system vibrator service
        if (vibrator != null && vibrator.hasVibrator()) {
            // Vibrate for 50 milliseconds with the default amplitude
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

    private void openNewLogDetail() {
        JournalEntry newLog = new JournalEntry();
        newLog.setTitle("New Log");
        newLog.setBody("");
        Log.d("JournalActivity", "New log body: " + newLog.getBody());
        newLog.setTimestamp(new Date());
        newLog.setImagePaths("[]");

        new Thread(() -> {
            long insertedId = userPlantDatabase.journalEntryDao().insert(newLog);

            Log.d("JournalActivity", "New log inserted with ID: " + newLog.getId());
            runOnUiThread(() -> {
                Intent intent = new Intent(JournalActivity.this, LogDetailActivity.class);
                intent.putExtra("entryId", (int) insertedId);
                intent.putExtra("plantId", plantId);
                startActivityForResult(intent, REQUEST_LOG_DETAIL);
            });
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOG_DETAIL && resultCode == RESULT_OK) {
            // Get the entryId from the intent
            int updatedEntryId = data.getIntExtra("entryId", -1);
            if (updatedEntryId != -1) {
                // Fetch the updated journal entry and update the list
                new Thread(() -> {
                    JournalEntry updatedEntry = journalEntryDao.getEntryById(updatedEntryId);
                    runOnUiThread(() -> {
                        if (updatedEntry != null) {
                            // Add the new entry to the list and update the RecyclerView
//                            entries.add(updatedEntry);
                            jeAdapter.notifyItemInserted(entries.size() - 1);
                        }
                    });
                }).start();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJournalEntries();
    }

    private void loadJournalEntries() {
        new Thread(() -> {
            entries = userPlantDatabase.journalEntryDao().getEntriesForPlant(plantId);
            runOnUiThread(() -> jeAdapter.updateEntries(entries));
        }).start();
    }
}