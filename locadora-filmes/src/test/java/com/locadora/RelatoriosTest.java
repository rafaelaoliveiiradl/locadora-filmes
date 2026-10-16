package com.locadora;

import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Categoria;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Filme;
import com.locadora.modelo.FormaPagamento;
import com.locadora.modelo.Funcionario;
import com.locadora.servico.Locadora;
import com.locadora.servico.Relatorios;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Testes dos indicadores e relatórios.
 *
 * Cenário:
 *  - Maria aluga "Novo" e devolve no prazo           -> R$ 16,00 (pago)
 *  - João aluga "Novo" e devolve com 3 dias de atraso -> R$ 16,00 + R$ 12,00 de multa = R$ 28,00 (pago)
 *  - Maria aluga "Clássico" e devolve no prazo        -> R$ 15,00 (pago)
 */
public class RelatoriosTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 1);

    private Locadora locadora;
    private Relatorios relatorios;
    private Funcionario ana;
    private Cliente maria;
    private Cliente joao;
    private Filme novo;
    private Filme classico;

    @BeforeEach
    public void preparar() {
        locadora = new Locadora("Teste");
        relatorios = new Relatorios(locadora);
        ana = locadora.cadastrarFuncionario("Ana", "Atendente");
        maria = locadora.cadastrarCliente("Maria", "11111111111", LocalDate.of(1990, 1, 1), "1");
        joao = locadora.cadastrarCliente("João", "22222222222", LocalDate.of(1985, 1, 1), "2");
        novo = locadora.cadastrarFilme("Novo", "Ação", 2025, 0, Categoria.LANCAMENTO, 1);
        classico = locadora.cadastrarFilme("Clássico", "Drama", 1960, 0, Categoria.CLASSICO, 1);
    }

    private void criarHistorico() {
        Aluguel a1 = locadora.alugarFilme(maria.getId(), novo.getId(), ana.getId(), DIA);
        locadora.devolverFilme(a1.getId(), DIA.plusDays(2));
        locadora.registrarPagamento(a1.getId(), FormaPagamento.PIX, DIA.plusDays(2));

        Aluguel a2 = locadora.alugarFilme(joao.getId(), novo.getId(), ana.getId(), DIA.plusDays(3));
        locadora.devolverFilme(a2.getId(), DIA.plusDays(8)); // previsto DIA+5 -> 3 dias de atraso
        locadora.registrarPagamento(a2.getId(), FormaPagamento.CARTAO, DIA.plusDays(8));

        Aluguel a3 = locadora.alugarFilme(maria.getId(), classico.getId(), ana.getId(), DIA.plusDays(10));
        locadora.devolverFilme(a3.getId(), DIA.plusDays(12));
        locadora.registrarPagamento(a3.getId(), FormaPagamento.DINHEIRO, DIA.plusDays(12));
    }

    @Test
    public void totalArrecadadoESomaDosPagamentos() {
        criarHistorico();
        assertEquals(59.0, relatorios.calcularTotalArrecadado(), 0.001);
    }

    @Test
    public void totalDeMultas() {
        criarHistorico();
        assertEquals(12.0, relatorios.calcularTotalMultas(), 0.001);
    }

    @Test
    public void valorMedioPorAluguel() {
        criarHistorico();
        assertEquals(59.0 / 3, relatorios.calcularValorMedioPorAluguel(), 0.001);
    }

    @Test
    public void mediaESemDadosRetornaZero() {
        assertEquals(0.0, relatorios.calcularValorMedioPorAluguel(), 0.001);
    }

    @Test
    public void maiorPagamento() {
        criarHistorico();
        assertEquals(28.0, relatorios.encontrarMaiorPagamento().getValorTotal(), 0.001);
    }

    @Test
    public void filmeMaisEMenosAlugado() {
        criarHistorico();
        assertEquals("Novo", relatorios.encontrarFilmeMaisAlugado().getTitulo());
        assertEquals("Clássico", relatorios.encontrarFilmeMenosAlugado().getTitulo());
    }

    @Test
    public void rankingDeClientesEFilmes() {
        criarHistorico();
        assertEquals("Maria", relatorios.rankingClientes().get(0).getNome());
        assertEquals("João", relatorios.rankingClientes().get(1).getNome());
        assertEquals("Novo", relatorios.rankingFilmes().get(0).getTitulo());
    }

    @Test
    public void taxaDeAtrasoE1DeCadaTresDevolucoes() {
        criarHistorico();
        assertEquals(100.0 / 3, relatorios.calcularTaxaAtraso(), 0.001);
    }

    @Test
    public void taxaDeOcupacaoConsideraCopiasAlugadas() {
        criarHistorico();
        assertEquals(0.0, relatorios.calcularTaxaOcupacao(), 0.001);

        locadora.alugarFilme(maria.getId(), classico.getId(), ana.getId(), DIA.plusDays(20));
        assertEquals(50.0, relatorios.calcularTaxaOcupacao(), 0.001); // 1 de 2 cópias
    }

    @Test
    public void comissaoEntraNoRankingDeFuncionarios() {
        criarHistorico();
        assertEquals(2.95, ana.calcularComissao(), 0.001); // 5% de R$ 59,00
        assertEquals("Ana", relatorios.rankingFuncionarios().get(0).getNome());
    }

    @Test
    public void semFilmesNaoHaMaisAlugado() {
        Relatorios vazio = new Relatorios(new Locadora("Vazia"));
        assertNull(vazio.encontrarFilmeMaisAlugado());
    }
}
