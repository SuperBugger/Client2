package com.doroshenko.client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    //последний выбранный день
    private View lastView;
    //Зал 1, 2 и выбранный зал
    private LinearLayout fstRoom, sndRoom, selectedRoom;

    @SuppressLint({"ResourceType", "SetTextI18n", "MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Определение переменных
        fstRoom = findViewById(R.id.fst_line);
        sndRoom = findViewById(R.id.snd_line);
        selectedRoom = fstRoom;

        LinearLayout eventItem = (LinearLayout) findViewById(R.id.eventItem);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //кнопка плюсик
        Button openDialogButton = findViewById(R.id.openDialogButton);
        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.daysScrollView);
        LinearLayout weekBox = findViewById(R.id.weekBox);
        LocalDate start = LocalDate.now().minusDays(30);
        Button DelEvent = findViewById(R.id.del_event);

        //отображение UI в зависимости от роли залогиневшегося пользователя
        if (User.getRole() == 1){
            openDialogButton.setVisibility(View.VISIBLE);
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        //создание обработчика нажатий на кнопку плюс
        openDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDialog();
            }
        });

        //создание и заполнение элементов выбора дня
        for (int i = 0; i < 61; i++) {

            LinearLayout dayBox = new LinearLayout(this);
            dayBox.setOrientation(LinearLayout.VERTICAL);
            dayBox.setBackgroundResource(R.drawable.shape);
            dayBox.setMinimumWidth(140);
            dayBox.setMinimumHeight(260);
            ConstraintLayout.LayoutParams linLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            linLayoutParams.setMargins(18, 32, 18, 20);
            weekBox.addView(dayBox, linLayoutParams);
            ViewGroup.LayoutParams lpView = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            AppCompatTextView dayDay = new AppCompatTextView(this);
            dayDay.setId(R.string.dayDay);
            String day = start.getDayOfWeek().toString().charAt(0) + start.getDayOfWeek().toString().substring(1).toLowerCase();
            dayDay.setText(String.format("%." + 3 + "s", day));
            dayDay.setTextSize(15);
            dayDay.setTextColor(getResources().getColor(R.color.not_selected_text));
            dayDay.setPadding(20, 50, 20, 5);
            dayDay.setLayoutParams(lpView);
            dayDay.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dayBox.addView(dayDay);
            AppCompatTextView dayDate = new AppCompatTextView(this);
            dayDate.setId(R.string.dayDate);
            int date = start.getDayOfMonth();
            if (date < 10)
                dayDate.setText("0" + date);
            else
                dayDate.setText(Integer.toString(date));
            dayDate.setTextSize(15);
            dayDate.setTextColor(getResources().getColor(R.color.not_selected_text));
            dayDate.setPadding(20, 0, 20, 30);
            dayDate.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            dayDate.setLayoutParams(lpView);
            dayBox.addView(dayDate);

            LocalDate finalStart = start;

            if (i == 30){
                lastView = dayBox;
                MakeDaySelected(dayBox);
                getTrainingFromServer((TextView) selectedRoom.getChildAt(0), finalStart);
            }

            if (i == 27) {
            }

            //обработчик нажатия на элемент выбора дня
            dayBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lastView.setBackgroundResource(R.drawable.shape);
                    TextView notSelectedDayDay = lastView.findViewById(R.string.dayDay);
                    notSelectedDayDay.setTextColor(getColor(R.color.not_selected_text));
                    TextView notSelectedDayDate = lastView.findViewById(R.string.dayDate);
                    notSelectedDayDate.setTextColor(getColor(R.color.not_selected_text));

                    MakeDaySelected(view);

                    ShowTrains(finalStart);

                    lastView = view;

                    //прокрутка выделенного дня до середины экрана
                    scrollView.smoothScrollTo((int) lastView.getX() - width/2 + lastView.getWidth()/2, 0);

                }
            });

            start = start.plusDays(1);
        }

        //обработчик нажати на Зал 1
        fstRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRoom = fstRoom;
                sndRoom.getChildAt(1).setBackgroundResource(R.drawable.line);
                fstRoom.getChildAt(1).setBackgroundResource(R.drawable.selected_line);
                lastView.callOnClick();
            }
        });
        //обработчик нажатия на Зал 2
        sndRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedRoom = sndRoom;
                sndRoom.getChildAt(1).setBackgroundResource(R.drawable.selected_line);
                fstRoom.getChildAt(1).setBackgroundResource(R.drawable.line);
                lastView.callOnClick();
            }
        });
        //обработчик нажатий на bottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(
                new BottomNavigationView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_timetable:
                                return true;
                            case R.id.navigation_adminpanel:
                                Intent adminpanelIntent = new Intent(MainActivity.this, AdminPanelActivity.class);
                                startActivity(adminpanelIntent);
                                overridePendingTransition(0, 0);
                                return true;
                        }
                        return false;
                }});

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.smoothScrollTo((int) lastView.getX() - width/2 + lastView.getWidth()/2, 0);
            }
        }, 500);

    }

    //метод открытия диалогового окна добавления тренировки
    private void OpenDialog() {
        DialogClass dialog = new DialogClass();
        dialog.show(getSupportFragmentManager(), "dialog_fragment");
    }
    //метод открытия диалогового окна просмотра тренировки
    private void OpenEventDialog(String groupTitle, String Description, String startTime, String endTime, Integer id){
        DialogEventClass dialogEventClass = new DialogEventClass(groupTitle, Description, startTime, endTime, id, this);
        dialogEventClass.show(getSupportFragmentManager(), "dialogEvent_fragment");
    }

    //метод получения данных о тренировке с сервера
    private void getTrainingFromServer(TextView selectedRoomTextView, LocalDate date) {
        String roomTitle = (String) selectedRoomTextView.getText();
        Integer hall_id = Integer.valueOf(roomTitle.substring(4, 5));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM get_trainings(?, ?);");
                    preparedStatement.setInt(1, hall_id);
                    preparedStatement.setDate(2, java.sql.Date.valueOf(date.toString()));
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()){
                        Integer id = resultSet.getInt("training_id");
                        String startTime = resultSet.getTime("training_start_time").toString();
                        String endTime = resultSet.getTime("training_end_time").toString();
                        String description = resultSet.getString("training_description");
                        String group = resultSet.getString("group_name");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    AddTrain(group, description, startTime + ".0000000", endTime + ".0000000", id);
                                } catch (ParseException e) {
                                    System.out.println(e);
                                }
                            }
                        });
                    }
                    connection.close();
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

    //метод отправки данных о новой тренировке на сервер
    void sendTrainingToServer(Training training) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                java.sql.Date dateToQuery = java.sql.Date.valueOf(training.getDate());
                Time startTimeToQuery = Time.valueOf(training.getTimeStart());
                Time endTimeToQuery = Time.valueOf(training.getTimeEnd());

                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT create_training(?, ?, ?, ?, ?, (SELECT group_id FROM dance_group WHERE group_name = ?));");
                    preparedStatement.setDate(1, dateToQuery);
                    preparedStatement.setTime(2, startTimeToQuery);
                    preparedStatement.setTime(3, endTimeToQuery);
                    preparedStatement.setString(4, training.getDescription());
                    preparedStatement.setInt(5, training.getRoomId());
                    preparedStatement.setString(6, training.getGroup());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    if (resultSet.getBoolean("create_training")){
                        // TODO: сообщение об успешном добавлении тренировки
                    } else {
                        // TODO: сообщение об ошибке при создании тренировки
                    }
                    connection.close();




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

    //метод отрисовки нового элемента тренировки
    void AddTrain(String GroupName, String Description, String StartTime, String EndTime, Integer Id) throws ParseException {

        String baseTime = "09:00:00.0000000";

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSSSSSS");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        java.util.Date startDate = format.parse(StartTime);
        java.util.Date endDate = format.parse(EndTime);
        java.util.Date baseDate = format.parse(baseTime);

        long _startLong = startDate.getTime() - baseDate.getTime();
        long _endLong = endDate.getTime() - baseDate.getTime();

        float startTime = (float) _startLong / (60 * 60 * 1000);
        float endTime = (float) _endLong / (60 * 60 * 1000);

        TimeLineLayout_1 timeLineLayout = findViewById(R.id.timeLine);
        Event_1 event = new Event_1();
        event.setGroup(GroupName);
        event.setDescription(Description);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setId(Id);
        EventView_1.SetupViewListener setupViewListener = new EventView_1.SetupViewListener() {
            @Override
            public void setupView(View view) {
                TextView tvGroup = view.findViewById(R.id.tvGroup);
                tvGroup.setText(event.getGroup());
                TextView tvDescription = view.findViewById(R.id.tvDescription);
                tvDescription.setText(event.getDescription());
                tvDescription.setTextColor(getColor(R.color.gray));
                view.setBackgroundResource(R.drawable.training_background);
            }
        };



        EventView_1<Event_1> eventView = new EventView_1(this, event, 1, R.layout.item_event_two, setupViewListener, new EventView_1.OnClickListener() {
            //создание обработчика нажатий на элемент тренировки
            @Override
            public void onClick(Event_1 event) {
                OpenEventDialog(event.getGroup(), event.getDescription(), StartTime.substring(0, 5), EndTime.substring(0, 5), event.getId());
            }
        });
        timeLineLayout.addEvent(eventView);
        ViewGroup frameLayout = (ViewGroup) timeLineLayout.getChildAt(0);
        ViewGroup timeLineLayoutGroup = (ViewGroup) frameLayout.getChildAt(0);
        for (int i = 0; i < timeLineLayoutGroup.getChildCount(); i++){
            ViewGroup train = (ViewGroup) timeLineLayoutGroup.getChildAt(i);
            train.setVisibility(View.GONE);
            train.setVisibility(View.VISIBLE);
        }

    }

    //метод отображения тренировок
    void ShowTrains(LocalDate finalStart){
        TimeLineLayout_1 timeLineLayout = findViewById(R.id.timeLine);
        ViewGroup frameLayout = (ViewGroup) timeLineLayout.getChildAt(0);
        ViewGroup timeLineLayoutGroup = (ViewGroup) frameLayout.getChildAt(0);
        timeLineLayoutGroup.removeAllViews();
        timeLineLayout.scrollTo(0, 0);

        getTrainingFromServer((TextView) selectedRoom.getChildAt(0), finalStart);
    }

    //метод выделения выбранного дня
    void MakeDaySelected(View dayBox){
        dayBox.setBackgroundResource(R.drawable.selected_shape);
        @SuppressLint("ResourceType") TextView selectedDayDay = dayBox.findViewById(R.string.dayDay);
        selectedDayDay.setTextColor(getColor(R.color.selected_text));
        @SuppressLint("ResourceType") TextView selectedDayDate = dayBox.findViewById(R.string.dayDate);
        selectedDayDate.setTextColor(getColor(R.color.selected_text));
    }

}