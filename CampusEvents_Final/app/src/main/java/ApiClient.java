package com.campusevents.app;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ApiClient {
    // Update this if your laptop's Wi-Fi IPv4 address changes.
    public static final String BASE_URL = "http://192.168.1.6:5000";
    private ApiClient() {}

    public static JSONObject request(String method, String path, JSONObject body) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
        c.setRequestMethod(method);
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestProperty("Accept", "application/json");
        if (body != null) {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            try (OutputStream out = c.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
        int code = c.getResponseCode();
        InputStream stream = code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream();
        StringBuilder result = new StringBuilder();
        if (stream != null) try (BufferedReader r = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line; while ((line = r.readLine()) != null) result.append(line);
        }
        c.disconnect();
        JSONObject json = new JSONObject(result.toString());
        if (code < 200 || code >= 300) throw new IOException(json.optString("message", "HTTP " + code));
        return json;
    }
}
