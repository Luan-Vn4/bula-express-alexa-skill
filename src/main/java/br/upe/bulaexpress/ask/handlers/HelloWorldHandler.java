package br.upe.bulaexpress.ask.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component()
public class HelloWorldHandler implements IntentRequestHandler {

    public static String intentName = "HelloWorldIntent";

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        return intentRequest.getIntent().getName().equals(intentName);
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        String speechText = "Olá, mundo!";
        return handlerInput.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build();
    }

}
