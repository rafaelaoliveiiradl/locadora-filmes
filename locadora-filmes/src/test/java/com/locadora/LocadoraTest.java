package com.locadora;

import com.locadora.excecao.RegraNegocioException;
import com.locadora.modelo.Aluguel;
import com.locadora.modelo.Categoria;
import com.locadora.modelo.Cliente;
import com.locadora.modelo.Filme;
import com.locadora.modelo.FormaPagamento;
import com.locadora.modelo.Funcionario;
import com.locadora.modelo.Pagamento;
import com.locadora.modelo.StatusAluguel;
import com.locadora.servico.Locadora;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes das regras de negócio da locadora.
 */
public class LocadoraTest {

    private static final LocalDate DIA = LocalDate.of(2026, 9, 1);

    private Locadora locadora;
    private Funcionario ana;
    private Cliente maria;
    private Cliente pedro; // menor de idade
    private Filme lancamento; // diária 8,00 | prazo 2 dias | livre | 1 cópia
    private Filme adulto;     // diária 3,00 | prazo 5 dias | 18 anos | 1 cópia
    private Filme catalogo;   // diária 5,00 | prazo 3 dias | livre | 5 cópias

    @BeforeEach
    public void preparar() {
        locadora = new Locadora("Locadora de Teste");
        ana = locadora.cadastrarFuncionario("Ana", "Atendente");
        maria = locadora.cadastrarCliente("Maria", "11111111111", LocalDate.of(1990, 1, 1), "9999-9999");
        pedro = locadora.cadastrarCliente("Pedro", "22222222222", LocalDate.of(2015, 1, 1), "8888-8888");
        lancamento = locadora.cadastrarFilme("Filme Novo", "Ação", 2025, 0, Categoria.LANCAMENTO, 1);
        adulto = locadora.cadastrarFilme("Filme Adulto", "Terror", 2000, 18, Categoria.CLASSICO, 1);
        catalogo = locadora.cadastrarFilme("Filme Catálogo", "Drama", 2010, 0, Categoria.CATALOGO, 5);
    }

    // ---------- REGRA 1: estoque ----------

    @Test
    public void alugarMarcaCopiaComoIndisponivel() {
        locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        assertEquals(0, lancamento.getQuantidadeDisponivel());
        assertEquals(1, maria.contarAlugueisAtivos());
    }

    @Test
    public void naoAlugaQuandoNaoHaCopiaDisponivel() {
        Cliente joao = locadora.cadastrarCliente("João", "33333333333", LocalDate.of(1985, 3, 3), "7777");
        locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        assertThrows(RegraNegocioException.class,
                () -> locadora.alugarFilme(joao.getId(), lancamento.getId(), ana.getId(), DIA));
    }

    @Test
    public void devolverLiberaACopia() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        locadora.devolverFilme(a.getId(), DIA.plusDays(1));
        assertEquals(1, lancamento.getQuantidadeDisponivel());
    }

    // ---------- REGRA 2: classificação indicativa ----------

    @Test
    public void menorDeIdadeNaoAlugaFilmeDe18Anos() {
        assertThrows(RegraNegocioException.class,
                () -> locadora.alugarFilme(pedro.getId(), adulto.getId(), ana.getId(), DIA));
    }

    @Test
    public void menorDeIdadeAlugaFilmeLivre() {
        Aluguel a = locadora.alugarFilme(pedro.getId(), catalogo.getId(), ana.getId(), DIA);
        assertNotNull(a);
    }

    // ---------- REGRA 3: limite de aluguéis ativos ----------

    @Test
    public void naoAlugaMaisDoQueOLimiteDeFilmes() {
        locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA);
        locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA);
        locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA);
        assertThrows(RegraNegocioException.class,
                () -> locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA));
    }

    // ---------- REGRA 4: bloqueio por pendência ou atraso ----------

    @Test
    public void pagamentoPendenteBloqueiaNovoAluguel() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        locadora.devolverFilme(a.getId(), DIA.plusDays(1));

        assertThrows(RegraNegocioException.class,
                () -> locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA.plusDays(2)));

        locadora.registrarPagamento(a.getId(), FormaPagamento.PIX, DIA.plusDays(2));
        Aluguel novo = locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA.plusDays(2));
        assertNotNull(novo);
    }

    @Test
    public void filmeAtrasadoBloqueiaNovoAluguel() {
        locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA); // prevista: DIA + 2
        assertThrows(RegraNegocioException.class,
                () -> locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA.plusDays(5)));
    }

    // ---------- REGRA 5: valor por categoria e multa por atraso ----------

    @Test
    public void devolucaoNoPrazoNaoTemMultaEDaPontos() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        Pagamento p = locadora.devolverFilme(a.getId(), DIA.plusDays(2));

        assertEquals(16.0, p.getValorAluguel(), 0.001); // 2 dias x R$ 8,00
        assertEquals(0.0, p.getMulta(), 0.001);
        assertEquals(16.0, p.getValorTotal(), 0.001);
        assertEquals(10, maria.getPontos());
    }

    @Test
    public void devolucaoComAtrasoCalculaMultaENaoDaPontos() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        // prevista: DIA + 2. Devolvido em DIA + 5 => 3 dias de atraso
        Pagamento p = locadora.devolverFilme(a.getId(), DIA.plusDays(5));

        assertEquals(3, a.calcularDiasAtraso(DIA.plusDays(5)));
        assertEquals(12.0, p.getMulta(), 0.001);        // 3 dias x 50% de R$ 8,00
        assertEquals(28.0, p.getValorTotal(), 0.001);   // 16 + 12
        assertEquals(0, maria.getPontos());
    }

    @Test
    public void valorDoAluguelDependeDaCategoria() {
        Aluguel a1 = locadora.alugarFilme(maria.getId(), adulto.getId(), ana.getId(), DIA);
        assertEquals(15.0, a1.calcularValorBase(), 0.001); // 5 dias x R$ 3,00
        Aluguel a2 = locadora.alugarFilme(maria.getId(), catalogo.getId(), ana.getId(), DIA);
        assertEquals(15.0, a2.calcularValorBase(), 0.001); // 3 dias x R$ 5,00
    }

    // ---------- REGRA 6: desconto de fidelidade ----------

    @Test
    public void clienteComPontosGanhaDesconto() {
        maria.adicionarPontos(60); // 10%
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        Pagamento p = locadora.devolverFilme(a.getId(), DIA.plusDays(2));

        assertEquals(1.6, p.getDesconto(), 0.001);
        assertEquals(14.4, p.getValorTotal(), 0.001);
    }

    @Test
    public void descontoMaximoE20PorCento() {
        maria.adicionarPontos(120);
        assertEquals(20.0, maria.getPercentualDesconto(), 0.001);
    }

    // ---------- REGRA 7: status automático ----------

    @Test
    public void statusMudaParaAtrasadoAutomaticamente() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        assertEquals(StatusAluguel.ATIVO, a.getStatus());

        int atrasados = locadora.atualizarStatusAlugueis(DIA.plusDays(3));
        assertEquals(1, atrasados);
        assertEquals(StatusAluguel.ATRASADO, a.getStatus());
    }

    // ---------- REGRA 8: renovação ----------

    @Test
    public void renovacaoAumentaOPrazoSoUmaVez() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        locadora.renovarAluguel(a.getId(), DIA.plusDays(1));

        assertEquals(DIA.plusDays(4), a.getDataPrevistaDevolucao());
        assertEquals(32.0, a.calcularValorBase(), 0.001); // 4 dias x R$ 8,00
        assertThrows(RegraNegocioException.class, () -> locadora.renovarAluguel(a.getId(), DIA.plusDays(1)));
    }

    @Test
    public void naoRenovaAluguelAtrasado() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        assertThrows(RegraNegocioException.class, () -> locadora.renovarAluguel(a.getId(), DIA.plusDays(3)));
    }

    // ---------- REGRA 9: comissão ----------

    @Test
    public void comissaoDoFuncionarioE5PorCentoDoValorPago() {
        Aluguel a = locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        locadora.devolverFilme(a.getId(), DIA.plusDays(2));
        assertEquals(0.0, ana.calcularComissao(), 0.001); // ainda não pagou

        locadora.registrarPagamento(a.getId(), FormaPagamento.DINHEIRO, DIA.plusDays(2));
        assertEquals(0.8, ana.calcularComissao(), 0.001); // 5% de R$ 16,00
    }

    // ---------- desativação ----------

    @Test
    public void naoDesativaClienteComFilmeParaDevolver() {
        locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        assertThrows(RegraNegocioException.class, () -> locadora.desativarCliente(maria.getId()));
    }

    @Test
    public void desativaClienteSemPendencias() {
        locadora.desativarCliente(pedro.getId());
        assertFalse(pedro.isAtivo());
        assertThrows(RegraNegocioException.class,
                () -> locadora.alugarFilme(pedro.getId(), catalogo.getId(), ana.getId(), DIA));
    }

    @Test
    public void naoDesativaFilmeComCopiaAlugada() {
        locadora.alugarFilme(maria.getId(), lancamento.getId(), ana.getId(), DIA);
        assertThrows(RegraNegocioException.class, () -> locadora.desativarFilme(lancamento.getId()));
    }

    // ---------- cadastro, pesquisa e ordenação ----------

    @Test
    public void naoPermiteCpfDuplicado() {
        assertThrows(RegraNegocioException.class,
                () -> locadora.cadastrarCliente("Outra Maria", "111.111.111-11", LocalDate.of(1992, 2, 2), "1"));
    }

    @Test
    public void pesquisaFilmePorTituloEGenero() {
        assertEquals(1, locadora.pesquisarFilmesPorTitulo("adulto").size());
        assertEquals(1, locadora.pesquisarFilmesPorGenero("terror").size());
        assertTrue(locadora.pesquisarFilmesPorTitulo("inexistente").isEmpty());
    }

    @Test
    public void listaFilmesOrdenadosPorTitulo() {
        assertEquals("Filme Adulto", locadora.listarFilmesOrdenadosPorTitulo().get(0).getTitulo());
        assertEquals("Filme Novo", locadora.listarFilmesOrdenadosPorTitulo().get(2).getTitulo());
    }
}
