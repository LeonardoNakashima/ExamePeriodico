package com.aula.exameperiodico.recyclerView;

import java.util.Date;

public class ExameMedico {
    private Integer cracha;
    private String nomeColaborador;
    private Date dataHora;
    private Date inicioAtendimento;
    private Date terminoAtendimento;

    public ExameMedico() {}

    public ExameMedico(Integer cracha, String nomeColaborador, Date dataHora, Date inicioAtendimento, Date terminoAtendimento) {
        this.cracha = cracha;
        this.nomeColaborador = nomeColaborador;
        this.dataHora = dataHora;
        this.inicioAtendimento = inicioAtendimento;
        this.terminoAtendimento = terminoAtendimento;
    }

    public Integer getCracha() { return cracha; }
    public void setCracha(Integer cracha) { this.cracha = cracha; }

    public String getNomeColaborador() { return nomeColaborador; }
    public void setNomeColaborador(String nomeColaborador) { this.nomeColaborador = nomeColaborador; }

    public Date getDataHora() { return dataHora; }
    public void setDataHora(Date dataHora) { this.dataHora = dataHora; }

    public Date getInicioAtendimento() { return inicioAtendimento; }
    public void setInicioAtendimento(Date inicioAtendimento) { this.inicioAtendimento = inicioAtendimento; }

    public Date getTerminoAtendimento() { return terminoAtendimento; }
    public void setTerminoAtendimento(Date terminoAtendimento) { this.terminoAtendimento = terminoAtendimento; }
}
