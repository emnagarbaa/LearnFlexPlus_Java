package org.example.bot;

import org.example.Services.EvenementService;
import org.example.entities.Evenement;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

public class EvenementBot extends TelegramLongPollingBot {

    private final EvenementService evenementService = new EvenementService();
    private final GroqAgent        agent            = new GroqAgent();

    

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String text   = update.getMessage().getText().trim();
        long   chatId = update.getMessage().getChatId();

        switch (text) {
            case "/start" -> sendText(chatId,
                    "👋 *Bienvenue sur LearnFlex+!*\n\n" +
                            "Je suis votre assistant e-learning intelligent 🤖\n\n" +
                            "📌 Commandes:\n" +
                            "/evenements — Voir les événements\n" +
                            "/aide — Aide\n\n" +
                            "💬 Ou posez-moi directement une question sur l'e-learning!");

            case "/evenements" -> sendEvenements(chatId);

            case "/aide" -> sendText(chatId,
                    "📌 *Commandes disponibles:*\n\n" +
                            "/start — Accueil\n" +
                            "/evenements — Liste des événements\n" +
                            "/aide — Afficher cette aide\n\n" +
                            "💬 Vous pouvez aussi me poser des questions librement!");

            // Tout autre message → LLM Groq (comme ton bot Python)
            default -> {
                // ✅ Chercher d'abord si c'est un nom d'événement
                try {
                    List<Evenement> list = evenementService.findAll();
                    Evenement found = null;

                    for (Evenement e : list) {
                        if (e.getTitre().toLowerCase()
                                .contains(text.toLowerCase())) {
                            found = e;
                            break;
                        }
                    }

                    if (found != null) {
                        // ✅ Afficher les détails de l'événement trouvé
                        sendText(chatId,
                                "📌 *" + found.getTitre() + "*\n\n" +
                                        "📍 *Lieu:* " + found.getLieu() + "\n" +
                                        "🗓 *Début:* " + found.getDateDebut() + "\n" +
                                        "🏁 *Fin:* " + found.getDateFin() + "\n" +
                                        "👥 *Public:* " + found.getPublicCible() + "\n" +
                                        "🎯 *Mode:* " + found.getMode() + "\n" +
                                        "👤 *Capacité:* " + found.getCapaciteMax() + "\n" +
                                        "📧 *Email:* " + found.getContactEmail() + "\n" +
                                        "📞 *Tél:* " + found.getContactTelephone() + "\n" +
                                        "🔗 *Lien:* " + found.getLienInscription() + "\n\n" +
                                        "📝 *Description:*\n" + found.getDescription()
                        );
                    } else {
                        // ✅ Pas un événement → LLM Groq
                        sendText(chatId, "⏳ Je réfléchis...");
                        String aiResponse = agent.getResponse(text);
                        sendText(chatId, aiResponse);
                    }

                } catch (SQLException ex) {
                    sendText(chatId, "❌ Erreur base de données.");
                }
            }
        }
    }

    private void sendEvenements(long chatId) {
        try {
            List<Evenement> list = evenementService.findAll();
            if (list.isEmpty()) {
                sendText(chatId, "Aucun événement disponible.");
                return;
            }
            StringBuilder sb = new StringBuilder("📅 *Événements disponibles:*\n\n");
            for (Evenement e : list) {
                sb.append("🔹 *").append(e.getTitre()).append("*\n")
                        .append("📍 ").append(e.getLieu()).append("\n")
                        .append("🗓 ").append(e.getDateDebut()).append("\n")
                        .append("👥 ").append(e.getPublicCible()).append("\n\n");
            }
            sendText(chatId, sb.toString());
        } catch (SQLException ex) {
            sendText(chatId, "❌ Erreur récupération des événements.");
        }
    }

    private void sendText(long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        msg.setParseMode("Markdown");
        try { execute(msg); }
        catch (TelegramApiException e) { e.printStackTrace(); }
    }
}