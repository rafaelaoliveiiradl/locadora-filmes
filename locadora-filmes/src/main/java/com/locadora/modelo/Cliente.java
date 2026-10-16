package com.locadora.modelo;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.util.Formatador;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;

/**
 * Cliente da locadora. Guarda a lista de todos os aluguéis que já fez.
 */
public class Cliente {

    public static final int LIMITE_ALUGUEIS_ATIVOS = 3;
    public static final int PONTOS_POR_DEVOLUCAO_NO_PRAZO = 10;

    private int id;
    private String nome;
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;
    private boolean ativo;
    private int pontos;
    private ArrayList<Aluguel> alugueis;

    public Cliente(int id, String nome, String cpf, LocalDate dataNascimento, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.ativo = true;
        this.pontos = 0;
        this.alugueis = new ArrayList<>();
    }

    // ---------- métodos de negócio ----------

    public void adicionarAluguel(Aluguel aluguel) {
        alugueis.add(aluguel);
    }

    public int getIdade(LocalDate referencia) {
        return Period.between(dataNascimento, referencia).getYears();
    }

    public int getIdade() {
        return getIdade(LocalDate.now());
    }

    public boolean temIdadeParaFilme(Filme filme, LocalDate referencia) {
        return getIdade(referencia) >= filme.getClassificacao();
    }

    /** Conta os aluguéis que ainda não foram devolvidos. */
    public int contarAlugueisAtivos() {
        int total = 0;
        for (Aluguel a : alugueis) {
            if (a.getStatus() != StatusAluguel.DEVOLVIDO) {
                total++;
            }
        }
        return total;
    }

    public boolean temAtraso(LocalDate hoje) {
        for (Aluguel a : alugueis) {
            if (a.estaAtrasado(hoje)) {
                return true;
            }
        }
        return false;
    }

    public boolean temPagamentoPendente() {
        for (Aluguel a : alugueis) {
            Pagamento p = a.getPagamento();
            if (p != null && !p.isPago()) {
                return true;
            }
        }
        return false;
    }

    /** Desconto de fidelidade: 50 pontos = 10%, 100 pontos ou mais = 20%. */
    public double getPercentualDesconto() {
        if (pontos >= 100) {
            return 20.0;
        }
        if (pontos >= 50) {
            return 10.0;
        }
        return 0.0;
    }

    /** Cliente ganha pontos de fidelidade se devolver o filme no prazo. */
    public void pontuarDevolucao(boolean noPrazo) {
        if (noPrazo) {
            pontos += PONTOS_POR_DEVOLUCAO_NO_PRAZO;
        }
    }

    public void adicionarPontos(int quantidade) {
        pontos += quantidade;
    }

    /**
     * Verifica todas as regras que impedem o cliente de alugar um filme.
     * Lança RegraNegocioException se alguma regra for violada.
     */
    public void validarPodeAlugar(Filme filme, LocalDate data) {
        if (!ativo) {
            throw new RegraNegocioException("Cliente inativo não pode alugar.");
        }
        if (temPagamentoPendente()) {
            throw new RegraNegocioException("Cliente possui pagamento pendente. Quite antes de alugar.");
        }
        if (temAtraso(data)) {
            throw new RegraNegocioException("Cliente possui filme em atraso. Devolva antes de alugar.");
        }
        if (contarAlugueisAtivos() >= LIMITE_ALUGUEIS_ATIVOS) {
            throw new RegraNegocioException("Limite de " + LIMITE_ALUGUEIS_ATIVOS + " aluguéis ativos atingido.");
        }
        if (!temIdadeParaFilme(filme, data)) {
            throw new RegraNegocioException("Cliente não tem idade para este filme (classificação "
                    + filme.getClassificacao() + " anos).");
        }
    }

    // ---------- getters e setters ----------

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getPontos() {
        return pontos;
    }

    public int getTotalAlugueis() {
        return alugueis.size();
    }

    public ArrayList<Aluguel> getAlugueis() {
        return new ArrayList<>(alugueis);
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nome + " | CPF: " + cpf
                + " | Nasc.: " + Formatador.data(dataNascimento)
                + " | Pontos: " + pontos
                + " | " + (ativo ? "Ativo" : "INATIVO");
    }
}
