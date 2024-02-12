package com.doroshenko.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextCode, editTextPhone, editTextPassword;
    private Button buttonLogin, buttonRegister;
    private LinearLayout switchToLogin, switchToRegister;
    private static final String url = "https://be23-91-193-177-118.ngrok-free.app/";
    private boolean isErrorVisible = false;
    private static final int SHOW_ERROR_MESSAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //импорт элементов из xml
        editTextName = findViewById(R.id.editTextFullname);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextCode = findViewById(R.id.editTextCode);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.loginButton);
        buttonRegister = findViewById(R.id.registerButton);
        switchToLogin = findViewById(R.id.loginSwitch);
        switchToRegister = findViewById(R.id.registerSwitch);

        //блок отвечающий за форматирование номера телефона при вводе
        EditText phoneNumberEditText = findViewById(R.id.editTextPhone);
        phoneNumberEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher("RU"));

        //переключение login <-> register
        switchToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextName.setVisibility(View.GONE);
                editTextEmail.setVisibility(View.GONE);
                editTextCode.setVisibility(View.GONE);
                buttonLogin.setVisibility(View.VISIBLE);
                buttonRegister.setVisibility(View.GONE);
                switchToLogin.getChildAt(1).setBackgroundResource(R.drawable.selected_line);
                switchToRegister.getChildAt(1).setBackgroundResource(R.drawable.line);
                editTextName.setText("");
                editTextEmail.setText("");
                editTextCode.setText("");
                editTextPhone.setText("");
                editTextPassword.setText("");
            }
        });
        switchToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextName.setVisibility(View.VISIBLE);
                editTextEmail.setVisibility(View.VISIBLE);
                editTextCode.setVisibility(View.VISIBLE);
                buttonLogin.setVisibility(View.GONE);
                buttonRegister.setVisibility(View.VISIBLE);
                switchToLogin.getChildAt(1).setBackgroundResource(R.drawable.line);
                switchToRegister.getChildAt(1).setBackgroundResource(R.drawable.selected_line);
                editTextPhone.setText("");
                editTextPassword.setText("");
            }
        });

        //обработчик события нажатия на кнопку регистрации
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextName.getText().toString().equals("") || editTextEmail.getText().toString().equals("") ||
                        editTextPhone.getText().toString().equals("") || editTextPassword.getText().toString().equals("")){
                    showError(getResources().getString(R.string.register_data_error_msg));
                }
                else {
                    if (editTextPhone.getText().toString().replaceAll("[^\\d]", "").length() != 11){
                        showError(getResources().getString(R.string.incorrect_phone_msg));
                    } else {
                        if (editTextCode.getText().toString().equals("2")) {
                            registerNewUser(editTextName.getText().toString(), editTextEmail.getText().toString(),
                                    editTextPhone.getText().toString(), editTextPassword.getText().toString(), 1);
                        } else {
                            registerNewUser(editTextName.getText().toString(), editTextEmail.getText().toString(),
                                    editTextPhone.getText().toString(), editTextPassword.getText().toString(), 2);
                        }
                    }
                }
            }
        });

        //обработчик события нажатия на кнопку логина
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextPhone.getText().toString().equals("") || editTextPassword.getText().toString().equals("")){
                    showError(getResources().getString(R.string.register_data_error_msg));
                } else {
                    login_user(editTextPhone.getText().toString(), editTextPassword.getText().toString());
                }
            }
        });

    }

    //отправка данных на сервер для регистрации пользователя
    private void registerNewUser(String fio, String email, String phone, String password, Integer role) {
        String password_hash = hashPassword(password);
        String phoneNumber = phone.replaceAll("[^\\d]", "");

        editTextName = findViewById(R.id.editTextFullname);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextCode = findViewById(R.id.editTextCode);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextPassword = findViewById(R.id.editTextPassword);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", "registrar", "regpass");
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT register_new_user(?, ?, ?, ?, ?);");
                    preparedStatement.setString(1, fio);
                    preparedStatement.setString(2, email);
                    preparedStatement.setString(3, phoneNumber);
                    preparedStatement.setString(4, password_hash);
                    preparedStatement.setInt(5, role);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    if(resultSet.getBoolean("register_new_user")){
                        showErrorFromOtherThread(getResources().getString(R.string.register_success_msg));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switchToLogin.callOnClick();
                            }
                        });
                    } else {
                        showErrorFromOtherThread(getResources().getString(R.string.register_error_msg));
                    }
                    connection.close();
                } catch (SQLException e) {
                    showErrorFromOtherThread(getResources().getString(R.string.register_error_msg));
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


    //отправка данных на сервер для логина
    private void login_user(String phone, String password){
        String password_hash = hashPassword(password);
        String phoneNumber = phone.replaceAll("[^\\d]", "");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = DriverManager.getConnection("jdbc:postgresql://192.168.31.12:5432/knoops", "registrar", "regpass");
                    PreparedStatement preparedStatement = connection.prepareStatement("SELECT login_user(?, ?);");
                    preparedStatement.setString(1, phoneNumber);
                    preparedStatement.setString(2, password_hash);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    connection.close();
                    if(resultSet.getInt("login_user") != 0){
                        User.setPhone(phoneNumber);
                        User.setPassword(password_hash);
                        User.setRole((byte) resultSet.getInt("login_user"));
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    } else {
                        showErrorFromOtherThread(getResources().getString(R.string.login_error_msg));
                    }
                } catch (SQLException e) {
                    showErrorFromOtherThread(getResources().getString(R.string.login_error_msg));
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

    //хэширование пароля
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    // Метод для отображения сообщения об ошибке с анимацией
    private void showError(String msgText) {
        if (!isErrorVisible) {
            Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_top);
            LinearLayout errorLayout = findViewById(R.id.errorLayout);
            ((TextView)errorLayout.getChildAt(0)).setText(msgText);
            errorLayout.setVisibility(View.VISIBLE);
            isErrorVisible = true;
            errorLayout.startAnimation(slideIn);
            long delayMillis = 2000;
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    hideError();
                }
            }, delayMillis);
        }
    }

    // Метод для скрытия сообщения об ошибке с анимацией
    private void hideError() {
        if (isErrorVisible) {
            Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_top);
            LinearLayout errorLayout = findViewById(R.id.errorLayout);
            errorLayout.startAnimation(slideOut);
            slideOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    errorLayout.setVisibility(View.GONE);
                    isErrorVisible = false;
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }
    //метод для отпраки сообщений из другого потока
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_ERROR_MESSAGE) {
                showError((String) msg.obj);
            }
        }
    };

    private void showErrorFromOtherThread(String msgText) {
        Message message = handler.obtainMessage(SHOW_ERROR_MESSAGE, msgText);
        handler.sendMessage(message);
    }
}