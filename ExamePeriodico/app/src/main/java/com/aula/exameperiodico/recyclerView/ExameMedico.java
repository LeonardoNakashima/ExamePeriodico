package com.aula.exameperiodico.recyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExameMedico {
    private int numCracha;
    private String nomeColaborador;
    private Date dataHora;
    private Date inicioAtendimento;
    private Date terminoAtendimento; // Use 'terminoAtendimento' para consistência com o que você mostrou no adapter

    public ExameMedico() {
        // Construtor vazio necessário para Firebase Firestore
    }

    // Construtor que aceita objetos Date, como recebido do HomeFragment
    public ExameMedico(int numCracha, String nomeColaborador, Date dataHora, Date inicioAtendimento, Date terminoAtendimento) {
        this.numCracha = numCracha;
        this.nomeColaborador = nomeColaborador;
        this.dataHora = dataHora;
        this.inicioAtendimento = inicioAtendimento;
        this.terminoAtendimento = terminoAtendimento;
    }

    // --- Getters e Setters (já devem existir, mas incluindo para clareza) ---
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

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
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

    // --- Métodos de Formatação Segura para uso no Adapter ---
    private String formatDateSafely(Date date) {
        if (date == null) {
            return ""; // Retorna string vazia se a data for null
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    public String getFormattedDataHora() {
        return formatDateSafely(dataHora);
    }

    public String getFormattedInicioAtendimento() {
        return formatDateSafely(inicioAtendimento);
    }

    public String getFormattedTerminoAtendimento() {
        return formatDateSafely(terminoAtendimento);
    }
}
