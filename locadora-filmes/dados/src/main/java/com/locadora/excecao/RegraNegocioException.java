package com.locadora.excecao;

/**
 * Exceção lançada quando alguma regra de negócio da locadora é violada.
 * Exemplo: tentar alugar um filme sem cópias disponíveis.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
