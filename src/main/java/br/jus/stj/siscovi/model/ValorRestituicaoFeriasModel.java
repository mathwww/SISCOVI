package br.jus.stj.siscovi.model;

public class ValorRestituicaoFeriasModel {

    private final float valorFerias;
    private final float valorTercoConstitucional;
    private final float valorIncidenciaFerias;
    private final float valorIncidenciaTercoConstitucional;

    public ValorRestituicaoFeriasModel (float valorFerias, float valorTercoConstitucional, float valorIncidenciaFerias,
                                        float valorIncidenciaTercoConstitucional) {

        this.valorFerias = valorFerias;
        this.valorIncidenciaFerias = valorIncidenciaFerias;
        this.valorTercoConstitucional = valorTercoConstitucional;
        this.valorIncidenciaTercoConstitucional = valorIncidenciaTercoConstitucional;

    }

    public float getValorFerias () {

        return valorFerias;

    }

    public float getValorTercoConstitucional () {

        return valorTercoConstitucional;

    }

    public float getValorIncidenciaFerias () {

        return valorIncidenciaFerias;

    }

    public float getValorIncidenciaTercoConstitucional () {

        return valorIncidenciaTercoConstitucional;

    }

}
