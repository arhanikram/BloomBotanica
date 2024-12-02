package com.example.bloombotanica;

import android.content.SharedPreferences;
import android.content.Context;

import androidx.preference.PreferenceManager;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import static org.junit.Assert.*;

import com.example.bloombotanica.ui.AppearanceActivity;

public class AppearanceActivityUnitTest {

    private AppearanceActivity activity;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Before
    public void setUp() {
        // Mock the context and SharedPreferences
        Context context = mock(Context.class);
        preferences = mock(SharedPreferences.class);
        editor = mock(SharedPreferences.Editor.class);

        // Setup the mock to return a mocked editor
        when(preferences.edit()).thenReturn(editor);

        activity = new AppearanceActivity();
        activity.preferences = preferences; // Inject mock preferences
    }

    @Test
    public void testSaveThemeMode() {
        // Simulate saving light mode (MODE_NIGHT_NO)
        activity.saveThemeMode(0);

        // Verify that the putInt method is called with correct arguments
        verify(editor).putInt("theme_mode", 0);
        verify(editor).apply();
    }

    @Test
    public void testSaveDarkMode() {
        // Simulate saving dark mode (MODE_NIGHT_YES)
        activity.saveThemeMode(1);

        // Verify that the putInt method is called with correct arguments
        verify(editor).putInt("theme_mode", 1);
        verify(editor).apply();
    }
}
