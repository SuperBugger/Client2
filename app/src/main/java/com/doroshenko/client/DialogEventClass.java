package com.doroshenko.client;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.logging.Logger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DialogEventClass extends DialogFragment {
    private final Integer id;
    private Button delButton;

    private TextView groupTitle;
    private TextView description;
    private TextView startTime;
    private TextView endTime;
    private String groupTitleTxt;
    private String descriptionTxt;
    private String startTimeTxt;
    private String endTimeTxt;
    private OkHttpClient client;
    Logger log = Logger.getLogger(MainActivity.class.getName());

    public DialogEventClass(String groupTitle, String Description, String startTime, String endTime, Integer id) {

        this.groupTitleTxt = groupTitle;
        this.descriptionTxt = Description;
        this.startTimeTxt = startTime;
        this.endTimeTxt = endTime;
        this.id = id;

    }

    @SuppressLint("ResourceType")
    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_event, null);

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(1000, 1000);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        delButton = dialog.findViewById(R.id.del_event);
        groupTitle = dialog.findViewById(R.id.eventGroupTitleText);
        description = dialog.findViewById(R.id.eventDescriptionText);
        startTime = dialog.findViewById(R.id.eventStartTimeText);
        endTime = dialog.findViewById(R.id.eventEndTimeText);

        groupTitle.setText(groupTitleTxt);
        description.setText(descriptionTxt);
        startTime.setText(startTimeTxt);
        endTime.setText(endTimeTxt);

        client = new OkHttpClient();

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteTrain(id);
                final Toast toast = Toast.makeText(getContext(), "Занятие удалено", Toast.LENGTH_SHORT);
                toast.show();
                dismiss();

            }
        });

        return dialog;
    }

    //метод удаления данных о тренировке с сервера
    private void deleteTrain(Integer id){
        String url = "https://523a-185-32-134-61.ngrok-free.app/api/data" + "?id=" + id;

        Request request = new Request.Builder().url(url).delete().build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                System.out.println("ошибка удаления");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {

                    log.warning("успех");

                } else {
                    log.warning("ошибка1");
                }
            }
        });
    }

}
