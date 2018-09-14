package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.helpers.ConsultaTSQL;
import br.jus.stj.siscovi.model.ValorRestituicaoDecimoTerceiroModel;

import java.sql.*;

public class TesteRestituicaoDecimoTerceiro {

    public static void main(String[] args){

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        RestituicaoDecimoTerceiro restituicaoDecimoTerceiro = new RestituicaoDecimoTerceiro(connectSQLServer.dbConnect());
        ConsultaTSQL consulta = new ConsultaTSQL(connectSQLServer.dbConnect());
        DecimoTerceiro decimoTerceiro = new DecimoTerceiro(connectSQLServer.dbConnect());

        int vCodContrato = consulta.RetornaCodContratoAleatorio();
        int vCodTerceirizadoContrato = consulta.RetornaCodTerceirizadoAleatorio(vCodContrato);
        String vTipoRestituicao = String.valueOf("RESGATE");
        String vLoginAtualizacao = String.valueOf("VSSOUSA");
        float vValorMovimentado = 1500;
        int vNumeroParcela = 0;
        String sqlDelete = "DELETE FROM TB_SALDO_RESIDUAL_DEC_TER; DELETE FROM TB_RESTITUICAO_DECIMO_TERCEIRO;";
        Date vDataInicioContagem = decimoTerceiro.RetornaDataInicioContagem(vCodTerceirizadoContrato);
        String vDataFim = String.valueOf(vDataInicioContagem.toLocalDate().getYear()) + "-12-31";

        System.out.print(vDataFim);
        Date vDataFimContagem = Date.valueOf(vDataFim);

        System.out.print("Dados do teste\nCOD_CONTRATO: " + vCodContrato + " COD_TERCEIRIZADO_CONTRATO: " +
                vCodTerceirizadoContrato + "\n");
        System.out.print("Tipo de restituição: " + vTipoRestituicao + "\n" + "Data de início da contagem: " + vDataInicioContagem
                + "\n" + "Data fim da contagem: " + vDataFimContagem + "\n");

        ValorRestituicaoDecimoTerceiroModel restituicao = restituicaoDecimoTerceiro.CalculaRestituicaoDecimoTerceiro(
                vCodTerceirizadoContrato, vNumeroParcela, vDataInicioContagem, vDataFimContagem);

        System.out.println(restituicao.getValorDecimoTerceiro());
        System.out.println(restituicao.getValorIncidenciaDecimoTerceiro());

        restituicaoDecimoTerceiro.RegistraRestituicaoDecimoTerceiro(vCodTerceirizadoContrato,
                vTipoRestituicao,
                vNumeroParcela,
                vDataInicioContagem,
                restituicao.getValorDecimoTerceiro(),
                restituicao.getValorIncidenciaDecimoTerceiro(),
                vValorMovimentado,
                vLoginAtualizacao);

        try {

            preparedStatement = connectSQLServer.dbConnect().prepareStatement(sqlDelete);
            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível deletar os dados de teste inseridos no banco.");

        }

    }

}
