package com.example.livetv;


import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class Server {
    private static final String URL = "https://telescope-5uyq.onrender.com";

    public static String getBackendUrl(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        String url = sharedPreferences.getString("SERVER_URL", "");
        return url;
    }
}
