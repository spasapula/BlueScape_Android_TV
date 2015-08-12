package com.bluescape.util;

import java.util.HashMap;

public class ColorUtil {

    public static int[] getNativeColor(String colorString) {
        String color = colorString.substring(1);
        int[] ret = new int[3];
        for (int i = 0; i < 3; i++) {
            ret[i] = Integer.parseInt(color.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }


    public static float[] getNormalisedColor(int color[]) {
        float[] normalisedColor=new float[3];
        for (int i = 0; i < color.length; i++) {
            normalisedColor[i] = color[i] / 255.0f;
        }
        return normalisedColor;
    }


    //Method implementing Memoization
    private static HashMap whiteColorMapWithAlpha = new HashMap();
    public static float[] getWhiteColorFloatWithAlpha(float alpha) {

        if(whiteColorMapWithAlpha.get(alpha) != null) {
            return (float[]) whiteColorMapWithAlpha.get(alpha);
        } else {
            float[] whiteColorArray = {1f,1f,1f,alpha};
            whiteColorMapWithAlpha.put(alpha,whiteColorArray);
            return (float[]) whiteColorMapWithAlpha.get(alpha);
        }

    }
}
