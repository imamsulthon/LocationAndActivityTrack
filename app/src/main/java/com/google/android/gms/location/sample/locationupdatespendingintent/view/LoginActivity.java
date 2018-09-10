package com.google.android.gms.location.sample.locationupdatespendingintent.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.sample.locationupdatespendingintent.R;
import com.google.android.gms.location.sample.locationupdatespendingintent.Utils;

public class LoginActivity extends AppCompatActivity {

    String user_1 = "driver1";
    String user_2 = "driver2";
    String password_1 = "goelf1";
    String password_2 = "goelf2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        final EditText inputUserName = findViewById(R.id.input_username);
        final EditText inputPassword = findViewById(R.id.input_password);
        Button loginButton = findViewById(R.id.btn_login);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = inputUserName.getText().toString();
                String password = inputPassword.getText().toString();

                if (!username.isEmpty() && !password.isEmpty()) {
                    if (validate(username, password)) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        Utils.setLoggedIn(getApplicationContext(), true);
                        Utils.setFirstLoginState(getApplicationContext(), true);
                        LoginActivity.this.finish();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "username & password harus diisi", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    boolean validate(String username, String password) {
        if (username.equals(user_1) && password.equals(password_1)) {
            Toast.makeText(this, "You are Loggin as Driver 1", Toast.LENGTH_LONG).show();
            Utils.setUsername(this, 1);
            return true;
        } else if (username.equals(user_2) && password.equals(password_2)) {
            Toast.makeText(this, "You are Loggin as Driver 2", Toast.LENGTH_LONG).show();
            Utils.setUsername(this,2 );
            return true;
        } else {
            Toast.makeText(this, "username & password anda salah", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
