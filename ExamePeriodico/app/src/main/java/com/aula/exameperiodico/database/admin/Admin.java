package com.aula.exameperiodico.database.admin;

import com.aula.exameperiodico.database.colaborador.Colaborador;

import java.util.Date;

public class Admin extends Colaborador {
    private String senha;

    public Admin() {
        super();
    }
    public Admin(Integer numCracha, String nome, Date inicioAtendimento, Date fimAtendimento, Date dataInsercao, String senha) {
        super(numCracha, nome, inicioAtendimento, fimAtendimento, dataInsercao);
        this.senha = senha;
    }
    public String getSenha() {
        return senha;
    }
    public void setSenha(String senha) {
        this.senha = senha;
    }
}
