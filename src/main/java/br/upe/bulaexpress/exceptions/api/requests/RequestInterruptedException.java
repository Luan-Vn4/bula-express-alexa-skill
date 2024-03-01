package br.upe.bulaexpress.exceptions.api.requests;

public class RequestInterruptedException extends RequestException{

    public RequestInterruptedException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RequestInterruptedException(Throwable throwable) {
        super(throwable);
    }

}
