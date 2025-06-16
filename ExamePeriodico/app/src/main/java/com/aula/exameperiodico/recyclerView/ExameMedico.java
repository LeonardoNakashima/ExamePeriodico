package com.aula.exameperiodico.recyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.Serializable; // Adicionar se você planeja passar este objeto entre Activities/Fragments

public class ExameMedico implements Serializable {
    private int numCracha;
    private String nomeColaborador;
    private String dataHora;
    private Date inicioAtendimento;
    private Date terminoAtendimento;
    private Boolean status;
    private String documentId;


    public ExameMedico() {
    }

    // Construtor com todos os campos (exceto documentId, que é gerado pelo Firestore)
    public ExameMedico(int numCracha, String nomeColaborador, String dataHora, Date inicioAtendimento, Date terminoAtendimento, Boolean status) {
        this.numCracha = numCracha;
        this.nomeColaborador = nomeColaborador;
        this.dataHora = dataHora;
        this.inicioAtendimento = inicioAtendimento;
        this.terminoAtendimento = terminoAtendimento;
        this.status = status;
    }

    // --- Getters e Setters para os campos existentes ---

    public int getNumCracha() {
        return numCracha;
    }

    public void setNumCracha(int numCracha) {
        this.numCracha = numCracha;
    }

    public String getNomeColaborador() {
        return nomeColaborador;
    }

    public void setNomeColaborador(String nomeColaborador) {
        this.nomeColaborador = nomeColaborador;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public Date getInicioAtendimento() {
        return inicioAtendimento;
    }

    public void setInicioAtendimento(Date inicioAtendimento) {
        this.inicioAtendimento = inicioAtendimento;
    }

    public Date getTerminoAtendimento() {
        return terminoAtendimento;
    }

    public void setTerminoAtendimento(Date terminoAtendimento) {
        this.terminoAtendimento = terminoAtendimento;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    // --- NOVO: Getter e Setter para documentId ---
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // --- Métodos de formatação de data (já existentes) ---
    private String formatDateSafely(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public String getFormattedInicioAtendimento() {
        return formatDateSafely(inicioAtendimento);
    }

    public String getFormattedTerminoAtendimento() {
        return formatDateSafely(terminoAtendimento);
    }

    @Override
    public String toString() {
        return "ExameMedico{" +
                "numCracha=" + numCracha +
                ", nomeColaborador='" + nomeColaborador + '\'' +
                ", dataHora='" + dataHora + '\'' +
                ", inicioAtendimento=" + inicioAtendimento +
                ", terminoAtendimento=" + terminoAtendimento +
                ", status=" + status +
                ", documentId='" + documentId + '\'' +
                '}';
    }
}