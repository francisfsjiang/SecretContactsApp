package com.never.secretcontacts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private UserRegisterTask auth_task_ = null;

    // UI references.
    private EditText email_view_;
    private EditText passwd_view_;
    private View progress_view_;
    private View login_form_view_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        email_view_ = (EditText) findViewById(R.id.email);

        passwd_view_ = (EditText) findViewById(R.id.password);
        passwd_view_.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        login_form_view_ = findViewById(R.id.login_form);
        progress_view_ = findViewById(R.id.login_progress);
    }

    private void attemptRegister() {
        if (auth_task_ != null) {
            return;
        }

        // Reset errors.
        email_view_.setError(null);
        passwd_view_.setError(null);

        // Store values at the time of the login attempt.
        String email = email_view_.getText().toString();
        String password = passwd_view_.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwd_view_.setError(getString(R.string.error_invalid_password));
            focusView = passwd_view_;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            email_view_.setError(getString(R.string.error_field_required));
            focusView = email_view_;
            cancel = true;
        } else if (!isEmailValid(email)) {
            email_view_.setError(getString(R.string.error_invalid_email));
            focusView = email_view_;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            auth_task_ = new UserRegisterTask(email, password);
            auth_task_.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            login_form_view_.setVisibility(show ? View.GONE : View.VISIBLE);
            login_form_view_.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_form_view_.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress_view_.setVisibility(show ? View.VISIBLE : View.GONE);
            progress_view_.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress_view_.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progress_view_.setVisibility(show ? View.VISIBLE : View.GONE);
            login_form_view_.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;
        private final String mPassword;

        UserRegisterTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                JSONObject json = new JSONObject();
                json.put("email", mEmail);
                json.put("passwd", mPassword);
                JSONObject resp_json = MyApp.HttpPostJson(MyApp.URL_REGISTER, json);
                if(resp_json == null) {
                    return -2;
                }
                int status_code = resp_json.getInt("status_code");
                Log.i("http", "code " + status_code);
                if(status_code == HttpURLConnection.HTTP_OK) {
                    MyApp.updateLoginStatus(
                            resp_json.getString("auth_id"),
                            resp_json.getString("auth_key")
                    );
                    if(MyApp.checkLoginStatus()) {
                        Log.i("account status", "register success");
                        return 1;
                    }
                }
                return -1;

            }
            catch (Exception e) {
                Log.e("network", "Network failed." + e.getMessage());
            }

            return -2;
        }

        @Override
        protected void onPostExecute(final Integer res) {
            auth_task_ = null;
            showProgress(false);

            if (res == 1) {
                Toast.makeText(RegisterActivity.this, "成功注册", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if (res == -1){
                email_view_.setError(getString(R.string.error_incorrect_register_before));
                email_view_.requestFocus();
            }
            else {
                Toast.makeText(RegisterActivity.this, "网络错误", Toast.LENGTH_SHORT).show();

            }
        }

        @Override
        protected void onCancelled() {
            auth_task_ = null;
            showProgress(false);
        }
    }
}

