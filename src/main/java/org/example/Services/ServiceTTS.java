package org.example.Services;

import java.io.IOException;

public class ServiceTTS {

    private Process currentProcess;
    private boolean speaking = false;

    /**
     * Lit un texte à voix haute selon l'OS
     */
    public void lire(String texte) {
        arreter();

        // Nettoyer le texte (enlever emojis et caractères spéciaux)
        String textePropre = texte
                .replaceAll("[^\\p{L}\\p{N}\\s.,!?;:'-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (textePropre.isEmpty()) return;

        speaking = true;
        String os = System.getProperty("os.name").toLowerCase();

        try {
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Windows — PowerShell TTS natif
                String script = String.format(
                        "Add-Type -AssemblyName System.Speech; " +
                                "$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                                "$s.Rate = 0; " +
                                "$s.Speak('%s');",
                        textePropre.replace("'", " ")
                );
                pb = new ProcessBuilder("powershell", "-Command", script);

            } else if (os.contains("mac")) {
                // macOS — commande say native
                pb = new ProcessBuilder("say", "-v", "Thomas", textePropre);

            } else {
                // Linux — espeak (sudo apt install espeak)
                pb = new ProcessBuilder("espeak", "-v", "fr", "-s", "140", textePropre);
            }

            pb.redirectErrorStream(true);
            currentProcess = pb.start();

            new Thread(() -> {
                try {
                    currentProcess.waitFor();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    speaking = false;
                }
            }).start();

        } catch (IOException e) {
            System.err.println("❌ TTS indisponible : " + e.getMessage());
            speaking = false;
        }
    }

    /**
     * Arrête la lecture en cours
     */
    public void arreter() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroyForcibly();
        }
        speaking = false;
    }

    public boolean isSpeaking() {
        return speaking;
    }
}