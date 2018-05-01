package com.deepinspire.gmatclub.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dmytro mytsko on 29.03.18.
 */
public class Validator {
    public static boolean validUsername(String name) {
        return !(name == null || name.trim().isEmpty());
    }

    public static boolean validEmail(String email) {
        return !(email == null || email.trim().isEmpty()) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean validPassword(String password) {
        //String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        //String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
        String pattern = "^(?=.*[0-9])(?=.*[A-Z])(?=\\S+$).{8,}$";
        Pattern p = Pattern.compile(pattern);
        Matcher m  = p.matcher(password);

        return !(password == null || password.trim().isEmpty());//m.matches();
    }
}