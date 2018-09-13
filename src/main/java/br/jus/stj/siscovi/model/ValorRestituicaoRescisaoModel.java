package br.jus.stj.siscovi.model;

public class ValorRestituicaoRescisaoModel {

    private final float valorDecimoTerceiro;
    private final float valorIncidenciaDecimoTerceiro;
    private final float valorFGTSDecimoTerceiro;
    private final float valorFerias;
    private final float valorTerco;
    private final float valorIncidenciaFerias;
    private final float valorIncidenciaTerco;
    private final float valorFGTSFerias;
    private final float valorFGTSTerco;
    private final float valorFGTSSalario;

    public ValorRestituicaoRescisaoModel (float valorDecimoTerceiro,
                                          float valorIncidenciaDecimoTerceiro,
                                          float valorFGTSDecimoTerceiro,
                                          float valorFerias,
                                          float valorTerco,
                                          float valorIncidenciaFerias,
                                          float valorIncidenciaTerco,
                                          float valorFGTSFerias,
                                          float valorFGTSTerco,
                                          float valorFGTSSalario) {

        this.valorDecimoTerceiro = valorDecimoTerceiro;
        this.valorIncidenciaDecimoTerceiro = valorIncidenciaDecimoTerceiro;
        this.valorFGTSDecimoTerceiro = valorFGTSDecimoTerceiro;
        this.valorFerias = valorFerias;
        this.valorTerco = valorTerco;
        this.valorIncidenciaFerias = valorIncidenciaFerias;
        this.valorIncidenciaTerco = valorIncidenciaTerco;
        this.valorFGTSFerias = valorFGTSFerias;
        this.valorFGTSTerco = valorFGTSTerco;
        this.valorFGTSSalario = valorFGTSSalario;

    }

    public float getValorDecimoTerceiro () {

        return valorDecimoTerceiro;

    }

    public float getValorIncidenciaDecimoTerceiro () {

        return valorIncidenciaDecimoTerceiro;

    }

    public float getValorFGTSDecimoTerceiro () {

        return valorFGTSDecimoTerceiro;

    }

    public float getValorFerias () {

        return valorFerias;

    }

    public float getValorTerco () {

        return valorTerco;

    }

    public float getValorIncidenciaFerias () {

        return valorIncidenciaFerias;

    }

    public float getValorIncidenciaTerco () {

        return valorIncidenciaTerco;

    }

    public float getValorFGTSFerias () {

        return valorFGTSFerias;

    }

    public float getValorFGTSTerco () {

        return valorFGTSTerco;

    }

    public float getValorFGTSSalario () {

        return valorFGTSSalario;

    }

}
