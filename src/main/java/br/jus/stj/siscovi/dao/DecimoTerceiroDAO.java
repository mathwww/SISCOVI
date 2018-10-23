package br.jus.stj.siscovi.dao;

import br.jus.stj.siscovi.calculos.DecimoTerceiro;
import br.jus.stj.siscovi.calculos.Saldo;
import br.jus.stj.siscovi.model.*;
import com.sun.istack.internal.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DecimoTerceiroDAO {
    private Connection connection;

    public DecimoTerceiroDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * @param codigoContrato
     * @return
     */
    public ArrayList<TerceirizadoDecimoTerceiro> getListaTerceirizadoParaCalculoDeDecimoTerceiro(int codigoContrato) {
        ArrayList<TerceirizadoDecimoTerceiro> terceirizados = new ArrayList<>();
        String sql = "SELECT TC.COD, " +
                " T.NOME" +
                " FROM tb_terceirizado_contrato TC " +
                " JOIN tb_terceirizado T ON T.COD = TC.COD_TERCEIRIZADO " +
                " WHERE COD_CONTRATO = ? AND T.ATIVO = 'S'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, codigoContrato);
            DecimoTerceiro decimoTerceiro = new DecimoTerceiro(connection);
            Saldo saldoDecimoTerceiro = new Saldo(connection);
            float vSaldoDecimoTericeiro = 0; //Este saldo é correspondente ao ano da data de início da contagem.
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Date inicioContagem = decimoTerceiro.RetornaDataInicioContagem(resultSet.getInt("COD"));
                    vSaldoDecimoTericeiro = saldoDecimoTerceiro.SaldoContaVinculada(resultSet.getInt("COD"), inicioContagem.toLocalDate().getYear(), 1, 3);
                    TerceirizadoDecimoTerceiro terceirizadoDecimoTerceiro = new TerceirizadoDecimoTerceiro(resultSet.getInt("COD"),
                            resultSet.getString("NOME"),
                            inicioContagem,
                            vSaldoDecimoTericeiro,
                            0);
                    terceirizados.add(terceirizadoDecimoTerceiro);
                }
            }
        } catch (SQLException e) {
            throw new NullPointerException("Nenhum funcionário ativo encontrado para este contrato.");
        }
        return terceirizados;
    }

    public List<DecimoTerceiroPendenteModel> getCalculosPendentes(int codigoContrato, int codigoUsuario) {
        List<DecimoTerceiroPendenteModel> lista = new ArrayList<>();
        int codigo = new UsuarioDAO(connection).verifyPermission(codigoUsuario, codigoContrato);
        int codGestor = new ContratoDAO(connection).codigoGestorContrato(codigoUsuario, codigoContrato);
        String status = "";
        if(codigo == codGestor) {
            String sql = "SELECT RDT.COD_TERCEIRIZADO_CONTRATO, " +
                    " T.NOME as TERCEIRIZADO," +
                    " TR.NOME as TIPO," +
                    " TF.NOME AS \"FUNÇÃO\"," +
                    " RDT.PARCELA," +
                    " RDT.DATA_INICIO_CONTAGEM," +
                    " RDT.VALOR," +
                    " RDT.INCIDENCIA_SUBMODULO_4_1 AS INCIDENCIA," +
                    " RDT.DATA_REFERENCIA," +
                    " RDT.AUTORIZADO" +
                    " FROM tb_terceirizado_contrato TC" +
                    " JOIN tb_terceirizado T ON T.COD = TC.COD_TERCEIRIZADO " +
                    " JOIN tb_restituicao_decimo_terceiro RDT ON RDT.COD_TERCEIRIZADO_CONTRATO =TC.COD_TERCEIRIZADO" +
                    " JOIN tb_tipo_restituicao TR ON TR.COD=RDT.COD_TIPO_RESTITUICAO " +
                    " JOIN tb_funcao_terceirizado FT ON FT.COD_TERCEIRIZADO_CONTRATO= TC.cod" +
                    " JOIN tb_funcao TF  ON TF.COD=FT.COD_FUNCAO_CONTRATO" +
                    " WHERE COD_CONTRATO = ? AND T.ATIVO = 'S' AND (AUTORIZADO IS NULL)";
            try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, codigoContrato);
                try(ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        TerceirizadoDecimoTerceiro terceirizadoDecimoTerceiro = new TerceirizadoDecimoTerceiro(resultSet.getInt("COD_TERCEIRIZADO_CONTRATO"),
                                resultSet.getString("TERCEIRIZADO"), resultSet.getDate("DATA_INICIO_CONTAGEM"),0, 0 );
                        terceirizadoDecimoTerceiro.setTipoRestituicao(resultSet.getString("TIPO"));
                        ValorRestituicaoDecimoTerceiroModel vrdtm = new ValorRestituicaoDecimoTerceiroModel(resultSet.getFloat("VALOR"), resultSet.getFloat("INCIDENCIA"));
                        terceirizadoDecimoTerceiro.setValoresDecimoTerceiro(vrdtm);
                        terceirizadoDecimoTerceiro.setParcelas(resultSet.getInt("PARCELA"));
                        terceirizadoDecimoTerceiro.setNomeCargo(resultSet.getString("FUNÇÃO"));
                        status = avaliaStatus(1, resultSet.getString("AUTORIZADO"), null);
                        DecimoTerceiroPendenteModel decimoTerceiroPendenteModel = new DecimoTerceiroPendenteModel(terceirizadoDecimoTerceiro, status, null);
                        lista.add(decimoTerceiroPendenteModel);
                    }
                }
            }catch (SQLException sqle) {
                System.out.println();
                sqle.printStackTrace();
                throw new  RuntimeException("");
            }
        }
        return lista;
    }

    private String avaliaStatus(int operacao, String autorizado, String restituido) {
        switch (operacao) {
            case 1:
                if(autorizado == null)
                    return "EM ANÁLISE";
            case 2:
                if(autorizado.toUpperCase().equals("N"))
                    return "NEGADDO";
            case 3:
                if(autorizado.toUpperCase().equals("S") && restituido == null)
                    return "";
        }
        return null;
    }

    public boolean salvarAlteracoesCalculo(AvaliacaoDecimoTerceiro avaliacaoDecimoTerceiro) {
        int codigo = new UsuarioDAO(connection).verifyPermission(avaliacaoDecimoTerceiro.getUser().getId(), avaliacaoDecimoTerceiro.getCodigoContrato());
        int codGestor = new ContratoDAO(connection).codigoGestorContrato(avaliacaoDecimoTerceiro.getUser().getId(), avaliacaoDecimoTerceiro.getCodigoContrato());
        if(codGestor == codigo) {
            String sql = "UPDATE TB_RESTITUICAO_DECIMO_TERCEIRO SET ";
        }
        return true;
    }
}