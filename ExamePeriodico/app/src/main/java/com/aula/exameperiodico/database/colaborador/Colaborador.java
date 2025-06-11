package com.aula.exameperiodico.database.colaborador;

import java.io.Serializable;
import java.util.Date;

public class Colaborador implements Serializable {

    private int numCracha;
    private String nomeColaborador;
    private Date inicioAtendimento;
    private String dataHora;
    private Date fimAtendimento;
    private Boolean status;
    private String documentId;


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

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public Date getFimAtendimento() {
        return fimAtendimento;
    }

    public void setFimAtendimento(Date fimAtendimento) {
        this.fimAtendimento = fimAtendimento;
    }
    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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
