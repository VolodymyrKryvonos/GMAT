package com.deepinspire.gmatclub.utils;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.deepinspire.gmatclub.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmytro mytsko on 29.03.18.
 */
class FieldWatcher implements TextWatcher {
    private View view;
    private Activity activity;
    private View dialogLayout;

    FieldWatcher(View view, Activity activity, View dialogLayout) {
        this.view = view;
        this.activity = activity;
        this.dialogLayout = dialogLayout;
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    public void afterTextChanged(Editable editable) {

        TextView errorMessage = (TextView) dialogLayout.findViewById(R.id.errorMessage);

        switch(view.getId()) {
            case R.id.signInInputUsername:
            case R.id.signInInputPassword:
                TextView signInInputPasswordForgot = (TextView) dialogLayout.findViewById(R.id.signInInputPasswordForgot);

                EditText signInInputUsername = (EditText) dialogLayout.findViewById(R.id.signInInputUsername);
                EditText signInInputPassword = (EditText) dialogLayout.findViewById(R.id.signInInputPassword);

                Map<String, String> messages = new HashMap<String, String>(){{
                    put("login", "Incorrect Login");
                    put("password", "Incorrect Password");
                    put("loginpassword", "Incorrect Login and Password");
                }};

                StringBuffer keyError = new StringBuffer("");

                if(Validator.validUsername(signInInputUsername.getText().toString().trim())) {
                    signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp);
                } else {
                    keyError.append("login");
                }

                if(Validator.validPassword(signInInputPassword.getText().toString().trim())) {
                    signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp);
                    signInInputPasswordForgot.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.grey_A50));
                } else {
                    keyError.append("password");
                }

                String message = messages.get(keyError.toString());

                if(message == null) {
                    errorMessage.setVisibility(View.GONE);
                    errorMessage.setText("");
                }
                break;
            case R.id.forgotPasswordInputEmail:
                EditText forgotPasswordInputEmail = (EditText) dialogLayout.findViewById(R.id.forgotPasswordInputEmail);

                if(Validator.validEmail(forgotPasswordInputEmail.getText().toString().trim())) {
                    errorMessage.setVisibility(View.GONE);
                    errorMessage.setText("");
                    forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp);
                }
                break;
        }
    }
}