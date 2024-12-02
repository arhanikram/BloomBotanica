package com.example.bloombotanica;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.bloombotanica.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
class SettingsFragmentInstrumentedTest {

    @Rule
    public ActivityScenarioRule<FragmentActivity> activityRule = new ActivityScenarioRule<>(FragmentActivity.class);

    @Test
    public void testClickOnAccountInfoLayout() {
        // Test that clicking on the Account Info layout shows the dialog
        onView(withId(R.id.account_info_layout)).perform(ViewActions.click());

        // Check if the dialog is shown (Check for name input field)
        onView(withId(R.id.name_edit_text)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testChangeThemeFromPopup() {
        // Test that clicking on the Appearance layout opens the theme popup
        onView(withId(R.id.appearance_layout)).perform(ViewActions.click());

        // Change theme to light mode via popup menu (simulate click on light mode)
        onView(withText("Light Mode")).perform(ViewActions.click());

        // Verify that the theme has changed to light mode
        assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void testNotificationPreferencesDialog() {
        // Test that clicking on the Notifications layout shows the notification preferences dialog
        onView(withId(R.id.notifications_layout)).perform(ViewActions.click());

        // Check that the push notifications toggle is visible
        onView(withId(R.id.push_notifications_toggle)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSaveNotificationPreferences() {
        // Test saving push notification preferences
        onView(withId(R.id.notifications_layout)).perform(ViewActions.click());

        // Toggle the switch (assume it's unchecked)
        onView(withId(R.id.push_notifications_toggle)).perform(ViewActions.click());

        // Simulate clicking the Save button
        onView(withText("Save")).perform(ViewActions.click());

        // Verify that the preference has been saved (using SharedPreferences)
 //       SharedPreferences preferences = activityRule.getScenario().getResult().getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Boolean preferences = null;
        boolean notificationsEnabled = preferences.getBoolean("push_notifications_enabled");
        assertTrue(notificationsEnabled);
    }
}
