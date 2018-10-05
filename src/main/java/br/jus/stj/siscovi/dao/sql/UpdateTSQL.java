package br.jus.stj.siscovi.dao.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateTSQL {

    private Connection connection;

    public UpdateTSQL(Connection connection) {

        this.connection = connection;

    }

    /**
     * Método que atualiza um registro da tabela de restituição de férias.
     *
     * @param pCodRestituicaoFerias;
     * @param pCodTipoRestituicao;
     * @param pInicioPeriodoAquisitivo;
     * @param pFimPeriodoAquisitivo;
     * @param pInicioFerias;
     * @param pFimFerias;
     * @param pDiasVendidos;
     * @param pTotalFerias;
     * @param pTotalTercoConstitucional;
     * @param pTotalIncidenciaFerias;
     * @param pTotalIncidenciaTerco;
     * @param pParcela;
     * @param pAutorizado;
     * @param pRestituido;
     * @param pObservacao;
     * @param pLoginAtualizacao;
     */

    public void UpdateRestituicaoFerias (int pCodRestituicaoFerias,
                                         int pCodTipoRestituicao,
                                         Date pInicioPeriodoAquisitivo,
                                         Date pFimPeriodoAquisitivo,
                                         Date pInicioFerias,
                                         Date pFimFerias,
                                         int pDiasVendidos,
                                         float pTotalFerias,
                                         float pTotalTercoConstitucional,
                                         float pTotalIncidenciaFerias,
                                         float pTotalIncidenciaTerco,
                                         int pParcela,
                                         String pAutorizado,
                                         String pRestituido,
                                         String pObservacao,
                                         String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        if (pRestituido.length() > 1 || pAutorizado.length() > 1) {

            throw new NullPointerException("Argumento em autorizado ou restituído está fora do padrão esperado (S ou N).");

        }

        String vSQLQuerry = "UPDATE tb_restituicao_ferias" +
                " SET COD_TIPO_RESTITUICAO = ?," +
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
                " RESTITUIDO = ?," +
                " OBSERVACAO = ?," +
                " LOGIN_ATUALIZACAO = ?," +
                " DATA_ATUALIZACAO = CURRENT_TIMESTAMP" +
                " WHERE cod = ?";

        try {

            preparedStatement = connection.prepareStatement(vSQLQuerry);
            preparedStatement.setInt(1, pCodTipoRestituicao);
            preparedStatement.setDate(2, pInicioPeriodoAquisitivo);
            preparedStatement.setDate(3, pFimPeriodoAquisitivo);
            preparedStatement.setDate(4, pInicioFerias);
            preparedStatement.setDate(5, pFimFerias);
            preparedStatement.setInt(6, pDiasVendidos);
            preparedStatement.setFloat(7, pTotalFerias);
            preparedStatement.setFloat(8, pTotalTercoConstitucional);
            preparedStatement.setFloat(9, pTotalIncidenciaFerias);
            preparedStatement.setFloat(10, pTotalIncidenciaTerco);
            preparedStatement.setInt(11, pParcela);
            preparedStatement.setString(12, pAutorizado);
            preparedStatement.setString(13, pRestituido);
            preparedStatement.setString(14, pObservacao);
            preparedStatement.setString(15, pLoginAtualizacao);
            preparedStatement.setInt(16, pCodRestituicaoFerias);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            sqle.printStackTrace();

            throw new NullPointerException("Erro na execução da atualização dos dados da restiuição de férias.");

        }

    }

    /**
     * Método que atualiza um registro da tabela de restituição de décimo terceiro.
     *
     * @param pCodRestituicaoDecimoTerceiro;
     * @param pTipoRestituicao;
     * @param pParcela;
     * @param pInicioContagem;
     * @param pValorDecimoTerceiro;
     * @param pValorIncidencia;
     * @param pDataReferencia;
     * @param pAutorizado;
     * @param pRestituido;
     * @param pObservacao;
     * @param pLoginAtualizacao;
     */

    public void UpdateRestituicaoDecimoTerceiro (int pCodRestituicaoDecimoTerceiro,
                                                 String pTipoRestituicao,
                                                 int pParcela,
                                                 Date pInicioContagem,
                                                 float pValorDecimoTerceiro,
                                                 float pValorIncidencia,
                                                 Date pDataReferencia,
                                                 String pAutorizado,
                                                 String pRestituido,
                                                 String pObservacao,
                                                 String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);
        int vCodTipoRestituicao = consulta.RetornaCodTipoRestituicao(pTipoRestituicao);

        if (pRestituido.length() > 1 || pAutorizado.length() > 1) {

            throw new NullPointerException("Argumento em autorizado ou restituído está fora do padrão esperado (S ou N).");

        }

        String vSQLQuerry = "UPDATE tb_restituicao_decimo_terceiro" +
                " SET COD_TIPO_RESTITUICAO = ?," +
                " PARCELA = ?," +
                " DATA_INICIO_CONTAGEM = ?," +
                " VALOR = ?," +
                " INCIDENCIA_SUBMODULO_4_1 = ?," +
                " DATA_REFERENCIA = ?," +
                " AUTORIZADO = ?," +
                " RESTITUIDO = ?," +
                " OBSERVACAO = ?," +
                " LOGIN_ATUALIZACAO = ?," +
                " DATA_ATUALIZACAO = CURRENT_TIMESTAMP" +
                " WHERE cod = ?";

        try {

            preparedStatement = connection.prepareStatement(vSQLQuerry);
            preparedStatement.setInt(1, vCodTipoRestituicao);
            preparedStatement.setInt(2, pParcela);
            preparedStatement.setDate(3, pInicioContagem);
            preparedStatement.setFloat(4, pValorDecimoTerceiro);
            preparedStatement.setFloat(5, pValorIncidencia);
            preparedStatement.setDate(6, pDataReferencia);
            preparedStatement.setString(7, pAutorizado);
            preparedStatement.setString(8, pRestituido);
            preparedStatement.setString(9, pObservacao);
            preparedStatement.setString(10, pLoginAtualizacao);
            preparedStatement.setInt(11, pCodRestituicaoDecimoTerceiro);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Erro na execução da atualização dos dados da restiuição de décimo tercero.");

        }

    }

    /**
     * Método que atualiza um registro da tabela de restituição de férias.
     *
     * @param pCodRestituicaoRescisao;
     * @param pTipoRestituicao;
     * @param pTipoRescisao;
     * @param pDataDesligamento;
     * @param pValorDecimoTerceiro;
     * @param pValorIncidenciaDecimoTerceiro;
     * @param pValorFGTSDecimoTerceiro;
     * @param pValorFerias;
     * @param pValorTerco;
     * @param pValorIncidenciaFerias;
     * @param pValorIncidenciaTerco;
     * @param pValorFGTSFerias;
     * @param pValorFGTSTerco;
     * @param pValorFGTSSalario;
     * @param pDataReferencia;
     * @param pAutorizado;
     * @param pRestituido;
     * @param pObservacao;
     * @param pLoginAtualizacao;
     */

    public void UpdateRestituicaoRescisao (int pCodRestituicaoRescisao,
                                           String pTipoRestituicao,
                                           String pTipoRescisao,
                                           Date pDataDesligamento,
                                           float pValorDecimoTerceiro,
                                           float pValorIncidenciaDecimoTerceiro,
                                           float pValorFGTSDecimoTerceiro,
                                           float pValorFerias,
                                           float pValorTerco,
                                           float pValorIncidenciaFerias,
                                           float pValorIncidenciaTerco,
                                           float pValorFGTSFerias,
                                           float pValorFGTSTerco,
                                           float pValorFGTSSalario,
                                           Date pDataReferencia,
                                           char pAutorizado,
                                           char pRestituido,
                                           String pObservacao,
                                           String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);
        int vCodTipoRestituicao = consulta.RetornaCodTipoRestituicao(pTipoRestituicao);
        int vCodTipoRescisao = consulta.RetornaCodTipoRescisao(pTipoRescisao);

        String vSQLQuery = "UPDATE tb_restituicao_rescisao" +
                " SET COD_TIPO_RESTITUICAO = ?," +
                " COD_TIPO_RESCISAO = ?," +
                " DATA_DESLIGAMENTO = ?," +
                " VALOR_DECIMO_TERCEIRO = ?," +
                " INCID_SUBMOD_4_1_DEC_TERCEIRO = ?," +
                " INCID_MULTA_FGTS_DEC_TERCEIRO = ?," +
                " VALOR_FERIAS = ?," +
                " VALOR_TERCO = ?," +
                " INCID_SUBMOD_4_1_FERIAS = ?," +
                " INCID_SUBMOD_4_1_TERCO = ?," +
                " INCID_MULTA_FGTS_FERIAS = ?," +
                " INCID_MULTA_FGTS_TERCO = ?," +
                " MULTA_FGTS_SALARIO = ?," +
                " DATA_REFERENCIA = ?," +
                " AUTORIZADO = ?," +
                " RESTITUIDO = ?," +
                " OBSERVACAO = ?," +
                " LOGIN_ATUALIZACAO = ?," +
                " DATA_ATUALIZACAO = CURRENT_TIMESTAMP" +
                " WHERE cod = ?";

        try {

            preparedStatement = connection.prepareStatement(vSQLQuery);
            preparedStatement.setInt(1, vCodTipoRestituicao);
            preparedStatement.setInt(2, vCodTipoRescisao);
            preparedStatement.setDate(3, pDataDesligamento);
            preparedStatement.setFloat(4, pValorDecimoTerceiro);
            preparedStatement.setFloat(5, pValorIncidenciaDecimoTerceiro);
            preparedStatement.setFloat(6, pValorFGTSDecimoTerceiro);
            preparedStatement.setFloat(7, pValorFerias);
            preparedStatement.setFloat(8, pValorTerco);
            preparedStatement.setFloat(9, pValorIncidenciaFerias);
            preparedStatement.setFloat(10, pValorIncidenciaTerco);
            preparedStatement.setFloat(11, pValorFGTSFerias);
            preparedStatement.setFloat(12, pValorFGTSTerco);
            preparedStatement.setFloat(13, pValorFGTSSalario);
            preparedStatement.setDate(14, pDataReferencia);
            preparedStatement.setString(15,String.valueOf(pAutorizado));
            preparedStatement.setString(16, String.valueOf(pRestituido));
            preparedStatement.setString(17, pObservacao);
            preparedStatement.setString(18, pLoginAtualizacao);
            preparedStatement.setInt(19, pCodRestituicaoRescisao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Erro na execução da atualização dos dados da restiuição de rescisão.");

        }

    }

}
