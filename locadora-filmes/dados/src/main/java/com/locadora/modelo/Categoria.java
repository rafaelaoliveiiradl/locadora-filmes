package com.locadora.modelo;

/**
 * Categoria do filme. Define o valor da diária e o prazo (em dias) do aluguel.
 */
public enum Categoria {
    LANCAMENTO("Lançamento", 8.00, 2),
    CATALOGO("Catálogo", 5.00, 3),
    CLASSICO("Clássico", 3.00, 5);

    private final String descricao;
    private final double valorDiaria;
    private final int prazoDias;

    Categoria(String descricao, double valorDiaria, int prazoDias) {
        this.descricao = descricao;
        this.valorDiaria = valorDiaria;
        this.prazoDias = prazoDias;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getValorDiaria() {
        return valorDiaria;
    }

    public int getPrazoDias() {
        return prazoDias;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
