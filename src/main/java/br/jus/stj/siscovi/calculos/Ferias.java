package br.jus.stj.siscovi.calculos;

import com.sun.scenario.effect.impl.prism.ps.PPSBlend_REDPeer;
import javax.validation.constraints.Null;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Ferias {

    private Connection connection;

    Ferias(Connection connection) {

        this.connection = connection;

    }

    /**
     * Função que retorna o número de dias que um terceirizado possui em um determinado
     * período aquisitivo.
     * @param pDataInicio
     * @param pDataFim
     * @return float
     */

    public float DiasPeriodoAquisitivo (Date pDataInicio, Date pDataFim) {

        //Checked.

        float vDiasAUsufruir = 0;
        float vContagemDeDias = 0;
        int vControle = 0;
        Date vDataInicio = null;
        Date vDataFim = null;

        /**Calcula o número de dias baseado no período aquisitivo.*/

        do {

            /**Inicializa a data de início no primeiro laço.*/

            if (vDataInicio == null) {

                vDataInicio = pDataInicio;

            }

            /**Define o fim do mês como dia 30 exceto para fevereiro.*/

            if (vDataInicio.toLocalDate().getMonthValue() != 2) {

                vDataFim = Date.valueOf(vDataInicio.toLocalDate().getYear() + "-" + vDataInicio.toLocalDate().getMonthValue() + "-30");

            } else {

                vDataFim = Date.valueOf(vDataInicio.toLocalDate().withDayOfMonth(vDataInicio.toLocalDate().lengthOfMonth()));

            }

            /**Ajusta a data fim para o final do período aquisitivo no mês correspondente.*/

            if ((vDataFim.toLocalDate().getMonthValue() == pDataFim.toLocalDate().getMonthValue()) &&
                (vDataFim.toLocalDate().getYear() == pDataFim.toLocalDate().getYear()) &&
                (pDataFim.toLocalDate().getDayOfMonth() != 31)) {

                vDataFim = pDataFim;

            }

            vContagemDeDias = vContagemDeDias + (int)(ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate()) + 1);

            /**Para o mês de fevereiro se equaliza o número de dias contados.*/

            if (vDataFim.toLocalDate().getMonthValue() == 2) {

                /**Se o mês for de 28 dias então soma-se 2 a contagem.*/

                if (vDataFim.toLocalDate().getDayOfMonth() == 28) {

                    vContagemDeDias = vContagemDeDias + 2;

                } else {

                    /**Se o mês não for de 28 dias ele é de 29.*/

                    if (vDataFim.toLocalDate().getDayOfMonth() == 29) {

                        vContagemDeDias = vContagemDeDias + 1;

                    }

                }

            }

            vDataInicio = Date.valueOf((vDataInicio.toLocalDate().withDayOfMonth(vDataInicio.toLocalDate().lengthOfMonth())).plusDays(1));

            if ((vDataFim.toLocalDate().getMonthValue() == pDataFim.toLocalDate().getMonthValue()) &&
                    (vDataFim.toLocalDate().getYear() == pDataFim.toLocalDate().getYear())) {

                vControle = 1;

            }

        } while (vControle == 0);

        if (vContagemDeDias > 360) {

            throw new NullPointerException("O período aquisitivo informado contabiliza mais de 360 dias.");

        }

        /**A cada 12 dias de trabalho o funcionário adquire 1 dias de férias,
         considerando um período de 360 dias, óbviamente.*/

        vDiasAUsufruir = vContagemDeDias/12;

        return vDiasAUsufruir;

    }

    /**
     * Função que retorna o número de parcelas de férias
     * concedidas a um funcionário em um determinado
     * contrato em um período aquisitivo específico.
     * período aquisitivo.
     * @param pCodTerceirizadoContrato
     * @param pDataInicio
     * @param pDataFim
     * @return int
     */

    public int ParcelasConcedidas (int pCodTerceirizadoContrato, Date pDataInicio, Date pDataFim) {

        //Checked.

        int vParcelasConcedidas = 0;

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        try {

            preparedStatement = connection.prepareStatement("SELECT COUNT(cod)" +
                                                                 " FROM tb_restituicao_ferias" +
                                                                 " WHERE cod_terceirizado_contrato = ?" +
                                                                   " AND data_inicio_periodo_aquisitivo = ?" +
                                                                   " AND data_fim_periodo_aquisitivo = ?");

            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            preparedStatement.setDate(2, pDataFim);
            preparedStatement.setDate(3, pDataFim);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vParcelasConcedidas = resultSet.getInt(1);

            }

        } catch (SQLException sqle) {

            sqle.printStackTrace();
            throw new NullPointerException("Erro ao verificar parcelas de férias concedidas no período.");

        }

        return vParcelasConcedidas;

    }

}