package com.android.instapost;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button loginButton;
    Button registerButton;
    EditText email;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginButton = (Button) findViewById(R.id.btn_login);
        registerButton = (Button) findViewById(R.id.btn_register);
        email = (EditText) findViewById(R.id.user_email);
        password = (EditText) findViewById(R.id.password);
        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                loginUser();
                break;
            case R.id.btn_register:
                registerUser();
                break;
        }
    }

    private void loginUser() {
    }

    private void registerUser() {
        Intent intent = new Intent(this, Register.class);
        startActivityForResult(intent, 1);
    }
}
