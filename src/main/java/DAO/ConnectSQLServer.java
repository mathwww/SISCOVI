package DAO;
import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;

public class ConnectSQLServer {

    public Connection dbConnect() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection("jdbc:sqlserver://STJ195934\\STJ\\mmsousa:1433;database=SISCOVI;integratedSecurity=true");

            //System.out.println("Conectado");
           // Statement statement = conn.createStatement();
            //String queryString = "select * from TB_PERFIL";
            //ResultSet rs = statement.executeQuery(queryString);
            //while (rs.next()){
            //System.out.println(rs.getString(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5));
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
         }
        //public static void main(String[] args){
        //ConnectSQLServer connServer = new ConnectSQLServer();
       // connServer.dbConnect();
       // }
        //return conn;

}