package com.example.bloombotanica;

import android.widget.TextView;
import android.widget.ProgressBar;

import com.example.bloombotanica.ui.UserPlantProfileActivity;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UserPlantProfileActivityUnitTest {

    @Mock
    private TextView plantNickname;

    @Mock
    private TextView plantName;

    @Mock
    private TextView plantSciName;

    @Mock
    private TextView plantDescription;

    @Mock
    private ProgressBar turnProgress;

    private UserPlantProfileActivity activity;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // Create the activity instance (using Robolectric for simulating activity behavior)
        activity = new UserPlantProfileActivity();

        // Inject the mocks into the activity
        activity.plantNickname = plantNickname;
        activity.plantName = plantName;
        activity.plantSciName = plantSciName;
        activity.turnProgress = turnProgress;
    }

    // Test 1: Update plant information
    @Test
    public void testUpdatePlantInfo() {
        // Create mock data for the plant
        //UserPlant userPlant = new UserPlant(1, "Cactus", "Cactaceae", "A cactus plant");

        // Call the method to update plant info
      //  activity.updatePlantInfo(userPlant);

        // Verify that the TextViews are updated with the correct data
        verify(plantNickname).setText("Cactus");
        verify(plantName).setText("Cactaceae");
        verify(plantSciName).setText("Cactaceae");
        verify(plantDescription).setText("A cactus plant");
    }

    // Test 2: Mark plant as turned
    @Test
    public void testMarkPlantAsTurned() {
        // Create a mock plant with a specific last turned date
      //  UserPlant userPlant = new UserPlant(1, "Cactus", "Cactaceae", "A cactus plant");
        Date lastTurnedDate = new Date(); // Use current date
   //     userPlant.setLastTurned(lastTurnedDate);

        // Call the method to mark the plant as turned
     //   activity.markPlantAsTurned(userPlant);

        // Verify that the progress bar is updated
        verify(turnProgress).setProgress(anyInt());
    }

    // Test 3: Calculate days since last turned
    @Test
    public void testCalculateDaysSinceLastTurned() {
        // Create a calendar object to simulate the last turned date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3); // Plant was turned 3 days ago
        Date lastTurned = calendar.getTime();

       // UserPlant userPlant = new UserPlant(1, "Cactus", "Cactaceae", "A cactus plant");
      //  userPlant.setLastTurned(lastTurned);

        // Call the method to calculate days since last turned
    //    int days = activity.calculateDaysSinceLastTurned(userPlant.getLastTurned());

        // Verify that the method correctly calculates the days (3 days ago)
     //   assertEquals(3, days);
    }

    // Test 4: Update turn progress
    @Test
    public void testUpdateTurnProgress() {
        // Simulate a plant with a last turned date of 1 day ago
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1); // Plant last turned 1 day ago
        Date lastTurned = calendar.getTime();

     //   UserPlant userPlant = new UserPlant(1, "Cactus", "Cactaceae", "A cactus plant");
     //   userPlant.setLastTurned(lastTurned);

        // Simulate next turning date (1 day ahead)
        calendar.add(Calendar.DAY_OF_YEAR, 1);
      //  userPlant.setNextTurningDate(calendar.getTime());

        // Call the method to update the turn progress
      //  activity.updateTurnProgress(userPlant);

        // Verify that the progress bar is updated with the correct progress
        verify(turnProgress).setProgress(anyInt());
    }

    // Test 5: Task overdue status
    @Test
    public void testTaskOverdueStatus() {
        // Create a mock overdue task
    //    Task overdueTask = new Task("Water", true); // Task is overdue

        // Verify that the task is overdue
    //    assertTrue(overdueTask.isOverdue());

        // Create a mock task that is not overdue
    //    Task notOverdueTask = new Task("Rotate", false);
     //   assertFalse(notOverdueTask.isOverdue());
    }
}
