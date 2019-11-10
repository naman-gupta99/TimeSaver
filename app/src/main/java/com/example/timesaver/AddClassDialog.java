package com.example.timesaver;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class AddClassDialog extends AppCompatDialogFragment implements AdapterView.OnItemSelectedListener {

    private EditText subjectNameET;
    private EditText locationET;
    private EditText startTimeET;
    private EditText endTimeET;
    private Spinner spinner;
    private AddClassListener listener;
    int dayOfWeek = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_subject, null);

        builder.setView(view);
        builder.setTitle("Add Subject");
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
                String subjectName = subjectNameET.getText().toString();
                String location = locationET.getText().toString();
                Calendar startTime = Calendar.getInstance();
                int startTimeHour = 0;
                int startTimeMin = 0;
                try {
                    startTime.setTime(Objects.requireNonNull(formatter.parse(startTimeET.getText().toString())));
                    startTimeHour = startTime.get(Calendar.HOUR_OF_DAY);
                    startTimeMin = startTime.get(Calendar.MINUTE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Calendar endTime = Calendar.getInstance();
                int endTimeHour = 0;
                int endTimeMin = 0;
                try {
                    endTime.setTime(Objects.requireNonNull(formatter.parse(endTimeET.getText().toString())));
                    endTimeHour = endTime.get(Calendar.HOUR_OF_DAY);
                    endTimeMin = endTime.get(Calendar.MINUTE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                listener.applyTexts(subjectName, location, startTimeHour, startTimeMin, endTimeHour, endTimeMin, dayOfWeek);
            }
        });

        subjectNameET = view.findViewById(R.id.subject_name);
        locationET = view.findViewById(R.id.location);
        startTimeET = view.findViewById(R.id.start_time);
        endTimeET = view.findViewById(R.id.end_time);
        spinner = view.findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.my_header_title, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AddClassListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement ExampleDialogListener");
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
         dayOfWeek = i-1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        dayOfWeek = 0;
    }



    public interface AddClassListener {
        void applyTexts(String subjectName, String location, int startTimeHour, int startTimeMin, int endTimeHour, int endTimeMin, int dayOfWeek);
    }

}
