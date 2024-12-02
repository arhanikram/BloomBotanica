package com.example.bloombotanica.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import com.example.bloombotanica.R;

public class AccountInfoActivity extends DialogFragment {

    private EditText nameEditText;
    public Button saveButton;
    private SharedPreferences sharedPreferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this dialog fragment
        View view = inflater.inflate(R.layout.dialog_account_info, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Initialize views
        nameEditText = view.findViewById(R.id.name_edit_text);
        saveButton = view.findViewById(R.id.save_button);

        // Load the saved username from SharedPreferences (if available)
        String userName = sharedPreferences.getString("username", "");
        nameEditText.setText(userName);  // Set the current name in the EditText

        // Set up the save button to save the new username
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString();
            if (!newName.isEmpty()) {
                // Save the new username to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", newName);
                editor.apply(); // Apply changes asynchronously

                // Show a Toast message confirming the name was saved
                Toast.makeText(getContext(), "Name saved!", Toast.LENGTH_SHORT).show();

                // Dismiss the dialog
                dismiss();
            } else {
                // If the name is empty, show a Toast to notify the user
                Toast.makeText(getContext(), "Please enter a name.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
