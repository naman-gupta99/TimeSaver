package com.example.timesaver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

class ClassInfo {
    public int startHour, startMin;
    public String className, classPlace;

    ClassInfo(int startHour, int startMin, String className, String classPlace) {
        this.startHour = startHour;
        this.startMin = startMin;
        this.className = className;
        this.classPlace = classPlace;
    }
}

public class Timer extends AppCompatActivity {

    TextView timerTV, nameTV, placeTV, trTV;

    String ClassName, ClassPlace;
    CountDownTimer countDownTimer;
    long timeLeft = 600000;

    public final static String TAG = "TAG";
    public final static String SHARED_PREFS = "sharedPrefs";
    public final static String DAY_NUM = "dayNum";
    public final static String CLASS_NUM = "classNum";

    private int dayNum;
    private int classNum;

    ArrayList<ClassInfo>[] arr = new ArrayList[7];

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(Timer.this, EditTimeTable.class);
        startActivity(intent);
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        Intent intent = getIntent();
        String timeTableJson = intent.getStringExtra("timeTableJson");

        if (timeTableJson == null || timeTableJson == "{}") {
            Intent intent1 = new Intent(this, EditTimeTable.class);
            startActivity(intent1);
            finish();
        } else {

            nameTV = findViewById(R.id.textView);
            placeTV = findViewById(R.id.textView2);
            timerTV = findViewById(R.id.textView3);
            trTV = findViewById(R.id.tr);

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            classNum = sharedPreferences.getInt(CLASS_NUM, 0);
            Calendar cal = Calendar.getInstance();
            dayNum = sharedPreferences.getInt(DAY_NUM, cal.get(Calendar.DAY_OF_WEEK) - 2);

            for (int i = 0; i < 7; i++) {
                arr[i] = new ArrayList<ClassInfo>();
            }


            try {
                JSONObject json = new JSONObject(timeTableJson);
                JSONArray stickers = json.getJSONArray("sticker");
                for (int i = 0; i < stickers.length(); i++) {
                    JSONObject obj = stickers.getJSONObject(i);
                    JSONArray schedule = obj.getJSONArray("schedule");
                    obj = schedule.getJSONObject(0);

                    int day, startH, startM;
                    String name, place;

                    name = obj.getString("classTitle");
                    place = obj.getString("classPlace");
                    day = obj.getInt("day");

                    JSONObject start = obj.getJSONObject("startTime");
                    startH = start.getInt("hour");
                    startM = start.getInt("minute");

                    ClassInfo classObj = new ClassInfo(startH, startM, name, place);

                    for (int j = 0; j < arr[day].size(); j++) {
                        if ((arr[day].get(j).startHour == classObj.startHour && arr[day].get(j).startMin >= classObj.startMin) || arr[day].get(j).startHour > classObj.startHour) {
                            arr[day].add(j, classObj);
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                Log.e("TAG", "Some Error Occurred during reading data for Timer : " + e.getMessage());
            }

            getNew();

            Toolbar toolbar = findViewById(R.id.toolbar2);
            toolbar.setTitle(R.string.app_name);
            toolbar.setTitleTextColor(getResources().getColor(R.color.colorHeadText));
            setSupportActionBar(toolbar);
        }
    }

    private void getNew() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        String finName = "";
        String finPlace = "";
        Long finMillisecs = Long.MAX_VALUE;

        if (dayNum == currentDay && classNum < arr[currentDay].size()) {
            Log.d(TAG, "getNew: in" + arr[currentDay]);
            if((arr[currentDay].get(classNum).startHour == hour && arr[currentDay].get(classNum).startMin >= minute) || arr[currentDay].get(classNum).startHour > hour) {
                finMillisecs = (long) ((arr[currentDay].get(classNum).startHour - hour) * 3600000 + (arr[currentDay].get(classNum).startMin - minute) * 60000);
                finName = arr[currentDay].get(classNum).className;
                finPlace = arr[currentDay].get(classNum).classPlace;
            } else {
                finMillisecs = Long.MAX_VALUE;
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                while(classNum < arr[currentDay].size()) {
                    if ((arr[currentDay].get(classNum).startHour == hour && arr[currentDay].get(classNum).startMin >= minute) || arr[currentDay].get(classNum).startHour > hour) {
                        finMillisecs = (long) ((arr[currentDay].get(classNum).startHour - hour) * 3600000 + (arr[currentDay].get(classNum).startMin- minute) * 60000);
                        finName = arr[currentDay].get(classNum).className;
                        finPlace = arr[currentDay].get(classNum).classPlace;
                        break;
                    }
                    classNum++;
                }

                editor.putInt(CLASS_NUM, classNum);
                editor.apply();
            }
        } else if (dayNum != currentDay) {
            finMillisecs = Long.MAX_VALUE;
            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(DAY_NUM, currentDay);
            dayNum = currentDay;
            classNum = 0;

            while(classNum < arr[currentDay].size()) {
                if ((arr[currentDay].get(classNum).startHour == hour && arr[currentDay].get(classNum).startMin >= minute) || arr[currentDay].get(classNum).startHour > hour) {
                    finMillisecs = (long) ((arr[currentDay].get(classNum).startHour - hour) * 3600000 + (arr[currentDay].get(classNum).startMin- minute) * 60000);
                    finName = arr[currentDay].get(classNum).className;
                    finPlace = arr[currentDay].get(classNum).classPlace;
                    break;
                }
                classNum++;
            }

            editor.putInt(CLASS_NUM, classNum);
            editor.apply();
        }
        if (finMillisecs == Long.MAX_VALUE) {
            timerTV.setText("No more classes today");
            nameTV.setText("");
            placeTV.setText("");
            trTV.setText("");
            return;
        } else {
            timeLeft = finMillisecs;
            nameTV.setText(finName);
            placeTV.setText(finPlace);
            trTV.setText("Time remaining : ");
        }

        countDownTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long l) {
                timeLeft = l;
                int s = (int) l/1000;
                int m = (int) s/60;
                int h = (int) m/60;
                s %= 60;
                m %= 60;
                String min = m < 10 ? "0" + m : "" + m;
                String sec = s < 10 ? "0" + s : "" + s;
                String time = h + ":" + min + ":" + sec;
                timerTV.setText(time);
            }

            @Override
            public void onFinish() {
                getNew();
            }
        }.start();

    }

    public void bunk(View v) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7;

        if(classNum > arr[currentDay].size()) {
            Toast.makeText(this, "No Class to bunk", Toast.LENGTH_SHORT).show();
        } else {

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putInt(CLASS_NUM, classNum + 1);
            editor.apply();
            classNum++;
            Log.d(TAG, "bunk: " + classNum);
            countDownTimer.cancel();
            getNew();
        }
    }

}
