package br.jus.stj.siscovi.model;

import java.util.ArrayList;

public class TotalMensalPendenteModel {
    private final ListaTotalMensalData totaisMensais;
    private final String status;

    public TotalMensalPendenteModel(ListaTotalMensalData totaisMensais, String status) {
        this.totaisMensais = totaisMensais;
        this.status = status;
    }
}
