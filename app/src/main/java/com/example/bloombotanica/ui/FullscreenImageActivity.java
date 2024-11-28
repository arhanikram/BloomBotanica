package com.example.bloombotanica.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bloombotanica.R;
import com.example.bloombotanica.adapters.FullscreenImageAdapter;

import java.util.List;

public class FullscreenImageActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private FullscreenImageAdapter fullscreenImageAdapter;
    private List<String> imagePaths;
    private int initialPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fullscreen_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewPager = findViewById(R.id.view_pager);

        // Get the image paths and initial position from the intent
        imagePaths = getIntent().getStringArrayListExtra("imagePaths");
        initialPosition = getIntent().getIntExtra("position", 0);

        // Initialize the adapter with the image paths
        fullscreenImageAdapter = new FullscreenImageAdapter(this, imagePaths);
        viewPager.setAdapter(fullscreenImageAdapter);

        // Set the initial position of the ViewPager
        viewPager.setCurrentItem(initialPosition, false);
    }
}