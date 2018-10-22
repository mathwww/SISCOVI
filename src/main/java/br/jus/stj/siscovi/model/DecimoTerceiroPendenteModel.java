package br.jus.stj.siscovi.model;

import br.jus.stj.siscovi.calculos.RestituicaoDecimoTerceiro;

public class DecimoTerceiroPendenteModel {
    private final TerceirizadoDecimoTerceiro terceirizadoDecTer;
    private final String status;
    private final String observacoes;

    public DecimoTerceiroPendenteModel(TerceirizadoDecimoTerceiro terceirizadoDecTer, String status, String observacoes) {
        this.terceirizadoDecTer = terceirizadoDecTer;
        this.status = status;
        this.observacoes = observacoes;
    }

    public TerceirizadoDecimoTerceiro getTerceirizadoDecTer() {
        return terceirizadoDecTer;
    }

    public String getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    @Override
    public String toString() {
        return "DecimoTerceiroPendenteModel{" +
                "terceirizadoDecTer=" + terceirizadoDecTer +
                ", status='" + status + '\'' +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }
}
