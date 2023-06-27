package com.mm.android.mobilecommon.pps;

import android.content.Context;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GlobalFunc {

    public static final Pattern EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final Pattern PASSWOLD_REGEX_ALPHA_NUM = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{8,}$");

    public static void logout() {
    }

    public static boolean validatePass(String passStr) {
        Matcher matcher = PASSWOLD_REGEX_ALPHA_NUM.matcher(passStr);
        return matcher.find();
    }

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static void showToast(Context context, String text){
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
    }
}