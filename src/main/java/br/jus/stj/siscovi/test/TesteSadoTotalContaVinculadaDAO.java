package br.jus.stj.siscovi.test;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.SaldoTotalContaVincualadaDAO;
import br.jus.stj.siscovi.model.SaldoTotalContaVinculada;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class TesteSadoTotalContaVinculadaDAO {

    public static void main (String[] args) {

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        SaldoTotalContaVincualadaDAO saldoTotalContaVincualadaDAO = new SaldoTotalContaVincualadaDAO(connectSQLServer.dbConnect());
        ArrayList<SaldoTotalContaVinculada> saldoLista = saldoTotalContaVincualadaDAO.getSaldoContaVinculadaContrato(41, 142);

        for (int i = 0; i < saldoLista.size(); i++) {

            System.out.print(saldoLista.get(i).getFuncao() + "\n");
            System.out.println(saldoLista.get(i).getValorFeriasRetido());
            System.out.println(saldoLista.get(i).getValorTotalRestituido());
            System.out.println(saldoLista.get(i).getValorSaldo());

        }

    }

}
