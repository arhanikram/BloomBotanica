package com.example.bloombotanica;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.bloombotanica.ui.JournalActivity;
import com.example.bloombotanica.ui.UserPlantProfileActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.Matchers.not;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
public class UserPlantProfileActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<UserPlantProfileActivity> activityRule = new ActivityScenarioRule<>(UserPlantProfileActivity.class);

    @Before
    public void setup() {
        // Any setup code (e.g., initializing mock data or components) can go here.
    }

    // 1. Test the "Water" Button and Check for Toast
    @Test
    public void testWaterButtonClick() {
        // Simulate clicking the "water_button" in the UserPlantProfileActivity
        onView(withId(R.id.water_button)).perform(click());

        // Verify that a Toast message appears, indicating the plant has been watered
        onView(withText("Plant watered successfully!"))
     //           .inRoot(withDecorView(not(ViewMatchers.isRoot(activityRule.getScenario().onActivity().getWindow().getDecorView()))))
                .check(matches(ViewMatchers.isDisplayed()));
    }

    // 2. Test the "Turn" Button and Check for Toast
    @Test
    public void testTurnButtonClick() {
        // Simulate clicking the "rotate_button" in the UserPlantProfileActivity
        onView(withId(R.id.rotate_button)).perform(click());

        // Verify that a Toast message appears, indicating the plant has been turned
        onView(withText("Plant turned successfully!"))
       //         .inRoot(withDecorView(not(ViewMatchers.isRoot(activityRule.getScenario().onActivity().getWindow().getDecorView()))))
                .check(matches(ViewMatchers.isDisplayed()));
    }

    // 3. Test Clicking on the Plant Image and Verify Image Display
    @Test
    public void testPlantImageClick() {
        // Simulate clicking on the plant image in the UserPlantProfileActivity
        onView(withId(R.id.user_plant_profile_img)).perform(click());

        // Verify that the plant image is displayed (or some other logic you expect to happen)
        onView(withId(R.id.user_plant_profile_img)).check(matches(ViewMatchers.isDisplayed()));
    }

    // 4. Test Clicking the "Journal" Button to Verify Activity Launch
    @Test
    public void testJournalButtonClick() {
        // Simulate clicking the "journal_button" in the UserPlantProfileActivity
        onView(withId(R.id.journal_button)).perform(click());

        // Verify that the JournalActivity is launched
        intended(hasComponent(JournalActivity.class.getName()));
    }
}
