package com.aula.exameperiodico.recyclerView;

public class ExameMedico {
    private String cracha;
    private String dataHora;
    private String nomeColaborador;
    private String inicioAtendimento;
    private String terminoAtendimento; // pode ser null

    // Construtor vazio obrigatório para o Firebase
    public ExameMedico() {}

    public ExameMedico(String cracha, String dataHora, String nomeColaborador, String inicioAtendimento, String terminoAtendimento) {
        this.cracha = cracha;
        this.dataHora = dataHora;
        this.nomeColaborador = nomeColaborador;
        this.inicioAtendimento = inicioAtendimento;
        this.terminoAtendimento = terminoAtendimento;
    }

    // Getters e setters
    public String getCracha() { return cracha; }
    public void setCracha(String cracha) { this.cracha = cracha; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getNomeColaborador() { return nomeColaborador; }
    public void setNomeColaborador(String nomeColaborador) { this.nomeColaborador = nomeColaborador; }

    public String getInicioAtendimento() { return inicioAtendimento; }
    public void setInicioAtendimento(String inicioAtendimento) { this.inicioAtendimento = inicioAtendimento; }

    public String getTerminoAtendimento() { return terminoAtendimento; }
    public void setTerminoAtendimento(String terminoAtendimento) { this.terminoAtendimento = terminoAtendimento; }
}

