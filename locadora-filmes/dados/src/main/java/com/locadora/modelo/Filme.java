package com.locadora.modelo;

import com.locadora.util.Formatador;

import java.util.ArrayList;

/**
 * Filme do acervo da locadora. Possui uma lista de cópias físicas.
 */
public class Filme {

    private int id;
    private String titulo;
    private String genero;
    private int ano;
    private int classificacao; // idade mínima: 0 (livre), 10, 12, 14, 16 ou 18
    private Categoria categoria;
    private boolean ativo;
    private ArrayList<Copia> copias;

    public Filme(int id, String titulo, String genero, int ano, int classificacao, Categoria categoria) {
        this.id = id;
        this.titulo = titulo;
        this.genero = genero;
        this.ano = ano;
        this.classificacao = classificacao;
        this.categoria = categoria;
        this.ativo = true;
        this.copias = new ArrayList<>();
    }

    // ---------- métodos de negócio ----------

    public void adicionarCopia() {
        Copia nova = new Copia(copias.size() + 1, this);
        copias.add(nova);
    }

    public void adicionarCopias(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            adicionarCopia();
        }
    }

    /** Retorna a primeira cópia livre ou null se todas estiverem alugadas. */
    public Copia buscarCopiaDisponivel() {
        for (Copia c : copias) {
            if (c.isDisponivel()) {
                return c;
            }
        }
        return null;
    }

    public int getQuantidadeCopias() {
        return copias.size();
    }

    public int getQuantidadeDisponivel() {
        int total = 0;
        for (Copia c : copias) {
            if (c.isDisponivel()) {
                total++;
            }
        }
        return total;
    }

    public boolean temCopiaAlugada() {
        return getQuantidadeDisponivel() < getQuantidadeCopias();
    }

    /** Soma de quantas vezes todas as cópias foram alugadas. */
    public int getVezesAlugado() {
        int total = 0;
        for (Copia c : copias) {
            total += c.getVezesAlugada();
        }
        return total;
    }

    public double getValorDiaria() {
        return categoria.getValorDiaria();
    }

    public int getPrazoDias() {
        return categoria.getPrazoDias();
    }

    // ---------- getters e setters ----------

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getAno() {
        return ano;
    }

    public int getClassificacao() {
        return classificacao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public ArrayList<Copia> getCopias() {
        return new ArrayList<>(copias);
    }

    @Override
    public String toString() {
        String classif = (classificacao == 0) ? "Livre" : classificacao + " anos";
        return "[" + id + "] " + titulo + " (" + ano + ") | " + genero
                + " | Classif.: " + classif
                + " | " + categoria
                + " | Diária: " + Formatador.moeda(getValorDiaria())
                + " | Cópias livres: " + getQuantidadeDisponivel() + "/" + getQuantidadeCopias()
                + (ativo ? "" : " | INATIVO");
    }
}
