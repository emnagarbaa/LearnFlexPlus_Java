package org.example.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ServiceIA {

    private static final String API_KEY = "GROQ_API_KEY"; // ← ta clé
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL   = "llama-3.1-8b-instant";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * Génère un feedback personnalisé après un challenge.
     *
     * @param titreChallenge   titre du challenge
     * @param score            score obtenu (0-100)
     * @param badge            badge obtenu (Expert, Compétent, Apprenti, Novice)
     * @param objectif         objectif du challenge (ex: 80)
     * @param niveau           niveau recommandé (facile, moyen, difficile)
     * @param nbQuestions      nombre total de questions
     */
    public String genererFeedback(String titreChallenge,
                                  int    score,
                                  String badge,
                                  int    objectif,
                                  String niveau,
                                  int    nbQuestions) throws IOException {

        boolean objectifAtteint = score >= objectif;

        String prompt =
                "Tu es un coach pédagogique bienveillant et motivant. " +
                        "Un étudiant vient de terminer le challenge « " + titreChallenge + " ».\n\n" +

                        "RÉSULTATS :\n" +
                        "- Score obtenu : " + score + "%\n" +
                        "- Objectif fixé : " + objectif + "%\n" +
                        "- Objectif " + (objectifAtteint ? "✅ ATTEINT" : "❌ NON ATTEINT") + "\n" +
                        "- Badge obtenu : " + badge + "\n" +
                        "- Niveau recommandé : " + niveau + "\n" +
                        "- Nombre de questions : " + nbQuestions + "\n\n" +

                        "INSTRUCTIONS :\n" +
                        "1. Retourne UNIQUEMENT un objet JSON valide, rien d'autre\n" +
                        "2. Pas de texte avant ou après le JSON\n" +
                        "3. Pas de markdown, pas de ```json\n" +
                        "4. Respecte EXACTEMENT ce format :\n\n" +

                        "{\n" +
                        "  \"titre\": \"Titre court et motivant du feedback (max 8 mots)\",\n" +
                        "  \"message\": \"Message personnalisé de 2-3 phrases selon le score et l'objectif\",\n" +
                        "  \"points_forts\": [\"point fort 1\", \"point fort 2\"],\n" +
                        "  \"axes_amelioration\": [\"conseil 1\", \"conseil 2\"],\n" +
                        "  \"conseil_final\": \"Un conseil motivant et personnalisé pour progresser\"\n" +
                        "}";

        // ── Construction requête Groq ─────────────────────────
        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("temperature", 0.8);
        body.put("max_tokens", 1024);

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

            // ── Nettoyer si Groq ajoute des backticks markdown ─
            if (content.startsWith("```")) {
                content = content
                        .replaceAll("^```json\\s*", "")
                        .replaceAll("^```\\s*",     "")
                        .replaceAll("```\\s*$",     "")
                        .trim();
            }

            // ── Valider que c'est du JSON valide ──────────────
            new JSONObject(content);

            return content;
        }
    }
}