package br.jus.stj.siscovi.model;

import java.sql.Date;

public class TerceirizadoDecimoTerceiro {

    private final int codigoTerceirizadoContrato;
    private final String nomeTerceirizado;
    private final Date incioContagem;
    private final float valorDisponivel;

    public TerceirizadoDecimoTerceiro (int codigoTerceirizadoContrato, String nomeTerceirizado, Date inicioContagem, float valorDisponivel) {

        this.codigoTerceirizadoContrato = codigoTerceirizadoContrato;
        this.nomeTerceirizado = nomeTerceirizado;
        this.incioContagem = inicioContagem;
        this.valorDisponivel = valorDisponivel;

    }

}