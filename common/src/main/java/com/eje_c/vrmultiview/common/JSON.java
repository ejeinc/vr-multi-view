package com.eje_c.vrmultiview.common;

import com.google.gson.Gson;

public class JSON {
    private static final Gson GSON = new Gson();

    public static String stringify(Object obj) {
        return GSON.toJson(obj);
    }

    public static <T> T parse(String json, Class<T> cls) {
        return GSON.fromJson(json, cls);
    }
}
