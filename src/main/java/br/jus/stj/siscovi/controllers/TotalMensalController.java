package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.calculos.TotalMensalAReter;
import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.ContratoDAO;
import br.jus.stj.siscovi.dao.TotalMensalDAO;
import br.jus.stj.siscovi.helpers.ErrorMessage;
import br.jus.stj.siscovi.model.CalculoPendenteModel;
import br.jus.stj.siscovi.model.ListaTotalMensalData;
import br.jus.stj.siscovi.model.TotalMensal;
import br.jus.stj.siscovi.model.TotalMensalPendenteModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.validation.constraints.Null;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;


@Path("/total-mensal-a-reter")
public class TotalMensalController {

    @GET
    @Path("/getValoresRetidos/{codigoContrato}/{codigoUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValoresRetidos(@PathParam("codigoContrato") int codigoContrato, @PathParam("codigoUsuario") int codigoUsuario) {
        Connection connection = new ConnectSQLServer().dbConnect();
        ContratoDAO contratoDAO = new ContratoDAO(connection);
        TotalMensalDAO totalMensalDAO = new TotalMensalDAO(connection);
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd").create();
        String json;
        ArrayList<ListaTotalMensalData> lista = totalMensalDAO.getValoresCalculadosAnteriormente(codigoContrato, contratoDAO.codigoGestorContrato(codigoUsuario, codigoContrato));
        if(lista.size() > 0) {
            if(lista.get(0).getTotaisSize() > 0) {
                json = gson.toJson(lista);
            } else {
                json = gson.toJson(null);
            }
        }else {
            json = gson.toJson(null);
        }
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println(e.getStackTrace());
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/calculaTotalMensal={codigoUsuario}/codigo={codigoContrato}/mes={mes}/ano={ano}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response calcularTotalMensal(@PathParam("codigoUsuario") int codigoUsuario, @PathParam("codigoContrato") int codigoContrato, @PathParam("mes") int mes, @PathParam("ano") int ano){
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();


        Gson gson = new Gson();
        String json = null;
        try {
            new TotalMensalAReter(connectSQLServer.dbConnect()).CalculaTotalMensal(codigoContrato, mes, ano);
            TotalMensalDAO totalMensalDAO = new TotalMensalDAO(connectSQLServer.dbConnect());
            ArrayList<TotalMensal> totais = totalMensalDAO.getCalculoRealizado(new ContratoDAO(connectSQLServer.dbConnect()).codigoGestorContrato(codigoUsuario, codigoContrato), codigoContrato, mes, ano);

            if (totais.size() > 0) {
                json = gson.toJson(totais);
            }
            connectSQLServer.dbConnect().close();
        } catch(NullPointerException npe) {
            System.err.println(npe.toString());
            ErrorMessage error = new ErrorMessage();
            error.error = npe.getMessage();
            json = gson.toJson(error);
        } catch (SQLException e) {
            System.err.println(e.toString());
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    @GET
    @Path("/getValoresPendentes/{codigoContrato}/{codigoUsuario}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getValoresPendentes(@PathParam("codigoContrato") int codigoContrato, @PathParam("codigoUsuario") int codigoUsuario) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        TotalMensalDAO totalMensalDAO = new TotalMensalDAO(connectSQLServer.dbConnect());
        String json = "";
        try {
            ArrayList<TotalMensalPendenteModel> calculosPendentes  = totalMensalDAO.getTotalMensalPendente(codigoContrato, codigoUsuario);
            if(calculosPendentes == null || calculosPendentes.size() == 0) {
                json = gson.toJson(null);
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
           json = gson.toJson(calculosPendentes);

        }catch(RuntimeException rte){
            System.err.println(rte.toString());
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.error = rte.getMessage();
            json = gson.toJson(errorMessage);
            return Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        try {
            connectSQLServer.dbConnect().close();
        }catch (SQLException sqle) {
            System.err.println(sqle.getStackTrace());
            return  Response.status(500).build();
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
