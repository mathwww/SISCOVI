package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.model.CodFuncaoContratoECodFuncaoTerceirizadoModel;
import br.jus.stj.siscovi.model.ValorRestituicaoDecimoTerceiroModel;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RestituicaoDecimoTerceiro {

    private Connection connection;

    public RestituicaoDecimoTerceiro (Connection connection) {

        this.connection = connection;

    }

    /**
     * Método que calcula o total de 13° a ser restituído para um
     * determinado período aquisitivo.
     *
     * @param pCodTerceirizadoContrato;
     * @param pNumeroParcela;
     * @param pInicioContagem;
     * @param pFimContagem;
     */

    public ValorRestituicaoDecimoTerceiroModel CalculaRestituicaoDecimoTerceiro (int pCodTerceirizadoContrato,
                                                                                 int pNumeroParcela,
                                                                                 Date pInicioContagem,
                                                                                 Date pFimContagem) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;
        Retencao retencao = new Retencao(connection);
        Percentual percentual = new Percentual(connection);
        Periodos periodo = new Periodos(connection);
        Remuneracao remuneracao = new Remuneracao(connection);

        /*Chaves primárias.*/

        int vCodContrato = 0;

        /*Variáveis totalizadoras de valores.*/

        float vTotalDecimoTerceiro = 0;
        float vTotalIncidencia = 0;

        /*Variáveis de valores parciais.*/

        float vValorDecimoTerceiro = 0;
        float vValorIncidencia = 0;

        /*Variáveis de percentuais.*/

        float vPercentualDecimoTerceiro = 0;
        float vPercentualIncidencia = 0;

        /*Variável de remuneração da função.*/

        float vRemuneracao = 0;

        /*Variáveis de data.*/

        Date vDataReferencia = null;
        Date vDataInicio = null;
        Date vDataFim = null;
        int vAno = 0;
        int vMes = 0;

        /*Variável para a checagem de existência do terceirizado.*/

        int vCheck = 0;

        /*Variáveis de controle.*/

        int vDiasSubperiodo = 0;

        /*Checagem dos parâmetros passados.*/

        if (pInicioContagem == null || pFimContagem == null) {

            throw new NullPointerException("Erro na checagem dos parâmetros.");

        }

        /*Checagem da existência do terceirizado no contrato.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT COUNT(COD) FROM TB_TERCEIRIZADO_CONTRATO WHERE COD=?");

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

        vMes = pInicioContagem.toLocalDate().getMonthValue();
        vAno = pInicioContagem.toLocalDate().getYear();

        /*Definição da data referência (sempre o primeiro dia do mês).*/

        vDataReferencia = Date.valueOf(vAno + "-" + vMes + "-" + "01");

        /*Início da contabilização de férias do período.*/

        do {

            /*Seleciona as funções que o terceirizado ocupou no mês avaliado.*/

            ArrayList<CodFuncaoContratoECodFuncaoTerceirizadoModel> tuplas = selecionaFuncaoContratoEFuncaoTerceirizado(pCodTerceirizadoContrato, vDataReferencia);

            Convencao convencao = new Convencao(connection);

            /*Para cada função que o terceirizado ocupou no mês avaliado.*/

            for (int i = 0; i < tuplas.size(); i++) {

                /*Caso não exista mais de uma remuneração vigente no mês e não tenha havido alteração nos percentuais do contrato ou nos percentuais estáticos.*/

                if (!convencao.ExisteDuplaConvencao(tuplas.get(i).getCodFuncaoContrato(), vMes, vAno, 2) && !percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Define o valor da remuneração da função e dos percentuais do contrato.*/

                    vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tuplas.get(i).getCodFuncaoContrato(), vMes, vAno, 1, 2);
                    vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vMes, vAno, 1, 2);
                    vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vMes, vAno, 1, 2);

                    if (vRemuneracao == 0) {

                        throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                    }

                    /*Cálculo do valor integral correspondente ao mês avaliado.*/

                    vValorDecimoTerceiro = (vRemuneracao * (vPercentualDecimoTerceiro / 100));
                    vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                    /*o caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                     situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                    if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tuplas.get(i).getCod(), vMes, vAno)) {

                        vValorDecimoTerceiro = (vValorDecimoTerceiro / 30) * periodo.DiasTrabalhadosMes(tuplas.get(i).getCod(), vMes, vAno);
                        vValorIncidencia = (vValorIncidencia / 30) * periodo.DiasTrabalhadosMes(tuplas.get(i).getCod(), vMes, vAno);

                    }

                    /*Contabilização do valor calculado.*/

                    vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                    vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

                }

                /*Se existe apenas alteração de percentual no mês.*/

                if (!convencao.ExisteDuplaConvencao(tuplas.get(i).getCodFuncaoContrato(), vMes, vAno, 2) && percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Define a remuneração do cargo, que não se altera no período.*/

                    vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tuplas.get(i).getCodFuncaoContrato(), vMes, vAno, 1, 2);

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

                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 30 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tuplas.get(i).getCod(), vMes, vAno)) {

                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tuplas.get(i).getCod(), vDataInicio, vDataFim);
                            vValorIncidencia = (vValorIncidencia / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tuplas.get(i).getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração de remuneração apenas.*/

                if (convencao.ExisteDuplaConvencao(tuplas.get(i).getCodFuncaoContrato(), vMes, vAno, 2) && !percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

                    /*Definição dos percentuais, que não se alteram no período.*/

                    vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vMes, vAno, 1, 2);
                    vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vMes, vAno, 1, 2);

                    /*Definição da data de início como sendo a data referência (primeiro dia do mês).*/

                    vDataInicio = vDataReferencia;

                    /*Loop contendo das datas das alterações de percentuais que comporão os subperíodos.*/

                    /*Seleciona as datas que compõem os subperíodos gerados pelas alterações de percentual no mês.*/

                    List<Date> datas = new ArrayList<>();

                    try {

                        preparedStatement = connection.prepareStatement("SELECT rfc.data_inicio AS data" + " FROM tb_remuneracao_fun_con rfc\n" + " JOIN tb_funcao_contrato fc ON fc.cod = rfc.cod_funcao_contrato" + " WHERE fc.cod_contrato = ?" + " AND fc.cod = ?" + " AND (MONTH(rfc.data_inicio) = ?" + " AND" + " YEAR(rfc.data_inicio) = ?)" + " UNION" + " SELECT rfc.data_fim AS data " + " FROM tb_remuneracao_fun_con rfc" + " JOIN tb_funcao_contrato fc ON fc.cod = rfc.cod_funcao_contrato" + " WHERE fc.cod_contrato = ?" + " AND fc.cod = ?" + " AND (MONTH(rfc.data_fim) = ?" + " AND " + " YEAR(rfc.data_fim) = ?)" + " UNION" + " SELECT CASE WHEN ? = 2 THEN" + " EOMONTH(CONVERT(DATE, CONCAT('28/' , ? , '/' ,?), 103))" + " ELSE" + " CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103) END AS data" + " EXCEPT" + " SELECT CASE WHEN DAY(EOMONTH(CONVERT(DATE, CONCAT('30/' , ? , '/' ,?), 103))) = 31 THEN" + " CONVERT(DATE, CONCAT('31/' , ? , '/' ,?), 103)" + " ELSE" + " NULL END AS data" + " ORDER BY DATA ASC");

                        preparedStatement.setInt(1, vCodContrato);
                        preparedStatement.setInt(2, tuplas.get(i).getCodFuncaoContrato());
                        preparedStatement.setInt(3, vMes);
                        preparedStatement.setInt(4, vAno);
                        preparedStatement.setInt(5, vCodContrato);
                        preparedStatement.setInt(6, tuplas.get(i).getCodFuncaoContrato());
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

                        throw new NullPointerException("Não foi possível determinar os subperíodos do mês provenientes da alteração de remuneração da função: " + tuplas.get(i).getCodFuncaoContrato() + " na data referência: " + vDataReferencia);

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

                        vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tuplas.get(i).getCodFuncaoContrato(), vDataInicio, vDataFim, 2);

                        if (vRemuneracao == 0) {

                            throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                        }

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tuplas.get(i).getCod(), vMes, vAno)) {

                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tuplas.get(i).getCod(), vDataInicio, vDataFim);
                            vValorIncidencia = (vValorIncidencia / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tuplas.get(i).getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração na remuneração e nos percentuais.*/

                if (convencao.ExisteDuplaConvencao(tuplas.get(i).getCodFuncaoContrato(), vMes, vAno, 2) && percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

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
                        preparedStatement.setInt(12, tuplas.get(i).getCodFuncaoContrato());
                        preparedStatement.setInt(13, vMes);
                        preparedStatement.setInt(14, vAno);
                        preparedStatement.setInt(15, vCodContrato);
                        preparedStatement.setInt(16, tuplas.get(i).getCodFuncaoContrato());
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

                        throw new NullPointerException("Não foi possível determinar os subperíodos do mês provenientes da alteração de percentuais e da remuneração da função: " + tuplas.get(i).getCodFuncaoContrato() + " na data referência: " + vDataReferencia);

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

                        vRemuneracao = remuneracao.RetornaRemuneracaoPeriodo(tuplas.get(i).getCodFuncaoContrato(), vDataInicio, vDataFim, 2);

                        if (vRemuneracao == 0) {

                            throw new NullPointerException("Erro na execução do procedimento: Remuneração não encontrada. Código -20001");

                        }

                        /*Definição dos percentuais do subperíodo.*/

                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tuplas.get(i).getCod(), vMes, vAno)) {

                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tuplas.get(i).getCod(), vDataInicio, vDataFim);
                            vValorIncidencia = (vValorIncidencia / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tuplas.get(i).getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

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

        } while (vDataReferencia.before(pFimContagem) || vDataReferencia.equals(pFimContagem));

        //System.out.println(vTotalDecimoTerceiro);
        //System.out.println(vTotalIncidencia);

        /*No caso de segunda parcela a movimentação gera resíduos referentes ao
         valor do décimo terceiro que é afetado pelos descontos (IRPF, INSS e etc.)*/

        if (pNumeroParcela == 1 || pNumeroParcela == 2) {

            vTotalDecimoTerceiro = vTotalDecimoTerceiro / 2;

            vTotalIncidencia = vTotalIncidencia / 2;

        }

        ValorRestituicaoDecimoTerceiroModel valorRestituicaoDecimoTerceiroModel =
                new ValorRestituicaoDecimoTerceiroModel(vTotalDecimoTerceiro,
                        vTotalIncidencia);

        return valorRestituicaoDecimoTerceiroModel;

    }


    /**
     * Método que registra o calculo do total de férias a ser restituído para um
     * determinado período aquisitivo.
     *
     * @param pCodTerceirizadoContrato;
     * @param pTipoRestituicao;
     * @param pNumeroParcela;
     * @param pValorDecimoTerceiro;
     * @param pValorIncidencia;
     */

    public void RegistraRestituicaoDecimoTerceiro (int pCodTerceirizadoContrato,
                                                   String pTipoRestituicao,
                                                   int pNumeroParcela,
                                                   Date pInicioContagem,
                                                   float pValorDecimoTerceiro,
                                                   float pValorIncidencia,
                                                   float pValorMovimentado) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;

        /*Chaves primárias.*/

        int vCodTbRestituicao13 = 0;
        int vCodTipoRestituicao = 0;

        /*Variáveis auxiliares.*/

        float vValor = 0;
        float vIncidencia = 0;

        /*Atribuição do cod do tipo de restituição.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT COD" + " FROM TB_TIPO_RESTITUICAO" + " WHERE UPPER(nome) = UPPER(?)");

            preparedStatement.setString(1, pTipoRestituicao);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTipoRestituicao = resultSet.getInt(1);

            }

        } catch (SQLException e) {

            e.printStackTrace();

        }

        if (vCodTipoRestituicao == 0) {

            throw new NullPointerException("Tipo de restituição não encontrada.");

        }

        /*Recuparação do próximo valor da sequência da chave primária da tabela tb_restituicao_decimo_terceiro.*/

        try {

            preparedStatement = connection.prepareStatement("SELECT ident_current ('TB_RESTITUICAO_DECIMO_TERCEIRO')");

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {

                vCodTbRestituicao13 = resultSet.getInt(1);
                vCodTbRestituicao13 = vCodTbRestituicao13 + 1;

            }

        } catch (SQLException sqle) {

            throw new NullPointerException("Não foi possível recuperar o número de sequência da chave primária da tabela de restituição de férias.");

        }

        /*No caso de segunda parcela a movimentação gera resíduos referentes ao
         valor do décimo terceiro que é afetado pelos descontos (IRPF, INSS e etc.)*/

        if ((pNumeroParcela == 2 || pNumeroParcela == 0) && (pTipoRestituicao == "MOVIMENTAÇÃO")) {

            vValor = pValorDecimoTerceiro - pValorMovimentado;

            pValorDecimoTerceiro = pValorMovimentado;

        }

        /*Provisionamento da incidência para o saldo residual no caso de movimentação.*/

        if (pTipoRestituicao == "MOVIMENTAÇÃO") {

            vIncidencia = pValorIncidencia;

            pValorIncidencia = 0;

        }

        /*Gravação no banco*/

        try {

            String sql = "SET IDENTITY_INSERT tb_restituicao_decimo_terceiro ON; " +
                    "INSERT INTO TB_RESTITUICAO_DECIMO_TERCEIRO (COD,"+
                    " COD_TERCEIRIZADO_CONTRATO," +
                    " COD_TIPO_RESTITUICAO," +
                    " PARCELA," +
                    " DATA_INICIO_CONTAGEM," +
                    " VALOR," +
                    " INCIDENCIA_SUBMODULO_4_1," +
                    " DATA_REFERENCIA," +
                    " LOGIN_ATUALIZACAO," +
                    " DATA_ATUALIZACAO)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), 'SYSTEM', CURRENT_TIMESTAMP);" +
                    " SET IDENTITY_INSERT tb_restituicao_decimo_terceiro OFF;";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setInt(1, vCodTbRestituicao13);
            preparedStatement.setInt(2, pCodTerceirizadoContrato);
            preparedStatement.setInt(3, vCodTipoRestituicao);
            preparedStatement.setInt(4, pNumeroParcela);
            preparedStatement.setDate(5, pInicioContagem);
            preparedStatement.setFloat(6, pValorDecimoTerceiro);
            preparedStatement.setFloat(7, pValorIncidencia);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException("Erro ao tentar inserir os resultados do cálculo de 13° no banco de dados!");

        }

        if (pTipoRestituicao == "MOVIMENTAÇÃO") {

            try {

                String sql = "INSERT INTO TB_SALDO_RESIDUAL_DEC_TER (COD_RESTITUICAO_DEC_TERCEIRO," +
                        " VALOR," +
                        " INCIDENCIA_SUBMODULO_4_1," +
                        " LOGIN_ATUALIZACAO," +
                        " DATA_ATUALIZACAO)" +
                        " VALUES (?, ?, ?, 'SYSTEM', CURRENT_TIMESTAMP)";

                preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setInt(1, vCodTbRestituicao13);
                preparedStatement.setFloat(2, vValor);
                preparedStatement.setFloat(3, vIncidencia);

                preparedStatement.executeUpdate();

            } catch (SQLException e) {

                e.printStackTrace();

                throw new RuntimeException("Erro ao tentar inserir os resultados do cálculo de 13º no banco de dados!");

            }

        }

    }

    /*Seleção do código da função terceirizado e da função contrato.*/

    ArrayList<CodFuncaoContratoECodFuncaoTerceirizadoModel> selecionaFuncaoContratoEFuncaoTerceirizado (int pCodTerceirizadoContrato, Date pDataReferencia) {

        /*Busca as funções que um funcionário exerceu no mês de cálculo.*/

        ArrayList<CodFuncaoContratoECodFuncaoTerceirizadoModel> tuplas = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ft.cod_funcao_contrato, " +
                "ft.cod" +
                " FROM tb_funcao_terceirizado ft" +
                " WHERE ft.cod_terceirizado_contrato = ?" +
                " AND ((((CONVERT(date, CONVERT(varchar, year(ft.data_inicio)) + '-' + CONVERT(varchar, month(ft.data_inicio)) + '-01')) <= ?)" +
                " AND" +
                " (ft.data_fim >= ?))" +
                " OR" +
                " (((CONVERT(date, CONVERT(varchar, year(ft.data_inicio)) + '-' + CONVERT(varchar, month(ft.data_inicio)) + '-01')) <= ?) " +
                "AND" +
                " (ft.data_fim IS NULL)))")){

            preparedStatement.setInt(1, pCodTerceirizadoContrato);
            preparedStatement.setDate(2, pDataReferencia);
            preparedStatement.setDate(3, pDataReferencia);
            preparedStatement.setDate(4, pDataReferencia);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    CodFuncaoContratoECodFuncaoTerceirizadoModel tupla = new CodFuncaoContratoECodFuncaoTerceirizadoModel(resultSet.getInt("COD"), resultSet.getInt("COD_FUNCAO_CONTRATO"));

                    tuplas.add(tupla);

                }

            }

        } catch(SQLException slqe) {
            //slqe.printStackTrace();
            throw new NullPointerException("Problemas durante a consulta ao banco em relação ao terceirizado: " + pCodTerceirizadoContrato);

        }

        return tuplas;

    }

}