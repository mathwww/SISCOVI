package Calculos;

import java.sql.*;

public class Remuneracao {
    private Connection connection;
    Remuneracao(Connection connection) {
        this.connection = connection;
    }
    public float RetornaRemuneracaoPeriodo(int pCodFuncaoContrato, int pMes, int pAno, int pOperacao, int pRetroatividade) {

        /**
            -- Função que recupera o valor da remuneração vigente para o cargo de um
            -- contrato em um determinado perído em dupla vigência de convenção.
         */

        float vRemuneracao = 0;
        /**--Definição da data referência.*/
        Date vDataReferencia = Date.valueOf(""+pAno+"-"+pMes+"-"+"01");
        int vCodContrato = 0;
        int vCodRemuneracaoCargoContrato = 0;
        boolean vRetroatividade = false;
        Date vDataAditamento = null;

        /**
         --Operação 1: Remuneração do mês em que não há dupla vigência ou remuneração atual.
         --Operação 2: Remuneração encerrada do mês em que há dupla vigência.
         --pRetroatividade 1: Considera a retroatividade.
         --pRetroatividade 2: Desconsidera os períodos de retroatividade.
         */

        ResultSet rs;
        PreparedStatement preparedStatement;

        /**--Definição do cod_contrato. */
        try{
            preparedStatement = connection.prepareStatement("SELECT COD_CONTRATO FROM tb_funcao_contrato_CONTRATO WHERE COD=?");
            preparedStatement.setInt(1, pCodFuncaoContrato);
            rs = preparedStatement.executeQuery();
            if(rs.next()) {
                vCodContrato = rs.getInt(1);
            }else {
                return -1;
            }
        }catch(SQLException sqle){
            sqle.printStackTrace();
        }
        if(pRetroatividade == 1) {
            try {
                Retroatividade retroatividade = new Retroatividade(connection);
                vRetroatividade = retroatividade.ExisteRetroatividade(vCodContrato, pCodFuncaoContrato, pMes, pAno, 1);
            }catch(SQLException sqle) {
                sqle.printStackTrace();
                return -2;
            }
        }
        if(pOperacao == 1) {
            try {
                preparedStatement = connection.prepareStatement("SELECT remuneracao, cod, data_aditamento FROM tb_remuneracao_fun_con" +
                        " WHERE COD_FUNCAO_CONTRATO = ?  AND data_aditamento IS NOT NULL" +
                        " AND (SELECT CAST(CAST(DATA_ADITAMENTO AS DATE) AS DATETIME) ) <= (SELECT CAST(CAST(getdate() AS DATE) AS DATETIME))" +
                        "  AND data_aditamento IS NOT NULL" +
                                " AND CAST(data_aditamento) <= CAST(GETDATE() AS DATE)" +
                                " AND ((((CAST(data_inicio) <= CAST(? AS DATE))" +
                                " AND (CAST(data_inicio) < EOMONTH(CAST(? AS DATE))))" +
                                " AND (((CAST(data_fim) >= CAST(? AS DATE))" +
                                " AND (CAST(data_fim AS DATE) >= EOMONTH(CAST(? AS DATE))" +
                                " OR data_fim IS NULL))) OR (MONTH(DATA_INICIO) = MONTH(?)" + // --Ou início no mês referência
                                " AND YEAR(data_inicio) = YEAR(?))))");
                preparedStatement.setInt(1, pCodFuncaoContrato);
                preparedStatement.setDate(2, vDataReferencia);
                preparedStatement.setDate(3, vDataReferencia);
                preparedStatement.setDate(4, vDataReferencia);
                preparedStatement.setDate(5, vDataReferencia);
                preparedStatement.setDate(6, vDataReferencia);
                preparedStatement.setDate(7, vDataReferencia);
                rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    vRemuneracao = rs.getFloat("REMUNERACAO");
                    vCodRemuneracaoCargoContrato = rs.getInt("COD");
                    vDataAditamento = rs.getDate("DATA_ADITAMENTO");
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }

        if(pOperacao == 2){
            try {
                preparedStatement = connection.prepareStatement("SELECT REMUNERACAO FROM tb_remuneracao_fun_con WHERE COD_FUNCAO_CONTRATO=? AND DATA_ADITAMENTO IS NOT NULL AND" +
                        " ((SELECT DATEPART(MONTH, DATA_FIM_CONVENCAO)) = (SELECT DATEPART(MONTH , ?))" + // --Ou início no mês referência.
                        " AND (SELECT DATEPART(YEAR, DATA_FIM_CONVENCAO)) = (SELECT DATEPART(YEAR, ?)))");
                preparedStatement.setInt(1, pCodFuncaoContrato);
                preparedStatement.setDate(2, vDataReferencia);
                preparedStatement.setDate(3, vDataReferencia);
                rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    vRemuneracao = rs.getFloat("REMUNERACAO");
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }
        if((pOperacao == 1) && (vRetroatividade == true)){
            try {
                Convencao convencao = new Convencao(connection);
                vCodRemuneracaoCargoContrato = convencao.RetornaConvencaoAnterior(vCodRemuneracaoCargoContrato);
                preparedStatement = connection.prepareStatement("SELECT REMUNERACAO FROM tb_remuneracao_fun_con WHERE COD=?");
                preparedStatement.setInt(1, vCodRemuneracaoCargoContrato);
                rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    vRemuneracao = rs.getFloat("REMUNERACAO");
                    return vRemuneracao;
                }
            }catch(SQLException sqle){
                sqle.printStackTrace();
            }
        }
        if((pRetroatividade == 1) && (pOperacao == 1)){
            vRemuneracao = RetornaRemuneracaoPeriodo(pCodFuncaoContrato, pMes, pAno, 2, 1);
            return vRemuneracao;
        }
        if ((pRetroatividade == 2) && (pOperacao == 1)) {
            vRemuneracao = RetornaRemuneracaoPeriodo(pCodFuncaoContrato, pMes, pAno,2,2);
            return vRemuneracao;
        }

        return 0;
    }
}
