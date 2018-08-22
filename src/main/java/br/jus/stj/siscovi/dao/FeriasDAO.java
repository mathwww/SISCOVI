package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.calculos.Ferias;
import br.jus.stj.siscovi.model.TerceirizadoFerias;

import java.sql.*;
import java.util.ArrayList;


public class FeriasDAO {
    private final Connection connection;

    public FeriasDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @param codigoContrato
     * @return
     */
    public ArrayList<TerceirizadoFerias> getListaTerceirizadoParaCalculoDeFerias(int codigoContrato) {
        ArrayList<TerceirizadoFerias> terceirizados = new ArrayList<>();
        String sql = "SELECT TC.COD, " +
                " T.NOME " +
                " FROM tb_terceirizado_contrato TC " +
                " JOIN " +
                " tb_terceirizado T ON T.COD=TC.COD_TERCEIRIZADO " +
                " WHERE COD_CONTRATO=? AND T.ATIVO='S'";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codigoContrato);
            Ferias ferias = new Ferias(connection);
            try(ResultSet resultSet = preparedStatement.executeQuery()){

                while(resultSet.next()) {

                    Date inicioPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(resultSet.getInt("COD"), 1);
                    Date fimPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(resultSet.getInt("COD"), 2);
                    TerceirizadoFerias terceirizadoFerias = new TerceirizadoFerias(resultSet.getInt("COD"), resultSet.getString("NOME"), inicioPeriodoAquisitivo, fimPeriodoAquisitivo);
                    terceirizados.add(terceirizadoFerias);
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Nenhum funcionário ativo encontrado para este contrato.");
        }
        return terceirizados;
    }


}
