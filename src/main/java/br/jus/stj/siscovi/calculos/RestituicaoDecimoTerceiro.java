package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.model.CodFuncaoContratoECodFuncaoTerceirizadoModel;
import br.jus.stj.siscovi.model.ValorRestituicaoDecimoTerceiroModel;
import br.jus.stj.siscovi.dao.sql.*;

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
        ConsultaTSQL consulta = new ConsultaTSQL(connection);

        /*Chaves primárias.*/

        int vCodContrato;

        /*Variáveis totalizadoras de valores.*/

        float vTotalDecimoTerceiro = 0;
        float vTotalIncidencia = 0;

        /*Variáveis de valores parciais.*/

        float vValorDecimoTerceiro;
        float vValorIncidencia;

        /*Variáveis de percentuais.*/

        float vPercentualDecimoTerceiro;
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

        vCodContrato = consulta.RetornaContratoTerceirizado(pCodTerceirizadoContrato);

        /*Define o valor das variáveis vMes e Vano de acordo com a adata de inínio do período aquisitivo.*/

        vMes = pInicioContagem.toLocalDate().getMonthValue();
        vAno = pInicioContagem.toLocalDate().getYear();

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

                    if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                        vValorDecimoTerceiro = (vValorDecimoTerceiro / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);
                        vValorIncidencia = (vValorIncidencia / 30) * periodo.DiasTrabalhadosMes(tupla.getCod(), vMes, vAno);

                    }

                    /*Contabilização do valor calculado.*/

                    vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                    vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

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

                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 30 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidencia = (vValorIncidencia / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

                        vDataInicio = Date.valueOf(vDataFim.toLocalDate().plusDays(1));

                    }

                }

                /*Se existe alteração de remuneração apenas.*/

                if (convencao.ExisteDuplaConvencao(tupla.getCodFuncaoContrato(), vMes, vAno, 2) && !percentual.ExisteMudancaPercentual(vCodContrato, vMes, vAno, 2)) {

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

                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidencia = (vValorIncidencia / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

                        }

                        /*Contabilização do valor calculado.*/

                        vTotalDecimoTerceiro = vTotalDecimoTerceiro + vValorDecimoTerceiro;
                        vTotalIncidencia = vTotalIncidencia + vValorIncidencia;

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

                        vPercentualDecimoTerceiro = percentual.RetornaPercentualContrato(vCodContrato, 3, vDataInicio, vDataFim, 2);
                        vPercentualIncidencia = percentual.RetornaPercentualContrato(vCodContrato, 7, vDataInicio, vDataFim, 2);

                        /*Calculo da porção correspondente ao subperíodo.*/

                        vValorDecimoTerceiro = ((vRemuneracao * (vPercentualDecimoTerceiro / 100)) / 30) * vDiasSubperiodo;
                        vValorIncidencia = (vValorDecimoTerceiro * (vPercentualIncidencia / 100));

                        /*No caso de mudança de função temos um recolhimento proporcional ao dias trabalhados no cargo,
                         situação similar para a retenção proporcional por menos de 14 dias trabalhados.*/

                        if (retencao.ExisteMudancaFuncao(pCodTerceirizadoContrato, vMes, vAno) || !retencao.FuncaoRetencaoIntegral(tupla.getCod(), vMes, vAno)) {

                            vValorDecimoTerceiro = (vValorDecimoTerceiro / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);
                            vValorIncidencia = (vValorIncidencia / vDiasSubperiodo) * periodo.DiasTrabalhadosPeriodo(tupla.getCod(), vDataInicio, vDataFim);

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

        return new ValorRestituicaoDecimoTerceiroModel(vTotalDecimoTerceiro,
                vTotalIncidencia);

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
     * @param pLoginAtualizacao;
     */

    public Integer RegistraRestituicaoDecimoTerceiro (int pCodTerceirizadoContrato,
                                                      String pTipoRestituicao,
                                                      int pNumeroParcela,
                                                      Date pInicioContagem,
                                                      float pValorDecimoTerceiro,
                                                      float pValorIncidencia,
                                                      float pValorMovimentado,
                                                      String pLoginAtualizacao) {

        PreparedStatement preparedStatement;
        ResultSet resultSet;
        ConsultaTSQL consulta = new ConsultaTSQL(connection);
        InsertTSQL insert = new InsertTSQL(connection);

        /*Chaves primárias.*/

        int vCodTbRestituicao13 = 0;
        int vCodTipoRestituicao;

        /*Variáveis auxiliares.*/

        float vValor = 0;
        float vIncidencia = 0;

        /*Atribuição do cod do tipo de restituição.*/

        vCodTipoRestituicao = consulta.RetornaCodTipoRestituicao(pTipoRestituicao);

        if (vCodTipoRestituicao == 0) {

            throw new NullPointerException("Tipo de restituição não encontrada.");

        }

        /*Recuparação do próximo valor da sequência da chave primária da tabela tb_restituicao_decimo_terceiro.*/



        /*No caso de segunda parcela a movimentação gera resíduos referentes ao
         valor do décimo terceiro que é afetado pelos descontos (IRPF, INSS e etc.)*/

        if ((pNumeroParcela == 2 || pNumeroParcela == 0) && (pTipoRestituicao.equals("MOVIMENTAÇÃO"))) {

            vValor = pValorDecimoTerceiro - pValorMovimentado;

            pValorDecimoTerceiro = pValorMovimentado;

        }

        /*Provisionamento da incidência para o saldo residual no caso de movimentação.*/

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            vIncidencia = pValorIncidencia;

            pValorIncidencia = 0;

        }

        /*Gravação no banco*/

        vCodTbRestituicao13 = insert.InsertRestituicaoDecimoTerceiro(pCodTerceirizadoContrato,
                vCodTipoRestituicao,
                pNumeroParcela,
                pInicioContagem,
                pValorDecimoTerceiro,
                pValorIncidencia,
                pLoginAtualizacao);

        if (pTipoRestituicao.equals("MOVIMENTAÇÃO")) {

            insert.InsertSaldoResidualDecimoTerceiro(vCodTbRestituicao13,
                    vValor,
                    vIncidencia,
                    pLoginAtualizacao);

        }

        return vCodTbRestituicao13;

    }

}