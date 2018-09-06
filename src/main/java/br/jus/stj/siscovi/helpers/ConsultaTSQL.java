package br.jus.stj.siscovi.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class ConsultaTSQL {

    private Connection connection;

    public ConsultaTSQL(Connection connection) {

        this.connection = connection;

    }

    public int RetornaCodContratoAleatorio () {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        int vCodContrato = 0;

        //Carregamento do código do contrato.

        try {

            preparedStatement = connection.prepareStatement("SELECT TOP 1 cod\n" +
                    " FROM tb_contrato;");

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodContrato = resultSet.getInt(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar o código do contrato.");

        }

        return vCodContrato;

    }

    public int RetornaCodTerceirizadoAleatorio (int pCodContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        int vCodTerceirizadoContrato = 0;

        //Carregamento do código do terceirizado no contrato.

        try {

            preparedStatement = connection.prepareStatement("SELECT cod\n" +
                    " FROM tb_terceirizado_contrato\n" +
                    " WHERE cod_contrato = ?;");

            preparedStatement.setInt(1, pCodContrato);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTerceirizadoContrato = resultSet.getInt(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar o código do terceirizado.");

        }

        return vCodTerceirizadoContrato;

    }

    public Date RetornaDataDisponibilizacaoTerceirizado (int pCodTerceirizadoContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        Date vDataDisponibilizacao = null;

        //Carregamento da data de disponibilização do terceirizado.

        try {

            preparedStatement = connection.prepareStatement("SELECT DATA_DISPONIBILIZACAO\n" +
                    " FROM tb_terceirizado_contrato\n" +
                    " WHERE cod = ?;");

            preparedStatement.setInt(1, pCodTerceirizadoContrato);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vDataDisponibilizacao = resultSet.getDate(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar a data de disponibilização do terceirizado.");

        }

        return vDataDisponibilizacao;

    }

    public Date RetornaDataInicioContrato (int pCodContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        Date dataInicioContrato = null;

        //Carregamento da data início do contrato.

        try {

            preparedStatement = connection.prepareStatement("SELECT MIN(data_inicio_vigencia)\n" +
                    " FROM tb_evento_contratual\n" +
                    " WHERE cod_contrato = ?");

            preparedStatement.setInt(1, pCodContrato);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                dataInicioContrato = resultSet.getDate(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar o código do contrato.");

        }

        return dataInicioContrato;

    }

}
