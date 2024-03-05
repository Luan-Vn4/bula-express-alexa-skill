package br.upe.bulaexpress.utils;

import br.upe.bulaexpress.datalayer.models.bula.Bula;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BulaSectionExtractor {

    // ATRIBUTOS

    // Padrão: "x. <título da sessão>"
    // x - Dígito
    private static final String BULA_SECTION_PATTERN = "\\d\\. [^|.]* ?";

    private String currentReadLine = "";

    private int currentLineIndex = 0;

    private boolean lastLineNotReached() {
        return this.currentReadLine != null;
    }

    private boolean currentLineMatchesSectionPattern() {
        return this.currentReadLine.matches(BULA_SECTION_PATTERN);
    }

    // MÉTODOS DE EXTRAÇÃO

    public synchronized Bula extract(byte[] bytes) throws IOException {
        return extract(Loader.loadPDF(bytes));
    }

    public synchronized Bula extract(String path) throws IOException{
        return extract(Loader.loadPDF(new File(path)));
    }

    public synchronized Bula extract(File file) throws IOException {
        return extract(Loader.loadPDF(file));
    }

    public synchronized Bula extract(PDDocument pdfDocument) throws IOException{
        List<String> sections = new ArrayList<>();

        String text = getTextFromPDF(pdfDocument);

        String textWithoutFooter = getPdfTextWithoutFooters(text);

        int[] boundaries = searchBulaContentBoundaries(textWithoutFooter);
        int startBound = boundaries[0];
        int endBound = boundaries[1];

        try (BufferedReader buffer = new BufferedReader(new StringReader(textWithoutFooter))) {
            while (this.currentLineIndex < endBound && lastLineNotReached() && sections.size() < 9) {
                // Identifica se aquela(s) linha(s) apresenta(m) o padrão das seções de uma bula, para então guardar
                // o título daquela seção como a key e extrair o texto daquela sessão como o valor associado
                if (this.currentLineIndex > startBound && currentLineMatchesSectionPattern()) {
                    sections.add(extractSessionText(buffer, endBound));

                    // Este trecho impede que uma seção seja ignorada, caso a extração tenha parado por conta daquela
                    // ter sido alcançada
                    if (lastLineNotReached() && currentLineMatchesSectionPattern()) {
                        continue;
                    }
                }
                this.currentReadLine = buffer.readLine();
                this.currentLineIndex++;
            }

            return new Bula(sections);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.redefineAuxiliaryFields();
        }

    }

    private String getTextFromPDF(PDDocument pdfDocument) {
        try {
            return new PDFTextStripper().getText(pdfDocument);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractSessionText(BufferedReader buffer, int endBound) throws IOException {
        StringBuilder sessionTextBuilder = new StringBuilder();

        while (true) {
            this.currentReadLine = buffer.readLine();
            this.currentLineIndex++;
            if (this.currentLineIndex < endBound && lastLineNotReached() && !currentLineMatchesSectionPattern()) {
                if (!this.currentReadLine.isBlank()) {
                sessionTextBuilder.append(this.currentReadLine).append("\n");
                }
                continue;
            }
            break;
        }

        return sessionTextBuilder.toString();
    }

    private static int[] searchBulaContentBoundaries(String text) throws IOException {
        int inicio = searchLineIndex(text, "informações ao paciente");
        int fim = searchLineIndex(text, "dizeres legais");
        return new int[] {inicio, fim};
    }

    private static int searchLineIndex(String text, String trechoBuscado) throws IOException {
        try (BufferedReader buffer = new BufferedReader(new StringReader(text))) {

            String linhaLida;
            int numeroLinha = 0;
            boolean textoEncontrado = false;

            while ((linhaLida = buffer.readLine()) != null) {
                numeroLinha++;

                if (linhaLida.toLowerCase().contains(trechoBuscado)) {
                    textoEncontrado = true;
                    break;
                }
            }

            if (textoEncontrado) {
                return numeroLinha;
            } else {
                return -1;
            }
        }
    }

    private static String getPdfTextWithoutFooters(String text) throws IOException {
        List<String> possibleFooters = buscarRodapes(text);
        String footer = identificarRodape(possibleFooters);

        return substituir(text, footer);
    }

    private static List<String> buscarRodapes(String text) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new StringReader(text))) {
            String linhaLida;
            List<String> resultados = new ArrayList<>();

            String linhaTeste = null;
            boolean isBranco = false;
            boolean isLinhaNext = false;

            while ((linhaLida = bufferedReader.readLine()) != null) {
                if (linhaLida.trim().isEmpty() || linhaLida.trim().length() == 1) {
                    if (isBranco && isLinhaNext) {
                        resultados.add(linhaTeste);
                    }

                    isBranco = true;
                    isLinhaNext = false;
                } else {
                    if (isBranco && isLinhaNext) {
                        isBranco = false;
                        isLinhaNext = false;
                        continue;
                    }

                    if (isBranco) {
                        linhaTeste = linhaLida;
                        isLinhaNext = true;
                        continue;
                    }

                    isBranco = false;
                    isLinhaNext = false;
                }
            }
            return resultados.stream()
                    .map(string -> string.substring(0, (int) (string.length() * 0.75)))
                    .toList();
        }
    }

    private static String identificarRodape(List<String> lista) {
        Map<String, Integer> contador = new HashMap<>();

        for (String elemento : lista) {
            contador.put(elemento, contador.getOrDefault(elemento, 0) + 1);
        }

        String elementoMaisRepetido = null;
        int frequenciaMaxima = 0;
        for (Map.Entry<String, Integer> entry : contador.entrySet()) {
            if (entry.getValue() > frequenciaMaxima) {
                elementoMaisRepetido = entry.getKey();
                frequenciaMaxima = entry.getValue();
            }
        }

        return elementoMaisRepetido;
    }

    private static String substituir(String text, String rodape) throws IOException {
        StringWriter writer = new StringWriter();
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {

            String linhaLida;

            while ((linhaLida = reader.readLine()) != null) {
                if (linhaLida.toLowerCase().contains(rodape.toLowerCase()) || linhaLida.trim().length() == 1) {
                    writer.write("\n");
                } else {
                    writer.write(linhaLida + "\n");
                }
            }
        }
        return writer.toString();
    }

    private void redefineAuxiliaryFields() {
        this.currentReadLine = "";
        this.currentLineIndex = 0;
    }

}
