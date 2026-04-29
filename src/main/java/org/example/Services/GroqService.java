package org.example.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GroqService {

    private static final String API_KEY = "";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.1-8b-instant";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Génère des questions mixtes (QCM + Vrai/Faux + Ouvertes) en JSON pur
     * directement compatible avec ChallengeUIController.
     */
    public String genererQuestionsJson(String titreChallenge,
                                       String descriptionChallenge,
                                       String niveau,
                                       int nbQuestions) throws IOException {

        // ── Calcul du mix de types ────────────────────────────
        int nbQcm       = (int) Math.ceil(nbQuestions * 0.5);   // 50% QCM
        int nbTrueFalse = (int) Math.ceil(nbQuestions * 0.3);   // 30% Vrai/Faux
        int nbOpen      = nbQuestions - nbQcm - nbTrueFalse;    // 20% Ouvertes

        String prompt =
                "Tu es un expert pédagogique. Génère exactement " + nbQuestions + " questions " +
                        "de niveau " + niveau + " pour le challenge : « " + titreChallenge + " ».\n" +
                        "Description : " + descriptionChallenge + "\n\n" +

                        "RÉPARTITION OBLIGATOIRE :\n" +
                        "- " + nbQcm       + " question(s) de type QCM (type: qcm)\n" +
                        "- " + nbTrueFalse + " question(s) de type Vrai/Faux (type: truefalse)\n" +
                        "- " + nbOpen      + " question(s) de type ouverte (type: open)\n\n" +

                        "RÈGLES STRICTES :\n" +
                        "1. Retourne UNIQUEMENT un objet JSON valide, rien d'autre\n" +
                        "2. Pas de texte avant ou après le JSON\n" +
                        "3. Pas de markdown, pas de ```json\n" +
                        "4. Respecte EXACTEMENT ce format :\n\n" +

                        "{\n" +
                        "  \"questions\": [\n" +
                        "    {\n" +
                        "      \"type\": \"qcm\",\n" +
                        "      \"question\": \"Texte de la question ?\",\n" +
                        "      \"choices\": [\"A) choix1\", \"B) choix2\", \"C) choix3\", \"D) choix4\"],\n" +
                        "      \"correct_answer\": \"A\",\n" +
                        "      \"explanation\": \"Explication courte\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"type\": \"truefalse\",\n" +
                        "      \"question\": \"Affirmation à évaluer ?\",\n" +
                        "      \"correct_answer\": true,\n" +
                        "      \"explanation\": \"Explication courte\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"type\": \"open\",\n" +
                        "      \"question\": \"Question ouverte ?\",\n" +
                        "      \"expected_keywords\": [\"mot1\", \"mot2\", \"mot3\"],\n" +
                        "      \"model_answer\": \"Réponse modèle complète\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";

        // ── Construction requête Groq ─────────────────────────
        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("temperature", 0.7);
        body.put("max_tokens", 4096);

        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);
        body.put("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        body.toString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Erreur Groq API : " + response.code()
                        + " - " + response.body().string());
            }

            String responseBody = response.body().string();
            JSONObject json     = new JSONObject(responseBody);
            String content      = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();

            // ── Nettoyer si Groq ajoute des backticks markdown ──
            if (content.startsWith("```")) {
                content = content
                        .replaceAll("^```json\\s*", "")
                        .replaceAll("^```\\s*", "")
                        .replaceAll("```\\s*$", "")
                        .trim();
            }

            // ── Valider que c'est du JSON valide ──────────────
            new JSONObject(content); // lève une exception si invalide

            return content;
        }
    }
}