package br.jus.stj.siscovi.calculos;
import br.jus.stj.siscovi.dao.sql.*;
import br.jus.stj.siscovi.model.ValorRestituicaoFeriasModel;


import br.jus.stj.siscovi.dao.ConnectSQLServer;

import java.sql.*;

public class TesteRestituicaoFerias {

    public static void main(String[] args){

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();

        RestituicaoFerias restituicaoFerias = new RestituicaoFerias(connectSQLServer.dbConnect());
        ConsultaTSQL consulta = new ConsultaTSQL(connectSQLServer.dbConnect());
        DeleteTSQL delete = new DeleteTSQL(connectSQLServer.dbConnect());
        UpdateTSQL update = new UpdateTSQL(connectSQLServer.dbConnect());

        Ferias ferias = new Ferias(connectSQLServer.dbConnect());

        int vCodContrato = consulta.RetornaCodContratoAleatorio();
        int retorno;
        int vCodTerceirizadoContrato = consulta.RetornaCodTerceirizadoAleatorio(vCodContrato);
        String vTipoRestituicao = String.valueOf("RESGATE");
        String vLoginAtualizacao = String.valueOf("VSSOUSA");
        int vParcela = 0;
        int vDiasVendidos = 0;
        float vValorMovimentado = 12842;

        System.out.print("Dados do teste\nCOD_CONTRATO: " + vCodContrato + " COD_TERCEIRIZADO_CONTRATO: " +
                vCodTerceirizadoContrato + "\n");
        System.out.print("Tipo de restituição: " + vTipoRestituicao + "\nDias vendidos: " + vDiasVendidos + "\n");

        Date vInicioFerias = Date.valueOf("2017-09-01");
        Date vFimFerias = Date.valueOf("2017-09-30");
        Date vInicioPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(vCodTerceirizadoContrato, 1);
        Date vFimPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(vCodTerceirizadoContrato, 2);

        ValorRestituicaoFeriasModel restituicao  = restituicaoFerias.CalculaRestituicaoFerias(vCodTerceirizadoContrato,
                vDiasVendidos, vInicioFerias, vFimFerias, vInicioPeriodoAquisitivo, vFimPeriodoAquisitivo);

        System.out.println(restituicao.getValorFerias());
        System.out.println(restituicao.getValorTercoConstitucional());
        System.out.println(restituicao.getValorIncidenciaFerias());
        System.out.println(restituicao.getValorIncidenciaTercoConstitucional());

        retorno = restituicaoFerias.RegistraRestituicaoFerias(vCodTerceirizadoContrato, vTipoRestituicao, vDiasVendidos,
                vInicioFerias, vFimFerias, vInicioPeriodoAquisitivo, vFimPeriodoAquisitivo, vParcela,
                vValorMovimentado, restituicao.getValorFerias(), restituicao.getValorTercoConstitucional(),
                restituicao.getValorIncidenciaFerias(), restituicao.getValorIncidenciaTercoConstitucional(), vLoginAtualizacao);

        update.UpdateRestituicaoFerias(retorno, vTipoRestituicao, vInicioPeriodoAquisitivo, vFimPeriodoAquisitivo,
                vInicioFerias, vFimFerias, vDiasVendidos, 0, 0, 0,
                0, 0, vInicioFerias, 'N', 'N', String.valueOf(""), String.valueOf(""));

        delete.DeleteSaldoResidualFerias(retorno);
        delete.DeleteRestituicaoFerias(retorno);

    }

}
