package com.neotys.ps.fix.send.message;

public class SendMessageUtils {
    public static long getDelay(String log_line) {
        //Get the characters before the 1st comma
        return Long.parseLong(log_line.split(",")[0]);
    }
    public static String getMessage(String log_line) {
        //Get the characters before the 1st comma
        return log_line.split(",")[1];
    }
}
