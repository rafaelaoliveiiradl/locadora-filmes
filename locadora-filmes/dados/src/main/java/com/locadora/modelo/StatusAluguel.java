package com.locadora.modelo;

/**
 * Situação de um aluguel.
 * ATIVO -> filme com o cliente, dentro do prazo.
 * ATRASADO -> filme com o cliente, prazo vencido.
 * DEVOLVIDO -> filme já devolvido.
 */
public enum StatusAluguel {
    ATIVO,
    ATRASADO,
    DEVOLVIDO
}
