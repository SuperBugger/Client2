package com.doroshenko.client;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class DialogEventClass extends DialogFragment {
    private final Integer id;

    private final String groupTitleTxt;
    private final String descriptionTxt;
    private final String startTimeTxt;
    private final String endTimeTxt;
    private Context context;

    public DialogEventClass(String groupTitle, String Description, String startTime, String endTime, Integer id, Context context) {

        this.groupTitleTxt = groupTitle;
        this.descriptionTxt = Description;
        this.startTimeTxt = startTime;
        this.endTimeTxt = endTime;
        this.id = id;
        this.context = context;

    }

    @SuppressLint("ResourceType")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_event, null);

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(1000, 1200);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button delButton = dialog.findViewById(R.id.del_event);
        TextView groupTitle = dialog.findViewById(R.id.eventGroupTitleText);
        TextView description = dialog.findViewById(R.id.eventDescriptionText);
        TextView startTime = dialog.findViewById(R.id.eventStartTimeText);
        TextView endTime = dialog.findViewById(R.id.eventEndTimeText);
        TextView WillOrWontBeText = dialog.findViewById(R.id.will_or_wont_be_text);

        groupTitle.setText(groupTitleTxt);
        description.setText(descriptionTxt);
        startTime.setText(startTimeTxt);
        endTime.setText(endTimeTxt);

        //обработчик нажатия на буду/не буду
        WillOrWontBeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WillOrWontBeText.getText() == getResources().getString(R.string.will_be)){
                    WillOrWontBeText.setText(getResources().getString(R.string.wont_be));
                }
                else {
                    WillOrWontBeText.setText(getResources().getString(R.string.will_be));
                }
                setAttendance(id, groupTitleTxt, User.getPhone(), WillOrWontBeText.getText().toString());
            }
        });

        TableLayout AttendanceTable = dialog.findViewById(R.id.attendance_table);
        TableRow row = new TableRow(context);

        TextView willBeTextView = new TextView(context);
        willBeTextView.setText("Будут:");
        willBeTextView.setTextColor(getResources().getColor(R.color.white));
        willBeTextView.setTextSize(15);
        willBeTextView.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));


        TextView wontBeTextView = new TextView(context);
        wontBeTextView.setText("       Не будут:");
        wontBeTextView.setTextColor(getResources().getColor(R.color.white));
        wontBeTextView.setTextSize(15);
        wontBeTextView.setPadding(10, 0, 0, 15);
        wontBeTextView.setLayoutParams(new TableRow.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
        ));

        row.addView(willBeTextView);
        row.addView(wontBeTextView);

        AttendanceTable.addView(row);
        makeAttendanceTable(dialog, id);

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteTrain(id);
                final Toast toast = Toast.makeText(getContext(), "Занятие удалено", Toast.LENGTH_SHORT);
                toast.show();
                dismiss();
            }
        });

        //отображение UI в зависимости от роли залогиневшегося пользователя
        if (User.getRole() == 1) {
            delButton.setVisibility(View.VISIBLE);
            AttendanceTable.setVisibility(View.VISIBLE);
            WillOrWontBeText.setVisibility(View.GONE);
        } else {
            delButton.setVisibility(View.GONE);
            AttendanceTable.setVisibility(View.GONE);
            if (isMemberInGroup(groupTitle.getText().toString(), User.getPhone())){
                WillOrWontBeText.setVisibility(View.VISIBLE);
            }
        }
        return dialog;
    }

    //метод удаления данных о тренировке с сервера
    private void deleteTrain(Integer id) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("CALL delete_training(?);");
                    preparedStatement.setInt(1, id);
                    preparedStatement.execute();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    //метод получения списка посещения пользователей
    private void makeAttendanceTable(AlertDialog dialog, Integer trainingId) {
        TableLayout AttendanceTable = dialog.findViewById(R.id.attendance_table);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM get_all_will_or_wont_be(?);");
                    preparedStatement.setInt(1, trainingId);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    connection.close();
                    while (resultSet.next()) {
                        TableRow row = new TableRow(context);
                        String willBeString = resultSet.getString("will_be");
                        if (willBeString != null){
                            willBeString = willBeString.replaceAll(",\"", "\n");
                            willBeString = willBeString.replaceAll("\"", "");
                            willBeString = willBeString.replaceAll("\\{", "");
                            willBeString = willBeString.replace("}", "");
                            TextView willBeTextView = new TextView(context);
                            willBeTextView.setText(willBeString);
                            willBeTextView.setTextColor(getResources().getColor(R.color.white));
                            willBeTextView.setTextSize(15);
                            willBeTextView.setLayoutParams(new TableRow.LayoutParams(
                                    AttendanceTable.getWidth() / 2,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1f
                            ));
                            row.addView(willBeTextView);
                        } else {
                            TextView willBeTextView = new TextView(context);
                            willBeTextView.setText("");
                            willBeTextView.setLayoutParams(new TableRow.LayoutParams(
                                    AttendanceTable.getWidth() / 2,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1f
                            ));
                            row.addView(willBeTextView);
                        }

                        String wontBeString = resultSet.getString("wont_be");
                        if (wontBeString != null){
                            wontBeString = wontBeString.replaceAll(",\"", "\n");
                            wontBeString = wontBeString.replaceAll("\"", "");
                            wontBeString = wontBeString.replaceAll("\\{", "");
                            wontBeString = wontBeString.replace("}", "");
                            TextView wontBeTextView = new TextView(context);
                            wontBeTextView.setText(wontBeString);
                            wontBeTextView.setTextColor(getResources().getColor(R.color.white));
                            wontBeTextView.setTextSize(15);
                            wontBeTextView.setPadding(10, 0, 0, 15);
                            wontBeTextView.setLayoutParams(new TableRow.LayoutParams(
                                    AttendanceTable.getWidth() / 2,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1f
                            ));
                            row.addView(wontBeTextView);
                        } else {
                            TextView willBeTextView = new TextView(context);
                            willBeTextView.setText("");
                            willBeTextView.setLayoutParams(new TableRow.LayoutParams(
                                    AttendanceTable.getWidth() / 2,
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    1f
                            ));
                            row.addView(willBeTextView);
                        }
                        AttendanceTable.addView(row);
                    }
                } catch (SQLException e) {
                    System.out.println(e);
                }

            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
    //метод изменения состояния прибытия на тренировке
    private void setAttendance(Integer training_id, String groupTitle, String phone, String attendance){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("CALL set_will_or_wont_be(?, ?, ?, ?::attendance_status);");
                    preparedStatement.setInt(1, training_id);
                    preparedStatement.setString(2, groupTitle);
                    preparedStatement.setString(3, phone);
                    preparedStatement.setString(4, attendance.toLowerCase());
                    preparedStatement.execute();
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
    //метод проверки принадлежности участника к группе
    private boolean isMemberInGroup(String groupTitle, String phone){
        final AtomicBoolean isMemberInGroup = new AtomicBoolean(false);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT is_member_in_group(?, ?);");
                    preparedStatement.setString(1, groupTitle);
                    preparedStatement.setString(2, phone);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    connection.close();
                    resultSet.next();
                    isMemberInGroup.set(resultSet.getBoolean("is_member_in_group"));
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        });
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        return isMemberInGroup.get();
    }
}

