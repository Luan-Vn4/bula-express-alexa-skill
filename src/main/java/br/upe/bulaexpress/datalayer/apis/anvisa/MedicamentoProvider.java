package br.upe.bulaexpress.datalayer.apis.anvisa;

import br.upe.bulaexpress.datalayer.models.Medicamento;

public interface MedicamentoProvider {

    Medicamento getMedicamento(String nomeMedicamento, String fabricante);

}
