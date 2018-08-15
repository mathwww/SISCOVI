package br.jus.stj.siscovi.calculos;

import com.sun.scenario.effect.impl.prism.ps.PPSBlend_REDPeer;
import javax.validation.constraints.Null;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Saldo {

    private Connection connection;

    Saldo (Connection connection) {

        this.connection = connection;

    }

    /**
     * Função que retorna um valor relacionado ao saldo da conta vinculada.
     * período aquisitivo.
     * @param pCodTerceirizadoContrato
     * @param pAno
     * @param pOperacao
     * @param pCodRubrica
     * @return float
     */

    public float SaldoContaVinculada (int pCodTerceirizadoContrato, int pAno, int pOperacao, int pCodRubrica) {

        //Checked.

        /**pOperacao = 1 - RETENÇÃO POR FUNCIONÁRIO
           pOperacao = 2 - RESTITUIÇÃO FÉRIAS POR FUNCIONÁRIO
           pOperacao = 3 - RESTITUICAO 13º POR FUNCIONÁRIO

           Legenda de rúbricas:
           1 - Férias
           2 - Terço constitucional
           3 - Décimo terceiro
           4 - FGTS
           5 - Multa do FGTS
           6 - Penalidade do FGTS
           7 - Incidência do submódulo 4.1 (corresponde a inciência de retenção)

           pCodRubrica especiais:
           100 - TOTAL
           101 - Incidência de férias
           102 - Incidência de terço de férias
           103 - Incidência de décimo terceiro*/

        float vFeriasRetido = 0;
        float vTercoConstitucionalRetido = 0;
        float vDecimoTerceiroRetido = 0;
        float vIncidenciaRetido = 0;
        float vMultaFGTSRetido = 0;
        float vTotalRetido = 0;
        float vFeriasRestituido = 0;
        float vTercoConstitucionalRestituido = 0;
        float vIncidenciaFeriasRestituido = 0;
        float vIncidenciaTercoRestituido = 0;
        float vDecimoTerceiroRestituido = 0;
        float vIncidencia13Restituido = 0;
        float vTotalRestituido = 0;

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        //Definição dos valores relacionados a retenção por funcionário.

        if (pOperacao == 1) {

            try {

                preparedStatement = connection.prepareStatement("SELECT SUM(tmr.ferias + DECODE(rtm.ferias, NULL, 0, rtm.ferias)) AS \"Férias retido\",\n" +
                        "           SUM(tmr.terco_constitucional + DECODE(rtm.terco_constitucional, NULL, 0, rtm.terco_constitucional))  AS \"Abono de férias retido\",\n" +
                        "           SUM(tmr.decimo_terceiro + DECODE(rtm.decimo_terceiro, NULL, 0, rtm.decimo_terceiro)) AS \"Décimo terceiro retido\",\n" +
                        "           SUM(tmr.incidencia_submodulo_4_1 + DECODE(rtm.incidencia_submodulo_4_1, NULL, 0, rtm.incidencia_submodulo_4_1)) AS \"Incid. do submód. 4.1 retido\",\n" +
                        "           SUM(tmr.multa_fgts + DECODE(rtm.multa_fgts, NULL, 0, rtm.multa_fgts)) AS \"Multa do FGTS retido\",\n" +
                        "           SUM(tmr.total + DECODE(rtm.total, NULL, 0, rtm.total)) AS \"Total retido\"\n" +
                        "      FROM tb_total_mensal_a_reter tmr\n" +
                        "        JOIN tb_terceirizado_contrato tc ON tc.cod = tmr.cod_terceirizado_contrato\n" +
                        "        LEFT JOIN tb_retroatividade_total_mensal rtm ON rtm.cod_total_mensal_a_reter = tmr.cod\n" +
                        "      WHERE YEAR(tmr.data_referencia) = ?\n" +
                        "        AND tc.cod = ?");

                preparedStatement.setInt(1, pAno);
                preparedStatement.setInt(2, pCodTerceirizadoContrato);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {

                    vFeriasRetido = resultSet.getFloat(1);
                    vTercoConstitucionalRetido = resultSet.getFloat(2);
                    vDecimoTerceiroRetido = resultSet.getFloat(3);
                    vIncidenciaRetido = resultSet.getFloat(4);
                    vMultaFGTSRetido = resultSet.getFloat(5);
                    vTotalRetido = resultSet.getFloat(6);

                }

            } catch (SQLException sqle) {

                throw new NullPointerException("Não foi possível recuperar o valor retido.");

            }

        }

        //Definição dos valores relacionados a restituição de férias por funcionário.

        if (pOperacao == 2) {

            try {

                preparedStatement = connection.prepareStatement("SELECT SUM(rf.valor_ferias) AS \"Férias restituído\",\n" +
                        "           SUM(rf.valor_terco_constitucional) AS \"1/3 constitucional restituído\",\n" +
                        "           SUM(rf.incid_submod_4_1_ferias) AS \"Incid. de férias restituído\",\n" +
                        "           SUM(rf.incid_submod_4_1_terco) AS \"Incod. de terço restituído\",\n" +
                        "           SUM(rf.valor_ferias + rf.valor_terco_constitucional + rf.incid_submod_4_1_ferias + rf.incid_submod_4_1_terco) AS \"Total restituído\"\n" +
                        "      FROM tb_restituicao_ferias rf\n" +
                        "        JOIN tb_terceirizado_contrato tc ON tc.cod = rf.cod_terceirizado_contrato\n" +
                        "      WHERE YEAR(rf.data_inicio_periodo_aquisitivo) = ?\n" +
                        "        AND tc.cod = ?;");

                preparedStatement.setInt(1, pAno);
                preparedStatement.setInt(2, pCodTerceirizadoContrato);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {

                    vFeriasRestituido = resultSet.getFloat(1);
                    vTercoConstitucionalRestituido = resultSet.getFloat(2);
                    vIncidenciaFeriasRestituido = resultSet.getFloat(3);
                    vIncidenciaTercoRestituido = resultSet.getFloat(4);
                    vTotalRestituido = resultSet.getFloat(5);

                }

            } catch (SQLException sqle) {

                sqle.printStackTrace();

                throw new NullPointerException("Não foi possível recuperar o valor restituído de férias.");

            }

        }

        //Definição dos valores relacionados a restituição de férias por funcionário.

        if (pOperacao == 3) {

            try {

                preparedStatement = connection.prepareStatement("SELECT SUM(rdt.valor) AS \"Décimo terceiro restituído\",\n" +
                        "           SUM(rdt.incidencia_submodulo_4_1) AS \"Incid. de 13° restituído\",\n" +
                        "           SUM(rdt.valor + rdt.incidencia_submodulo_4_1) AS \"Total restituído\"\n" +
                        "      FROM tb_restituicao_decimo_terceiro rdt\n" +
                        "        JOIN tb_terceirizado_contrato tc ON tc.cod = rdt.cod_terceirizado_contrato\n" +
                        "      WHERE YEAR(rdt.data_inicio_contagem) = ?\n" +
                        "        AND tc.cod = ?;");

                preparedStatement.setInt(1, pAno);
                preparedStatement.setInt(2, pCodTerceirizadoContrato);
                resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {

                    vDecimoTerceiroRestituido = resultSet.getFloat(1);
                    vIncidencia13Restituido = resultSet.getFloat(2);
                    vTotalRestituido = resultSet.getFloat(3);

                }

            } catch (SQLException sqle) {

                sqle.printStackTrace();

                throw new NullPointerException("Não foi possível recuperar o valor restituído de 13°.");

            }

        }

        /**Retorno do valor de férias retido.*/

        if ((pOperacao == 1) && (pCodRubrica == 1)) {

        return vFeriasRetido;

        }

        /**Retorno do valor de terço constitucional retido.*/

        if (pOperacao == 1 && pCodRubrica == 2) {

            return vTercoConstitucionalRetido;

        }

        /**Retorno do valor de décimo terceiro retido.*/

        if (pOperacao == 1 && pCodRubrica == 3) {

            return vDecimoTerceiroRetido;

        }

        /**Retorno do valor de incidência retido.*/

        if (pOperacao == 1 && pCodRubrica == 7) {

            return vIncidenciaRetido;

        }

        /**Retorno do valor de multa do FGTS retido.*/

        if (pOperacao == 1 && pCodRubrica == 5) {

            return vMultaFGTSRetido;

        }

        /**Retorno do valor total retido.*/

        if (pOperacao == 1 && pCodRubrica == 100) {

            return vTotalRetido;

        }

        /**Retorno do valor de férias restituído.*/

        if (pOperacao == 2 && pCodRubrica == 1) {

            return vFeriasRestituido;

        }

        /**Retorno do valor de terço constitucional restituído.*/

        if (pOperacao == 2 && pCodRubrica == 2) {

            return vTercoConstitucionalRestituido;

        }

        /**Retorno do valor de incidência sobre férias restituído.*/

        if (pOperacao == 2 && pCodRubrica == 101) {

            return vIncidenciaFeriasRestituido;

        }

        /**Retorno do valor de incidência sobre férias restituído.*/

        if (pOperacao == 2 && pCodRubrica == 102) {

            return vIncidenciaTercoRestituido;

        }

        /**Retorno do valor total restituído de férias.*/

        if (pOperacao == 2 && pCodRubrica == 100) {

            return vTotalRestituido;

        }

        /**Retorno do valor de décimo terceiro restituído.*/

        if (pOperacao == 3 && pCodRubrica == 3) {

            return vDecimoTerceiroRestituido;

        }

        /**Retorno do valor de incidência de décimo terceiro restituído.*/

        if (pOperacao == 3 && pCodRubrica == 103) {

            return vIncidencia13Restituido;

        }

        /**Retorno do valor total restituído de férias.*/

        if (pOperacao == 3 && pCodRubrica == 100) {

            return vTotalRestituido;

        }

        return 0;

    }


}