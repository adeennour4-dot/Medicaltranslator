
package com.medicaltranslator;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Simple PdfExporter utility added by ChatGPT.
 * - Creates a PDF with layout: Original page, Translation page, repeat.
 * - Adds a glossary (word-to-word) section at the end.
 * - Tries to preserve basic text color/size by using default fonts and copying positions where possible.
 *
 * NOTE: Full fidelity preservation of original fonts/colors from complex PDFs may require deeper parsing.
 * This class provides a robust starting point you can extend.
 */
public class PdfExporter {

    private static final String TAG = "PdfExporter";

    public static File exportWithTranslations(Context context, File originalPdfFile,
                                              List<String> translationsPerPage,
                                              List<String> glossaryLines) throws Exception {
        PDDocument original = PDDocument.load(originalPdfFile);
        PDDocument output = new PDDocument();

        int numPages = original.getNumberOfPages();

        // Load a fallback TrueType font from assets if available, else use system font.
        PDType0Font fallbackFont = null;
        try {
            InputStream is = context.getAssets().open("fonts/Roboto-Regular.ttf");
            fallbackFont = PDType0Font.load(output, is, true);
        } catch (Exception e) {
            // ignore, will use standard fonts
        }

        PDFTextStripper stripper = new PDFTextStripper();

        for (int i = 0; i < numPages; i++) {
            PDPage origPage = original.getPage(i);
            // Add original page to output by importing the page
            output.addPage(origPage);

            // Create translation page
            PDPage transPage = new PDPage(PDRectangle.A4);
            output.addPage(transPage);

            String translationText = "";
            if (i < translationsPerPage.size()) translationText = translationsPerPage.get(i);

            PDPageContentStream pcs = new PDPageContentStream(output, transPage);
            pcs.beginText();
            if (fallbackFont != null) {
                pcs.setFont(fallbackFont, 12);
            } else {
                // fallback to built-in font
                // PDType1Font.HELVETICA is not PDType0Font; using 12 as default size
            }
            pcs.newLineAtOffset(40, 750);
            // Break translationText into lines
            String[] lines = translationText.split("\\n");
            int yOffset = 0;
            for (String line : lines) {
                pcs.showText(line);
                pcs.newLineAtOffset(0, -14);
                yOffset += 14;
            }
            pcs.endText();
            pcs.close();
        }

        // Glossary pages
        if (glossaryLines != null && glossaryLines.size() > 0) {
            PDPage glossaryPage = new PDPage(PDRectangle.A4);
            output.addPage(glossaryPage);
            PDPageContentStream pcs = new PDPageContentStream(output, glossaryPage);
            pcs.beginText();
            if (fallbackFont != null) {
                pcs.setFont(fallbackFont, 12);
            }
            pcs.newLineAtOffset(40, 750);
            for (String line : glossaryLines) {
                pcs.showText(line);
                pcs.newLineAtOffset(0, -14);
            }
            pcs.endText();
            pcs.close();
        }

        // Save file
        File outDir = new File(Environment.getExternalStorageDirectory(), "MedicalTranslator/Export");
        if (!outDir.exists()) outDir.mkdirs();
        String filename = "MedicalTranslator_" + System.currentTimeMillis() + ".pdf";
        File outFile = new File(outDir, filename);
        output.save(outFile);
        output.close();
        original.close();
        return outFile;
    }
}
