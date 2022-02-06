package io.github.pronze.sba.util;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GsonUtils {
    private Gson instance = new Gson();

    public Gson gson() {
        return instance;
    }
}

