package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.DecimoTerceiroDAO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("/decimo-terceiro")
public class DecimoTerceiroController {
    @GET
    @Path("/getTerceirizadosDecimoTerceiro={codigoContrato}/{tipoRestituicao}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTerceirizadosParaDecimoTerceiro (@PathParam("codigoContrato") int codigoContrato,
                                                        @PathParam("tipoRestituicao") String tipoRestituicao) {
        Gson gson = new GsonBuilder().setDateFormat("YYYY-MM-dd").create();
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        DecimoTerceiroDAO decimoTerceiroDAO = new DecimoTerceiroDAO(connectSQLServer.dbConnect());
        String json = "";
        json = gson.toJson(decimoTerceiroDAO.getListaTerceirizadoParaCalculoDeDecimoTerceiro(codigoContrato));
        try {
            connectSQLServer.dbConnect().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}