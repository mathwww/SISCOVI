package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.dao.sql.ConsultaTSQL;
import br.jus.stj.siscovi.dao.sql.InsertTSQL;
import br.jus.stj.siscovi.model.CargoModel;
import br.jus.stj.siscovi.model.ContratoModel;
import br.jus.stj.siscovi.model.HistoricoGestorModel;
import br.jus.stj.siscovi.model.PercentualModel;

import javax.xml.transform.Result;
import javax.xml.ws.Response;
import java.sql.*;
import java.util.ArrayList;

public class ContratoDAO {
    private Connection connection;

    public ContratoDAO(Connection connection){
        this.connection = connection;
    }

    /**
     *
     * @param username
     * @return
     * @throws NullPointerException
     * @throws SQLException
     */
    public ArrayList<ContratoModel> retornaContratoDoUsuario(String username) throws NullPointerException,SQLException {
        ArrayList<ContratoModel> contratos = new ArrayList<ContratoModel>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT SIGLA FROM TB_PERFIL_USUARIO P JOIN tb_usuario U ON U.COD_PERFIL=P.cod WHERE U.LOGIN=?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            if(resultSet.getString("SIGLA").equals("ADMINISTRADOR")) {
                preparedStatement = connection.prepareStatement("SELECT DISTINCT C.COD, NOME_EMPRESA, CNPJ, NUMERO_CONTRATO, SE_ATIVO, " +
                        " EC.DATA_INICIO_VIGENCIA as DATA_INICIO, EC.DATA_FIM_VIGENCIA AS DATA_FIM, OBJETO" +
                        " FROM TB_CONTRATO C" +
                        " JOIN tb_evento_contratual EC ON EC.COD_CONTRATO=C.COD\n" +
                        " JOIN TB_TIPO_EVENTO_CONTRATUAL TEC ON TEC.COD=EC.COD_TIPO_EVENTO\n" +
                        " WHERE TEC.TIPO='CONTRATO';");
                resultSet = preparedStatement.executeQuery();
                while(resultSet.next()){
                    ContratoModel contrato = new ContratoModel(resultSet.getInt("COD"), resultSet.getString("NOME_EMPRESA"), resultSet.getString("CNPJ"));
                    contrato.setNumeroDoContrato(resultSet.getString("NUMERO_CONTRATO"));
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
                preparedStatement = connection.prepareStatement("SELECT DISTINCT C.COD , NOME_EMPRESA,CNPJ, NUMERO_CONTRATO,hgc.data_inicio, hgc.data_fim, SE_ATIVO, OBJETO  FROM TB_CONTRATO C" +
                        " JOIN tb_historico_gestao_contrato hgc ON hgc.cod_contrato = c.cod" +
                        " JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                        " JOIN tb_perfil_usuario p ON p.cod = u.cod_perfil" +
                        " WHERE u.login = ?");
                preparedStatement.setString(1, username);
                resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    ContratoModel contrato = new ContratoModel(resultSet.getInt("COD"), resultSet.getString("NOME_EMPRESA"), resultSet.getString("CNPJ"));
                    contrato.setNumeroDoContrato(resultSet.getString("NUMERO_CONTRATO"));
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

    /**
     *
     * @param codigo
     * @return
     */
    public String retornaNomeDoGestorDoContrato(int codigo) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            preparedStatement = connection.prepareStatement("SELECT U.NOME FROM TB_USUARIO U JOIN tb_historico_gestao_contrato HGC ON HGC.COD_USUARIO=U.cod " +
                    "JOIN TB_CONTRATO C ON  C.cod=HGC.COD_CONTRATO WHERE C.COD = ?");
            preparedStatement.setInt(1, codigo);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString("NOME");
            }
        }catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param codigoUsuario
     * @param codigoContrato
     * @return
     */
    public int codigoGestorContrato(int codigoUsuario, int codigoContrato) {
        int codigoGestor = 0;
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT PU.SIGLA AS \"USUARIO\", COD_USUARIO FROM TB_USUARIO U" +
                " JOIN TB_PERFIL_USUARIO PU ON PU.COD=U.COD_PERFIL" +
                " JOIN tb_historico_gestao_contrato HGC ON HGC.COD_CONTRATO=?" +
                " JOIN TB_PERFIL_GESTAO PG ON PG.COD=HGC.COD_PERFIL_GESTAO WHERE U.COD=?")){
            preparedStatement.setInt(1, codigoContrato);
            preparedStatement.setInt(2, codigoUsuario);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()) {
                    if(resultSet.getInt("COD_USUARIO") == codigoUsuario) {
                        codigoGestor = codigoUsuario;
                    }else if(resultSet.getString("USUARIO").equals("USUÁRIO") || resultSet.getString(1).equals("GESTOR") || resultSet.getString(1).equals("1° SUBSTITUTO") ||
                                resultSet.getString(1).equals("2° SUBSTITUTO")){
                        codigoGestor = resultSet.getInt("COD_USUARIO");
                    }
                    if(resultSet.getString(1).equals("ADMINISTRADOR")){
                        codigoGestor = resultSet.getInt("COD_USUARIO");
                    }
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Erro ao tentar recuperar cálculos anteriores. Erro na função: 'codigoGestorContrato em ContratoDao.class'");
        }
        return codigoGestor;
    }

    public boolean cadastrarContrato(ContratoModel contrato, String username) throws RuntimeException, SQLException {
        this.connection.setAutoCommit(false);
        Savepoint savepoint = this.connection.setSavepoint("Savepoint1");
        InsertTSQL insertTSQL = new InsertTSQL(connection);
        ConsultaTSQL consultaTSQL = new ConsultaTSQL(connection);
        int vCodContrato = 0;
        int vCodUsuarioGestor = 0;

        try {
            vCodContrato = insertTSQL.InsertContrato(contrato.getNomeDaEmpresa(), contrato.getCnpj(), contrato.getNumeroDoContrato(), contrato.getNumeroProcessoSTJ(), contrato.getSeAtivo(),
                    contrato.getObjeto(), username);
            if(vCodContrato != 0) {
                for(HistoricoGestorModel hgc: contrato.getHistoricoGestao()) {
                    String sql = "SELECT COD FROM TB_USUARIO WHERE NOME=?";
                    try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                        String nomeGestor = "";
                        preparedStatement.setString(1, hgc.getGestor());
                        try(ResultSet resultSet = preparedStatement.executeQuery()) {
                            if(resultSet.next()) {
                                vCodUsuarioGestor = resultSet.getInt("COD");
                            }
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException("Erro. Usuário indicado para gestor do contrato não existe no sistema !");
                    }
                    insertTSQL.InsertHistoricoGestaoContrato(vCodContrato, vCodUsuarioGestor, hgc.getCodigoPerfilGestao(), hgc.getInicio(), hgc.getFim(), username);
                }
                for(PercentualModel pm: contrato.getPercentuais()) {
                    insertTSQL.InsertPercentualContrato(vCodContrato, pm.getRubrica().getCodigo(), pm.getPercentual(), pm.getDataInicio(), pm.getDataFim(), pm.getDataAditamento(), username);
                }
                for(CargoModel cm: contrato.getFuncoes()) {
                    int vCodFuncaoContrato = insertTSQL.InsertFuncaoContrato(vCodContrato, cm.getCodigo(), cm.getDescricao(), username);
                    insertTSQL.InsertRemuneracaoFunCon(vCodFuncaoContrato,
                            cm.getConvencao().getCodigo(),
                            contrato.getDataInicio(),
                            null,
                            contrato.getDataInicio(),
                            cm.getRemuneracao(),
                            cm.getAdicionais(),
                            cm.getTrienios(),
                            username);
                }
                if(vCodContrato != 0) {
                    insertTSQL.InsertEventoContratual(vCodContrato,
                            retornaCodEventoContratual("CONTRATO"),
                            "N",
                            null ,
                            contrato.getDataInicio(),
                            contrato.getDataFim(),
                            contrato.getDataAssinatura(),
                            username);
                }
            }
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        }catch (NullPointerException npe) {
            npe.printStackTrace();
            connection.rollback(savepoint);
            throw new RuntimeException("" + npe.getMessage());
        }
    }

    public int retornaCodEventoContratual(String tipoEventoContratual) {
        String sql = "SELECT COD FROM TB_TIPO_EVENTO_CONTRATUAL WHERE TIPO = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tipoEventoContratual);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                    return  resultSet.getInt("COD");
                }
            }
        }catch (SQLException  sqle) {
            throw new RuntimeException("Tipo de evento contratual não encontrado. " + sqle.getMessage());
        }
        return 0;
    }
}
