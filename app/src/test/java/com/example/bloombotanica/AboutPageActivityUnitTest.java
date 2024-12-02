package com.example.bloombotanica;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDialog;

import com.example.bloombotanica.R;
import com.example.bloombotanica.ui.AboutPageActivity;

@RunWith(RobolectricTestRunner.class)
public class AboutPageActivityUnitTest {

    private AboutPageActivity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(AboutPageActivity.class)
                .create()
                .resume()
                .get();
    }

    @Test
    public void dialogIsShownOnCreate() {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull("Dialog should be shown", dialog);
        assertTrue("Dialog should be showing", dialog.isShowing());
    }

    @Test
    public void dialogHasCorrectLayout() {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        View dialogView = dialog.findViewById(R.layout.dialog_help);
        assertNotNull("Dialog should have the help layout", dialogView);
    }

    @Test
    public void dialogHasOkButton() {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        assertNotNull("Dialog should have an OK button", okButton);
    }

    @Test
    public void dialogDismissesOnOkButton() {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.performClick();

        assertNotNull("Dialog should exist", dialog);
        assertTrue("Dialog should not be showing after OK click",
                !dialog.isShowing());
    }

//    @Test
//    public void dialogCannotBeCancelledByClickingOutside() {
//        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
//        assertTrue("Dialog should not be cancelable", !dialog.isCancelable());
//    }
//
//    @Test
//    public void dialogViewHasWindowInsets() {
//        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
//        View dialogView = dialog.findViewById(R.layout.dialog_help);
//        assertNotNull("Dialog view should have window insets listener",
//                dialogView.getOnApplyWindowInsetsListener());
//    }
}