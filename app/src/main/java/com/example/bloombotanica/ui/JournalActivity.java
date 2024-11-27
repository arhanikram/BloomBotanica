package com.example.bloombotanica.ui;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.JournalEntryAdapter;
import com.example.bloombotanica.database.UserPlantDatabase;
import com.example.bloombotanica.models.JournalEntry;

import java.util.ArrayList;
import java.util.List;

public class JournalActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private int plantId;
    private JournalEntryAdapter jeAdapter;
    private UserPlantDatabase userPlantDatabase;

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
        jeAdapter = new JournalEntryAdapter(new ArrayList<>());
        recyclerView.setAdapter(jeAdapter);

        userPlantDatabase = UserPlantDatabase.getInstance(this);

        loadJournalEntries();
    }

    private void loadJournalEntries() {
        new Thread(() -> {
            List<JournalEntry> entries = userPlantDatabase.journalEntryDao().getEntriesForPlant(plantId);
            runOnUiThread(() -> jeAdapter.updateEntries(entries));
        }).start();
    }
}