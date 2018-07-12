package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.model.TotalMensal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TotalMensalDAO {
    private final Connection connection;

    public TotalMensalDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<TotalMensal> getValoresCalculados(){
        String sql = "";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();

        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}
