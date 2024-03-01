package br.upe.bulaexpress.exceptions.api.requests;

public class RequestBuildingException extends RequestException{

    public RequestBuildingException(String message) {
        super(message);
    }

    public RequestBuildingException(Throwable throwable) {
        super(throwable);
    }

}
