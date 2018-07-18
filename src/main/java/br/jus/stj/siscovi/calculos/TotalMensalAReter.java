package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.model.CodTerceirizadoECodFuncaoTerceirizadoModel;

import java.sql.*;
import java.util.ArrayList;

public class TotalMensalAReter {
    private Connection connection;
    public TotalMensalAReter(Connection connection) {
        this.connection = connection;
    }

    /**
     * Método que calcula o total mensal a reter em um determinado mês para
     * um determinado contrato.
     *
     * @param pCodContrato
     * @param pMes
     * @param pAno
     */
    public void CalculaTotalMensal(int pCodContrato, int pMes, int pAno) {
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        Retencao retencao = new Retencao(connection);
        Percentual percentual = new Percentual(connection);
        Periodos periodo = new Periodos(connection);
        Remuneracao remuneracao = new Remuneracao(connection);

        float vTotalFerias = 0;
        float vTotalTercoConstitucional = 0;
        float vTotalDecimoTerceiro = 0;
        float vTotalIncidencia = 0;
        float vTotalIndenizacao = 0;
        float vTotal = 0;

        float vValorFerias = 0;
        float vValorTercoConstitucional = 0;
        float vValorDecimoTerceiro = 0;
        float vValorIncidencia = 0;
        float vValorIndenizacao = 0 ;

        float vPercentualFerias = 0;
        float vPercentualTercoConstitucional = 0;
        float vPercentualDecimoTerceiro = 0;
        float vPercentualIncidencia = 0;
        float vPercentualIndenizacao = 0;
        float vPercentualPenalidadeFGTS = 0;
        float vPercentualMultaFGTS = 0;
        float vRemuneracao = 0;
        float vRemuneracao2 = 0;

        int vExisteCalculo = 0;
        Date vDataReferencia = Date.valueOf(pAno + "-" + pMes + "-01");
        Date vDataInicioConvencao = null;
        Date vDataFimConvencao = null;
        Date vDataInicioPercentual = null;
        Date vDataFimPercentual = null;
        Date vDataFimPercentualEstatico = null;
        Date vDataFimMes = Date.valueOf(vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth()));
        Date vDataRetroatividadeConvencao = null;
        Date vFimRetroatividadeConvencao = null;
        Date vDataRetroatividadePercentual = null;
        Date vFimRetroatividadePercentual = null;
        Date vDataRetroatividadePercentual2 = null;
        Date vDataFimRetroatividadePercentual2 = null;
        Date vDataInicio = null;
        Date vDataFim = null;
        Date vDataCobranca = null;
        Date vDataInicioContrato = null;
        Date vDataFimContrato = null;

        int vCheck = 0;

        /* --Checagem da validade do contrato passado (existe). */

        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT(COD) FROM TB_CONTRATO WHERE COD=?");
            preparedStatement.setInt(1, pCodContrato);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vCheck = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        vDataFimMes = adaptaDataPara360(vDataFimMes);

        /*--Se a data passada for anterior ao contrato ou posterior ao seu termino aborta-se.*/
        try{
            preparedStatement = connection.prepareStatement("SELECT MIN(EC.DATA_INICIO_VIGENCIA), MAX(EC.DATA_FIM_VIGENCIA) FROM tb_evento_contratual EC WHERE EC.COD_CONTRATO=?");
            preparedStatement.setInt(1, pCodContrato);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vDataInicioContrato = resultSet.getDate(1);
                vDataFimContrato = resultSet.getDate(2);
            }
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        Date dataTemp = Date.valueOf(vDataFimContrato.toLocalDate().minusMonths(1).plusDays(1));
        if(vDataReferencia.after(Date.valueOf(dataTemp.toLocalDate().withDayOfMonth(dataTemp.toLocalDate().lengthOfMonth())))) {
            return;
        }

        /*--Verificação da existência de cálculo para aquele mês e consequente deleção.*/

        try {
            preparedStatement = connection.prepareStatement("SELECT COUNT (TMR.COD) FROM TB_TOTAL_MENSAL_A_RETER TMR JOIN TB_TERCEIRIZADO_CONTRATO TC ON TC.COD=TMR.COD_TERCEIRIZADO_CONTRATO" +
                    " WHERE MONTH(TMR.DATA_REFERENCIA)=? AND YEAR(TMR.DATA_REFERENCIA)=? AND TC.COD_CONTRATO=?");
            preparedStatement.setInt(1, pMes);
            preparedStatement.setInt(2, pAno);
            preparedStatement.setInt(3, pCodContrato);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                vExisteCalculo = resultSet.getInt(1);
            }
            if(vExisteCalculo > 0) {
                /*--Deleta as retroatividades associadas aquele mês/ano.*/
                preparedStatement = connection.prepareStatement("DELETE FROM TB_RETROATIVIDADE_TOTAL_MENSAL WHERE COD_TOTAL_MENSAL_A_RETER IN (SELECT TMR.COD" +
                        " FROM TB_TOTAL_MENSAL_A_RETER TMR JOIN TB_TERCEIRIZADO_CONTRATO TC ON TC.COD=TMR.COD_TERCEIRIZADO_CONTRATO" +
                        " WHERE MONTH(TMR.DATA_REFERENCIA)=? AND YEAR(TMR.DATA_REFERENCIA)=? AND TC.COD_CONTRATO=?)");
                preparedStatement.setInt(1, pMes);
                preparedStatement.setInt(2, pAno);
                preparedStatement.setInt(3, pCodContrato);
                preparedStatement.executeUpdate();
                /*--Deleta os recolhimentos realizados naquele mês/ano.*/
                preparedStatement = connection.prepareStatement("DELETE FROM TB_TOTAL_MENSAL_A_RETER WHERE MONTH(DATA_REFERENCIA)=? AND YEAR(DATA_REFERENCIA)=?" +
                        " AND COD_TERCEIRIZADO_CONTRATO IN (SELECT TC.COD FROM TB_TERCEIRIZADO_CONTRATO TC WHERE TC.COD_CONTRATO=?)");
                preparedStatement.setInt(1, pMes);
                preparedStatement.setInt(2, pAno);
                preparedStatement.setInt(3, pCodContrato);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar as retroatividades ");
        }
        /*--Caso não haja mudaça de percentual no mês designado carregam-se os valores.*/

        if(!percentual.ExisteMudancaPercentual(pCodContrato, pMes, pAno, 1)) {
            /*--Definição dos percentuais.*/
            vPercentualFerias = percentual.RetornaPercentualContrato(pCodContrato, 1, pMes, pAno, 1,1);
            vPercentualTercoConstitucional = vPercentualFerias/3;
            vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(pCodContrato, 3, pMes, pAno, 1, 1);
            vPercentualIncidencia = (percentual.RetornaPercentualContrato(pCodContrato, 7, pMes, pAno, 1, 1) *
                    (vPercentualFerias + vPercentualDecimoTerceiro + vPercentualTercoConstitucional))/100;
            vPercentualIndenizacao = percentual.RetornaPercentualEstatico(pCodContrato, 4, pMes, pAno, 1, 1);
            vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 6, pMes, pAno, 1, 1);
            vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 5, pMes, pAno, 1, 1);
            vPercentualIndenizacao = (((vPercentualIndenizacao/100) *  (vPercentualPenalidadeFGTS/100) * (vPercentualMultaFGTS/100)) *
                    (1 + (vPercentualFerias/100) + (vPercentualDecimoTerceiro/100) + (vPercentualTercoConstitucional/100))) * 100;
        }
        // Busca funções do contrato
        ArrayList<Integer> c1 = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT COD FROM TB_FUNCAO_CONTRATO WHERE COD_CONTRATO=?");
            preparedStatement.setInt(1, pCodContrato);
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
             c1.add(resultSet.getInt("COD"));
            }
        }catch (SQLException sqle) {
            throw new NullPointerException("Erro ao tentar buscar as funções do contrato !");
        }
        // --Para cada função do contrato.
        Convencao convencao = new Convencao(connection);
        for(int i = 0; i < c1.size(); i++) {
            ArrayList<CodTerceirizadoECodFuncaoTerceirizadoModel> tuplas = selecionaTerceirizadosContratoFuncao(c1.get(i), vDataReferencia, pMes, pAno);
            // --Se não existe dupla convenção e duplo percentual.
            if(!convencao.ExisteDuplaConvencao(c1.get(i), pMes, pAno, 1) && !percentual.ExisteMudancaPercentual(pCodContrato, pMes, pAno, 1)) {
                vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(c1.get(i), pMes, pAno, 1, 1);
                if(vRemuneracao == 0) {
                    throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. CÓDICO: -20001");
                }
                // --Para cada funcionário que ocupa aquele função.
                for(int j= 0; j < tuplas.size(); j++) {
                    // Redefine todas as variáveis.
                    vTotal = 0;
                    vTotalFerias = 0;
                    vTotalTercoConstitucional = 0;
                    vTotalDecimoTerceiro = 0;
                    vTotalIncidencia = 0;
                    vTotalIndenizacao = 0;

                    // --Se a retenção for para período integral.

                    vTotalFerias = vRemuneracao * (vPercentualFerias/100);
                    vTotalTercoConstitucional = vRemuneracao * (vPercentualTercoConstitucional/100);
                    vTotalDecimoTerceiro = vRemuneracao * (vPercentualDecimoTerceiro/100);
                    vTotalIncidencia = vRemuneracao * (vPercentualIncidencia/100);
                    vTotalIndenizacao = vRemuneracao * (vPercentualIndenizacao/100);

                    // --No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo, situação similar para a retenção proporcional.
                    if(retencao.ExisteMudancaFuncao(tuplas.get(j).getCodTerceirizadoContrato(), pMes, pAno) && !retencao.FuncaoRetencaoIntegral(tuplas.get(j).getCod(), pMes, pAno)) {
                        vTotalFerias = (vTotalFerias/30) * periodo.DiasTrabalhadosMes(tuplas.get(j).getCod(), pMes, pAno);
                        vTotalTercoConstitucional = (vTotalTercoConstitucional/30) * periodo.DiasTrabalhadosMes(tuplas.get(j).getCod(), pMes, pAno);
                        vTotalDecimoTerceiro = (vTotalDecimoTerceiro/30) * periodo.DiasTrabalhadosMes(tuplas.get(j).getCod(), pMes, pAno);
                        vTotalIncidencia = (vTotalIncidencia/30) * periodo.DiasTrabalhadosMes(tuplas.get(j).getCod(), pMes, pAno);
                        vTotalIndenizacao = (vTotalIndenizacao/30) * periodo.DiasTrabalhadosMes(tuplas.get(j).getCod(), pMes, pAno);
                    }
                    vTotal = (vTotalFerias + vTotalTercoConstitucional + vTotalDecimoTerceiro + vTotalIncidencia + vTotalIndenizacao);
                    try {
                        preparedStatement = connection.prepareStatement("INSERT INTO TB_TOTAL_MENSAL_A_RETER (COD_TERCEIRIZADO_CONTRATO, COD_FUNCAO_TERCEIRIZADO, FERIAS, TERCO_CONSTITUCIONAL," +
                                " DECIMO_TERCEIRO, INCIDENCIA_SUBMODULO_4_1, MULTA_FGTS, TOTAL, DATA_REFERENCIA, LOGIN_ATUALIZACAO, DATA_ATUALIZACAO) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYSTEM', CURRENT_TIMESTAMP)");
                        preparedStatement.setInt(1, tuplas.get(j).getCodTerceirizadoContrato());
                        preparedStatement.setInt(2, tuplas.get(j).getCod());
                        preparedStatement.setFloat(3, vTotalFerias);
                        preparedStatement.setFloat(4, vTotalTercoConstitucional);
                        preparedStatement.setFloat(5, vTotalDecimoTerceiro);
                        preparedStatement.setFloat(6, vTotalIncidencia);
                        preparedStatement.setFloat(7, vTotalIndenizacao);
                        preparedStatement.setFloat(8, vTotal);
                        preparedStatement.setDate(9, vDataReferencia);
                        preparedStatement.executeUpdate();
                    } catch (SQLException e) {
                        throw new RuntimeException("Erro ao tentar inserir os resultados do cálculo de Total Mensal a Reter no banco de dados !");
                    }

                }

            }
            /*
            // --Se não existe dupla convenção e existe duplo percentual.
            if(!convencao.ExisteDuplaConvencao(c1[i], pMes, pAno, 1) && percentual.ExisteMudancaPercentual(pCodContrato, pMes, pAno, 1)) {
                // Define a remuneração da função
                vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(c1[i], pMes, pAno, 1 ,1);
                if(vRemuneracao == 0) {
                    throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada");
                }
                // --Para cada funcionário que ocupa aquele função.

                for(int j = 0; j > tuplas.size(); j++) {
                    // --Redefine todas as variáveis.
                    vTotal = 0;
                    vTotalFerias = 0;
                    vTotalTercoConstitucional = 0;
                    vTotalDecimoTerceiro = 0;
                    vTotalIncidencia = 0;
                    vTotalIndenizacao = 0;

                    // --Definição dos percentuais da primeira metade do mês
                    vPercentualFerias = percentual.RetornaPercentualContrato(pCodContrato, 1, pMes, pAno, 2 ,1);
                    vPercentualTercoConstitucional = vPercentualFerias/3;
                    vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(pCodContrato, 3, pMes, pAno, 2 ,1);
                    vPercentualIncidencia = (percentual.RetornaPercentualContrato(pCodContrato, 7, pMes, pAno, 2 ,1) *
                            (vPercentualFerias + vPercentualDecimoTerceiro + vPercentualTercoConstitucional))/100;
                    vPercentualIndenizacao = percentual.RetornaPercentualEstatico(pCodContrato, 4, pMes, pAno, 2, 1);
                    vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 6, pMes, pAno, 2, 1);
                    vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 5, pMes, pAno, 2, 1);
                    vPercentualIndenizacao = (((vPercentualIndenizacao/100) *  (vPercentualPenalidadeFGTS/100) * (vPercentualMultaFGTS/100)) *
                            (1 + (vPercentualFerias/100) + (vPercentualDecimoTerceiro/100) + (vPercentualTercoConstitucional/100))) * 100;

                    // --Se a retenção for para período integral.

                    if(retencao.FuncaoRetencaoIntegral(tuplas.get(j).getCod(), pMes, pAno)) {
                        // --Recolhimento referente a primeira metade do mês.
                        vTotalFerias = (((vRemuneracao * (vPercentualFerias/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 3));
                        vTotalTercoConstitucional = (((vRemuneracao * (vPercentualTercoConstitucional/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 3));
                        vTotalDecimoTerceiro = (((vRemuneracao * (vPercentualDecimoTerceiro/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 3));
                        vTotalIncidencia = (((vRemuneracao * (vPercentualIncidencia/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 3));
                        vTotalIndenizacao = (((vRemuneracao * (vPercentualIndenizacao/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 3));

                        // --Definição dos percentuais da segunda metade do mês.
                        vPercentualFerias = percentual.RetornaPercentualContrato(pCodContrato, 1, pMes, pAno, 1 ,1);
                        vPercentualTercoConstitucional = vPercentualFerias / 3;
                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(pCodContrato, 3, pMes, pAno, 1, 1);
                        vPercentualIncidencia = (percentual.RetornaPercentualContrato(pCodContrato, 7, pMes, pAno, 1, 1) *
                                (vPercentualMultaFGTS + vPercentualDecimoTerceiro + vPercentualTercoConstitucional)) / 100;
                        vPercentualIndenizacao = percentual.RetornaPercentualEstatico(pCodContrato, 4, pMes, pAno, 1 ,1);
                        vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 6, pMes, pAno, 1, 1);
                        vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 5, pMes, pAno, 1, 1);
                        vPercentualIndenizacao = (((vPercentualIndenizacao/100) *  (vPercentualPenalidadeFGTS/100) *
                                (vPercentualMultaFGTS/100)) * (1 + (vPercentualFerias/100) + (vPercentualDecimoTerceiro/100) + (vPercentualTercoConstitucional/100))) * 100;

                        // --Recolhimento referente a primeira metade do mês.

                        vTotalFerias = vTotalFerias + (((vRemuneracao * (vPercentualFerias/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 4));
                        vTotalTercoConstitucional = vTotalTercoConstitucional + (((vRemuneracao * (vPercentualTercoConstitucional/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 4));
                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + (((vRemuneracao * (vPercentualDecimoTerceiro/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 4));
                        vTotalIncidencia = vTotalIncidencia + (((vRemuneracao * (vPercentualIncidencia/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 4));
                        vTotalIndenizacao = vTotalIndenizacao + (((vRemuneracao * (vPercentualIndenizacao/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 4));

                    }

                    // --Caso o funcionário não tenha trabalhado 15 dias ou mais no período.

                    if(!retencao.FuncaoRetencaoIntegral(tuplas.get(j).getCod(), pMes, pAno)) {
                        vPercentualIndenizacao = percentual.RetornaPercentualEstatico(pCodContrato, 4, pMes, pAno, 2 ,1);
                        vPercentualIndenizacao = (((vPercentualIndenizacao/100) *  (vPercentualPenalidadeFGTS/100) * (vPercentualMultaFGTS/100))) * 100;
                        vTotalIndenizacao = (((vRemuneracao * (vPercentualIndenizacao/100))/30) * periodo.DiasTrabalhadosMesParcial(c1[i], tuplas.get(j).getCod(), pMes, pAno, 3));

                        //--Definição dos percentuais da segunda metade do mês.

                        vPercentualIndenizacao = percentual.RetornaPercentualEstatico(pCodContrato, 4, pMes, pAno, 1, 1);
                        vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 6, pMes, pAno, 1, 1);
                        vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(pCodContrato, 5, pMes, pAno, 1, 1);
                        vPercentualIndenizacao = (((vPercentualIndenizacao/100) *  (vPercentualPenalidadeFGTS/100) * (vPercentualMultaFGTS/100))) * 100;

                        vTotalIndenizacao = vTotalIndenizacao + (((vRemuneracao * (vPercentualIndenizacao/100))/30) * periodo.DiasTrabalhadosMesParcial(c1[i], tuplas.get(j).getCod(), pMes, pAno, 4));
                    }

                    vTotal = (vTotalFerias + vTotalTercoConstitucional + vTotalDecimoTerceiro + vTotalIncidencia + vTotalIndenizacao);
                    try {
                        preparedStatement = connection.prepareStatement("INSERT INTO TB_TOTAL_MENSAL_A_RETER(COD_TERCEIRIZADO_CONTRATO," +
                                "COD_FUNCAO_TERCEIRIZADO, FERIAS, TERCO_CONSTITUCIONAL, DECIMO_TERCEIRO, INCIDENCIA_SUBMODULO_4_1, MULTA_FGTS, TOTAL, DATA_REFERENCIA, LOGIN_ATUALIZACAO, DATA_ATUALIZACAO)" +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYSTEM', GETDATE())");
                        preparedStatement.setInt(1, tuplas.get(j).getCodTerceirizadoContrato());
                        preparedStatement.setInt(2, tuplas.get(j).getCod());
                        preparedStatement.setFloat(3, vTotalFerias);
                        preparedStatement.setFloat(4, vTotalTercoConstitucional);
                        preparedStatement.setFloat(5, vTotalDecimoTerceiro);
                        preparedStatement.setFloat(6, vTotalIncidencia);
                        preparedStatement.setFloat(7, vTotalIndenizacao);
                        preparedStatement.setFloat(8, vTotal);
                        preparedStatement.setDate(9, vDataReferencia);
                        preparedStatement.executeUpdate();
                    }catch (SQLException sqle) {
                        throw new RuntimeException("Erro ao tentar Inserir os valores de Total Mensal a Reter. Código do Contrato: " + pCodContrato +
                                ". Código da função do contrato: " + c1[i] + ". Código do Terceirizado no contrato: " + tuplas.get(j).getCodTerceirizadoContrato());
                    }
                }
            }
            // Se existe dupla convenção e não existe duplo percentual.
            if(convencao.ExisteDuplaConvencao(c1[i], pMes, pAno, 1) && percentual.ExisteMudancaPercentual(pCodContrato, pMes, pAno, 1)) {
                // --Define a remuneração do funcao

                vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(c1[i], pMes, pAno, 2 ,1);
                vRemuneracao2 = remuneracao.RetornaRemuneracaoPeriodo(c1[i], pMes, pAno, 1, 1);
                if(vRemuneracao == 0 || vRemuneracao2 == 0 ) {
                    throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. COD: -20001");
                }
                // --Para cada funcionário que ocupa aquele funcao.

                for(int j = 0;j > tuplas.size(); j++) {

                    // Redefine todas as variáveis.

                    vTotal = 0;
                    vTotalFerias = 0;
                    vTotalTercoConstitucional = 0;
                    vTotalDecimoTerceiro = 0;
                    vTotalIncidencia = 0;
                    vTotalIndenizacao = 0;
                    // Se a retenção for para período integral.
                    if(retencao.FuncaoRetencaoIntegral(tuplas.get(j).getCod(), pMes, pAno)) {
                        // --Retenção proporcional da primeira convenção.
                        vTotalFerias = ((vRemuneracao * (vPercentualFerias/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 1);
                        vTotalTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 1);
                        vTotalDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 1);
                        vTotalIncidencia = ((vRemuneracao * (vPercentualIncidencia/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 1);
                        vTotalIndenizacao = ((vRemuneracao * (vPercentualIndenizacao/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 1);

                        // Retenção proporcional da segunda convenção.
                        vTotalFerias = vTotalFerias + (((vRemuneracao2 * (vPercentualFerias/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 2));
                        vTotalTercoConstitucional = vTotalTercoConstitucional + (((vRemuneracao2 * (vPercentualTercoConstitucional/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 2));
                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + (((vRemuneracao2 * (vPercentualDecimoTerceiro/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 2));
                        vTotalIncidencia = vTotalIncidencia + (((vRemuneracao2 * (vPercentualIncidencia/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 2));
                        vTotalIndenizacao = vTotalIndenizacao + (((vRemuneracao2 * (vPercentualIndenizacao/100))/30) * periodo.RetornaNumeroDiasMesParcial(c1[i], pMes, pAno, 2));
                    }

                    //--Caso o funcionário não tenha trabalhado 15 dias ou mais no período.
                    if(retencao.FuncaoRetencaoIntegral(tuplas.get(j).getCod(), pMes, pAno)) {
                        vPercentualIndenizacao = percentual.RetornaPercentualEstatico(pCodContrato, 4, pMes, pAno, 1, 1);
                        vPercentualIndenizacao = (((vPercentualIndenizacao/100) *  (vPercentualPenalidadeFGTS/100) * (vPercentualMultaFGTS/100))) * 100;

                        // Retenção proporcional da primeira convenção.

                        vTotalIndenizacao = (((vRemuneracao * (vPercentualIndenizacao/100))/30) * periodo.DiasTrabalhadosMesParcial(c1[i], tuplas.get(j).getCod(), pMes, pAno, 1));

                        //--Retenção proporcional da segunda convenção.
                        vTotalIndenizacao = vTotalIndenizacao + (((vRemuneracao2 * (vPercentualIndenizacao/100))/30) *  periodo.DiasTrabalhadosMesParcial(c1[i], tuplas.get(j).getCod(), pMes, pAno, 2));
                    }
                    vTotal = (vTotalFerias + vTotalTercoConstitucional + vTotalDecimoTerceiro + vTotalIncidencia + vTotalIndenizacao);
                    try {
                        preparedStatement = connection.prepareStatement("INSERT INTO TB_TOTAL_MENSAL_A_RETER (COD_TERCEIRIZADO_CONTRATO, COD_FUNCAO_TERCEIRIZADO," +
                                "FERIAS, TERCO_CONSTITUCIONAL, DECIMO_TERCEIRO, INCIDENCIA_SUBMODULO_4_1, MULTA_FGTS, TOTAL, DATA_REFERENCIA, LOGIN_ATUALIZACAO, DATA_ATUALIZACAO) " +
                                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'SYSTEM', GETDATE())");
                        preparedStatement.setInt(1, tuplas.get(j).getCodTerceirizadoContrato());
                        preparedStatement.setInt(2, tuplas.get(j).getCod());
                        preparedStatement.setFloat(3, vTotalFerias);
                        preparedStatement.setFloat(4, vTotalTercoConstitucional);
                        preparedStatement.setFloat(5, vTotalDecimoTerceiro);
                        preparedStatement.setFloat(6, vTotalIncidencia);
                        preparedStatement.setFloat(7, vTotalIndenizacao);
                        preparedStatement.setFloat(8, vTotal);
                        preparedStatement.setDate(9, vDataReferencia);
                        preparedStatement.executeUpdate();
                    }catch (SQLException sqle) {
                        throw new NullPointerException("");
                    }
                }
            }
            // Se existe mudança de percentual e mudança de convenção.
            if(convencao.ExisteDuplaConvencao(c1[i], pMes, pAno, 1) && percentual.ExisteMudancaPercentual(pCodContrato, pMes, pAno,1)) {
                // Define a remuneração do funcao.
                vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(c1[i], pMes, pAno, 2, 1);
                vRemuneracao2 = remuneracao.RetornaRemuneracaoPeriodo(c1[i], pMes, pAno, 1, 1);

                // Definição das datas para os períodos da convenção e percentuais.
                try {
                    preparedStatement = connection.prepareStatement("SELECT DATA_FIM FROM TB_REMUNERACAO_FUN_CON WHERE COD_FUNCAO_CONTRATO=? AND DATA_ADITAMENTO IS NOT NULL" +
                            " AND MONTH(DATA_FIM)=? AND YEAR(DATA_FIM)=?");
                    preparedStatement.setInt(1, c1[i]);
                    preparedStatement.setInt(2, pMes);
                    preparedStatement.setInt(3, pAno);
                    resultSet = preparedStatement.executeQuery();
                    if(resultSet.next()) {
                        vDataFimConvencao = resultSet.getDate("DATA_FIM");
                    }
                }catch (SQLException sqle) {
                    throw new NullPointerException("");
                }
                // Observação: datas dos percentuais são todas iguais para um bloco.
                 // Para o percentual do contrato.

                if(percentual.MudancaPercentualContrato(pCodContrato, pMes, pAno, 1)) {
                    try {
                        preparedStatement = connection.prepareStatement("SELECT DISTINCT(DATA_FIM) FROM TB_PERCENTUAL_CONTRATO WHERE COD_CONTRATO = ? AND DATA_ADITAMENTO IS NOT NULL" +
                                " AND MONTH(DATA_FIM)=? AND YEAR(DATA_FIM)=?");
                        preparedStatement.setInt(1, pCodContrato);
                        preparedStatement.setInt(2, pMes);
                        preparedStatement.setInt(3, pAno);
                        resultSet = preparedStatement.executeQuery();
                        if(resultSet.next()) {
                            vDataFimPercentual = resultSet.getDate(1);
                        }
                    } catch (SQLException e) {
                        throw new NullPointerException("");
                    }
                }

                //--Para o percentual estático.
                */
        }
    }
    Date adaptaDataPara360(Date vDataFimMes) {
        if(vDataFimMes.toLocalDate().getDayOfMonth() == 31) {
            vDataFimMes = Date.valueOf(vDataFimMes.toLocalDate().minusDays(1));
        }
        if(vDataFimMes.toLocalDate().getDayOfMonth() == 28){
            vDataFimMes = Date.valueOf(vDataFimMes.toLocalDate().plusDays(2));
        }
        if(vDataFimMes.toLocalDate().getDayOfMonth() == 29) {
            vDataFimMes = Date.valueOf(vDataFimMes.toLocalDate().plusDays(1));
        }
        return vDataFimMes;
    }
    ArrayList<CodTerceirizadoECodFuncaoTerceirizadoModel> selecionaTerceirizadosContratoFuncao(int pCodFuncaoContrato, Date pDataReferencia, int pMes, int pAno) {
        // Busca funcionários do contrato na respectiva função c1[i]
        ArrayList<CodTerceirizadoECodFuncaoTerceirizadoModel> tuplas = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ft.cod_terceirizado_contrato, ft.cod FROM tb_funcao_terceirizado ft WHERE ft.cod_funcao_contrato = ?" +
                " AND ((ft.data_inicio <= ?) OR (MONTH(ft.data_inicio) = ?) AND YEAR(ft.data_inicio) = ?) AND ((ft.data_fim IS NULL) OR (ft.data_fim >= EOMONTH(?))" +
                " OR (MONTH(ft.data_fim) = ?) AND YEAR(ft.data_fim) = ?)")){
            preparedStatement.setInt(1, pCodFuncaoContrato);
            preparedStatement.setDate(2, pDataReferencia);
            preparedStatement.setInt(3, pMes);
            preparedStatement.setInt(4, pAno);
            preparedStatement.setDate(5, pDataReferencia);
            preparedStatement.setInt(6, pMes);
            preparedStatement.setInt(7, pAno);
            try(ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    CodTerceirizadoECodFuncaoTerceirizadoModel tupla = new CodTerceirizadoECodFuncaoTerceirizadoModel(resultSet.getInt("COD_TERCEIRIZADO_CONTRATO"), resultSet.getInt("COD"));
                    tuplas.add(tupla);
                }
            }
        }catch(SQLException slqe) {
            throw new NullPointerException("Não foram encontrardos funcionários para a função: " + pCodFuncaoContrato);
        }
        return tuplas;
    }
}
