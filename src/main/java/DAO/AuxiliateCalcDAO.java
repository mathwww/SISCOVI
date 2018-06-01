package DAO;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import com.sun.scenario.effect.impl.prism.ps.PPSBlend_REDPeer;
import sun.dc.pr.PRError;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

public class AuxiliateCalcDAO {
    private Connection connection;
    public AuxiliateCalcDAO(Connection connection){
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

    public String RetornaAnoContrato(int cod) throws SQLException {
        String dataI;
        Date dataInicio;
        dataInicio = RetornaAnoContratoD(cod);
        if(dataInicio == null){
            return null;
        }else {
            dataI = dataInicio.toString();
            return dataI;
        }
    }

    public Date RetornaAnoContratoD(int cod) throws SQLException{
        ResultSet rs;
        Date dataInicio;
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT DATA_INICIO FROM TB_CONTRATO WHERE COD=?");
        preparedStatement.setInt(1,cod);
        rs = preparedStatement.executeQuery();
        if(rs.next()){
            dataInicio = rs.getDate(1);
            return dataInicio;
        }
        return null;
    }

    public Boolean ExisteRetroatividade(int codContrato, int codCargoContrato, int mes, int ano, int operacao ) throws SQLException {
        ResultSet rs;
        Date dataRef = Date.valueOf(""+ano+"-"+mes+"-"+"01");
        // int retroatividade;

        if(operacao == 1) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(rc.cod) FROM TB_RETROATIVIDADE_CONVENCAO rc " +
                    "JOIN TB_CONVENCAO_COLETIVA cc ON cc.cod=rc.cod_convencao_coletiva WHERE cc.cod_cargo_contrato=? AND ? >= DATEADD(DAY,1,EOMONTH(DATEADD(MONTH,-1,inicio))) AND ?<= fim");
            preparedStatement.setInt(1,codCargoContrato);
            preparedStatement.setDate(2, dataRef);
            preparedStatement.setDate(3,dataRef);
             rs = preparedStatement.executeQuery();

            if(rs.next()){
                if(rs.getInt(1) == 0){
                    return false;
                }else {
                    return true;
                }

            }
           return null;
        }
        if(operacao == 2) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(rp.cod) FROM TB_RETROATIVIDADE_PERCENTUAL rp " +
                    "JOIN TB_PERCENTUAL_CONTRATO pc ON pc.cod=rp.cod_percentual_contrato WHERE pc.cod_contrato=? AND ? >= DATEADD(DAY,1,EOMONTH(DATEADD(MONTH,-1,inicio))) AND ?<= fim");
            preparedStatement.setInt(1,codContrato);
            preparedStatement.setDate(2, dataRef);
            preparedStatement.setDate(3,dataRef);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                rs.getInt(1);
                return true;
            }
            return null;
        }

        return false;
    }

    public int RetornaConvencaoAnterior(int codConvencao) throws SQLException {
        int codConvencaoAnterior = 0;
        int codCargoContrato = 0;
        Date dataRef = null;
        ResultSet rs;
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COD_CARGO_CONTRATO, DATA_INICIO_CONVENCAO FROM TB_CONVENCAO_COLETIVA WHERE COD=?");
        preparedStatement.setInt(1,codConvencao);
        rs = preparedStatement.executeQuery();
        if(rs.next()){
            codCargoContrato = rs.getInt(1);
            dataRef = rs.getDate(2);

        }
        preparedStatement = connection.prepareStatement("SELECT COD FROM TB_CONVENCAO_COLETIVA WHERE DATA_ADITAMENTO IS NOT NULL AND COD_CARGO_CONTRATO=? AND DATA_INICIO_CONVENCAO="+
                "(SELECT MAX(DATA_INICIO_CONVENCAO) FROM TB_CONVENCAO_COLETIVA WHERE DATA_INICIO_CONVENCAO < ? AND COD_CARGO_CONTRATO=? AND DATA_ADITAMENTO IS NOT NULL)");
        preparedStatement.setInt(1, codCargoContrato);
        preparedStatement.setDate(2, dataRef);
        preparedStatement.setInt(3, codCargoContrato);
        rs = preparedStatement.executeQuery();
        if(rs.next()){
            codConvencaoAnterior = rs.getInt(1);
            return codConvencaoAnterior;
        }
        return -1;
    }

    public float RetornaRemuneracaoPeriodo(int pCodCargoContrato, int pMes, int pAno, int pOperacao, int pRetroatividade) throws SQLException{

        float vRemuneracao = 0;
        Date vDataReferencia = Date.valueOf(""+pAno+"-"+pMes+"-"+"01");
        int vCodContrato = 0;
        int vCodConvencao = 0;
        boolean vRetroatividade = false;
        Date vDataAditamento = null;

        ResultSet rs;


        PreparedStatement preparedStatement = connection.prepareStatement("SELECT COD_CONTRATO FROM TB_CARGO_CONTRATO WHERE COD=?");
        preparedStatement.setInt(1, pCodCargoContrato);
        rs = preparedStatement.executeQuery();
        if(rs.next()) {
            vCodContrato = rs.getInt(1);
        }else {
            return -1;
        }
        if(pRetroatividade == 1) {
            try {
                vRetroatividade = ExisteRetroatividade(vCodContrato, pCodCargoContrato, pMes, pAno, 1);
            }catch(SQLException sqle) {
                sqle.printStackTrace();
                return -2;
            }
        }
        if(pOperacao == 1) {
            preparedStatement = connection.prepareStatement("SELECT remuneracao, cod, data_aditamento FROM tb_convencao_coletiva" +
                    " WHERE cod_cargo_contrato = ?  AND data_aditamento IS NOT NULL" +
                    " AND (SELECT CAST(CAST(DATA_ADITAMENTO AS DATE) AS DATETIME) ) <= (SELECT CAST(CAST(CURRENT_TIMESTAMP AS DATE) AS DATETIME))" +
                    " AND (((((SELECT CAST(CAST(DATA_INICIO_CONVENCAO AS DATE) AS DATETIME)) <= (SELECT CAST(CAST(? AS DATE) AS DATETIME))) AND" +
                    " ((SELECT CAST(CAST(DATA_INICIO_CONVENCAO AS DATE) AS DATETIME)) <= (SELECT CAST(CAST(EOMONTH(?) AS DATE) AS DATETIME))))" +
                    " AND ((((SELECT CAST(CAST(DATA_FIM_CONVENCAO AS DATE) AS DATETIME)) >= (SELECT CAST(CAST(? AS DATE) AS DATETIME)))" +
                    " AND ((SELECT CAST(CAST(DATA_FIM_CONVENCAO AS DATE) AS DATETIME)) >= (SELECT CAST(CAST(EOMONTH(?) AS DATE) AS DATETIME)))" +
                    " OR data_fim_convencao IS NULL)))" +
                    " OR ((SELECT DATEPART(MONTH, DATA_INICIO_CONVENCAO)) = (SELECT DATEPART(MONTH, ?))" +
                    " AND (SELECT DATEPART(YEAR, DATA_INICIO_CONVENCAO)) = (SELECT DATEPART(YEAR, ?))))");
            preparedStatement.setInt(1, pCodCargoContrato);
            preparedStatement.setDate(2, vDataReferencia);
            preparedStatement.setDate(3, vDataReferencia);
            preparedStatement.setDate(4, vDataReferencia);
            preparedStatement.setDate(5, vDataReferencia);
            preparedStatement.setDate(6, vDataReferencia);
            preparedStatement.setDate(7, vDataReferencia);
            rs = preparedStatement.executeQuery();
            if(rs.next()){
                vRemuneracao = rs.getFloat("REMUNERACAO");
                vCodConvencao = rs.getInt("COD");
                vDataAditamento = rs.getDate("DATA_ADITAMENTO");
                return vRemuneracao;
            }
        }

        if(pOperacao == 2){
            preparedStatement = connection.prepareStatement("SELECT REMUNERACAO FROM TB_CONVENCAO_COLETIVA WHERE COD_CARGO_CONTRATO=? AND DATA_ADITAMENTO IS NOT NULL AND" +
                    " ((SELECT DATEPART(MONTH, DATA_FIM_CONVENCAO)) = (SELECT DATEPART(MONTH , ?))" +
                    " AND (SELECT DATEPART(YEAR, DATA_FIM_CONVENCAO)) = (SELECT DATEPART(YEAR, ?)))");
            preparedStatement.setInt(1, pCodCargoContrato);
            preparedStatement.setDate(2, vDataReferencia);
            preparedStatement.setDate(3, vDataReferencia);
            rs = preparedStatement.executeQuery();
            if(rs.next()){
                vRemuneracao = rs.getFloat("REMUNERACAO");
                return vRemuneracao;
            }
        }
        if((pOperacao == 1) && (vRetroatividade == true)){
            vCodConvencao = RetornaConvencaoAnterior(vCodConvencao);
            preparedStatement = connection.prepareStatement("SELECT REMUNERACAO FROM TB_CONVENCAO_COLETIVA WHERE COD=?");
            preparedStatement.setInt(1, vCodConvencao);
            rs = preparedStatement.executeQuery();
            if (rs.next()){
                vRemuneracao = rs.getFloat("REMUNERACAO");
                return vRemuneracao;
            }
        }
        if((pRetroatividade == 1) && (pOperacao == 1)){
                vRemuneracao = RetornaRemuneracaoPeriodo(pCodCargoContrato, pMes, pAno, 2, 1);

                return vRemuneracao;
            }
            if ((pRetroatividade == 2) && (pOperacao == 1)) {
                vRemuneracao = RetornaRemuneracaoPeriodo(pCodCargoContrato, pMes, pAno,2,2);
                return vRemuneracao;
            }

        return 0;
    }

    public long DiasTrabalhadosMesParcial(int pCodCargoContrato, int pCodCargoFuncionario, int pMes, int pAno, int pOperacao) throws SQLException {

        /**
         --Função que retorna o número de dias que um funcionário
         --trabalhou em determinado período do mês. */

        int vCodContrato = 0;
        Date vDataReferencia = null;
        Date vDataFim = null;
        Date vDataDisponibilizacao = null;
        Date vDataDesligamento = null;
        Date vDataInicio = null;

        /**
         --Operação 1: Primeira metade da convenção.
         --Operação 2: Segunda metade da convenção.
         --Operação 3: Primeira metade do percentual.
         --Operação 4: Segunda metade do percentual. */

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        vDataReferencia = Date.valueOf(""+pAno+"-"+pMes+"-01");

        preparedStatement = connection.prepareStatement("SELECT COD_CONTRATO FROM TB_CARGO_CONTRATO WHERE COD=?");
        preparedStatement.setInt(1, pCodCargoContrato);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            vCodContrato = resultSet.getInt("COD_CONTRATO");
        }
        preparedStatement = connection.prepareStatement("SELECT DATA_DISPONIBILIZACAO, DATA_DESLIGAMENTO FROM TB_CARGO_FUNCIONARIO WHERE COD=?");
        preparedStatement.setInt(1, pCodCargoFuncionario);
        resultSet = preparedStatement.executeQuery();
        if(resultSet.next()){
            vDataDisponibilizacao = resultSet.getDate("DATA_DISPONIBILIZACAO");
            vDataDesligamento = resultSet.getDate("DATA_DESLIGAMENTO");
        }

        /** --Primeira metade da convenção (a convenção anterior tem data fim naquele mês). */

        if(pOperacao == 1){
            preparedStatement = connection.prepareStatement("SELECT DATA_FIM_CONVENCAO, ? FROM TB_CONVENCAO_COLETIVA WHERE DATA_ADITAMENTO IS NOT NULL " +
                    "AND (SELECT DATEPART(MONTH, DATA_FIM_CONVENCAO))=(SELECT DATEPART(MONTH, ?)) AND (SELECT DATEPART(YEAR, DATA_FIM_CONVENCAO)) = (SELECT DATEPART(YEAR, ?))");
            preparedStatement.setDate(1, vDataReferencia);
            preparedStatement.setDate(2, vDataReferencia);
            preparedStatement.setDate(3, vDataReferencia);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                vDataFim = resultSet.getDate("DATA_FIM_CONVENCAO");
                vDataInicio = resultSet.getDate(2);
            }
        }

        /** --Segunda metade da convenção (a convenção mais recente tem data inicio naquele mês). */

        if(pOperacao == 2){
            preparedStatement = connection.prepareStatement("SELECT (SELECT EOMONTH(?)), DATA_INICIO_CONVENCAO FROM TB_CONVENCAO_COLETIVA WHERE DATA_ADITAMENTO IS NOT NULL " +
                    "AND (SELECT DATEPART(MONTH, DATA_INICIO_CONVENCAO))=(SELECT DATEPART(MONTH, ?)) AND (SELECT DATEPART(YEAR, DATA_INICIO_CONVENCAO))=(SELECT DATEPART(YEAR, ?))");
            preparedStatement.setDate(1, vDataReferencia);
            preparedStatement.setDate(2, vDataReferencia);
            preparedStatement.setDate(3, vDataReferencia);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                vDataFim = resultSet.getDate(1);
                vDataInicio = resultSet.getDate("DATA_INICIO_CONVENCAO");
            }
            LocalDate date = vDataFim.toLocalDate(); // Converte vDataFim para a nova API de Datas do Java 8
            int dataFim = date.getDayOfMonth(); // Extrai o dia do mês da data segundo a nova API
            if(dataFim == 31){
                vDataFim = Date.valueOf(date.minusDays(1)); // Converte de volta o dia do mês menos 1 para a data em sql
            }
        }

        /** --Primeira metade do percentual (último percentual não tem data fim). */

        if(pOperacao == 3){
            preparedStatement = connection.prepareStatement("SELECT MAX(pc.DATA_FIM), ? FROM TB_PERCENTUAL_CONTRATO pc JOIN TB_RUBRICAS r ON r.COD=pc.COD_RUBRICA WHERE COD_CONTRATO=? " +
                    "AND pc.DATA_ADITAMENTO IS NOT NULL AND (SELECT DATEPART(MONTH, pc.DATA_FIM))=(SELECT DATEPART(MONTH, ?)) AND (SELECT DATEPART(YEAR, DATA_FIM))=(SELECT DATEPART(YEAR, ?))");
            preparedStatement.setDate(1, vDataReferencia);
            preparedStatement.setInt(2, vCodContrato);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                vDataFim = resultSet.getDate(1);
                vDataInicio = resultSet.getDate(2);
            }
        }

        /** -Segunda metade do percentual. */

        if(pOperacao == 4) {
            preparedStatement = connection.prepareStatement("SELECT MIN(PC.DATA_INICIO), EOMONTH(?) FROM TB_PERCENTUAL_CONTRATO PC JOIN TB_RUBRICAS R ON R.COD=PC.COD_RUBRICA" +
                    " WHERE COD_CONTRATO=? AND PC.DATA_ADITAMENTO IS NOT NULL AND (SELECT DATEPART(MONTH, PC.DATA_INICIO))=(SELECT DATEPART(MONTH,?)) AND" +
                    " (SELECT DATEPART(YEAR, DATA_INICIO))=(SELECT DATEPART(YEAR, ?))");
            preparedStatement.setDate(1, vDataReferencia);
            preparedStatement.setInt(2, vCodContrato);
            preparedStatement.setDate(3, vDataReferencia);
            preparedStatement.setDate(4, vDataReferencia);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                vDataInicio = resultSet.getDate(1);
                vDataReferencia = resultSet.getDate(2);
                /** --Ajuste do último dia para que o mês contenha apenas 30 dias. */

                LocalDate date = vDataInicio.toLocalDate(); // Converte vDataInicio para a nova API de Datas do Java 8
                int dataInicio = date.getDayOfMonth(); // Extrai o dia do mês da data segundo a nova API
                if(dataInicio == 31){
                    vDataInicio = Date.valueOf(date.minusDays(1)); // Converte de volta o dia do mês menos 1 para a data em sql
                }
            }
        }

        /** --Definição do número de dias trabalhados para o caso de primeira metade do mês. */
        if((pOperacao == 1) || (pOperacao == 3)){
            if(vDataDesligamento == null){
                if(vDataDisponibilizacao.before(vDataReferencia)){
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataReferencia = vDataReferencia.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataReferencia, dataFim) + 1;
                    return numeroDeDias;
                }

                if((vDataDisponibilizacao.after(vDataReferencia) || vDataDisponibilizacao.equals(vDataReferencia)) && (vDataDisponibilizacao.equals(vDataFim) || vDataDisponibilizacao.before(vDataFim))){
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataDisponibilizacao = vDataDisponibilizacao.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataDisponibilizacao, dataFim) + 1;
                    return  numeroDeDias;
                }

            }
            if(vDataDesligamento != null){
                /** --Se a data de disponibilização é inferior a data referência e a data de
                 --desligamento é superior ao último dia do mês referência então o
                 --funcionário trabalhou os dias entre o inicio do mes e a data fim. */

                if((vDataDisponibilizacao.before(vDataReferencia)) && (vDataDesligamento.after(vDataFim))){
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataReferencia = vDataReferencia.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataReferencia, dataFim) + 1;
                    return numeroDeDias;
                }

                /** --Se a data de disponibilização está no mês referência e a data de
                 --desligamento é superior mês referência, então se verifica a quantidade
                 --de dias trabalhados pelo funcionário entre a data fim e a disponibilização. */

                if((vDataDisponibilizacao.after(vDataReferencia) || vDataDisponibilizacao.equals(vDataReferencia)) && (vDataDisponibilizacao.before(vDataFim) || vDataDisponibilizacao.equals(vDataFim))
                        && (vDataDesligamento.after(vDataFim))){
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataDisponibilizacao = vDataDisponibilizacao.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataDisponibilizacao,dataFim) + 1;
                    return numeroDeDias;
                }

                /** --Se a data de disponibilização está na primeira metade do mês referência
                 --e também a data de desligamento, então contam-se os dias trabalhados
                 --pelo funcionário entre o desligamento e a disponibilização. */

                if((vDataDisponibilizacao.equals(vDataReferencia) || vDataDisponibilizacao.after(vDataReferencia)) && (vDataDisponibilizacao.before(vDataReferencia) || vDataDisponibilizacao.equals(vDataReferencia)) &&
                        (vDataDesligamento.after(vDataReferencia) || vDataDesligamento.equals(vDataReferencia)) && (vDataDesligamento.before(vDataFim) || vDataDesligamento.equals(vDataFim))) {
                    LocalDate dataDesligamento = vDataDesligamento.toLocalDate();
                    LocalDate dataDisponibilizacao = vDataDisponibilizacao.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataDisponibilizacao, dataDesligamento) + 1;
                    return numeroDeDias;
                }

                /** --Se a data da disponibilização for inferior ao mês de cálculo e
                 --o funcionário tiver desligamento antes da data fim, então contam-se
                 --os dias trabalhados nesse período. */

                if(vDataDisponibilizacao.before(vDataReferencia) && (vDataDesligamento.after(vDataReferencia) || vDataDesligamento.equals(vDataReferencia))
                        && (vDataDesligamento.before(vDataFim) || vDataDesligamento.before(vDataFim))){
                    LocalDate dataDesligamento = vDataDesligamento.toLocalDate();
                    LocalDate dataReferencia = vDataReferencia.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataReferencia, dataDesligamento) + 1;
                    return numeroDeDias;
                }
            }
        }

        /** --Cálculo para a segunda metade do mês. */

        if((pOperacao == 2) || (pOperacao == 4)){

            if(vDataDesligamento == null){
                if(vDataReferencia.after(vDataDisponibilizacao)) {
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataInicio = vDataInicio.toLocalDate();
                    long numeroDeDias = Duration.between(dataFim, dataInicio).toDays() + 1;
                    return numeroDeDias;
                }

                if((vDataDisponibilizacao.after(vDataInicio) || vDataDisponibilizacao.equals(vDataInicio)) && (vDataDisponibilizacao.before(vDataFim) || vDataDisponibilizacao.equals(vDataFim))){
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataDisponibilizacao = vDataDisponibilizacao.toLocalDate();
                    long numeroDeDias = Duration.between( dataDisponibilizacao, dataFim).toDays() + 1;
                    return numeroDeDias;
                }
            }

            if(vDataDesligamento != null){

                /** --Se a data de disponibilização é inferior a data referência e a data de
                 --desligamento é superior ao último dia do mês referência então o
                 --funcionário trabalhou os dias entre o início e o fim. */

                if(vDataDisponibilizacao.before(vDataReferencia) && vDataDesligamento.after(vDataFim)){
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataInicio = vDataInicio.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
                    return numeroDeDias;
                }

                /** --Se a data de disponibilização é maior que a data de inicio
                 --e a data de desligamento superior a data de fim então
                 --conta-se o período entre data fim e data de disponibilização. */

                if((vDataInicio.before(vDataDisponibilizacao) || vDataDisponibilizacao.equals(vDataInicio)) && (vDataDisponibilizacao.before(vDataFim) || vDataDisponibilizacao.equals(vDataFim))
                        && (vDataDesligamento.after(vDataFim))) {
                    LocalDate dataFim = vDataFim.toLocalDate();
                    LocalDate dataDisponibilizacao = vDataDisponibilizacao.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataDisponibilizacao,dataFim) + 1;
                    return numeroDeDias;
                }

                /** --Se a data de disponibilização é maior que a data de inicio
                 --e a data de desligamento inferior a data de fim e superior
                 --a data de inicio então conta-se este período. */

                if((vDataDisponibilizacao.after(vDataInicio) || vDataDisponibilizacao.equals(vDataInicio)) && (vDataDisponibilizacao.before(vDataFim) || vDataDisponibilizacao.equals(vDataFim))
                        && (vDataDesligamento.after(vDataInicio) || vDataDesligamento.equals(vDataInicio)) && (vDataDesligamento.before(vDataFim) || vDataDesligamento.equals(vDataFim))) {
                    LocalDate dataDesligamento = vDataDesligamento.toLocalDate();
                    LocalDate dataDisponibilizacao = vDataDisponibilizacao.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataDisponibilizacao, dataDesligamento) + 1;
                    return numeroDeDias;
                }

                /** --Se a data da disponibilização for inferior ao mês de cálculo e
                 --o funcionário tiver desligamento no mês referência, então contam-se
                 --os dias trabalhados. */

                if(vDataDisponibilizacao.before(vDataInicio) && (vDataDesligamento.after(vDataInicio) || vDataDesligamento.equals(vDataInicio))
                        && (vDataDesligamento.before(vDataFim) || vDataDesligamento.equals(vDataFim))){
                    LocalDate dataDesligamento = vDataDesligamento.toLocalDate();
                    LocalDate dataInicio = vDataInicio.toLocalDate();
                    long numeroDeDias = ChronoUnit.DAYS.between(dataInicio, dataDesligamento) + 1;
                    return numeroDeDias;
                }
            }
        }

        return 0;
    }
}
