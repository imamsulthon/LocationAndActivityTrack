package com.google.android.gms.location.sample.locationupdatespendingintent.view;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import com.google.android.gms.location.sample.locationupdatespendingintent.R;
import com.google.android.gms.location.sample.locationupdatespendingintent.Utils;
import com.google.android.gms.location.sample.locationupdatespendingintent.view.LoginActivity;
import com.google.android.gms.location.sample.locationupdatespendingintent.view.MainActivity;

public class SplashScreenActivity extends AppCompatActivity {

    boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash_screen);

        isLogin = Utils.getLoggedIn(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLogin) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }
        }, 2000L);
    }
}