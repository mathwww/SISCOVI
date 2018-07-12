package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.dao.ConnectSQLServer;

public class TesteCalculoTotalMesal {
    public static void main(String[] args){

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        TotalMensalAReter totalMensalAReter = new TotalMensalAReter(connectSQLServer.dbConnect());
        totalMensalAReter.CalculaTotalMensal(1, 9, 2016);
    }
}
