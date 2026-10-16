package com.locadora.modelo;

import com.locadora.excecao.RegraNegocioException;

/**
 * Representa uma cópia física (DVD/Blu-ray) de um filme.
 * Um filme pode ter várias cópias.
 */
public class Copia {

    private int numero;
    private Filme filme;
    private boolean disponivel;
    private int vezesAlugada;

    public Copia(int numero, Filme filme) {
        this.numero = numero;
        this.filme = filme;
        this.disponivel = true;
        this.vezesAlugada = 0;
    }

    public void marcarComoAlugada() {
        if (!disponivel) {
            throw new RegraNegocioException("Esta cópia já está alugada.");
        }
        disponivel = false;
        vezesAlugada++;
    }

    public void marcarComoDevolvida() {
        disponivel = true;
    }

    public int getNumero() {
        return numero;
    }

    public Filme getFilme() {
        return filme;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public int getVezesAlugada() {
        return vezesAlugada;
    }

    @Override
    public String toString() {
        return filme.getTitulo() + " (cópia " + numero + ")";
    }
}
