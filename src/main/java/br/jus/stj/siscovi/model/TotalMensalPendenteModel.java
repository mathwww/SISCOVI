package br.jus.stj.siscovi.model;

public class TotalMensalPendenteModel {
    private final ListaTotalMensalData totaisMensais;
    private final String status;
    private String observacoes;
    public TotalMensalPendenteModel(ListaTotalMensalData totaisMensais, String status) {
        this.totaisMensais = totaisMensais;
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    public ListaTotalMensalData getTotaisMensais() {
        return totaisMensais;
    }
    @Override
    public String toString() {
        return "TotalMensalPendenteModel{" +
                "totaisMensais=" + totaisMensais +
                ", status='" + status + '\'' +
                ", observacoes='" + observacoes + '\'' +
                '}';
    }
}
