package com.example.insite.app.model;

import android.app.Application;

/**
 * Created by Xing Yi on 7/4/2015.
 */
public class AppSetting extends Application {

    public static String APItoken = "51d3b1d3beb959685da8fa662de3948a";
    public static String baseUrl = "http://192.168.1.5/insite/v1/issue";
    public static String imagePostUrl = "http://192.168.1.5/insite/v1/image";

    private static AppSetting singleton;

    public static AppSetting getInstance() {
        return singleton;
    }
}
