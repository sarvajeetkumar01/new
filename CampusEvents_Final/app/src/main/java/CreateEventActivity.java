
package com.campusevents.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateEventActivity extends Activity {

    private EditText titleInput;
    private EditText descriptionInput;
    private EditText dateInput;
    private EditText timeInput;
    private EditText venueInput;
    private EditText categoryInput;

    private Button createButton;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        titleInput = findViewById(R.id.eventTitleInput);
        descriptionInput = findViewById(R.id.eventDescriptionInput);
        dateInput = findViewById(R.id.eventDateInput);
        timeInput = findViewById(R.id.eventTimeInput);
        venueInput = findViewById(R.id.eventVenueInput);
        categoryInput = findViewById(R.id.eventCategoryInput);

        createButton = findViewById(R.id.createButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        createButton.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {

        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();
        String time = timeInput.getText().toString().trim();
        String venue = venueInput.getText().toString().trim();
        String category = categoryInput.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty()
                || date.isEmpty() || time.isEmpty()
                || venue.isEmpty() || category.isEmpty()) {

            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject event = new JSONObject();

        try {
            event.put("title", title);
            event.put("description", description);
            event.put("date", date);
            event.put("time", time);
            event.put("venue", venue);
            event.put("category", category);
            event.put("capacity", 50);

        } catch (Exception e) {
            Toast.makeText(this,
                    "Unable to prepare event",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        createButton.setEnabled(false);
        createButton.setText("Saving...");

        executor.execute(() -> {
            try {
                JSONObject response = ApiClient.request(
                        "POST",
                        "/api/events",
                        event
                );

                runOnUiThread(() -> {
                    createButton.setEnabled(true);
                    createButton.setText("Create Event");

                    Toast.makeText(this,
                            "Event created successfully!",
                            Toast.LENGTH_LONG).show();

                    finish();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    createButton.setEnabled(true);
                    createButton.setText("Create Event");

                    Toast.makeText(this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
