package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.calculos.Ferias;
import br.jus.stj.siscovi.model.AvaliacaoFerias;
import br.jus.stj.siscovi.model.CalcularFeriasModel;
import br.jus.stj.siscovi.model.CalculoPendenteModel;
import br.jus.stj.siscovi.model.TerceirizadoFerias;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class FeriasDAO {
    private final Connection connection;

    public FeriasDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     * @param codigoContrato
     * @return
     */
    public ArrayList<TerceirizadoFerias> getListaTerceirizadoParaCalculoDeFerias(int codigoContrato) {
        ArrayList<TerceirizadoFerias> terceirizados = new ArrayList<>();
        String sql = "SELECT TC.COD, " +
                " T.NOME " +
                " FROM tb_terceirizado_contrato TC " +
                " JOIN " +
                " tb_terceirizado T ON T.COD=TC.COD_TERCEIRIZADO " +
                " WHERE COD_CONTRATO=? AND T.ATIVO='S'";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codigoContrato);
            Ferias ferias = new Ferias(connection);
            try(ResultSet resultSet = preparedStatement.executeQuery()){

                while(resultSet.next()) {

                    Date inicioPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(resultSet.getInt("COD"), 1);
                    Date fimPeriodoAquisitivo = ferias.DataPeriodoAquisitivo(resultSet.getInt("COD"), 2);

                    TerceirizadoFerias terceirizadoFerias = new TerceirizadoFerias(resultSet.getInt("COD"),
                            resultSet.getString("NOME"),
                            inicioPeriodoAquisitivo,
                            fimPeriodoAquisitivo,
                            ferias.ExisteFeriasTerceirizado(resultSet.getInt("COD")));
                    terceirizados.add(terceirizadoFerias);
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Nenhum funcionário ativo encontrado para este contrato.");
        }
        return terceirizados;
    }

    /**
     *  Returns a list of previous vacation calculus for all the employees that had It's vacation rights calculated and has not been apreciated by the responsible party
     * @param codigoContrato - Id of the contract user wants to get pending vacation calculus
     * @return
     */
    public List<CalculoPendenteModel> getCalculosPendentes(int codigoContrato) {
        List<CalculoPendenteModel> lista = new ArrayList<>();
        String sql = "SELECT rt.COD_TERCEIRIZADO_CONTRATO AS \"COD\"," +
                " u.nome AS \"Gestor\"," +
                " c.nome_empresa AS \"Empresa\"," +
                " c.numero_contrato AS \"Contrato N°\"," +
                " tr.nome AS \"Tipo de restituição\"," +
                " t.nome AS \"Terceirizado\"," +
                " f.nome AS \"Cargo\"," +
                " rt.data_inicio_periodo_aquisitivo AS \"Início do período aquisitivo\"," +
                " rt.data_fim_periodo_aquisitivo AS \"Fim do período aquisitivo\"," +
                " rt.data_inicio_usufruto AS \"Início do usufruto\"," +
                " rt.data_fim_usufruto AS \"Fim do usufruto\"," +
                " rt.dias_vendidos AS \"Dias vendidos\"," +
                " rt.parcela AS \"PARCELA\"," +
                " rt.valor_ferias AS \"Valor de férias\"," +
                " rt.valor_terco_constitucional AS \"Valor de 1/3\"," +
                " rt.incid_submod_4_1_ferias AS \"Incidência sobre férias\"," +
                " rt.incid_submod_4_1_terco AS \"Incidência sobre 1/3\"," +
                " rt.valor_ferias + rt.valor_terco_constitucional + rt.incid_submod_4_1_ferias + rt.incid_submod_4_1_terco AS \"Total\"," +
                " rt.AUTORIZADO," +
                " rt.RESTITUIDO" +
                " FROM tb_restituicao_ferias rt" +
                " JOIN tb_terceirizado_contrato tc ON tc.cod = rt.cod_terceirizado_contrato" +
                " JOIN tb_funcao_terceirizado ft ON ft.cod_terceirizado_contrato = tc.cod" +
                " JOIN tb_terceirizado t ON t.cod = tc.cod_terceirizado" +
                " JOIN tb_contrato c ON c.cod = tc.cod_contrato" +
                " JOIN tb_tipo_restituicao tr ON tr.cod = rt.cod_tipo_restituicao" +
                " JOIN tb_historico_gestao_contrato hgc ON hgc.cod_contrato = c.cod" +
                " JOIN tb_usuario u ON u.cod = hgc.cod_usuario" +
                " JOIN tb_funcao_contrato fc ON fc.cod = ft.cod_funcao_contrato" +
                " JOIN tb_funcao f ON f.cod = fc.cod_funcao" +
                " WHERE tc.COD_CONTRATO = ? AND AUTORIZADO IS NULL OR AUTORIZADO='N' OR AUTORIZADO='N'";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codigoContrato);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while(resultSet.next()) {
                    String status = "";
                    if(resultSet.getString("AUTORIZADO") == null ) {
                        status = "Em Análise";
                    }else if((resultSet.getString("AUTORIZADO") != null) && (resultSet.getString("RESTITUIDO") == null) ) {
                        status = "Aprovado";
                    }else if((resultSet.getString("AUTORIZADO") != null) && (resultSet.getString("RESTITUIDO") != null)) {
                        status = "Executado";
                    }
                    CalcularFeriasModel calcularFeriasModel = new CalcularFeriasModel(resultSet.getInt("COD"),
                            resultSet.getString("Tipo de restituição"),
                            resultSet.getInt("Dias vendidos"),
                            resultSet.getDate("Início do usufruto"),
                            resultSet.getDate("Fim do usufruto"),
                            resultSet.getDate("Início do período aquisitivo"),
                            resultSet.getDate("Fim do período aquisitivo"),
                            0,
                            resultSet.getInt("PARCELA"),
                            resultSet.getFloat("TOTAL"),
                            resultSet.getFloat("Valor de férias"),
                            resultSet.getFloat("Valor de 1/3"),
                            resultSet.getFloat("Incidência sobre férias"),
                            resultSet.getFloat("Incidência sobre 1/3"));
                    CalculoPendenteModel calculoPendenteModel = new CalculoPendenteModel(calcularFeriasModel,
                            resultSet.getString("Terceirizado"),
                            resultSet.getString("Cargo"),
                            status,
                            resultSet.getFloat("Total"));
                    lista.add(calculoPendenteModel);
                }
            }
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return lista;
    }

    public boolean salvaAvaliacaoCalculosFerias(AvaliacaoFerias avaliacaoFerias) {
        String sql = "UPDATE tb_restituicao_ferias" +
                " SET " +
                " DATA_INICIO_PERIODO_AQUISITIVO = ?," +
                " DATA_FIM_PERIODO_AQUISITIVO = ?," +
                " DATA_INICIO_USUFRUTO = ?," +
                " DATA_FIM_USUFRUTO = ?," +
                " DIAS_VENDIDOS = ?," +
                " VALOR_FERIAS = ?," +
                " VALOR_TERCO_CONSTITUCIONAL = ?," +
                " INCID_SUBMOD_4_1_FERIAS = ?," +
                " INCID_SUBMOD_4_1_TERCO = ?," +
                " PARCELA = ?," +
                " DATA_REFERENCIA = GETDATE()," +
                " AUTORIZADO = ?," +
                " OBSERVACAO = ?," +
                " LOGIN_ATUALIZACAO = ?," +
                " DATA_ATUALIZACAO = CURRENT_TIMESTAMP" +
                " WHERE COD_TERCEIRIZADO_CONTRATO = ?";
        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (CalculoPendenteModel calculoPendenteModel : avaliacaoFerias.getCalculosAvaliados()) {
                System.out.println(calculoPendenteModel);
                int i = 1;
                //preparedStatement.setString(1, calculoPendenteModel.getCalcularFeriasModel().getTipoRestituicao());
                preparedStatement.setDate(i++, calculoPendenteModel.getCalcularFeriasModel().getInicioPeriodoAquisitivo());
                preparedStatement.setDate(i++, calculoPendenteModel.getCalcularFeriasModel().getFimPeriodoAquisitivo());
                preparedStatement.setDate(i++, calculoPendenteModel.getCalcularFeriasModel().getInicioFerias());
                preparedStatement.setDate(i++, calculoPendenteModel.getCalcularFeriasModel().getFimFerias());
                preparedStatement.setInt(i++, calculoPendenteModel.getCalcularFeriasModel().getDiasVendidos());
                preparedStatement.setFloat(i++, calculoPendenteModel.getCalcularFeriasModel().getpTotalFerias());
                preparedStatement.setFloat(i++, calculoPendenteModel.getCalcularFeriasModel().getpTotalTercoConstitucional());
                preparedStatement.setFloat(i++, calculoPendenteModel.getCalcularFeriasModel().getpTotalIncidenciaFerias());
                preparedStatement.setFloat(i++, calculoPendenteModel.getCalcularFeriasModel().getpTotalIncidenciaTerco());
                preparedStatement.setInt(i++, calculoPendenteModel.getCalcularFeriasModel().getParcelas());
                preparedStatement.setString(i++,calculoPendenteModel.getStatus());
                preparedStatement.setString(i++, calculoPendenteModel.getObservacoes());
                preparedStatement.setString(i++, avaliacaoFerias.getUser().getUsername().toUpperCase());
                preparedStatement.setInt(i++, calculoPendenteModel.getCalcularFeriasModel().getCodTerceirizadoContrato());
                preparedStatement.executeUpdate();
            }
        }catch (SQLException sqle) {

        }
        return true;
    }
}
