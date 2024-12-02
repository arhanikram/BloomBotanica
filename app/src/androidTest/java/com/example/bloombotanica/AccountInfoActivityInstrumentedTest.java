package com.example.bloombotanica;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.bloombotanica.R;
import com.example.bloombotanica.ui.AccountInfoActivity;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AccountInfoActivityInstrumentedTest {
    private Context context;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        // Clear preferences before each test
        sharedPreferences.edit().clear().commit();
    }

    @Test
    public void testUIElements_AreDisplayed() {
        FragmentScenario.launchInContainer(AccountInfoActivity.class);

        onView(withId(R.id.name_edit_text)).check(matches(isDisplayed()));
        onView(withId(R.id.save_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testSaveButton_WithValidInput_SavesNameAndDismissesDialog() {
        FragmentScenario<AccountInfoActivity> scenario =
                FragmentScenario.launchInContainer(AccountInfoActivity.class);

        String testName = "John Doe";
        onView(withId(R.id.name_edit_text)).perform(typeText(testName));
        onView(withId(R.id.save_button)).perform(click());

        // Verify toast message
        onView(withText("Name saved!")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));

        // Verify saved preferences
        String savedName = sharedPreferences.getString("username", "");
        assertEquals(testName, savedName);
    }

    @Test
    public void testSaveButton_WithEmptyInput_ShowsError() {
        FragmentScenario.launchInContainer(AccountInfoActivity.class);

        onView(withId(R.id.save_button)).perform(click());

        // Verify error toast
        onView(withText("Please enter a name.")).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testLoadExistingUsername_DisplaysInEditText() {
        // Set up existing username
        String existingName = "Jane Doe";
        sharedPreferences.edit().putString("username", existingName).commit();

        FragmentScenario.launchInContainer(AccountInfoActivity.class);

        // Verify the existing name is displayed
        onView(withId(R.id.name_edit_text)).check(matches(withText(existingName)));
    }
}

// Custom Toast Matcher for testing toasts
class ToastMatcher extends TypeSafeMatcher<Root> {
    @Override
    public boolean matchesSafely(Root root) {
        int type = root.getWindowLayoutParams().get().type;
        if (type == WindowManager.LayoutParams.TYPE_TOAST) {
            IBinder windowToken = root.getDecorView().getWindowToken();
            IBinder appToken = root.getDecorView().getApplicationWindowToken();
            return windowToken == appToken;
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("is toast");
    }
}