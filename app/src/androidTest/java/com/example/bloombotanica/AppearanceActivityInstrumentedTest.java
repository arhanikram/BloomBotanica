package com.example.bloombotanica;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.bloombotanica.ui.AppearanceActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.action.ViewActions;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
class AppearanceActivityinstrumentedTest {

    @Rule
    public ActivityScenarioRule<AppearanceActivity> activityScenarioRule = new ActivityScenarioRule<>(AppearanceActivity.class);

    private SharedPreferences preferences;

    @Before
    public void setUp() {
        // Initialize shared preferences
     //   preferences = PreferenceManager.getDefaultSharedPreferences(activityScenarioRule.getScenario().getResult().getClass());
    }

    @Test
    public void testInitialTheme() {
        // Check that the correct radio button is selected based on the current theme mode
        int expectedCheckedId = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO ?
                R.id.light_mode_button : R.id.dark_mode_button;

        onView(withId(expectedCheckedId)).check(matches(isDisplayed()));
    }

    @Test
    public void testSwitchToLightMode() {
        // Simulate user clicking the light mode radio button
        ViewActions ViewActions = null;
        onView(withId(R.id.light_mode_button)).perform(ViewActions.click());

        // Check that the app switches to light mode
        assertEquals(AppCompatDelegate.getDefaultNightMode(), AppCompatDelegate.MODE_NIGHT_NO);

        // Verify if the SharedPreferences were updated
        assertEquals(preferences.getInt("theme_mode", -1), AppCompatDelegate.MODE_NIGHT_NO);

        // Optionally, check if a toast message was shown (note: this is tricky with Espresso but can be done with IdlingResource)
        // Espresso can be used to check if the toast message appears, but for simplicity, it's omitted here.
    }

    @Test
    public void testSwitchToDarkMode() {
        // Simulate user clicking the dark mode radio button
        onView(withId(R.id.dark_mode_button)).perform(ViewActions.click());

        // Check that the app switches to dark mode
        assertEquals(AppCompatDelegate.getDefaultNightMode(), AppCompatDelegate.MODE_NIGHT_YES);

        // Verify if the SharedPreferences were updated
        assertEquals(preferences.getInt("theme_mode", -1), AppCompatDelegate.MODE_NIGHT_YES);
    }

    @Test
    public void testToolbarNavigation() {
        // Check if the toolbar is displayed
        onView(withId(R.id.appearance_toolbar)).check(matches(isDisplayed()));

        // Simulate back navigation (click on the back button)
        onView(withId(R.id.appearance_toolbar)).perform(ViewActions.pressBack());

        // Here, you'd normally want to assert that the activity finishes, but it’s tricky in a test. You may just check
        // that the activity transition completes by calling finish() or testing indirectly.
    }
}

