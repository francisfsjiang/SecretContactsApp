package com.never.secretcontacts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {

    private EditText pin_password_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Boolean ret = requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
//        Log.i("full", ret.toString());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fullscreen);

        MyApp.init();


        pin_password_ = (EditText) findViewById(R.id.pin_password);
        pin_password_.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE) {
                    String pin_password = pin_password_.getText().toString();
                    Integer time = MyApp.getPinPasswordWrongTime();
                    if (MyApp.vaildatePinPassword(pin_password)){
                        MyApp.setPinPasswordWrongTime(0);
                        Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
                        FullscreenActivity.this.startActivity(intent);
                        FullscreenActivity.this.finish();
                        return true;
                    }
                    else {
                        time++;
                        MyApp.setPinPasswordWrongTime(time);
                        if (time > 5) {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "错误次数超过5次，应用已清空数据，请重新登录",
                                    Toast.LENGTH_LONG).show();
                            MyApp.clearAllData();
                            finish();
                        }
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "PIN码错误,已尝试" + time + "次，错误超过5次将自动清除应用内数据",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.havePinPassword()) {
            pin_password_.setVisibility(View.VISIBLE);
        }
        else {
            new Handler().postDelayed(new Runnable() {
                // 为了减少代码使用匿名Handler创建一个延时的调用
                public void run() {
                    Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
                    FullscreenActivity.this.startActivity(intent);
                    FullscreenActivity.this.finish();
                }
            }, 2000);
        }
    }

}
