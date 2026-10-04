package com.campusevents.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentHomeActivity extends Activity {
    private TextView[] cards;
    private Button allButton, workshopsButton, seminarsButton, culturalButton;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final List<JSONObject> events=new ArrayList<>();
    private String category="all";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);
        cards=new TextView[]{findViewById(R.id.eventCard1),findViewById(R.id.eventCard2),findViewById(R.id.eventCard3)};
        allButton=findViewById(R.id.allButton); workshopsButton=findViewById(R.id.workshopsButton);
        seminarsButton=findViewById(R.id.seminarsButton); culturalButton=findViewById(R.id.culturalButton);
        allButton.setOnClickListener(v->filter("all"));
        workshopsButton.setOnClickListener(v->filter("workshop"));
        seminarsButton.setOnClickListener(v->filter("seminar"));
        culturalButton.setOnClickListener(v->filter("cultural"));
        for(TextView card:cards) card.setOnClickListener(v->{
            Object id=card.getTag();
            if(id instanceof Integer) {
                Intent intent=new Intent(this,EventDetailsActivity.class);
                intent.putExtra("event_id",(Integer)id); startActivity(intent);
            }
        });
        findViewById(R.id.myEventsNav).setOnClickListener(v->startActivity(new Intent(this,MyEventsActivity.class)));
        findViewById(R.id.chatNav).setOnClickListener(v->startActivity(new Intent(this,AIChatbotActivity.class)));
        findViewById(R.id.profileNav).setOnClickListener(v->startActivity(new Intent(this,ProfileActivity.class)));
        loadEvents();
    }

    private void loadEvents(){
        for(TextView c:cards){c.setText("Loading events...");c.setVisibility(View.VISIBLE);}
        io.execute(()->{
            try{
                JSONObject response=ApiClient.request("GET","/api/events",null);
                JSONArray arr=response.optJSONArray("events");
                List<JSONObject> loaded=new ArrayList<>();
                if(arr!=null) for(int i=0;i<arr.length();i++) loaded.add(arr.getJSONObject(i));
                runOnUiThread(()->{events.clear();events.addAll(loaded);render();});
            }catch(Exception e){runOnUiThread(()->{
                for(TextView c:cards)c.setText("Could not load events. Check Wi-Fi and Flask server.");
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show();
            });}
        });
    }
    private void filter(String value){category=value; select(value); render();}
    private void render(){
        List<JSONObject> shown=new ArrayList<>();
        for(JSONObject e:events){
            String c=e.optString("category","").toLowerCase();
            if(category.equals("all") || c.contains(category)) shown.add(e);
        }
        for(int i=0;i<cards.length;i++){
            if(i>=shown.size()){cards[i].setVisibility(View.GONE);continue;}
            JSONObject e=shown.get(i);
            cards[i].setVisibility(View.VISIBLE); cards[i].setTag(e.optInt("id"));
            cards[i].setText(e.optString("title","Untitled event")+"\n\n"+
                e.optString("date","Date TBA")+" | "+e.optString("time","Time TBA")+
                " | "+e.optString("venue","Venue TBA"));
        }
        if(shown.isEmpty()){cards[0].setVisibility(View.VISIBLE);cards[0].setTag(null);
            cards[0].setText("No events found. Ask an admin to add events.");}
    }
    private void select(String selected){
        Button[] all={allButton,workshopsButton,seminarsButton,culturalButton};
        String[] keys={"all","workshop","seminar","cultural"};
        for(int i=0;i<all.length;i++){
            boolean active=keys[i].equals(selected);
            all[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(active?0xFF000000:0xFFF5F5F5));
            all[i].setTextColor(active?0xFFFFFFFF:0xFF000000);
        }
    }
    @Override protected void onDestroy(){super.onDestroy();io.shutdown();}
}
