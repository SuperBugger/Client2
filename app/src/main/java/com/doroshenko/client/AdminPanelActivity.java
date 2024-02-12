package com.doroshenko.client;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {
    private Button PlusNewGroup, AddMemberToGroup;
    private LinearLayout PurpleButtonsLayout, AddEverythingBlock, MainLayout, GroupsScrollLayout;
    private EditText EnterGroup;
    private AutoCompleteTextView EnterDancer;
    private List<String> newGroupNames = new ArrayList<>();

    public AdminPanelActivity() {
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminpanel_activity);
        MainLayout = findViewById(R.id.main_layout_admin_panel);
        GroupsScrollLayout = findViewById(R.id.groups_scroll_layout);
        //изменение положения основных кнопок
        AddMemberToGroup = findViewById(R.id.add_member_to_group_btn);
        PlusNewGroup = findViewById(R.id.plus_new_group);
        PurpleButtonsLayout = findViewById(R.id.purple_buttons_layout);
        EnterGroup = findViewById(R.id.enter_group);
        AddEverythingBlock = findViewById(R.id.add_everything_block);
        PlusNewGroup.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                AddEverythingBlock.setBackground(getResources().getDrawable(R.drawable.dialog_background));
                EnterGroup.setVisibility(View.VISIBLE);
                EnterDancer.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                int marginInDp10 = 10;
                int marginInDp25 = 25;
                float scale = getResources().getDisplayMetrics().density;
                int marginInPixels10 = (int) (marginInDp10 * scale + 0.5f);
                int marginInPixels25 = (int) (marginInDp25 * scale + 0.5f);
                layoutParams.setMargins(marginInPixels10, marginInPixels25, marginInPixels10, marginInPixels10);
                PurpleButtonsLayout.setLayoutParams(layoutParams);
                AddMemberToGroup.setVisibility(View.VISIBLE);
                AddMemberToGroup.setText(getResources().getString(R.string.add_group_to_btn));
                AddMemberToGroup.setTextColor(getResources().getColor(R.color.white));
                AddMemberToGroup.setTextSize(15);
                PlusNewGroup.setVisibility(View.GONE);
            }
        });
        getAllGroups();
        //автозаполнение поля ввода участника
        EnterDancer = findViewById(R.id.enter_dancer);
        String[] all_users = getAllMembers().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, all_users);
        EnterDancer.setAdapter(adapter);
        //обработчик нажатий на bottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_admin);
        bottomNavigationView.setOnItemSelectedListener(
                new BottomNavigationView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.navigation_timetable:
                                Intent mainActivityIntent = new Intent(AdminPanelActivity.this, MainActivity.class);
                                startActivity(mainActivityIntent);
                                overridePendingTransition(0, 0);
                                return true;
                            case R.id.navigation_adminpanel:
                                return true;
                        }
                        return false;
                    }
                });
        bottomNavigationView.setSelectedItemId(R.id.navigation_adminpanel);
        //обработчик нажаитй на кнопку создать новую группу или добавить участников
        AddMemberToGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMemberToGroup(EnterDancer.getText().toString(), EnterGroup.getText().toString());
                EnterDancer.setText("");
            }
        });

    }

    //метод получения всех групп из бд
    private void getAllGroups() {
        Thread thread = new Thread(new Runnable() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM dance_group;");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        //сама плашка
                        final LinearLayout groupLine = new LinearLayout(AdminPanelActivity.this);
                        groupLine.setBackground(getResources().getDrawable(R.drawable.group_line_background));
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dpToPx(53));
                        int lineMarginInDp = dpToPx(15);
                        layoutParams.setMargins(lineMarginInDp, 0, lineMarginInDp, lineMarginInDp);
                        groupLine.setLayoutParams(layoutParams);
                        groupLine.setOrientation(LinearLayout.HORIZONTAL);
                        //текст названия группы
                        final TextView GroupName = new TextView(AdminPanelActivity.this);
                        GroupName.setText(resultSet.getString("group_name"));
                        GroupName.setTextColor(getResources().getColor(R.color.white));
                        GroupName.setTextSize(15);
                        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams1.weight = 1;
                        layoutParams1.gravity = Gravity.LEFT;
                        GroupName.setPadding(0, 17, 0, 0);
                        GroupName.setLayoutParams(layoutParams1);
                        groupLine.addView(GroupName);
                        //кнопка добавления участников
                        final Button AddUser = new Button(AdminPanelActivity.this);
                        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                                100,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams2.gravity = Gravity.RIGHT;
                        AddUser.setLayoutParams(layoutParams2);
                        AddUser.setBackgroundColor(getResources().getColor(R.color.transparent));
                        AddUser.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.add_user_icon3, 0, 0);
                        //действие по нажатию на кнопку
                        AddUser.setOnClickListener(new View.OnClickListener() {
                            @SuppressLint("ResourceType")
                            @Override
                            public void onClick(View view) {
                                if(PlusNewGroup.getVisibility() == View.VISIBLE){
                                    PlusNewGroup.callOnClick();
                                }
                                AddMemberToGroup.setText(getResources().getString(R.string.add_member));
                                AddMemberToGroup.setTextColor(getResources().getColor(R.color.white));
                                AddMemberToGroup.setTextSize(15);
                                EnterGroup.setText(GroupName.getText());
                                System.out.println(GroupName.getText());
                            }
                        });

                        groupLine.addView(AddUser);
                        GroupsScrollLayout.addView(groupLine);
                    }
                    connection.close();
                } catch (SQLException e) {
                    System.out.println(e);
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
    //метод получения всех участников из бд
    private List<String> getAllMembers(){
        List<String> all_members = new ArrayList<>();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM get_members();");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    while (resultSet.next()) {
                        all_members.add(resultSet.getString("get_members"));
                    }
                }catch (SQLException e) {
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
        return all_members;
    }
    //метод добавления участника
    private void addMemberToGroup(String fio, String groupTitle){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", User.getPhone(), User.getPassword());
                    PreparedStatement preparedStatement = connection.prepareStatement("CALL add_member_to_group(?, ?);");
                    preparedStatement.setString(1, fio);
                    preparedStatement.setString(2, groupTitle);
                    preparedStatement.execute();
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

    // Метод для преобразования dp в пиксели
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
