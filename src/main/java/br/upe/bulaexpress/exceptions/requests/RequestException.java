package br.upe.bulaexpress.exceptions.requests;

public class RequestException extends RuntimeException {

    public RequestException() {}

    public RequestException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RequestException(String message) {
        super(message);
    }

    public RequestException(Throwable throwable) {
        super(throwable);
    }

}
