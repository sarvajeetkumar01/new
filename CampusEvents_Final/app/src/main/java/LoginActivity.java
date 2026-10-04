package com.campusevents.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends Activity {
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login = findViewById(R.id.loginButton);
        Button admin = findViewById(R.id.adminButton);
        EditText email = findViewById(R.id.emailInput);
        EditText password = findViewById(R.id.passwordInput);
        login.setOnClickListener(v -> {
            String e=email.getText().toString().trim(), p=password.getText().toString();
            if(e.isEmpty() || p.isEmpty()){ Toast.makeText(this,"Enter email and password.",Toast.LENGTH_SHORT).show(); return; }
            login.setEnabled(false);
            io.execute(() -> {
                try {
                    JSONObject body=new JSONObject(); body.put("email",e); body.put("password",p);
                    JSONObject response=ApiClient.request("POST","/api/login",body);
                    JSONObject user=response.getJSONObject("user");
                    getSharedPreferences("campus_session",MODE_PRIVATE).edit()
                        .putInt("user_id",user.getInt("id")).putString("user_name",user.optString("name"))
                        .putString("user_email",user.optString("email")).apply();
                    runOnUiThread(() -> { startActivity(new Intent(this,StudentHomeActivity.class)); finish(); });
                } catch(Exception ex) { runOnUiThread(() -> { login.setEnabled(true); Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show(); }); }
            });
        });
        admin.setOnClickListener(v -> startActivity(new Intent(this, AdminDashboardActivity.class)));
    }
    @Override protected void onDestroy(){ super.onDestroy(); io.shutdown(); }
}
