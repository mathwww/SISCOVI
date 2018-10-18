package br.jus.stj.siscovi.model;

import java.sql.Date;

public class CalculoPendenteModel {
    private final CalcularFeriasModel calcularFeriasModel;
    private final String nomeTerceirizado;
    private final String nomeCargo;
    private final String status;
    private final float total;
    public CalculoPendenteModel(CalcularFeriasModel calcularFeriasModel, String nomeTerceirizado, String nomeCargo, String status, float total) {
        this.calcularFeriasModel = calcularFeriasModel;
        this.nomeTerceirizado = nomeTerceirizado;
        this.nomeCargo = nomeCargo;
        this.status = status;
        this.total = total;
    }

}