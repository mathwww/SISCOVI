package br.jus.stj.siscovi.model;

import java.sql.Date;

public class TerceirizadoRescisao {
    private final int codTerceirizadoContrato;
    private final String nomeTerceirizado;
    private Date dataDesligamento;
    private String tipoRescisao;
    private String tipoRestituicao;

    public TerceirizadoRescisao (int codTerceirizadoContrato, String nomeTerceirizado) {
        this.codTerceirizadoContrato = codTerceirizadoContrato;
        this.nomeTerceirizado = nomeTerceirizado;
    }

    public int getCodTerceirizadoContrato() {
        return codTerceirizadoContrato;
    }

    public String getNomeTerceirizado() {
        return nomeTerceirizado;
    }

    public Date getDataDesligamento() {
        return dataDesligamento;
    }

    public String getTipoRescisao() {
        return tipoRescisao;
    }

    public String getTipoRestituicao() {
        return tipoRestituicao;
    }
}
