package com.campusevents.app;

import android.app.Activity;
import android.os.Bundle;

public class ProfileActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.myEventsButton).setOnClickListener(v ->
            startActivity(new android.content.Intent(this, MyEventsActivity.class)));
    }
}
