package DAO;

import Model.ContratoModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ContratoDAO {
    private Connection connection;

    public ContratoDAO(Connection connection){
        this.connection = connection;
    }

    public ArrayList<ContratoModel> retornaContratoDoUsuario(String username) throws NullPointerException,SQLException {
        ArrayList<ContratoModel> contratos = new ArrayList<ContratoModel>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT SIGLA FROM TB_PERFIL P JOIN tb_usuario U ON U.COD_PERFIL=P.cod WHERE U.LOGIN=?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getString("SIGLA").equals("ADMINISTRADOR")) {
                preparedStatement = connection.prepareStatement("SELECT DISTINCT C.COD, NOME_EMPRESA, CNPJ, NUMERO_CONTRATO, SE_ATIVO, DATA_INICIO, DATA_FIM, OBJETO " +
                        "FROM TB_CONTRATO C JOIN tb_historico_gestor_contrato HGC ON HGC.COD_CONTRATO=C.COD");
                resultSet = preparedStatement.executeQuery();
                while(resultSet.next()){
                    ContratoModel contrato = new ContratoModel(resultSet.getInt("COD"), resultSet.getString("NOME_EMPRESA"), resultSet.getString("CNPJ"));
                    contrato.setNumeroDoContrato(resultSet.getInt("NUMERO_CONTRATO"));
                    contrato.setAnoDoContrato(resultSet.getDate("DATA_INICIO").toLocalDate().getYear()); // RECUPERA O ANO DA DATA INÍCIO DO CONTRATO
                    contrato.setDataInicio(resultSet.getDate("DATA_INICIO"));
                    contrato.setNomeDaEmpresa(contrato.getNomeDaEmpresa());
                    if(resultSet.getString("SE_ATIVO").equals("S")) {
                        contrato.setSeAtivo("Sim");
                    }else {
                        contrato.setSeAtivo("Não");
                    }
                    if (resultSet.getDate("DATA_FIM") != null) {
                        contrato.setDataFim(resultSet.getDate("DATA_FIM"));
                    }else{
                        contrato.setDataFim(null);
                    }
                    if(resultSet.getString("OBJETO") == null){
                        contrato.setObjeto("-");
                    }else {
                        contrato.setObjeto(resultSet.getString("OBJETO"));
                    }
                    contratos.add(contrato);
                }
            }else{
                preparedStatement = connection.prepareStatement("SELECT DISTINCT C.COD , NOME_EMPRESA,CNPJ, NUMERO_CONTRATO, SE_ATIVO, DATA_INICIO, DATA_FIM, OBJETO  FROM TB_CONTRATO C" +
                        " JOIN tb_historico_gestor_contrato hgc ON hgc.cod_contrato = c.cod" +
                        " JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                        " JOIN tb_perfil p ON p.cod = u.cod_perfil" +
                        " WHERE u.login = ?");
                preparedStatement.setString(1, username);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    ContratoModel contrato = new ContratoModel(resultSet.getInt("COD"), resultSet.getString("NOME_EMPRESA"), resultSet.getString("CNPJ"));
                    contrato.setNumeroDoContrato(resultSet.getInt("NUMERO_CONTRATO"));
                    contrato.setAnoDoContrato(resultSet.getDate("DATA_INICIO").toLocalDate().getYear()); // RECUPERA O ANO DA DATA INÍCIO DO CONTRATO
                    contrato.setDataInicio(resultSet.getDate("DATA_INICIO"));
                    contrato.setSeAtivo(resultSet.getString("SE_ATIVO"));
                    if (resultSet.getDate("DATA_FIM") != null) {
                        contrato.setDataFim(resultSet.getDate("DATA_FIM"));
                    }else{
                        contrato.setDataFim(null);
                    }
                    if(resultSet.getString("OBJETO") == null){
                        contrato.setObjeto("-");
                    }else {
                        contrato.setObjeto(resultSet.getString("OBJETO"));
                    }
                    contratos.add(contrato);
                }
            }
            return contratos;
        }catch (NullPointerException npe){
            npe.printStackTrace();
        }
        catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return null;
    }
}
