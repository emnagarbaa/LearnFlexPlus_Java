package org.example.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CertificatGenerator {

    public static File generer(String nomEtudiant, String titreQuiz,
                               int score, int total) throws IOException {

        PDDocument doc = new PDDocument();

        // ── PAGE PAYSAGE ───────────────────────────────────────────
        PDRectangle paysage = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
        PDPage page = new PDPage(paysage);
        doc.addPage(page);

        float W = paysage.getWidth();   // 841
        float H = paysage.getHeight();  // 595

        PDPageContentStream cs = new PDPageContentStream(doc, page);

        // ── FOND ───────────────────────────────────────────────────
        cs.setNonStrokingColor(new PDColor(new float[]{0.03f, 0.15f, 0.25f}, PDDeviceRGB.INSTANCE));
        cs.addRect(0, 0, W, H);
        cs.fill();

        // ── BORDURE DORÉE DOUBLE ───────────────────────────────────
        PDColor or = new PDColor(new float[]{0.85f, 0.65f, 0.13f}, PDDeviceRGB.INSTANCE);
        cs.setStrokingColor(or);
        cs.setLineWidth(5);
        cs.addRect(18, 18, W - 36, H - 36);
        cs.stroke();
        cs.setLineWidth(1.5f);
        cs.addRect(26, 26, W - 52, H - 52);
        cs.stroke();

        // ── POLICES ────────────────────────────────────────────────
        PDFont bold   = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont italic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        PDColor blanc = new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE);
        PDColor gris  = new PDColor(new float[]{0.75f, 0.75f, 0.75f}, PDDeviceRGB.INSTANCE);

        // ── TITRE ──────────────────────────────────────────────────
        cs.setNonStrokingColor(or);
        drawCentered(cs, "CERTIFICAT DE REUSSITE", bold, 34, W, H - 95);

        // ── LIGNE DÉCORATIVE HAUTE ─────────────────────────────────
        cs.setStrokingColor(or);
        cs.setLineWidth(1f);
        cs.moveTo(W / 2f - 180, H - 115);
        cs.lineTo(W / 2f + 180, H - 115);
        cs.stroke();

        // ── SOUS-TITRE ─────────────────────────────────────────────
        cs.setNonStrokingColor(blanc);
        drawCentered(cs, "Ce certificat est decerne a", italic, 15, W, H - 160);

        // ── NOM ÉTUDIANT ───────────────────────────────────────────
        cs.setNonStrokingColor(or);
        // Tronquer si trop long
        String nom = nomEtudiant.toUpperCase();
        if (nom.length() > 35) nom = nom.substring(0, 35) + "...";
        drawCentered(cs, nom, bold, 28, W, H - 210);

        // ── LIGNE DÉCORATIVE BASSE NOM ─────────────────────────────
        cs.setStrokingColor(or);
        cs.moveTo(W / 2f - 160, H - 225);
        cs.lineTo(W / 2f + 160, H - 225);
        cs.stroke();

        // ── TEXTE QUIZ ─────────────────────────────────────────────
        cs.setNonStrokingColor(blanc);
        drawCentered(cs, "pour avoir complete avec succes le quiz", normal, 14, W, H - 265);

        // Tronquer le titre si trop long
        String titre = titreQuiz;
        if (titre.length() > 45) titre = titre.substring(0, 45) + "...";
        cs.setNonStrokingColor(or);
        drawCentered(cs, "\"" + titre + "\"", bold, 18, W, H - 300);

        // ── SCORE ──────────────────────────────────────────────────
        int pct = total == 0 ? 0 : (int) Math.round((double) score / total * 100);
        cs.setNonStrokingColor(blanc);
        drawCentered(cs, "Score obtenu : " + score + " / " + total + "  (" + pct + "%)", bold, 16, W, H - 345);

        // ── LIGNE SÉPARATRICE ──────────────────────────────────────
        cs.setStrokingColor(gris);
        cs.setLineWidth(0.5f);
        cs.moveTo(W / 2f - 200, H - 375);
        cs.lineTo(W / 2f + 200, H - 375);
        cs.stroke();

        // ── DATE ───────────────────────────────────────────────────
        cs.setNonStrokingColor(gris);
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        // Remplacer les accents pour PDFBox Type1
        date = date.replace("é", "e").replace("û", "u").replace("â", "a")
                .replace("ê", "e").replace("î", "i").replace("ô", "o");
        drawCentered(cs, "Delivre le " + date, italic, 12, W, H - 410);

        // ── SIGNATURE ──────────────────────────────────────────────
        cs.setNonStrokingColor(or);
        drawCentered(cs, "LearnFlex+", bold, 13, W, H - 455);
        cs.setNonStrokingColor(gris);
        drawCentered(cs, "Plateforme d'apprentissage en ligne", normal, 11, W, H - 472);
// ── QR CODE ──────────────────────────────────────────────
        try {
            String qrText = "http://localhost:8080/certificats/certificat_123.pdf";

            File qrFile = genererQRCode(qrText);

            PDImageXObject qrImage = PDImageXObject.createFromFile(
                    qrFile.getAbsolutePath(), doc
            );

            float qrSize = 100;
            float qrX = W - qrSize - 40;
            float qrY = 40;

            cs.drawImage(qrImage, qrX, qrY, qrSize, qrSize);

        } catch (Exception e) {
            e.printStackTrace();
        }
        cs.close();

        // ── SAUVEGARDE ─────────────────────────────────────────────
        String nomFichier = "certificat_" + nomEtudiant.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
        File output = new File(System.getProperty("user.home"), nomFichier);
        doc.save(output);
        doc.close();
        return output;
    }

    private static void drawCentered(PDPageContentStream cs, String text,
                                     PDFont font, float size,
                                     float pageWidth, float y) throws IOException {
        // Remplacer les caractères non supportés par Helvetica
        text = text.replace("é", "e").replace("è", "e").replace("ê", "e")
                .replace("à", "a").replace("â", "a").replace("ù", "u")
                .replace("û", "u").replace("î", "i").replace("ô", "o")
                .replace("ç", "c").replace("É", "E").replace("È", "E")
                .replace("À", "A").replace("Ç", "C").replace("œ", "oe")
                .replace("°", " ");

        float textWidth = font.getStringWidth(text) / 1000 * size;
        float x = (pageWidth - textWidth) / 2f;
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }
    private static File genererQRCode(String text) throws Exception {
        int width = 150;
        int height = 150;

        BitMatrix matrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        String filePath = System.getProperty("java.io.tmpdir") + "/qrcode.png";
        Path path = FileSystems.getDefault().getPath(filePath);

        MatrixToImageWriter.writeToPath(matrix, "PNG", path);

        return new File(filePath);
    }
}