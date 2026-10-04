package com.campusevents.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class AdminDashboardActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        findViewById(R.id.manageEventsButton).setOnClickListener(v ->
            startActivity(new Intent(this, ManageEventsActivity.class)));
        findViewById(R.id.participantsButton).setOnClickListener(v ->
            startActivity(new Intent(this, EventParticipantsActivity.class)));
        findViewById(R.id.createEventButton).setOnClickListener(v ->
            startActivity(new Intent(this, CreateEventActivity.class)));
        findViewById(R.id.attendanceButton).setOnClickListener(v ->
            startActivity(new Intent(this, MarkAttendanceActivity.class)));
    }
}
