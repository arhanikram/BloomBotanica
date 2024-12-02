package com.example.bloombotanica;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.Toast;

import com.example.bloombotanica.ui.DashboardFragment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public class DashboardFragmentUnitTest {

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Mock
    private Toast mockToast;

    private DashboardFragment dashboardFragment;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock SharedPreferences and Toast
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockToast.getView()).thenReturn(mock(View.class));

        dashboardFragment = new DashboardFragment();
    }

    @Test
    public void testUpdateUserStatus() {
        String status = "Status updated!";

        // Call the method that updates the user status
       // dashboardFragment.updateUserStatus(status);

        // Verify that SharedPreferences Editor was called to save the status
        verify(mockEditor).putString("user_status", status);
        verify(mockEditor).apply();

        // Verify that Toast was shown
        verify(mockToast).makeText(Mockito.any(), eq("Status updated to: " + status), eq(Toast.LENGTH_SHORT));
    }
}
