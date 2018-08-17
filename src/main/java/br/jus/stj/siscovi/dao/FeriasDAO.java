package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.model.TerceirizadoFerias;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

public class FeriasDAO {
    private final Connection connection;

    public FeriasDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<TerceirizadoFerias> getListaTerceirizadoParaCalculoDeFerias(int codigoContrato) {
        ArrayList<TerceirizadoFerias> terceirizados = new ArrayList<>();
        String sql = "SELECT TC.COD, " +
                "T.NOME, " +
                "TC.DATA_DISPONIBILIZACAO, " +
                "TC.DATA_DESLIGAMENTO " +
                "FROM tb_terceirizado_contrato TC " +
                "JOIN " +
                "tb_terceirizado T ON T.COD=TC.COD_TERCEIRIZADO " +
                "WHERE COD_CONTRATO=?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codigoContrato);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()) {
                    Date inicioPeriodoAquisitivo = null;
                    Date fimPeriodoAquisitivo;
                    Date primeiroDiaDoAno = Date.valueOf(LocalDate.now().with(firstDayOfYear())); //Primeiro dia do ano
                    if(resultSet.getDate("DATA_DISPONIBILIZACAO").before(primeiroDiaDoAno) && resultSet.getDate("DATA_DESLIGAMENTO") == null) {
                        inicioPeriodoAquisitivo = primeiroDiaDoAno;
                    }else if(resultSet.getDate("DATA_DISPONIBILIZACAO").after(primeiroDiaDoAno) || resultSet.getDate("DATA_DISPONIBILIZACAO").equals(primeiroDiaDoAno)) {
                        inicioPeriodoAquisitivo = primeiroDiaDoAno;
                    }

                    if(resultSet.getDate("DATA_DESLIGAMENTO") == null) {
                        fimPeriodoAquisitivo = Date.valueOf(inicioPeriodoAquisitivo.toLocalDate().plusYears(1));
                    }else {
                        fimPeriodoAquisitivo = resultSet.getDate("DATA_DESLIGAMENTO");
                    }
                    TerceirizadoFerias terceirizadoFerias = new TerceirizadoFerias(resultSet.getInt("COD"), resultSet.getString("NOME"), inicioPeriodoAquisitivo, fimPeriodoAquisitivo);
                    terceirizados.add(terceirizadoFerias);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return terceirizados;
    }
}
