package com.example.bloombotanica;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.bloombotanica.ui.SettingsFragment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class SettingsFragmentUnitTest {

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private Toast mockToast;

    private SettingsFragment settingsFragment;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock the SharedPreferences behavior
        when(mockContext.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);

        settingsFragment = new SettingsFragment();
    }

    @Test
    public void testShowAccountInfoDialog_withValidName() {
        // Mock the behavior of the Toast to ensure it's shown
        when(mockToast.getView()).thenReturn(mock(View.class));

        // Simulate the action of saving a name
        settingsFragment.showAccountInfoDialog();

        // Assume that you have a valid name entered (simulate the user input)
        // Mock the name change behavior
        String validName = "John Doe";
        when(mockSharedPreferences.getString("username", "")).thenReturn(validName);
        settingsFragment.showAccountInfoDialog();

        // Verify if SharedPreferences editor is called to save the name
        verify(mockEditor).putString(eq("username"), eq(validName));
    }

    @Test
    public void testChangeThemeMode() {
        // Assuming shared preferences are set to dark mode (AppCompatDelegate.MODE_NIGHT_YES)
        when(mockSharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_NO)).thenReturn(AppCompatDelegate.MODE_NIGHT_YES);

        settingsFragment.applySavedThemeMode();

        // Verify that the theme mode is applied correctly
        assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode());
    }

    @Test
    public void testInvalidNameEntry() {
        // Simulate invalid name input (empty string or invalid characters)
        String invalidName = "John123";
        settingsFragment.showAccountInfoDialog();

        // Check that Toast is triggered for invalid name
        verify(mockToast).makeText(mockContext, "Name can only contain letters and spaces.", Toast.LENGTH_SHORT);
    }
}
