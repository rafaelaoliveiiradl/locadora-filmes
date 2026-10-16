package com.locadora.modelo;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.util.Formatador;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Aluguel de uma cópia por um cliente, registrado por um funcionário.
 * Fluxo: ATIVO -> (ATRASADO) -> DEVOLVIDO -> gera Pagamento.
 */
public class Aluguel {

    public static final double PERCENTUAL_MULTA_POR_DIA = 0.5; // 50% da diária por dia de atraso

    private int id;
    private Cliente cliente;
    private Copia copia;
    private Funcionario funcionario;
    private LocalDate dataAluguel;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucao;
    private StatusAluguel status;
    private boolean renovado;
    private Pagamento pagamento;

    public Aluguel(int id, Cliente cliente, Copia copia, Funcionario funcionario, LocalDate dataAluguel) {
        this.id = id;
        this.cliente = cliente;
        this.copia = copia;
        this.funcionario = funcionario;
        this.dataAluguel = dataAluguel;
        this.dataPrevistaDevolucao = dataAluguel.plusDays(copia.getFilme().getPrazoDias());
        this.status = StatusAluguel.ATIVO;
        this.renovado = false;
    }

    // ---------- regras de negócio ----------

    public Filme getFilme() {
        return copia.getFilme();
    }

    /** Quantidade de dias que o cliente contratou (prazo da categoria + renovações). */
    public long getDiasContratados() {
        return ChronoUnit.DAYS.between(dataAluguel, dataPrevistaDevolucao);
    }

    /** Valor do aluguel = diária da categoria x dias contratados. */
    public double calcularValorBase() {
        return getFilme().getValorDiaria() * getDiasContratados();
    }

    public long calcularDiasAtraso(LocalDate referencia) {
        long dias = ChronoUnit.DAYS.between(dataPrevistaDevolucao, referencia);
        if (dias > 0) {
            return dias;
        }
        return 0;
    }

    /** Multa = dias de atraso x 50% da diária do filme. */
    public double calcularMulta(LocalDate referencia) {
        return calcularDiasAtraso(referencia) * getFilme().getValorDiaria() * PERCENTUAL_MULTA_POR_DIA;
    }

    public boolean estaAtrasado(LocalDate hoje) {
        return status != StatusAluguel.DEVOLVIDO && hoje.isAfter(dataPrevistaDevolucao);
    }

    /** Muda o status automaticamente para ATRASADO quando o prazo vence. */
    public void atualizarStatus(LocalDate hoje) {
        if (status == StatusAluguel.DEVOLVIDO) {
            return;
        }
        if (estaAtrasado(hoje)) {
            status = StatusAluguel.ATRASADO;
        } else {
            status = StatusAluguel.ATIVO;
        }
    }

    /** Renovação: só uma vez, e apenas se o aluguel não estiver atrasado. */
    public void renovar(LocalDate hoje) {
        if (status == StatusAluguel.DEVOLVIDO) {
            throw new RegraNegocioException("Este aluguel já foi devolvido.");
        }
        if (renovado) {
            throw new RegraNegocioException("Este aluguel já foi renovado uma vez.");
        }
        if (estaAtrasado(hoje)) {
            throw new RegraNegocioException("Não é possível renovar um aluguel atrasado.");
        }
        dataPrevistaDevolucao = dataPrevistaDevolucao.plusDays(getFilme().getPrazoDias());
        renovado = true;
    }

    public void devolver(LocalDate data) {
        if (status == StatusAluguel.DEVOLVIDO) {
            throw new RegraNegocioException("Este aluguel já foi devolvido.");
        }
        if (data.isBefore(dataAluguel)) {
            throw new RegraNegocioException("A data de devolução não pode ser anterior à data do aluguel.");
        }
        dataDevolucao = data;
        status = StatusAluguel.DEVOLVIDO;
        copia.marcarComoDevolvida();
    }

    public boolean devolvidoComAtraso() {
        return status == StatusAluguel.DEVOLVIDO && dataDevolucao.isAfter(dataPrevistaDevolucao);
    }

    /** Cria a cobrança do aluguel (valor + multa - desconto de fidelidade). */
    public Pagamento gerarPagamento(int idPagamento) {
        if (status != StatusAluguel.DEVOLVIDO) {
            throw new RegraNegocioException("A cobrança só é gerada após a devolução.");
        }
        double valorBase = calcularValorBase();
        double desconto = valorBase * cliente.getPercentualDesconto() / 100.0;
        double multa = calcularMulta(dataDevolucao);
        pagamento = new Pagamento(idPagamento, this, valorBase, desconto, multa);
        return pagamento;
    }

    // ---------- getters ----------

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Copia getCopia() {
        return copia;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public LocalDate getDataAluguel() {
        return dataAluguel;
    }

    public LocalDate getDataPrevistaDevolucao() {
        return dataPrevistaDevolucao;
    }

    public LocalDate getDataDevolucao() {
        return dataDevolucao;
    }

    public StatusAluguel getStatus() {
        return status;
    }

    public boolean isRenovado() {
        return renovado;
    }

    public Pagamento getPagamento() {
        return pagamento;
    }

    @Override
    public String toString() {
        return "Aluguel #" + id + " | " + cliente.getNome() + " | " + copia
                + " | Alugado: " + Formatador.data(dataAluguel)
                + " | Previsto: " + Formatador.data(dataPrevistaDevolucao)
                + " | Devolvido: " + Formatador.data(dataDevolucao)
                + " | " + status + (renovado ? " (renovado)" : "");
    }
}
