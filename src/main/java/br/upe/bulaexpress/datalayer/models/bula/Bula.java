package br.upe.bulaexpress.datalayer.models.bula;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class Bula {

    @SerializedName("secoes")
    private List<String> secoes;

    public Bula(List<String> secoes) {
        this.secoes = secoes;
    }

    public String getConteudoSecao(SecaoBula secao) {
        return this.secoes.get(secao.getValue());
    }

}
