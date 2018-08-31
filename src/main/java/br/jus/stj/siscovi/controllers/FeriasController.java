package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.calculos.Ferias;
import br.jus.stj.siscovi.calculos.RestituicaoFerias;
import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.FeriasDAO;
import br.jus.stj.siscovi.helpers.ErrorMessage;
import br.jus.stj.siscovi.model.CalcularFeriasModel;
import br.jus.stj.siscovi.model.ValorRestituicaoFeriasModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("/ferias")
public class FeriasController {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getTerceirizadosFerias={codigoContrato}/{tipoRestituicao}")
    public Response getTerceirizadosParaFerias(@PathParam("codigoContrato") int codigoContrato, @PathParam("tipoRestituicao") String tipoRestituicao) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        FeriasDAO feriasDAO= new FeriasDAO(connectSQLServer.dbConnect());
        String json = "";
        if(tipoRestituicao.equals("MOVIMENTACAO")) {
           json = gson.toJson(feriasDAO.getListaTerceirizadoParaCalculoDeFerias(codigoContrato));
        }
        try {
            connectSQLServer.dbConnect().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getValorRestituicaoFeriasModel")
    public Response getValoresFeriasTerceirizado(String object) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        CalcularFeriasModel cfm = gson.fromJson(object, CalcularFeriasModel.class);
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        RestituicaoFerias restituicaoFerias = new RestituicaoFerias(connectSQLServer.dbConnect());
        String json = "";
        try {
            if(cfm.getTipoRestituicao().equals("MOVIMENTACAO")) {
                cfm.setTipoRestituicao("MOVIMENTAÇÃO");
            } else if (cfm.getTipoRestituicao().equals("RESGATE")) {

            }
        ValorRestituicaoFeriasModel vrfm = restituicaoFerias.CalculaRestituicaoFerias(cfm.getCodTerceirizadoContrato(),
                cfm.getDiasVendidos(),
                cfm.getInicioFerias(),
                cfm.getFimFerias(),
                cfm.getInicioPeriodoAquisitivo(),
                cfm.getFimPeriodoAquisitivo());
        json = gson.toJson(vrfm);

            connectSQLServer.dbConnect().close();
        } catch (SQLException e) {
            System.err.println(e.toString());
        }catch(NullPointerException npe) {
            System.err.println(npe.toString());
            ErrorMessage error = new ErrorMessage();
            error.error = npe.getMessage();
            json = gson.toJson(error);
            return Response.accepted(json).status(200).build();
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/calcularFeriasTerceirizados")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response calcularFeriasTerceirizados(String object) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        ArrayList<CalcularFeriasModel> listaTerceirizadosParaCalculo = gson.fromJson(object, new ArrayList<CalcularFeriasModel>().getClass());
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        RestituicaoFerias restituicaoFerias = new RestituicaoFerias(connectSQLServer.dbConnect());
        for(CalcularFeriasModel feriasTerceirizado : listaTerceirizadosParaCalculo){
            restituicaoFerias.RegistraRestituicaoFerias(feriasTerceirizado.getCodTerceirizadoContrato(),
            feriasTerceirizado.getTipoRestituicao(),
            feriasTerceirizado.getDiasVendidos(),
            feriasTerceirizado.getInicioFerias(),
            feriasTerceirizado.getFimFerias(),
            feriasTerceirizado.getInicioPeriodoAquisitivo(),
            feriasTerceirizado.getFimPeriodoAquisitivo(),
            feriasTerceirizado.getProporcional(),
            feriasTerceirizado.getValorMovimentado(),
            feriasTerceirizado.getpTotalFerias(),
            feriasTerceirizado.getpTotalTercoConstitucional(),
            feriasTerceirizado.getpTotalIncidenciaFerias(),
                    feriasTerceirizado.getpTotalIncidenciaTerco());
        }
        try {
            connectSQLServer.dbConnect().close();
        } catch (SQLException e) {
            System.err.println(e.toString());
        }
        String json = gson.toJson("'success': " + true);
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
