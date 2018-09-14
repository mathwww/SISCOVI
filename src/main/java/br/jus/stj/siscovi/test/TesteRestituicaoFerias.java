package br.jus.stj.siscovi.calculos;
import br.jus.stj.siscovi.helpers.ConsultaTSQL;
import br.jus.stj.siscovi.model.ValorRestituicaoFeriasModel;


import br.jus.stj.siscovi.dao.ConnectSQLServer;

import java.sql.*;

public class TesteRestituicaoFerias {

    public static void main(String[] args){

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        RestituicaoFerias restituicaoFerias = new RestituicaoFerias(connectSQLServer.dbConnect());
        ConsultaTSQL consulta = new ConsultaTSQL(connectSQLServer.dbConnect());
        Ferias ferias = new Ferias(connectSQLServer.dbConnect());

        int vCodContrato = consulta.RetornaCodContratoAleatorio();
        int vCodTerceirizadoContrato = consulta.RetornaCodTerceirizadoAleatorio(vCodContrato);
        String vTipoRestituicao = String.valueOf("MOVIMENTAÇÃO");
        String vLoginAtualizacao = String.valueOf("VSSOUSA");
        int vParcela = 0;
        int vDiasVendidos = 0;
        float vValorMovimentado = 12842;
        String sqlDelete = "DELETE FROM TB_SALDO_RESIDUAL_FERIAS; DELETE FROM TB_RESTITUICAO_FERIAS;";

        System.out.print("Dados do teste\nCOD_CONTRATO: " + vCodContrato + " COD_TERCEIRIZADO_CONTRATO: " +
                vCodTerceirizadoContrato + "\n");
        System.out.print("Tipo de restituição: " + vTipoRestituicao + "\nDias vendidos: " + vDiasVendidos + "\n");

        Date vInicioFerias = Date.valueOf("2017-09-01");
        Date vFimFerias = Date.valueOf("2017-09-30");;
        Date vInicioPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(vCodTerceirizadoContrato, 1);
        Date vFimPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(vCodTerceirizadoContrato, 2);

        ValorRestituicaoFeriasModel restituicao  = restituicaoFerias.CalculaRestituicaoFerias(vCodTerceirizadoContrato,
                vDiasVendidos, vInicioFerias, vFimFerias, vInicioPeriodoAquisitivo, vFimPeriodoAquisitivo);

        System.out.println(restituicao.getValorFerias());
        System.out.println(restituicao.getValorTercoConstitucional());
        System.out.println(restituicao.getValorIncidenciaFerias());
        System.out.println(restituicao.getValorIncidenciaTercoConstitucional());

        restituicaoFerias.RegistraRestituicaoFerias(vCodTerceirizadoContrato, vTipoRestituicao, vDiasVendidos,
                vInicioFerias, vFimFerias, vInicioPeriodoAquisitivo, vFimPeriodoAquisitivo, vParcela,
                vValorMovimentado, restituicao.getValorFerias(), restituicao.getValorTercoConstitucional(),
                restituicao.getValorIncidenciaFerias(), restituicao.getValorIncidenciaTercoConstitucional(), vLoginAtualizacao);

        try {

            preparedStatement = connectSQLServer.dbConnect().prepareStatement(sqlDelete);
            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível deletar os dados de teste inseridos no banco.");

        }

    }

}
