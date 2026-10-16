package com.locadora.modelo;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.util.Formatador;

import java.time.LocalDate;

/**
 * Cobrança gerada na devolução. Registra valores e se já foi paga.
 * Liga o cliente, o filme (via aluguel) e o valor cobrado.
 */
public class Pagamento {

    private int id;
    private Aluguel aluguel;
    private double valorAluguel;
    private double desconto;
    private double multa;
    private boolean pago;
    private FormaPagamento forma;
    private LocalDate dataPagamento;

    public Pagamento(int id, Aluguel aluguel, double valorAluguel, double desconto, double multa) {
        this.id = id;
        this.aluguel = aluguel;
        this.valorAluguel = valorAluguel;
        this.desconto = desconto;
        this.multa = multa;
        this.pago = false;
    }

    public double getValorTotal() {
        return valorAluguel - desconto + multa;
    }

    public void pagar(FormaPagamento forma, LocalDate data) {
        if (pago) {
            throw new RegraNegocioException("Este pagamento já foi realizado.");
        }
        this.forma = forma;
        this.dataPagamento = data;
        this.pago = true;
    }

    public int getId() {
        return id;
    }

    public Aluguel getAluguel() {
        return aluguel;
    }

    public double getValorAluguel() {
        return valorAluguel;
    }

    public double getDesconto() {
        return desconto;
    }

    public double getMulta() {
        return multa;
    }

    public boolean isPago() {
        return pago;
    }

    public FormaPagamento getForma() {
        return forma;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    @Override
    public String toString() {
        return "Pagamento #" + id + " | Aluguel #" + aluguel.getId()
                + " | " + aluguel.getCliente().getNome()
                + " | " + aluguel.getFilme().getTitulo()
                + " | Aluguel: " + Formatador.moeda(valorAluguel)
                + " | Desconto: " + Formatador.moeda(desconto)
                + " | Multa: " + Formatador.moeda(multa)
                + " | TOTAL: " + Formatador.moeda(getValorTotal())
                + " | " + (pago ? "PAGO (" + forma + ")" : "PENDENTE");
    }
}
