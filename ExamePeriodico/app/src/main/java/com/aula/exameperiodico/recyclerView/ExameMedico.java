package com.aula.exameperiodico.recyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ExameMedico {
    private int numCracha;
    private String nomeColaborador;
    private Date dataHora;
    private Date inicioAtendimento;
    private Date terminoAtendimento;
    public ExameMedico() {
    }

    public ExameMedico(int numCracha, String nomeColaborador, Date dataHora, Date inicioAtendimento, Date terminoAtendimento) {
        this.numCracha = numCracha;
        this.nomeColaborador = nomeColaborador;
        this.dataHora = dataHora;
        this.inicioAtendimento = inicioAtendimento;
        this.terminoAtendimento = terminoAtendimento;
    }

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
