package com.example.timesaver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditTimeTable extends AppCompatActivity implements AddClassDialog.AddClassListener{

    private ImageButton button;
    private FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference timeTablesRef = database.getReference();
    TimetableView timetable;
    boolean dataLoaded = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.next_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(dataLoaded) {
            Intent intent = new Intent(EditTimeTable.this, Timer.class);
            String timeTableJson = timetable.createSaveData();
            intent.putExtra("timeTableJson", timeTableJson);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Data still Loading", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_time_table);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorHeadText));
        setSupportActionBar(toolbar);

        timetable = findViewById(R.id.timetable);

        button = findViewById(R.id.add_subject_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TimeTables timeTable = dataSnapshot.getValue(TimeTables.class);
                if (timeTable != null && !timeTable.timeTableJSON.equals("{}")) {
                    timetable.load(timeTable.timeTableJSON);
                    dataLoaded = true;
                    Toast.makeText(EditTimeTable.this, "Data Loaded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
            }
        };

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        timeTablesRef.child("time-tables").child(user.getUid()).addListenerForSingleValueEvent(postListener);

//        String json = timetable.createSaveData();
//        timetable.load();
    }

    public void openDialog() {
        AddClassDialog addClassDialog = new AddClassDialog();
        addClassDialog.show(getSupportFragmentManager(), "Add Class Dialog");
    }

    @Override
    public void applyTexts(String subjectName, String location, int startTimeHour, int startTimeMin, int endTimeHour, int endTimeMin, int dayOfWeek) {

        if (dayOfWeek == -1) {
            Toast toast = Toast.makeText(this, "Invalid Day", Toast.LENGTH_LONG);
            toast.show();
        } else {
            TimetableView timetable = findViewById(R.id.timetable);

            ArrayList<Schedule> schedules = new ArrayList<>();
            Schedule schedule = new Schedule();
            schedule.setClassTitle(subjectName);
            schedule.setClassPlace(location);
            schedule.setStartTime(new Time(startTimeHour, startTimeMin));
            schedule.setEndTime(new Time(endTimeHour, endTimeMin));
            schedule.setDay(dayOfWeek);
            schedules.add(schedule);
            timetable.add(schedules);

            String json = timetable.createSaveData();
            TimeTables timeTable = new TimeTables(json);
            timeTablesRef.child("time-tables").child(user.getUid()).setValue(timeTable);

        }

    }
}
