package com.akotnana.pollr.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.utils.BackendUtils;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.VolleyCallback;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static android.view.View.GONE;

public class SignUpActivity extends AppCompatActivity {

    public String TAG = "SignUpActivity";

    EditText username;
    EditText password;
    EditText email;
    Button signUp;
    LinearLayout signIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        username = (EditText) findViewById(R.id.username_edit_text);
        password = (EditText) findViewById(R.id.password_edit_text);
        email = (EditText) findViewById(R.id.email_edit_text);
        signUp = (Button) findViewById(R.id.register_button);

        final String token = FirebaseInstanceId.getInstance().getToken();
        String realToken = "";
        if(token.equals("")) {
            realToken = new DataStorage(getApplicationContext()).getData("firebaseID");
        } else {
            realToken = token;
        }

        final String finalRealToken = realToken;
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validate()) {
                    onSignUpFailed();
                    return;
                }
                signUp.setEnabled(false);
                final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this,
                        R.style.AppTheme_Dark_Dialog);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");
                progressDialog.show();
                BackendUtils.doPostRequest("/api/v1/register", new HashMap<String, String>() {{
                    put("username", username.getText().toString());
                    String pass = password.getText().toString();
                    try {
                        pass = new DataStorage(getApplicationContext()).md5(pass);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    put("password", pass);
                    put("firebase_id", finalRealToken);
                }}, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        new DataStorage(getApplicationContext()).storeData("auth_token", result.trim());
                        onSignUpSuccess();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(JSONObject result) {

                    }

                    @Override
                    public void onError(VolleyError error) {

                        if (error == null || error.networkResponse == null) {
                            return;
                        }

                        String body = "";
                        //get status code here
                        final String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        try {
                            body = new String(error.networkResponse.data,"UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            // exception
                        }

                        Log.e(TAG, body + "\n");
                    }
                }, getApplicationContext());
            }
        });

        signIn = (LinearLayout) findViewById(R.id.sign_in_link);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String username = this.username.getText().toString();
        String email = this.email.getText().toString();
        String password = this.password.getText().toString();

        if (username.isEmpty()) {
            this.username.setError("enter a valid username");
            valid = false;
        } else {
            this.username.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("enter a valid email address");
            valid = false;
        } else {
            this.email.setError(null);
        }

        if (password.isEmpty() || password.length() < 4) {
            this.password.setError("password too short");
            valid = false;
        } else {
            this.password.setError(null);
        }

        return valid;
    }

    public void onSignUpFailed() {
        Toast.makeText(getBaseContext(), "Sign up failed", Toast.LENGTH_SHORT).show();
        signUp.setEnabled(true);
    }

    public void onSignUpSuccess() {
        signUp.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

}
