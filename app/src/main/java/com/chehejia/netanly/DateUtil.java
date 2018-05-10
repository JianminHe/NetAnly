package com.chehejia.netanly;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chj1090 on 2018/5/9.
 */

public class DateUtil {

    public static long getDate(int year, int month, int day) {

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        return cal.getTimeInMillis();
    }

    public static long getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, hour, minute, second);
        return cal.getTimeInMillis();
    }

    public static long stringToDate(String strTime) {
        Date date = null;

        try {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        date = formatter.parse(strTime);
        }
        catch (ParseException ex) {
            Log.e("NetAnly", ex.getMessage().toString());
            date = new Date();
        } finally {
            return date.getTime();
        }

    }

    public static String longToDate(long lo){
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sd.format(date);
    }


}
