package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.DecimoTerceiroDAO;
import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/decimo-terceiro")

public class DecimoTerceiroController {

    @GET
    @Path("/getTerceirizadoDecimoTerceiro={codigoContrato}/{tipoRestituicao}")
    @Produces(MediaType.APPLICATION_JSON)

    public Response getTerceirizadosParaDecimoTerceiro (@PathParam("codigoContrato") int codigoContrato,
                                                        @PathParam("tipoRestituicao") String tipoRestituicao) {

        Gson gson = new Gson();
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        DecimoTerceiroDAO decimoTerceiroDAO = new DecimoTerceiroDAO(connectSQLServer.dbConnect());
        String json = "";

        if (tipoRestituicao.equals("MOVIMENTAÇÃO")) {

            json = gson.toJson(decimoTerceiroDAO.getListaTerceirizadoParaCalculoDeDecimoTerceiro(codigoContrato));

        }

        return Response.ok(json, MediaType.APPLICATION_JSON).build();

    }

}
