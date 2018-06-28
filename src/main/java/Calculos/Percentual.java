package Calculos;

import com.sun.scenario.effect.impl.prism.ps.PPSBlend_REDPeer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Percentual {
    private Connection connection;
    Percentual(Connection connection){
        this.connection = connection;
    }
    public boolean ExisteMundancaPercentual(int pCodContrato, int pMes, int pAno, int pRetroatividade) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        /**
         --Função que retorna se em um dado mês existe ao menos um caso
         --de mudança de percentual que enseje dupla vigência.

         --pRetroatividade = 1 - Considera a retroatividade.
         --pRetroatividade = 2 - Desconsidera a retroatividade.
         */

        int vCount = 0;
        int vCount2 = 0;
        boolean vRetroatividade = false;

        /** --Definição do modo de funcionamento da função. */

        if(pRetroatividade == 1) {
            Retroatividade retroatividade = new Retroatividade(connection);
            try {
                vRetroatividade = retroatividade.ExisteRetroatividade(pCodContrato, 0, pMes, pAno, 2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /** --Conta o número de percentuais da mesma rubrica vigentes no mês. */

        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT(COD_RUBRICA) FROM (SELECT cod_rubrica, COUNT(pc.cod) AS \"CONTAGEM\"" +
                    " FROM tb_percentual_contrato pc" +
                    " WHERE pc.cod_contrato = ?" +
                    " AND (((MONTH(pc.data_inicio) = ? AND YEAR(pc.data_inicio) = ?)" +
                    " AND (MONTH(data_aditamento) = ? AND YEAR(data_aditamento) = ?)" +
                    " AND (CAST (data_aditamento AS DATE) <= CAST(GETDATE() AS DATE)))" + //--Define a validade da convenção
                    " OR (MONTH(pc.data_fim) = ? AND YEAR(pc.data_fim) = ?))" +
                    " GROUP BY cod_rubrica) AS X WHERE X.CONTAGEM > 1");
            preparedStatement.setInt(1, pCodContrato);
            preparedStatement.setInt(2, pMes);
            preparedStatement.setInt(3, pAno);
            preparedStatement.setInt(4, pMes);
            preparedStatement.setInt(5, pAno);
            preparedStatement.setInt(6, pMes);
            preparedStatement.setInt(7, pAno);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT(cod_rubrica) FROM (SELECT pe.cod_rubrica, COUNT(pe.cod) AS \"CONTAGEM\"" +
                    " FROM tb_percentual_estatico pe" +
                    " WHERE (((MONTH(pe.data_inicio)=? AND YEAR(pe.data_inicio)=?)" +
                    " AND (MONTH(data_aditamento)=? AND YEAR(data_aditamento)=?)" +
                    " AND (CAST(data_aditamento AS DATE) <= CAST(GETDATE() AS DATE)))" + //--Define a validade da convenção.
                    " OR (MONTH(pe.data_fim)=? AND YEAR(pe.data_fim)=?))" +
                    " GROUP BY pe.cod_rubrica) AS X WHERE X.CONTAGEM > 1");
            int i = 1;
            preparedStatement.setInt(i++, pMes);
            preparedStatement.setInt(i++, pAno);
            preparedStatement.setInt(i++, pMes);
            preparedStatement.setInt(i++, pAno);
            preparedStatement.setInt(i++, pMes);
            preparedStatement.setInt(i, pAno);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vCount2 = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        /** --Se houver qualquer número de percentuais da mesma rubrica no mês passado retorna VERDADEIRO. */

        if(((vCount > 0) && (vRetroatividade == false)) || ((vCount2 > 0) && (!vRetroatividade))) {
            return true;
        }
        return false;
    }
    public boolean ExisteMudancaEstatico(int pCodContrato, int pMes, int pAno, int pRetroatividade) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        Retroatividade retroatividade = new Retroatividade(connection);

        /**
         * --Função que retorna se em um dado mês existe ao menos um caso
         --de mudança de percentual estático que enseje dupla vigência.

         --pRetroatividade = 1 - Considera a retroatividade.
         --pRetroatividade = 2 - Desconsidera a retroatividade.
         */

        int vCount = 0;
        boolean vRetroatividade = false;

        /**--Definição do modo de funcionamento da função.*/

        if(pRetroatividade == 1) {
            try {
                vRetroatividade = retroatividade.ExisteRetroatividade(pCodContrato, 0, pMes, pAno, 2);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        /** --Conta o número de percentuais da mesma rubrica vigentes no mês. */
        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT(COD_RUBRICA) FROM (SELECT pe.cod_rubrica, COUNT(pe.cod) AS \"CONTAGEM\"\n" +
                    " FROM tb_percentual_estatico pe" +
                    " WHERE (((MONTH(pe.data_inicio) = ? AND YEAR(pe.data_inicio) = ?)" +
                    " AND (MONTH(data_aditamento) = ? AND YEAR(data_aditamento) = ?)" +
                    " AND (CAST(data_aditamento AS DATE) <= CAST(GETDATE() AS DATE)))" + /*--Define a validade da convenção.*/
                    " OR (MONTH(pe.data_fim) = ? AND YEAR(pe.data_fim) = ?))" +
                    " GROUP BY pe.cod_rubrica) WHERE CONTAGEM > 1");
            preparedStatement.setInt(1, pMes);
            preparedStatement.setInt(2, pAno);
            preparedStatement.setInt(3, pMes);
            preparedStatement.setInt(4, pAno);
            preparedStatement.setInt(5, pMes);
            preparedStatement.setInt(6, pAno);
            resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                vCount = resultSet.getInt(1);
            }
            if(vCount != 0) {
                /**--Se houver qualquer número de percentuais da mesma rubrica no mês passado retorna VERDADEIRO.*/
                if((vCount > 0) && (vRetroatividade == false)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean ExisteMudancaContrato(int pCodContrato, int pMes, int pAno, int pRetroatividade) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        /**
         * --Função que retorna se em um dado mês existe ao menos um caso
         --de mudança de percentual do contrato que enseje dupla vigência.

         --pRetroatividade = 1 - Considera a retroatividade.
         --pRetroatividade = 2 - Desconsidera a retroatividade.
         */

        int vCount = 0;
        boolean vRetroatividade = false;

        /** --Definição do modo de funcionamento da função. */

        if(pRetroatividade == 1) {
            Retroatividade retroatividade = new Retroatividade(this.connection);
            try {
                vRetroatividade = retroatividade.ExisteRetroatividade(pCodContrato, 0, pMes, pAno, 2);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /**
         * --Conta o número de percentuais da mesma rubrica vigentes no mês.
         */

        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT(COD_RUBRICA) FROM (SELECT cod_rubrica, COUNT(pc.cod) AS \"CONTAGEM\"" +
                    " FROM tb_percentual_contrato pc" +
                    " WHERE pc.cod_contrato = ?" +
                    " AND (((MONTH(data_inicio) = ? AND YEAR(pc.data_inicio) = ?)" +
                    " AND (MONTH(data_aditamento) = ? AND YEAR(data_aditamento) = ?)" +
                    " AND (CAST(data_aditamento AS DATE) <= CAST(GETDATE() AS DATE)))" + /**--Define a validade da convenção.*/
                    " OR (MONTH(pc.data_fim) = ? AND YEAR(pc.data_fim) = ?))" +
                    " GROUP BY cod_rubrica) WHERE CONTAGEM > 1");
            int i = 1;
            preparedStatement.setInt(i++, pCodContrato);
            preparedStatement.setInt(i++, pMes);
            preparedStatement.setInt(i++, pAno);
            preparedStatement.setInt(i++, pMes);
            preparedStatement.setInt(i++, pAno);
            preparedStatement.setInt(i++, pMes);
            preparedStatement.setInt(i++, pAno);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vCount = resultSet.getInt(1);
            }

            if(vCount != 0) {

                /**--Se houver qualquer número de percentuais da mesma rubrica no mês passado retorna VERDADEIRO. */

                if((vCount > 0 ) && (vRetroatividade == false)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
