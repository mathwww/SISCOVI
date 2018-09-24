package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.model.CodFuncaoContratoECodFuncaoTerceirizadoModel;
import br.jus.stj.siscovi.model.ValorRestituicaoFeriasModel;
import br.jus.stj.siscovi.dao.sql.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


public class RestituicaoFerias {

    private Connection connection;

    public RestituicaoFerias(Connection connection) {

        this.connection = connection;

    }

    /**
     * Método que calcula o total de férias a ser restituído para um
     * determinado período aquisitivo.
     *
     * @param pCodTerceirizadoContrato;
     * @param pDiasVendidos;
     * @param pInicioFerias;
     * @param pFimFerias;
     * @param pInicioPeriodoAquisitivo;
     * @param pFimPeriodoAquisitivo;
     */

    public ValorRestituicaoFeriasModel CalculaRestituicaoFerias (int pCodTerceirizadoContrato,
                                                                 int pDiasVendidos,
                                                                 Date pInicioFerias,
                                                                 Date pFimFerias,
                                                                 Date pInicioPeriodoAquisitivo,
                                                                 Date pFimPeriodoAquisitivo) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;
        Retencao retencao = new Retencao(connection);
        Percentual percentual = new Percentual(connection);
        Periodos periodo = new Periodos(connection);
        Remuneracao remuneracao = new Remuneracao(connection);
        Ferias ferias = new Ferias(connection);
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        /*Chaves primárias.*/

        int vCodContrato = 0;

        /*Variáveis totalizadoras de valores.*/

        float vTotalFerias = 0;
        float vTotalTercoConstitucional = 0;
        float vTotalIncidenciaFerias = 0;
        float vTotalIncidenciaTerco = 0;

        /*Variáveis de valores parciais.*/

        float vValorFerias;
        float vValorTercoConstitucional;
        float vValorIncidenciaFerias;
        float vValorIncidenciaTerco;

        /*Variáveis de percentuais.*/

        float vPercentualFerias;
        float vPercentualTercoConstitucional;
        float vPercentualIncidencia;

        /*Variável de remuneração da função.*/

        float vRemuneracao;

        /*Variáveis de data.*/

        Date vDataReferencia;
        Date vDataInicio;
        Date vDataFim;
        int vAno;
        int vMes;

        /*Variável para a checagem de existência do terceirizado.*/

        int vCheck = 0;

        /*Variáveis de controle.*/

        int vDiasSubperiodo;
        float vDiasDeFerias;
        float vDiasAdquiridos;
        int vDiasVendidos;
        boolean vFeriasMenorDeAno = false;

        /*Checagem dos parâmetros passados.*/

        if (pInicioFerias == null || pFimFerias == null || pInicioPeriodoAquisitivo == null || pFimPeriodoAquisitivo == null) {

            throw new NullPointerException("Erro na checagem dos parâmetros.");

        }

        /*Verificação do período aquisitivo menor que 1 ano.*/

        if (pInicioFerias.before(pFimPeriodoAquisitivo) && !ferias.ExisteFeriasTerceirizado(pCodTerceirizadoContrato)) {

            pFimPeriodoAquisitivo = Date.valueOf(pInicioFerias.toLocalDate().plusDays(-1));
            vFeriasMenorDeAno = true;

        } else {

            if (pInicioFerias.before(pFimPeriodoAquisitivo) && ferias.ExisteFeriasTerceirizado(pCodTerceirizadoContrato)) {

                throw new NullPointerException("Período de usufruto informado é inconsistente (dentro do período aquisitivo).");

            }

        }

        /*Checagem da existência do terceirizado no contrato.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT COUNT(COD) FROM TB_TERCEIRIZADO_CONTRATO WHERE COD = ?");

            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCheck = resultSet.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        if (vCheck == 0) {

            throw new NullPointerException("Terceirizado não encontrado no contrato.");

        }

        /*Carrega o código do contrato.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT tc.cod_contrato FROM tb_terceirizado_contrato tc WHERE tc.cod=?");

            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodContrato = resultSet.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        /*Define o valor das variáveis vMes e Vano de acordo com a adata de inínio do período aquisitivo.*/

        vMes = pInicioPeriodoAquisitivo.toLocalDate().getMonthValue();
        vAno = pInicioPeriodoAquisitivo.toLocalDate().getYear();

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
                    vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vMes, vAno, 1, 2);

                    if (vRemuneracao == 0) {

                        throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                    }

                    /*Cálculo do valor integral correspondente ao mês avaliado.*/

                    vValorFerias = (vRemuneracao * (vPercentualFerias / 100));
                    vValorTercoConstitucional = (vRemuneracao * (vPercentualTercoConstitucional / 100));
                    vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                    vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));

                    /*o caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                    situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                    if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                        vValorFerias = (vValorFerias / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorTercoConstitucional = (vValorTercoConstitucional / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorIncidenciaFerias = (vValorIncidenciaFerias / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorIncidenciaTerco = (vValorIncidenciaTerco / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);

                    }

                    /*Contabilização do valor calculado.*/

                    vTotalFerias = vTotalFerias + vValorFerias;
                    vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                    vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                    vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;

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

                    /*Loop contendo das datas das alterações de percentuais que comporão os subperíodos.*/

                    List<Date> datas = new ArrayList<>();

                    /*Seleciona as datas que compõem os subperíodos gerados pelas alterações de percentual no mês.*/

                    try {

                        preparedStatement = connection.prepareStatement("SELECT data_inicio AS data" + " FROM tb_percentual_contrato" + " WHERE cod_contrato = ?" + " AND (MONTH(DATA_INICIO) = ?" + " AND \n" + " YEAR(DATA_INICIO) = ?)" + " UNION" + " SELECT data_fim AS data" + " FROM tb_percentual_contrato" + " WHERE cod_contrato = ?" + " AND (MONTH(DATA_FIM)=?" + " AND" + " YEAR(DATA_FIM) = ?)" + " UNION" + " SELECT data_inicio AS data" + " FROM tb_percentual_estatico" + " WHERE (MONTH(DATA_INICIO)=?" + " AND " + " YEAR(DATA_INICIO)=?)" + " UNION" + " SELECT data_fim AS data" + " FROM tb_percentual_estatico" + " WHERE (MONTH(DATA_FIM)=?" + " AND" + " YEAR(DATA_FIM)=?)" + " UNION" + " SELECT CASE WHEN ? = 2 THEN" + " EOMONTH(CONVERT(DATE, CONCAT('28/' , ? , '/' ,?), 103))" + " ELSE" + " CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103) END AS data" + " EXCEPT" + " SELECT CASE WHEN DAY(EOMONTH(CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103))) = 31 THEN" + " CONVERT(DATE, CONCAT('31/' , ? , '/' ,?), 103)" + " ELSE" + " NULL END AS data" + " ORDER BY data ASC");

                        preparedStatement.setInt(1, vCodContrato);
                        preparedStatement.setInt(2, vMes);
                        preparedStatement.setInt(3, vAno);
                        preparedStatement.setInt(4, vCodContrato);
                        preparedStatement.setInt(5, vMes);
                        preparedStatement.setInt(6, vAno);
                        preparedStatement.setInt(7, vMes);
                        preparedStatement.setInt(8, vAno);
                        preparedStatement.setInt(9, vMes);
                        preparedStatement.setInt(10, vAno);
                        preparedStatement.setInt(11, vMes);
                        preparedStatement.setInt(12, vMes);
                        preparedStatement.setInt(13, vAno);
                        preparedStatement.setInt(14, vMes);
                        preparedStatement.setInt(15, vAno);
                        preparedStatement.setInt(16, vMes);
                        preparedStatement.setInt(17, vAno);
                        preparedStatement.setInt(18, vMes);
                        preparedStatement.setInt(19, vAno);
                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {

                            datas.add(resultSet.getDate("data"));

                        }

                    } catch (SQLException e) {

                        throw new NullPointerException("Erro ao tentar carregar as datas referentes ao percentuais. " + " Contrato: " + vCodContrato + ". No perídodo: " + vDataReferencia.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

                    }

                    for (Date data : datas) {

                        /*Definição da data fim do subperíodo.*/

                        vDataFim = data;

                        /*Definição dos dias contidos no subperíodo*/

                        vDiasSubperiodo = (int) ((ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate())) + 1);

                        if (vMes == 2) {

                            if (vDataFim.toLocalDate().getDayOfMonth() == Date.valueOf(vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth())).toLocalDate().getDayOfMonth()) {

                                if (vDataFim.toLocalDate().getDayOfMonth() == 28) {

                                    vDiasSubperiodo = vDiasSubperiodo + 2;

                                } else {

                                    vDiasSubperiodo = vDiasSubperiodo + 1;

                                }

                            }

                        }

                        /*Definição dos percentuais do subperíodo.*/

                        vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vDataInicio, vDataFim, 2);
                        vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorFerias = ((vRemuneracao * (vPercentualFerias / 100)) / 30) * vDiasSubperiodo;
                        vValorTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                        vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 30 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorFerias = (vValorFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorTercoConstitucional = (vValorTercoConstitucional / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaFerias = (vValorIncidenciaFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaTerco = (vValorIncidenciaTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalFerias = vTotalFerias + vValorFerias;
                        vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                        vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                        vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração de remuneração apenas.*/

                if (convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && !percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Definição dos percentuais, que não se alteram no período.*/

                    vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vMes, vAno, 1, 2);
                    vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vMes, vAno, 1, 2);
                    vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vMes, vAno, 1, 2);

                    /*Definição da data de início como sendo a data referência (primeiro dia do mês).*/

                    vDataInicio = vDataReferencia;

                    /*Loop contendo das datas das alterações de percentuais que comporão os subperíodos.*/

                    /*Seleciona as datas que compõem os subperíodos gerados pelas alterações de percentual no mês.*/

                    List<Date> datas = new ArrayList<>();

                    try {

                        preparedStatement = connection.prepareStatement("SELECT rfc.data_inicio AS data" + " FROM tb_remuneracao_fun_con rfc\n" + " JOIN tb_funcao_contrato fc ON fc.cod = rfc.cod_funcao_contrato" + " WHERE fc.cod_contrato = ?" + " AND fc.cod = ?" + " AND (MONTH(rfc.data_inicio) = ?" + " AND" + " YEAR(rfc.data_inicio) = ?)" + " UNION" + " SELECT rfc.data_fim AS data " + " FROM tb_remuneracao_fun_con rfc" + " JOIN tb_funcao_contrato fc ON fc.cod = rfc.cod_funcao_contrato" + " WHERE fc.cod_contrato = ?" + " AND fc.cod = ?" + " AND (MONTH(rfc.data_fim) = ?" + " AND " + " YEAR(rfc.data_fim) = ?)" + " UNION" + " SELECT CASE WHEN ? = 2 THEN" + " EOMONTH(CONVERT(DATE, CONCAT('28/' , ? , '/' ,?), 103))" + " ELSE" + " CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103) END AS data" + " EXCEPT" + " SELECT CASE WHEN DAY(EOMONTH(CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103))) = 31 THEN" + " CONVERT(DATE, CONCAT('31/' , ? , '/' ,?), 103)" + " ELSE" + " NULL END AS data" + " ORDER BY DATA ASC");

                        preparedStatement.setInt(1, vCodContrato);
                        preparedStatement.setInt(2, tupla.getCodFuncaoContrato());
                        preparedStatement.setInt(3, vMes);
                        preparedStatement.setInt(4, vAno);
                        preparedStatement.setInt(5, vCodContrato);
                        preparedStatement.setInt(6, tupla.getCodFuncaoContrato());
                        preparedStatement.setInt(7, vMes);
                        preparedStatement.setInt(8, vAno);
                        preparedStatement.setInt(9, vMes);
                        preparedStatement.setInt(10, vMes);
                        preparedStatement.setInt(11, vAno);
                        preparedStatement.setInt(12, vMes);
                        preparedStatement.setInt(13, vAno);
                        preparedStatement.setInt(14, vMes);
                        preparedStatement.setInt(15, vAno);
                        preparedStatement.setInt(16, vMes);
                        preparedStatement.setInt(17, vAno);
                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {

                            datas.add(resultSet.getDate("data"));

                        }
                    } catch (SQLException e) {

                        throw new NullPointerException("Não foi possível determinar os subperíodos do mês provenientes da alteração de remuneração da função: " + tupla.getCodFuncaoContrato() + " na data referência: " + vDataReferencia);

                    }

                    for (Date data : datas) {

                        /*Definição da data fim do subperíodo.*/

                        vDataFim = data;

                        /*Definição dos dias contidos no subperíodo*/

                        vDiasSubperiodo = (int) ((ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate())) + 1);

                        if (vMes == 2) {

                            if (vDataFim.toLocalDate().getDayOfMonth() == Date.valueOf(vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth())).toLocalDate().getDayOfMonth()) {

                                if (vDataFim.toLocalDate().getDayOfMonth() == 28) {

                                    vDiasSubperiodo = vDiasSubperiodo + 2;

                                } else {

                                    vDiasSubperiodo = vDiasSubperiodo + 1;

                                }

                            }

                        }

                        /*Define a remuneração do cargo, que não se altera no período.*/

                        vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tupla.getCodFuncaoContrato(), vDataInicio, vDataFim, 2);

                        if (vRemuneracao == 0) {

                            throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                        }

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorFerias = ((vRemuneracao * (vPercentualFerias / 100)) / 30) * vDiasSubperiodo;
                        vValorTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                        vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorFerias = (vValorFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorTercoConstitucional = (vValorTercoConstitucional / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaFerias = (vValorIncidenciaFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaTerco = (vValorIncidenciaTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalFerias = vTotalFerias + vValorFerias;
                        vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                        vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                        vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração na remuneração e nos percentuais.*/

                if (convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Definição da data de início como sendo a data referência (primeiro dia do mês).*/

                    vDataInicio = vDataReferencia;

                    List<Date> datas = new ArrayList<>();

                    try {

                        preparedStatement = connection.prepareStatement("SELECT data_inicio AS data" + " FROM tb_percentual_contrato" + " WHERE cod_contrato = ?" + " AND (MONTH(DATA_INICIO) = ?" + " AND \n" + " YEAR(DATA_INICIO) = ?)" + " UNION" + " SELECT data_fim AS data" + " FROM tb_percentual_contrato" + " WHERE cod_contrato = ?" + " AND (MONTH(DATA_FIM)=?" + " AND" + " YEAR(DATA_FIM) = ?)" + " UNION" + " SELECT data_inicio AS data" + " FROM tb_percentual_estatico" + " WHERE (MONTH(DATA_INICIO)=?" + " AND " + " YEAR(DATA_INICIO)=?)" + " UNION" + " SELECT data_fim AS data" + " FROM tb_percentual_estatico" + " WHERE (MONTH(DATA_FIM)=?" + " AND" + " YEAR(DATA_FIM)=?)" + " UNION" + " SELECT rfc.data_inicio AS data" + " FROM tb_remuneracao_fun_con rfc\n" + " JOIN tb_funcao_contrato fc ON fc.cod = rfc.cod_funcao_contrato" + " WHERE fc.cod_contrato = ?" + " AND fc.cod = ?" + " AND (MONTH(rfc.data_inicio) = ?" + " AND" + " YEAR(rfc.data_inicio) = ?)" + " UNION" + " SELECT rfc.data_fim AS data " + " FROM tb_remuneracao_fun_con rfc" + " JOIN tb_funcao_contrato fc ON fc.cod = rfc.cod_funcao_contrato" + " WHERE fc.cod_contrato = ?" + " AND fc.cod = ?" + " AND (MONTH(rfc.data_fim) = ?" + " AND " + " YEAR(rfc.data_fim) = ?)" + " UNION" + " SELECT CASE WHEN ? = 2 THEN" + " EOMONTH(CONVERT(DATE, CONCAT('28/' , ? , '/' ,?), 103))" + " ELSE" + " CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103) END AS data" + " EXCEPT" + " SELECT CASE WHEN DAY(EOMONTH(CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103))) = 31 THEN" + " CONVERT(DATE, CONCAT('31/' , ? , '/' ,?), 103)" + " ELSE" + " NULL END AS data" + " ORDER BY DATA ASC");

                        preparedStatement.setInt(1, vCodContrato);
                        preparedStatement.setInt(2, vMes);
                        preparedStatement.setInt(3, vAno);
                        preparedStatement.setInt(4, vCodContrato);
                        preparedStatement.setInt(5, vMes);
                        preparedStatement.setInt(6, vAno);
                        preparedStatement.setInt(7, vMes);
                        preparedStatement.setInt(8, vAno);
                        preparedStatement.setInt(9, vMes);
                        preparedStatement.setInt(10, vAno);
                        preparedStatement.setInt(11, vCodContrato);
                        preparedStatement.setInt(12, tupla.getCodFuncaoContrato());
                        preparedStatement.setInt(13, vMes);
                        preparedStatement.setInt(14, vAno);
                        preparedStatement.setInt(15, vCodContrato);
                        preparedStatement.setInt(16, tupla.getCodFuncaoContrato());
                        preparedStatement.setInt(17, vMes);
                        preparedStatement.setInt(18, vAno);
                        preparedStatement.setInt(19, vMes);
                        preparedStatement.setInt(20, vMes);
                        preparedStatement.setInt(21, vAno);
                        preparedStatement.setInt(22, vMes);
                        preparedStatement.setInt(23, vAno);
                        preparedStatement.setInt(24, vMes);
                        preparedStatement.setInt(25, vAno);
                        preparedStatement.setInt(26, vMes);
                        preparedStatement.setInt(27, vAno);
                        resultSet = preparedStatement.executeQuery();

                        while (resultSet.next()) {

                            datas.add(resultSet.getDate("data"));

                        }

                    } catch (SQLException e) {

                        throw new NullPointerException("Não foi possível determinar os subperíodos do mês provenientes da alteração de percentuais e da remuneração da função: " + tupla.getCodFuncaoContrato() + " na data referência: " + vDataReferencia);

                    }

                    for (Date data : datas) {

                        /*Definição da data fim do subperíodo.*/

                        vDataFim = data;

                        /*Definição dos dias contidos no subperíodo*/

                        vDiasSubperiodo = (int) ((ChronoUnit.DAYS.between(vDataInicio.toLocalDate(), vDataFim.toLocalDate())) + 1);

                        if (vMes == 2) {

                            if (vDataFim.toLocalDate().getDayOfMonth() == Date.valueOf(vDataReferencia.toLocalDate().withDayOfMonth(vDataReferencia.toLocalDate().lengthOfMonth())).toLocalDate().getDayOfMonth()) {

                                if (vDataFim.toLocalDate().getDayOfMonth() == 28) {

                                    vDiasSubperiodo = vDiasSubperiodo + 2;

                                } else {

                                    vDiasSubperiodo = vDiasSubperiodo + 1;

                                }

                            }

                        }

                        /*Define a remuneração do cargo, que não se altera no período.*/

                        vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tupla.getCodFuncaoContrato(), vDataInicio, vDataFim, 2);

                        if (vRemuneracao == 0) {

                            throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                        }

                        /*Definição dos percentuais do subperíodo.*/

                        vPercentualFerias = percentual.RetornaPercentualContrato(vCodContrato, 1, vDataInicio, vDataFim, 2);
                        vPercentualTercoConstitucional = percentual.RetornaPercentualContrato(vCodContrato, 2, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorFerias = ((vRemuneracao * (vPercentualFerias / 100)) / 30) * vDiasSubperiodo;
                        vValorTercoConstitucional = ((vRemuneracao * (vPercentualTercoConstitucional / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidenciaFerias = (vValorFerias * (vPercentualIncidencia / 100));
                        vValorIncidenciaTerco = (vValorTercoConstitucional * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorFerias = (vValorFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorTercoConstitucional = (vValorTercoConstitucional / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaFerias = (vValorIncidenciaFerias / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidenciaTerco = (vValorIncidenciaTerco / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalFerias = vTotalFerias + vValorFerias;
                        vTotalTercoConstitucional = vTotalTercoConstitucional + vValorTercoConstitucional;
                        vTotalIncidenciaFerias = vTotalIncidenciaFerias + vValorIncidenciaFerias;
                        vTotalIncidenciaTerco = vTotalIncidenciaTerco + vValorIncidenciaTerco;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }


                }

            }

            if (vMes != 12) {

                vMes = vMes + 1;
            } else {

                vMes = 1;
                vAno = vAno + 1;

            }

            vDataReferencia = Date.valueOf(vAno + "-" + vMes + "-" + "01");

        } while (vDataReferencia.before(pFimPeriodoAquisitivo) || vDataReferencia.equals(pFimPeriodoAquisitivo));

        /*Atribuição de valor a vDiasVendidos*/

        if (pDiasVendidos == 0) {

            vDiasVendidos = 0;

        } else {

            vDiasVendidos = pDiasVendidos;

        }

        /*Dias de férias usufruídos para o cálculo proporcional.*/

        vDiasDeFerias = (ChronoUnit.DAYS.between(pInicioFerias.toLocalDate(), pFimFerias.toLocalDate()) + 1 + vDiasVendidos);

        /*Dias de férias adquiridos no período aquisitivo.*/

        vDiasAdquiridos = ferias.DiasPeriodoAquisitivo(pInicioPeriodoAquisitivo, pFimPeriodoAquisitivo);

        /*Definição do montante proporcional a ser restituído*/

        if ((vDiasDeFerias > vDiasAdquiridos) && !vFeriasMenorDeAno) {

            throw new NullPointerException("Foram concedidos mais dias de férias do que o disponível para o período aquisitivo informado.");

        } else {

            if ((vDiasDeFerias > vDiasAdquiridos) && vFeriasMenorDeAno) vDiasDeFerias = vDiasAdquiridos;

        }

        vTotalFerias = (vTotalFerias/vDiasAdquiridos) * vDiasDeFerias;
        vTotalIncidenciaFerias = (vTotalIncidenciaFerias/vDiasAdquiridos) * vDiasDeFerias;
        vTotalIncidenciaTerco = (vTotalIncidenciaTerco/vDiasAdquiridos) * vDiasDeFerias;

        /*Cancelamento do terço constitucional para parcela diferente da única ou primeira.*/

        if (ferias.ParcelasConcedidas(pCodTerceirizadoContrato, pInicioPeriodoAquisitivo, pFimPeriodoAquisitivo) > 0) {

            vTotalTercoConstitucional = 0;

        }

        return new ValorRestituicaoFeriasModel(vTotalFerias,
                vTotalTercoConstitucional,
                vTotalIncidenciaFerias,
                vTotalIncidenciaTerco);

    }

    /**
     * Método que registra o cálculo do total de férias a ser restituído para um
     * determinado período aquisitivo.
     *
     * @param pCodTerceirizadoContrato;
     * @param pTipoRestituicao;
     * @param pDiasVendidos;
     * @param pInicioFerias;
     * @param pFimFerias;
     * @param pInicioPeriodoAquisitivo;
     * @param pFimPeriodoAquisitivo;
     * @param pValorMovimentado;
     * @param pParcela;
     * @param pLoginAtualizacao;
     */

    public Integer RegistraRestituicaoFerias (int pCodTerceirizadoContrato,
                                              String pTipoRestituicao,
                                              int pDiasVendidos,
                                              Date pInicioFerias,
                                              Date pFimFerias,
                                              Date pInicioPeriodoAquisitivo,
                                              Date pFimPeriodoAquisitivo,
                                              int pParcela,
                                              float pValorMovimentado,
                                              float pTotalFerias,
                                              float pTotalTercoConstitucional,
                                              float pTotalIncidenciaFerias,
                                              float pTotalIncidenciaTerco,
                                              String pLoginAtualizacao) {

        ConsultaTSQL consulta = new ConsultaTSQL(connection);
        InsertTSQL insert = new InsertTSQL(connection);

        /*Chaves primárias.*/

        int vCodTbRestituicaoFerias;

        /*Atribuição do cod do tipo de restituição.*/

        int vCodTipoRestituicao = consulta.RetornaCodTipoRestituicao(pTipoRestituicao);

        /*Variáveis auxiliares.*/

        float vIncidenciaFerias = 0;
        float vIncidenciaTerco = 0;
        float vTerco = 0;
        float vFerias = 0;

        /*Provisionamento da incidência para o saldo residual no caso de movimentação.*/

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            vIncidenciaFerias = pTotalIncidenciaFerias;
            vIncidenciaTerco = pTotalIncidenciaTerco;

            vTerco = pTotalTercoConstitucional;
            vFerias = pTotalFerias;

            pTotalTercoConstitucional = pValorMovimentado/4;
            pTotalFerias = pValorMovimentado - pTotalTercoConstitucional;

            vTerco = vTerco - pTotalTercoConstitucional;
            vFerias = vFerias - pTotalFerias;

            pTotalIncidenciaFerias = 0;
            pTotalIncidenciaTerco = 0;

        }

        /*Gravação no banco*/

        vCodTbRestituicaoFerias = insert.InsertRestituicaoFerias(pCodTerceirizadoContrato,
                vCodTipoRestituicao,
                pDiasVendidos,
                pInicioFerias,
                pFimFerias,
                pInicioPeriodoAquisitivo,
                pFimPeriodoAquisitivo,
                pParcela,
                pTotalFerias,
                pTotalTercoConstitucional,
                pTotalIncidenciaFerias,
                pTotalIncidenciaTerco,
                pLoginAtualizacao);

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            insert.InsertSaldoResidualFerias(vCodTbRestituicaoFerias,
                    vFerias,
                    vTerco,
                    vIncidenciaFerias,
                    vIncidenciaTerco,
                    pLoginAtualizacao);

        }

        return vCodTbRestituicaoFerias;

    }

}