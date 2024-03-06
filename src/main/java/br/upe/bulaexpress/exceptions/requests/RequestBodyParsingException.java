package br.upe.bulaexpress.exceptions.requests;

public class RequestBodyParsingException extends RequestException{
    public RequestBodyParsingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RequestBodyParsingException(String message) {
        super(message);
    }

    public RequestBodyParsingException(Throwable throwable) {
        super(throwable);
    }
}
