package br.jus.stj.siscovi.dao.sql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertTSQL {

    private Connection connection;

    public InsertTSQL(Connection connection) {

        this.connection = connection;

    }

    public int InsertRestituicaoFerias (int pCodTerceirizadoContrato,
                                        int pCodTipoRestituicao,
                                        int pDiasVendidos,
                                        Date pInicioFerias,
                                        Date pFimFerias,
                                        Date pInicioPeriodoAquisitivo,
                                        Date pFimPeriodoAquisitivo,
                                        int pParcela,
                                        float pTotalFerias,
                                        float pTotalTercoConstitucional,
                                        float pTotalIncidenciaFerias,
                                        float pTotalIncidenciaTerco,
                                        String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbRestituicaoFerias = consulta.RetornaCodSequenceTbRestituicaoFerias();

        try {

            String sql = "SET IDENTITY_INSERT tb_restituicao_ferias ON;" +
                    " INSERT INTO TB_RESTITUICAO_FERIAS (COD,"+
                    " COD_TERCEIRIZADO_CONTRATO," +
                    " COD_TIPO_RESTITUICAO," +
                    " DATA_INICIO_PERIODO_AQUISITIVO," +
                    " DATA_FIM_PERIODO_AQUISITIVO," +
                    " DATA_INICIO_USUFRUTO," +
                    " DATA_FIM_USUFRUTO," +
                    " VALOR_FERIAS," +
                    " VALOR_TERCO_CONSTITUCIONAL," +
                    " INCID_SUBMOD_4_1_FERIAS," +
                    " INCID_SUBMOD_4_1_TERCO," +
                    " PARCELA," +
                    " DIAS_VENDIDOS," +
                    " DATA_REFERENCIA," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT tb_restituicao_ferias OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbRestituicaoFerias);
            preparedStatement.setInt(2, pCodTerceirizadoContrato);
            preparedStatement.setInt(3, pCodTipoRestituicao);
            preparedStatement.setDate(4, pInicioPeriodoAquisitivo);
            preparedStatement.setDate(5, pFimPeriodoAquisitivo);
            preparedStatement.setDate(6, pInicioFerias);
            preparedStatement.setDate(7, pFimFerias);
            preparedStatement.setFloat(8, pTotalFerias);
            preparedStatement.setFloat(9, pTotalTercoConstitucional);
            preparedStatement.setFloat(10, pTotalIncidenciaFerias);
            preparedStatement.setFloat(11, pTotalIncidenciaTerco);
            preparedStatement.setInt(12, pParcela);
            preparedStatement.setInt(13, pDiasVendidos);
            preparedStatement.setString(14, pLoginAtualizacao);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            throw new RuntimeException("Erro ao tentar inserir os resultados do cálculo de férias no banco de dados!");

        }

        return vCodTbRestituicaoFerias;

    }


    public void InsertSaldoResidualFerias (int pCodTbRestituicaoFerias,
                                           float pValorFerias,
                                           float pValorTerco,
                                           float pIncidenciaFerias,
                                           float pIncidenciaTerco,
                                           String pLoginAtualizacao) {

        PreparedStatement preparedStatement;

        try {

            String sql = "INSERT INTO TB_SALDO_RESIDUAL_FERIAS (COD_RESTITUICAO_FERIAS," +
                    " VALOR_FERIAS," +
                    " VALOR_TERCO," +
                    " INCID_SUBMOD_4_1_FERIAS," +
                    " INCID_SUBMOD_4_1_TERCO," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, pCodTbRestituicaoFerias);
            preparedStatement.setFloat(2, pValorFerias);
            preparedStatement.setFloat(3, pValorTerco);
            preparedStatement.setFloat(4, pIncidenciaFerias);
            preparedStatement.setFloat(5, pIncidenciaTerco);
            preparedStatement.setString(6, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            throw new RuntimeException("Erro ao tentar inserir os resultados do cálculo de férias no banco de dados!");

        }

    }

    public int InsertRestituicaoDecimoTerceiro (int pCodTerceirizadoContrato,
                                                int pCodTipoRestituicao,
                                                int pNumeroParcela,
                                                Date pInicioContagem,
                                                float pValorDecimoTerceiro,
                                                float pValorIncidencia,
                                                String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbRestituicaoDecimoTerceiro = consulta.RetornaCodSequenceTbRestituicaoDecimoTerceiro();

        try {

            String sql = "SET IDENTITY_INSERT tb_restituicao_decimo_terceiro ON; " +
                    "INSERT INTO TB_RESTITUICAO_DECIMO_TERCEIRO (COD,"+
                    " COD_TERCEIRIZADO_CONTRATO," +
                    " COD_TIPO_RESTITUICAO," +
                    " PARCELA," +
                    " DATA_INICIO_CONTAGEM," +
                    " VALOR," +
                    " INCIDENCIA_SUBMODULO_4_1," +
                    " DATA_REFERENCIA," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT tb_restituicao_decimo_terceiro OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbRestituicaoDecimoTerceiro);
            preparedStatement.setInt(2, pCodTerceirizadoContrato);
            preparedStatement.setInt(3, pCodTipoRestituicao);
            preparedStatement.setInt(4, pNumeroParcela);
            preparedStatement.setDate(5, pInicioContagem);
            preparedStatement.setFloat(6, pValorDecimoTerceiro);
            preparedStatement.setFloat(7, pValorIncidencia);
            preparedStatement.setString(8, pLoginAtualizacao);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("Erro ao tentar inserir dados na tabela de restituição de décimo terceiro!");

        }

        return vCodTbRestituicaoDecimoTerceiro;

    }

    public void InsertSaldoResidualDecimoTerceiro (int pCodRestituicaoDecimoTerceiro,
                                                   float pValorDecimoTerceiro,
                                                   float pValorIncidencia,
                                                   String pLoginAtualizacao) {

        PreparedStatement preparedStatement;

        try {

            String sql = "INSERT INTO TB_SALDO_RESIDUAL_DEC_TER (COD_RESTITUICAO_DEC_TERCEIRO," +
                    " VALOR," +
                    " INCIDENCIA_SUBMODULO_4_1," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, pCodRestituicaoDecimoTerceiro);
            preparedStatement.setFloat(2, pValorDecimoTerceiro);
            preparedStatement.setFloat(3, pValorIncidencia);
            preparedStatement.setString(4, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            throw new RuntimeException("Erro ao tentar inserir dados na tabela de saldo residual de décimo terceiro!");

        }

    }

    public int InsertRestituicaoRescisao (int pCodTerceirizadoContrato,
                                          int pCodTipoRestituicao,
                                          int pCodTipoRescisao,
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
                                          String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbRestituicaoRescisao = consulta.RetornaCodSequenceTbRestituicaoRescisao();

        try {

            String sql = "SET IDENTITY_INSERT tb_restituicao_rescisao ON;" +
                    " INSERT INTO tb_restituicao_rescisao (COD,"+
                    " COD_TERCEIRIZADO_CONTRATO," +
                    " COD_TIPO_RESTITUICAO," +
                    " COD_TIPO_RESCISAO," +
                    " DATA_DESLIGAMENTO," +
                    " VALOR_DECIMO_TERCEIRO," +
                    " INCID_SUBMOD_4_1_DEC_TERCEIRO," +
                    " INCID_MULTA_FGTS_DEC_TERCEIRO," +
                    " VALOR_FERIAS," +
                    " VALOR_TERCO," +
                    " INCID_SUBMOD_4_1_FERIAS," +
                    " INCID_SUBMOD_4_1_TERCO," +
                    " INCID_MULTA_FGTS_FERIAS," +
                    " INCID_MULTA_FGTS_TERCO," +
                    " MULTA_FGTS_SALARIO," +
                    " DATA_REFERENCIA," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT tb_restituicao_rescisao OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbRestituicaoRescisao);
            preparedStatement.setInt(2, pCodTerceirizadoContrato);
            preparedStatement.setInt(3, pCodTipoRestituicao);
            preparedStatement.setInt(4, pCodTipoRescisao);
            preparedStatement.setDate(5, pDataDesligamento);
            preparedStatement.setFloat(6, pValorDecimoTerceiro);
            preparedStatement.setFloat(7, pValorIncidenciaDecimoTerceiro);
            preparedStatement.setFloat(8, pValorFGTSDecimoTerceiro);
            preparedStatement.setFloat(9, pValorFerias);
            preparedStatement.setFloat(10, pValorTerco);
            preparedStatement.setFloat(11, pValorIncidenciaFerias);
            preparedStatement.setFloat(12, pValorIncidenciaTerco);
            preparedStatement.setFloat(13, pValorFGTSFerias);
            preparedStatement.setFloat(14, pValorFGTSTerco);
            preparedStatement.setFloat(15, pValorFGTSSalario);
            preparedStatement.setString(16, pLoginAtualizacao);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            throw new RuntimeException("Erro ao tentar inserir dados na tabela de restituição de rescisão.");

        }

        return vCodTbRestituicaoRescisao;

    }

    public void InsertSaldoResidualRescisao (int pCodRestituicaoRescisao,
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
                                             String pLoginAtualizacao) {

        PreparedStatement preparedStatement;

        try {

            String sql = "INSERT INTO TB_SALDO_RESIDUAL_RESCISAO (cod_restituicao_rescisao," +
                    " valor_decimo_terceiro," +
                    " incid_submod_4_1_dec_terceiro," +
                    " incid_multa_fgts_dec_terceiro," +
                    " valor_ferias," +
                    " valor_terco," +
                    " incid_submod_4_1_ferias," +
                    " incid_submod_4_1_terco," +
                    " incid_multa_fgts_ferias," +
                    " incid_multa_fgts_terco," +
                    " multa_fgts_salario," +
                    " login_atualizacao," +
                    " data_atualizacao)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, pCodRestituicaoRescisao);
            preparedStatement.setFloat(2, pValorDecimoTerceiro);
            preparedStatement.setFloat(3, pValorIncidenciaDecimoTerceiro);
            preparedStatement.setFloat(4, pValorFGTSDecimoTerceiro);
            preparedStatement.setFloat(5, pValorFerias);
            preparedStatement.setFloat(6, pValorTerco);
            preparedStatement.setFloat(7, pValorIncidenciaFerias);
            preparedStatement.setFloat(8, pValorIncidenciaTerco);
            preparedStatement.setFloat(9, pValorFGTSFerias);
            preparedStatement.setFloat(10, pValorFGTSTerco);
            preparedStatement.setFloat(11, pValorFGTSSalario);
            preparedStatement.setString(12, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            e.printStackTrace();

            throw new RuntimeException("Erro ao tentar inserir dados na tabela de saldo residual de rescisão.");

        }

    }

    public Integer InsertHistoricoRestituicaoDecimoTerceiro (int pCodTbRestituicaoFerias,
                                                             int pCodTipoRestituicao,
                                                             Date pInicioPeriodoAquisitivo,
                                                             Date pFimPeriodoAquisitivo,
                                                             Date pInicioFerias,
                                                             Date pFimFerias,
                                                             float pTotalFerias,
                                                             float pTotalTercoConstitucional,
                                                             float pTotalIncidenciaFerias,
                                                             float pTotalIncidenciaTerco,
                                                             int pParcela,
                                                             Date pDataReferencia,
                                                             int pDiasVendidos,
                                                             String pAutorizado,
                                                             String pRestituido,
                                                             String pObservacao,
                                                             String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbHistRestituicaoFerias = consulta.RetornaCodSequenceTbHistRestituicaoFerias();

        try {

            String sql = "INSERT INTO TB_HIST_RESTITUICAO_FERIAS (COD_RESTITUICAO_FERIAS," +
                    " COD_TIPO_RESTITUICAO," +
                    " DATA_INICIO_PERIODO_AQUISITIVO," +
                    " DATA_FIM_PERIODO_AQUISITIVO," +
                    " DATA_INICIO_USUFRUTO," +
                    " DATA_FIM_USUFRUTO," +
                    " VALOR_FERIAS," +
                    " VALOR_TERCO_CONSTITUCIONAL," +
                    " INCID_SUBMOD_4_1_FERIAS," +
                    " INCID_SUBMOD_4_1_TERCO," +
                    " PARCELA," +
                    " DIAS_VENDIDOS," +
                    " DATA_REFERENCIA," +
                    " AUTORIZADO," +
                    " RESTITUIDO," +
                    " OBSERVACAO," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, pCodTbRestituicaoFerias);
            preparedStatement.setInt(2, pCodTipoRestituicao);
            preparedStatement.setDate(3, pInicioPeriodoAquisitivo);
            preparedStatement.setDate(4, pFimPeriodoAquisitivo);
            preparedStatement.setDate(5, pInicioFerias);
            preparedStatement.setDate(6, pFimFerias);
            preparedStatement.setFloat(7, pTotalFerias);
            preparedStatement.setFloat(8, pTotalTercoConstitucional);
            preparedStatement.setFloat(9, pTotalIncidenciaFerias);
            preparedStatement.setFloat(10, pTotalIncidenciaTerco);
            preparedStatement.setInt(11, pParcela);
            preparedStatement.setInt(12, pDiasVendidos);
            preparedStatement.setDate(13, pDataReferencia);
            preparedStatement.setString(14, pAutorizado);
            preparedStatement.setString(15, pRestituido);
            preparedStatement.setString(16, pObservacao);
            preparedStatement.setString(17, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível inserir dados na tabela de histórico de restituição de férias.");

        }

        return vCodTbHistRestituicaoFerias;

    }

    public Integer InsertHistoricoRestituicaoFerias (int pCodTbRestituicaoFerias,
                                                     int pCodTipoRestituicao,
                                                     Date pInicioPeriodoAquisitivo,
                                                     Date pFimPeriodoAquisitivo,
                                                     Date pInicioFerias,
                                                     Date pFimFerias,
                                                     float pTotalFerias,
                                                     float pTotalTercoConstitucional,
                                                     float pTotalIncidenciaFerias,
                                                     float pTotalIncidenciaTerco,
                                                     int pParcela,
                                                     Date pDataReferencia,
                                                     int pDiasVendidos,
                                                     String pAutorizado,
                                                     String pRestituido,
                                                     String pObservacao,
                                                     String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbHistRestituicaoFerias = consulta.RetornaCodSequenceTbHistRestituicaoFerias();

        try {

            String sql = "INSERT INTO TB_HIST_RESTITUICAO_FERIAS (COD_RESTITUICAO_FERIAS," +
                    " COD_TIPO_RESTITUICAO," +
                    " DATA_INICIO_PERIODO_AQUISITIVO," +
                    " DATA_FIM_PERIODO_AQUISITIVO," +
                    " DATA_INICIO_USUFRUTO," +
                    " DATA_FIM_USUFRUTO," +
                    " VALOR_FERIAS," +
                    " VALOR_TERCO_CONSTITUCIONAL," +
                    " INCID_SUBMOD_4_1_FERIAS," +
                    " INCID_SUBMOD_4_1_TERCO," +
                    " PARCELA," +
                    " DIAS_VENDIDOS," +
                    " DATA_REFERENCIA," +
                    " AUTORIZADO," +
                    " RESTITUIDO," +
                    " OBSERVACAO," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, pCodTbRestituicaoFerias);
            preparedStatement.setInt(2, pCodTipoRestituicao);
            preparedStatement.setDate(3, pInicioPeriodoAquisitivo);
            preparedStatement.setDate(4, pFimPeriodoAquisitivo);
            preparedStatement.setDate(5, pInicioFerias);
            preparedStatement.setDate(6, pFimFerias);
            preparedStatement.setFloat(7, pTotalFerias);
            preparedStatement.setFloat(8, pTotalTercoConstitucional);
            preparedStatement.setFloat(9, pTotalIncidenciaFerias);
            preparedStatement.setFloat(10, pTotalIncidenciaTerco);
            preparedStatement.setInt(11, pParcela);
            preparedStatement.setInt(12, pDiasVendidos);
            preparedStatement.setDate(13, pDataReferencia);
            preparedStatement.setString(14, pAutorizado);
            preparedStatement.setString(15, pRestituido);
            preparedStatement.setString(16, pObservacao);
            preparedStatement.setString(17, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível inserir dados na tabela de histórico de restituição de férias.");

        }

        return vCodTbHistRestituicaoFerias;

    }



}