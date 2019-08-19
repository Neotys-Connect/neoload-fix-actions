package com.neotys.ps.fix.common;

public class FIXUtils {
    private static final String PRINT_SEPARATOR = "|";
    private static final String FIX_FIELD_SEPARATOR = "\001";

    public static String printFIXMessage(String FIXmessage){
        return FIXmessage.replace(FIX_FIELD_SEPARATOR, PRINT_SEPARATOR);
    }
}
