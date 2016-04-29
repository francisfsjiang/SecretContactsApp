package com.never.secretcontacts;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * A login screen that offers login via email/password.
 */
public class KeyActivity extends AppCompatActivity {

    private GetKeyTask key_task_ = null;

    // UI references.
    private View progress_view_;
    private View key_form_view_;

    private String recovery_key_;

    private Boolean is_key_got_;

    private TextView recovery_key_view_;
    private TextView recovery_key_hint_view_;

    private Button key_get_button_;
    private Button key_recover_button_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key);


        key_get_button_ = (Button) findViewById(R.id.key_get_button);
        key_get_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptGetKey("");
            }
        });

        key_recover_button_ = (Button) findViewById(R.id.key_recover_button);
        key_recover_button_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(KeyActivity.this);
                builder.setTitle("请输入您记下的密钥恢复密码");

                TextInputLayout input_layout =(TextInputLayout) getLayoutInflater().inflate(
                        R.layout.dialog_change_contact_name,
                        null
                );
                final EditText edit_text = (EditText)input_layout.getChildAt(0);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        attemptGetKey(edit_text.getText().toString());
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setView(input_layout);
                builder.show();
            }
        });

        key_form_view_ = findViewById(R.id.key_form);
        progress_view_ = findViewById(R.id.key_progress);
        recovery_key_view_ = (TextView)findViewById(R.id.recovery_key_view);
        recovery_key_hint_view_ = (TextView)findViewById(R.id.recovery_key_hint);

        recovery_key_view_.setVisibility(View.GONE);
        recovery_key_hint_view_.setVisibility(View.GONE);

        is_key_got_ = false;
    }

    private void attemptGetKey(String recovery_key) {
        if (key_task_ != null) {
            return;
        }

        boolean cancel = false;
        View focusView = null;


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            key_task_ = new GetKeyTask(recovery_key);
            key_task_.execute((Void) null);
        }
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

            key_form_view_.setVisibility(show ? View.GONE : View.VISIBLE);
            key_form_view_.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    key_form_view_.setVisibility(show ? View.GONE : View.VISIBLE);
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
            key_form_view_.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (is_key_got_) {

            recovery_key_view_.setVisibility(View.GONE);

            AlertDialog.Builder builder = new AlertDialog.Builder(KeyActivity.this);
            builder.setTitle("请输入您记下的密钥恢复密码");

            TextInputLayout input_layout =(TextInputLayout) getLayoutInflater().inflate(
                    R.layout.dialog_change_contact_name,
                    null
            );
            final EditText edit_text = (EditText)input_layout.getChildAt(0);
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (recovery_key_.equals(edit_text.getText().toString())) {
                        KeyActivity.this.finish();
                    }
                    else {
                        recovery_key_view_.setVisibility(View.VISIBLE);
                        Toast.makeText(KeyActivity.this, "密钥恢复密码错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setView(input_layout);
            builder.show();
        }
        else{
            this.finish();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class GetKeyTask extends AsyncTask<Void, Void, Integer> {

        private String input_recovery_key_;

        GetKeyTask(String recovery_key) {
            input_recovery_key_ = recovery_key;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                JSONObject json = MyApp.getAuthJson();
                if(json == null) {
                    return -1;
                }
                if(!input_recovery_key_.equals("")) {
                    json.put("recover", true);
                    json.put("recovery_key", input_recovery_key_);
                    JSONObject resp_json = MyApp.HttpPostJson(MyApp.URL_KEY, json);
                    if(resp_json == null) {
                        return -2;
                    }
                    int status_code = resp_json.getInt("status_code");
                    Log.i("http", "code " + status_code);
                    if(status_code == HttpURLConnection.HTTP_OK) {
                        Log.i("key", "recover key success. ");
                        Log.i("key", resp_json.getString("pri_key"));
                        Log.i("key", resp_json.getString("pub_key"));
                        Log.i("key", resp_json.getString("recovery_key"));
                        MyApp.key_manager_.saveKeyPair(
                                resp_json.getString("pri_key"),
                                resp_json.getString("pub_key")
                        );
                        return 1;
                    }
                    else if (status_code == HttpURLConnection.HTTP_FORBIDDEN) {
                        return -1;
                    }
                    else if (status_code == HttpURLConnection.HTTP_BAD_REQUEST) {
                        return -4;
                    }
                    else {
                        return -2;
                    }
                }
                else {
                    json.put("recover", false);
                    JSONObject resp_json = MyApp.HttpPostJson(MyApp.URL_KEY, json);
                    if(resp_json == null) {
                        return -2;
                    }
                    int status_code = resp_json.getInt("status_code");
                    Log.i("http", "code " + status_code);
                    if(status_code == HttpURLConnection.HTTP_OK) {
                        Log.i("key", "get key success. ");
                        Log.i("key", resp_json.getString("pri_key"));
                        Log.i("key", resp_json.getString("pub_key"));
                        Log.i("key", resp_json.getString("recovery_key"));
                        MyApp.key_manager_.saveKeyPair(
                                resp_json.getString("pri_key"),
                                resp_json.getString("pub_key")
                        );
                        recovery_key_ = resp_json.getString("recovery_key");
                        return 1;
                    }
                    else if (status_code == HttpURLConnection.HTTP_FORBIDDEN) {
                        return -1;
                    }
                    else if (status_code == HttpURLConnection.HTTP_CONFLICT) {
                        return -3;
                    }
                    else {
                        return -2;
                    }
                }
            }
            catch (Exception e) {
                Log.e("network", "Network failed." + e.getMessage());
            }

            return -2;
        }

        @Override
        protected void onPostExecute(final Integer res) {
            key_task_ = null;
            showProgress(false);

            if (res == 1) {
                Toast.makeText(KeyActivity.this, "成功获取密钥", Toast.LENGTH_SHORT).show();
                recovery_key_view_.setText(recovery_key_);
                recovery_key_view_.setVisibility(View.VISIBLE);
                recovery_key_hint_view_.setVisibility(View.VISIBLE);
                key_get_button_.setVisibility(View.GONE);
                key_recover_button_.setVisibility(View.GONE);
                is_key_got_ = true;
                if (!input_recovery_key_.equals("")) {
                    KeyActivity.this.finish();
                }
            }
            else if (res == -1){
                Toast.makeText(KeyActivity.this, "用户验证失败，请重新验证", Toast.LENGTH_SHORT).show();
            }
            else if (res == -2){
                Toast.makeText(KeyActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
            else if (res == -3){
                Toast.makeText(KeyActivity.this, "该账户已设有密钥，请输入您的密钥恢复密码", Toast.LENGTH_LONG).show();
            }
            else if (res == -4){
                Toast.makeText(KeyActivity.this, "密钥恢复密码错误", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(KeyActivity.this, "未知错误", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            key_task_ = null;
            showProgress(false);
        }
    }
}

