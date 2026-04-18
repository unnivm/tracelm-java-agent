package org.usbtechno.deve;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

public class TraceSender {

    public static void send(String json) {

        try {
            URL url = new URL("http://localhost:8080/traces");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
            }

            int responseCode = conn.getResponseCode();
            System.out.println("📡 Sent trace, response: " + responseCode);

        } catch (Exception e) {
            System.out.println("❌ Failed to send trace");
            e.printStackTrace();
        }

    }
}