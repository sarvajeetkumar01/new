package com.campusevents.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AIChatbotActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chatbot);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        EditText input = findViewById(R.id.messageInput);
        findViewById(R.id.sendButton).setOnClickListener(v -> {
            String message = input.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(this, "Type a message first.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Demo reply: I can help with campus events.", Toast.LENGTH_SHORT).show();
                input.setText("");
            }
        });
    }
}
