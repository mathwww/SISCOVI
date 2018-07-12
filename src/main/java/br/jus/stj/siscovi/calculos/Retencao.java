package br.jus.stj.siscovi.calculos;

import java.sql.*;
import java.time.LocalDate;
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

    /**
     * Função que retorna se um terceirizado trabalhou período igual ou superior a 15
     * dias em um determinado mês.
     * @param pCodFuncaoTerceirizado
     * @param pMes
     * @param pAno
     * @return boolean
     */
    public boolean FuncaoRetencaoIntegral(int pCodFuncaoTerceirizado, int pMes, int pAno) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        Date vDataInicio = null;
        Date vDataFim = null;
        Date vDataReferencia = Date.valueOf(pAno + "-" + pMes + "-01"); // Define como data referência o primeiro dia do mês e ano passados como argumentos.

        // Carrega as datas de disponibilização e desligamento do terceirizado.

        try {
            preparedStatement = connection.prepareStatement("SELECT DATA_INICIO, DATA_FIM FROM tb_funcao_terceirizado WHERE COD = ?");
            preparedStatement.setInt(1, pCodFuncaoTerceirizado);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vDataInicio = resultSet.getDate("DATA_DISPONIBILIZACAO");
                vDataFim = resultSet.getDate("DATA_DESLIGAMENTO");
            }
            }catch(SQLException sqle) {
                throw new NullPointerException("Erro ao carregar as datas de Inicio e Fim na função 'RetencaoIntegral'");
            }
                // --Caso não possua data de desligamento.
                if (vDataFim == null) {

                     // Se a data de disponibilização é inferior a data referência então o
                     // funcionário trabalhou os 30 dias do mês referência.

                    if(vDataInicio.before(vDataReferencia)) {
                        return true;
                    }

                    /*
                     -- Se a data de disponibilização está no mês referência e não se verifica
                     -- a quantidade de dias trabalhados pelo funcionário.
                     */
                    LocalDate dataRef = vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth());
                    if((vDataInicio.after(vDataReferencia) || vDataInicio.equals(vDataReferencia)) &&
                            (vDataInicio.before(Date.valueOf(dataRef)) || vDataInicio.equals(Date.valueOf(dataRef)))) {
                        LocalDate dataDisp = vDataInicio.toLocalDate().withDayOfMonth(vDataInicio.toLocalDate().lengthOfMonth());
                        if(ChronoUnit.DAYS.between(dataDisp, vDataInicio.toLocalDate()) + 1 >= 15) {
                            return true;
                        }
                    }
                }
                /*
                 * --Caso possua data de desligamento.
                 */
                if(vDataFim != null) {
                    LocalDate dataRef = vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth()); // Data com o último dia do mês da data de Referência

                    /*
                     * --Se a data de disponibilização é inferior a data referência e a data de
                     --desligamento é superior ao último dia do mês referência então o
                     --funcionário trabalhou os 30 dias.
                     */

                    if(vDataInicio.before(vDataReferencia) && vDataFim.after(Date.valueOf(dataRef))) {
                        return true;
                    }

                    /*
                     * --Se a data de disponibilização está no mês referência e a data de
                     --desligamento é superior ao mês referência, então se verifica a quantidade
                     --de dias trabalhados pelo funcionário.
                     */

                    if((vDataInicio.after(vDataReferencia) || vDataInicio.equals(vDataReferencia)) &&
                            (vDataInicio.before(Date.valueOf(dataRef)) || vDataFim.equals(Date.valueOf(dataRef))) &&
                            vDataInicio.after(Date.valueOf(dataRef))) {
                        LocalDate dataDisp = vDataInicio.toLocalDate().withDayOfMonth(vDataFim.toLocalDate().lengthOfMonth());
                        if(ChronoUnit.DAYS.between(dataDisp, vDataInicio.toLocalDate()) + 1 >= 15) {
                            return true;
                        }
                    }

                    /*--Se a data de disponibilização está no mês referência e também a data de
                            --desligamento, então contam-se os dias trabalhados pelo funcionário.
                     */

                    if((vDataInicio.after(vDataReferencia) || vDataInicio.equals(vDataReferencia)) &&
                            (vDataInicio.before(Date.valueOf(dataRef)) || vDataInicio.equals(Date.valueOf(dataRef)) &&
                                    (vDataFim.after(vDataReferencia) || vDataFim.equals(vDataReferencia)) &&
                                    (vDataFim.before(Date.valueOf(dataRef)) || vDataFim.equals(vDataReferencia)))) {
                        if(ChronoUnit.DAYS.between(vDataFim.toLocalDate(), vDataFim.toLocalDate()) + 1 >= 15) {
                            return true;
                        }

                    }

                    /*
                     --Se a data da disponibilização for inferior ao mês de cálculo e
                     --o funcionário tiver desligamento no mês referência, então contam-se
                     --os dias trabalhados.
                     */

                    if(vDataInicio.before(vDataReferencia) && (vDataFim.after(vDataReferencia) || vDataFim.equals(vDataReferencia)) &&
                            (vDataFim.before(Date.valueOf(dataRef)) || vDataFim.equals(Date.valueOf(dataRef)))) {
                        if(ChronoUnit.DAYS.between(vDataFim.toLocalDate(), vDataReferencia.toLocalDate()) + 1 >= 15) {
                            return true;
                        }
                    }
                }
        return false;
    }

    /**
     * Define se um terceirizado teve alterações em seu cargo em um determinado mês.
     * @param pCodTerceirizadoContrato
     * @param pMes
     * @param pAno
     * @return boolean
     */
    public boolean ExisteMudancaFuncao(int pCodTerceirizadoContrato, int pMes, int pAno) {

        Date vDataReferencia = Date.valueOf(pAno + "-" + pMes + "-01"); // --Definição da data referência como primeiro dia do m~es de acordo com os argumentos passados.
        int vNumeroRegistros = 0;
        boolean vRetorno = false;

        /*--Contagem do número de cargos ocupados por um determinado terceirizado no mês referência.*/

        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(COD) FROM TB_FUNCAO_TERCEIRIZADO WHERE COD_TERCEIRIZADO_CONTRATO = ? " +
                " AND ((MONTH(DATA_INICIO)=? AND YEAR(DATA_INICIO)=? ) OR (MONTH(DATA_FIM)=? AND YEAR(DATA_FIM)=?))")){
            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            preparedStatement.setInt(2, pMes);
            preparedStatement.setInt(3, pAno);
            preparedStatement.setInt(4, pMes);
            preparedStatement.setInt(5, pAno);
            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()) {
                    vNumeroRegistros = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Erro ao contar o número de cargos ocupados por um determinado terceirizado no mês referência.");
        }
        if(vNumeroRegistros != 0 && vNumeroRegistros > 1) {
            vRetorno = true;
        }else {
            vRetorno = false;
        }
        return vRetorno;
    }
}
