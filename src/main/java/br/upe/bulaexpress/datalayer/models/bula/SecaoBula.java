package br.upe.bulaexpress.datalayer.models.bula;

import java.util.Optional;

public enum SecaoBula {

    INDICACAO(0),
    FUNCIONAMENTO(1),
    QUANDO_UTILIZAR(2),
    OQUE_SABER(3),
    ONDE_COMO_QUANTO_TEMPO_GUARDAR(4),
    COMO_UTILIZAR(5),
    OQUE_FAZER_ESQUECIMENTO(6),
    QUAIS_MALES(7),
    QUANTIDADE_MAIOR_QUE_INDICADA(8);

    private final int sectionNumber;

    SecaoBula(int sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public int getValue() {
        return this.sectionNumber;
    }

    public static Optional<SecaoBula> getSecaoByIndex(int index) {
        return Optional.of(SecaoBula.values()[index-1]);
    }

}
