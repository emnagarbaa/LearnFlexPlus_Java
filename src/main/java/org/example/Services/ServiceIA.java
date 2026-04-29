package org.example.Services;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServiceIA {

    // ⚠️ REMPLACE AVEC TA CLÉ API GROQ
    private static final String API_KEY = " ";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";   // Recommandé, très performant

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * Génère un feedback avec analyse
     */
    public String genererFeedbackAvecAnalyse(String titreChallenge,
                                             int score,
                                             String badge,
                                             int objectif,
                                             String niveau,
                                             int nbQuestions,
                                             Map<String, Object> analyse) throws IOException {

        // Fallback direct si pas de clé API
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("gsk_VOTRE_CLE_API_ICI")) {
            System.out.println("📝 Utilisation du feedback local (pas de clé API)");
            return getLocalFeedback(score, analyse);
        }

        try {
            int reussies = (int) analyse.getOrDefault("reussies", 0);
            int ratees = (int) analyse.getOrDefault("ratees", 0);

            @SuppressWarnings("unchecked")
            List<String> categoriesFaibles = (List<String>) analyse.get("categories_faibles");

            // Construction du message utilisateur
            StringBuilder userContent = new StringBuilder();
            userContent.append("Tu es un coach pédagogique. Donne un feedback à un étudiant.\n\n");
            userContent.append("CHALLENGE: ").append(titreChallenge).append("\n");
            userContent.append("SCORE: ").append(score).append("%\n");
            userContent.append("OBJECTIF: ").append(objectif).append("%\n");
            userContent.append("BADGE: ").append(badge).append("\n");
            userContent.append("RÉUSSITES: ").append(reussies).append("/").append(nbQuestions).append("\n");
            userContent.append("ÉCHECS: ").append(ratees).append("/").append(nbQuestions).append("\n");

            if (categoriesFaibles != null && !categoriesFaibles.isEmpty()) {
                userContent.append("CATÉGORIES FAIBLES: ").append(String.join(", ", categoriesFaibles)).append("\n");
            }

            userContent.append("\nRéponds UNIQUEMENT avec ce JSON, rien d'autre:\n");
            userContent.append("""
                {
                    "titre": "titre court",
                    "message": "message de 2-3 phrases",
                    "points_forts": ["point1", "point2"],
                    "axes_amelioration": ["axe1", "axe2"],
                    "conseil_final": "conseil",
                    "recommandations_challenges": [
                        {"titre": "challenge 1", "niveau": "facile", "raison": "pourquoi", "thematique": "quoi"}
                    ]
                }
                """);

            // Construction de la requête
            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("temperature", 0.7);
            body.put("max_tokens", 800);
            body.put("top_p", 0.9);

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "Tu es un coach pédagogique expert. Tu réponds toujours en JSON valide.");
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userContent.toString());
            messages.put(userMessage);

            body.put("messages", messages);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                String responseBody = response.body() != null ? response.body().string() : "";

                if (code == 200) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String content = json.getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        // Nettoyer le contenu
                        content = content.trim();
                        if (content.startsWith("```json")) {
                            content = content.substring(7);
                        }
                        if (content.startsWith("```")) {
                            content = content.substring(3);
                        }
                        if (content.endsWith("```")) {
                            content = content.substring(0, content.length() - 3);
                        }
                        content = content.trim();

                        // Vérifier que c'est du JSON valide
                        new JSONObject(content);
                        return content;

                    } catch (Exception e) {
                        System.err.println("Erreur parsing JSON: " + e.getMessage());
                        return getLocalFeedback(score, analyse);
                    }
                } else {
                    System.err.println("⚠️ Erreur API " + code + ": " + responseBody);
                    return getLocalFeedback(score, analyse);
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Exception: " + e.getMessage());
            return getLocalFeedback(score, analyse);
        }
    }
    /**
     * Détecte si un texte est généré par IA
     * Retourne un JSONObject avec: probabilite, verdict, indices, explication
     */
    public String detecterIA(String texte) throws IOException {
        if (API_KEY == null || API_KEY.isEmpty()) {
            return "{\"verdict\":\"Inconnu\",\"probabilite\":0,\"explication\":\"Clé API manquante.\",\"indices\":[]}";
        }

        String prompt = """
        Analyse ce texte et détermine s'il a été généré par une IA.
        Réponds UNIQUEMENT avec ce JSON, rien d'autre :
        {
            "probabilite": (nombre entre 0 et 100),
            "verdict": "IA" ou "Humain" ou "Incertain",
            "indices": ["indice1", "indice2", "indice3"],
            "explication": "explication courte en français"
        }
        
        TEXTE À ANALYSER:
        """ + texte;

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("temperature", 0.2);
        body.put("max_tokens", 400);

        JSONArray messages = new JSONArray();
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", "Tu es un expert en détection de textes générés par IA. Tu réponds toujours en JSON valide uniquement.");
        messages.put(system);

        JSONObject user = new JSONObject();
        user.put("role", "user");
        user.put("content", prompt);
        messages.put(user);

        body.put("messages", messages);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (response.code() == 200) {
                String content = new JSONObject(responseBody)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                        .replaceAll("```json|```", "")
                        .trim();
                new JSONObject(content); // validation
                return content;
            }
        }
        return "{\"verdict\":\"Inconnu\",\"probabilite\":0,\"explication\":\"Erreur API.\",\"indices\":[]}";
    }
    /**
     * Feedback local (ne nécessite pas d'API)
     */
    private String getLocalFeedback(int score, Map<String, Object> analyse) {
        @SuppressWarnings("unchecked")
        List<String> categoriesFaibles = (List<String>) analyse.get("categories_faibles");
        int reussies = (int) analyse.getOrDefault("reussies", 0);
        int ratees = (int) analyse.getOrDefault("ratees", 0);
        int total = reussies + ratees;

        JSONObject feedback = new JSONObject();
        JSONArray recommandations = new JSONArray();

        if (score >= 80) {
            feedback.put("titre", "🏆 Excellent ! Maîtrise parfaite");
            feedback.put("message", String.format("Félicitations ! Tu as obtenu %d/%d (%d%%). Continue sur cette excellente lancée !", reussies, total, score));
            feedback.put("points_forts", new JSONArray().put("Maîtrise exceptionnelle du sujet").put("Réponses précises").put("Très bonne compréhension"));
            feedback.put("axes_amelioration", new JSONArray().put("Passe aux challenges avancés").put("Approfondis les concepts complexes"));
            feedback.put("conseil_final", "Tu es prêt pour le niveau expert ! Relève de nouveaux défis.");

            JSONObject reco1 = new JSONObject();
            reco1.put("titre", "Challenge Expert");
            reco1.put("niveau", "difficile");
            reco1.put("raison", "Pour aller plus loin");
            reco1.put("thematique", "Perfectionnement");
            recommandations.put(reco1);

        } else if (score >= 60) {
            feedback.put("titre", "📈 Bon travail !");
            feedback.put("message", String.format("Tu as obtenu %d/%d (%d%%). Continue comme ça, tu progresses bien !", reussies, total, score));
            feedback.put("points_forts", new JSONArray().put("Bonne compréhension globale").put("Effort constant"));

            JSONArray axes = new JSONArray();
            if (categoriesFaibles != null && !categoriesFaibles.isEmpty()) {
                for (String cat : categoriesFaibles) {
                    axes.put("Revoir: " + cat);
                }
            } else {
                axes.put("Revise les concepts moins maîtrisés");
                axes.put("Pratique régulièrement");
            }
            feedback.put("axes_amelioration", axes);
            feedback.put("conseil_final", "Identifie tes points faibles et concentre-toi dessus, tu vas progresser rapidement !");

            JSONObject reco1 = new JSONObject();
            reco1.put("titre", "Challenge Pratique");
            reco1.put("niveau", "moyen");
            reco1.put("raison", "Pour renforcer tes compétences");
            reco1.put("thematique", "Application");
            recommandations.put(reco1);

        } else {
            feedback.put("titre", "💪 Continue tes efforts !");
            feedback.put("message", String.format("Tu as obtenu %d/%d (%d%%). Ce challenge t'a permis d'identifier tes axes de progression.", reussies, total, score));
            feedback.put("points_forts", new JSONArray().put("Participation active").put("Volonté d'apprendre"));
            feedback.put("axes_amelioration", new JSONArray().put("Revoir les fondamentaux").put("Commencer par des challenges plus faciles"));
            feedback.put("conseil_final", "Ne te décourage pas ! Commence par les bases et progresse étape par étape.");

            JSONObject reco1 = new JSONObject();
            reco1.put("titre", "Challenge Initiation");
            reco1.put("niveau", "facile");
            reco1.put("raison", "Pour consolider les bases");
            reco1.put("thematique", "Fondamentaux");
            recommandations.put(reco1);
        }

        feedback.put("recommandations_challenges", recommandations);
        return feedback.toString();
    }

    /**
     * Feedback simple (version originale)
     */
    public String genererFeedback(String titreChallenge,
                                  int score,
                                  String badge,
                                  int objectif,
                                  String niveau,
                                  int nbQuestions) throws IOException {

        JSONObject feedback = new JSONObject();

        if (score >= 80) {
            feedback.put("titre", "🏆 Excellent travail !");
            feedback.put("message", "Félicitations ! Tu as excellé dans ce challenge.");
            feedback.put("points_forts", new JSONArray().put("Maîtrise parfaite").put("Réponses précises"));
            feedback.put("axes_amelioration", new JSONArray().put("Passe au niveau supérieur"));
            feedback.put("conseil_final", "Continue sur cette lancée !");
        } else if (score >= 60) {
            feedback.put("titre", "📈 Bon travail !");
            feedback.put("message", "Tu as obtenu un bon score. Continue à pratiquer !");
            feedback.put("points_forts", new JSONArray().put("Bonne compréhension").put("Effort constant"));
            feedback.put("axes_amelioration", new JSONArray().put("Revise les bases").put("Pratique régulièrement"));
            feedback.put("conseil_final", "Tu es sur la bonne voie !");
        } else {
            feedback.put("titre", "💪 Continue tes efforts !");
            feedback.put("message", "Ce challenge t'a aidé à identifier tes axes de progression.");
            feedback.put("points_forts", new JSONArray().put("Participation").put("Motivation"));
            feedback.put("axes_amelioration", new JSONArray().put("Revoir les fondamentaux"));
            feedback.put("conseil_final", "Chaque erreur est une opportunité d'apprendre !");
        }

        return feedback.toString();
    }
}