package br.jus.stj.siscovi.model;

import java.sql.Date;

public class TerceirizadoDecimoTerceiro {
    private final int codigoTerceirizadoContrato;
    private final String nomeTerceirizado;
    private final Date inicioContagem;
    private final float valorDisponivel;
    private String tipoRestituicao;
    private final float valorMovimentado;
    private int parcelas;
    private Date fimContagem;

    public TerceirizadoDecimoTerceiro(int codigoTerceirizadoContrato, String nomeTerceirizado, Date inicioContagem, float valorDisponivel, float valorMovimentado) {
        this.codigoTerceirizadoContrato = codigoTerceirizadoContrato;
        this.nomeTerceirizado = nomeTerceirizado;
        this.inicioContagem = inicioContagem;
        this.valorDisponivel = valorDisponivel;
        this.valorMovimentado = valorMovimentado;
    }
    public void setTipoRestituicao(String tipoRestituicao) {
        this.tipoRestituicao = tipoRestituicao;
    }
    public int getParcelas() {
        return parcelas;
    }
    public Date getFimContagem() {
        return fimContagem;
    }
}