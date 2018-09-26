package br.jus.stj.siscovi.model;

import java.util.ArrayList;

public class TotalMensalPendenteModel {
    private final ArrayList<TotalMensal> totaisMensais;
    private final String status;

    public TotalMensalPendenteModel(ArrayList<TotalMensal> totaisMensais, String status) {
        this.totaisMensais = totaisMensais;
        this.status = status;
    }
}
