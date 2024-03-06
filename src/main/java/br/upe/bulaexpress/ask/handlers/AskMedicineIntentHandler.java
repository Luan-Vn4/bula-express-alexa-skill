package br.upe.bulaexpress.ask.handlers;

import br.upe.bulaexpress.datalayer.apis.anvisa.MedicamentoProvider;
import br.upe.bulaexpress.datalayer.models.Medicamento;
import br.upe.bulaexpress.exceptions.apis.anvisa.MedicamentoNotFound;
import br.upe.bulaexpress.exceptions.requests.RequestException;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.RequestHelper;
import com.amazon.ask.response.ResponseBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class AskMedicineIntentHandler implements IntentRequestHandler {

    MedicamentoProvider medicamentoProvider;

    public AskMedicineIntentHandler(MedicamentoProvider medicamentoProvider) {
        this.medicamentoProvider = medicamentoProvider;
    }

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().matches("AskMedicineIntent");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        ResponseBuilder responseBuilder = input.getResponseBuilder();

        RequestHelper requestHelper = RequestHelper.forHandlerInput(input);

        if (requestHelper.getSlotValue("medicine").isEmpty()) {
            return responseBuilder
                .addElicitSlotDirective("medicine", intentRequest.getIntent())
                .build();
        }

        if (requestHelper.getSlotValue("company").isEmpty()) {
            return responseBuilder
                    .addElicitSlotDirective("company", intentRequest.getIntent())
                    .build();
        }

        String medicine = requestHelper.getSlotValue("medicine").get();
        String company = requestHelper.getSlotValue("company").get();

        Medicamento medicamento;
        try {
            medicamento = medicamentoProvider.getMedicamento(medicine, company);
        } catch (MedicamentoNotFound | RequestException e) {
            return responseBuilder
                    .withSpeech("Não foi possível encontrar o medicamento")
                    .build();
        }

        Map<String, Object> sessionAttributes = input.getAttributesManager().getSessionAttributes();
        sessionAttributes.put("medicamento", medicamento);

        return responseBuilder
                .withSpeech("O que você deseja saber sobre o medicamento " + medicamento.getNome())
                .build();
    }
}
