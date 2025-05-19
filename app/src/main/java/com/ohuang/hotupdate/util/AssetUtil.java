package com.ohuang.hotupdate.util;

import android.content.Context;

import java.io.InputStream;

public class AssetUtil {

    public static String readText(Context context, String fileName) {
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int available = inputStream.available();
            byte[] bytes = new byte[available];
            inputStream.read(bytes);
            inputStream.close();
            return new String(bytes);
        } catch (Exception e) {

        }
        return "";
    }
}
