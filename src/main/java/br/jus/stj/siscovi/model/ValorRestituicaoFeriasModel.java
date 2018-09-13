package br.jus.stj.siscovi.model;

public class ValorRestituicaoFeriasModel {

    private final float valorFerias;
    private final float valorTercoConstitucional;
    private final float valorIncidenciaFerias;
    private final float valorIncidenciaTercoConstitucional;

    public ValorRestituicaoFeriasModel (float valorFerias, float valorTercoConstitucional, float valorIncidenciaFerias,
                                        float valorIncidenciaTercoConstitucional) {

        this.valorFerias = Math.round(valorFerias * 100.0f) / 100.0f;
        this.valorIncidenciaFerias = Math.round(valorIncidenciaFerias * 100.0f) / 100.0f;
        this.valorTercoConstitucional = Math.round(valorTercoConstitucional * 100.0f) / 100.0f;
        this.valorIncidenciaTercoConstitucional = Math.round(valorIncidenciaTercoConstitucional * 100.0f) / 100.0f;

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
