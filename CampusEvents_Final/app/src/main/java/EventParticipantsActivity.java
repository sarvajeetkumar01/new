package com.campusevents.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class EventParticipantsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_participants);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.exportButton).setOnClickListener(v ->
            Toast.makeText(this, "CSV export demo.", Toast.LENGTH_SHORT).show());
    }
}
