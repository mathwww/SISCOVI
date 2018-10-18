package br.jus.stj.siscovi.calculos;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.sql.ConsultaTSQL;
import br.jus.stj.siscovi.dao.sql.DeleteTSQL;
import br.jus.stj.siscovi.dao.sql.InsertTSQL;
import br.jus.stj.siscovi.model.ValorRestituicaoRescisaoModel;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TesteRestituicaoRescisao {

    public static void main(String[] args){

        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        PreparedStatement preparedStatement;
        ResultSet resultSet;

        RestituicaoRescisao restituicaoRescisao = new RestituicaoRescisao(connectSQLServer.dbConnect());
        ConsultaTSQL consulta = new ConsultaTSQL(connectSQLServer.dbConnect());
        DeleteTSQL delete = new DeleteTSQL(connectSQLServer.dbConnect());

        int vCodContrato = consulta.RetornaCodContratoAleatorio();
        int vCodTerceirizadoContrato = consulta.RetornaCodTerceirizadoAleatorio(vCodContrato);
        int vRetorno;
        String vTipoRestituicao = String.valueOf("RESGATE");
        String vTipoRescisao = String.valueOf("SEM JUSTA CAUSA");
        String vLoginAtualizacao = String.valueOf("VSSOUSA");
        Date vDataDesligamento = Date.valueOf("2016-12-31");
        Date vDataInicioFerias = Date.valueOf("2016-12-31");

        System.out.print("Dados do teste\nCOD_CONTRATO: " + vCodContrato + " COD_TERCEIRIZADO_CONTRATO: " +
                vCodTerceirizadoContrato + "\n");
        System.out.print("Tipo de restituição: " + vTipoRestituicao + "\n" + "Tipo de rescisão: " + vTipoRescisao
                + "\n" + "Data do desligamento: " + vDataDesligamento + "\n" + "Login usuário: " + vLoginAtualizacao + "\n");

        ValorRestituicaoRescisaoModel restituicao = restituicaoRescisao.CalculaRestituicaoRescisao(vCodTerceirizadoContrato,
                                                                                                    vDataDesligamento);

        System.out.println(restituicao.getValorDecimoTerceiro());
        System.out.println(restituicao.getValorIncidenciaDecimoTerceiro());
        System.out.println(restituicao.getValorFGTSDecimoTerceiro());
        System.out.println(restituicao.getValorFerias());
        System.out.println(restituicao.getValorTerco());
        System.out.println(restituicao.getValorIncidenciaFerias());
        System.out.println(restituicao.getValorIncidenciaTerco());
        System.out.println(restituicao.getValorFerias());
        System.out.println(restituicao.getValorFGTSTerco());
        System.out.println(restituicao.getValorFGTSSalario());

        vRetorno = restituicaoRescisao.RegistrarRestituicaoRescisao(vCodTerceirizadoContrato,
                                                                    vTipoRestituicao,
                                                                    vTipoRescisao,
                                                                    vDataDesligamento,
                                                                    vDataInicioFerias,
                                                                    restituicao.getValorDecimoTerceiro(),
                                                                    restituicao.getValorIncidenciaDecimoTerceiro(),
                                                                    restituicao.getValorFGTSDecimoTerceiro(),
                                                                    restituicao.getValorFerias(),
                                                                    restituicao.getValorTerco(),
                                                                    restituicao.getValorIncidenciaFerias(),
                                                                    restituicao.getValorIncidenciaTerco(),
                                                                    restituicao.getValorFGTSFerias(),
                                                                    restituicao.getValorFGTSTerco(),
                                                                    restituicao.getValorFGTSSalario(),
                                                                    vLoginAtualizacao);

        restituicaoRescisao.RecalculoRestituicaoRescisao(vRetorno, "RESGATE", vTipoRescisao, vDataDesligamento, vDataInicioFerias, 0, 0, 0, 0, 0,0 ,0,0,0, 0, "SYSTEM");

        delete.DeleteHistRestituicaoRescisao(vRetorno);
        delete.DeleteSaldoResidualRescisao(vRetorno);
        delete.DeleteRestituicaoRescisao(vRetorno);

    }

}
