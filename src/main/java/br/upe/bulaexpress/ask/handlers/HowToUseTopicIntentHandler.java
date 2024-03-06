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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class HowToUseTopicIntentHandler implements IntentRequestHandler {


    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.getAttributesManager().getSessionAttributes().get("medicamento") != null &&
                intentRequest.getIntent().getName().equals("HowToUseTopicIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        Map<String, Object> jsonMedicamento = (Map<String, Object>) input.getAttributesManager().getSessionAttributes().get("medicamento");

        Gson gson = new Gson();
        Medicamento medicamento = gson.fromJson(JsonParser.parseString(gson.toJson(jsonMedicamento)), Medicamento.class);

        String conteudo = medicamento.getBula().getConteudoSecao(SecaoBula.COMO_UTILIZAR);
        System.out.println(conteudo);

        return input.getResponseBuilder().withSpeech(conteudo).build();
    }

}
