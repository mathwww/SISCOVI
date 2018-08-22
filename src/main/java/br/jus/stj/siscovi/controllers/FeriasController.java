package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.FeriasDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
