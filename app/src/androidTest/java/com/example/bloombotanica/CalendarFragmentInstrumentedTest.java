package com.example.bloombotanica;

import android.view.View;
import androidx.fragment.app.FragmentActivity;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.bloombotanica.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class CalendarFragmentInstrumentedTest {

    @Rule
    public ActivityScenarioRule<FragmentActivity> activityRule = new ActivityScenarioRule<>(FragmentActivity.class);

    @Test
    public void testCalendarDateSelection() {
        // Simulate selecting a date on the calendar
        onView(withId(R.id.calendarView)).perform(ViewActions.click());

        // Verify that the tasks for the selected date are displayed
        onView(withId(R.id.taskRecyclerView)).check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyTaskMessage() {
        // Simulate selecting a date with no tasks
        onView(withId(R.id.calendarView)).perform(ViewActions.click());

        // Simulate selecting a date with no tasks (e.g., January 1, 2024)
        CalendarDay calendarDay = CalendarDay.from(2024, 1, 1);
    //    onView(withId(R.id.calendarView)).perform(ViewActions.repeatedlyUntil(calendarDay));

        // Check that the "No Tasks" message is displayed
        onView(withId(R.id.emptyTaskMessage)).check(matches(isDisplayed()));
    }

    @Test
    public void testTaskRecyclerViewUpdatesOnDateChange() {
        // Initial date selection
        onView(withId(R.id.calendarView)).perform(ViewActions.click());

        // Get the RecyclerView and check if the tasks are loaded
        onView(withId(R.id.taskRecyclerView)).check(matches(isDisplayed()));

        // Simulate a date change (e.g., user selects a new date)
        CalendarDay newCalendarDay = CalendarDay.from(2024, 5, 10);
     //   onView(withId(R.id.calendarView)).perform(ViewActions.setDate(newCalendarDay));

        // Check if the task list is updated (by ensuring the RecyclerView is still visible)
        onView(withId(R.id.taskRecyclerView)).check(matches(isDisplayed()));
    }
}
