package br.upe.bulaexpress.exceptions.apis.anvisa;

public class MedicamentoNotFound extends RuntimeException {

  public MedicamentoNotFound(){}

  public MedicamentoNotFound(String mensagem) {
    super(mensagem);
  }

  public MedicamentoNotFound(Throwable throwable) {
    super(throwable);
  }
}
