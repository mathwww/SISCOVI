package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.RescisaoDAO;
import br.jus.stj.siscovi.model.TerceirizadoRescisao;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/rescisao")
public class RescisaoController {
    @GET
    @Path("/getTerceirizadosRescisao={codigoContrato}/{tipoRestituicao}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTerceirizadosParaRescisao (@PathParam("codigoContrato") int codigoContrato,
                                                  @PathParam("tipoRestituicao") String tipoRestituicao) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        RescisaoDAO rescisaoDAO = new RescisaoDAO(connectSQLServer.dbConnect());
        String json = "";
        json = gson.toJson(rescisaoDAO.getListaTerceirizadoParaCalculoDeRescisao(codigoContrato));

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/calculaRescisaoTerceirizados")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response calculaRescisaoTerceirizados(String object) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        List<TerceirizadoRescisao> lista = gson.fromJson(object, new TypeToken<List<TerceirizadoRescisao>>(){}.getType());
        
        return Response.ok().build();
    }
}