package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.model.*;

import javax.management.relation.RoleUnresolved;
import javax.xml.transform.Result;
import javax.xml.ws.Response;
import java.sql.*;
import java.util.ArrayList;

public class TotalMensalDAO {
    private final Connection connection;

    public TotalMensalDAO(Connection connection) {
        this.connection = connection;
    }

    public ArrayList<ListaTotalMensalData> getValoresCalculadosAnteriormente(int codContrato, int codGestor){
        ContratoDAO contratoDAO = new ContratoDAO(connection);
        ArrayList<DatasDeCalculoModel> datasDeCalculo = recuperaAnosDeCalculosAnteriores(codContrato);
        ArrayList<ListaTotalMensalData> lista = new ArrayList<>();
        for(int i = 0; i < datasDeCalculo.size(); i++) {
            ArrayList<TotalMensal> totais = new ArrayList<>();

            String sql = "SELECT  u.nome AS \"Gestor\"," +
                    "c.nome_empresa AS \"Empresa\",\n" +
                    " 'Contrato Nº: ' + c.numero_contrato AS \"Contrato\"," +
                    " f.nome AS \"Função\",\n" +
                    " tmr.data_referencia AS \"Data referência\"," +
                    " ROUND(SUM(tmr.ferias), 2) AS \"Férias retido\"," +
                    " ROUND(SUM(tmr.terco_constitucional), 2) AS \"Terço constitucional retido\"," +
                    " ROUND(SUM(tmr.decimo_terceiro), 2) AS \"Décimo terceiro retido\"," +
                    " ROUND(SUM(tmr.incidencia_submodulo_4_1), 2) AS \"Incidência retido\"," +
                    " ROUND(SUM(tmr.multa_fgts), 2) AS \"MULTA do FGTS retido\"," +
                    " ROUND(SUM(tmr.ferias) + SUM(tmr.terco_constitucional) + SUM(tmr.decimo_terceiro) + SUM(tmr.incidencia_submodulo_4_1) + SUM(tmr.multa_fgts), 2) AS \"Total retido\"" +
                    " FROM tb_funcao_contrato fc" +
                    " JOIN tb_contrato c ON c.cod = fc.cod_contrato" +
                    " JOIN tb_funcao f ON f.cod = fc.cod_funcao" +
                    " JOIN tb_funcao_terceirizado ft ON ft.cod_funcao_contrato = fc.cod" +
                    " JOIN tb_terceirizado_contrato tc ON tc.cod = ft.cod_terceirizado_contrato" +
                    " JOIN tb_total_mensal_a_reter tmr ON tmr.cod_terceirizado_contrato = tc.cod" +
                    " JOIN tb_historico_gestao_contrato hgc ON hgc.cod_contrato = c.cod" +
                    " JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                    " WHERE c.cod = ?" +
                    " AND MONTH(tmr.data_referencia) = ?" +
                    " AND YEAR(tmr.data_referencia) = ?" +
                    " AND hgc.cod_usuario = ?" +
                    " AND tmr.cod_funcao_terceirizado = ft.cod" +
                    " AND tmr.retido = 'S'" +
                    " AND tmr.autorizado = 'S'"+
                    " GROUP BY u.nome," +
                    " c.nome_empresa," +
                    " 'Contrato Nº: ' + c.numero_contrato," +
                    " f.nome," +
                    " tmr.data_referencia" +
                    " ORDER BY 1,2,3,4;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, codContrato);
                preparedStatement.setInt(2, datasDeCalculo.get(i).getMes());
                preparedStatement.setInt(3, datasDeCalculo.get(i).getAno());
                preparedStatement.setInt(4, codGestor);
                try (ResultSet resultSet = preparedStatement.executeQuery()){
                    while(resultSet.next()){
                        TotalMensal totalMensal = new TotalMensal(resultSet.getFloat(6), resultSet.getFloat(7), resultSet.getFloat(8),
                                resultSet.getFloat(9), resultSet.getFloat(10), resultSet.getFloat(11), resultSet.getString("FUNÇÃO"));
                        totais.add(totalMensal);
                    }
                    Date dataReferencia = Date.valueOf(datasDeCalculo.get(i).getAno() + "-" + datasDeCalculo.get(i).getMes() + "-01");
                    ListaTotalMensalData listaTotalMensalData = new ListaTotalMensalData(dataReferencia, totais);
                    lista.add(listaTotalMensalData);
                }

            } catch (SQLException e) {
                throw new NullPointerException("Erro ao tentar carregar os cálculos de Total Mensal a Reter para o contrato: " + codContrato);
            }
        }
        return lista;
    }
    protected ArrayList<DatasDeCalculoModel> recuperaAnosDeCalculosAnteriores (int codContrato) {
        ArrayList<DatasDeCalculoModel> tuplas = new ArrayList<>();
        String sql = "SELECT DISTINCT((DATA_REFERENCIA)) FROM TB_TOTAL_MENSAL_A_RETER TMR JOIN TB_FUNCAO_TERCEIRIZADO FT ON FT.COD=TMR.COD_FUNCAO_TERCEIRIZADO" +
        " JOIN TB_FUNCAO_CONTRATO FC ON FC.cod=FT.COD_FUNCAO_CONTRATO WHERE FC.COD_CONTRATO=? ORDER BY DATA_REFERENCIA ASC";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, codContrato);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    DatasDeCalculoModel datasDeCalculoModel = new DatasDeCalculoModel( resultSet.getDate("DATA_REFERENCIA").toLocalDate().getYear(),
                            resultSet.getDate("DATA_REFERENCIA").toLocalDate().getMonthValue());
                    tuplas.add(datasDeCalculoModel);
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Não há cálculos realizados ainda !");
        }
        return tuplas;
    }
    public ArrayList<TotalMensal> getCalculoRealizado(int codUsuario ,int codContrato,int mes, int ano){
        ArrayList<TotalMensal> lista = new ArrayList<>();
        String sql = "SELECT  u.nome AS \"Gestor\"," +
                "c.nome_empresa AS \"Empresa\"," +
                " 'Contrato Nº: ' + c.numero_contrato AS \"Contrato\"," +
                " f.nome AS \"Função\"," +
                " tmr.data_referencia AS \"Data referência\"," +
                " ROUND(SUM(tmr.ferias), 2) AS \"Férias retido\"," +
                " ROUND(SUM(tmr.terco_constitucional), 2) AS \"Terço constitucional retido\"," +
                " ROUND(SUM(tmr.decimo_terceiro), 2) AS \"Décimo terceiro retido\"," +
                " ROUND(SUM(tmr.incidencia_submodulo_4_1), 2) AS \"Incidência retido\"," +
                " ROUND(SUM(tmr.multa_fgts), 2) AS \"MULTA do FGTS retido\"," +
                " ROUND(SUM(tmr.ferias) + SUM(tmr.terco_constitucional) + SUM(tmr.decimo_terceiro) + SUM(tmr.incidencia_submodulo_4_1) + SUM(tmr.multa_fgts), 2) AS \"Total retido\"" +
                " FROM tb_funcao_contrato fc" +
                " JOIN tb_contrato c ON c.cod = fc.cod_contrato" +
                " JOIN tb_funcao f ON f.cod = fc.cod_funcao" +
                " JOIN tb_funcao_terceirizado ft ON ft.cod_funcao_contrato = fc.cod" +
                " JOIN tb_terceirizado_contrato tc ON tc.cod = ft.cod_terceirizado_contrato" +
                " JOIN tb_total_mensal_a_reter tmr ON tmr.cod_terceirizado_contrato = tc.cod" +
                " JOIN tb_historico_gestao_contrato hgc ON hgc.cod_contrato = c.cod" +
                " JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                " WHERE c.cod = ?" +
                " AND MONTH(tmr.data_referencia) = ?" +
                " AND YEAR(tmr.data_referencia) = ?" +
                " AND hgc.cod_usuario = ?" +
                " AND tmr.cod_funcao_terceirizado = ft.cod" +
                " GROUP BY u.nome," +
                " c.nome_empresa," +
                " 'Contrato Nº: ' + c.numero_contrato," +
                " f.nome," +
                " tmr.data_referencia" +
                " ORDER BY 1,2,3,4;";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, codContrato);
            preparedStatement.setInt(2, mes);
            preparedStatement.setInt(3, ano);
            preparedStatement.setInt(4, codUsuario);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while(resultSet.next()){
                    TotalMensal totalMensal = new TotalMensal(resultSet.getFloat(6), resultSet.getFloat(7), resultSet.getFloat(8), resultSet.getFloat(9),
                            resultSet.getFloat(10), resultSet.getFloat(11), resultSet.getString("Função"));
                    lista.add(totalMensal);
                }
            }

        } catch (SQLException e) {
            throw new NullPointerException("Erro ao tentar buscar cálculo de Total a mensal a reter com a data de referência: 01/" + mes + "/" + ano + ". Contrato: " + codContrato);
        }
        return lista;
    }
    public ArrayList<TotalMensalPendenteModel> getTotalMensalPendente(int codContrato, int codUsuario) {
        int codGestor = 0;
        String perfil = "";
        String perfilUsuario = "";
        String sql = "SELECT SIGLA FROM TB_USUARIO U JOIN TB_PERFIL_USUARIO PU ON PU.COD=U.COD_PERFIL WHERE U.COD=?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codUsuario);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                if(resultSet.next()) {
                   perfil =  resultSet.getString("SIGLA");
                }
            }
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        sql = "SELECT PG.SIGLA FROM TB_CONTRATO CO JOIN TB_HISTORICO_GESTAO_CONTRATO HGC ON HGC.COD_CONTRATO=CO.COD JOIN tb_perfil_gestao PG ON PG.COD=HGC.COD_PERFIL_GESTAO WHERE HGC.COD_USUARIO=? AND CO.COD=?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codUsuario);
            preparedStatement.setInt(2, codContrato);
            try(ResultSet resultSet  = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    perfilUsuario = resultSet.getString("SIGLA");
                }
            }
        }catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        if(perfil.equals("ADMINISTRADOR") || perfilUsuario.equals("1° SUBSTITUTO") || perfilUsuario.equals("2° SUBSTITUTO")) {
            sql = "SELECT HGC.COD_USUARIO FROM TB_CONTRATO CO JOIN tb_historico_gestao_contrato HGC ON HGC.COD_CONTRATO=CO.COD WHERE COD_CONTRATO=?";
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setInt(1, codContrato);
                try(ResultSet resultSet = preparedStatement.executeQuery()){
                    if(resultSet.next()) {
                        codGestor = resultSet.getInt("COD_USUARIO");
                    }
                }
            }catch(SQLException sqle) {
                sqle.printStackTrace();
            }
        }else {
            if(perfilUsuario.equals("GESTOR")) {
                codGestor = codUsuario;
            }else {
                System.err.println("Ação maliciosa detectada. Codigo Usuário: " + codUsuario + ". Codigo do Contrato da tentativa de  acesso: " + codContrato);
                throw new RuntimeException("Acesso negado ! Entre em contato com o responsável pelo Sistema");
            }
        }
        ArrayList<DatasDeCalculoModel> datasDeCalculo = recuperaAnosDeCalculosAnteriores(codContrato);
        ArrayList<TotalMensalPendenteModel> lista = new ArrayList<>();
        for(int i = 0; i < datasDeCalculo.size(); i++) {
        ArrayList<TotalMensal> totais = new ArrayList<>();
        sql = "SELECT u.nome AS \"Gestor\"," +
                " c.nome_empresa AS \"Empresa\"," +
                " 'Contrato Nº: ' + c.numero_contrato AS \"Contrato\"," +
                " f.nome AS \"Função\"," +
                " tmr.data_referencia AS \"Data referência\"," +
                " ROUND(SUM(tmr.ferias), 2) AS \"Férias retido\"," +
                " ROUND(SUM(tmr.terco_constitucional), 2) AS \"Terço constitucional retido\"," +
                " ROUND(SUM(tmr.decimo_terceiro), 2) AS \"Décimo terceiro retido\"," +
                " ROUND(SUM(tmr.incidencia_submodulo_4_1), 2) AS \"Incidência retido\"," +
                " ROUND(SUM(tmr.multa_fgts), 2) AS \"MULTA do FGTS retido\"," +
                " ROUND(SUM(tmr.ferias) + SUM(tmr.terco_constitucional) + SUM(tmr.decimo_terceiro) + SUM(tmr.incidencia_submodulo_4_1) + SUM(tmr.multa_fgts), 2) AS \"Total retido\"" +
                " FROM tb_funcao_contrato fc" +
                " JOIN tb_contrato c ON c.cod = fc.cod_contrato" +
                " JOIN tb_funcao f ON f.cod = fc.cod_funcao" +
                " JOIN tb_funcao_terceirizado ft ON ft.cod_funcao_contrato = fc.cod" +
                " JOIN tb_terceirizado_contrato tc ON tc.cod = ft.cod_terceirizado_contrato" +
                " JOIN tb_total_mensal_a_reter tmr ON tmr.cod_terceirizado_contrato = tc.cod" +
                " JOIN tb_historico_gestao_contrato hgc ON hgc.cod_contrato = c.cod" +
                " JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                " WHERE c.cod = ?" +
                " AND MONTH(tmr.data_referencia) = ?" +
                " AND YEAR(tmr.data_referencia) = ?" +
                " AND hgc.cod_usuario = ?" +
                " AND tmr.cod_funcao_terceirizado = ft.cod"+
                " AND tmr.AUTORIZADO is null" +
                " AND tmr.RETIDO is null" +
                " GROUP BY u.nome," +
                " c.nome_empresa," +
                " 'Contrato Nº: ' + c.numero_contrato," +
                " f.nome," +
                " tmr.data_referencia" +
                " ORDER BY 1,2,3,4";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, codContrato);
                preparedStatement.setInt(2, datasDeCalculo.get(i).getMes());
                preparedStatement.setInt(3, datasDeCalculo.get(i).getAno());
                preparedStatement.setInt(4, codGestor);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while(resultSet.next()){
                        TotalMensal totalMensal = new TotalMensal(resultSet.getFloat(6), resultSet.getFloat(7), resultSet.getFloat(8),
                                resultSet.getFloat(9), resultSet.getFloat(10), resultSet.getFloat(11), resultSet.getString("FUNÇÃO"));
                        totais.add(totalMensal);
                    }
                    if(totais.size() <= 0) {
                        return null;
                    }
                    Date dataReferencia = Date.valueOf(datasDeCalculo.get(i).getAno() + "-" + datasDeCalculo.get(i).getMes() + "-01");
                    ListaTotalMensalData listaTotalMensalData = new ListaTotalMensalData(dataReferencia, totais);
                    TotalMensalPendenteModel totalMensalPendenteModel = new TotalMensalPendenteModel(listaTotalMensalData, "EM ANÁLISE");
                    lista.add(totalMensalPendenteModel);
                }
            } catch (SQLException sqle) {
                System.err.println(sqle.getStackTrace());
                throw new RuntimeException("Houve um erro ao tentar recuperar os cálculos de Total Mensal a Reter !");
            }
        }
        return lista;
    }
}
