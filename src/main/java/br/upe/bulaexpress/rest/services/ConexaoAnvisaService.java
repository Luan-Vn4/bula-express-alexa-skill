package br.upe.bulaexpress.rest.services;

import br.upe.bulaexpress.entity.Medicamento;
import br.upe.bulaexpress.rest.exception.ApiException;
import com.google.gson.*;
import org.apache.tomcat.util.http.fileupload.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class ConexaoAnvisaService {
  private static final String URL_CONSULTA_API =
      "https://consultas.anvisa.gov.br/api/consulta/"
          + "bulario?count=50&filter%5BnomeProduto%5D={nome}&page={pagina}";
  private static final String CAMINHO_DIRETORIO_BULAS =
      "src/main/java/br/upe/bulaexpress/rest/bula/";
  private static final HttpClient clienteHttp = HttpClient.newHttpClient();

  private static JsonObject obterCorpoResposta(String url)
      throws URISyntaxException, IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(new URI(url))
            .header("accept", "application/json, text/plain, application/pdf, */*")
            .header("accept-language", "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7")
            .header("authorization", "Guest")
            .header("cache-control", "no-cache")
            .header("if-modified-since", "Mon, 26 Jul 1997 05:00:00 GMT")
            .header("pragma", "no-cache")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "'Windows'")
            .header("sec-fetch-dest", "empty")
            .header("sec-fetch-mode", "cors")
            .header("sec-fetch-site", "same-origin")
            .header("Referer", "https://consultas.anvisa.gov.br/")
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
            .header("Referrer-Policy", "no-referrer-when-downgrade")
            .GET()
            .build();

    HttpResponse<String> response = clienteHttp.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new ApiException("Ocorreu um erro ao consultar o medicamento na API da ANVISA");
    }

    JsonElement jsonElement = JsonParser.parseString(response.body());
    return jsonElement.getAsJsonObject();
  }

  private static List<Medicamento> extrairMedicamentosPagina(JsonObject responseBody) {
    JsonArray jsonArray = responseBody.getAsJsonArray("content");
    List<Medicamento> medicamentos = new ArrayList<>();

    for (JsonElement elemento : jsonArray) {
      Medicamento medicamento = new Gson().fromJson(elemento, Medicamento.class);
      medicamentos.add(medicamento);
    }

    if (medicamentos.isEmpty()) {
      throw new ApiException("Não foi encontrado nenhum medicamento com o nome fornecido");
    }

    return medicamentos;
  }

  private static List<Medicamento> extrairTodosMedicamentos(String nomeMedicamento)
      throws URISyntaxException, IOException, InterruptedException {
    int numeroPagina = 0;
    List<Medicamento> listaCompleta = new ArrayList<>();

    do {
      String url =
          URL_CONSULTA_API
              .replace("{nome}", nomeMedicamento.toLowerCase())
              .replace("{pagina}", String.valueOf(++numeroPagina));

      JsonObject resposta = obterCorpoResposta(url);
      List<Medicamento> medicamentosPagina = extrairMedicamentosPagina(resposta);
      listaCompleta.addAll(medicamentosPagina);

      if (resposta.getAsJsonPrimitive("last").getAsBoolean()) {
        break;
      }
    } while (true);

    return listaCompleta;
  }

  private static String gerarLinkPdf(String token) {
    return "https://consultas.anvisa.gov.br/api/consulta/medicamentos/arquivo/bula/parecer/"
        + token
        + "/?Authorization=";
  }

  private static String obterPorNomeEFabricante(String nomeMedicamento, String fabricante)
      throws URISyntaxException, IOException, InterruptedException {
    List<Medicamento> medicamentos = extrairTodosMedicamentos(nomeMedicamento);

    List<Medicamento> matchFabricante =
        medicamentos.stream()
            .filter(
                medicamento ->
                    medicamento.getFabricante().toLowerCase().contains(fabricante.toLowerCase()))
            .toList();

    if (matchFabricante.isEmpty()) {
      throw new ApiException("Não foi encontrado nenhum medicamento com o fabricante fornecido");
    }

    return gerarLinkPdf(matchFabricante.get(0).getToken());
  }

  public void baixarPdf(String nomeMedicamento, String fabricante)
      throws URISyntaxException, IOException, InterruptedException {
    String linkPdf = obterPorNomeEFabricante(nomeMedicamento, fabricante);
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(linkPdf)).build();

    HttpResponse<InputStream> response =
        clienteHttp.send(request, HttpResponse.BodyHandlers.ofInputStream());

    if (response.statusCode() != 200) {
      throw new ApiException("Ocorreu um erro ao baixar o arquivo da bula");
    }

    File arquivo = new File(CAMINHO_DIRETORIO_BULAS + "resultado" + ".pdf");
    FileOutputStream arquivoSaida = new FileOutputStream(arquivo);
    IOUtils.copy(response.body(), arquivoSaida);
  }
}
