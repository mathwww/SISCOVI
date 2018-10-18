package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.calculos.Saldo;
import br.jus.stj.siscovi.model.SaldoTotalContaVinculada;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.Connection;

public class SaldoTotalContaVincualadaDAO {

    private final Connection connection;

    public SaldoTotalContaVincualadaDAO (Connection connection) {

        this.connection = connection;

    }

    public ArrayList<SaldoTotalContaVinculada> getSaldoContaVinculadaContrato (int pCodContrato, int pCodGestorContrato) {

        ArrayList<SaldoTotalContaVinculada> lista = new ArrayList<>();

        int vCodFuncaoContrato = 0;

        String sql = "SELECT  u.nome," +
                           "  c.nome_empresa," +
                           "  c.numero_contrato," +
                           "  f.nome ," +
                           "  c.cod ," +
                           "  fc.cod" +
                       " FROM tb_funcao_contrato fc" +
                      "  JOIN tb_contrato c ON c.cod = fc.cod_contrato" +
                      "  JOIN tb_funcao f ON f.cod = fc.cod_funcao" +
                      "  JOIN tb_historico_gestao_contrato hgc ON hgc.cod_contrato = c.cod" +
                      "  JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                      " WHERE c.cod = ?" +
                   "      AND hgc.cod_usuario = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, pCodContrato);
            preparedStatement.setInt(2, pCodGestorContrato);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                resultSet.next();

                vCodFuncaoContrato = resultSet.getInt(6);

                do {

                    Saldo saldoContaVinculada = new Saldo(connection);

                    SaldoTotalContaVinculada saldoTotalContaVinculada =

                            new SaldoTotalContaVinculada(resultSet.getString(4),
                                                         resultSet.getString(1),
                                                         resultSet.getString(2),
                                                         resultSet.getString(3),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 1,1),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 1,2),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 1,3),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 1,7),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 1,100),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,1),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,2),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 3,3),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,100) + saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 3,100),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,101),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,102),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,100),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 3,103),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 3,100),
                                                         saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 1,100) - (saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 2,100) + saldoContaVinculada.SaldoTotalContaVinculada(pCodContrato, vCodFuncaoContrato, 3,100)));

                    lista.add(saldoTotalContaVinculada);

                } while (resultSet.next());

            }

        } catch (SQLException sqle) {

            sqle.printStackTrace();

            throw new NullPointerException("Falha na aquisição do saldo da conta vinculada.");

        }

        return lista;

    }

}
