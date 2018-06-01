package Model;

import java.sql.Date;
import java.util.ArrayList;

public class CargosFuncionariosModel {
    private FuncionarioModel funcionario;
    private Date dataDisponibilizacao;
    private Date dataDesligamento;

    public void setFuncionario(FuncionarioModel funcionario) {
        this.funcionario = funcionario;
    }

    public void setDataDesligamento(Date dataDesligamento) {
        this.dataDesligamento = dataDesligamento;
    }

    public void setDataDisponibilizacao(Date dataDisponibilizacao) {
        this.dataDisponibilizacao = dataDisponibilizacao;
    }

    public FuncionarioModel getFuncionario() {
        return funcionario;
    }
}
