package com.example.bloombotanica;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.bloombotanica.R;
import com.example.bloombotanica.ui.AboutPageActivity;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AboutPageActivityInstrumentedTest {

    private ActivityScenario<AboutPageActivity> activityScenario;

    @Before
    public void setUp() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AboutPageActivity.class);
        activityScenario = ActivityScenario.launch(intent);
    }

    @After
    public void tearDown() {
        if (activityScenario != null) {
            activityScenario.close();
        }
    }

    @Test
    public void testDialogIsDisplayed() {
        // Check if the dialog is displayed when activity starts
        onView(withId(R.layout.dialog_help))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDialogDismissWithOkButton() {
        // Click the OK button
        onView(withText("OK"))
                .inRoot(isDialog())
                .perform(click());

        // Verify the dialog is no longer displayed
        onView(withId(R.layout.dialog_help))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void testDialogNotCancellableOutside() {
        // Try to click outside the dialog (this should not dismiss it)
        onView(withId(android.R.id.content))
                .perform(click());

        // Verify the dialog is still displayed
        onView(withId(R.layout.dialog_help))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Custom matcher for dialog windows
     */
    private static TypeSafeMatcher<Root> isDialog() {
        return new TypeSafeMatcher<Root>() {
            @Override
            protected boolean matchesSafely(Root root) {
                return root.getDecorView().isShown() && root.getDecorView().hasWindowFocus();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is dialog");
            }
        };
    }
}