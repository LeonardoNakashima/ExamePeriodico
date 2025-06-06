package com.aula.exameperiodico.database.colaborador;

import java.util.Date;

public class Colaborador {
    private Integer numCracha;
    private String nomeColaborador;
    private Date dataHora;
    private Date inicioAtendimento;
    private Date fimAtendimento;

    public Colaborador() {

    }
    public Colaborador(Integer numCracha, String nome, Date dataHora, Date inicioAtendimento, Date fimAtendimento) {
        this.numCracha = numCracha;
        this.nomeColaborador = nome;
        this.dataHora = dataHora;
        this.inicioAtendimento = inicioAtendimento;
        this.fimAtendimento = fimAtendimento;
    }

    public Integer getNumCracha() {
        return numCracha;
    }

    public void setNumCracha(Integer numCracha) {
        this.numCracha = numCracha;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }

    public void setNomeColaborador(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
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

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }
}
