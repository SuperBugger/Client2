package com.doroshenko.client;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    Logger  log = Logger.getLogger(MainActivity.class.getName());
    //кнопка плюсик
    private Button OpenDialogButton;
    private OkHttpClient client;
    //последний выбранный день
    private View lastView;
    private View centralDayBox;
    //Зал 1
    private LinearLayout fstRoom;
    //Зал 2
    private LinearLayout sndRoom;
    //Ввыбранный зал
    private LinearLayout selectedRoom;
    private LinearLayout eventItem;

    @SuppressLint({"ResourceType", "SetTextI18n", "MissingInflatedId", "LocalSuppress"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Определение переменных
        fstRoom = findViewById(R.id.fst_line);
        sndRoom = findViewById(R.id.snd_line);
        selectedRoom = fstRoom;

        eventItem = (LinearLayout) findViewById(R.id.eventItem);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;

        OpenDialogButton = findViewById(R.id.openDialogButton);
        client = new OkHttpClient();
        HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.daysScrollView);
        LinearLayout weekBox = findViewById(R.id.weekBox);
        LocalDate start = LocalDate.now().minusDays(30);

        //создание обработчика нажатий на кнопку плюс
        OpenDialogButton.setOnClickListener(new View.OnClickListener() {
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
            dayBox.setMinimumHeight(200);
            ConstraintLayout.LayoutParams linLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            linLayoutParams.setMargins(18, 32, 18, 20);
            weekBox.addView(dayBox, linLayoutParams);
            ViewGroup.LayoutParams lpView = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            AppCompatTextView dayDay = new AppCompatTextView(this);
            dayDay.setId(R.string.dayDay);
            String day = start.getDayOfWeek().toString().charAt(0) + start.getDayOfWeek().toString().substring(1).toLowerCase();
            dayDay.setText(String.format("%." + 3 + "s", day));
            dayDay.setTextSize(18);
            dayDay.setTextColor(getResources().getColor(R.color.not_selected_text));
            dayDay.setPadding(20, 25, 20, 5);
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
            dayDate.setTextSize(18);
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
                centralDayBox = dayBox;
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
        DialogEventClass dialogEventClass = new DialogEventClass(groupTitle, Description, startTime, endTime, id);
        dialogEventClass.show(getSupportFragmentManager(), "dialogEvent_fragment");
    }

    //метод получения данных о тренировке с сервера
    private void getTrainingFromServer(TextView selectedRoomTextView, LocalDate date) {
        String roomTitle = (String) selectedRoomTextView.getText();
        int startIndex = 4;
        int endIndex = 5;

        String url = "https://523a-185-32-134-61.ngrok-free.app/api/data" + "?room=" + roomTitle.substring(startIndex, endIndex) + "&date=" + date;

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                if (response.isSuccessful()) {

                    String responseBody = response.body().string();

                    try {

                        JSONArray jsonArray = new JSONArray(responseBody);

                        for(int i = 0; i < jsonArray.length(); i++){

                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String group = jsonObject.getString("GroupTitle");
                            String description = jsonObject.getString("Description");
                            String startTime = jsonObject.getString("StartTime");
                            String endTime = jsonObject.getString("EndTime");
                            String id = jsonObject.getString("Id");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Обработка успешного ответа на UI-потоке
                                    try {
                                        AddTrain(group, description, startTime, endTime, Integer.valueOf(id));
                                    } catch (ParseException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    log.warning("ошибка1");
                }

            }

        });

    }

    //метод отправки данных о новой тренировке на сервер
    void sendTrainingToServer(Train train) {
        String url = "https://523a-185-32-134-61.ngrok-free.app/api/data";

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("room", train.getRoom());
            jsonObject.put("group", train.getGroup());
            jsonObject.put("date", train.getDate().toString());
            jsonObject.put("startTime", train.getTimeStart().toString());
            jsonObject.put("endTime", train.getTimeEnd().toString());
            jsonObject.put("description", train.getDescription());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Ошибка при добавлении тренировки", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                        log.warning("успех");

                } else {
                    log.warning("ошибка");
                }
            }
        });
    }

    //метод отрисовки нового элемента тренировки
    void AddTrain(String GroupName, String Description, String StartTime, String EndTime, Integer Id) throws ParseException {

        String baseTime = "09:00:00.0000000";

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSSSSSS");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date startDate = format.parse(StartTime);
        Date endDate = format.parse(EndTime);
        Date baseDate = format.parse(baseTime);

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
                view.setBackgroundResource(R.drawable.dialog_background);
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

    //метод выделения текст бокса
    void MakeDaySelected(View dayBox){
        dayBox.setBackgroundResource(R.drawable.selected_shape);
        @SuppressLint("ResourceType") TextView selectedDayDay = dayBox.findViewById(R.string.dayDay);
        selectedDayDay.setTextColor(getColor(R.color.selected_text));
        @SuppressLint("ResourceType") TextView selectedDayDate = dayBox.findViewById(R.string.dayDate);
        selectedDayDate.setTextColor(getColor(R.color.selected_text));
    }

}