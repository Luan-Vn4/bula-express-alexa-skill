package br.upe.bulaexpress.rest.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class BulaService {
  private static final String PATH_TXT = "src/main/java/br/upe/bulaexpress/rest/bula/output.txt";
  private static final String PATH_TEMP1 = "src/main/java/br/upe/bulaexpress/rest/bula/temp.txt";
  private static final String PATH_TEMP2 = "src/main/java/br/upe/bulaexpress/rest/bula/temp2.txt";

  private static void converterPdfParaTxt(String pdfPath) throws IOException {
    File file = new File(pdfPath);

    try (PDDocument document = PDDocument.load(file)) {
      PDFTextStripper textStripper = new PDFTextStripper();
      String text = textStripper.getText(document);

      File arquivoConvertido = new File(PATH_TXT);
      Files.write(arquivoConvertido.toPath(), text.getBytes());
    }
  }

  private static int buscarLinha(String textPath, String trechoBuscado) throws IOException {
    try (FileReader fileReader = new FileReader(textPath)) {
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      String linhaLida;
      int numeroLinha = 0;
      boolean textoEncontrado = false;

      while ((linhaLida = bufferedReader.readLine()) != null) {
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

  private static int[] buscarTrecho(String textPath) throws IOException {
    int inicio = buscarLinha(textPath, "informações ao paciente");
    int fim = buscarLinha(textPath, "dizeres legais");
    return new int[] {inicio, fim};
  }

  private static void separarTrecho(String textPath, int[] posicoes) throws IOException {
    try (FileReader fileReader = new FileReader(textPath);
        FileWriter fileWriter = new FileWriter(PATH_TEMP1);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

      String linhaLida;
      int contador = 0;

      while ((linhaLida = bufferedReader.readLine()) != null) {
        contador++;

        if (contador >= posicoes[0] + 1 && contador <= posicoes[1] - 1) {
          bufferedWriter.write(linhaLida);
          bufferedWriter.newLine();
        }
      }
    }
  }

  private static List<String> buscarRodapes() throws IOException {
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(PATH_TEMP1))) {
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
      return resultados;
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

  private static void substituir(String rodape) throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(PATH_TEMP1));
        BufferedWriter writer = new BufferedWriter(new FileWriter(PATH_TEMP2))) {

      String linhaLida;

      while ((linhaLida = reader.readLine()) != null) {
        if (linhaLida.equals(rodape) || linhaLida.trim().length() == 1) {
          writer.write("\n");
        } else {
          writer.write(linhaLida + "\n");
        }
      }
    }
  }

  public void operation(String pdfPath) throws IOException {
    converterPdfParaTxt(pdfPath);
    int[] posicoes = buscarTrecho(PATH_TXT);
    separarTrecho(PATH_TXT, posicoes);

    List<String> rodapes = buscarRodapes();
    String frase = identificarRodape(rodapes);

    substituir(frase);
  }
}
