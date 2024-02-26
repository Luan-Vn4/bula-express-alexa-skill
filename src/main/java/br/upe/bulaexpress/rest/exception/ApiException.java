package br.upe.bulaexpress.rest.exception;

public class ApiException extends RuntimeException {
  public ApiException(String mensagem) {
    super(mensagem);
  }
}
