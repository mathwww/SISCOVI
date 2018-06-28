package DAO;
import Model.UsuarioModel;


import java.sql.*;
import java.util.ArrayList;

public class UsuarioDAO {
    private Connection connection;

    public UsuarioDAO( Connection connection){
        this.connection = connection;
    }

    public ArrayList<UsuarioModel> getAllUsers() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<UsuarioModel> usuarios = new ArrayList<UsuarioModel>();
        try {
            preparedStatement = connection.prepareStatement("SELECT U.cod, U.NOME, LOGIN, SIGLA, U.LOGIN_ATUALIZACAO, U.DATA_ATUALIZACAO FROM tb_usuario U" +
                    " JOIN TB_PERFIL_USUARIO P ON P.cod=u.COD_PERFIL");
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                UsuarioModel usuarioModel = new UsuarioModel(resultSet.getInt("COD"),
                        resultSet.getString("NOME"),
                        resultSet.getString("LOGIN"),
                        resultSet.getString("LOGIN_ATUALIZACAO"),
                        resultSet.getDate("DATA_ATUALIZACAO"));
                usuarioModel.setPerfil(resultSet.getString("SIGLA"));
                usuarios.add(usuarioModel);
            }
            return usuarios;
        }catch (NullPointerException npe) {
            npe.printStackTrace();
        }catch (SQLException sqle){
            sqle.printStackTrace();
        }
        return null;
    }

    public String retornaNomeDoGestorDoContrato(int codigoContrato){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try{
            preparedStatement = connection.prepareStatement("SELECT U.NOME FROM TB_USUARIO U JOIN TB_HISTORICO_GESTAO_CONTRATO HGC ON HGC.COD_USUARIO=U.COD JOIN TB_PERFIL_GESTAO PG ON PG.COD=HGC.COD_PERFIL_GESTAO" +
                    " WHERE HGC.COD_CONTRATO=?");
            preparedStatement.setInt(1, codigoContrato);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString("NOME");
            }
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }
    public Boolean existeNome(String nome) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT NOME FROM TB_USUARIO WHERE NOME=?");
            preparedStatement.setString(1, nome);
            resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return true; // Verdadeiro: Existe um usuário com esse nome no Sistema, ou seja, esta pessoa provavelmente já está cadastrada
            }else {
                return false; // Falso: Não existe um usuário com esse nome no Sistema, ou seja, esta pessoa provavelmente está sendo cadastrada pela primeira vez
            }
        }catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return true;
    }
    public Boolean existeLogin(String login) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("SELECT LOGIN FROM TB_USUARIO WHERE LOGIN=?");
            preparedStatement.setString(1, login);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return true;
            }else {
                return false;
            }
        }catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return true;
    }
    public Boolean cadastrarUsuario(UsuarioModel usuario, String password, String currentUser, int codigo) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("INSERT INTO TB_USUARIO(COD_PERFIL, NOME, LOGIN, PASSWORD, LOGIN_ATUALIZACAO, DATA_ATUALIZACAO) VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)");
            preparedStatement.setInt(1, codigo);
            preparedStatement.setString(2, usuario.getNome());
            preparedStatement.setString(3, usuario.getLogin());
            preparedStatement.setString(4, password);
            preparedStatement.setString(5, currentUser.toUpperCase());
            preparedStatement.executeUpdate();
            return true;
        }catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return false;
    }
    public ArrayList<UsuarioModel> getGestores(){
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<UsuarioModel> usuarios = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement("SELECT U.cod, U.NOME, LOGIN, SIGLA, U.LOGIN_ATUALIZACAO, U.DATA_ATUALIZACAO FROM tb_usuario U JOIN TB_PERFIL_USUARIO P ON P.cod=u.COD_PERFIL WHERE P.COD=4");
            resultSet = preparedStatement.executeQuery();
            while(resultSet.next()) {
                UsuarioModel usuarioModel = new UsuarioModel(resultSet.getInt("COD"),
                        resultSet.getString("NOME"),
                        resultSet.getString("LOGIN"),
                        resultSet.getString("LOGIN_ATUALIZACAO"),
                        resultSet.getDate("DATA_ATUALIZACAO"));
                usuarioModel.setPerfil(resultSet.getString("SIGLA"));
                usuarios.add(usuarioModel);
            }
            return usuarios;
        }catch(SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }
}
