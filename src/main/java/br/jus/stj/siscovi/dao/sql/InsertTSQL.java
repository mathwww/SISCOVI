package br.jus.stj.siscovi.dao.sql;

import java.sql.*;

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
                                          Date pDataInicioFerias,
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
                    " DATA_INICIO_FERIAS," +
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
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE(), ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT tb_restituicao_rescisao OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbRestituicaoRescisao);
            preparedStatement.setInt(2, pCodTerceirizadoContrato);
            preparedStatement.setInt(3, pCodTipoRestituicao);
            preparedStatement.setInt(4, pCodTipoRescisao);
            preparedStatement.setDate(5, pDataDesligamento);
            preparedStatement.setDate(6, pDataInicioFerias);
            preparedStatement.setFloat(7, pValorDecimoTerceiro);
            preparedStatement.setFloat(8, pValorIncidenciaDecimoTerceiro);
            preparedStatement.setFloat(9, pValorFGTSDecimoTerceiro);
            preparedStatement.setFloat(10, pValorFerias);
            preparedStatement.setFloat(11, pValorTerco);
            preparedStatement.setFloat(12, pValorIncidenciaFerias);
            preparedStatement.setFloat(13, pValorIncidenciaTerco);
            preparedStatement.setFloat(14, pValorFGTSFerias);
            preparedStatement.setFloat(15, pValorFGTSTerco);
            preparedStatement.setFloat(16, pValorFGTSSalario);
            preparedStatement.setString(17, pLoginAtualizacao);
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

    public Integer InsertHistoricoRestituicaoDecimoTerceiro (int pCodTbRestituicaoDecTer,
                                                             int pCodTipoRestituicao,
                                                             int pParcela,
                                                             Date pDataInicioContagem,
                                                             float pValor,
                                                             float pIncidenciaSubmodulo41,
                                                             Date pDataReferencia,
                                                             String pAutorizado,
                                                             String pRestituido,
                                                             String pObservacao,
                                                             String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbHistRestituicaoDecTer = consulta.RetornaCodSequenceTbHistRestituicaoDecTer();

        try {

            String sql = "SET IDENTITY_INSERT TB_HIST_RESTITUICAO_DEC_TER ON;" +
                    " INSERT INTO TB_HIST_RESTITUICAO_DEC_TER (COD," +
                    " COD_RESTITUICAO_DEC_TERCEIRO," +
                    " COD_TIPO_RESTITUICAO," +
                    " PARCELA," +
                    " DATA_INICIO_CONTAGEM," +
                    " VALOR," +
                    " INCIDENCIA_SUBMODULO_4_1," +
                    " DATA_REFERENCIA," +
                    " AUTORIZADO," +
                    " RESTITUIDO," +
                    " OBSERVACAO," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT TB_HIST_RESTITUICAO_DEC_TER OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbHistRestituicaoDecTer);
            preparedStatement.setInt(2, pCodTbRestituicaoDecTer);
            preparedStatement.setInt(3, pCodTipoRestituicao);
            preparedStatement.setInt(4, pParcela);
            preparedStatement.setDate(5, pDataInicioContagem);
            preparedStatement.setFloat(6, pValor);
            preparedStatement.setFloat(7, pIncidenciaSubmodulo41);
            preparedStatement.setDate(8, pDataReferencia);
            preparedStatement.setString(9, pAutorizado);
            preparedStatement.setString(10, pRestituido);
            preparedStatement.setString(11, pObservacao);
            preparedStatement.setString(12, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            sqle.printStackTrace();

            throw new NullPointerException("Não foi possível inserir dados na tabela de histórico de restituição de décimo terceiro.");

        }

        return vCodTbHistRestituicaoDecTer;

    }

    public Integer InsertHistoricoRestituicaoRescisao (int pCodTbRestituicaoRescisao,
                                                       int pCodTipoRestituicao,
                                                       int pCodTipoRescisao,
                                                       Date pDataDesligamento,
                                                       Date pDataInicioFerias,
                                                       float pValorDecimoTerceiro,
                                                       float pIncidSubmod41DecTerceiro,
                                                       float pIncidMultaFGTSDecTeceriro,
                                                       float pValorFerias,
                                                       float pValorTerco,
                                                       float pIncidSubmod41Ferias,
                                                       float pIncidSubmod41Terco,
                                                       float pIncidMultaFGTSFerias,
                                                       float pIncidMultaFGTSTerco,
                                                       float pMultaFGTSSalario,
                                                       Date pDataReferencia,
                                                       String pAutorizado,
                                                       String pRestituido,
                                                       String pObservacao,
                                                       String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodTbHistRestituicaoRescisao = consulta.RetornaCodSequenceTbHistRestituicaoRescisao();

        try {

            String sql = "SET IDENTITY_INSERT TB_HIST_RESTITUICAO_RESCISAO ON;" +
                    " INSERT INTO TB_HIST_RESTITUICAO_RESCISAO (COD," +
                    " COD_RESTITUICAO_RESCISAO," +
                    " COD_TIPO_RESTITUICAO," +
                    " COD_TIPO_RESCISAO," +
                    " DATA_DESLIGAMENTO," +
                    " DATA_INICIO_FERIAS," +
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
                    " AUTORIZADO," +
                    " RESTITUIDO," +
                    " OBSERVACAO," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT TB_HIST_RESTITUICAO_RESCISAO OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbHistRestituicaoRescisao);
            preparedStatement.setInt(2, pCodTbRestituicaoRescisao);
            preparedStatement.setInt(3, pCodTipoRestituicao);
            preparedStatement.setInt(4, pCodTipoRescisao);
            preparedStatement.setDate(5, pDataDesligamento);
            preparedStatement.setDate(6, pDataInicioFerias);
            preparedStatement.setFloat(7, pValorDecimoTerceiro);
            preparedStatement.setFloat(8, pIncidSubmod41DecTerceiro);
            preparedStatement.setFloat(9, pIncidMultaFGTSDecTeceriro);
            preparedStatement.setFloat(10, pValorFerias);
            preparedStatement.setFloat(11, pValorTerco);
            preparedStatement.setFloat(12, pIncidSubmod41Ferias);
            preparedStatement.setFloat(13, pIncidSubmod41Terco);
            preparedStatement.setFloat(14, pIncidMultaFGTSFerias);
            preparedStatement.setFloat(15, pIncidMultaFGTSTerco);
            preparedStatement.setFloat(16, pMultaFGTSSalario);
            preparedStatement.setDate(17, pDataReferencia);
            preparedStatement.setString(18, pAutorizado);
            preparedStatement.setString(19,pRestituido);
            preparedStatement.setString(20, pObservacao);
            preparedStatement.setString(21, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            sqle.printStackTrace();

            throw new NullPointerException("Não foi possível inserir dados na tabela de histórico de restituição de rescisão.");

        }

        return vCodTbHistRestituicaoRescisao;

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

            String sql = "SET IDENTITY_INSERT TB_HIST_RESTITUICAO_FERIAS ON;" +
                    " INSERT INTO TB_HIST_RESTITUICAO_FERIAS (COD," +
                    " COD_RESTITUICAO_FERIAS," +
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
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT TB_HIST_RESTITUICAO_FERIAS OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbHistRestituicaoFerias);
            preparedStatement.setInt(2, pCodTbRestituicaoFerias);
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
            preparedStatement.setDate(14, pDataReferencia);
            preparedStatement.setString(15, pAutorizado);
            preparedStatement.setString(16, pRestituido);
            preparedStatement.setString(17, pObservacao);
            preparedStatement.setString(18, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível inserir dados na tabela de histórico de restituição de férias.");

        }

        return vCodTbHistRestituicaoFerias;

    }


    public int InsertRubrica (String pNome,
                              String pSigla,
                              String pDescricao,
                              String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        int vCodRubrica = consulta.RetornaCodSequenceTable("TB_RUBRICA");

        try {

            String sql = "SET IDENTITY_INSERT TB_RUBRICA ON;" +
                    " INSERT INTO TB_RUBRICA (COD," +
                    " NOME," +
                    " SIGLA," +
                    " DESCRICAO," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT TB_RUBRICA OFF;";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, vCodRubrica);
            preparedStatement.setString(2, pNome);
            preparedStatement.setString(3, pSigla);
            preparedStatement.setString(4, pDescricao);
            preparedStatement.setString(5, pLoginAtualizacao);

            preparedStatement.executeUpdate();

        } catch (SQLException sqle) {

            sqle.printStackTrace();

            throw new NullPointerException("Não foi possível inserir a nova rubrica.");

        }

        return vCodRubrica;

    }



}