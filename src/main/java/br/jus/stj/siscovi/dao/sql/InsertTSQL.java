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

}
