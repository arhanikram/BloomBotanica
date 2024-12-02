package com.example.bloombotanica;

import static org.mockito.Mockito.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;

import com.example.bloombotanica.ui.AccountInfoActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AccountInfoActivityUnitTest {
    @Mock private Context mockContext;
    @Mock private SharedPreferences mockSharedPreferences;
    @Mock private SharedPreferences.Editor mockEditor;
    @Mock private View mockView;
    @Mock private EditText mockEditText;
    @Mock private Button mockButton;

    private AccountInfoActivity accountInfoActivity;

    @Before
    public void setUp() {
        accountInfoActivity = new AccountInfoActivity();

        // Set up mock behavior
        when(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
    }

    @Test
    public void testSaveUsername_Success() {
        String testName = "Test User";
        when(mockEditText.getText().toString()).thenReturn(testName);

        // Simulate save button click
        accountInfoActivity.saveButton.performClick();

        // Verify SharedPreferences interactions
        verify(mockEditor).putString("username", testName);
        verify(mockEditor).apply();
    }

    @Test
    public void testSaveUsername_EmptyName() {
        when(mockEditText.getText().toString()).thenReturn("");

        // Simulate save button click
        accountInfoActivity.saveButton.performClick();

        // Verify SharedPreferences was not called
        verify(mockEditor, never()).putString(anyString(), anyString());
        verify(mockEditor, never()).apply();
    }

    @Test
    public void testLoadExistingUsername() {
        String existingName = "Existing User";
        when(mockSharedPreferences.getString("username", "")).thenReturn(existingName);

        // Trigger onCreate
        accountInfoActivity.onCreateView(mock(LayoutInflater.class),
                mock(ViewGroup.class),
                mock(Bundle.class));

        // Verify EditText was updated
        verify(mockEditText).setText(existingName);
    }
}