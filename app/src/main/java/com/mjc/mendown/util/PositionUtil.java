package com.mjc.mendown.util;

/**
 * Created by mjc on 2016/2/29.
 */
public class PositionUtil {

    public static int getRangeX(int screenWidth) {
        double rate = Math.random();
        while (rate>=0.75){
            rate = Math.random();
        }
        return (int) (screenWidth * rate);
    }
}
