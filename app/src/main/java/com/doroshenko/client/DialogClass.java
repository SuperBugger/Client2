package com.doroshenko.client;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DialogClass extends DialogFragment {

    private Switch roomSwitch;
    private Spinner groupSpinner;
    private EditText description;
    private Button datePicker, startTimePicker, endTimePicker, addEvent;
    private TextView dateText;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @SuppressLint("CutPasteId")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_layout, null);

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(1000, 1500);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        roomSwitch = (Switch) dialog.findViewById(R.id.switch_room);
        groupSpinner = (Spinner) dialog.findViewById(R.id.spinner);
        description = (EditText) dialog.findViewById(R.id.description);
        addEvent = (Button) dialog.findViewById(R.id.add_event);
        datePicker = (Button) dialog.findViewById(R.id.date_button);
        startTimePicker = (Button) dialog.findViewById(R.id.time_picker_start_btn);
        endTimePicker = (Button) dialog.findViewById(R.id.time_picker_end_btn);
        dateText = (TextView) dialog.findViewById(R.id.date_text);


        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), R.style.DatePickerTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                dateText.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        startTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                mHour = cal.get(Calendar.HOUR_OF_DAY);
                mMinute = cal.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), R.style.TimePickerTheme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                startTimePicker.setText(String.format("%02d:%02d", hourOfDay, minute));
                            }
                        }, mHour, mMinute, false);

                timePickerDialog.show();
            }
        });

        endTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                mHour = cal.get(Calendar.HOUR_OF_DAY);
                mMinute = cal.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), R.style.TimePickerTheme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                endTimePicker.setText(String.format("%02d:%02d", hourOfDay, minute));
                            }
                        }, mHour, mMinute, false);

                timePickerDialog.show();
            }
        });

        addEvent.setOnClickListener(new  View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String room;
                if(roomSwitch.isChecked())room = "2";
                else room = "1";
                String group = groupSpinner.getSelectedItem().toString();
                String date = dateText.getText().toString();
                String startTime = startTimePicker.getText().toString();
                String endTime = endTimePicker.getText().toString();
                String textInput = description.getText().toString();

                LocalTime _stReqTime = LocalTime.parse(startTime);
                LocalTime _edReqTime = LocalTime.parse(endTime);
                LocalTime _startStTime = LocalTime.of(8, 59);
                LocalTime _startEdTime = LocalTime.of(22, 1);
                LocalTime _endStTime = LocalTime.of(9, 59);
                LocalTime _endEdTime = LocalTime.of(23, 1);

                if (_stReqTime.isAfter(_startStTime) && _stReqTime.isBefore(_startEdTime) && _edReqTime.isAfter(_endStTime) && _edReqTime.isBefore(_endEdTime)){

                    Training training = new Training(room, group, date, startTime, endTime, textInput);
                    MainActivity mainActivity = new MainActivity();
                    mainActivity.sendTrainingToServer(training);
                    Toast.makeText(getContext(), "Занятие добавлено", Toast.LENGTH_SHORT).show();
                    dismiss();

                }
                else {
                    Toast.makeText(getContext(), "Неверные данные", Toast.LENGTH_SHORT).show();
                }

            }
        });

        TextView currentDateTextView = (TextView) dialog.findViewById(R.id.date_text);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String formattedStringDate = getString(R.string.current_date, currentDate);
        currentDateTextView.setText(formattedStringDate);

        currentDateTextView.setTextColor(Color.WHITE);
        currentDateTextView.setTextSize(25);

        Button currentTimeButtonStart = (Button) dialog.findViewById(R.id.time_picker_start_btn);
        Button currentTimeButtonEnd = (Button) dialog.findViewById(R.id.time_picker_end_btn);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        int startHour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.add(Calendar.HOUR_OF_DAY, 2);
        int endHour = calendar.get(Calendar.HOUR_OF_DAY);
        String formattedStringTimeStart = getString(R.string.start_time, String.format("%02d:00", startHour));
        String formattedStringTimeEnd = getString(R.string.end_time, String.format("%02d:00", endHour));
        currentTimeButtonStart.setText(formattedStringTimeStart);
        currentTimeButtonEnd.setText(formattedStringTimeEnd);

        currentTimeButtonStart.setTextColor(Color.WHITE);
        currentTimeButtonStart.setTextSize(25);
        currentTimeButtonEnd.setTextColor(Color.WHITE);
        currentTimeButtonEnd.setTextSize(25);

        return dialog;
    }

}

