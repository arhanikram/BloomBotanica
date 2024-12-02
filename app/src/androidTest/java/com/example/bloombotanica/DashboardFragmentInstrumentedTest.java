package com.example.bloombotanica;

import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.bloombotanica.R;
import com.example.bloombotanica.ui.DashboardFragment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.firebase.crashlytics.buildtools.reloc.com.google.common.base.CharMatcher.is;
import static org.junit.Assert.assertEquals;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.matches;

import java.util.ResourceBundle;

@RunWith(AndroidJUnit4.class)
public class DashboardFragmentInstrumentedTest {

    @Rule
    public ActivityScenarioRule<FragmentActivity> activityRule = new ActivityScenarioRule<>(FragmentActivity.class);

    @Test
    public void testUpdateStatusButton() {
        // Click on the "Update Status" button
        onView(withId(R.id.mark_as_done_button)).perform(ViewActions.click());

        // Verify that the status update Toast is shown
   //     onView(withText("Status updated to: Status updated!")).inRoot(withDecorView(not(is(activityRule.getScenario().getResult().getClass().getDecorView())))).check(matches(isDisplayed()));
    }

    @Test
    public void testSaveStatusToSharedPreferences() {
        // Perform button click to update status
        onView(withId(R.id.mark_as_done_button)).perform(ViewActions.click());

        // Verify that the status is saved in SharedPreferences (you may need to access the actual SharedPreferences)
      //  SharedPreferences preferences = activityRule.getScenario().getResult().getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        ResourceBundle preferences = null;
        String status = preferences.getString("user_status");
        assertEquals("Status updated!", status);
    }
}
