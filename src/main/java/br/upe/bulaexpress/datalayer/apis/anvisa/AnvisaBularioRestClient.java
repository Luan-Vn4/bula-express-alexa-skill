package br.upe.bulaexpress.datalayer.apis.anvisa;

import br.upe.bulaexpress.datalayer.models.Medicamento;
import br.upe.bulaexpress.datalayer.models.bula.Bula;
import br.upe.bulaexpress.exceptions.api.anvisa.MedicamentoNotFound;
import br.upe.bulaexpress.exceptions.api.requests.*;
import br.upe.bulaexpress.utils.BulaSectionExtractor;
import com.google.gson.*;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.Loader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
@Log4j2
public class AnvisaBularioRestClient implements MedicamentoProvider {

    // Atributos

    private final BulaSectionExtractor bulaSectionExtractor;

    private static final String URL_CONSULTA_API =
            "https://consultas.anvisa.gov.br/api/consulta/"
                    + "bulario?count=50&filter%5BnomeProduto%5D={nome}&page={pagina}";

    private static final HttpClient clienteHttp = HttpClient.newHttpClient();

    // Métodos de acesso

    public AnvisaBularioRestClient(BulaSectionExtractor bulaSectionExtractor) {
        this.bulaSectionExtractor = bulaSectionExtractor;
    }

    // Operações

    @Override
    public Medicamento getMedicamento(String nomeMedicamento, String fabricante) throws MedicamentoNotFound {
        String nomeFormatado = nomeMedicamento.toLowerCase().replace(" ", "%20");
        Medicamento medicamento = requestMedicamento(nomeFormatado, fabricante);
        medicamento.setBula(getBulaFromMedicamentoToken(medicamento.getToken()));
        return medicamento;
    }

    private static Medicamento requestMedicamento(String nomeMedicamento, String fabricante) {
        List<Medicamento> medicamentos = extrairTodosMedicamentos(nomeMedicamento);

        List<Medicamento> matchFabricante =
                medicamentos.stream()
                            .filter(medicamento ->
                                medicamento.getFabricante().toLowerCase().contains(fabricante.toLowerCase()))
                            .toList();

        if (matchFabricante.isEmpty()) {
            String message = "Não foi encontrado nenhum medicamento com o fabricante fornecido - " + fabricante;
            log.warn(message, new MedicamentoNotFound());
            throw new MedicamentoNotFound(message);
        }

        return matchFabricante.get(0);
    }

    private static List<Medicamento> extrairTodosMedicamentos(String nomeMedicamento) {
        int numeroPagina = 0;
        List<Medicamento> listaCompleta = new ArrayList<>();

        do {
            String url = URL_CONSULTA_API
                    .replace("{nome}", nomeMedicamento.toLowerCase())
                    .replace("{pagina}", String.valueOf(++numeroPagina));

            JsonObject corpoResposta = obterCorpoResposta(url);
            List<Medicamento> medicamentosPagina = extrairMedicamentosPagina(corpoResposta, nomeMedicamento);
            listaCompleta.addAll(medicamentosPagina);

            if (corpoResposta.getAsJsonPrimitive("last").getAsBoolean()) {
                break;
            }
        } while (true);

        return listaCompleta;
    }

    private static JsonObject obterCorpoResposta(String url) {
        HttpRequest anvisaMedicinesListRequest = buildAnvisaMedicamentoRequest(url);

        HttpResponse<String> anvisaMedicinesListResponse = sendResquest(anvisaMedicinesListRequest, HttpResponse.BodyHandlers.ofString());

        if (anvisaMedicinesListResponse.statusCode() != 200) {
            throw new RequestException("Ocorreu um erro ao consultar o medicamento na API da ANVISA");
        }

        JsonElement jsonBody = JsonParser.parseString(anvisaMedicinesListResponse.body());
        return jsonBody.getAsJsonObject();
    }

    private static HttpRequest buildAnvisaMedicamentoRequest(String url) throws RequestBuildingException {
        try {
            return HttpRequest.newBuilder()
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
        } catch (URISyntaxException e) {
            log.error("Ocorreu um erro ao construir a requisição para a Anvisa", new RequestBuildingException(e));
            throw new RequestBuildingException("Ocorreu um erro ao construir a requisição para a Anvisa");
        }
    }

    private static <T> HttpResponse<T> sendResquest(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler)
            throws RequestConnectionException, RequestInterruptedException {
        try {
            return clienteHttp.send(request, bodyHandler);
        } catch (IOException e) {
            String message = "Houve um erro de conexão com o servidor da Anvisa";
            log.warn(message, new RequestConnectionException(e));
            throw new RequestConnectionException(message, e);
        } catch (InterruptedException e) {
            String message = "A operação de requisição foi interrompida";
            log.warn(message, new RequestInterruptedException(e));
            throw new RequestInterruptedException(message, e);
        }
    }

    private static List<Medicamento> extrairMedicamentosPagina(JsonObject responseBody, String nomeMedicamento) {
        JsonArray jsonArray = responseBody.getAsJsonArray("content");
        List<Medicamento> medicamentos = new ArrayList<>();

        for (JsonElement elemento : jsonArray) {
            Medicamento medicamento = new Gson().fromJson(elemento, Medicamento.class);
            medicamentos.add(medicamento);
        }

        if (medicamentos.isEmpty()) {
            String message = "Não foi encontrado nenhum medicamento com o nome fornecido - " + nomeMedicamento;
            log.warn(message, new MedicamentoNotFound());
            throw new MedicamentoNotFound("Não foi encontrado nenhum medicamento com o nome fornecido");
        }

        return medicamentos;
    }

    private Bula getBulaFromMedicamentoToken(String token) {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(gerarLinkPdf(token))).build();

        HttpResponse<InputStream> response = sendResquest(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            String message = "Ocorreu um erro ao baixar o arquivo da bula " + response.statusCode();
            log.warn(message, new RequestException());
            throw new RequestException(message);
        }

        try (InputStream responseBodyInputStream = response.body()){
            return bulaSectionExtractor.extract(Loader.loadPDF(responseBodyInputStream.readAllBytes()));
        } catch (IOException e) {
            String message = "Ocorreu um erro ao transcrever o corpo da requisição para bytes válidos";
            log.warn(message, new RequestBodyParsingException(e));
            throw new RequestBodyParsingException(message, e);
        }
    }

    private static String gerarLinkPdf(String token) {
        return "https://consultas.anvisa.gov.br/api/consulta/medicamentos/arquivo/bula/parecer/"
                + token
                + "/?Authorization=";
    }

}
