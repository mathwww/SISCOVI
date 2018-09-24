package br.jus.stj.siscovi.model;

import java.sql.Date;

public class TerceirizadoRescisao {
    private final int codTerceirizadoContrato;
    private final String nomeTerceirizado;

    public TerceirizadoRescisao (int codTerceirizadoContrato, String nomeTerceirizado) {
        this.codTerceirizadoContrato = codTerceirizadoContrato;
        this.nomeTerceirizado = nomeTerceirizado;
    }
}
