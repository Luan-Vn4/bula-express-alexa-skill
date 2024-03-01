package br.upe.bulaexpress.exceptions.api.requests;

public class RequestConnectionException extends RequestException{

    public RequestConnectionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RequestConnectionException(String message) {
        super(message);
    }

    public RequestConnectionException(Throwable throwable) {
        super(throwable);
    }

}
