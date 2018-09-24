package br.jus.stj.siscovi.dao.sql;

import br.jus.stj.siscovi.model.CodFuncaoContratoECodFuncaoTerceirizadoModel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;

public class ConsultaTSQL {

    private Connection connection;

    public ConsultaTSQL(Connection connection) {

        this.connection = connection;

    }

    /**
     *Função que retorna o código de um contrato aleatório no banco de dados.
     *
     * @return Um código (cod) de contrato aleatório.
     */

    public int RetornaCodContratoAleatorio () {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        int vCodContrato = 0;

        //Carregamento do código do contrato.

        try {

            preparedStatement = connection.prepareStatement("SELECT TOP 1 cod\n" +
                    " FROM tb_contrato;");

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodContrato = resultSet.getInt(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar o código do contrato.");

        }

        return vCodContrato;

    }

    /**
     *Função que retorna o código de um registro de terceirizado no contrato aleatório do banco de dados.
     * Esse registro é responsável por "linkar" terceirizado e contrato de alguma prestadora de serviço.
     *
     * @param pCodContrato;
     *
     * @return Um código (cod) de terceirizado_contrato aleatório.
     */

    public int RetornaCodTerceirizadoAleatorio (int pCodContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        int vCodTerceirizadoContrato = 0;

        //Carregamento do código do terceirizado no contrato.

        try {

            preparedStatement = connection.prepareStatement("SELECT cod\n" +
                    " FROM tb_terceirizado_contrato\n" +
                    " WHERE cod_contrato = ?;");

            preparedStatement.setInt(1, pCodContrato);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTerceirizadoContrato = resultSet.getInt(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar o código do terceirizado.");

        }

        return vCodTerceirizadoContrato;

    }

    /**
     *Função que retorna a data de disponibilização de um terceirizado em determinado contrato.
     *
     * @param pCodTerceirizadoContrato;
     *
     * @return A data de disponibilização do terceirizado em um contrato.
     */

    public Date RetornaDataDisponibilizacaoTerceirizado (int pCodTerceirizadoContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        Date vDataDisponibilizacao = null;

        //Carregamento da data de disponibilização do terceirizado.

        try {

            preparedStatement = connection.prepareStatement("SELECT DATA_DISPONIBILIZACAO\n" +
                    " FROM tb_terceirizado_contrato\n" +
                    " WHERE cod = ?;");

            preparedStatement.setInt(1, pCodTerceirizadoContrato);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vDataDisponibilizacao = resultSet.getDate(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar a data de disponibilização do terceirizado.");

        }

        return vDataDisponibilizacao;

    }

    /**
     *Função que retorna a data de início de um contrato registrado no banco de dados.
     *
     * @param pCodContrato;
     *
     * @return A data de início de um contrato.
     */

    public Date RetornaDataInicioContrato (int pCodContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        Date dataInicioContrato = null;

        //Carregamento da data início do contrato.

        try {

            preparedStatement = connection.prepareStatement("SELECT MIN(data_inicio_vigencia)\n" +
                    " FROM tb_evento_contratual\n" +
                    " WHERE cod_contrato = ?");

            preparedStatement.setInt(1, pCodContrato);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                dataInicioContrato = resultSet.getDate(1);

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível carregar o código do contrato.");

        }

        return dataInicioContrato;

    }

    /**
     *Função que retorna o código de um tipo de restituição.
     *
     * @param pTipoRestituicao;
     *
     * @return O código (cod) do registro correspondente a um tipo de restituição.
     */

    public int RetornaCodTipoRestituicao (String pTipoRestituicao) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;
        int vCodTipoRestituicao = 0;

        /*Atribuição do cod do tipo de restituição.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT COD" + " FROM TB_TIPO_RESTITUICAO" + " WHERE UPPER(nome) = UPPER(?)");

            preparedStatement.setString(1, pTipoRestituicao);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTipoRestituicao = resultSet.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        if (vCodTipoRestituicao == 0) {

            throw new NullPointerException("Tipo de restituição não encontrada.");

        }

        return vCodTipoRestituicao;

    }

    /**
     *Função que retorna o código de um tipo de rescisão.
     *
     * @param pTipoRescisao;
     *
     * @return O código (cod) do registro correspondente a um tipo de rescisão.
     */

    public int RetornaCodTipoRescisao (String pTipoRescisao) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;
        int vCodTipoRescisao = 0;

        /*Atribuição do cod do tipo de rescisão.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT COD" + " FROM tb_tipo_rescisao" + " WHERE UPPER(TIPO_RESCISAO) = UPPER(?)");

            preparedStatement.setString(1, pTipoRescisao);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTipoRescisao = resultSet.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        if (vCodTipoRescisao == 0) {

            throw new NullPointerException("Tipo de rescisão não encontrada.");

        }

        return vCodTipoRescisao;

    }

    /*Seleção do código da função terceirizado e da função contrato.*/

    public ArrayList<CodFuncaoContratoECodFuncaoTerceirizadoModel> SelecionaFuncaoContratoEFuncaoTerceirizado (int pCodTerceirizadoContrato, Date pDataReferencia) {

        /*Busca as funções que um funcionário exerceu no mês de cálculo.*/

        ArrayList<CodFuncaoContratoECodFuncaoTerceirizadoModel> tuplas = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ft.cod_funcao_contrato, " +
                "ft.cod" +
                " FROM tb_funcao_terceirizado ft" +
                " WHERE ft.cod_terceirizado_contrato = ?" +
                " AND ((((CONVERT(date, CONVERT(varchar, year(ft.data_inicio)) + '-' + CONVERT(varchar, month(ft.data_inicio)) + '-01')) <= ?)" +
                " AND" +
                " (ft.data_fim >= ?))" +
                " OR" +
                " (((CONVERT(date, CONVERT(varchar, year(ft.data_inicio)) + '-' + CONVERT(varchar, month(ft.data_inicio)) + '-01')) <= ?) " +
                "AND" +
                " (ft.data_fim IS NULL)))")){

            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            preparedStatement.setDate(2, pDataReferencia);
            preparedStatement.setDate(3, pDataReferencia);
            preparedStatement.setDate(4, pDataReferencia);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    CodFuncaoContratoECodFuncaoTerceirizadoModel tupla = new CodFuncaoContratoECodFuncaoTerceirizadoModel(resultSet.getInt("COD"), resultSet.getInt("COD_FUNCAO_CONTRATO"));

                    tuplas.add(tupla);

                }

            }

        } catch(SQLException slqe) {
            //slqe.printStackTrace();
            throw new NullPointerException("Problemas durante a consulta ao banco em relação ao terceirizado: " + pCodTerceirizadoContrato);

        }

        return tuplas;

    }

    //Retorna o cod contrato do registro contido em tb_terceirizado_contrato com cod correspondente a pCodTerceirizadoContrato.

    public int RetornaContratoTerceirizado (int pCodTerceirizadoContrato) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;
        int vCodContrato = 0;

        try {

            preparedStatement = connection.prepareStatement("SELECT tc.cod_contrato FROM tb_terceirizado_contrato tc WHERE tc.cod = ?");

            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodContrato = resultSet.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        return vCodContrato;

    }

    /**
     * Recuparação do próximo valor da sequência da chave primária da tabela tb_restituicao_ferias.
     *
     * @return Próximo valor de sequência da chave primária da tabela.
     * */

    public int RetornaCodSequenceTbRestituicaoFerias () {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        int vCodTbRestituicaoFerias = 0;

        try {

            preparedStatement = connection.prepareStatement("SELECT ident_current ('TB_RESTITUICAO_FERIAS')");

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTbRestituicaoFerias = resultSet.getInt(1);
                vCodTbRestituicaoFerias = vCodTbRestituicaoFerias + 1;

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível recuperar o número de sequência da chave primária da tabela de restituição de férias.");

        }

        if (vCodTbRestituicaoFerias == 0) {

            throw new NullPointerException("Não foi possível recuperar o número de sequência da chave primária da tabela de restituição de férias.");

        }

        return vCodTbRestituicaoFerias;

    }

    /**
     * Recuparação do próximo valor da sequência da chave primária da tabela tb_restituicao_ferias.
     *
     * @return Próximo valor de sequência da chave primária da tabela.
     * */

    public int RetornaCodSequenceTbRestituicaoDecimoTerceiro () {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        int vCodTbRestituicaoDecimoTerceiro = 0;

        try {

            preparedStatement = connection.prepareStatement("SELECT ident_current ('TB_RESTITUICAO_DECIMO_TERCEIRO')");

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTbRestituicaoDecimoTerceiro = resultSet.getInt(1);
                vCodTbRestituicaoDecimoTerceiro = vCodTbRestituicaoDecimoTerceiro + 1;

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível recuperar o número de sequência da chave primária da tabela de restituição de décimo terceiro.");

        }

        return vCodTbRestituicaoDecimoTerceiro;

    }

}
