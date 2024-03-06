package br.upe.bulaexpress.ask.handlers.factories;

import com.amazon.ask.dispatcher.request.handler.RequestHandler;

public interface RequestHandlerFactory {

    RequestHandler getRequestHandlerInstance(String requestName);

    RequestHandler[] getRequestHandlers(String... requestNames);

    RequestHandler[] getRequestHandlers();

}
