package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.ContratoDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.sql.Connection;


@Path("/total-mensal-a-reter")
public class TotalMensalController {

    @GET
    @Path("/getValoresRetidos={codigo}")
    public Response getValoresCalculados(@PathParam("codigo") int codigo) {
        Connection connection = new ConnectSQLServer().dbConnect();
        ContratoDAO contratoDAO = new ContratoDAO(connection);

        return Response.ok().build();
    }

}
