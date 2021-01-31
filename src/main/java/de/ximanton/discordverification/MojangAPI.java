package de.ximanton.discordverification;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public abstract class MojangAPI {

    private static final String playerUUIDEndpoint = "https://api.mojang.com/users/profiles/minecraft/";

    public static String getPlayerUUID(String playerName) {
        URL url;
        try {
            url = new URL(playerUUIDEndpoint + playerName);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            String r = getResponseContent(con);
            if (r == null) return null;
            if (r.isEmpty()) return null;
            JsonElement id = new JsonParser().parse(r).getAsJsonObject().get("id");
            if (id == null) return null;
            return id.getAsString();
        } catch (IOException ignored) {}
        return null;
    }

    private static String getResponseContent(HttpsURLConnection c) {
        StringBuilder s = new StringBuilder();
        if (c != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                String input;
                while ((input = br.readLine()) != null) {
                    s.append(input);
                }
                br.close();
                return s.toString();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

}
