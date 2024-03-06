package br.upe.bulaexpress.ask.handlers;

import br.upe.bulaexpress.datalayer.models.Medicamento;
import br.upe.bulaexpress.datalayer.models.bula.SecaoBula;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;

@Component
public class TopicIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.getAttributesManager().getSessionAttributes().get("medicamento") != null &&
                intentRequest.getIntent().getName().contains("TopicIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        Map<String, Object> jsonMedicamento = (Map<String, Object>) input.getAttributesManager().getSessionAttributes().get("medicamento");

        Gson gson = new Gson();
        Medicamento medicamento = gson.fromJson(JsonParser.parseString(gson.toJson(jsonMedicamento)), Medicamento.class);

        String conteudo = getSectionContentByIntentSectionName(intentRequest.getIntent().getName(), medicamento);

        //String conteudo = medicamento.getBula().getConteudoSecao(SecaoBula.FUNCIONAMENTO);
        System.out.println(conteudo);

        return input.getResponseBuilder().withSpeech(conteudo).build();
    }

    private String getSectionContentByIntentSectionName(String intentName, Medicamento medicamento) {
        return switch (intentName) {
            case "IndicationTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.INDICACAO);
            case "HowItWorksTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.FUNCIONAMENTO);
            case "WhenNotToUseTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.QUANDO_NAO_UTILIZAR);
            case "WhatToKnowBeforeTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.OQUE_SABER);
            case "StorageTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.ONDE_COMO_QUANTO_TEMPO_GUARDAR);
            case "HowToUseTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.COMO_UTILIZAR);
            case "ForgettingTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.OQUE_FAZER_ESQUECIMENTO);
            case "HarmsTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.QUAIS_MALES);
            case "BiggerQuantityTopicIntent" -> medicamento.getBula().getConteudoSecao(SecaoBula.QUANTIDADE_MAIOR_QUE_INDICADA);
            default -> throw new IllegalStateException("Unexpected value: " + intentName);
        };
    }

}
