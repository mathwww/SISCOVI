package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.calculos.DecimoTerceiro;
import br.jus.stj.siscovi.calculos.Saldo;
import br.jus.stj.siscovi.model.TerceirizadoDecimoTerceiro;
import java.sql.*;
import java.util.ArrayList;

public class DecimoTerceiroDAO {
    private Connection connection;

    public DecimoTerceiroDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * @param codigoContrato
     * @return
     */
    public ArrayList<TerceirizadoDecimoTerceiro> getListaTerceirizadoParaCalculoDeDecimoTerceiro(int codigoContrato) {
        ArrayList<TerceirizadoDecimoTerceiro> terceirizados = new ArrayList<>();
        String sql = "SELECT TC.COD, " +
                " T.NOME" +
                " FROM tb_terceirizado_contrato TC " +
                " JOIN tb_terceirizado T ON T.COD = TC.COD_TERCEIRIZADO " +
                " WHERE COD_CONTRATO = ? AND T.ATIVO = 'S'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codigoContrato);
            DecimoTerceiro decimoTerceiro = new DecimoTerceiro(connection);
            Saldo saldoDecimoTerceiro = new Saldo(connection);
            float vSaldoDecimoTericeiro = 0; //Este saldo é correspondente ao ano da data de início da contagem.
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Date inicioContagem = decimoTerceiro.RetornaDataInicioContagem(resultSet.getInt("COD"));
                    vSaldoDecimoTericeiro = saldoDecimoTerceiro.SaldoContaVinculada(resultSet.getInt("COD"), inicioContagem.toLocalDate().getYear(), 1, 3);
                    TerceirizadoDecimoTerceiro terceirizadoDecimoTerceiro = new TerceirizadoDecimoTerceiro(resultSet.getInt("COD"),
                            resultSet.getString("NOME"),
                            inicioContagem,
                            vSaldoDecimoTericeiro,
                            0);
                    terceirizados.add(terceirizadoDecimoTerceiro);
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Nenhum funcionário ativo encontrado para este contrato.");
        }
        return terceirizados;
    }
}