package org.example.Services;

import com.google.gson.*;
import org.example.config.ApiConfig;
import org.example.entities.EvenementMondialDTO;

import java.net.URI;
import java.net.http.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {

    public List<EvenementMondialDTO> getEvenementsOrientation() throws Exception {
        String url = ApiConfig.getUrl()
                + "?category=academic,conferences,expos,community"
                + "&q=orientation"
                + "&limit=20"
                + "&sort=start";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + ApiConfig.getToken())
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erreur API " + response.statusCode()
                    + " → " + response.body());
        }

        return parseResults(response.body());
    }

    private List<EvenementMondialDTO> parseResults(String json) {
        List<EvenementMondialDTO> list = new ArrayList<>();
        Gson gson = new Gson();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.getAsJsonArray("results");

        for (JsonElement el : results) {
            EvenementMondialDTO dto = gson.fromJson(el, EvenementMondialDTO.class);
            list.add(dto);
        }
        return list;
    }
}