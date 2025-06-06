package com.aula.exameperiodico.database.admin;

import com.aula.exameperiodico.database.colaborador.Colaborador;

import java.util.Date;

public class Admin extends Colaborador {
    private String senha;

    private String email;

    public Admin() {
        super();
    }

    public Admin(Integer numCracha, String nome, Date inicioAtendimento, Date fimAtendimento, Date dataInsercao, String senha) {
        super(numCracha, nome, inicioAtendimento, fimAtendimento, dataInsercao);

    public Admin(String senha, String email) {
        this.senha = senha;
        this.email = email;
    }

    public Admin(Integer numCracha, String nome, Date inicioAtendimento, Date fimAtendimento, String senha, String email) {
        super(numCracha, nome, inicioAtendimento, fimAtendimento);
        this.senha = senha;
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
