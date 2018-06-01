package Controllers;

import DAO.CargoDAO;
import DAO.ConnectSQLServer;
import DAO.UsuarioDAO;
import Model.CargoModel;
import Model.CargoResponseModel;
import Model.ContratoModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;

@Path("/cargo")
public class CargoController {

    @GET
    @Path("/getAllCargos")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getAllCargos() throws SQLException {
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
        CargoDAO cargoDAO = new CargoDAO(connectSQLServer.dbConnect());
        String json = gson.toJson(cargoDAO.getAllCargos());
        connectSQLServer.dbConnect().close();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    @POST
    @Path("/getCargosDosContratos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCargosDosContratos(String request) throws SQLException {
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        Gson gson = new GsonBuilder().serializeNulls().setDateFormat("dd/MM/yyyy").create();
        CargoDAO cargoDAO = new CargoDAO(connectSQLServer.dbConnect());
        UsuarioDAO usuarioDAO = new UsuarioDAO(connectSQLServer.dbConnect());
        ArrayList<ContratoModel> contratos;
        ArrayList<ContratoModel> contratoModels = new ArrayList<>();
        ArrayList<CargoResponseModel> cargos = new ArrayList<>();
        contratos = gson.fromJson(request, new ArrayList<ContratoModel>().getClass());
        for(int i = 0; i < contratos.size(); i++){
            String temp = gson.toJson(contratos.get(i));
            contratoModels.add(gson.fromJson(temp, ContratoModel.class));
        }
        for (int i = 0; i < contratoModels.size(); i++) {
            CargoResponseModel cargoResponse = new CargoResponseModel();
            cargoResponse.setCargos(cargoDAO.getCargosDeUmContrato(contratoModels.get(i).getCodigo()));
            cargoResponse.setContrato(contratoModels.get(i));
            cargoResponse.setGestor(usuarioDAO.retornaNomeDoGestorDoContrato(contratoModels.get(i).getCodigo()));
            cargos.add(cargoResponse);
        }
        connectSQLServer.dbConnect().close();
        String json = gson.toJson(cargos);
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
