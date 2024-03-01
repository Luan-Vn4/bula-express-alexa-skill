package br.upe.bulaexpress.datalayer.models;

import br.upe.bulaexpress.datalayer.models.bula.Bula;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Medicamento {

  @SerializedName("idProduto")
  private long id;

  @SerializedName("nomeProduto")
  private String nome;

  @SerializedName("razaoSocial")
  private String fabricante;

  @SerializedName("idBulaPacienteProtegido")
  private String token;

  @SerializedName("bula")
  private Bula bula;

}
