package com.locadora.modelo;

import java.util.ArrayList;

/**
 * Funcionário que registra os aluguéis. Ganha comissão sobre os pagamentos recebidos.
 */
public class Funcionario {

    public static final double PERCENTUAL_COMISSAO = 0.05; // 5%

    private int id;
    private String nome;
    private String cargo;
    private ArrayList<Aluguel> alugueisRegistrados;

    public Funcionario(int id, String nome, String cargo) {
        this.id = id;
        this.nome = nome;
        this.cargo = cargo;
        this.alugueisRegistrados = new ArrayList<>();
    }

    public void registrarAluguel(Aluguel aluguel) {
        alugueisRegistrados.add(aluguel);
    }

    /** Comissão = 5% de tudo que já foi pago nos aluguéis registrados por ele. */
    public double calcularComissao() {
        double totalPago = 0;
        for (Aluguel a : alugueisRegistrados) {
            Pagamento p = a.getPagamento();
            if (p != null && p.isPago()) {
                totalPago += p.getValorTotal();
            }
        }
        return totalPago * PERCENTUAL_COMISSAO;
    }

    public int getTotalAlugueis() {
        return alugueisRegistrados.size();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public ArrayList<Aluguel> getAlugueisRegistrados() {
        return new ArrayList<>(alugueisRegistrados);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " | " + cargo + " | Aluguéis registrados: " + alugueisRegistrados.size();
    }
}
