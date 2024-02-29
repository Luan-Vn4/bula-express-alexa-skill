package br.upe.bulaexpress.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class BulaSectionExtractor {

    // Padrão: "x. <título da sessão>"
    // x - Dígito
    private static final String BULA_SECTION_PATTERN = "\\d\\. [^|.]* ?";

    private String currentReadLine = "";

    private boolean lastLineNotReached() {
        return this.currentReadLine != null;
    }

    private boolean currentLineMatchesSectionPattern() {
        return this.currentReadLine.matches(BULA_SECTION_PATTERN);
    }

    public List<String> extractFromDirectory(String path) {
        PDDocument document;
        try {
            File file = new File(path);
            document = Loader.loadPDF(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return extract(document);
    }

    public List<String> extract(PDDocument pdfDocument) {
        List<String> sections = new ArrayList<>();

        String text = getTextFromPDF(pdfDocument);

        try (BufferedReader buffer = new BufferedReader(new StringReader(text))) {
            while (lastLineNotReached() && sections.size() < 9) {
                // Identifica se aquela(s) linha(s) apresenta(m) o padrão das seções de uma bula, para então guardar
                // o título daquela seção como a key e extrair o texto daquela sessão como o valor associado
                if (currentLineMatchesSectionPattern()) {
                    sections.add(extractSessionText(buffer));

                    // Este trecho impede que uma seção seja ignorada, caso a extração tenha parado por conta daquela
                    // ter sido alcançada
                    if (lastLineNotReached() && currentLineMatchesSectionPattern()) {
                        continue;
                    }
                }
                currentReadLine = buffer.readLine();
            }

            return sections;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTextFromPDF(PDDocument pdfDocument) {
        try {
            return new PDFTextStripper().getText(pdfDocument);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractSessionText(BufferedReader buffer) throws IOException {
        StringBuilder sessionTextBuilder = new StringBuilder();

        while (true) {
            currentReadLine = buffer.readLine();
            if (lastLineNotReached() && !currentLineMatchesSectionPattern() && !currentReadLine.isBlank()) {
                sessionTextBuilder.append(currentReadLine).append("\n");
                continue;
            }
            break;
        }

        return sessionTextBuilder.toString();
    }

}
