package br.jus.stj.siscovi.test;

        import br.jus.stj.siscovi.dao.ConnectSQLServer;
        import br.jus.stj.siscovi.dao.sql.ConsultaTSQL;


public class TestConsultaTSQL {

    public static void main (String[] args) {

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        ConsultaTSQL consulta = new ConsultaTSQL(connectSQLServer.dbConnect());

        int retorno;

        retorno = consulta.RetornaCodSequenceTable("TB_RUBRICA");

        System.out.print(retorno);

    }

}
