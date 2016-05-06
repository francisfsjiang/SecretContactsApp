package com.never.secretcontacts;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * A login screen that offers login via email/password.
 */
public class PinPasswordSetActivity extends AppCompatActivity {


    // UI references.
    private EditText pin_old_view_;
    private EditText pin_new_view_;
    private EditText pin_new_again_view_;

    private Button set_pin_password_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_password_set);
        // Set up the login form.
        pin_old_view_ = (EditText) findViewById(R.id.pin_old);
        pin_new_view_ = (EditText) findViewById(R.id.pin_new);
        pin_new_again_view_ = (EditText) findViewById(R.id.pin_new_again);

        if (!MyApp.havePinPassword()) {
            pin_old_view_.setVisibility(View.GONE);
        }

        set_pin_password_ = (Button) findViewById(R.id.set_pin_password);
        set_pin_password_.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSet();
            }
        });

    }

    private void attemptSet() {

        // Reset errors.
        pin_old_view_.setError(null);
        pin_new_view_.setError(null);
        pin_new_again_view_.setError(null);

        // Store values at the time of the login attempt.
        String pin_old= pin_old_view_.getText().toString();
        String pin_new = pin_new_view_.getText().toString();
        String pin_new_again = pin_new_again_view_.getText().toString();
        if (!MyApp.havePinPassword() || MyApp.vaildatePinPassword(pin_old)) {
            if (isPinPasswordValid(pin_new)) {
                if (pin_new.equals(pin_new_again)) {
                    MyApp.setPinPassword(pin_new);
                    MyApp.setPinPasswordWrongTime(0);
                    finish();
                }
                else {
                    pin_new_again_view_.setError(getString(R.string.error_pin_password_match));
                }
            }
            else {
                pin_new_view_.setError(getString(R.string.error_pin_password));
            }
        }
        else {
            pin_old_view_.setError(getString(R.string.error_pin_old_password));
        }
    }


    private boolean isPinPasswordValid(String pin_password) {
        return pin_password.length() >= 6;
    }



}

