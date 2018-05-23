package com.example.xyzreader.remote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class RemoteEndpointUtil {

    private RemoteEndpointUtil() {
    }

    public static JSONArray fetchJsonArray() {
        String itemsJson;
        try {
            itemsJson = fetchPlainText();
        } catch (IOException e) {
            Timber.e(e, "Error fetching items JSON");
            return null;
        }

        // Parse JSON
        try {
            JSONTokener tokener = new JSONTokener(itemsJson);
            Object val = tokener.nextValue();
            if (!(val instanceof JSONArray)) {
                throw new JSONException("Expected JSONArray");
            }
            return (JSONArray) val;
        } catch (JSONException e) {
            Timber.e(e, "Error parsing items JSON");
        }

        return null;
    }

    private static String fetchPlainText() throws IOException {
        final OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder()
                .url(Config.BASE_URL)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
