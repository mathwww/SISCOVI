package DAO;

import Model.CargoModel;
import Model.ContratoModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CargoDAO {
    private Connection connection;
    public CargoDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<CargoModel> getAllCargos() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<CargoModel> cargos = new ArrayList<>();
        try{
            preparedStatement = connection.prepareStatement("SELECT * FROM TB_FUNCAO ORDER BY NOME");
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                CargoModel cargo = new CargoModel(resultSet.getInt("COD"),
                        resultSet.getString("NOME"),
                        resultSet.getString("LOGIN_ATUALIZACAO"),
                        resultSet.getDate("DATA_ATUALIZACAO"));
                if (resultSet.getString("DESCRICAO") ==  null) {
                    cargo.setDescricao("-");
                }else {
                    cargo.setDescricao(resultSet.getString("DESCRICAO"));
                }
                cargos.add(cargo);
            }
            return cargos;
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }
    public ArrayList<CargoModel> getCargosDeUmContrato (int codigo) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<CargoModel> cargos = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT CC.COD_CARGO,CA.NOME,CA.DESCRICAO,CA.LOGIN_ATUALIZACAO,CA.DATA_ATUALIZACAO FROM tb_cargo_contrato " +
                    "CC join tb_FUNCAO CA on CA.cod=CC.COD_FUNCAO where CC.COD_CONTRATO=? ORDER BY CA.NOME");
            preparedStatement.setInt(1, codigo);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CargoModel cargo = new CargoModel(resultSet.getInt("COD_CARGO"),
                                                    resultSet.getString("NOME"),
                                                    resultSet.getString("LOGIN_ATUALIZACAO"),
                                                    resultSet.getDate("DATA_ATUALIZACAO"));
                if (resultSet.getString("DESCRICAO") != null) {
                    cargo.setDescricao(resultSet.getString("DESCRICAO"));
                }else {
                    cargo.setDescricao("-");
                }
                cargos.add(cargo);
            }
            return cargos;
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }
    public boolean cadastroCargos(ArrayList<CargoModel> cargos, String currentUser) {
        PreparedStatement preparedStatement = null;
        int a = 1;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement("INSERT INTO TB_FUNCAO (NOME, DESCRICAO, LOGIN_ATUALIZACAO, DATA_ATUALIZACAO) VALUES (?, ?, ?, CURRENT_TIMESTAMP)");
            for (CargoModel cargo : cargos) {
                if ((a % 4) == 0) {
                    a = 1;
                }
                preparedStatement.setString(a++, cargo.getNome());
                preparedStatement.setString(a++, cargo.getDescricao());
                preparedStatement.setString(a++, currentUser);
                preparedStatement.addBatch();
            }
            int [] updateCounts = preparedStatement.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return false;
    }
}
