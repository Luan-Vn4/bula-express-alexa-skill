package br.upe.bulaexpress.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Medicamento {
  @SerializedName("nomeProduto")
  private String nome;

  @SerializedName("razaoSocial")
  private String fabricante;

  @SerializedName("idBulaPacienteProtegido")
  private String token;

}
