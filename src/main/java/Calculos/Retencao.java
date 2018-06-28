package Calculos;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

public class Retencao {
    private Connection connection;
    Retencao(Connection connection){
        this.connection = connection;
    }
    public String TipoDeRestituicao(int cod) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT NOME FROM TB_TIPO_RESGATE WHERE COD=?");
        ResultSet rs;
        preparedStatement.setInt(1,cod);
        rs = preparedStatement.executeQuery();
        if (rs.next()){
            return rs.getString("NOME");
        }else{
            return null;
        }
    }
    public boolean FuncaoRetencaoIntegral(int pCodTerceirizadoContrato, int pMes, int pAno) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        /** --Função que retorna se um terceirizado trabalhou período igual ou superior a 15
         --dias em um determinado mês.
         */

        Date vDataDisponibilizacao;
        Date vDataDesligamento;
        Date vDataReferencia;

        /** --Define como data referência o primeiro dia do mês e ano passados como argumentos. */

        vDataReferencia = Date.valueOf(pAno + "-" + pMes + "-01");

        /** --Carrega as datas de disponibilização e desligamento do terceirizado.*/

        try {
            preparedStatement = connection.prepareStatement("SELECT DATA_DISPONIBILIZACAO, DATA DESLIGAMENTO FROM tb_terceirizado_contrato WHERE COD = ?");
            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vDataDisponibilizacao = resultSet.getDate("DATA_DISPONIBILIZACAO");
                vDataDesligamento = resultSet.getDate("DATA_DESLIGAMENTO");
                /** --Caso não possua data de desligamento.*/
                if (vDataDesligamento == null) {
                    /**
                     --Se a data de disponibilização é inferior a data referência então o
                     --funcionário trabalhou os 30 dias do mês referência.
                     */
                    if(vDataDisponibilizacao.before(vDataReferencia)) {
                        return true;
                    }

                    /**
                     -- Se a data de disponibilização está no mês referência e não se verifica
                     -- a quantidade de dias trabalhados pelo funcionário.
                     */
                    LocalDate dataRef = vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth());
                    if((vDataDisponibilizacao.after(vDataReferencia) || vDataDisponibilizacao.equals(vDataReferencia)) &&
                            (vDataDisponibilizacao.before(Date.valueOf(dataRef)) || vDataDisponibilizacao.equals(Date.valueOf(dataRef)))) {
                        LocalDate dataDisp = vDataDisponibilizacao.toLocalDate().withDayOfMonth(vDataDisponibilizacao.toLocalDate().lengthOfMonth());
                        if(ChronoUnit.DAYS.between(dataDisp, vDataDisponibilizacao.toLocalDate()) + 1 >= 15) {
                            return true;
                        }
                    }
                }
                /**
                 * --Caso possua data de desligamento.
                 */
                if(vDataDesligamento != null) {
                    LocalDate dataRef = vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth()); // Data com o último dia do mês da data de Referência

                    /**
                     * --Se a data de disponibilização é inferior a data referência e a data de
                     --desligamento é superior ao último dia do mês referência então o
                     --funcionário trabalhou os 30 dias.
                     */

                    if(vDataDisponibilizacao.before(vDataReferencia) && vDataDesligamento.after(Date.valueOf(dataRef))) {
                        return true;
                    }

                    /**
                     * --Se a data de disponibilização está no mês referência e a data de
                     --desligamento é superior ao mês referência, então se verifica a quantidade
                     --de dias trabalhados pelo funcionário.
                     */

                    if((vDataDisponibilizacao.after(vDataReferencia) || vDataDisponibilizacao.equals(vDataReferencia)) &&
                            (vDataDisponibilizacao.before(Date.valueOf(dataRef)) || vDataDisponibilizacao.equals(Date.valueOf(dataRef))) &&
                            vDataDesligamento.after(Date.valueOf(dataRef))) {
                        LocalDate dataDisp = vDataDisponibilizacao.toLocalDate().withDayOfMonth(vDataDisponibilizacao.toLocalDate().lengthOfMonth());
                        if(ChronoUnit.DAYS.between(dataDisp, vDataDisponibilizacao.toLocalDate()) + 1 >= 15) {
                            return true;
                        }
                    }

                    /**--Se a data de disponibilização está no mês referência e também a data de
                            --desligamento, então contam-se os dias trabalhados pelo funcionário.
                     */

                    if((vDataDisponibilizacao.after(vDataReferencia) || vDataDisponibilizacao.equals(vDataReferencia)) &&
                            (vDataDisponibilizacao.before(Date.valueOf(dataRef)) || vDataDisponibilizacao.equals(Date.valueOf(dataRef)) &&
                                    (vDataDesligamento.after(vDataReferencia) || vDataDesligamento.equals(vDataReferencia)) &&
                                    (vDataDesligamento.before(Date.valueOf(dataRef)) || vDataDesligamento.equals(vDataReferencia)))) {
                        if(ChronoUnit.DAYS.between(vDataDesligamento.toLocalDate(), vDataDisponibilizacao.toLocalDate()) + 1 >= 15) {
                            return true;
                        }

                    }

                    /**
                     --Se a data da disponibilização for inferior ao mês de cálculo e
                     --o funcionário tiver desligamento no mês referência, então contam-se
                     --os dias trabalhados.
                     */

                    if(vDataDisponibilizacao.before(vDataReferencia) && (vDataDesligamento.after(vDataReferencia) || vDataDesligamento.equals(vDataReferencia)) &&
                            (vDataDesligamento.before(Date.valueOf(dataRef)) || vDataDesligamento.equals(Date.valueOf(dataRef)))) {
                        if(ChronoUnit.DAYS.between(vDataDesligamento.toLocalDate(), vDataReferencia.toLocalDate()) + 1 >= 15) {
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
