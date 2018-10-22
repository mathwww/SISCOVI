package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.dao.sql.ConsultaTSQL;
import br.jus.stj.siscovi.dao.sql.DeleteTSQL;
import br.jus.stj.siscovi.dao.sql.InsertTSQL;
import br.jus.stj.siscovi.dao.sql.UpdateTSQL;
import br.jus.stj.siscovi.model.CodFuncaoContratoECodFuncaoTerceirizadoModel;
import br.jus.stj.siscovi.model.RegistroRestituicaoRescisao;
import br.jus.stj.siscovi.model.ValorRestituicaoRescisaoModel;

import java.sql.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RestituicaoRescisao {

    private Connection connection;

    public RestituicaoRescisao(Connection connection) {

        this.connection = connection;

    }

    /**
     * Método que calcula o total da rescisão a ser restituída para um
     * determinado período aquisitivo.
     *
     * @param pCodTerceirizadoContrato;
     * @param pDataDesligamento;
     */

    public ValorRestituicaoRescisaoModel CalculaRestituicaoRescisao (int pCodTerceirizadoContrato,
                                                                     Date pDataDesligamento) {

        Retencao retencao = new Retencao(connection);
        Percentual percentual = new Percentual(connection);
        Periodos periodo = new Periodos(connection);
        Remuneracao remuneracao = new Remuneracao(connection);
        Saldo saldo = new Saldo(connection);
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        /*Chaves primárias.*/

        int vCodContrato;

        /*Variáveis totalizadoras de valores.*/

        float vTotalFerias = 0;
        float vTotalTercoConstitucional = 0;
        float vTotalIncidenciaFerias = 0;
        float vTotalIncidenciaTerco = 0;
        float vTotalDecimoTerceiro = 0;
        float vTotalIncidenciaDecimoTerceiro = 0;
        float vTotalMultaFGTSRemuneracao = 0;
        float vTotalMultaFGTSFerias = 0;
        float vTotalMultaFGTSTerco = 0;
        float vTotalMultaFGTSDecimoTerceiro = 0;

        /*Variáveis de valores parciais.*/

        float vValorFerias;
        float vValorTercoConstitucional;
        float vValorIncidenciaFerias;
        float vValorIncidenciaTerco;
        float vValorDecimoTerceiro;
        float vValorIncidenciaDecimoTerceiro;
        float vValorMultaFGTSRemuneracao;
        float vValorMultaFGTSFerias;
        float vValorMultaFGTSTerco;
        float vValorMultaFGTSDecimoTerceiro;

        /*Variáveis de percentuais.*/

        float vPercentualFerias;
        float vPercentualTercoConstitucional;
        float vPercentualIncidencia;
        float vPercentualDecimoTerceiro;
        float vPercentualFGTS;
        float vPercentualMultaFGTS;
        float vPercentualPenalidadeFGTS;

        /*Variável de remuneração da função.*/

        float vRemuneracao;

        /*Variáveis de data.*/

        Date vDataDisponibilizacao;
        Date vDataReferencia;
        Date vDataInicio;
        Date vDataFim;
        int vAno;
        int vMes;

        /*Variáveis de controle.*/

        int vDiasSubperiodo;

        /*Checagem dos parâmetros passados.*/

        if (pDataDesligamento == null) {

            throw new NullPointerException("Erro na checagem dos parâmetros.");

        }

        /*Atribuiçao da data de disponibilização e do cod do contrato.*/

        vCodContrato = consulta.RetornaContratoTerceirizado(pCodTerceirizadoContrato);
        vDataDisponibilizacao = consulta.RetornaDataDisponibilizacaoTerceirizado(pCodTerceirizadoContrato);

        /*Define o valor das variáveis vMes e Vano de acordo com a adata de inínio do período aquisitivo.*/

        vMes = vDataDisponibilizacao.toLocalDate().getMonthValue();
        vAno = vDataDisponibilizacao.toLocalDate().getYear();

        /*Definição da data referência (sempre o primeiro dia do mês).*/

        vDataReferencia = Date.valueOf(vAno + "-" + vMes + "-" + "01");

        /*Início da contabilização de férias do período.*/

        do {

            /*Seleciona as funções que o terceirizado ocupou no mês avaliado.*/

            ArrayList<CodFuncaoContratoECodFuncaoTerceirizadoModel> tuplas = consulta.SelecionaFuncaoContratoEFuncaoTerceirizado(pCodTerceirizadoContrato, vDataReferencia);

            Convencao convencao = new Convencao(connection);

            /*Para cada função que o terceirizado ocupou no mês avaliado.*/

            for (CodFuncaoContratoECodFuncaoTerceirizadoModel tupla : tuplas) {

                /*Caso não exista mais de uma remuneração vigente no mês e não tenha havido alteração nos percentuais do contrato ou nos percentuais estáticos.*/

                if (!convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && !percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Define o valor da remuneração da função e dos percentuais do contrato.*/

                    vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tupla.getCodFuncaoContrato(), vMes, vAno, 1, 2);
                    vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vMes, vAno, 1, 2);
                    vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vMes, vAno, 1, 2);
                    vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vMes, vAno, 1, 2);
                    vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vMes, vAno, 1, 2);
                    vPercentualFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 4, vMes, vAno, 1, 2);
                    vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 6, vMes, vAno, 1, 2);
                    vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 5, vMes, vAno, 1, 2);

                    if (vRemuneracao == 0) {

                        throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                    }

                    /*Cálculo do valor integral correspondente ao mês avaliado.*/

                    vValorFerias = (vRemuneracao * (vPercentualFerias / 100));
                    vValorTercoConstitucional = (vRemuneracao * (vPercentualTercoConstitucional / 100));
                    vValorDecimoTerceiro = (vRemuneracao * (vPercentualDecimoTerceiro / 100));
                    vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                    vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));
                    vValorIncidenciaDecimoTerceiro = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));
                    vValorMultaFGTSFerias = (vValorFerias * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100)));
                    vValorMultaFGTSTerco = (vValorTercoConstitucional * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100)));
                    vValorMultaFGTSDecimoTerceiro = (vValorDecimoTerceiro * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100)));
                    vValorMultaFGTSRemuneracao = (vRemuneracao * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100)));

                    /*o caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                     situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                    if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                        vValorFerias = (vValorFerias / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorTercoConstitucional = (vValorTercoConstitucional / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorDecimoTerceiro = (vValorDecimoTerceiro / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorIncidenciaFerias = (vValorIncidenciaFerias / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorIncidenciaTerco = (vValorIncidenciaTerco / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorIncidenciaDecimoTerceiro = (vValorIncidenciaDecimoTerceiro / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorMultaFGTSFerias = (vValorMultaFGTSFerias / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorMultaFGTSTerco = (vValorMultaFGTSTerco / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorMultaFGTSDecimoTerceiro = (vValorMultaFGTSDecimoTerceiro / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorMultaFGTSRemuneracao = (vValorMultaFGTSRemuneracao / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);

                    }

                    /*Contabilização do valor calculado.*/

                    vTotalFerias = vTotalFerias + vValorFerias;
                    vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                    vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                    vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                    vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;
                    vTotalIncidenciaDecimoTerceiro = vTotalIncidenciaDecimoTerceiro + vValorIncidenciaDecimoTerceiro;
                    vTotalMultaFGTSRemuneracao = vTotalMultaFGTSRemuneracao + vValorMultaFGTSRemuneracao;
                    vTotalMultaFGTSFerias = vTotalMultaFGTSFerias + vValorMultaFGTSFerias;
                    vTotalMultaFGTSTerco = vTotalMultaFGTSTerco + vValorMultaFGTSTerco;
                    vTotalMultaFGTSDecimoTerceiro = vTotalMultaFGTSDecimoTerceiro + vValorMultaFGTSDecimoTerceiro;

                }

                /*Se existe apenas alteração de percentual no mês.*/

                if (!convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Define a remuneração do cargo, que não se altera no período.*/

                    vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tupla.getCodFuncaoContrato(), vMes, vAno, 1, 2);

                    if (vRemuneracao == 0) {

                        throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                    }

                    /*Definição da data de início como sendo a data referência (primeiro dia do mês).*/

                    vDataInicio = vDataReferencia;

                    /*Seleciona as datas que compõem os subperíodos gerados pelas alterações de percentual no mês.*/

                    List<Date> datas = consulta.RetornaSubperiodosMesPercentual(vCodContrato,
                            vMes,
                            vAno,
                            vDataReferencia);

                    /*Loop contendo das datas das alterações de percentuais que comporão os subperíodos.*/

                    for (Date data : datas) {

                        /*Definição da data fim do subperíodo.*/

                        vDataFim = data;

                        /*Definição dos dias contidos no subperíodo*/

                        vDiasSubperiodo = (int) ((ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate())) + 1);

                        if (vMes == 2) {

                            vDiasSubperiodo = periodo.AjusteDiasSubperiodoFevereiro(vDataReferencia, vDataFim, vDiasSubperiodo);

                        }

                        /*Definição dos percentuais do subperíodo.*/

                        vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vDataInicio, vDataFim, 2);
                        vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vDataInicio, vDataFim, 2);
                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);
                        vPercentualFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 4, vDataInicio, vDataFim, 2);
                        vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 6, vDataInicio, vDataFim, 2);
                        vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 5, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorFerias = ((vRemuneracao * (vPercentualFerias / 100)) / 30) * vDiasSubperiodo;
                        vValorTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional / 100)) / 30) * vDiasSubperiodo;
                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                        vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));
                        vValorIncidenciaDecimoTerceiro = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));
                        vValorMultaFGTSFerias = ((vValorFerias * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSTerco = ((vValorTercoConstitucional * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSDecimoTerceiro = ((vValorDecimoTerceiro * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSRemuneracao = ((vRemuneracao * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 30 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorFerias = (vValorFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorTercoConstitucional = (vValorTercoConstitucional / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaFerias = (vValorIncidenciaFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaTerco = (vValorIncidenciaTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaDecimoTerceiro = (vValorIncidenciaDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSFerias = (vValorMultaFGTSFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSTerco = (vValorMultaFGTSTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSDecimoTerceiro = (vValorMultaFGTSDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSRemuneracao = (vValorMultaFGTSRemuneracao / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalFerias = vTotalFerias + vValorFerias;
                        vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                        vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;
                        vTotalIncidenciaDecimoTerceiro = vTotalIncidenciaDecimoTerceiro + vValorIncidenciaDecimoTerceiro;
                        vTotalMultaFGTSRemuneracao = vTotalMultaFGTSRemuneracao + vValorMultaFGTSRemuneracao;
                        vTotalMultaFGTSFerias = vTotalMultaFGTSFerias + vValorMultaFGTSFerias;
                        vTotalMultaFGTSTerco = vTotalMultaFGTSTerco + vValorMultaFGTSTerco;
                        vTotalMultaFGTSDecimoTerceiro = vTotalMultaFGTSDecimoTerceiro + vValorMultaFGTSDecimoTerceiro;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração de remuneração apenas.*/

                if (convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && !percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Definição dos percentuais, que não se alteram no período.*/

                    vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vMes, vAno, 1, 2);
                    vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vMes, vAno, 1, 2);
                    vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vMes, vAno, 1, 2);
                    vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vMes, vAno, 1, 2);
                    vPercentualFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 4, vMes, vAno, 1, 2);
                    vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 6, vMes, vAno, 1, 2);
                    vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 5, vMes, vAno, 1, 2);

                    /*Definição da data de início como sendo a data referência (primeiro dia do mês).*/

                    vDataInicio = vDataReferencia;

                    /*Seleciona as datas que compõem os subperíodos gerados pelas alterações de remuneração no mês.*/

                    List<Date> datas = consulta.RetornaSubperiodosMesRemuneracao(vCodContrato,
                            vMes,
                            vAno,
                            tupla.getCodFuncaoContrato(),
                            vDataReferencia);

                    /*Loop contendo das datas das alterações de remuneração que comporão os subperíodos.*/

                    for (Date data : datas) {

                        /*Definição da data fim do subperíodo.*/

                        vDataFim = data;

                        /*Definição dos dias contidos no subperíodo*/

                        vDiasSubperiodo = (int) ((ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate())) + 1);

                        if (vMes == 2) {

                            vDiasSubperiodo = periodo.AjusteDiasSubperiodoFevereiro(vDataReferencia, vDataFim, vDiasSubperiodo);

                        }

                        /*Define a remuneração do cargo, que não se altera no período.*/

                        vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tupla.getCodFuncaoContrato(), vDataInicio, vDataFim, 2);

                        if (vRemuneracao == 0) {

                            throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                        }

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorFerias = ((vRemuneracao * (vPercentualFerias / 100)) / 30) * vDiasSubperiodo;
                        vValorTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional / 100)) / 30) * vDiasSubperiodo;
                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                        vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));
                        vValorIncidenciaDecimoTerceiro = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));
                        vValorMultaFGTSFerias = ((vValorFerias * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSTerco = ((vValorTercoConstitucional * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSDecimoTerceiro = ((vValorDecimoTerceiro * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSRemuneracao = ((vRemuneracao * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorFerias = (vValorFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorTercoConstitucional = (vValorTercoConstitucional / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaFerias = (vValorIncidenciaFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaTerco = (vValorIncidenciaTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaDecimoTerceiro = (vValorIncidenciaDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSFerias = (vValorMultaFGTSFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSTerco = (vValorMultaFGTSTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSDecimoTerceiro = (vValorMultaFGTSDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSRemuneracao = (vValorMultaFGTSRemuneracao / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalFerias = vTotalFerias + vValorFerias;
                        vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                        vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;
                        vTotalIncidenciaDecimoTerceiro = vTotalIncidenciaDecimoTerceiro + vValorIncidenciaDecimoTerceiro;
                        vTotalMultaFGTSRemuneracao = vTotalMultaFGTSRemuneracao + vValorMultaFGTSRemuneracao;
                        vTotalMultaFGTSFerias = vTotalMultaFGTSFerias + vValorMultaFGTSFerias;
                        vTotalMultaFGTSTerco = vTotalMultaFGTSTerco + vValorMultaFGTSTerco;
                        vTotalMultaFGTSDecimoTerceiro = vTotalMultaFGTSDecimoTerceiro + vValorMultaFGTSDecimoTerceiro;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração na remuneração e nos percentuais.*/

                if (convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Definição da data de início como sendo a data referência (primeiro dia do mês).*/

                    vDataInicio = vDataReferencia;

                    /*Seleciona as datas que compõem os subperíodos gerados pelas alterações de percentual e remuneração no mês.*/

                    List<Date> datas = consulta.RetornaSubperiodosMesPercentualRemuneracao(vCodContrato,
                            vMes,
                            vAno,
                            tupla.getCodFuncaoContrato(),
                            vDataReferencia);

                    /*Loop contendo das datas das alterações de percentual e remuneração que comporão os subperíodos.*/

                    for (Date data : datas) {

                        /*Definição da data fim do subperíodo.*/

                        vDataFim = data;

                        /*Definição dos dias contidos no subperíodo*/

                        vDiasSubperiodo = (int) ((ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate())) + 1);

                        if (vMes == 2) {

                            vDiasSubperiodo = periodo.AjusteDiasSubperiodoFevereiro(vDataReferencia, vDataFim, vDiasSubperiodo);

                        }

                        /*Define a remuneração do cargo, que não se altera no período.*/

                        vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tupla.getCodFuncaoContrato(), vDataInicio, vDataFim, 2);

                        if (vRemuneracao == 0) {

                            throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                        }

                        /*Definição dos percentuais do subperíodo.*/

                        vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vDataInicio, vDataFim, 2);
                        vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vDataInicio, vDataFim, 2);
                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);
                        vPercentualFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 4, vDataInicio, vDataFim, 2);
                        vPercentualPenalidadeFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 6, vDataInicio, vDataFim, 2);
                        vPercentualMultaFGTS = percentual.RetornaPercentualEstatico(vCodContrato, 5, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorFerias = ((vRemuneracao * (vPercentualFerias / 100)) / 30) * vDiasSubperiodo;
                        vValorTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional / 100)) / 30) * vDiasSubperiodo;
                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                        vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));
                        vValorIncidenciaDecimoTerceiro = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));
                        vValorMultaFGTSFerias = ((vValorFerias * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSTerco = ((vValorTercoConstitucional * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSDecimoTerceiro = ((vValorDecimoTerceiro * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;
                        vValorMultaFGTSRemuneracao = ((vRemuneracao * ((vPercentualFGTS / 100) * (vPercentualMultaFGTS / 100) * (vPercentualPenalidadeFGTS / 100))) / 30) * vDiasSubperiodo;

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorFerias = (vValorFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorTercoConstitucional = (vValorTercoConstitucional / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaFerias = (vValorIncidenciaFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaTerco = (vValorIncidenciaTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaDecimoTerceiro = (vValorIncidenciaDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSFerias = (vValorMultaFGTSFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSTerco = (vValorMultaFGTSTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSDecimoTerceiro = (vValorMultaFGTSDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorMultaFGTSRemuneracao = (vValorMultaFGTSRemuneracao / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalFerias = vTotalFerias + vValorFerias;
                        vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                        vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;
                        vTotalIncidenciaDecimoTerceiro = vTotalIncidenciaDecimoTerceiro + vValorIncidenciaDecimoTerceiro;
                        vTotalMultaFGTSRemuneracao = vTotalMultaFGTSRemuneracao + vValorMultaFGTSRemuneracao;
                        vTotalMultaFGTSFerias = vTotalMultaFGTSFerias + vValorMultaFGTSFerias;
                        vTotalMultaFGTSTerco = vTotalMultaFGTSTerco + vValorMultaFGTSTerco;
                        vTotalMultaFGTSDecimoTerceiro = vTotalMultaFGTSDecimoTerceiro + vValorMultaFGTSDecimoTerceiro;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }


                }

            }

            /*Contabilização do valor final (valor calculado menos restituições).*/

            if (vMes == 12 || (vMes == pDataDesligamento.toLocalDate().getMonthValue()) && vAno == pDataDesligamento.toLocalDate().getYear()) {

                vTotalFerias = (vTotalFerias - saldo.SaldoContaVinculada(pCodTerceirizadoContrato, vAno, 2, 1));
                vTotalTercoConstitucional = (vTotalTercoConstitucional - saldo.SaldoContaVinculada(pCodTerceirizadoContrato, vAno, 2, 2));
                vTotalDecimoTerceiro = vTotalDecimoTerceiro - saldo.SaldoContaVinculada(pCodTerceirizadoContrato, vAno, 3, 3);
                vTotalIncidenciaDecimoTerceiro = vTotalIncidenciaDecimoTerceiro - saldo.SaldoContaVinculada(pCodTerceirizadoContrato, vAno, 3, 103);
                vTotalIncidenciaFerias = (vTotalIncidenciaFerias - saldo.SaldoContaVinculada(pCodTerceirizadoContrato, vAno, 2, 101));
                vTotalIncidenciaTerco = (vTotalIncidenciaTerco - saldo.SaldoContaVinculada(pCodTerceirizadoContrato, vAno, 2, 102));

            }

            if (vMes != 12) {

                vMes = vMes + 1;
            } else {

                vMes = 1;
                vAno = vAno + 1;

            }

            vDataReferencia = Date.valueOf(vAno + "-" + vMes + "-" + "01");

        } while (vDataReferencia.before(pDataDesligamento) || vDataReferencia.equals(pDataDesligamento));

//        System.out.println(vTotalFerias);
        //       System.out.println(vTotalTercoConstitucional);
        //      System.out.println(vTotalIncidenciaFerias);
        //     System.out.println(vTotalIncidenciaTerco);

        return new ValorRestituicaoRescisaoModel(vTotalDecimoTerceiro,
                vTotalIncidenciaDecimoTerceiro,
                vTotalMultaFGTSDecimoTerceiro,
                vTotalFerias,
                vTotalTercoConstitucional,
                vTotalIncidenciaFerias,
                vTotalIncidenciaTerco,
                vTotalMultaFGTSFerias,
                vTotalMultaFGTSTerco,
                vTotalMultaFGTSRemuneracao);

    }

    /**
     * Método que registra o calculo do total da rescisão a ser restituído para um
     * determinado empregado.
     *
     * @param pCodTerceirizadoContrato;
     * @param pTipoRestituicao;
     * @param pTipoRescisao;
     * @param pDataDesligamento;
     * @param pValorDecimoTerceiro;
     * @param pValorIncidenciaDecimoTerceiro;
     * @param pValorFGTSDecimoTerceiro;
     * @param pValorFerias;
     * @param pValorTerco;
     * @param pValorIncidenciaFerias;
     * @param pValorIncidenciaTerco;
     * @param pValorFGTSFerias;
     * @param pValorFGTSTerco;
     * @param pValorFGTSSalario;
     * @param pLoginAtualizacao;
     */

    public Integer RegistrarRestituicaoRescisao (int pCodTerceirizadoContrato,
                                                 String pTipoRestituicao,
                                                 String pTipoRescisao,
                                                 Date pDataDesligamento,
                                                 Date pDataInicioFerias,
                                                 float pValorDecimoTerceiro,
                                                 float pValorIncidenciaDecimoTerceiro,
                                                 float pValorFGTSDecimoTerceiro,
                                                 float pValorFerias,
                                                 float pValorTerco,
                                                 float pValorIncidenciaFerias,
                                                 float pValorIncidenciaTerco,
                                                 float pValorFGTSFerias,
                                                 float pValorFGTSTerco,
                                                 float pValorFGTSSalario,
                                                 String pLoginAtualizacao) {

        ConsultaTSQL consulta = new ConsultaTSQL(connection);
        InsertTSQL insert = new InsertTSQL(connection);

         /*Chaves Primárias*/

        int vCodTbRestituicaoRescisao;
        int vCodTipoRestituicao;
        int vCodTipoRescisao;

         /*Variáveis de controle do saldo reidual.*/

        float vIncidDecTer = 0;
        float vFGTSDecimoTerceiro = 0;
        float vIncidFerias = 0;
        float vIncidTerco = 0;
        float vFGTSFerias = 0;
        float vFGTSTerco = 0;
        float vFGTSRemuneracao = 0;

         /*Atribuição do cod do tipo de restituição.*/

        vCodTipoRestituicao = consulta.RetornaCodTipoRestituicao(pTipoRestituicao);

         /*Atribuição do cod do tipo de rescisão.*/

        vCodTipoRescisao = consulta.RetornaCodTipoRescisao(pTipoRescisao);

        /*Provisionamento da incidência para o saldo residual no caso de movimentação.*/

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            vIncidDecTer = pValorIncidenciaDecimoTerceiro;
            vIncidFerias = pValorIncidenciaFerias;
            vIncidTerco = pValorIncidenciaTerco;
            vFGTSDecimoTerceiro = pValorFGTSDecimoTerceiro;
            vFGTSFerias = pValorFGTSFerias;
            vFGTSTerco = pValorFGTSTerco;
            vFGTSRemuneracao = pValorFGTSSalario;

            pValorIncidenciaDecimoTerceiro = 0;
            pValorIncidenciaFerias = 0;
            pValorIncidenciaTerco = 0;
            pValorFGTSDecimoTerceiro = 0;
            pValorFGTSFerias = 0;
            pValorFGTSTerco = 0;
            pValorFGTSSalario = 0;

        }

        /*Gravação no banco*/

        vCodTbRestituicaoRescisao = insert.InsertRestituicaoRescisao(pCodTerceirizadoContrato,
                vCodTipoRestituicao,
                vCodTipoRescisao,
                pDataDesligamento,
                pDataInicioFerias,
                pValorDecimoTerceiro,
                pValorIncidenciaDecimoTerceiro,
                pValorFGTSDecimoTerceiro,
                pValorFerias,
                pValorTerco,
                pValorIncidenciaFerias,
                pValorIncidenciaTerco,
                pValorFGTSFerias,
                pValorFGTSTerco,
                pValorFGTSSalario,
                pLoginAtualizacao);

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            insert.InsertSaldoResidualRescisao(vCodTbRestituicaoRescisao,
                    0,
                    vIncidDecTer,
                    vFGTSDecimoTerceiro,
                    0,
                    0,
                    vIncidFerias,
                    vIncidTerco,
                    vFGTSFerias,
                    vFGTSTerco,
                    vFGTSRemuneracao,
                    pLoginAtualizacao);

        }

        return vCodTbRestituicaoRescisao;

    }

    public void RecalculoRestituicaoRescisao (int pCodRestituicaoRescisao,
                                              String pTipoRestituicao,
                                              String pTipoRescisao,
                                              Date pDataDesligamento,
                                              Date pDataInicioFerias,
                                              float pValorDecimoTerceiro,
                                              float pValorIncidenciaDecimoTerceiro,
                                              float pValorFGTSDecimoTerceiro,
                                              float pValorFerias,
                                              float pValorTerco,
                                              float pValorIncidenciaFerias,
                                              float pValorIncidenciaTerco,
                                              float pValorFGTSFerias,
                                              float pValorFGTSTerco,
                                              float pValorFGTSSalario,
                                              String pLoginAtualizacao) {

        int vRetornoChavePrimaria;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);
        InsertTSQL insert = new InsertTSQL(connection);
        UpdateTSQL update = new UpdateTSQL(connection);
        DeleteTSQL delete = new DeleteTSQL(connection);

        int vCodTipoRestituicao = consulta.RetornaCodTipoRestituicao(pTipoRestituicao);
        int vCodTipoRescisao = consulta.RetornaCodTipoRescisao(pTipoRescisao);

        RegistroRestituicaoRescisao registro = consulta.RetornaRegistroRestituicaoRescisao(pCodRestituicaoRescisao);

        if (registro == null) {

            throw new NullPointerException("Registro anterior não encontrado.");

        }

        vRetornoChavePrimaria = insert.InsertHistoricoRestituicaoRescisao(registro.getpCod(),
                registro.getpCodTipoRestituicao(),
                registro.getpCodTipoRescisao(),
                registro.getpDataDesligamento(),
                registro.getpDataInicioFerias(),
                registro.getpValorDecimoTerceiro(),
                registro.getpIncidSubmod41DecTerceiro(),
                registro.getpIncidMultaFGTSDecTeceriro(),
                registro.getpValorFerias(),
                registro.getpValorTerco(),
                registro.getpIncidSubmod41Ferias(),
                registro.getpIncidSubmod41Terco(),
                registro.getpIncidMultaFGTSFerias(),
                registro.getpIncidMultaFGTSTerco(),
                registro.getpMultaFGTSSalario(),
                registro.getpDataReferencia(),
                registro.getpAutorizado(),
                registro.getpRestituido(),
                registro.getpObservacao(),
                registro.getpLoginAtualizacao());

        delete.DeleteSaldoResidualRescisao(pCodRestituicaoRescisao);

        /*Variáveis de controle do saldo reidual.*/

        float vIncidDecTer = 0;
        float vFGTSDecimoTerceiro = 0;
        float vIncidFerias = 0;
        float vIncidTerco = 0;
        float vFGTSFerias = 0;
        float vFGTSTerco = 0;
        float vFGTSRemuneracao = 0;

        /*Provisionamento da incidência para o saldo residual no caso de movimentação.*/

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            vIncidDecTer = pValorIncidenciaDecimoTerceiro;
            vIncidFerias = pValorIncidenciaFerias;
            vIncidTerco = pValorIncidenciaTerco;
            vFGTSDecimoTerceiro = pValorFGTSDecimoTerceiro;
            vFGTSFerias = pValorFGTSFerias;
            vFGTSTerco = pValorFGTSTerco;
            vFGTSRemuneracao = pValorFGTSSalario;

            pValorIncidenciaDecimoTerceiro = 0;
            pValorIncidenciaFerias = 0;
            pValorIncidenciaTerco = 0;
            pValorFGTSDecimoTerceiro = 0;
            pValorFGTSFerias = 0;
            pValorFGTSTerco = 0;
            pValorFGTSSalario = 0;

        }

        update.UpdateRestituicaoRescisao(pCodRestituicaoRescisao,
                vCodTipoRestituicao,
                vCodTipoRescisao,
                pDataDesligamento,
                pDataInicioFerias,
                pValorDecimoTerceiro,
                pValorIncidenciaDecimoTerceiro,
                pValorFGTSDecimoTerceiro,
                pValorFerias,
                pValorTerco,
                pValorIncidenciaFerias,
                pValorIncidenciaTerco,
                pValorFGTSFerias,
                pValorFGTSTerco,
                pValorFGTSSalario,
                "",
                "",
                "",
                pLoginAtualizacao);

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            insert.InsertSaldoResidualRescisao(pCodRestituicaoRescisao,
                    0,
                    vIncidDecTer,
                    vFGTSDecimoTerceiro,
                    0,
                    0,
                    vIncidFerias,
                    vIncidTerco,
                    vFGTSFerias,
                    vFGTSTerco,
                    vFGTSRemuneracao,
                    pLoginAtualizacao);

        }

    }

}