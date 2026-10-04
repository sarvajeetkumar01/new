package com.campusevents.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ManageEventsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.addEventButton).setOnClickListener(v ->
            startActivity(new Intent(this, CreateEventActivity.class)));
        findViewById(R.id.participantsButton).setOnClickListener(v ->
            startActivity(new Intent(this, EventParticipantsActivity.class)));
    }
}
