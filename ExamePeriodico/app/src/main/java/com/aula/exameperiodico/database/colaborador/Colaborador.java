package com.aula.exameperiodico.database.colaborador;

import java.io.Serializable;
import java.util.Date;

public class Colaborador implements Serializable {

    private static final long serialVersionUID = 1L;

    private int numCracha;
    private String nomeColaborador;
    private Date inicioAtendimento;
    private Date dataHora;
    private Date fimAtendimento;
    public String getNomeColaborador() {
        return nomeColaborador;
    }

    public void setNomeColaborador(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }

    public Colaborador() {
    }

    public int getNumCracha() {
        return numCracha;
    }

    public void setNumCracha(int numCracha) {
        this.numCracha = numCracha;
    }

    public Date getInicioAtendimento() {
        return inicioAtendimento;
    }

    public void setInicioAtendimento(Date inicioAtendimento) {
        this.inicioAtendimento = inicioAtendimento;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public Date getFimAtendimento() {
        return fimAtendimento;
    }

    public void setFimAtendimento(Date fimAtendimento) {
        this.fimAtendimento = fimAtendimento;
    }

    @Override
    public String toString() {
        return "Colaborador{" +
                "numCracha=" + numCracha +
                ", inicioAtendimento=" + inicioAtendimento +
                ", fimAtendimento=" + fimAtendimento +
                '}';
    }
}
