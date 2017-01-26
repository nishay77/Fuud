package com.nishay.fuud;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.nishay.fuud.sql.SQLHelper;


public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new SQLHelper(this).getWritableDatabase();  //just to initiate first time

        new CountDownTimer(1500, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                Log.v(this.getClass().getSimpleName(),"ending splash");
                finish();
            }
        }.start();
    }


}
