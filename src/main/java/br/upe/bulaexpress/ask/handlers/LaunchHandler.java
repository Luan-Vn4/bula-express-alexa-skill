package br.upe.bulaexpress.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LaunchHandler implements LaunchRequestHandler {
    @Override
    public boolean canHandle(HandlerInput input, LaunchRequest launchRequest) {
        return input.getRequest().getType().matches("LaunchRequest");
    }

    @Override
    public Optional<Response> handle(HandlerInput input, LaunchRequest launchRequest) {
        String responseText = "Olá, forneça o nome do medicamente e seu fabricante para que o BulaExpress possa ajudá-lo!";

        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withSimpleCard("Bem-vindo ao BulaExpress", responseText)
                .withReprompt(responseText)
                .build();
    }
}
