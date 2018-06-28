package Controllers;

import DAO.CargoDAO;
import DAO.ConnectSQLServer;
import DAO.ContratoDAO;
import DAO.UsuarioDAO;
import Model.CadastroCargoModel;
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
        ContratoDAO contratoDAO = new ContratoDAO(connectSQLServer.dbConnect());
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
            cargoResponse.setGestor(contratoDAO.retornaNomeDoGestorDoContrato(contratoModels.get(i).getCodigo()));
            cargos.add(cargoResponse);
        }
        connectSQLServer.dbConnect().close();
        String json = gson.toJson(cargos);
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    @POST
    @Path("/cadastrarCargos")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cadastrarCargos(String object) throws SQLException {
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        Gson gson = new Gson();
        CadastroCargoModel ccm = gson.fromJson(object, CadastroCargoModel.class);
        CargoDAO cargoDAO = new CargoDAO(connectSQLServer.dbConnect());
        String json;
        if(cargoDAO.cadastroCargos(ccm.getCargos(), ccm.getCurrentUser())){
            json = gson.toJson("Cadastro realizado com sucesso !");
        }else {
            json = gson.toJson("Ocorreu Algum erro");
        }
        try {
            connectSQLServer.dbConnect().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}
