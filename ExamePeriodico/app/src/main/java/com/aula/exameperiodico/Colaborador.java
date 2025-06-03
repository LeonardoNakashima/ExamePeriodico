package com.aula.exameperiodico;

import java.util.Date;

public class Colaborador {
    private Integer numCracha;
    private String nome;
    private Date inicioAtendimento;
    private Date fimAtendimento;

    public Colaborador() {

    }
    public Colaborador(Integer numCracha, String nome, Date inicioAtendimento, Date fimAtendimento) {
        this.numCracha = numCracha;
        this.nome = nome;
        this.inicioAtendimento = inicioAtendimento;
        this.fimAtendimento = fimAtendimento;
    }

    public Integer getNumCracha() {
        return numCracha;
    }

    public void setNumCracha(Integer numCracha) {
        this.numCracha = numCracha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Date getInicioAtendimento() {
        return inicioAtendimento;
    }

    public void setInicioAtendimento(Date inicioAtendimento) {
        this.inicioAtendimento = inicioAtendimento;
    }

    public Date getFimAtendimento() {
        return fimAtendimento;
    }

    public void setFimAtendimento(Date fimAtendimento) {
        this.fimAtendimento = fimAtendimento;
    }
}
