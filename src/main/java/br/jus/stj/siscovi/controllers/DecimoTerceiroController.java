package br.jus.stj.siscovi.controllers;

import br.jus.stj.siscovi.calculos.RestituicaoDecimoTerceiro;
import br.jus.stj.siscovi.dao.ConnectSQLServer;
import br.jus.stj.siscovi.dao.DecimoTerceiroDAO;
import br.jus.stj.siscovi.helpers.ErrorMessage;
import br.jus.stj.siscovi.model.TerceirizadoDecimoTerceiro;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import javax.validation.constraints.Null;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    @POST
    @Path("/calcularDecimoTerceiroTerceirizados")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response calcularDecimoTerceiroListaTerceirizados(String object) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        ArrayList<TerceirizadoDecimoTerceiro> lista = gson.fromJson(object, new TypeToken<List<TerceirizadoDecimoTerceiro>>(){}.getType());
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        RestituicaoDecimoTerceiro restituicaoDecimoTerceiro = new RestituicaoDecimoTerceiro(connectSQLServer.dbConnect());
        String json = "";
        for(TerceirizadoDecimoTerceiro calculo : lista) {
            Date ultimoDiaDoAno = Date.valueOf("" + calculo.getInicioContagem().toLocalDate().getYear() + "-12-31");
            try {
                calculo.setValoresDecimoTerceiro(restituicaoDecimoTerceiro.CalculaRestituicaoDecimoTerceiro(calculo.getCodigoTerceirizadoContrato(), calculo.getParcelas(), calculo.getInicioContagem(), ultimoDiaDoAno));
                calculo.setFimContagem(ultimoDiaDoAno);
            }catch(NullPointerException npe) {
                System.err.println(npe.getStackTrace());
                ErrorMessage error = new ErrorMessage();
                error.error = npe.getMessage();
                json = gson.toJson(error);
                return Response.ok(json, MediaType.APPLICATION_JSON).build();
            }
        }
        json = gson.toJson(lista);
        try {
            connectSQLServer.dbConnect().close();
        }catch(SQLException sqle) {
            System.err.println(sqle.getStackTrace());
            return Response.accepted().status(500).build();
        }
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/registrarCalculoDecimoTerceiro")
    public Response registratCalculoDecimoTerceiro(String object) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        ArrayList<TerceirizadoDecimoTerceiro> lista = gson.fromJson(object, new TypeToken<List<TerceirizadoDecimoTerceiro>>(){}.getType());
        ConnectSQLServer connectSQLServer = new ConnectSQLServer();
        RestituicaoDecimoTerceiro restituicaoDecimoTerceiro = new RestituicaoDecimoTerceiro(connectSQLServer.dbConnect());
        String json = "";
        for(TerceirizadoDecimoTerceiro decimoTerceiro : lista) {
            if(decimoTerceiro.getTipoRestituicao().equals("RESGATE")) {
                try {
                    restituicaoDecimoTerceiro.RegistraRestituicaoDecimoTerceiro(decimoTerceiro.getCodigoTerceirizadoContrato(),
                            decimoTerceiro.getTipoRestituicao(),
                            decimoTerceiro.getParcelas(),
                            decimoTerceiro.getInicioContagem(),
                            decimoTerceiro.getValoresDecimoTerceiro().getValorDecimoTerceiro(),
                            decimoTerceiro.getValoresDecimoTerceiro().getValorIncidenciaDecimoTerceiro(),
                            decimoTerceiro.getValorMovimentado(),
                            decimoTerceiro.getId());
                }catch(NullPointerException npe) {
                    System.err.println(npe.getStackTrace());
                    ErrorMessage error = new ErrorMessage();
                    error.error = "Houve um erro ao tentar registrar o cálculo de Décimo Terceiro";
                    json = gson.toJson(error);
                    return Response.ok(json, MediaType.APPLICATION_JSON).build();
                }
            }else if(decimoTerceiro.getTipoRestituicao().equals("MOVIMENTAÇÃO")) {
                try {
                    restituicaoDecimoTerceiro.RegistraRestituicaoDecimoTerceiro(decimoTerceiro.getCodigoTerceirizadoContrato(),
                            decimoTerceiro.getTipoRestituicao(),
                            decimoTerceiro.getParcelas(),
                            decimoTerceiro.getInicioContagem(),
                            0,
                            0,
                            decimoTerceiro.getValorMovimentado(),
                            decimoTerceiro.getId());
                }catch(NullPointerException npe) {
                    System.err.println(npe.getStackTrace());
                    ErrorMessage error = new ErrorMessage();
                    error.error = "Houve um erro ao tentar registrar o cálculo !";
                    json = gson.toJson(error);
                    return Response.ok(json, MediaType.APPLICATION_JSON).build();
                }
            }
        }
        try {
            connectSQLServer.dbConnect().close();
        }catch (SQLException sqle) {
            System.err.println(sqle.getStackTrace());
            return Response.accepted().status(500).build();
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("success", true);
        json = gson.toJson(jsonObject);
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }
}