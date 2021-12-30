package de.ximanton.discordverification;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public abstract class MojangAPI {

    private static final String playerUUIDEndpoint = "https://api.mojang.com/users/profiles/minecraft/";

    /**
     * Fetches the uuid for a playername from the mojang api
     * @param playerName the player to fetch
     * @return UUID-String if player has been found, null if not
     */
    public static String getPlayerUUID(String playerName) {
        URL url;
        try {
            url = new URL(playerUUIDEndpoint + playerName);
            // open connection to the mojang api endpoint
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
                // Read the response content and append it to a string
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
