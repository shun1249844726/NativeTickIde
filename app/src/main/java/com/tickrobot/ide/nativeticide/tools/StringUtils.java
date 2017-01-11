package com.tickrobot.ide.nativeticide.tools;

/**
 * Created by xushun on 2017/1/9.
 */

public class StringUtils {
    public static String to2String(int input) {
        String temp = "";
        if (input < 16) {
            temp = ("0" + String.valueOf(Integer.toHexString(input & 0xff)));
        } else {
            temp = String.valueOf(Integer.toHexString(input & 0xff));
        }
        return temp;
    }
}
