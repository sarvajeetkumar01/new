package com.campusevents.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventDetailsActivity extends Activity {
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private int eventId,userId;
    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_event_details);
        eventId=getIntent().getIntExtra("event_id",-1);
        userId=getSharedPreferences("campus_session",MODE_PRIVATE).getInt("user_id",-1);
        findViewById(R.id.backButton).setOnClickListener(v->finish());
        findViewById(R.id.registerButton).setOnClickListener(v->register());
        if(eventId>0) loadDetails();
    }
    private void loadDetails(){
        io.execute(()->{try{
            JSONObject e=ApiClient.request("GET","/api/events/"+eventId,null).getJSONObject("event");
            runOnUiThread(()->{
                TextView title=findViewById(R.id.detailTitle), info=findViewById(R.id.detailInfo), desc=findViewById(R.id.detailDescription);
                title.setText(e.optString("title","Event"));
                info.setText(e.optString("date","")+"   "+e.optString("time","")+"\n\n"+e.optString("venue",""));
                desc.setText(e.optString("description",""));
            });
        }catch(Exception ex){runOnUiThread(()->Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show());}});
    }
    private void register(){
        if(eventId<1||userId<1){Toast.makeText(this,"Please log in with a registered account first.",Toast.LENGTH_LONG).show();return;}
        io.execute(()->{try{
            JSONObject body=new JSONObject();body.put("user_id",userId);
            ApiClient.request("POST","/api/events/"+eventId+"/register",body);
            runOnUiThread(()->Toast.makeText(this,"Registration successful!",Toast.LENGTH_SHORT).show());
        }catch(Exception ex){runOnUiThread(()->Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show());}});
    }
    @Override protected void onDestroy(){super.onDestroy();io.shutdown();}
}
