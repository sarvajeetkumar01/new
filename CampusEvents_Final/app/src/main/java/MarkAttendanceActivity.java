package com.campusevents.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MarkAttendanceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveButton).setOnClickListener(v ->
            Toast.makeText(this, "Attendance saved in demo mode.", Toast.LENGTH_SHORT).show());
    }
}
